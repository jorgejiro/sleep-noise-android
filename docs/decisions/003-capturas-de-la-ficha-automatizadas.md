# ADR 003 — Las capturas de la ficha se generan con un script, no a mano

- **Fecha**: 2026-08-24
- **Estado**: aceptada (diseño; implementación en H9)

## Contexto

Play pide capturas en tres formatos —teléfono, tablet de 7" y tablet de 10"— y la ficha va en dos
idiomas. Con seis escenas por idioma son **36 imágenes**, y hay que rehacerlas cada vez que cambie una
pantalla. Hechas a mano son 36 secuencias de navegación con dos problemas que no avisan:

- **Una captura mala no da error.** Sale negra o a medio pintar, se guarda y se sube.
- **Un idioma se cuela.** Es lo que pasó con el primer juego de capturas de «¡Aquí hay tomate!», y de
  ahí viene este pipeline.

Existe ya un pipeline probado en «¡Bebe agua!» (`docs/store-assets/generar-capturas/`), con sus
lecciones documentadas. Reescribirlo desde cero sería repetir ese aprendizaje pagándolo otra vez.

## Decisión

Portar el pipeline a este repositorio con las escenas de esta app, y **portar antes las lecciones que
siguen aplicando**: esperar por contenido y no por tiempo, coincidencia exacta al buscar texto, el
idioma desde los ajustes de la app y no del sistema, vaciar la sombra antes de capturar la
notificación, y `revisar.py` obligatorio antes de subir.

Los scripts se escriben en H9, cuando haya app que navegar. El diseño, las seis escenas y las
diferencias respecto a Bebe Agua están ya en
[`docs/store-assets/generar-capturas/README.md`](../store-assets/generar-capturas/README.md).

## Consecuencias

Tres cosas de esta app simplifican el pipeline heredado, y una lo complica:

- **No hay siembra de datos**, porque no hay base de datos. Desaparece el `run-as` que obligaba a
  capturar con el APK debug y que con el de release dejaba el pase entero en verde con las capturas
  vacías. Aquí se puede capturar con el AAB de release, que es lo que se publica.
- **La notificación no hay que provocarla**: existe mientras suena. Desaparece todo el enredo de
  mover el reloj del emulador hasta la alarma pendiente.
- **El nombre de la app es el mismo en los dos idiomas**, así que la limitación del idioma del
  sistema dentro de la notificación deja de afectar a la ficha española.
- **Y lo que se complica: el halo animado rompería el control de idioma.** `revisar.py` detecta un
  idioma colado comprobando que la captura española y la inglesa de la misma escena no sean
  idénticas. El halo respira con ciclo de 9 s, así que nunca lo serían y el control pasaría siempre,
  perdiendo justo la comprobación que motivó el pipeline. La solución es desactivar las animaciones
  del sistema durante el pase: la app respeta `animator_duration_scale` a 0 y para el halo, que es un
  requisito de accesibilidad que ya teníamos por otro motivo. Las capturas quedan además
  reproducibles píxel a píxel.

## Referencias

- `docs/store-assets/generar-capturas/README.md` — diseño y lecciones portadas
- `docs/play-store-publication-texts.md` — orden de las capturas en la ficha
- Pipeline de origen: `bebe-agua-android/docs/store-assets/generar-capturas/`
