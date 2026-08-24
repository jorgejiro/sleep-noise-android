# ADR 006 — Nivel de salida y ruidos pensados para enmascarar

- **Fecha**: 2026-08-24
- **Estado**: aceptada
- **Contexto**: la app se usa también para tapar ruido ambiente, no solo para dormir.

## Problema

Uno de los usos reales de la app es ponerse tapones y encima unos auriculares de diadema en un sitio
ruidoso, y que el ruido generado tape lo que hay alrededor. Medido contra ese uso, la app tenía dos
defectos, y el segundo es el grave.

### 1. Regalaba 6 dB

Los generadores salían a **-18 dBFS RMS**, con los picos medidos 4 dB por debajo de fondo de escala.
Eso es margen sin usar: a volumen máximo la app entregaba una fracción de lo que el hardware puede
dar.

### 2. El sonido por defecto era el que peor enmascara

La voz humana vive entre 250 Hz y 4 kHz, y lo que la hace *inteligible* —las consonantes, lo que hace
que entiendas lo que dicen en la mesa de al lado— está entre 1 y 4 kHz. Midiendo dónde pone su energía
cada ruido, a igual nivel total:

| Ruido | Banda de la voz (250 Hz – 4 kHz) | Inteligibilidad (1–4 kHz) |
|---|---|---|
| Marrón | 8,6 % | 1,4 % |
| Blanco | 23,5 % | 18,8 % |
| Rosa | 41,3 % | 20,5 % |
| Enmascarador | **71,0 %** | **31,8 %** |

El **ruido marrón**, que era el sonido por defecto, tiene el 91 % de su energía por debajo de 250 Hz.
Para tapar una conversación es casi inútil: estaba gastando el volumen donde el problema no está.

## Decisión

**Tres cambios, y los tres se suman.**

1. **El nivel sube de -18 a -12 dBFS RMS.** El límite lo pone la distorsión, no el gusto: una señal
   gaussiana no tiene tope, así que la pregunta real es cuántas muestras chocan con el techo. A -14
   dBFS es una por segundo, a -12 unas treinta, y a -10 son varios cientos y empieza a oírse como un
   crujido. Treinta muestras limitadas por segundo, y con un limitador suave, no se oyen: medido,
   entre 13 y 35 según el sonido. El limitador pasa a aplicarse a **todos** los generadores, no solo
   al marrón.
2. **Dos sonidos nuevos**: **ruido rosa**, el todoterreno clásico y el que suena «plano» al oído
   humano, y un **ruido enmascarador**, que no lleva nombre de color porque no es uno: es una forma
   elegida para un trabajo. Plano hasta 800 Hz y cayendo unos 6 dB por octava a partir de ahí, que es
   aproximadamente la curva que emiten los sistemas de enmascaramiento de las oficinas abiertas.
3. **El sonido por defecto pasa a ser el enmascarador.** Quien instala la app para tapar una oficina
   la habría juzgado por un sonido que no puede hacerlo; quien la quiere para dormir encuentra el
   marrón a un toque.

Sumando forma y nivel, son **unos 16 dB** de enmascaramiento efectivo frente a lo que había: más del
doble de sonoridad percibida, y puesta donde sirve.

### Por qué 800 Hz y no los 500 del estándar

La curva de las oficinas es plana hasta 500 Hz y cae 5 dB por octava. Un filtro de un polo —que es lo
que se puede implementar en tres líneas— cae 6, no 5. Subir la esquina a 800 Hz compensa esa diferencia
devolviendo energía a la banda de 1–4 kHz, que es la que decide si entiendes las palabras: con la
esquina a 500 Hz el resultado medía 22,5 % ahí, y con ella a 800 mide 31,8 %.

## Consecuencias

- **Cambia el alcance de la 1.0**, que decía «solo dos sonidos, a propósito». Ahora son cuatro, y la
  ficha de Play, el changelog y las capturas se reescriben en consecuencia. La justificación sigue
  siendo la misma en espíritu: no hay catálogo, hay **cuatro sonidos que hacen cosas distintas**.
- **La fila de botones pasa a rejilla 2×2.** A 390 dp, un cuarto botón en una fila deja sitio para
  cinco caracteres.
- **El volumen 50 suena 6 dB más fuerte que antes.** Como la 1.0 no está publicada, nadie está
  acostumbrado al nivel anterior; quien use la app para dormir bajará el mando.
- **Sobre el oído.** La app ahora puede sonar más fuerte, y se usa durante horas. Ocho horas de
  exposición a más de 80 dB SPL tienen riesgo acumulativo, y con tapones puestos es fácil subir más de
  lo que uno cree. El control lo tiene el usuario, y el volumen del sistema sigue mandando por encima
  del de la app.
- El generador rosa necesitó **su propio bloqueo de continua**: el filtro de Kellett tiene ganancia en
  continua, y sin él medía 2,9·10⁻³ de desviación, tres veces el límite. Lo pilló un test.
- La calibración de ganancia y el precalentamiento, que estaban dentro del generador marrón, pasan a
  una clase base común: los tres sonidos filtrados los necesitan por el mismo motivo.

## Referencias

- Medidas: `NoiseSpectrumTest`, que imprime la energía en la banda de voz de cada sonido en cada
  `./gradlew test`
- Espectro completo: `build/reports/noise-spectrum.csv`
- Nivel y limitador: `NoiseLevelTest`, que mide cuántas muestras se limitan por segundo
