# Post de Reddit — Sleep Noise

Dos versiones del mismo post: en inglés, para los subreddits internacionales, y en español, para los
hispanohablantes. El cuerpo va en Markdown de Reddit: se pega tal cual en el editor en modo
«Markdown».

Antes de publicar en cada subreddit:

- Leer sus reglas y su wiki. Muchos solo admiten autopromoción en un hilo semanal o con un flair
  concreto (`Dev`, `Self-promotion`, `Open source`…).
- Decir desde la primera línea que eres el desarrollador. Reddit castiga mucho más el post que lo
  esconde que el que lo reconoce.
- No publicar el mismo texto en muchos subreddits el mismo día: el filtro antispam lo detecta y
  quien sigue varios subreddits lo ve repetido. Mejor espaciarlos y adaptar el primer párrafo a cada
  comunidad.
- Quedarse a contestar los comentarios las primeras horas. Es lo que más visibilidad da.

---

## English

### Title

```text
I couldn't find a noise app that could cover people talking, so I built one — free, open source, no ads, no internet
```

### Body

```markdown
Hi everyone! I'm the developer, so full disclosure up front: this is my own app.

**The problem.** I use background noise for two things that matter equally to me: falling asleep, and covering the noise around me when I can't leave where I am — headphones on, sometimes with earplugs underneath, in a place where people are talking or working.

I tried a lot of the noise apps on the Play Store and none of them covered what I actually needed:

- **Most of them don't really cover voices.** They offer dozens of "colours" and nature sounds, but none is shaped for speech. Brown noise is lovely for sleeping and does almost nothing against a conversation.
- **Many play recorded loops.** After a while you start hearing the loop repeat, and compressed audio adds a metallic texture to noise that becomes obvious at 3 a.m.
- **Too much friction.** Welcome screens, accounts, subscriptions, ads, a catalogue to scroll through when all you want is to open it and hear the noise.
- **Too many permissions and trackers** for something that should just play a sound.

So I built **Sleep Noise**.

**What it does**

- **It plays the moment you open it.** No play button to hunt for: it starts with the last sound you were listening to.
- **Four sounds, and not one more**, each with a different job:
  - **Masking** (the default): shaped to cover speech. It puts **71% of its energy in the band where voices live**, against brown noise's 9%. This is the one for headphones in a busy room.
  - **Pink**: the balanced one, equal energy per octave.
  - **White**: flat, good against sharp sounds.
  - **Brown**: deep, like distant rain. The most comfortable for sleeping.
- **Generated, not recorded.** The noise is synthesized on your phone, sample by sample, in real time. There is no loop to notice and no compression artefacts: it sounds the same in the eighth hour as in the first.
- **Sleep timer with a gradual fade out** over the last minute, because an abrupt stop wakes you up. You can add ten minutes from the notification.
- **Do Not Disturb while it plays** (optional): it silences the phone while the noise is on and restores it when you stop. **Alarms still get through**, and it never turns off a Do Not Disturb you set yourself.
- **Control from the notification or your headphone button**, with the app closed and the screen off. Unplugging your headphones pauses it, so it never blasts from the speaker.
- **Always dark UI**, because it's used in bed with the lights off.
- English and Spanish.

**Privacy.** The app **doesn't even request the internet permission**, so it couldn't send anything anywhere if it wanted to. No accounts, no ads, no analytics, no in-app purchases. It's free.

**It's open source.** The full source code is on GitHub under the MIT License:
https://github.com/jorgejiro/sleep-noise-android

If you're curious about the technical side, the repo explains why the noise is generated instead of played from files, and how the masking sound was designed by measuring where each noise puts its energy.

**Get it on Google Play:**
https://play.google.com/store/apps/details?id=com.jjrapps.sleepnoise

**Missing something?** If you like the idea but there's a feature you'd need, email me at **jjrmobileapps@gmail.com** — I'll gladly review every feature request. Bug reports are just as welcome, here or as a GitHub issue.

Thanks for reading, and I hope it helps you sleep (or focus) a bit better.
```

---

## Español

### Título

```text
No encontraba ninguna app de ruido que tapara las conversaciones, así que hice la mía: gratis, open source, sin anuncios y sin internet
```

### Cuerpo

```markdown
¡Hola! Soy el desarrollador, así que lo digo desde el principio: la app es mía.

**El problema.** Uso ruido de fondo para dos cosas que me importan lo mismo: dormirme, y tapar el ruido de alrededor cuando no me puedo ir de donde estoy —con auriculares, a veces con tapones debajo, en un sitio donde hay gente hablando o trabajando—.

Probé muchas de las apps de ruido que hay en Google Play y ninguna cubría lo que yo necesitaba:

- **La mayoría no tapa las voces.** Tienen decenas de «colores» y sonidos de naturaleza, pero ninguno pensado para la voz. El ruido marrón es estupendo para dormir y apenas hace nada contra una conversación.
- **Muchas reproducen bucles grabados.** Al rato notas que el bucle se repite, y el audio comprimido le deja al ruido una textura metálica que a las tres de la mañana se oye.
- **Demasiada fricción.** Pantallas de bienvenida, cuentas, suscripciones, anuncios, un catálogo que recorrer cuando lo único que quieres es abrirla y que suene.
- **Demasiados permisos y rastreadores** para algo que solo debería poner un sonido.

Así que hice **Sleep Noise**.

**Qué hace**

- **Suena en cuanto la abres.** No hay que buscar el botón de play: arranca con el último sonido que escuchaste.
- **Cuatro sonidos, y ni uno más**, cada uno con un trabajo distinto:
  - **Enmascarador** (el que viene puesto): con la forma pensada para tapar la voz. Pone **el 71 % de su energía en la banda donde vive la voz**, frente al 9 % del marrón. Es el de los auriculares en un sitio con gente.
  - **Rosa**: el equilibrado, la misma energía en cada octava.
  - **Blanco**: plano, bueno contra los ruidos agudos y secos.
  - **Marrón**: grave, como lluvia lejana. El más cómodo para dormir.
- **Generado, no grabado.** El ruido se sintetiza en el teléfono, muestra a muestra, en tiempo real. No hay bucle que notar ni artefactos de compresión: suena igual la octava hora que la primera.
- **Temporizador con apagado progresivo** durante el último minuto, porque un corte seco despierta. Se pueden añadir diez minutos desde la notificación.
- **No molestar mientras suena** (opcional): silencia el teléfono mientras suena el ruido y lo deja como estaba al parar. **Las alarmas siguen sonando**, y nunca apaga un No molestar que hayas puesto tú.
- **Control desde la notificación o el botón de los auriculares**, con la app cerrada y la pantalla apagada. Al desenchufar los auriculares se pausa, para que no suene de golpe por el altavoz.
- **Interfaz oscura siempre**, porque se usa en la cama con la luz apagada.
- Español e inglés.

**Privacidad.** La app **ni siquiera pide el permiso de internet**, así que no podría enviar nada aunque quisiera. Sin cuentas, sin anuncios, sin analítica y sin compras integradas. Es gratis.

**Es open source.** Todo el código está en GitHub con licencia MIT:
https://github.com/jorgejiro/sleep-noise-android

Si te interesa la parte técnica, en el repositorio se explica por qué el ruido se genera en vez de reproducirse desde ficheros, y cómo se diseñó el enmascarador midiendo dónde pone su energía cada ruido.

**Descárgala en Google Play:**
https://play.google.com/store/apps/details?id=com.jjrapps.sleepnoise

**¿Echas algo en falta?** Si te gusta la idea pero necesitas alguna funcionalidad que no tiene, escríbeme a **jjrmobileapps@gmail.com** y revisaré con gusto tu petición de mejora. Los errores también son bienvenidos, aquí o como issue en GitHub.

Gracias por leer, y ojalá te ayude a dormir (o a concentrarte) un poco mejor.
```
