# ADR 007 — Las flechas de la notificación cambian de sonido

- **Fecha**: 2026-08-27
- **Estado**: aceptada. Sustituye a la decisión de «notificación mínima» de la especificación §5.3.
- **Contexto**: probando la 1.0 ya publicada, en un Galaxy S25.

## Problema

En la tarjeta de medios del sistema aparecían dos cosas que no significaban nada:

1. Una **barra de progreso con su tiempo al lado**. Una cabecera WAV no sabe declarar más de 6,2
   horas, así que el reproductor tenía una duración concreta y toda superficie de medios dibuja
   barra en cuanto la duración es positiva. Esa barra medía la aritmética de la cabecera, no la
   sesión.
2. Una **flecha de «anterior»** apagada, que no hacía nada al pulsarla.

La barra se arregló sola en cuanto la sesión dejó de declarar una duración —que además es la verdad:
el ruido no dura nada—. La flecha no, y ahí estaba el problema de fondo: **ese hueco no lo dibuja la
app**. Las plantillas de medios del sistema reservan cinco huecos alrededor del play, y los pintan
haya o no acción detrás. En AOSP desaparecen si no hay nada; en One UI se quedan, apagados. La
sesión ya no anunciaba saltar de pista desde el commit `ebb492c` —verificado en `dumpsys
media_session`: `actions=17027`, sin `SKIP_TO_PREVIOUS` ni `SKIP_TO_NEXT`— y aun así la flecha
seguía ahí.

O sea: no se puede borrar. Solo se puede **ocupar**.

## Decisión

Ocuparla, y con lo único que esta app tiene sentido que ofrezca en ese sitio: **la flecha atrás y la
adelante pasan al sonido anterior y al siguiente**, en el mismo orden en que la pantalla principal
los muestra —blanco, rosa, marrón, enmascarador—, dando la vuelta en los extremos.

Dar la vuelta no es un adorno: un orden con principio y final dejaría el primer y el último sonido
con una flecha muerta al lado, que es exactamente lo que se estaba arreglando.

El «+15 min» del temporizador se queda, ahora en el hueco secundario de la derecha, y sigue
apareciendo solo mientras hay temporizador (RF-08).

Esto **cambia una decisión escrita**. La especificación decía que ocupar esos huecos con acciones
propias era una decisión de producto tomada en contra, porque la notificación se quedaba mínima. El
argumento se cae al comprobar dos cosas: que el hueco se dibuja igual —así que la elección real no
era «mínima o no», sino «botón útil o botón muerto»—, y que la app tiene **dos propósitos**
(ADR 006). Quien la usa para taparse la oficina sí manipula el móvil, y cambiar de sonido sin abrir
la app le sirve. Quien la usa para dormir no toca nada, y para ese caso las flechas son inertes por
omisión, no por estar apagadas.

## Consecuencias

- Dos comandos de sesión nuevos, `PREVIOUS_SOUND` y `NEXT_SOUND`, sin argumento: quien pulsa la
  flecha no sabe qué sonido hay puesto —el botón se dibujó hace rato— y el único que lo sabe con
  certeza es el servicio.
- Los botones pasan de `setCustomLayout` a **`setMediaButtonPreferences`**, que es la API que
  entiende de huecos (`CommandButton.SLOT_BACK`, `SLOT_FORWARD`, `SLOT_FORWARD_SECONDARY`).
- Dos textos nuevos en los dos idiomas, que además son lo que lee TalkBack sobre esos botones.
- Cambiar de sonido desde la notificación pasa por el mismo camino que cambiarlo desde la pantalla,
  con su crossfade y su reconstrucción de la fuente. No hace falta nada aparte.
- La escena 04 de las capturas de la ficha —el control en la sombra— cambia, y se regenera con el
  pipeline.
- Verificado en emulador API 37: las flechas recorren los cuatro sonidos y dan la vuelta, el «+15
  min» sigue alargando el temporizador y la barra de progreso ya no se dibuja. **Queda por confirmar
  en el S25**, que es donde se vio el problema: es la plantilla de One UI la que decide.
