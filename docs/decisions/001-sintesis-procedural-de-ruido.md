# ADR 001 — El ruido se sintetiza en tiempo real, no se reproduce desde ficheros

- **Fecha**: 2026-08-24
- **Estado**: aceptada
- **Contexto**: definición de la arquitectura de audio de la v1.

## Problema

La app necesita reproducir ruido blanco y ruido marrón de forma continua, durante horas, sin cortes
audibles. La solución obvia es empaquetar un fichero de audio por ruido y reproducirlo en bucle.

## Por qué no funciona el enfoque obvio

1. **Los codecs perceptuales destrozan el ruido blanco.** Opus y AAC parten de que hay estructura
   tonal que explotar; el ruido gaussiano no tiene ninguna. A 64–96 kbps aparece un artefacto metálico
   y burbujeante que es perfectamente audible con auriculares en una habitación a oscuras, que es
   exactamente la condición de uso de esta app.
2. **PCM sin comprimir no es viable.** Unos 5,5 MB por minuto de audio mono a 48 kHz. Un bucle
   suficientemente largo para no notarse pesaría más que toda la app.
3. **Cualquier bucle acaba siendo detectable.** El oído reconoce el patrón tras unas cuantas
   repeticiones. Con ruido marrón hay además que resolver la continuidad de DC en la costura, o se oye
   un golpe en cada vuelta.

## Decisión

Generar el ruido en tiempo real, en el dispositivo:

- PRNG rápido (xoshiro256++) alimentando Box-Muller polar para el ruido blanco gaussiano.
- Integrador con fuga para el marrón, con corrección de deriva de DC, normalización de sonoridad
  respecto al blanco y limitador suave.
- Dos generadores independientes, uno por canal, para un estéreo decorrelacionado.
- Conexión a ExoPlayer con un `DataSource` propio que sirve una cabecera WAV más PCM sintetizado, en un
  stream de longitud infinita, consumido por el `WavExtractor` de Media3.

La alternativa considerada y descartada fue `SimpleBasePlayer` escribiendo directamente en un
`AudioTrack`: da control total del pipeline, pero obliga a reimplementar a mano audio focus, wake lock
y el ciclo de vida del `AudioTrack`, que ExoPlayer ya resuelve. Se reconsideraría si algún día hiciera
falta mezclar varios ruidos simultáneos.

## Consecuencias

**A favor:**

- APK de unos 3 MB, sin un solo byte de audio empaquetado.
- Calidad exacta: gaussiano real, sin costuras y sin artefactos de compresión.
- Un ruido nuevo (rosa, gris) son unas pocas líneas de matemáticas.
- La síntesis es matemática pura sin dependencias de Android, así que se testea en JVM: RMS, offset de
  DC, ausencia de clipping y pendiente espectral con FFT.

**En contra / a vigilar:**

- Coste de CPU permanente durante la reproducción. Es despreciable en términos absolutos (48 000
  muestras/s de aritmética simple, menos del 1 % de CPU), pero en una sesión de ocho horas conviene
  usar buffers grandes en el `AudioTrack` para reducir los despertares de CPU por segundo.
- No hay offload de audio a hardware, que sí sería posible con un fichero comprimido.
- Los tests verifican las matemáticas, no que suene bien. Hace falta escucha real antes de publicar.

## Referencias

- `docs/analisis-tecnico.md` §3
