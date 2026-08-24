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

- Ruido blanco gaussiano y ruido marrón, sintetizados en el dispositivo en tiempo real, sin ficheros
  de audio empaquetados.
- Reproducción en bucle indefinido en un servicio en primer plano con `MediaSession`, que sigue
  sonando con la pantalla apagada, la app en segundo plano y la app fuera de recientes.
- Arranque automático al abrir la app con el último sonido escuchado; ruido marrón a volumen 50 tras
  la primera instalación.
- Aro de volumen arrastrable con slider convencional de apoyo, sobre curva perceptual.
- Cambio de sonido en caliente con crossfade de 800 ms.
- Temporizador de apagado con presets de 15 a 120 minutos y valor personalizado de 5 a 600, con fade
  out durante el último minuto y opción de añadir 15 minutos.
- Notificación `MediaStyle` con pausa, parar y ampliar el temporizador.
- Ajustes: reproducir al abrir, idioma, versión, novedades y envío de comentarios.
- Inglés y español, con fallback a inglés para cualquier otro idioma del sistema y cambio desde los
  ajustes de la app.
