# Changelog

Todas las versiones publicadas de Sleep Noise, la más reciente arriba.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado es
[semántico](https://semver.org/lang/es/).

Este fichero es **interno**: se escribe para quien trabaja en el repositorio. Los otros dos textos que
acompañan a cada versión son distintos a propósito — los `string-array` `changelog_*` son para quien
ya tiene la app instalada, y `docs/play-release-notes.md` es para quien todavía no la tiene.

---

## [1.1.0] — 2026-09-08

Dos evolutivos. El primero tapa el ruido que la app dejaba pasar: el del propio teléfono.

### Añadido

- **No molestar mientras suena** (RF-22), activado por defecto. El teléfono se silencia al empezar el
  ruido y vuelve a la normalidad al pararlo, en cada pausa incluida la del sistema —una llamada
  entrante que no se oye es peor que una noche de ruido interrumpida—. El filtro es
  `INTERRUPTION_FILTER_PRIORITY` y no `NONE`, para que las alarmas sigan pasando: una app para dormir
  que hace que la gente no se levante no tiene defensa. La app **solo devuelve el silencio que ella
  cogió**, así que pausar no puede apagar el No molestar de quien lo activó él (ADR 008).
- **Una hoja explicativa, una sola vez, al primer sonido**, que pide el acceso a No molestar. Es un
  acceso especial: no hay diálogo de permiso que pedir, lo concede el usuario a mano en una pantalla
  del sistema, y el intent que lleva directo a la fila de esta app es `@SystemApi`. No es un
  onboarding —la especificación §2 lo deja fuera— sino el mismo sitio y el mismo razonamiento que ya
  estaba escrito para el permiso de notificaciones: se pide donde significa algo. Descartarla cuenta
  como respuesta y no vuelve a aparecer.
- **Interruptor en Ajustes**, con su fila de «conceder acceso» al lado mientras falte. Apagarlo con el
  ruido ya sonando devuelve el teléfono en ese momento, no en la sesión siguiente.
- Miga de pan en DataStore (`dnd_held`) para que una sesión que muere sin ejecutar la devolución
  —memoria, «forzar detención»— no deje el teléfono mudo hasta que alguien lo descubra.

### Cambiado

- **El temporizador va de cinco en cinco hasta la media hora**: 10, 15, 20, 25 y 30 minutos, y
  después 40, 50, 60, 90 y 120. La rejilla se hace más gruesa a medida que crecen los números porque
  así funciona la elección: quien pide veinte minutos está midiendo algo que nota —una siesta en el
  tren, lo que tarda en caerse—, y cinco arriba o abajo son la diferencia. Pasada la hora ya no
  significan nada. El botón de la notificación sigue añadiendo 10, que sigue en la rejilla.

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
