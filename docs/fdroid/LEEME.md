# Publicación en F-Droid

F-Droid no admite subir APK: compila la app desde este repositorio con una **receta** que vive en
[`fdroiddata`](https://gitlab.com/fdroid/fdroiddata), y la firma con su propia clave. Publicar es
abrir un merge request allí con la receta.

| Pieza | Dónde |
|---|---|
| Receta, validada con `fdroid lint` y en el formato de `fdroid rewritemeta` | [`com.jjrapps.sleepnoise.yml`](com.jjrapps.sleepnoise.yml) |
| Ficha: título, descripciones, changelog, icono, gráfico y capturas | `fastlane/metadata/android/{en-US,es-ES}/` — F-Droid la lee del repositorio |

## Enviar la app

1. Crear una cuenta en [GitLab](https://gitlab.com/users/sign_up), si no la hay.
2. Hacer fork de [`fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata).
3. En el fork, crear una rama `com.jjrapps.sleepnoise` y añadir la receta como
   `metadata/com.jjrapps.sleepnoise.yml`. Se puede hacer desde la web de GitLab, sin clonar: el repo
   pesa varios GB.
4. Commit: `New app: Sleep Noise`.
5. Abrir el merge request contra `fdroid/fdroiddata:master` y rellenar la checklist de la plantilla.
   La CI del merge request compila la app: si falla, el log dice por qué.
6. Contestar a los revisores. La revisión la hacen voluntarios y puede tardar semanas.

## Lo que hay que saber

- **La firma es la de F-Droid, no la tuya.** Quien instala desde Play no puede actualizar desde
  F-Droid, ni al revés, sin desinstalar antes. Para firmar con la clave propia hacen falta builds
  reproducibles, y es un trabajo aparte.
- **La receta de la 1.1.1 apunta a un commit, no al tag `v1.1.1`.** El tag es anterior a quitar el
  bloque de dependencias cifrado para Google (`dependenciesInfo`), que F-Droid rechaza. La app es la
  misma: solo cambia la configuración del build.
- **Las versiones siguientes se publican solas.** Con `UpdateCheckMode: Tags` y
  `AutoUpdateMode: Version`, F-Droid detecta cada tag `vX.Y.Z` nuevo, lee `versionCode` y
  `versionName` de `app/build.gradle.kts` y añade el build. Basta con seguir etiquetando las releases.
- **El changelog de cada versión** va en `fastlane/metadata/android/<idioma>/changelogs/<versionCode>.txt`,
  con un máximo de 500 caracteres. Sirve el mismo texto que las notas de Play.
- **La ficha de fastlane es una copia de la de Play.** Si cambian los textos de
  `scripts/generar-textos-ficha.py`, o las capturas, hay que volver a copiarlos aquí.
- La receta instala JDK 21 porque `gradle/gradle-daemon-jvm.properties` lo pide. Si la CI de F-Droid
  no lo encuentra, ese es el primer sitio que mirar.
