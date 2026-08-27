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

## 1.0.1 (versionCode 2) — pendiente de publicar

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
