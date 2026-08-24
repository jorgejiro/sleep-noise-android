# -*- coding: utf-8 -*-
"""Lo minimo para conducir la app desde fuera: encontrar cosas por su texto y
esperar a que aparezcan.

Dos reglas que vienen de haberlas roto:

- **Tocar por texto, nunca por coordenadas.** Un toque por coordenadas falla la
  mitad de las veces y no sobrevive a cambiar la resolucion, que es justo lo que
  hace este pipeline tres veces seguidas.
- **Esperar por contenido, nunca por tiempo.** Con un `sleep` fijo las capturas
  salen a medio pintar, y una tablet no tarda lo mismo que un telefono en componer.
"""
import re
import subprocess
import time

PACKAGE = "com.jjrapps.sleepnoise"
ACTIVITY = PACKAGE + "/.MainActivity"


class Device:
    """Un dispositivo, y solo uno.

    El serie se resuelve al construir en vez de dejarlo en None. Con mas de un
    dispositivo enchufado —un emulador y un movil de pruebas, que es lo normal— un
    `adb shell` sin `-s` falla con «more than one device» y devuelve **cadena
    vacia**, no un error. Todo lo que pregunte este pipeline contesta entonces que
    no, para siempre: se veia como «la app no llego a sonar» mientras la app estaba
    sonando delante de las narices.
    """

    def __init__(self, serial=None):
        self.serial = serial or self._only_device()

    @staticmethod
    def _only_device():
        out = subprocess.run(["adb", "devices"], capture_output=True, text=True).stdout
        devices = [line.split()[0] for line in out.split("\n")[1:]
                   if line.strip() and line.split()[-1] == "device"]
        if not devices:
            raise RuntimeError("no hay ningun dispositivo conectado")
        if len(devices) > 1:
            emulators = [d for d in devices if d.startswith("emulator-")]
            if len(emulators) == 1:
                print("  (hay %d dispositivos, se usa el emulador %s; --serial para otro)"
                      % (len(devices), emulators[0]))
                return emulators[0]
            raise RuntimeError(
                "hay varios dispositivos y no se sabe cual usar: %s. Pasa --serial"
                % ", ".join(devices))
        return devices[0]

    def _adb(self, *args, binary=False):
        cmd = ["adb"]
        if self.serial:
            cmd += ["-s", self.serial]
        cmd += list(args)
        result = subprocess.run(cmd, capture_output=True)
        return result.stdout if binary else result.stdout.decode("utf-8", "replace")

    def shell(self, *args):
        return self._adb("shell", *args)

    # ------------------------------------------------------------------ jerarquia

    def nodes(self):
        """Todos los nodos con texto o descripcion, con su centro."""
        self.shell("uiautomator", "dump", "/sdcard/ui.xml")
        xml = self.shell("cat", "/sdcard/ui.xml")
        found = []
        for node in re.finditer(r"<node[^>]*>", xml):
            tag = node.group(0)
            text = (re.search(r'text="([^"]*)"', tag) or [None, ""])[1]
            desc = (re.search(r'content-desc="([^"]*)"', tag) or [None, ""])[1]
            bounds = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
            if (text or desc) and bounds:
                x1, y1, x2, y2 = map(int, bounds.groups())
                found.append({
                    "text": text,
                    "desc": desc,
                    "center": ((x1 + x2) // 2, (y1 + y2) // 2),
                    "bounds": (x1, y1, x2, y2),
                })
        return found

    def find(self, label, exact=True):
        """Busca por texto o descripcion.

        `exact` por defecto y no por casualidad: «Blanco» esta dentro de «Ruido
        blanco», y «Rosa» dentro de «Ruido rosa». Buscar por subcadena toca el
        nombre del sonido en vez de su boton.
        """
        for node in self.nodes():
            if exact:
                if label in (node["text"], node["desc"]):
                    return node
            elif label in node["text"] or label in node["desc"]:
                return node
        return None

    def wait_for(self, label, exact=True, timeout=25):
        for _ in range(timeout * 2):
            node = self.find(label, exact)
            if node:
                return node
            time.sleep(0.5)
        raise TimeoutError("no aparecio: %r" % label)

    def tap(self, label, exact=True, timeout=25):
        node = self.wait_for(label, exact, timeout)
        x, y = node["center"]
        self.shell("input", "tap", str(x), str(y))
        return node

    def back(self):
        self.shell("input", "keyevent", "KEYCODE_BACK")

    def home(self):
        self.shell("input", "keyevent", "KEYCODE_HOME")

    # ---------------------------------------------------------------- capturas

    def screenshot(self, path):
        png = self._adb("exec-out", "screencap", "-p", binary=True)
        if not png.startswith(b"\x89PNG"):
            raise RuntimeError("screencap no devolvio un PNG: %r" % png[:80])
        with open(path, "wb") as handle:
            handle.write(png)
        return len(png)

    # -------------------------------------------------------------------- app

    def is_playing(self):
        """¿Hay una pista de audio de media sonando?

        El filtro corre **dentro** del dispositivo. `dumpsys audio` vuelca miles de
        lineas, y preguntarlo noventa veces seguidas desde fuera satura al emulador
        hasta el punto de retrasar el arranque de la propia app que se esta
        esperando: el bucle de espera se convertia en la causa de que no llegara a
        sonar. Asi solo viaja un numero.
        """
        out = self.shell("dumpsys audio | grep -c 'state:started.*USAGE_MEDIA'")
        digits = "".join(ch for ch in out if ch.isdigit())
        return bool(digits) and int(digits) > 0

    def wait_until_playing(self, timeout=30):
        for _ in range(timeout):
            if self.is_playing():
                return True
            time.sleep(1)
        raise TimeoutError("la app no llego a sonar")

    def restart_app(self):
        self.shell("am", "force-stop", PACKAGE)
        self.shell("am", "start", "-n", ACTIVITY)

    def launch_and_play(self, patience=45, attempts=2):
        """Arranca la app y espera a que suene.

        La paciencia es larga a proposito. Justo despues de cambiar el tamano o la
        densidad de pantalla el sistema esta reasentandose y la app tarda bastante
        mas de lo normal en llegar a sonar.

        Y el reintento **no** vuelve a hacer force-stop: la primera version de este
        metodo reintentaba cada quince segundos matando el proceso antes de cada
        intento, con lo que abortaba el arranque que estaba en marcha y no llegaba
        nunca. Cuatro reintentos fallaban donde un solo intento con paciencia
        funcionaba.
        """
        self.restart_app()
        for attempt in range(attempts):
            for _ in range(patience // 2):
                if self.is_playing():
                    return True
                time.sleep(2)      # sin prisa: preguntar mas a menudo lo empeora
            if attempt + 1 < attempts:
                print("      (la app no sonaba tras %d s, se relanza sin matarla)" % patience)
                self.shell("am", "start", "-n", ACTIVITY)
        raise TimeoutError("la app no llego a sonar en %d s" % (patience * attempts))

    def clear_data(self):
        self.shell("pm", "clear", PACKAGE)

    def open_shade(self):
        self.shell("cmd", "statusbar", "expand-notifications")

    def close_shade(self):
        self.shell("cmd", "statusbar", "collapse")

    def clear_other_notifications(self):
        """Aparta de la sombra todo lo que no sea nuestro.

        Con `cmd notification snooze`, que es lo unico que funciona: no existe un
        `dismiss`, el `service call notification 1` que se suele citar no vacia nada
        en Android moderno, y tocar «Borrar todo» exige que la sombra se abra, cosa
        que no siempre ocurre a la primera. Posponerlas diez minutos deja la sombra
        limpia el tiempo que dura el pase y no borra nada del dispositivo.

        La clave de una notificacion lleva barras verticales, asi que **va
        entrecomillada**: sin comillas, el shell del dispositivo la parte en tuberias
        y responde «inaccessible or not found» por cada trozo.

        Sin esto, en la captura de la sombra sale lo que hubiera: en la primera tanda
        aparecieron dos llamadas perdidas de una prueba y un recordatorio de otra app
        del mismo autor.
        """
        listing = self.shell("cmd", "notification", "list")
        apart = 0
        for key in listing.split("\n"):
            key = key.strip()
            if not key or PACKAGE in key:
                continue
            self.shell("cmd notification snooze --for 600000 '%s'" % key)
            apart += 1
        if apart:
            time.sleep(1.5)
        return apart
