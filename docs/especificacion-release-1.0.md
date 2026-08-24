# Sleep Noise — Especificación y plan de la release 1.0

> Documento vinculante para la primera versión publicable. Lo que no está aquí, no entra en la 1.0.
> Complementa a `docs/analisis-tecnico.md` (el *cómo* técnico) y a `CLAUDE.md` (las reglas de trabajo).
>
> Fecha: 2026-08-24 · Estado: **pendiente de una decisión del autor** (§13) · Sin código todavía.

---

## 1. Definición del producto

Una app Android que genera ruido de fondo continuo, con **dos propósitos que pesan igual**.

**Los dos casos de uso que gobiernan cada decisión:**

1. **Dormirse.** Una persona en la cama, a oscuras, con los ojos medio cerrados, que quiere que suene
   algo en dos segundos y luego olvidarse del móvil durante ocho horas.
2. **Tapar el ruido de alrededor.** Una persona con auriculares —y a veces tapones debajo— en un
   sitio del que no puede irse, que necesita que el ruido de la app cubra las conversaciones y el
   ruido ambiente todo lo posible.

Cualquier funcionalidad que no sirva a uno de esos dos momentos se queda fuera. Y no son intercambiables:
el segundo es el que fija el nivel de salida y la forma espectral de la mitad del catálogo (ADR 006),
y el primero el que fija el temporizador, los fades y que la interfaz sea oscura.

| | |
|---|---|
| Nombre | Sleep Noise |
| `applicationId` | `com.jjrapps.sleepnoise` |
| Versión objetivo | `versionName` **1.0**, `versionCode` **1** |
| Idiomas | Inglés (base) y español |
| Dispositivos | Teléfonos y tablets Android 12+ (`minSdk` 31) |
| Precio | Gratis, sin anuncios, sin compras, sin cuentas |
| Red | La app **no usa internet**. No declara `INTERNET`. |

---

## 2. Alcance de la 1.0

### 2.1 Entra

| Bloque | Contenido |
|---|---|
| Sonidos | Cuatro: blanco, rosa, marrón y enmascarador, sintetizados en el dispositivo |
| Reproducción | Bucle indefinido, en segundo plano, con la pantalla apagada, durante horas |
| Arranque | Automático al abrir la app, con el último sonido escuchado; enmascarador tras instalar |
| Volumen | Aro arrastrable + slider convencional, con curva perceptual |
| Temporizador | Presets y valor personalizado, con fade out final |
| Notificación | `MediaStyle` con pausa, parar y +15 min |
| Ajustes | Reproducir al abrir, idioma, versión, novedades, feedback |
| Novedades | Changelog por versión, traducido |
| Accesibilidad | TalkBack completo, targets ≥ 48 dp, texto escalable hasta 200 % |

### 2.2 No entra (y no se discute durante la 1.0)

Ruido gris · sonidos naturales o grabados · mezcla de varios sonidos · ecualizador ·
modo claro · dynamic color · widget de escritorio · tile de ajustes rápidos · Wear OS · Android Auto ·
alarma de despertador · estadísticas de sueño · copia de seguridad en la nube · analítica ·
onboarding con pantallas de bienvenida · valoración in-app.

El modo claro y dynamic color quedan fuera **por decisión de diseño**, no por falta de tiempo: ver
`docs/decisions/002-direccion-visual-noche-profunda.md`.

---

## 3. Dirección visual

Dirección **«Noche profunda»**, aprobada el 2026-08-24. Tema **oscuro único**.

### 3.1 Color

Un solo color saturado en toda la app. El resto es una escala de grises cálidos.

| Token | Valor | Uso |
|---|---|---|
| `background` | `oklch(0.155 0.014 62)` | Fondo de todas las pantallas |
| `surface` | `oklch(0.185 0.012 62)` | Tarjetas y controles en reposo |
| `surfaceSelected` | `oklch(0.235 0.022 62)` | Control seleccionado |
| `surfaceRaised` | `oklch(0.215 0.018 62)` | Centro del aro, hojas modales |
| `outline` | `oklch(0.26 0.012 62)` | Bordes y separadores |
| `accent` | `#E8A860` | Aro de volumen, iconos activos, valores |
| `onBackground` | `oklch(0.94 0.008 62)` | Texto principal |
| `onBackgroundVariant` | `oklch(0.80 0.012 62)` | Texto secundario |
| `onBackgroundMuted` | `oklch(0.60 0.018 62)` | Etiquetas y unidades |

Contraste **medido**, no estimado: `onBackground` 16.4:1 · `onBackgroundVariant` 10.4:1 ·
`onBackgroundMuted` 4.9:1 · `accent` 9.5:1, todos sobre `background`. Lo comprueba
`ColorContrastTest` en cada `./gradlew test`, así que un token que se mueva y baje del mínimo de
WCAG AA rompe el build en lugar de colarse.

`onBackgroundMuted` subió de `oklch(0.58 …)` a `0.60` durante H0: a 0.58 medía 4.53:1, que clava el
mínimo de 4.5 por tres centésimas y se rompería con el siguiente ajuste.

### 3.2 Tipografía

**Sora** en pesos 200, 300, 400 y 600, empaquetada en la app (no descargada: la app no usa red).

| Rol | Tamaño | Peso | Uso |
|---|---|---|---|
| Display | 34 sp | 200 | Nombre del sonido |
| Título | 22 sp | 400 | Cabeceras de pantalla |
| Cuerpo | 15 sp | 300/400 | Filas de ajustes, etiquetas |
| Etiqueta | 12 sp | 300 | Unidades, mayúsculas con `letterSpacing` 0.1em |
| Marca | 11 sp | 400 | «SLEEP NOISE», `letterSpacing` 0.22em |

Todos los tamaños en `sp`, escalables hasta el 200 % del sistema sin que se corte texto.

### 3.3 Icono

**«Luna de grano»** (ADR 004): un creciente compuesto de 230 puntos de grano ámbar sobre el fondo
oscuro cálido. Cuatro piezas, no una:

| Pieza | Fichero | Notas |
|---|---|---|
| Icono adaptativo | `ic_launcher.xml` | Referencia a las tres capas siguientes |
| Fondo | `ic_launcher_background.xml` | Viñeteado radial cálido, a sangre en los 108 dp |
| Primer plano | `ic_launcher_foreground.xml` | El grano, 230 puntos en un solo `path` |
| Monocromo | `ic_launcher_monochrome.xml` | Creciente sólido con 74 puntos gruesos en el borde |
| Notificación | `ic_stat_sleep_noise.xml` | 24 dp, silueta sin grano ni fondo, tintada por el sistema |
| Ficha de Play | `play-icon-512.svg` | Origen del PNG de 512 × 512, sin máscara |

Todo se genera con `scripts/generar-iconos.py` y **no se edita a mano**: el grano viene de un PRNG
determinista para que no cambie entre regeneraciones, y el creciente se calcula por intersección de
circunferencias. Los assets viven en `docs/design/icono/vector/` hasta que exista `app/src/main/res/`.

El grano va como puntos vectoriales porque un `VectorDrawable` de Android no soporta filtros SVG.

### 3.4 Forma y movimiento

- Radios: 20 dp en píldoras de sonido, 24 dp en hojas modales, círculo completo en el aro y el botón.
- **Halo que respira**: ciclo de 9 s, escala 1 → 1.09, opacidad 0.55 → 0.9. Se detiene cuando la
  app pasa a segundo plano y cuando `Settings.Global.ANIMATOR_DURATION_SCALE` es 0.
- Transiciones entre pantallas: las de Material 3 Expressive por defecto (`motionScheme`), sin
  animaciones propias.
- Cambio de sonido: crossfade de audio de 800 ms, y el nombre en pantalla cruza con un fade de 200 ms.

---

## 4. Pantallas y navegación

| Id | Pantalla | Llega desde |
|---|---|---|
| P1 | Reproductor | Lanzador (pantalla de inicio de la app) |
| P2 | Temporizador (hoja modal inferior) | P1, fila «Temporizador» |
| P3 | Ajustes | P1, icono de engranaje |
| P4 | Novedades | P3, fila «Novedades» |

Navegación con Navigation Compose. P2 es un `ModalBottomSheet`, no un destino de navegación.
Predictive back activo en todas.

### P1 · Reproductor

De arriba abajo: marca «SLEEP NOISE» e icono de ajustes · aro de volumen de 264 dp con el botón de
play/pausa de 100 dp en el centro · nombre del sonido a 34 sp · línea de volumen («VOLUMEN 50») ·
**slider de volumen** · cuatro píldoras de sonido en rejilla 2×2 · separador · fila de temporizador
con su estado y un chevron.

El slider es la corrección acordada sobre el mockup: el aro es un atajo, el slider es el camino
garantizado.

### P2 · Temporizador

Hoja modal con: opción «Sin temporizador» · presets 15, 30, 45, 60, 90 y 120 minutos ·
«Personalizado…» que abre un selector de 5 a 600 minutos. La opción activa se marca con el acento.
Al elegir, la hoja se cierra y el temporizador arranca inmediatamente.

### P3 · Ajustes

Dos secciones, sin más:

- **Reproducción**: «Reproducir al abrir la app» (interruptor, activado por defecto).
- **Acerca de**: «Idioma» · «Versión» (solo lectura) · «Novedades» · «Enviar comentarios».

### P4 · Novedades

Lista de versiones, la más reciente arriba: número de versión, fecha y sus cambios en viñetas,
traducidos.

---

## 5. Requisitos funcionales

Cada requisito tiene un identificador estable. Los criterios de aceptación son la definición de
«hecho»: si no se pueden comprobar en un dispositivo, el requisito no está terminado.

### 5.1 Reproducción

**RF-01 · Arranque automático.** Al abrir la app se reproduce el último sonido escuchado, al último
volumen usado, sin que el usuario pulse nada.

- *Dado* que es la primera vez que se abre tras instalar, *cuando* se abre, *entonces* suena el
  **ruido enmascarador** a **volumen 50** con un fade in de 1,5 s. Es el que sirve para el uso más
  exigente —tapar una habitación con gente— y el marrón está a un toque para dormir (ADR 006).
- *Dado* que la última vez se escuchó ruido blanco a volumen 70, *cuando* se abre, *entonces* suena
  ruido blanco a volumen 70.
- *Dado* que el ruido **ya está sonando** en segundo plano, *cuando* se abre la app, *entonces* no se
  reinicia nada: la pantalla refleja el estado actual.
- *Dado* que el usuario desactivó «Reproducir al abrir la app», *cuando* se abre, *entonces* la app
  aparece en pausa con el último sonido seleccionado.

**RF-02 · Bucle indefinido.** El sonido no termina nunca por sí solo. Solo lo paran el usuario, el
temporizador o el sistema.

- *Dado* un sonido en reproducción, *cuando* pasan 8 horas, *entonces* sigue sonando sin cortes,
  saltos ni cambios de nivel.

**RF-03 · Cambio de sonido.** Se puede cambiar entre los cuatro durante la reproducción, en una
rejilla de 2×2.

- *Dado* que suena ruido marrón, *cuando* se pulsa «Blanco», *entonces* el audio cruza al ruido
  blanco en 800 ms sin silencio intermedio ni chasquido, y el nombre en pantalla cambia con él.
- *Dado* que la app está en pausa, *cuando* se cambia de sonido, *entonces* se guarda la selección y
  **no** empieza a sonar.

**RF-04 · Volumen.** Dos controles equivalentes sobre el mismo valor: el aro y el slider.

- El valor va de 0 a 100 y se muestra siempre junto al aro.
- La ganancia aplicada es `(valor/100)²`, no lineal, porque el oído no lo es. Volumen 50 equivale a
  unos -12 dB respecto al máximo.
- *Dado* volumen 50, *cuando* se arrastra el aro hasta arriba, *entonces* el valor llega a 100 y el
  audio sube sin saltos escalonados.
- *Dado* el volumen a 0, *entonces* el audio queda en silencio pero la reproducción **no** se pausa.
- El volumen de la app es independiente del volumen de multimedia del sistema. La app **nunca**
  modifica el volumen del sistema.

**RF-05 · Play y pausa.** Un único botón, en el centro del aro. **Pausar termina la sesión**: no es
una interrupción, es el final. El usuario ha acabado de escuchar.

- *Dado* que suena, *cuando* se pulsa pausa —en la app o en la notificación—, *entonces* el sonido se
  detiene con un fade out de 400 ms, **la notificación desaparece** y el servicio sale de primer
  plano. Es como cerrar la app.
- *Dado* que la sesión terminó así, *cuando* se abre la app otra vez, *entonces* vuelve a sonar el
  último sonido, como en cualquier otro arranque (RF-01).
- **La pausa que impone el sistema es otra cosa** y no termina la sesión: ver RF-11 y §7.

> **Por qué no es un ajuste.** Si abrir la app es «empieza», pausar tiene que ser «he terminado»: es
> el mismo gesto de cerrar. Ofrecerlo como opción sería preguntar algo que nadie quiere decidir, y
> dejaría media app con un control colgado en la sombra toda la noche.

### 5.2 Temporizador

**RF-06 · Configuración.** Presets de 15, 30, 45, 60, 90 y 120 minutos, «Sin temporizador» y un valor
personalizado entre 5 y 600 minutos. El último valor usado se recuerda y aparece preseleccionado.

**RF-07 · Cuenta atrás y apagado.** El tiempo restante se muestra en P1 y en la notificación, en
unidades legibles («1 h 26 min», no «01:26:04»).

- *Dado* un temporizador de 30 min, *cuando* quedan 60 s, *entonces* empieza un fade out lineal en dB.
- *Cuando* llega a cero, *entonces* el audio está en silencio y **la sesión termina como si el usuario
  hubiese pausado**: la notificación desaparece y el servicio sale de primer plano. Quien se ha
  dormido no necesita un control esperándole en la sombra hasta la mañana.
- *Dado* un temporizador activo, *cuando* el usuario pausa manualmente, *entonces* el temporizador se
  **congela**; al reanudar, continúa desde donde estaba.
- *Dado* un temporizador activo, *cuando* el usuario cambia de sonido, *entonces* el temporizador
  sigue corriendo sin alterarse.

**RF-08 · Ajuste en caliente.** Desde la notificación y desde P1 se puede añadir 15 minutos o
cancelar el temporizador.

- *Dado* un temporizador en fade out, *cuando* se pulsa «+15 min», *entonces* el volumen vuelve a su
  valor con un fade in de 1,5 s y la cuenta atrás se reanuda.

### 5.3 Segundo plano y sistema

**RF-09 · Notificación de control.** Notificación `MediaStyle` permanente mientras hay sesión.

- Contenido: nombre del sonido como título; tiempo restante del temporizador (o «Sin temporizador»)
  como texto secundario.
- Acciones: **pausa/reanudar**, **parar** y, si hay temporizador activo, **+15 min**.
- Canal `playback`, importancia baja, sin sonido ni vibración, no descartable mientras suena.
- *Dado* el ruido sonando y la app cerrada, *cuando* se pulsa pausa en la notificación, *entonces* el
  audio se detiene con fade out, el servicio sale de primer plano y **la notificación desaparece**
  (RF-05). No queda ningún control en la sombra.
- *Dado* que el sistema pausa por su cuenta —una llamada, otra app, unos auriculares desenchufados—,
  *entonces* la notificación **permanece**, porque eso es una interrupción y no un final.

**RF-10 · Reproducción con la app en segundo plano.**

- *Dado* el ruido sonando, *cuando* se pulsa inicio y se apaga la pantalla, *entonces* sigue sonando
  indefinidamente.
- *Dado* el ruido sonando, *cuando* el usuario **desliza la app fuera de recientes**, *entonces*
  **sigue sonando**. Cerrar la app es lo que hace alguien que se va a dormir, no una orden de parar.

**RF-11 · Convivencia con el sistema.** Ver la tabla completa en §7.

**RF-12 · Persistencia.** Sonido, volumen, último temporizador usado, «reproducir al abrir» e idioma
sobreviven a cerrar la app, matar el proceso y reiniciar el teléfono.

**RF-19 · Reanudación tras una interrupción.** *Dado* que el sistema pausó el ruido —una llamada,
otra app— *cuando* la interrupción termina, *entonces* vuelve a sonar sin intervención.

Lo que **no** hay es reanudación después de que el usuario pause: ahí la sesión se acaba y no queda
sesión de medios que reanudar. Es la consecuencia deliberada de RF-05, y el camino de vuelta es
abrir la app, que es de todas formas lo que alguien hace cuando quiere volver a dormirse.

**RF-20 · Botones de auriculares y del sistema.** Play/pausa responde al botón central de los
auriculares.

La sesión **anuncia solo lo que la app sabe hacer**: pausar, reproducir y parar. Nada de saltar de
pista, rebobinar, avanzar, buscar una posición, cambiar la velocidad, aleatorio o repetición. Por
defecto una sesión de medios anuncia las veinte, y Android dibuja botones a partir de ellas: el
resultado eran unas flechas en la notificación que no hacían absolutamente nada al pulsarlas.

**Límite conocido:** los huecos de esas flechas los dibuja la plantilla del sistema, no la app. Dejar
de anunciar las acciones las deja inertes y atenuadas, pero no las borra de la pantalla. Para que
desaparezcan habría que ocupar su sitio con acciones propias, que es una decisión de producto tomada
en contra: la notificación se queda mínima.

### 5.4 Ajustes y meta

**RF-13 · Reproducir al abrir.** Interruptor, activado por defecto. Desactivado, la app abre en pausa.

**RF-14 · Idioma.** Tres opciones: Automático (sistema), English, Español. El cambio se aplica al
instante sin reiniciar la app.

**RF-15 · Versión.** Fila de solo lectura con `versionName` y `versionCode`.

**RF-16 · Novedades.** Lista de versiones con sus cambios, traducida. Cada versión publicada tiene su
entrada.

**RF-17 · Enviar comentarios.** Abre el cliente de correo con el destinatario y un asunto que ya
incluye nombre de app, `versionName` y `versionCode`. Si no hay ningún cliente de correo instalado, la
app no se rompe: no pasa nada visible y se registra en el log.

**RF-18 · Permiso de notificaciones.** En Android 13+ se solicita `POST_NOTIFICATIONS` la primera vez
que se va a reproducir, explicando antes para qué sirve: es la única forma de pausar sin abrir la app.

- *Dado* que el usuario deniega el permiso, *entonces* el ruido suena igualmente y la app muestra una
  vez, de forma discreta, que sin la notificación habrá que abrir la app para pausar.

---

## 6. Requisitos no funcionales

Cada uno con su forma de medirlo. Un requisito no funcional sin método de medida es una intención.

| Id | Requisito | Objetivo | Cómo se mide |
|---|---|---|---|
| RNF-01 | Estabilidad de sesión larga | 8 h continuas sin corte, salto ni cambio de nivel | Dispositivo físico, pantalla apagada, Doze forzado con `adb shell dumpsys deviceidle force-idle`, auriculares Bluetooth |
| RNF-02 | Consumo de batería | ≤ 2,5 % por hora con pantalla apagada | `adb shell dumpsys batterystats` sobre una sesión de 4 h; objetivo a validar, no promesa |
| RNF-03 | Tiempo hasta el primer sonido | < 800 ms desde el toque en el icono, arranque en frío | `adb shell am start -W` más marca temporal del primer buffer |
| RNF-04 | Tamaño de descarga | < 4 MB el AAB firmado | Informe de tamaño de Play Console |
| RNF-05 | CPU en reproducción | < 3 % de un núcleo con la pantalla apagada | `adb shell top -m 5` durante 10 min |
| RNF-06 | Calidad de audio | Sin chasquidos en arranque, pausa ni cambio de sonido; deriva de DC < 0,001; menos de 200 muestras limitadas por segundo | Tests JVM del generador + escucha con auriculares de estudio |
| RNF-07 | Fluidez de la UI | Ningún frame por encima de 16 ms en P1 con el halo animando | Macrobenchmark o `adb shell dumpsys gfxinfo` |
| RNF-08 | Accesibilidad | Recorrido completo con TalkBack; targets ≥ 48 dp; texto legible al 200 % | Accessibility Scanner + recorrido manual con TalkBack |
| RNF-09 | Privacidad | La app no declara `INTERNET` y no recoge ningún dato | Revisión del manifest fusionado (`app/build/outputs/logs`) |
| RNF-10 | Cambios de configuración | Girar la pantalla o cambiar de idioma no interrumpe el audio ni pierde el estado | Prueba manual + test instrumentado |
| RNF-11 | Fabricantes agresivos | Sobrevive 8 h en un Xiaomi o Samsung con ahorro de batería activo, o detecta el corte y avisa | Prueba en dispositivo real de cada marca disponible |

---

## 7. Convivencia con el sistema

La tabla de decisiones que evita discusiones más adelante. Cada fila es un test manual.

| Evento del sistema | Comportamiento |
|---|---|
| Llega una notificación con sonido | **Duck**: el volumen baja temporalmente y vuelve solo |
| Entra una llamada | **Pausa**, y al colgar **reanuda**. La notificación se queda: es una interrupción, no un final |
| Otra app empieza a reproducir música | **Pausa** (pérdida permanente de foco). No reanuda solo, pero la notificación se queda para poder volver |
| Se desconectan los auriculares | **Pausa**. Nunca saltar al altavoz: sería un despertar brusco |
| Se conectan unos auriculares | No arranca solo. Si estaba sonando, sigue |
| Asistente de voz | Duck mientras habla |
| Pantalla apagada | Sigue sonando. `WAKE_MODE_LOCAL` mantiene el proceso vivo |
| Doze / modo avión | Sigue sonando: el servicio en primer plano está exento |
| Reinicio del teléfono | **No** arranca solo. El usuario abre la app cuando quiere dormir |
| Batería crítica | El sistema puede matar el servicio. No se hace nada especial |
| App deslizada fuera de recientes | **Sigue sonando** (RF-10) |
| Modo «No molestar» | Sigue sonando: es multimedia, no una notificación |
| Alarma del despertador | El sistema la mezcla por encima. La app no interfiere |

---

## 8. Datos persistidos

Todo en DataStore Preferences. **Sin base de datos**: no hay nada que historiar.

| Clave | Tipo | Valor inicial | Notas |
|---|---|---|---|
| `last_sound` | String | `"masking"` | `"white"`, `"pink"`, `"brown"` o `"masking"` |
| `volume` | Int | `50` | 0–100 |
| `timer_minutes` | Int | `60` | Último preset usado. `0` = sin temporizador |
| `autoplay_on_open` | Boolean | `true` | RF-13 |
| `language` | String | `"auto"` | `"auto"`, `"en"`, `"es"` |
| `last_seen_changelog` | Int | `0` | `versionCode` de las últimas novedades vistas |
| `notif_rationale_shown` | Boolean | `false` | Para no repetir el aviso de RF-18 |

El temporizador en curso **no** se persiste: si el proceso muere, se ha perdido la sesión de sueño de
todas formas y reanudar una cuenta atrás huérfana sería peor que no hacerlo.

`android:allowBackup="false"`: son siete preferencias reconstruibles en dos toques, y no merecen el
riesgo de restaurar un estado incoherente en otro dispositivo.

---

## 9. Internacionalización

Regla completa en `CLAUDE.md` §5. Resumen operativo:

- **Inglés** en `res/values/strings.xml` (base y fallback para cualquier idioma no soportado).
- **Español** en `res/values-es/strings.xml`, sin cualificador de región.
- `res/xml/locale_config.xml` con `en` y `es`, declarado en el manifest.
- Cambio en Ajustes con `AppCompatDelegate.setApplicationLocales()`, más copia en DataStore para
  API 31–32.
- `androidResources { localeFilters += listOf("en", "es") }`.

Superficies que hay que traducir y que se olvidan siempre: **el nombre del canal de notificación**,
**el texto de la notificación**, **las acciones de la notificación**, el nombre de la app en el
lanzador, el asunto del correo de feedback y las entradas del changelog.

Criterio de aceptación: con el teléfono en alemán, toda la app se ve en inglés, incluida la
notificación. Con el teléfono en español de México, toda la app se ve en español.

---

## 10. Accesibilidad

No es una fase final, es parte de cada pantalla.

- **Etiquetas de contenido** en todos los controles sin texto: botón de play/pausa (con estado:
  «Reproducir» / «Pausar»), icono de ajustes, chevron del temporizador.
- **El aro es un control**, así que expone su rol y su valor a TalkBack: `progressBarRangeInfo` con
  el valor actual, y acciones de incrementar/decrementar. El slider de debajo cubre a quien navegue
  con teclado o conmutador.
- **Estado leído en voz alta** al cambiar de sonido y al vencer el temporizador, mediante anuncios de
  accesibilidad, no solo cambio visual.
- **Objetivos táctiles ≥ 48 dp** en todo, incluidos los iconos de 20 dp, que van dentro de una caja
  de 44–48 dp.
- **Texto escalable** hasta el 200 % sin recortes: nada de alturas fijas en filas con texto.
- **Movimiento**: el halo se detiene si el sistema tiene las animaciones desactivadas.
- **Contraste**: mínimo 4.5:1 en texto normal y 3:1 en texto grande y elementos gráficos (§3.1).

---

## 11. Estrategia de pruebas

| Nivel | Qué cubre | Herramienta |
|---|---|---|
| Unitario JVM | Generadores de ruido (RMS, deriva de DC, clipping, pendiente espectral por FFT), curva de volumen, cálculo del fade, lógica del temporizador con reloj virtual, repositorio de preferencias | JUnit 4, MockK, Turbine |
| UI | P1 refleja el estado del reproductor; el slider y el aro escriben el mismo valor; la hoja del temporizador devuelve el preset; navegación a Ajustes y Novedades | Compose UI Test |
| Instrumentado | El servicio arranca en primer plano y publica notificación; el `MediaController` se conecta; parar desde la notificación detiene el audio | AndroidJUnit + `MediaController` de prueba |
| Manual obligatorio | Las 13 filas de §7, una por una | Dispositivo físico |
| Manual de resistencia | Una sesión de 8 h real con auriculares, y una escucha crítica de cada sonido | Oído |

Matriz mínima antes de publicar: **API 31** (emulador), **API 33** (emulador, para el permiso de
notificaciones), **API 36** (emulador, edge-to-edge y predictive back) y **un dispositivo físico**
para todo lo que tiene que ver con audio, batería y Doze.

Los tests unitarios y de UI se ejecutan con `./gradlew lint test`, y tienen que quedar en verde antes
de cada commit.

El **recorrido manual completo previo a cada release** está escrito como lista de comprobación en
`docs/play-store-publication-texts.md`, sección «Checklist para publicar»: es la que no se puede
saltar, e incluye el caso del permiso de notificaciones **denegado**, que es el que se olvida.

---

## 12. Plan de acción

Diez hitos secuenciales. Cada uno acaba con algo comprobable: si no se puede demostrar, no está hecho.
El orden no es negociable en las dependencias marcadas.

| # | Hito | Contenido | Hecho cuando |
|---|---|---|---|
| **H0** ✅ | Scaffolding | Proyecto Gradle, version catalog, Hilt, tema oscuro con tokens de §3, fuente Sora empaquetada, navegación con las cuatro pantallas vacías, i18n montada con dos strings de prueba | **Hecho el 2026-08-24.** `lint test assembleDebug` en verde, arranque en 449 ms en un Samsung Z Fold, los tres idiomas comprobados (es, en, y de cayendo a en) y la navegación recorrida. Capturas en `docs/capturas/h0/` |
| **H1** ✅ | Motor de ruido | `NoiseGenerator`, blanco gaussiano, marrón con corrección de DC y limitador, estéreo decorrelacionado | **Hecho el 2026-08-24.** 25 tests en verde. Blanco -18,01 dBFS y pendiente 0,00 dB/oct; marrón -18,12 dBFS y -5,91 dB/oct; continua 1,2·10⁻⁴; picos 0,62 y 0,52; correlación entre canales 0,0003. El espectro medido se vuelca a `build/reports/noise-spectrum.csv` en cada test |
| **H2** ✅ | Puente a Media3 | `NoiseDataSource` sirviendo WAV infinito, `ExoPlayer` configurado (atributos de audio, wake mode, becoming noisy, buffer grande) | **Hecho.** `AudioTrack` en `started`, `USAGE_MEDIA`, estéreo 48 kHz con `FLAG_DEEP_BUFFER` |
| **H3** ✅ | Servicio y notificación | `PlaybackService` sobre `MediaSessionService`, notificación `MediaStyle`, `onTaskRemoved` que no para, permiso de notificaciones | **Hecho.** Sigue sonando en segundo plano, con la pantalla apagada y tras matar la app (RF-10). Notificación en el canal `playback`, `NO_CLEAR`, dos acciones. Faltan por probar a mano las filas de §7 que necesitan hardware: llamada entrante y auriculares |
| **H4** ✅ | Pantalla principal | P1 completa: aro, botón, nombre, slider, píldoras de sonido, crossfade | **Hecho.** Aro, botón, slider, píldoras y fila del temporizador, verificados en el emulador tocando por texto |
| **H5** ✅ | Persistencia y arranque | DataStore con las siete claves, arranque automático (RF-01) con sus cuatro casos | **Hecho.** Cambiar de sonido, cerrar la app y reabrirla devuelve el sonido elegido y arranca solo |
| **H6** ✅ | Temporizador | Hoja P2, cuenta atrás, fade out, +15 min, cancelar, congelar al pausar | **Hecho.** Presets 15/30/45/60/90/120, la cuenta atrás aparece en la fila, y el congelado al pausar y el fade final están cubiertos por `SleepTimerTest` con reloj virtual |
| **H7** ✅ | Ajustes y novedades | P3 y P4 completas, changelog con su catálogo, feedback por correo, selector de idioma | **Hecho.** Cambio a español desde Ajustes: toda la app, incluida la fecha del changelog («24 de agosto de 2026») |
| **H8** 🟨 | Pulido | R8 y reglas de proguard, **copiar los assets del icono** a `res/` y rasterizar el PNG de Play, edge-to-edge, TalkBack completo, revisión de contrastes, splash | **Parcial.** R8 en verde: el APK minificado se instala y suena, sin nada que R8 se llevara. Objetivos táctiles medidos, todos ≥ 48 dp. Falta el recorrido con TalkBack, el icono en un dispositivo real y la sesión de resistencia de 8 h |
| **H9** | Publicación | Firma, pipeline de capturas (6 escenas × 2 idiomas × 3 formatos), publicar la política de privacidad, rellenar la ficha con los textos ya escritos, declaración de servicio en primer plano, test interno | §13 completo, `revisar.py` en verde y la release aceptada en Play Console |

Dependencias duras: H1 antes de H2, H2 antes de H3, H3 antes de H4 (la UI habla con el servicio, no
con el reproductor). H5, H6 y H7 son independientes entre sí una vez cerrado H4.

La sesión de resistencia de 8 h (RNF-01) se ejecuta al terminar H3 y **se repite** al terminar H8:
R8 y el minify pueden romper cosas que funcionaban.

---

## 13. Preparación de la publicación en Play

Lo que hay que tener listo además del código. Es donde se pierden las releases, así que **casi todo
está ya escrito**: lo que queda para H9 es ejecutarlo, no redactarlo.

| Pieza | Dónde está | Estado |
|---|---|---|
| Nombre, descripciones, promo, categoría, etiquetas | `docs/play-store-publication-texts.md` | **Escrito**, con los conteos de caracteres calculados |
| Notas de la versión 1.0 (`es-ES`, `en-US` y el formato con etiquetas) | `docs/play-release-notes.md` | **Escrito** |
| Política de privacidad bilingüe | `docs/privacy-policy/sleep-noise.html` | **Escrita**, pendiente de publicar en GitHub Pages |
| Respuestas de seguridad de los datos y clasificación de contenido | `docs/play-store-publication-texts.md` | **Escritas** |
| Declaración de servicio en primer plano (`mediaPlayback`), en los dos idiomas | `docs/play-store-publication-texts.md` | **Escrita** |
| Checklist de publicación y recorrido manual | `docs/play-store-publication-texts.md` | **Escrito** |
| Pipeline de capturas | `docs/store-assets/generar-capturas/README.md` | **Diseñado**, scripts pendientes de H9 |
| Capturas (36 imágenes) | `docs/store-assets/capturas/` | Pendiente de H9 |
| Icono de la app, las cuatro piezas | `docs/design/icono/vector/` | **Hecho** (ADR 004), pendiente de copiar a `res/` en H8 |
| Icono 512×512 de la ficha | `docs/design/icono/play-icon-512.svg` | **Diseñado**, pendiente de rasterizar a PNG |
| Gráfico de cabecera 1024×500 | — | Pendiente de H8 |

### 13.1 Firma y build

- Keystore de subida **nuevo** para esta app, con Play App Signing activado.
- `keystore.properties` y el `.jks` fuera del repositorio. El `.gitignore` los cubre **antes** del
  primer commit de código, no después.
- Build de release con `isMinifyEnabled` y `isShrinkResources` activados. El generador de ruido no usa
  reflexión, pero hay que confirmar que las reglas que trae Media3 se están aplicando de verdad.
- El keystore se respalda fuera del ordenador de desarrollo antes de publicar. Perderlo es perder la
  capacidad de actualizar la app.

### 13.2 Los textos, y por qué se generan con un script

`scripts/generar-textos-ficha.py` contiene los textos de origen, verifica los límites de Play y
**escribe** `play-store-publication-texts.md` y `play-release-notes.md` con los conteos calculados.
Los límites de Play son duros —30 caracteres el nombre, 80 la descripción corta, 500 las notas de la
versión— y se rechazan al pegar. Contar a mano es como se cuela el error.

Si se edita un texto, se edita en el script y se vuelve a pasar. Editar el markdown a mano deja los
conteos mintiendo, que es peor que no tenerlos.

Las tres cosas que la descripción larga dice **a propósito**, porque son las que evitan una valoración
de una estrella el primer día: que hay **cuatro sonidos y ni uno más**, que el ruido está **generado
y no grabado**, y que la interfaz es **oscura siempre**.

### 13.3 Capturas

Automatizadas: 6 escenas × 2 idiomas × 3 formatos = **36 imágenes**, con `revisar.py` obligatorio
antes de subirlas. El diseño, las escenas y las lecciones portadas de «¡Bebe agua!» están en
`docs/store-assets/generar-capturas/README.md`, y la decisión en el ADR 003.

Dos particularidades de esta app que el pipeline aprovecha o sufre:

- **No hay siembra de datos**, así que se puede capturar con el AAB de release, que es el que se
  publica. En Bebe Agua la siembra con `run-as` obligaba a usar el debug, y con el de release el pase
  terminaba en verde con las capturas vacías.
- **El halo animado rompería el control de idioma** de `revisar.py`, que detecta un idioma colado
  comprobando que la captura `es` y la `en` de la misma escena no sean idénticas. Se desactivan las
  animaciones del sistema durante el pase, que además hace las capturas reproducibles.

### 13.4 Ruta de lanzamiento

1. **Test interno** con el propio autor, para validar el AAB firmado y minificado en un dispositivo
   real, incluida la sesión de resistencia de 8 h.
2. **Test cerrado** si la cuenta de desarrollador lo requiere: las cuentas personales creadas después
   de noviembre de 2023 necesitan 12 testers durante 14 días antes de poder publicar en producción.
   Al haber publicado ya «¡Bebe agua!» con esta cuenta probablemente no aplique, pero **hay que
   confirmarlo en Play Console antes de planificar fechas**: son dos semanas de diferencia.
3. **Producción**, con despliegue por fases al 20 % durante 48 h antes del 100 %.

### 13.5 Documentación que se actualiza con cada release

- `CHANGELOG.md`: la sección de la versión.
- `ChangelogCatalog.kt` y los `string-array` `changelog_*` en inglés y español.
- `scripts/generar-textos-ficha.py`: el bloque de notas de la nueva versión, y volver a pasarlo.

Las tres cosas tienen que decir lo mismo con distinto nivel de detalle: el `CHANGELOG.md` es interno,
la pantalla de Novedades es para quien ya tiene la app, y las notas de Play son para quien todavía no.

## 14. Riesgos

| Riesgo | Impacto | Qué hacemos |
|---|---|---|
| Fabricantes que matan el servicio | El ruido se corta a mitad de la noche y la app parece rota | Wake mode, notificación permanente, y detección del corte para sugerir excluir la app del ahorro de batería |
| `material3` en alpha | Una subida de versión rompe la UI | Fijar versión exacta, revisar el changelog de la librería antes de subirla, no subir durante H9 |
| Valoraciones por «solo cuatro sonidos» | Media de estrellas baja desde el primer día | Decirlo en la descripción corta, y explicar que cada uno hace algo distinto en vez de ser un catálogo |
| Volumen alto durante horas | Riesgo auditivo acumulado, sobre todo con tapones puestos | El nivel sube 6 dB (ADR 006); el volumen del sistema sigue mandando por encima del de la app, y el usuario decide |
| El fade out del temporizador molesta | Alguien lo percibe como que la app «se muere» | 60 s es largo y es lineal en dB; si aparece la queja, hacerlo configurable en la 1.1 |
| El aro de volumen no se descubre | El usuario no sabe cambiar el volumen | Ya mitigado: el slider convencional debajo (§4, P1) |
| Consumo de batería por encima del objetivo | Desinstalaciones y reseñas negativas | Buffer grande en el `AudioTrack`, medida obligatoria en H3 y H8, y si RNF-02 no se cumple, evaluar volver a un bucle pregrabado antes de publicar |
| Revisión de Play por el servicio en primer plano | Retraso de días en el lanzamiento | Preparar la declaración y el vídeo **antes** de subir el AAB, no cuando lo pidan |

---

## 15. Decisiones pendientes de confirmar

Estas cinco no las puedo decidir yo. Ninguna bloquea empezar por H0.

1. **Dirección de correo para el feedback.** Asumida `jjrmobileapps@gmail.com`, la misma de Bebe
   Agua, y **ya escrita** en la política de privacidad y en los textos de la ficha. Si es otra, se
   cambia en un sitio y se vuelve a pasar el script.
2. **URL de la política de privacidad.** El documento está escrito
   (`docs/privacy-policy/sleep-noise.html`); falta **activar GitHub Pages** en este repositorio y
   pegar la URL resultante en Play Console.
3. **Requisito de test cerrado de 12 testers** (§13.4): hay que mirarlo en Play Console. Cambia el
   calendario en dos semanas y es lo único de esta lista que puede retrasar la publicación.
4. **Nombre en la ficha de Play.** Hay dos versiones escritas y contadas: con cola de búsqueda
   («Sleep Noise: ruido blanco» / «Sleep Noise: white noise», 25 y 24 caracteres) o limpio («Sleep
   Noise», 11). La recomendación es la cola en la ficha y «Sleep Noise» a secas en el lanzador.

   La cola decía antes «ruido para dormir», y se cambió: contaba la mitad del producto, porque la app
   sirve igual para tapar el ruido de alrededor. «Ruido blanco» es además lo que la gente escribe en
   el buscador y no inclina la app hacia uno de los dos usos.

   **Queda un asunto más grande que la cola: el nombre mismo.** «Sleep Noise» nombra un solo
   propósito, y ahora hay dos. Cambiarlo obliga a cambiar `applicationId`, que en Play es
   irreversible, así que la decisión es ahora o nunca.
5. **Presets del temporizador** (§4, P2): la lista propuesta es 15/30/45/60/90/120. Si prefieres otra,
   es el momento de cambiarla, porque el último preset usado se persiste y aparece en las capturas.

---

## 16. Definición de «listo para publicar»

La 1.0 se sube cuando **todo** esto es cierto:

- [ ] Los 20 requisitos funcionales de §5 comprobados en un dispositivo físico.
- [ ] Los 11 requisitos no funcionales de §6 medidos, con sus números anotados.
- [ ] Las 13 filas de §7 probadas una a una.
- [ ] Sesión de resistencia de 8 h superada **con el build de release minificado**.
- [ ] `./gradlew lint test` en verde, sin avisos suprimidos a mano.
- [ ] Recorrido completo con TalkBack, y Accessibility Scanner sin hallazgos críticos.
- [ ] La app entera revisada en inglés, en español y con el teléfono en un tercer idioma.
- [ ] Ficha de Play completa en los dos idiomas, con los textos de
      `docs/play-store-publication-texts.md` pegados tal cual.
- [ ] Las 36 capturas generadas y `revisar.py` en verde.
- [ ] Política de privacidad publicada en GitHub Pages y accesible desde Play Console.
- [ ] Declaración de servicio en primer plano enviada.
- [ ] `CHANGELOG.md`, `ChangelogCatalog.kt`, los `string-array` y `docs/play-release-notes.md`
      actualizados y coherentes entre sí.
- [ ] Keystore respaldado fuera del ordenador de desarrollo.
