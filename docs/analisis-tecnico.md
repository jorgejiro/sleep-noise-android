# Sleep Noise — Análisis técnico y plan de implementación

> Documento de partida, escrito antes de la primera línea de código. Recoge las decisiones
> técnicas tomadas y por qué. Si una decisión cambia, se actualiza aquí **y** se añade un ADR
> en `docs/decisions/`.
>
> Fecha: 2026-08-24 · Estado: aprobado, pendiente de la fase de diseño de UI.

---

## 1. Visión del producto

> **Nota posterior (ADR 006).** Este documento se escribió con un solo propósito en la cabeza:
> dormir. El segundo —tapar el ruido de alrededor con auriculares— apareció después y cambió el
> sonido por defecto, el nivel de salida y el número de sonidos. La visión vigente está en
> `CLAUDE.md` §1 y en la especificación §1.

Una app para dormir con ruido. Se abre, suena, y no pide nada más. La referencia funcional es
[White Noise de TMSOFT](https://play.google.com/store/apps/details?id=com.tmsoft.whitenoise.full),
pero solo en su núcleo: escuchar un ruido y poder cambiarlo por otro. Nada de catálogos de cientos
de sonidos, mezclas ni suscripciones.

Principios:

- **Cero fricción.** Abrir la app ya es escuchar. Sin pantalla de bienvenida, sin login, sin pulsar play.
- **Silencio de interfaz.** Quien usa esto está a punto de dormirse, con la luz apagada. La UI no
  compite por la atención.
- **Funciona con la pantalla apagada y durante horas.** Es el caso de uso principal, no un extra.
- **Sin red, sin cuentas, sin analítica.** La app no necesita internet para nada.

---

## 2. Alcance funcional de la v1

| # | Funcionalidad | Detalle |
|---|---|---|
| F1 | Reproducción de ruido | ~~Dos sonidos~~ → **cuatro**: blanco, rosa, marrón y enmascarador (ADR 006). En bucle indefinido. |
| F2 | Cambio de sonido en caliente | Sin cortes audibles: crossfade corto entre el sonido saliente y el entrante. |
| F3 | Arranque automático | Al abrir la app se reproduce **el último sonido escuchado**; tras instalar, ~~marrón~~ → el **enmascarador** (ADR 006). |
| F4 | Volumen | Arranca a nivel medio (0.5 del volumen interno de la app, curva perceptual). Slider en la pantalla principal. |
| F5 | Notificación de control | `MediaStyle` con play/pausa (+ stop y estado del temporizador). Sobrevive a app en background y pantalla apagada. |
| F6 | Temporizador de apagado | Presets (15/30/45/60/90 min + personalizado) con **fade out** en el último minuto. Cuenta atrás visible en app y notificación. |
| F7 | Ajustes | Changelog, versión (`versionName` + `versionCode`), enviar feedback por email, idioma. Mismo patrón que Bebe Agua. |
| F8 | Idiomas | Inglés y español, siguiendo el idioma del sistema con fallback a inglés, y cambiable desde Ajustes. Ver §6. |

**No en v1** (candidatos a roadmap): ruido rosa/gris, sonidos naturales, mezcla de varios sonidos
simultáneos, ecualizador, widget de escritorio, tile de ajustes rápidos, Wear OS, alarma de despertador.

---

## 3. La decisión técnica central: síntesis procedural, no ficheros de audio

Es la decisión que condiciona todo lo demás, y hay un argumento fuerte para no usar assets de audio.

**El ruido blanco es el peor caso posible para un codec perceptual.** Opus y AAC asumen que hay
estructura tonal que explotar; el ruido gaussiano no tiene ninguna, así que a bitrates razonables
(64–96 kbps) aparece un artefacto metálico y burbujeante perfectamente audible con auriculares a las
tres de la mañana. La alternativa, PCM sin comprimir, cuesta unos 5,5 MB por minuto de audio mono a
48 kHz. Y un bucle, por largo que sea, es detectable: el oído reconoce el patrón tras unas cuantas
repeticiones, y con ruido marrón hay además que resolver la costura de continuidad de DC.

**Decisión: generar el ruido en tiempo real.** Ventajas:

- APK de unos 3 MB, sin un solo byte de audio empaquetado.
- Calidad exacta: gaussiano real, sin costuras y sin artefactos de compresión.
- Un ruido nuevo son unas pocas líneas de matemáticas, no un fichero que producir y traducir a varias densidades de calidad.
- Coste de CPU despreciable: 48 000 muestras por segundo de aritmética simple es menos del 1 % de CPU
  en cualquier móvil de los últimos diez años.

### 3.1 Algoritmos

- **Blanco gaussiano**: PRNG rápido (xoshiro256++ / xorshift128+) alimentando Box-Muller en su forma
  polar, con caché del segundo valor generado. Escalado a unos -18 dBFS RMS para dejar headroom.
- **Marrón**: integrador con fuga, `y[n] = (1-a)·y[n-1] + a·x[n]`, que da la pendiente de -6 dB/octava
  característica. Necesita corrección de deriva de DC, normalización RMS igualada por sonoridad
  respecto al blanco (si no, el marrón se percibe mucho más flojo a igual amplitud) y un limitador
  suave, porque sin control se va a clipping.
- **Estéreo decorrelacionado**: dos generadores independientes, uno por canal. Con auriculares suena
  amplio en lugar de un punto fijo en el centro de la cabeza. Es un detalle que se nota mucho en
  este tipo de app.

Todo esto es **matemática pura sin dependencias de Android**, así que se testea en JVM: RMS, offset
de DC, ausencia de clipping y pendiente espectral con una FFT, en tests unitarios normales.

### 3.2 Cómo se conecta a Media3

| Opción | Cómo | Trade-off |
|---|---|---|
| **A (elegida)** | `DataSource` propio que sirve una cabecera WAV más PCM sintetizado, en un stream de longitud «infinita». ExoPlayer lo consume con su `WavExtractor` como cualquier otra fuente. | Heredamos **gratis** de ExoPlayer: audio focus, `handleAudioBecomingNoisy`, `WAKE_MODE_LOCAL`, gestión del `AudioTrack` y buffering. Es la vía con menos código propio y menos riesgo. |
| B | `SimpleBasePlayer` (API pública de Media3 pensada para envolver reproductores que no son ExoPlayer) escribiendo directamente en un `AudioTrack`. | Control total del pipeline, pero hay que reimplementar a mano audio focus, wake lock y ciclo de vida del `AudioTrack`. Solo merecería la pena si más adelante hiciera falta mezclar varios ruidos simultáneos. |

Vamos con **A**, dejando la síntesis aislada detrás de una interfaz `NoiseGenerator` para que un
cambio a B más adelante no toque nada más.

**Detalle de batería**: para una sesión de ocho horas conviene aumentar el buffer del `AudioTrack`
(vía `DefaultAudioSink.Builder().setAudioTrackBufferSizeProvider`). Buffers grandes significan menos
despertares de CPU por segundo. La latencia nos da igual: nadie pulsa play esperando respuesta en 10 ms.

---

## 4. Arquitectura de reproducción

```
MainActivity (Compose)
   │  MediaController (media3-session)
   ▼
PlaybackService : MediaSessionService     ← FGS tipo mediaPlayback
   ├── ExoPlayer
   │     └── NoiseDataSource.Factory → NoiseGenerator (White | Brown)
   ├── MediaSession  → notificación MediaStyle automática
   ├── SleepTimerController (corrutina + fade out)
   └── DataStore: último sonido, volumen, último temporizador usado
```

Puntos concretos:

- **`MediaSessionService`** en lugar de un `Service` propio: Media3 construye y mantiene la
  notificación `MediaStyle`, responde a los botones de los auriculares y al control de medios del
  sistema, y gestiona la promoción y salida de foreground.
  Manifest: permisos `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` y `POST_NOTIFICATIONS`,
  y el servicio con `android:foregroundServiceType="mediaPlayback"`.
- **`AudioAttributes`**: `USAGE_MEDIA` + `CONTENT_TYPE_MUSIC`, con `handleAudioFocus = true`. Así el
  ruido respeta el volumen de multimedia y hace duck ante una notificación en vez de callarse del todo.
- **`handleAudioBecomingNoisy(true)`**: al desenchufar los auriculares se pausa. Contraintuitivo para
  una app de sueño hasta que se piensa en la alternativa: ruido blanco a todo volumen por el altavoz
  a las cuatro de la mañana.
- **`setWakeMode(C.WAKE_MODE_LOCAL)`**: sin esto Doze corta el audio en sesiones largas.
- **`onTaskRemoved`**: deslizar la app fuera de recientes **no** debe parar el ruido. Esto va en contra
  de la recomendación por defecto de Google, que está pensada para música: aquí el usuario cierra la
  app precisamente porque se va a dormir. Se para desde la notificación o al vencer el temporizador.
- **`onPlaybackResumption`**: permite reanudar desde el control de medios del sistema sin abrir la app.
- **Media3 1.11 cambia el comportamiento por defecto de `MediaSession.Callback.onConnect`**: los
  controladores no confiables ya no reciben datos de sesión por defecto. Nuestro `MediaController` es
  de la misma app, así que no hay que hacer nada, pero conviene tenerlo presente si algún día se
  quiere integración con Android Auto o Assistant.

### 4.1 Qué aporta Media3 1.11 en concreto

- **`media3-ui-compose`**: `rememberPlayPauseButtonState`, `rememberCurrentMediaItemState`,
  `rememberErrorState`. Elimina el boilerplate de `Player.Listener` → `StateFlow` que normalmente se
  escribe a mano. Es la razón principal para usar 1.11 y no 1.10.
- Corrección del `ForegroundServiceStartNotAllowedException` en cargas asíncronas y threading más
  estricto de `MediaSession`.
- `PlayerPool` / `rememberPooledPlayer` y los muxers nuevos (`OggMuxer`, `WavMuxer`) no aplican a este caso.

**Media3 no trae sleep timer ni fades.** Ambos son código nuestro.

### 4.2 Temporizador

Corrutina en el servicio, con `delay` sobre un instante absoluto persistido, **sin `AlarmManager`**.
Mientras suena el ruido el servicio está en foreground, así que la corrutina es fiable, y nos evitamos
`SCHEDULE_EXACT_ALARM`, que en esta categoría de app sería una fricción innecesaria con Google Play.

Fade out lineal en dB durante los últimos 60 segundos y `pause()` al final: parar en seco puede
despertar a quien justo se estaba durmiendo. La notificación muestra el tiempo restante y ofrece
«+15 min» y «cancelar» como `CommandButton` del custom layout de la sesión.

---

## 5. Stack técnico

Alineado con [Bebe Agua](https://github.com/jorgejiro) salvo donde el dominio pide otra cosa.

| Capa | Decisión | Nota |
|---|---|---|
| Lenguaje | Kotlin 2.4.10 | |
| UI | Jetpack Compose, BOM **2026.08.00** | Trae Compose 1.12.0 estable |
| Material | **material3 1.5.0-alpha26** (override sobre la BOM) | Material 3 Expressive. Ver aviso abajo |
| Audio | **Media3 1.11.0** (`media3-exoplayer`, `media3-session`, `media3-ui-compose`) | Estable desde julio de 2026 |
| `minSdk` | 31 (Android 12) | Coherente con Bebe Agua |
| `compileSdk` | **37** | Lo exigen la Compose BOM 2026.08 y `material3` 1.5.0-alpha. Ver ADR 005 |
| `targetSdk` | 36 (Android 16) | Edge-to-edge obligatorio, predictive back |
| Build | AGP 9.3.2, Gradle 9.7.1, version catalog (`libs.versions.toml`) | |
| Arquitectura | MVVM + UDF, capas `ui` / `domain` / `data` | Igual |
| DI | Hilt 2.60.1 + KSP 2.3.11 (nunca kapt) | `@AndroidEntryPoint` en el servicio |
| Persistencia | **Solo DataStore Preferences 1.1.4** | **Sin Room**: no hay historial que guardar, solo cuatro preferencias |
| Navegación | Navigation Compose 2.9.0 | Tres destinos |
| Concurrencia | Coroutines 1.11.0 + Flow | |
| Tests | JUnit 4 + MockK 1.14.11 + Turbine 1.2.1 + Compose UI Test | La síntesis de ruido se testea en JVM puro |
| Logs | Timber 5.0.1, solo en debug | Igual |
| Backup | `android:allowBackup="false"` | Igual |

**Aviso sobre Material 3 Expressive**: las APIs expressive (`MaterialExpressiveTheme`,
`expressiveLightColorScheme`, formas morphing, `WavyProgressIndicator`, `ButtonGroup`,
`FloatingToolbar`, `motionScheme`) están **graduadas a estables dentro de la rama 1.5.0-alpha**: el
artefacto sigue siendo alpha, aunque esas APIs concretas ya no requieren `@OptIn`. No existe un
artefacto `material3-expressive` separado. Usarlo implica fijar `material3` a `1.5.0-alpha26` por
encima de la BOM y asumir que habrá que subir versión cada pocas semanas. Para esta app es aceptable:
el riesgo real de una alpha de Compose es superficie de UI, no corrupción de datos, y aquí no hay
datos que corromper.

Más allá de la estética, Expressive aporta algo pertinente: `WavyProgressIndicator` es literalmente
una forma de onda, y `MaterialShapes` da formas orgánicas. Encaja con «ruido» mejor que cualquier
componente estándar. Esto se concreta en la fase de diseño.

---

## 6. Internacionalización

**Requisito**: la app soporta inglés y español. Si el sistema está en inglés, inglés; si está en
español, español; **para cualquier otro idioma del dispositivo, inglés**. Y el idioma se puede cambiar
desde Ajustes, independientemente del idioma del sistema.

Implementación (misma que en Bebe Agua, que ya está validada en producción):

1. **Inglés como idioma base**: los strings por defecto viven en `res/values/strings.xml`. Esto es
   precisamente lo que da el fallback pedido: un dispositivo en alemán, francés o japonés no encuentra
   un `values-de`/`values-fr`/`values-ja` y cae en `values/`, es decir, inglés. No hace falta ninguna
   lógica: es el mecanismo de resolución de recursos de Android.
2. **Español en `res/values-es/strings.xml`**. Sin cualificador de región, para que cubra `es-ES`,
   `es-MX`, `es-AR` y el resto.
3. **`res/xml/locale_config.xml`** declarando `en` y `es`, referenciado desde el manifest con
   `android:localeConfig="@xml/locale_config"`. Esto hace que el selector de idioma por app de Android 13+
   («Ajustes del sistema → Idiomas → Sleep Noise») aparezca con las dos opciones correctas.
4. **Cambio desde los Ajustes de la app** con `AppCompatDelegate.setApplicationLocales()`:
   - `LocaleListCompat.getEmptyLocaleList()` para la opción «Automático (sistema)»,
   - `LocaleListCompat.forLanguageTags("en")` o `("es")` para forzar uno.

   En API 33+ AppCompat delega en el `LocaleManager` del sistema, así que la preferencia la persiste el
   propio sistema operativo y sobrevive a reinstalaciones de la configuración. Con `minSdk` 31 hay que
   mantener también la copia en DataStore para los dos niveles de API por debajo de 33, igual que en
   Bebe Agua. Requiere `androidx.appcompat` y que la Activity herede de `AppCompatActivity`.
5. **Tres opciones en Ajustes**: Automático (sistema) · English · Español. Por defecto, Automático.
6. **`androidResources { localeFilters += listOf("en", "es") }`** en `build.gradle.kts`, para que las
   traducciones que arrastran las librerías de AndroidX no metan en el APK cuarenta idiomas que la app
   no soporta y que descuadrarían el selector del sistema.

Reglas de trabajo derivadas:

- **Ningún string hardcodeado en Composables.** Todo pasa por `stringResource`.
- Cada string nuevo se añade a la vez en `values/strings.xml` (EN) y `values-es/strings.xml` (ES).
  Un string sin traducir no es un aviso de lint: es una frase en inglés en medio de una pantalla en español.
- El changelog también se traduce: cada versión es un `string-array` en los dos ficheros (ver §7).
- Formato de números, duraciones y fechas con las APIs de `java.time` y `NumberFormat` sensibles al
  locale, nunca concatenando strings a mano.
- Textos de la notificación incluidos: los genera el servicio, que también debe resolverlos vía recursos.

---

## 7. Estructura de proyecto propuesta

```
app/src/main/java/com/jjrapps/sleepnoise/
├── SleepNoiseApplication.kt
├── MainActivity.kt
├── audio/
│   ├── NoiseGenerator.kt          // interfaz + PRNG xoshiro256
│   ├── WhiteNoiseGenerator.kt     // Box-Muller
│   ├── BrownNoiseGenerator.kt     // integrador con fuga + limitador
│   ├── NoiseDataSource.kt         // DataSource de Media3, WAV infinito
│   └── Fader.kt                   // rampas de volumen en dB
├── playback/
│   ├── PlaybackService.kt         // MediaSessionService
│   ├── PlaybackConnection.kt      // MediaController ↔ UI
│   ├── SleepTimerController.kt
│   └── NoiseNotificationProvider.kt
├── data/
│   ├── datastore/PlaybackPreferencesDataSource.kt
│   └── repository/PlaybackPreferencesRepositoryImpl.kt
├── domain/
│   ├── model/{NoiseType, PlaybackState, SleepTimer}.kt
│   ├── repository/PlaybackPreferencesRepository.kt
│   └── usecase/…
├── di/{AudioModule, DataStoreModule, RepositoryModule}.kt
└── ui/
    ├── player/{PlayerScreen, PlayerViewModel, PlayerUiState}.kt
    ├── timer/{TimerSheet…}.kt
    ├── settings/{SettingsScreen, SettingsViewModel, SettingsUiState}.kt
    ├── changelog/{ChangelogScreen, ChangelogViewModel, ChangelogCatalog}.kt
    ├── navigation/{NavGraph, Screen}.kt
    └── theme/{Theme, Color, Type, Shape}.kt

app/src/main/res/
├── values/strings.xml       // inglés (base y fallback)
├── values-es/strings.xml    // español
└── xml/locale_config.xml    // en, es
```

La sección de Ajustes replica el patrón que ya funciona en Bebe Agua: filas `SettingRow`/`InfoRow`
dentro de tarjetas, `ChangelogCatalog` con un `string-array` localizado por versión, y feedback vía
`ACTION_SENDTO` con URI `mailto:`. El asunto lleva `versionName` y `versionCode`, y va **tanto en la
URI como en `EXTRA_SUBJECT`**, porque Gmail lee la URI e ignora el extra mientras otros clientes hacen
lo contrario: poner ambos es lo que hace que funcione en todas partes.

---

## 8. Riesgos y decisiones abiertas

1. **Arranque automático con sonido al abrir la app** (F3). Funciona muy bien de noche y es incómodo
   si abres la app en una reunión. Se implementa tal cual, **por defecto activado**, pero con un
   interruptor en Ajustes para desactivarlo. Si la app ya está sonando, abrirla no reinicia nada.
2. **`POST_NOTIFICATIONS` en Android 13+**: sin ese permiso el servicio arranca pero la notificación no
   se ve, y el usuario se queda sin forma de pausar salvo abriendo la app. Hay que pedirlo en el primer
   arranque, explicando para qué es.
3. **Fabricantes agresivos** (Xiaomi, Samsung, Huawei) matan servicios en foreground de larga duración
   pese a las reglas de la plataforma. Mitigación: wake mode, notificación ongoing, y detectar el caso
   para sugerir excluir la app de la optimización de batería si se detectan cortes.
4. **Alpha de Material 3**: dependencia que habrá que ir subiendo cada pocas semanas.
5. **Verificación auditiva real**: los tests unitarios confirman las matemáticas, no que suene bien.
   Antes de publicar hay que probar con auriculares una sesión larga de verdad.

---

## 9. Plan de trabajo por fases

| Fase | Contenido | Verificable con |
|---|---|---|
| 0 | Scaffolding: Gradle y version catalog, Hilt, tema Expressive, navegación vacía, i18n en su sitio | `./gradlew assembleDebug` |
| **Diseño** | Alternativas de diseño de UI (comando `/design`) | Mockups |
| 1 | Motor de ruido: generadores y `NoiseDataSource` | Tests JVM de RMS, DC, clipping y pendiente espectral |
| 2 | `PlaybackService` + `MediaSession` + notificación + audio focus | Suena en background, con pantalla apagada, controles del sistema |
| 3 | Pantalla principal: play/pausa, cambio de sonido con crossfade, volumen | Test de UI y prueba a oído |
| 4 | Persistencia y arranque automático | Test del repositorio |
| 5 | Temporizador con fade out y controles en la notificación | Test del controlador con reloj virtual |
| 6 | Ajustes, changelog, feedback e i18n EN/ES completa | `./gradlew lint test` |
| 7 | Pulido: R8, iconos, edge-to-edge, accesibilidad (TalkBack), assets de Play | Sesión de ocho horas real en dispositivo |

---

## 10. Referencias

- [Media3 1.11: What's new (Android Developers Blog)](https://android-developers.googleblog.com/2026/08/media3-1-11-whats-new.html)
- [Release notes de Media3 1.11.0 (GitHub androidx/media)](https://github.com/androidx/media/releases/tag/1.11.0)
- [Background playback with MediaSessionService](https://developer.android.com/media/media3/session/background-playback)
- [Compose Material3 release notes](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [What's new in the Jetpack Compose August '26 release](https://android-developers.googleblog.com/2026/08/jetpack-compose-august-2026-release.html)
- [Per-app language preferences](https://developer.android.com/guide/topics/resources/app-languages)
