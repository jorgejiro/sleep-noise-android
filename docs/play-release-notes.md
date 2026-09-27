# Novedades para Google Play — Sleep Noise

Textos de **«Novedades»** («What's new») listos para pegar en Play Console al crear la release:
*Producción → Crear nueva versión → Notas de la versión*.

- **Límite de Google Play: 500 caracteres por idioma.** Cada bloque indica los que ocupa, calculados
  por `scripts/generar-textos-ficha.py`.
- La ficha permanente (nombre, descripciones, capturas, cuestionarios) está en
  [`play-store-publication-texts.md`](play-store-publication-texts.md). Este fichero es solo el texto
  que cambia en cada publicación.
- Las viñetas salen del `CHANGELOG.md` y de los `string-array` `changelog_*`, pero **no son el mismo
  texto**: aquí se escribe para alguien que todavía no tiene la versión, así que se omite lo interno
  y se recuerda que la app no usa internet.
- Al publicar una versión nueva, su bloque va arriba y los anteriores se quedan como historial.
- **Cada bloque lleva SIEMPRE tres subsecciones**, en este orden: `es-ES`, `en-US` y **`Formato con
  etiquetas de idioma`**. La tercera repite los dos textos envueltos en `<es-ES>` y `<en-US>` en un
  único bloque, que es lo que Play Console acepta de una sola pegada. Sin ella hay que copiar idioma
  por idioma, así que un bloque con solo las dos primeras está incompleto.

---

## 1.1.1 (versionCode 4) — pendiente de publicar

### es-ES (482 caracteres)

```text
El teléfono ya no te despierta él, y el botón de pausa ya se ve como uno.

• No molestar se activa solo mientras suena el ruido, y se desactiva al parar. Las alarmas siguen pasando. Android pide conceder ese acceso a mano; la app te lo explica una vez.
• El temporizador va de cinco en cinco hasta la media hora: 10, 15, 20, 25 y 30, y después 40, 50, 60, 90 y 120.
• Arreglado el icono de pausa, que se leía como un cuadrado.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
```

### en-US (472 caracteres)

```text
Now your phone stops waking you up too, and the pause button looks like one again.

• Do Not Disturb switches on by itself while the noise plays, and off when you stop. Alarms still get through. Android needs that access granted by hand; the app explains it once.
• The sleep timer counts in fives up to half an hour: 10, 15, 20, 25 and 30, then 40, 50, 60, 90 and 120.
• Fixed the pause icon, which used to read like a square.

No accounts, no cloud, no ads, no tracking.
```

### Formato con etiquetas de idioma

```xml
<es-ES>
El teléfono ya no te despierta él, y el botón de pausa ya se ve como uno.

• No molestar se activa solo mientras suena el ruido, y se desactiva al parar. Las alarmas siguen pasando. Android pide conceder ese acceso a mano; la app te lo explica una vez.
• El temporizador va de cinco en cinco hasta la media hora: 10, 15, 20, 25 y 30, y después 40, 50, 60, 90 y 120.
• Arreglado el icono de pausa, que se leía como un cuadrado.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
</es-ES>
<en-US>
Now your phone stops waking you up too, and the pause button looks like one again.

• Do Not Disturb switches on by itself while the noise plays, and off when you stop. Alarms still get through. Android needs that access granted by hand; the app explains it once.
• The sleep timer counts in fives up to half an hour: 10, 15, 20, 25 and 30, then 40, 50, 60, 90 and 120.
• Fixed the pause icon, which used to read like a square.

No accounts, no cloud, no ads, no tracking.
</en-US>
```

---

## 1.1.0 (versionCode 3) — nunca enviada a Play; sus dos evolutivos van dentro de la 1.1.1

### es-ES (463 caracteres)

```text
El teléfono ya no te despierta él.

• No molestar se activa solo mientras suena el ruido, y se desactiva cuando lo paras. Las alarmas siguen pasando.
• Android pide conceder ese acceso a mano: la app te lo explica una vez y te lleva a la pantalla. También hay un interruptor en Ajustes.
• El temporizador va de cinco en cinco hasta la media hora: 10, 15, 20, 25 y 30 minutos, y después 40, 50, 60, 90 y 120.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
```

### en-US (447 caracteres)

```text
Now your phone stops waking you up too.

• Do Not Disturb switches on by itself while the noise plays, and off when you stop it. Alarms still get through.
• Android needs that access granted by hand: the app explains it once and takes you to the screen. There is a switch in Settings too.
• The sleep timer counts in fives up to half an hour: 10, 15, 20, 25 and 30 minutes, then 40, 50, 60, 90 and 120.

No accounts, no cloud, no ads, no tracking.
```

### Formato con etiquetas de idioma

```xml
<es-ES>
El teléfono ya no te despierta él.

• No molestar se activa solo mientras suena el ruido, y se desactiva cuando lo paras. Las alarmas siguen pasando.
• Android pide conceder ese acceso a mano: la app te lo explica una vez y te lleva a la pantalla. También hay un interruptor en Ajustes.
• El temporizador va de cinco en cinco hasta la media hora: 10, 15, 20, 25 y 30 minutos, y después 40, 50, 60, 90 y 120.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
</es-ES>
<en-US>
Now your phone stops waking you up too.

• Do Not Disturb switches on by itself while the noise plays, and off when you stop it. Alarms still get through.
• Android needs that access granted by hand: the app explains it once and takes you to the screen. There is a switch in Settings too.
• The sleep timer counts in fives up to half an hour: 10, 15, 20, 25 and 30 minutes, then 40, 50, 60, 90 and 120.

No accounts, no cloud, no ads, no tracking.
</en-US>
```

---

## 1.0.1 (versionCode 2) — publicada

### es-ES (403 caracteres)

```text
Lo que salió al usar la app en un teléfono de verdad.

• Las flechas de la notificación ya sirven para algo: pasan al sonido anterior y al siguiente, sin abrir la app.
• Fuera la barra de progreso, que avanzaba hacia un final que no existe: el ruido no se acaba.
• El temporizador va de diez en diez: 10, 20, 30, 40, 50 y 60 minutos, más 90 y 120.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
```

### en-US (402 caracteres)

```text
What came out of using the app on a real phone.

• The arrows in the notification now do something: they move to the previous or next sound, without opening the app.
• The progress bar is gone. It was creeping towards an end that does not exist: the noise never finishes.
• The sleep timer counts in tens: 10, 20, 30, 40, 50 and 60 minutes, plus 90 and 120.

No accounts, no cloud, no ads, no tracking.
```

### Formato con etiquetas de idioma

```xml
<es-ES>
Lo que salió al usar la app en un teléfono de verdad.

• Las flechas de la notificación ya sirven para algo: pasan al sonido anterior y al siguiente, sin abrir la app.
• Fuera la barra de progreso, que avanzaba hacia un final que no existe: el ruido no se acaba.
• El temporizador va de diez en diez: 10, 20, 30, 40, 50 y 60 minutos, más 90 y 120.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
</es-ES>
<en-US>
What came out of using the app on a real phone.

• The arrows in the notification now do something: they move to the previous or next sound, without opening the app.
• The progress bar is gone. It was creeping towards an end that does not exist: the noise never finishes.
• The sleep timer counts in tens: 10, 20, 30, 40, 50 and 60 minutes, plus 90 and 120.

No accounts, no cloud, no ads, no tracking.
</en-US>
```

---

## 1.0 (versionCode 1) — publicada el 2026-08-27

### es-ES (469 caracteres)

```text
Primera versión de Sleep Noise.

• Cuatro ruidos generados en tu teléfono, no reproducidos desde un fichero: sin bucle que se note ni artefactos de compresión. Uno de ellos está pensado para tapar conversaciones.
• Suena al abrir la app, con el último sonido que escuchaste.
• Temporizador de 15 a 120 minutos, con apagado progresivo.
• Control desde la notificación, con la app cerrada. Al pausar se cierra sola.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
```

### en-US (461 caracteres)

```text
The first version of Sleep Noise.

• Four noises generated on your phone, not played from a file: no loop you can notice, no compression artefacts. One of them is shaped to cover conversation.
• Plays as soon as you open the app, with the last sound you were listening to.
• Sleep timer from 15 to 120 minutes, with a gradual fade out.
• Control it from the notification, with the app closed. Pausing clears it away.

No accounts, no cloud, no ads, no tracking.
```

### Formato con etiquetas de idioma

```xml
<es-ES>
Primera versión de Sleep Noise.

• Cuatro ruidos generados en tu teléfono, no reproducidos desde un fichero: sin bucle que se note ni artefactos de compresión. Uno de ellos está pensado para tapar conversaciones.
• Suena al abrir la app, con el último sonido que escuchaste.
• Temporizador de 15 a 120 minutos, con apagado progresivo.
• Control desde la notificación, con la app cerrada. Al pausar se cierra sola.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
</es-ES>
<en-US>
The first version of Sleep Noise.

• Four noises generated on your phone, not played from a file: no loop you can notice, no compression artefacts. One of them is shaped to cover conversation.
• Plays as soon as you open the app, with the last sound you were listening to.
• Sleep timer from 15 to 120 minutes, with a gradual fade out.
• Control it from the notification, with the app closed. Pausing clears it away.

No accounts, no cloud, no ads, no tracking.
</en-US>
```
