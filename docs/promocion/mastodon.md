# Post de Mastodon — Sleep Noise

Un hilo de cuatro toots, en inglés y en español. Cada toot cabe en el límite por defecto de
Mastodon, 500 caracteres. Mastodon cuenta cada enlace como 23 caracteres, sea cual sea su longitud,
y los conteos de abajo ya lo tienen en cuenta.

Antes de publicar:

- **Publicar el hilo como respuestas encadenadas**: el primer toot es el que se comparte, y los demás
  van como respuesta a él. El primero se sostiene solo, por si alguien no abre el hilo.
- **Adjuntar capturas al primer toot, siempre con texto alternativo.** En Mastodon, una imagen sin
  descripción se comparte mucho menos, y hay quien no comparte nunca una sin ella. Las capturas y sus
  descripciones están al final.
- **Hashtags en CamelCase** (`#WhiteNoise`, no `#whitenoise`): los lectores de pantalla los leen como
  palabras. Mastodon no tiene algoritmo, así que los hashtags son lo que hace que alguien que no te
  sigue encuentre el post.
- **Visibilidad pública** en el primer toot, y «pública silenciosa» (*unlisted*) en las respuestas,
  para no llenar las líneas temporales locales con cuatro toots.
- Si publicas las dos versiones, mejor en días distintos, o la versión en español como hilo aparte
  marcando el idioma del toot como español en el selector de idioma.

---

## English

### 1/4 (396 caracteres)

```text
I couldn't find a noise app on the Play Store that could actually cover people talking, so I built one.

Sleep Noise: open it and it plays. Four sounds generated on your phone in real time, not recorded loops. Free, no ads, no accounts, no analytics — it doesn't even request the internet permission.

And it's open source, under the MIT License 🧵

#Android #OpenSource #FOSS #Privacy #WhiteNoise
```

### 2/4 (395 caracteres)

```text
The problem: most noise apps offer dozens of colours and nature sounds, but none shaped for speech. Brown noise is lovely for sleeping and does almost nothing against a conversation.

So the default sound is a masking noise: it puts 71% of its energy in the band where voices live, against brown noise's 9%. Then pink, white and brown, and not one more.

#Misophonia #Sleep #Insomnia #BrownNoise
```

### 3/4 (369 caracteres)

```text
Also in the app:

• Sleep timer that fades out over the last minute, so the stop doesn't wake you
• Do Not Disturb while it plays (optional), with alarms still getting through
• Control from the notification or the headphone button, screen off
• Pauses when you unplug your headphones
• Always dark UI, because it's used in bed with the lights off
• English and Spanish
```

### 4/4 (252 caracteres)

```text
Get it on Google Play: https://play.google.com/store/apps/details?id=com.jjrapps.sleepnoise

Source code: https://github.com/jorgejiro/sleep-noise-android

Missing a feature you'd need? Email me at jjrmobileapps@gmail.com and I'll gladly review your request.

Boosts are very welcome 🙏

#Kotlin #JetpackCompose #AndroidDev
```

### Imágenes del primer toot

| Fichero | Texto alternativo |
| :--- | :--- |
| `docs/store-assets/capturas/en/telefono/01-suena-enmascarador.png` | Main screen of Sleep Noise on a dark background, playing Masking noise at volume 50: a large amber volume ring with a pause button in the centre, a volume slider below, the four sounds (White, Pink, Brown, Masking) with Masking selected, and a Sleep timer row set to No timer. |
| `docs/store-assets/capturas/en/telefono/02-elegir-temporizador.png` | Sleep timer sheet with its options: No timer (selected), 10, 15, 20, 25, 30, 40 and 50 minutes, 1 hour, 1 hour 30 minutes and 2 hours. |
| `docs/store-assets/capturas/en/telefono/04-control-en-la-notificacion.png` | Android quick settings with the Sleep Noise media player showing Masking noise, with a pause button, previous and next sound buttons, and a plus button to add ten minutes to the timer. |
| `docs/store-assets/capturas/en/telefono/05-ruido-marron.png` | Main screen of Sleep Noise playing Brown noise at volume 50, with Brown selected among the four sounds and the sleep timer set to 1 h 30 min. |

---

## Español

### 1/4 (407 caracteres)

```text
No encontraba en Google Play ninguna app de ruido que tapara de verdad las conversaciones, así que hice la mía.

Sleep Noise: la abres y suena. Cuatro sonidos generados en el teléfono en tiempo real, no bucles grabados. Gratis, sin anuncios, sin cuentas, sin analítica: ni siquiera pide el permiso de internet.

Y es software libre, con licencia MIT 🧵

#Android #SoftwareLibre #FOSS #Privacidad #RuidoBlanco
```

### 2/4 (424 caracteres)

```text
El problema: casi todas las apps de ruido tienen decenas de colores y sonidos de naturaleza, pero ninguno pensado para la voz. El ruido marrón es estupendo para dormir y apenas hace nada contra una conversación.

Por eso el sonido que viene puesto es un enmascarador: pone el 71 % de su energía en la banda donde vive la voz, frente al 9 % del marrón. Luego rosa, blanco y marrón, y ni uno más.

#Misofonía #Insomnio #Dormir
```

### 3/4 (392 caracteres)

```text
Además:

• Temporizador que baja el volumen durante el último minuto, para que el final no te despierte
• No molestar mientras suena (opcional), y las alarmas siguen sonando
• Control desde la notificación o el botón de los auriculares, con la pantalla apagada
• Se pausa al desenchufar los auriculares
• Interfaz oscura siempre, porque se usa en la cama con la luz apagada
• Español e inglés
```

### 4/4 (275 caracteres)

```text
Descárgala en Google Play: https://play.google.com/store/apps/details?id=com.jjrapps.sleepnoise

Código fuente: https://github.com/jorgejiro/sleep-noise-android

¿Echas algo en falta? Escríbeme a jjrmobileapps@gmail.com y revisaré con gusto tu petición de mejora.

Se agradece mucho que lo compartas 🙏

#Kotlin #JetpackCompose #DesarrolloAndroid
```

### Imágenes del primer toot

| Fichero | Texto alternativo |
| :--- | :--- |
| `docs/store-assets/capturas/es/telefono/01-suena-enmascarador.png` | Pantalla principal de Sleep Noise sobre fondo oscuro, con el enmascarador sonando a volumen 50: un gran aro de volumen ámbar con el botón de pausa en el centro, un control de volumen debajo, los cuatro botones de sonido (Blanco, Rosa, Marrón y Máscara) con Máscara seleccionado, y la fila del temporizador sin temporizador. |
| `docs/store-assets/capturas/es/telefono/02-elegir-temporizador.png` | Hoja del temporizador con sus opciones: sin temporizador (seleccionada), 10, 15, 20, 25, 30, 40 y 50 minutos, 1 hora, 1 hora y 30 minutos, y 2 horas. |
| `docs/store-assets/capturas/es/telefono/04-control-en-la-notificacion.png` | Ajustes rápidos de Android con el reproductor de Sleep Noise mostrando el enmascarador, con botón de pausa, botones de sonido anterior y siguiente, y un botón de más para añadir diez minutos al temporizador. |
| `docs/store-assets/capturas/es/telefono/05-ruido-marron.png` | Pantalla principal de Sleep Noise con el ruido marrón sonando a volumen 50, el marrón seleccionado entre los cuatro sonidos y el temporizador puesto en 1 h 30 min. |
