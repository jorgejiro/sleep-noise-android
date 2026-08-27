# Changelog

Todas las versiones publicadas de Sleep Noise, la más reciente arriba.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado es
[semántico](https://semver.org/lang/es/).

Este fichero es **interno**: se escribe para quien trabaja en el repositorio. Los otros dos textos que
acompañan a cada versión son distintos a propósito — los `string-array` `changelog_*` son para quien
ya tiene la app instalada, y `docs/play-release-notes.md` es para quien todavía no la tiene.

---

## [1.0.1] — 2026-08-27

Lo que salió al probar en un teléfono real la versión que ya estaba en la tienda. Las dos entradas
son el mismo problema: trastos en la tarjeta de medios del sistema que no significaban nada.

### Cambiado

- **Las flechas de la notificación cambian de sonido**, al anterior y al siguiente, en el orden de la
  pantalla principal y dando la vuelta en los extremos. Antes eran huecos apagados que no hacían nada
  al pulsarlos, y no se pueden quitar: la plantilla del sistema los dibuja haya o no acción detrás
  —en AOSP desaparecen, en One UI se quedan—, así que la elección real era entre un botón útil y un
  botón muerto (ADR 007). Esto revierte la decisión de «notificación mínima» de la especificación
  §5.3.
- El botón de alargar el temporizador se mantiene, ahora en el hueco secundario de la derecha.
- **El temporizador va de diez en diez**: 10, 20, 30, 40, 50 y 60 minutos, más 90 y 120. Los cuartos
  de hora venían de contar en cuartos, no de cómo la gente piensa cuánto tarda en dormirse. Pasada la
  hora se mantienen los saltos gruesos, porque ahí la precisión ya no significa nada y once filas más
  las tendría que pasar de largo quien quiere veinte minutos. La hoja se abre desplegada del todo,
  para que las nueve opciones se vean sin arrastrarla.
- **El botón de alargar pasa de «+15 min» a «+10 min»**, para que siga en la misma rejilla: un
  temporizador de 40 alargado dos veces es una hora.

### Arreglado

- **La notificación ya no dibuja una barra de progreso.** Una cabecera WAV no sabe declarar más de
  6,2 horas, así que el reproductor tenía una duración concreta y toda superficie de medios pinta una
  barra en cuanto la duración es positiva: la barra medía la aritmética de la cabecera, no la sesión.
  La sesión declara ahora duración desconocida, que además es la verdad.

---

## [1.0] — 2026-08-27

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
