# Sleep Noise (Android)

App Android que genera ruido de fondo continuo con **dos propósitos que pesan igual**: dormirse, y
tapar el ruido de alrededor cuando no puedes irte de donde estás —con auriculares, y a veces con
tapones debajo—. Se abre, suena, y no pide nada más.

- **Sonidos**: cuatro, generados en tiempo real en el dispositivo. Blanco, rosa, marrón y un
  **enmascarador** con la forma espectral pensada para tapar conversaciones: pone el 71 % de su
  energía en la banda donde vive la voz, frente al 8,6 % del marrón.
- **Arranque**: al abrir la app suena el último sonido escuchado; tras instalar, el enmascarador.
- **Control**: notificación de medios para pausar y parar, con la app cerrada y la pantalla apagada.
- **Temporizador**: apagado a los X minutos, con fade out para no despertar a nadie.
- **Idiomas**: inglés y español, siguiendo el idioma del sistema con fallback a inglés, y cambiable
  desde los ajustes de la app.

Kotlin · Jetpack Compose · Material 3 Expressive · Media3 1.11

## Estado

Fase 0: análisis, dirección visual y especificación de la 1.0 cerrados. Sin código todavía.

## Documentación

- [Especificación y plan de la release 1.0](docs/especificacion-release-1.0.md)
- [Análisis técnico y plan de implementación](docs/analisis-tecnico.md)
- [Guía de trabajo del repositorio](CLAUDE.md)
- [Decisiones de arquitectura (ADR)](docs/decisions/)
- [Icono de la app](docs/design/icono/)
- [Textos de la ficha de Google Play](docs/play-store-publication-texts.md)
- [Notas de la versión para Play](docs/play-release-notes.md)
- [Política de privacidad](docs/privacy-policy/sleep-noise.html)
- [Generación de las capturas de la ficha](docs/store-assets/generar-capturas/README.md)
- [Changelog](CHANGELOG.md)
