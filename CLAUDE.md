# CLAUDE.md — Sleep Noise

Guía de trabajo para este repositorio. Léela entera antes de tocar nada.

> **Estado del proyecto: fase 0.** Todavía no hay código. Existe el análisis técnico
> (`docs/analisis-tecnico.md`) y este documento. La siguiente fase es el diseño de la interfaz;
> después, el scaffolding del proyecto Gradle.

---

## 1. Visión del producto

Una app para dormir con ruido. Se abre, suena, y no pide nada más. La referencia funcional es
[White Noise de TMSOFT](https://play.google.com/store/apps/details?id=com.tmsoft.whitenoise.full),
pero solo su núcleo: escuchar un ruido y poder cambiarlo por otro.

### Principios de diseño

- **Cero fricción.** Abrir la app ya es escuchar. Sin bienvenida, sin login, sin pulsar play.
- **Silencio de interfaz.** Quien usa esto está a punto de dormirse, con la luz apagada. La UI no
  compite por la atención.
- **Funciona con la pantalla apagada y durante horas.** Es el caso de uso principal, no un extra.
- **Sin red, sin cuentas, sin analítica.** La app no necesita internet para nada.

---

## 2. Funcionalidad de la v1

| # | Funcionalidad | Estado |
|---|---|---|
| F1 | Ruido blanco (gaussiano) y ruido marrón, en bucle indefinido | ⬜ Pendiente |
| F2 | Cambio de sonido en caliente, con crossfade | ⬜ Pendiente |
| F3 | Al abrir la app suena el último sonido escuchado (por defecto, marrón) | ⬜ Pendiente |
| F4 | Volumen a nivel medio al arrancar, con slider en la pantalla principal | ⬜ Pendiente |
| F5 | Notificación `MediaStyle` para pausar y parar | ⬜ Pendiente |
| F6 | Temporizador de apagado con fade out | ⬜ Pendiente |
| F7 | Ajustes: changelog, versión, feedback por email, idioma | ⬜ Pendiente |
| F8 | Inglés y español, con fallback a inglés y cambio desde Ajustes | ⬜ Pendiente |

**Fuera de la v1**: ruido rosa/gris, sonidos naturales, mezclas simultáneas, ecualizador, widget,
tile de ajustes rápidos, Wear OS, alarma de despertador.

---

## 3. Stack técnico

**Vinculante** (no cambiar sin justificarlo con un ADR en `docs/decisions/`):

| Capa | Decisión |
|---|---|
| Lenguaje | Kotlin **2.3.21** |
| UI | Jetpack Compose con BOM **2026.08.00** |
| Material | **Material 3 Expressive** — `androidx.compose.material3` **1.5.0-alpha26**, fijado por encima de la BOM |
| Audio | **Media3 1.11.0**: `media3-exoplayer`, `media3-session`, `media3-ui-compose` |
| `minSdk` | 31 (Android 12) |
| `targetSdk` / `compileSdk` | 36 (Android 16) |
| Build | Gradle Kotlin DSL + version catalog (`libs.versions.toml`), wrapper **9.5.0** |
| AGP | **9.3.1** |
| KSP | **2.3.7** (para Hilt; **no usar kapt**) |
| Arquitectura | MVVM + UDF, capas `ui` / `domain` / `data` |
| DI | Hilt **2.59.2** |
| Persistencia | **Solo DataStore Preferences 1.1.4**. **Sin Room**: no hay historial, solo preferencias |
| Navegación | Navigation Compose **2.9.0** |
| Background | **`MediaSessionService` de Media3** como foreground service `mediaPlayback`. **Sin `AlarmManager`** ni `WorkManager` |
| Concurrencia | Coroutines **1.10.2** + Flow |
| i18n | `values/` en inglés (base y fallback) + `values-es/`; `locale_config.xml`; cambio en runtime con `AppCompatDelegate.setApplicationLocales` |
| Tests | JUnit 4 + MockK + Turbine + Compose UI Test |
| Logs | Timber **5.0.1**, solo en debug |
| Backup | `android:allowBackup="false"` |

### Permisos requeridos

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

No se declara `SCHEDULE_EXACT_ALARM`: el temporizador es una corrutina dentro del servicio en
foreground, que es fiable mientras hay audio sonando.

---

## 4. Audio: lo que hay que entender antes de tocarlo

**El ruido se genera en tiempo real, no se reproduce desde ficheros.** El razonamiento completo está
en `docs/analisis-tecnico.md` §3 y en `docs/decisions/001-sintesis-procedural-de-ruido.md`. En resumen:
los codecs perceptuales destrozan el ruido blanco de forma audible, el PCM sin comprimir pesa 5,5 MB
por minuto, y cualquier bucle acaba siendo detectable por el oído.

Reglas:

- La síntesis vive en `audio/` y es **matemática pura, sin dependencias de Android**. Así se testea en
  JVM: RMS, offset de DC, ausencia de clipping y pendiente espectral con FFT.
- Se conecta a ExoPlayer mediante un `DataSource` propio que sirve cabecera WAV más PCM sintetizado en
  un stream de longitud infinita.
- El ruido marrón necesita corrección de deriva de DC, normalización de sonoridad respecto al blanco y
  un limitador suave. Sin eso, se va a clipping y se percibe mucho más flojo.
- Estéreo con **dos generadores independientes**, uno por canal. Duplicar el mismo canal suena como un
  punto fijo en el centro de la cabeza.
- **Nunca parar el audio en seco.** Todo arranque, parada y cambio de sonido pasa por una rampa de
  volumen (`Fader`). Un corte abrupto despierta a quien se estaba durmiendo, y produce un clic audible.

### Configuración del reproductor que no se toca sin motivo

- `AudioAttributes`: `USAGE_MEDIA` + `CONTENT_TYPE_MUSIC`, `handleAudioFocus = true`.
- `handleAudioBecomingNoisy(true)`: al desenchufar auriculares se pausa. La alternativa es ruido a todo
  volumen por el altavoz a las cuatro de la mañana.
- `setWakeMode(C.WAKE_MODE_LOCAL)`: sin esto Doze corta el audio en sesiones largas.
- `onTaskRemoved`: **no** parar la reproducción. Va contra la recomendación por defecto de Google, que
  está pensada para música; aquí el usuario cierra la app precisamente porque se va a dormir.
- Buffer del `AudioTrack` grande (`setAudioTrackBufferSizeProvider`): menos despertares de CPU en una
  sesión de ocho horas. La latencia es irrelevante en esta app.

---

## 5. Internacionalización

**Requisito**: inglés y español. Sistema en inglés → inglés; en español → español; **cualquier otro
idioma → inglés**. Y cambiable desde Ajustes.

- **Inglés es el idioma base**, en `res/values/strings.xml`. Eso *es* el fallback: un dispositivo en
  alemán no encuentra `values-de` y cae en `values/`. No hace falta lógica ninguna.
- Español en `res/values-es/strings.xml`, **sin cualificador de región**, para cubrir `es-ES`, `es-MX`, etc.
- `res/xml/locale_config.xml` con `en` y `es`, referenciado desde el manifest con `android:localeConfig`.
- Cambio en Ajustes con `AppCompatDelegate.setApplicationLocales()`:
  `LocaleListCompat.getEmptyLocaleList()` para «Automático (sistema)»,
  `LocaleListCompat.forLanguageTags("en"|"es")` para forzar uno. La Activity hereda de `AppCompatActivity`.
- `androidResources { localeFilters += listOf("en", "es") }`, para que las traducciones de las librerías
  de AndroidX no metan cuarenta idiomas en el APK ni descuadren el selector del sistema.

Reglas:

- **Nunca hardcodees un string en un Composable.** Todo por `stringResource`.
- Cada string nuevo se añade **a la vez** en `values/strings.xml` y `values-es/strings.xml`. Un string
  sin traducir no es un aviso de lint: es una frase en inglés en medio de una pantalla en español.
- Los textos de la notificación también van por recursos: los genera el servicio.
- Números, duraciones y fechas con `java.time` y `NumberFormat` sensibles al locale. Nunca concatenando.

---

## 6. Convenciones de código

- **Idioma del código y de los comentarios técnicos**: inglés. Strings de UI: recursos i18n.
  La documentación del repo (`docs/`, este fichero): español.
- **Package raíz**: `com.jjrapps.sleepnoise`.
- **Nomenclatura Compose**: una pantalla son tres ficheros — `PlayerScreen.kt`, `PlayerViewModel.kt`,
  `PlayerUiState.kt`.
- **Estado de pantalla**: `sealed interface XxxUiState` con `Loading`, `Success(data)`, `Error`.
  Eventos de una sola vez (snackbars, navegación) por `Channel<UiEvent>`.
- **Inyección por constructor** siempre. Nada de `@Inject lateinit`.
- **No usar `LiveData`** ni RxJava. Solo Flow/StateFlow.
- **No usar XML para UI.** Todo Compose. Única excepción tolerable: el splash con `androidx.core.splashscreen`.
- **Previews**: cada Composable público con al menos un `@Preview`, usando `@PreviewLightDark`.
- Lógica de negocio en `ViewModel` o `UseCase`. Nunca en Composables ni en la `Activity`.

---

## 7. Cómo trabajar en este repo

### Antes de cada tarea

1. Lee este fichero entero y `docs/analisis-tecnico.md`. Si la tarea contradice algo de aquí, pregunta
   antes de tirar adelante.
2. Mira el último commit y `git status` para no pisar cambios.
3. Si vas a tocar UI, abre `Theme.kt`, `Color.kt`, `Type.kt` y `Shape.kt` antes de inventarte estilos.

### Al hacer cambios

- Strings en `strings.xml`, en los **dos** idiomas. Ver §5.
- Si subes `versionCode`/`versionName` → actualiza `CHANGELOG.md`, los `string-array` `changelog_*`
  (EN y ES) y `ChangelogCatalog.kt`.
- Si la versión se publica en Play → añade su bloque de novedades en `docs/play-release-notes.md`, con
  sus tres subsecciones: `es-ES`, `en-US` (máximo 500 caracteres cada una) y **el formato con etiquetas
  de idioma**, que repite ambos textos envueltos en `<es-ES>`/`<en-US>` para pegarlos de una vez en
  Play Console. La tercera no es opcional: sin ella hay que copiar idioma por idioma.
- Si tocas audio o el servicio → prueba **una sesión larga real** con auriculares y pantalla apagada.
  Los tests no te dicen si suena bien.
- Si tocas notificaciones o permisos → prueba en emulador con Android 12, 13 y 16: la lógica cambia en
  cada uno.
- No añadas dependencias sin justificarlas y sin actualizar `libs.versions.toml`.

### Al terminar una tarea

- `./gradlew lint test`, todo en verde.
- Commit con [Conventional Commits](https://www.conventionalcommits.org/): `feat(player): …`,
  `fix(playback): …`, `refactor(audio): …`.
- Si has tomado decisiones técnicas no triviales (cambio de stack, librería nueva, workaround), añade
  un ADR ligero en `docs/decisions/NNN-titulo.md`.

### Lo que NO hacer (rojo)

- No usar WebView, Cordova, Capacitor, React Native, Flutter ni KMP para la UI. Esto es Android nativo
  en Compose.
- No mezclar layouts XML con Compose.
- No empaquetar ficheros de audio para los ruidos. Se generan. Ver §4.
- No usar `runBlocking` fuera de tests.
- No declarar `SCHEDULE_EXACT_ALARM` ni `USE_EXACT_ALARM`.
- No parar la reproducción cuando el usuario desliza la app fuera de recientes.
- No añadir analítica de terceros, crash reporting con telemetría ni SDKs de marketing en la v1.
- No subir secretos, claves de Play Console ni el `keystore` al repo.

---

## 8. Roadmap posterior a la v1

Sin compromiso de fechas ni de orden:

- Ruido rosa y ruido gris (mismo motor, otro filtro).
- Ecualizador simple: un control de «brillo» que interpola entre pendientes espectrales.
- Widget de escritorio con Glance y tile de ajustes rápidos.
- Fade in progresivo al arrancar, configurable.
- Mezcla de dos ruidos simultáneos.
- Wear OS.

---

## 9. Referencias

- Análisis técnico completo: `docs/analisis-tecnico.md`
- [Media3 1.11: What's new](https://android-developers.googleblog.com/2026/08/media3-1-11-whats-new.html)
- [Background playback with MediaSessionService](https://developer.android.com/media/media3/session/background-playback)
- [Compose Material3 release notes](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Per-app language preferences](https://developer.android.com/guide/topics/resources/app-languages)
