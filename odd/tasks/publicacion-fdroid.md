# Publicación en F-Droid

## Objetivo

Dejar Sleep Noise lista para entrar en el catálogo principal de F-Droid: el repositorio cumple sus
requisitos y la receta para `fdroiddata` está escrita y validada.

## Por qué

La app es MIT, no usa red ni servicios de Google, y el público de F-Droid es justo el que valora eso.

## Alcance y restricciones

- F-Droid compila desde el código y **firma con su clave**. Una instalación de Play y una de F-Droid
  no pueden actualizarse entre sí. Las builds reproducibles con la firma del desarrollador quedan
  fuera de esta primera entrada.
- El merge request a `gitlab.com/fdroid/fdroiddata` lo abre Jorge desde su cuenta de GitLab: no hay
  cuenta ni autorización para operar en GitLab desde aquí.
- TDD: no aplica (cambios de build y metadatos). Checks: `./gradlew lint test`, `assembleRelease`
  sin keystore y `fdroid lint` sobre la receta.

## Tareas

- [x] T1 · Quitar el bloque de dependencias cifrado de Google del APK/AAB (`dependenciesInfo`) y
  comprobar que `assembleRelease` compila sin `keystore.properties`. Ruta: inline (1 fichero).
- [x] T2 · Metadatos fastlane en `fastlane/metadata/android/{en-US,es-ES}`: título, descripciones,
  changelog del versionCode 4, icono, gráfico y capturas. Ruta: inline (copias mecánicas desde la
  ficha de Play).
- [x] T3 · Receta `docs/fdroid/com.jjrapps.sleepnoise.yml`, validada con `fdroid lint`, y guía del
  merge request. Ruta: inline.

## Progreso

- T1 · `c187a61`. `assembleRelease` sin `keystore.properties` genera `app-release-unsigned.apk`;
  `./gradlew lint test` en verde.
- T2 · `753115a`. Textos dentro de los límites (corta 72/69, larga 3292/3228, changelog 472/482).
- T3 · `fdroid lint` (fdroidserver 2.4.5, con `config/categories.yml` de fdroiddata) sin avisos;
  `fdroid rewritemeta` no cambia nada.
- Sin verificar: el build real en la CI de F-Droid (JDK 21 y `compileSdk` 37 en su servidor).

## Siguiente paso

Merge request abierto el 2026-09-28: https://gitlab.com/fdroid/fdroiddata/-/merge_requests/50449
(rama `com.jjrapps.sleepnoise` del fork `jorgejiro/fdroiddata`, commit `807b2c7`). Firma de F-Droid
elegida por Jorge: sin builds reproducibles, y ya no se puede cambiar. Falta que pase la CI y la
revisión.
