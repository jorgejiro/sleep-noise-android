# -*- coding: utf-8 -*-
"""Las siete escenas de la ficha, en un idioma.

El idioma se pone **desde la pantalla de Ajustes de la app**, no con
`cmd locale set-app-locales`: la app aplica su propia preferencia al arrancar y
sobreescribe el override del sistema. Comprobado en vivo — con el override puesto,
la app seguia saliendo en el idioma anterior.
"""
import time

from ui import Device

# Cada escena en los dos idiomas. Las claves son las etiquetas que hay que tocar o
# esperar; tenerlas aqui evita repartir cadenas por todo el fichero.
LABELS = {
    "es": {
        "settings": "Abrir ajustes",
        "language": "Idioma",
        "language_value": "Español",
        "close": "Cerrar",
        "timer_row": "Temporizador",
        "timer_90": "1 h 30 min",
        "timer_none": "Sin temporizador",
        "brown": "Marrón",
        "masking": "Máscara",
        "masking_title": "Ruido enmascarador",
        "brown_title": "Ruido marrón",
        "changelog": "Novedades",
    },
    "en": {
        "settings": "Open settings",
        "language": "Language",
        "language_value": "English",
        "close": "Close",
        "timer_row": "Sleep timer",
        "timer_90": "1 h 30 min",
        "timer_none": "No timer",
        "brown": "Brown",
        "masking": "Masking",
        "masking_title": "Masking noise",
        "brown_title": "Brown noise",
        "changelog": "What's new",
    },
}

SCENES = [
    "01-suena-enmascarador",
    "02-elegir-temporizador",
    "03-temporizador-activo",
    "04-control-en-la-notificacion",
    "05-ruido-marron",
    "06-ajustes",
    "07-novedades",
]


# Solo las del dialogo de permisos. La primera version incluia «OK» y «Aceptar», y
# eso es demasiado generico: con la sombra abierta de un pase anterior, el toque cayo
# en los ajustes rapidos y el pase acabo dentro de los ajustes de Bluetooth del
# sistema, tocando botones a ciegas.
PERMISSION_BUTTONS = ("Allow", "Permitir", "Don’t allow", "No permitir")


def dismiss_system_dialogs(device):
    """Cierra el dialogo de permisos si el sistema lo ha puesto delante.

    Con el permiso ya concedido no deberia salir, pero un dialogo del sistema en una
    captura de la ficha ensena Android y no la app.
    """
    for label in PERMISSION_BUTTONS:
        node = device.find(label)
        if node:
            x, y = node["center"]
            device.shell("input", "tap", str(x), str(y))
            time.sleep(1)
            return True
    return False


def set_language(device, language):
    """Deja la app en el idioma pedido, entrando por Ajustes."""
    # El estado se sanea antes de empezar: una sombra abierta o una hoja a medias de
    # un pase anterior desvia los toques a sitios donde no hay que tocar nada.
    device.close_shade()
    device.home()
    time.sleep(1)
    device.launch_and_play()
    dismiss_system_dialogs(device)
    # Se busca el engranaje por su descripcion en los dos idiomas, porque al empezar
    # no se sabe en cual esta la app.
    for label in (LABELS["es"]["settings"], LABELS["en"]["settings"]):
        if device.find(label):
            device.tap(label)
            break
    else:
        raise RuntimeError("no se encontro el boton de ajustes en ningun idioma")

    for label in (LABELS["es"]["language"], LABELS["en"]["language"]):
        if device.find(label):
            device.tap(label)
            break
    device.tap(LABELS[language]["language_value"])
    time.sleep(2)                      # el cambio de idioma recrea la actividad
    for label in (LABELS["es"]["close"], LABELS["en"]["close"]):
        if device.find(label):
            device.tap(label)
            break
    device.back()
    device.wait_for(LABELS[language]["timer_row"])


def run(device, language, out_dir):
    """Las siete escenas, en orden, dejando la app como la encontro."""
    words = LABELS[language]
    shots = []

    def shot(name):
        time.sleep(1)                  # un respiro para la ultima animacion de estado
        path = "%s/%s.png" % (out_dir, name)
        device.screenshot(path)
        shots.append(path)
        print("    %s" % name)

    set_language(device, language)

    # 01 — la pantalla principal con el sonido que viene puesto.
    device.wait_for(words["masking_title"])
    shot(SCENES[0])

    # 02 — la hoja del temporizador.
    device.tap(words["timer_row"])
    device.wait_for(words["timer_90"])
    shot(SCENES[1])

    # 03 — la cuenta atras corriendo. 90 minutos y captura inmediata: cualquier otro
    # valor depende de cuanto tarde el pase, y dos capturas de la misma escena con
    # tiempos distintos cantan al ponerlas juntas en la ficha.
    device.tap(words["timer_90"])
    device.wait_for(words["timer_90"])
    shot(SCENES[2])

    # 04 — el control en la sombra.
    device.clear_other_notifications()
    device.open_shade()
    device.wait_for(words["masking_title"])
    shot(SCENES[3])
    device.close_shade()

    # 05 — otro sonido, para que se vea que hay donde elegir.
    device.tap(words["brown"])
    device.wait_for(words["brown_title"])
    shot(SCENES[4])

    # 06 y 07 — ajustes y novedades.
    device.tap(words["settings"])
    device.wait_for(words["language"])
    shot(SCENES[5])
    device.tap(words["changelog"])
    time.sleep(1.5)
    shot(SCENES[6])
    device.back()
    device.back()

    # Se deja el enmascarador puesto y sin temporizador, que es como estaba.
    device.tap(words["masking"])
    device.tap(words["timer_row"])
    device.tap(words["timer_none"])
    return shots
