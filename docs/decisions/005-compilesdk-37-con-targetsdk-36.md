# ADR 005 — `compileSdk` 37 con `targetSdk` 36

- **Fecha**: 2026-08-24
- **Estado**: aceptada
- **Contexto**: primer build del proyecto (H0).

## Problema

La especificación fijaba `compileSdk` 36 y `targetSdk` 36. El primer `assembleDebug` falló: la
Compose BOM 2026.08.00 (Compose 1.12.0) y `material3` 1.5.0-alpha26 **exigen compilar contra la API
37 o posterior**. Trece dependencias dieron el mismo error, entre ellas `compose-ui`, `ui-tooling` y
el propio `material3` del que depende toda la dirección visual.

Las dos salidas eran bajar de versión —volver a la BOM de abril y renunciar a Material 3
Expressive, que es una decisión de producto ya tomada— o subir `compileSdk`.

## Decisión

`compileSdk` **37**, `targetSdk` **36**.

No es una inconsistencia: son dos cosas distintas y el propio mensaje de error de AGP lo dice.
`compileSdk` determina contra qué APIs se compila; `targetSdk` determina a qué comportamientos de
runtime se acoge la app. Compilar contra la 37 no activa ningún cambio de comportamiento de la 37, y
es además lo que Google recomienda: compilar siempre contra la última API disponible.

`targetSdk` sigue en 36 porque subirlo sí cambia comportamiento en tiempo de ejecución, y eso se
hace con pruebas por delante, no como efecto colateral de arreglar un build.

## Consecuencias

- Hizo falta la plataforma `android-37`, que AGP descargó sola. Quien clone el repositorio la
  necesita: si no está, el primer build la pide.
- `lint` avisa con `OldTargetApi` de que `targetSdk` no es el último. Es correcto y esperado: se deja
  el aviso a la vista en lugar de suprimirlo, para que la próxima subida de `targetSdk` sea una
  decisión y no un olvido.
- La documentación queda actualizada: `CLAUDE.md` §3 y la especificación §5.
