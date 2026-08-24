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

## 1.0 (versionCode 1) — pendiente de publicar

### es-ES (453 caracteres)

```text
Primera versión de Sleep Noise.

• Ruido blanco y ruido marrón para dormir, generados en tu teléfono en lugar de reproducirse desde un fichero: no hay bucle que se note ni artefactos de compresión.
• Suena al abrir la app, con el último sonido que escuchaste.
• Temporizador de 15 a 120 minutos, o el tiempo que quieras, con apagado progresivo.
• Control desde la notificación, con la app cerrada.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
```

### en-US (452 caracteres)

```text
The first version of Sleep Noise.

• White noise and brown noise for sleeping, generated on your phone instead of played from a file: no loop you can notice, no compression artefacts.
• Plays as soon as you open the app, with the last sound you were listening to.
• Sleep timer from 15 to 120 minutes, or any length you like, with a gradual fade out.
• Control it from the notification, with the app closed.

No accounts, no cloud, no ads, no tracking.
```

### Formato con etiquetas de idioma

```xml
<es-ES>
Primera versión de Sleep Noise.

• Ruido blanco y ruido marrón para dormir, generados en tu teléfono en lugar de reproducirse desde un fichero: no hay bucle que se note ni artefactos de compresión.
• Suena al abrir la app, con el último sonido que escuchaste.
• Temporizador de 15 a 120 minutos, o el tiempo que quieras, con apagado progresivo.
• Control desde la notificación, con la app cerrada.

Sin cuentas, sin nube, sin anuncios y sin seguimiento.
</es-ES>
<en-US>
The first version of Sleep Noise.

• White noise and brown noise for sleeping, generated on your phone instead of played from a file: no loop you can notice, no compression artefacts.
• Plays as soon as you open the app, with the last sound you were listening to.
• Sleep timer from 15 to 120 minutes, or any length you like, with a gradual fade out.
• Control it from the notification, with the app closed.

No accounts, no cloud, no ads, no tracking.
</en-US>
```
