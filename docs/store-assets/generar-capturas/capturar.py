# -*- coding: utf-8 -*-
"""Prepara el dispositivo y hace las dos tandas.

Uso:

    python3 capturar.py ../capturas --formato telefono
    python3 capturar.py ../capturas --formato tablet7 --idioma es

Las capturas se guardan como **`<raiz>/<idioma>/<formato>/`**, con el idioma
primero. No es un detalle de orden: Play Console pide los recursos por ficha de
idioma, y cada ficha lleva sus capturas de telefono, tablet de 7" y tablet de 10".
Con el idioma en el primer nivel, actualizar la ficha de un idioma es abrir una
carpeta; con el formato primero habia que ir picoteando por tres.
"""
import argparse
import os
import sys
import time

import tanda
import ui
from ui import Device

# Play pide 9:16 exacto en las capturas de tablet, con lados de 320-3840 px en la de
# 7" y de 1080-7680 px en la de 10". Las medidas las fija este script y no el AVD:
# los config.ini dicen otra cosa y no hay que depender de lo que quedara guardado.
#
# Las densidades de 288 dpi no son casualidad: dejan la tablet pequena en 600 dp de
# ancho y la grande en 800, que son los dos umbrales con los que Android decide que
# algo es una tablet.
FORMATS = {
    "telefono": {"size": "1080x2400", "density": 420},
    "tablet7": {"size": "1080x1920", "density": 288},
    "tablet10": {"size": "1440x2560", "density": 288},
}


def prepare(device, fmt):
    print("  preparando el dispositivo (%s)" % fmt)
    spec = FORMATS[fmt]
    device.shell("wm", "size", spec["size"])
    device.shell("wm", "density", str(spec["density"]))

    # La app es oscura siempre, pero la sombra de notificaciones sigue el tema del
    # sistema: sin esto, la escena 04 sale con un panel blanco alrededor de una app
    # negra.
    device.shell("cmd", "uimode", "night", "yes")

    # Las animaciones fuera, y por dos motivos. Uno: el halo de la pantalla principal
    # respira con un ciclo de nueve segundos, asi que cada captura lo cogeria en un
    # punto distinto. Y dos, que es el importante: revisar.py detecta un idioma colado
    # comprobando que la captura `es` y la `en` de la misma escena NO sean identicas,
    # y con el halo animado nunca lo serian — el control pasaria siempre y dejaria de
    # detectar justo el fallo que motivo este pipeline.
    for setting in ("window_animation_scale", "transition_animation_scale",
                    "animator_duration_scale"):
        device.shell("settings", "put", "global", setting, "0")

    # Formato de 24 horas: en la ficha espanola un «10:42 PM» canta.
    device.shell("settings", "put", "system", "time_12_24", "24")
    device.shell("input", "keyevent", "KEYCODE_WAKEUP")
    device.shell("wm", "dismiss-keyguard")

    # El permiso concedido antes de empezar. La app suena sin el —comprobado— pero
    # el dialogo del sistema se planta encima de la primera captura, y una captura
    # de la ficha con un dialogo de permisos delante no ensena la app: ensena Android.
    device.shell("pm", "grant", ui.PACKAGE, "android.permission.POST_NOTIFICATIONS")

    # Cambiar tamano y densidad reinicia el SystemUI. Sin esta pausa, el primer
    # arranque de la app cae en medio de ese reinicio y se pierde.
    time.sleep(5)


def restore(device):
    device.shell("wm", "size", "reset")
    device.shell("wm", "density", "reset")
    for setting in ("window_animation_scale", "transition_animation_scale",
                    "animator_duration_scale"):
        device.shell("settings", "put", "global", setting, "1")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("destino", help="raiz de las capturas, p. ej. ../capturas")
    parser.add_argument("--formato", choices=sorted(FORMATS), default="telefono")
    parser.add_argument("--idioma", choices=("es", "en"), action="append",
                        help="solo este idioma; se puede repetir")
    parser.add_argument("--serial", help="dispositivo concreto")
    parser.add_argument("--sin-restaurar", action="store_true",
                        help="deja el tamano y las animaciones como los dejo el pase")
    args = parser.parse_args()

    languages = args.idioma or ["es", "en"]
    device = Device(args.serial)
    prepare(device, args.formato)

    total = 0
    for language in languages:
        out_dir = os.path.join(args.destino, language, args.formato)
        os.makedirs(out_dir, exist_ok=True)
        print("  tanda en %s -> %s" % (language, out_dir))
        total += len(tanda.run(device, language, out_dir))

    if not args.sin_restaurar:
        restore(device)
    print("  %d capturas" % total)
    return 0


if __name__ == "__main__":
    sys.exit(main())
