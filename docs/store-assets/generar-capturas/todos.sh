#!/bin/bash
# Los tres formatos, en un solo emulador.
#
#   ./todos.sh
#
# No hacen falta tres AVD: capturar.py fija tamano y densidad con `wm size` y
# `wm density`, que es justo lo que Play mira. Tres emuladores serian tres arranques
# y tres oportunidades de que algo se quede a medias.
#
# Requiere un emulador arrancado con la app instalada. Al final pasa revisar.py, que
# es lo que decide si el juego vale.
set -euo pipefail
cd "$(dirname "$0")"

APK="${APK:-../../../app/build/outputs/apk/debug/app-debug.apk}"
ADB="${ADB:-adb}"

if [ -f "$APK" ]; then
  $ADB install -r "$APK"
fi

for fmt in telefono tablet7 tablet10; do
  echo "== $fmt =="
  # Datos limpios en cada formato: asi las capturas ensenan siempre el sonido que
  # viene puesto y no el que quedara de la tanda anterior.
  $ADB shell pm clear com.jjrapps.sleepnoise >/dev/null
  rm -rf ../capturas/*/"$fmt"
  python3 capturar.py ../capturas --formato "$fmt" --sin-restaurar
done

# El ultimo pase deja el tamano cambiado; se devuelve al del dispositivo.
$ADB shell wm size reset
$ADB shell wm density reset

python3 revisar.py ../capturas
