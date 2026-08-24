# Textos para Google Play — Sleep Noise

Documento de trabajo para rellenar la ficha de Play Console y llevar la 1.0 de test interno a
producción. Los conteos de caracteres de este fichero **están calculados, no estimados**: los genera
`scripts/generar-textos-ficha.py` a partir de los textos de origen. Si editas un texto aquí, vuelve a
pasar el script para que el conteo no mienta.

> La app no tiene cuentas, nube, anuncios, analítica ni seguimiento, y **no declara el permiso
> `INTERNET`**. Todo lo que guarda son siete preferencias locales. Eso hace que los cuestionarios de
> Play sean triviales y, sobre todo, verificables por quien revise.

---

## Checklist para publicar

1. `./gradlew lint test` en verde.
2. Con un dispositivo o emulador conectado: `./gradlew connectedDebugAndroidTest`.
3. Recorrido manual completo — **es la lista que no se puede saltar** (§11 de la especificación):
   - primer arranque tras instalar: suena ruido marrón a volumen 50;
   - segundo arranque: suena el último sonido al último volumen;
   - abrir la app mientras ya está sonando: no reinicia nada;
   - permiso de notificaciones concedido y **denegado**;
   - cambio de sonido durante la reproducción, sin chasquido;
   - volumen desde el aro y desde el slider;
   - temporizador: preset, personalizado, +15 min, cancelar, y pausa manual a mitad de cuenta;
   - fade out del último minuto;
   - las trece filas de la tabla de convivencia con el sistema (llamada entrante, otra app
     reproduciendo, auriculares desconectados, Doze, app fuera de recientes…);
   - control desde la notificación con la app cerrada;
   - cambio de idioma ES/EN, **incluida la notificación**;
   - novedades y envío de comentarios.
4. Sesión de resistencia de 8 h **con el AAB de release minificado**, no con el debug.
5. Generar el `.aab` firmado con la upload key vigente.
6. Subir a **Internal testing**, validar en dispositivo real, y promover.
7. Pegar las notas de la versión desde [`play-release-notes.md`](play-release-notes.md), con el
   bloque de etiquetas de idioma para hacerlo de una sola pegada.
8. Revisar en Play Console: ficha principal · países · categoría · seguridad de los datos ·
   clasificación de contenido · **declaración de servicio en primer plano** · precio gratis.
9. Enviar a revisión con despliegue por fases: 20 % durante 48 h, luego 100 %.

---

## Ficha principal — App name

Límite: 30 caracteres. **Decisión pendiente**: nombre limpio o nombre con cola de búsqueda. La cola
ayuda a que la app aparezca buscando «ruido para dormir» o «white noise», y el coste es que el nombre
se lee peor en el lanzador, donde de todas formas se trunca.

### Con cola de búsqueda (recomendado para la ficha)

- **es-ES** (30 caracteres): `Sleep Noise: ruido para dormir`
- **en-US** (30 caracteres): `Sleep Noise: white noise sleep`

### Limpio

- **es-ES** (11 caracteres): `Sleep Noise`
- **en-US** (11 caracteres): `Sleep Noise`

El nombre en el lanzador (`app_name` en `strings.xml`) es **siempre** «Sleep Noise», sin cola, en los
dos idiomas. La cola es de la ficha, no del icono.

---

## Ficha principal — Short description

Límite: 80 caracteres.

### es-ES (66 caracteres)

```text
Ruido blanco y marrón para dormir. Suena al abrir y se apaga solo.
```

### en-US (74 caracteres)

```text
White and brown noise for sleep. Plays when you open it, stops on a timer.
```

---

## Ficha principal — Full description

Límite: 4000 caracteres.

Las tres cosas que este texto dice **a propósito**, porque son las que evitan una valoración de una
estrella el primer día: que hay **solo dos sonidos**, que el ruido está **generado y no grabado**, y
que la interfaz es **oscura siempre**.

### es-ES (2017 caracteres)

```text
Sleep Noise reproduce ruido blanco o ruido marrón para dormir. Se abre y suena: no hay que pulsar nada.

Solo dos sonidos, y es a propósito. No hay catálogo que recorrer ni mezclas que configurar a las dos de la mañana. Ruido blanco cuando quieres tapar voces y ruidos agudos, ruido marrón cuando prefieres algo más grave y envolvente, parecido a la lluvia lejana o al mar de fondo.

El ruido no está grabado: se genera en tu teléfono, muestra a muestra, mientras lo escuchas. Eso significa que no hay un bucle que se repita y que acabes notando, y tampoco la textura metálica que deja el audio comprimido cuando intenta comprimir algo que no tiene estructura. Suena limpio la primera hora y la octava.

El temporizador apaga el sonido cuando tú digas: 15, 30, 45, 60, 90 o 120 minutos, o el tiempo exacto que quieras. Al llegar al final el volumen baja poco a poco durante el último minuto en vez de cortarse de golpe, porque un corte seco despierta a quien justo se estaba durmiendo.

Se controla sin abrir la app: la notificación permite pausar, parar y añadir quince minutos al temporizador, y el botón de los auriculares también funciona. Puedes cerrar la app y dejar el móvil boca abajo; el sonido sigue.

La interfaz es oscura siempre. No hay modo claro, y no es un olvido: esta app se usa en la cama con la luz apagada, y una pantalla blanca ahí deslumbra.

Sin cuentas, sin nube, sin anuncios y sin seguimiento. La app no usa internet en ningún momento: no pide el permiso para conectarse, así que no puede hacerlo aunque quisiera.

Qué incluye:

• Ruido blanco y ruido marrón, generados en el dispositivo.
• Suena al abrir la app, con el último sonido que escuchaste.
• Temporizador con apagado progresivo, y opción de añadir quince minutos.
• Control desde la notificación y desde los auriculares.
• Volumen propio, independiente del volumen de multimedia del sistema.
• Sigue sonando con la pantalla apagada y la app cerrada.
• Español e inglés.
• Sin internet, sin cuentas, sin anuncios, sin seguimiento.
```

### en-US (2066 caracteres)

```text
Sleep Noise plays white noise or brown noise to help you sleep. Open it and it plays — there is nothing to press.

Two sounds only, and that is on purpose. No catalogue to scroll through, no mix to configure at two in the morning. White noise when you want to cover voices and sharp sounds, brown noise when you prefer something deeper and more enveloping, closer to distant rain or the sea in the background.

The noise is not a recording: it is generated on your phone, sample by sample, as you listen. That means there is no loop that repeats until you start noticing it, and none of the metallic texture that compressed audio leaves behind when it tries to compress something with no structure in it. It sounds as clean in the eighth hour as in the first.

The sleep timer stops the sound whenever you say: 15, 30, 45, 60, 90 or 120 minutes, or any length you like. At the end the volume fades down through the last minute instead of cutting out, because an abrupt stop wakes up the person who was finally falling asleep.

You can control it without opening the app: the notification lets you pause, stop and add fifteen minutes to the timer, and your headphone button works too. Close the app and put the phone face down — the sound keeps going.

The interface is dark, always. There is no light mode, and that is not an oversight: this app gets used in bed with the lights off, and a white screen there is blinding.

No accounts, no cloud, no ads and no tracking. The app never uses the internet: it does not even request the permission to connect, so it could not do it if it wanted to.

What you get:

• White noise and brown noise, generated on your device.
• Plays as soon as you open the app, with the last sound you were listening to.
• Sleep timer with a gradual fade out, and a button to add fifteen minutes.
• Control from the notification and from your headphones.
• Its own volume, independent of the system media volume.
• Keeps playing with the screen off and the app closed.
• English and Spanish.
• No internet, no accounts, no ads, no tracking.
```

---

## Ficha principal — Categoría y etiquetas

- **Categoría**: Salud y bienestar.
- **Etiquetas** (hasta cinco, en orden de preferencia): Sueño · Relajación · Ruido blanco ·
  Meditación · Sonidos ambientales.
- **Precio**: gratis, sin compras integradas.
- **Países**: todos.

---

## Ficha principal — Texto promocional

Límite: 170 caracteres.

### es-ES (89 caracteres)

```text
Ruido blanco y marrón generados en tu teléfono. Suena al abrir. Se apaga cuando tú digas.
```

### en-US (81 caracteres)

```text
White and brown noise generated on your phone. Plays on open. Stops when you say.
```

---

## Capturas — Orden recomendado

El orden importa: en Play se ven las dos primeras sin desplazarse.

| # | Escena | Por qué está aquí |
|---|---|---|
| 1 | Pantalla principal sonando, ruido marrón | Es la app entera en una imagen |
| 2 | Temporizador abierto con los presets | La segunda razón por la que alguien instala esto |
| 3 | Pantalla principal con el temporizador corriendo | Enseña el estado en uso, no en reposo |
| 4 | Notificación de control en la sombra | Que se puede pausar sin abrir la app |
| 5 | Ajustes | Que existe el idioma y que no hay nada raro dentro |
| 6 | Novedades | Señal de app mantenida |

Generación automatizada: ver `docs/store-assets/generar-capturas/README.md`. **No se hacen a mano.**

---

## Feature graphic — Texto sugerido

1024 × 500. Fondo oscuro cálido con el aro ámbar, coherente con la app.

- **es-ES**: «Ruido para dormir. Generado en tu teléfono.»
- **en-US**: «Noise for sleeping. Generated on your phone.»

---

## Seguridad de los datos — Respuestas

| Pregunta de Play Console | Respuesta |
|---|---|
| ¿La app recopila o comparte alguno de los tipos de datos requeridos? | **No** |
| ¿Todos los datos se cifran en tránsito? | No aplica: no hay tránsito. La app no declara `INTERNET` |
| ¿Ofrece una forma de solicitar la eliminación de datos? | No aplica: no hay datos en ningún servidor. Desinstalar borra las preferencias locales |
| Resumen de privacidad | Sleep Noise no recopila ni comparte datos de usuario. Las preferencias (sonido, volumen, temporizador, idioma) se guardan solo en el dispositivo |

---

## Contenido de la app

| Sección | Respuesta |
|---|---|
| Clasificación de contenido (IARC) | Todo «no» → clasificación para todos los públicos |
| Público objetivo | Mayores de 13 años. No dirigida a menores |
| Anuncios | No contiene |
| Acceso a la app | Todo el contenido está disponible sin restricciones ni credenciales |
| Política de privacidad | URL pública obligatoria: `docs/privacy-policy/sleep-noise.html` publicada en GitHub Pages |

---

## Declaración de permisos y servicios

Play exige justificar el uso del servicio en primer plano. Sin esta declaración **la release se
bloquea en revisión**, así que se prepara antes de subir el AAB, no cuando la pidan.

### Servicio en primer plano — tipo `mediaPlayback`

**en-US**

```text
Sleep Noise plays continuous generated noise (white and brown) to help the user fall asleep. The
audio must keep playing while the app is in the background and the screen is off, for the entire
sleep session, so playback runs in a foreground service of type mediaPlayback with a MediaSession.
The ongoing notification is the user's control surface: pause, stop and extend the sleep timer
without unlocking the phone. No other foreground service type would allow uninterrupted audio for
several hours, and the service stops as soon as the user stops playback or the sleep timer expires.
```

**es-ES**

```text
Sleep Noise reproduce ruido generado de forma continua (blanco y marrón) para ayudar al usuario a
dormirse. El audio debe seguir sonando con la app en segundo plano y la pantalla apagada durante toda
la sesión de sueño, así que la reproducción corre en un servicio en primer plano de tipo mediaPlayback
con una MediaSession. La notificación permanente es la superficie de control del usuario: pausar,
parar y ampliar el temporizador sin desbloquear el teléfono. Ningún otro tipo de servicio permitiría
audio ininterrumpido durante horas, y el servicio se detiene en cuanto el usuario para la
reproducción o vence el temporizador.
```

### Vídeo de justificación, si lo piden

Grabación de pantalla de 20–30 s: abrir la app (suena) → pulsar inicio → apagar la pantalla →
encenderla y pausar desde la notificación. Sin cortes de edición.

### Permisos declarados

| Permiso | Para qué |
|---|---|
| `FOREGROUND_SERVICE` | Reproducción continua en segundo plano |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Tipo del servicio anterior, obligatorio desde Android 14 |
| `POST_NOTIFICATIONS` | Mostrar la notificación de control, que es la única forma de pausar sin abrir la app |

**No** se declaran: `INTERNET`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`
(lo gestiona Media3 por dentro), ni ningún permiso de almacenamiento.

---

## Declaración corta para soporte o revisión

**en-US**

```text
Sleep Noise is an offline white/brown noise player for sleeping. The noise is synthesised on the
device in real time; the app ships no audio files and never connects to the network. It stores seven
local preferences and collects no user data.
```

**es-ES**

```text
Sleep Noise es un reproductor de ruido blanco y marrón para dormir, que funciona sin conexión. El
ruido se sintetiza en el dispositivo en tiempo real; la app no incluye ficheros de audio y nunca se
conecta a la red. Guarda siete preferencias locales y no recoge ningún dato del usuario.
```
