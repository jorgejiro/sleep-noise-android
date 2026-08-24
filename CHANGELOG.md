# Changelog

Todas las versiones publicadas de Sleep Noise, la más reciente arriba.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado es
[semántico](https://semver.org/lang/es/).

Este fichero es **interno**: se escribe para quien trabaja en el repositorio. Los otros dos textos que
acompañan a cada versión son distintos a propósito — los `string-array` `changelog_*` son para quien
ya tiene la app instalada, y `docs/play-release-notes.md` es para quien todavía no la tiene.

---

## [1.0] — sin publicar

Primera versión.

### Añadido

- Cuatro ruidos sintetizados en el dispositivo en tiempo real, sin ficheros de audio empaquetados:
  blanco gaussiano, rosa, marrón y un **enmascarador** con forma pensada para tapar conversaciones
  —plano hasta 800 Hz y cayendo después—, que pone el 71 % de su energía en la banda de la voz frente
  al 8,6 % del marrón (ADR 006).
- Reproducción en bucle indefinido en un servicio en primer plano con `MediaSession`, que sigue
  sonando con la pantalla apagada, la app en segundo plano y la app fuera de recientes.
- Arranque automático al abrir la app con el último sonido escuchado; el enmascarador a volumen 50
  tras la primera instalación.
- Nivel de salida a -12 dBFS RMS, 6 dB más de lo que daba la primera versión del motor, con limitador
  suave en los cuatro generadores.
- Aro de volumen arrastrable con slider convencional de apoyo, sobre curva perceptual.
- Cambio de sonido en caliente con crossfade de 800 ms.
- Temporizador de apagado con presets de 15 a 120 minutos y valor personalizado de 5 a 600, con fade
  out durante el último minuto y opción de añadir 15 minutos.
- Notificación `MediaStyle` con pausa y ampliar el temporizador. **Pausar termina la sesión**: la
  notificación desaparece y el servicio sale de primer plano, como cerrar la app. La pausa que impone
  el sistema —una llamada, otra app, unos auriculares desenchufados— no cuenta como final y sí
  reanuda.
- Ajustes: reproducir al abrir, idioma, versión, novedades y envío de comentarios.
- Inglés y español, con fallback a inglés para cualquier otro idioma del sistema y cambio desde los
  ajustes de la app.
