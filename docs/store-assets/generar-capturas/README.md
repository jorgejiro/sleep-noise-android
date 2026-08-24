# Generar las capturas de la ficha de Play

**Estado: diseñado, sin implementar.** Los scripts se escriben en el hito H9, cuando exista una app
que navegar. Este documento es el diseño del pipeline y, sobre todo, el traslado de las lecciones que
costaron una iteración cada una en «¡Bebe agua!» y «¡Aquí hay tomate!». Leerlo antes de escribir la
primera línea ahorra ese día.

El juego completo es **7 escenas × 2 idiomas × 3 formatos = 42 imágenes**, en
`docs/store-assets/capturas/<formato>/<idioma>/`, versionadas en el repositorio.

Va automatizado porque hay que rehacerlas cada vez que cambie una pantalla, y hechas a mano son 42
secuencias de navegación con dos fallos que no avisan: **una captura mala no da error** (sale negra o
a medio pintar y se sube), y **un idioma se cuela** — que es exactamente lo que pasó con el primer
juego de «¡Aquí hay tomate!».

## Piezas previstas

| Fichero | Responsabilidad |
|---|---|
| `ui.py` | Localizar elementos por texto con `uiautomator dump` y tocar su centro. **Nunca por coordenadas**: falla la mitad de las veces y no sobrevive al cambio de resolución |
| `tanda.py` | Las siete escenas en **un** idioma |
| `capturar.py` | Orquesta las dos tandas y prepara el dispositivo (tamaño, densidad, tema oscuro, animaciones) |
| `revisar.py` | Control de las 36 antes de subirlas. Código de salida 1 si algo falla |
| `todos.sh` | Los tres formatos de un tirón, arrancando y apagando cada emulador |

## Las siete escenas

La ficha tiene que enseñar los **dos** propósitos, no solo el de dormir: quien busca algo para
concentrarse en una oficina ruidosa tiene que reconocer su caso en las capturas.

| Fichero | Qué enseña |
|---|---|
| `01-suena-enmascarador` | La pantalla principal reproduciendo el enmascarador, que es el sonido que viene puesto: el aro, el nombre y la rejilla de cuatro |
| `02-elegir-temporizador` | La hoja del temporizador abierta, con los presets |
| `03-temporizador-activo` | La principal con la cuenta atrás corriendo |
| `04-control-en-la-notificacion` | La sombra con el control de reproducción y sus acciones |
| `05-los-cuatro-sonidos` | La rejilla con los cuatro, para que se vea que hay uno pensado para tapar voces |
| `06-ajustes` | Ajustes completos: reproducir al abrir, idioma, versión, novedades, comentarios |
| `07-novedades` | El changelog de la versión |

Orden de subida a Play y motivo de cada posición: `docs/play-store-publication-texts.md`, sección
«Capturas — Orden recomendado».

## Los tres formatos

Play pide **9:16 exacto** en las capturas de tablet, con lados de 320–3840 px en la de 7" y de
1080–7680 px en la de 10". Las medidas **las fija el script** con `wm size` y `wm density` en cada
pase, no el AVD: los `config.ini` dicen otra cosa y no hay que depender de lo que quedara guardado.

| AVD | Resolución | Densidad | En dp | Aspecto |
|---|---|---|---|---|
| `Medium_Phone` | 1080 × 2400 | 420 | 411 × 914 | 9:20 |
| `Tablet7` | 1080 × 1920 | 288 | 600 × 1067 | 9:16 |
| `Tablet10` | 1440 × 2560 | 288 | 800 × 1422 | 9:16 |

Las densidades de 288 dpi no son casualidad: dejan la tablet pequeña en 600 dp de ancho y la grande
en 800 dp, los dos umbrales con los que Android decide que algo es una tablet.

## Lo que hereda de Bebe Agua (y sigue aplicando igual)

- **Esperar por contenido, nunca por tiempo.** Con un `sleep` fijo las capturas salen a medio pintar,
  y una tablet no tarda lo mismo que un teléfono en componer.
- **Y esperar por una subcadena no es esperar.** Aquí la trampa es literal: **«Blanco» está dentro de
  «Ruido blanco»** y «Marrón» dentro de «Ruido marrón», así que buscar la píldora sin coincidencia
  exacta toca el nombre del sonido. Las píldoras se tocan siempre con `exacto=True`.
- **El idioma se pone desde la pantalla de Ajustes de la app**, no con `cmd locale set-app-locales`:
  la app aplica su propio ajuste al arrancar y sobreescribiría el del sistema.
- **La sombra se vacía antes de capturar la notificación.** El emulador trae de fábrica el aviso «Set
  a screen lock», que en la captura sale pegado al nuestro como si fuera parte de la app.
- **Una captura mala no da error.** De ahí `revisar.py`, y de ahí que sea obligatorio pasarlo.
- **El formato de 24 horas se fuerza**, que en la ficha española sí canta.

## Lo que cambia respecto a Bebe Agua

- **No hay siembra de datos.** Sleep Noise no tiene base de datos: el estado de las capturas
  (sonido, volumen, temporizador) se consigue navegando por la interfaz. Eso elimina de golpe el
  fallo más grave del pipeline anterior — la siembra con `run-as`, que obligaba a capturar con el APK
  debug y que con el de release completaba el pase entero sin un solo error y con todas las capturas
  vacías. Aquí **da igual qué APK se use**, y conviene usar el de release, que es el que se publica.
- **La notificación no hay que provocarla.** En Bebe Agua había que mover el reloj hasta la alarma
  pendiente porque el receptor no estaba exportado. Aquí la notificación existe mientras suena:
  se abre la sombra y se captura. Una fuente de fragilidad menos.
- **El sistema se pone en tema oscuro** (`cmd uimode night yes`) antes del pase. La app es oscura
  siempre, pero la sombra de notificaciones sigue el tema del sistema: sin esto, la escena 04 sale
  con un panel blanco alrededor de una app negra.
- **Las animaciones del sistema se desactivan durante el pase**
  (`settings put global animator_duration_scale 0`). Dos motivos, y el segundo es el importante:
  1. El halo de la pantalla principal respira con un ciclo de 9 s, así que sin esto cada captura lo
     coge en un punto distinto del ciclo y no son reproducibles.
  2. **Rompería la comprobación de idioma de `revisar.py`.** Ese control funciona verificando que la
     captura española y la inglesa de la misma escena **no sean idénticas**. Con el halo animado
     nunca lo son, así que el test pasaría siempre y dejaría de detectar el idioma colado, que es
     justo el fallo que motivó el pipeline. La app respeta `animator_duration_scale` a 0 y para el
     halo (es un requisito de accesibilidad, RNF-08), así que el propio diseño de la app hace esto
     posible.
- **La limitación del idioma del sistema casi desaparece.** En Bebe Agua, el nombre de la app dentro
  de la notificación salía en inglés en la ficha española. Aquí el nombre en el lanzador es «Sleep
  Noise» en los dos idiomas, así que solo quedan en inglés los textos propios del sistema en la
  sombra y la barra de estado. Si algún día molesta, la vía es arrancar el emulador con
  `-prop persist.sys.locale=es-ES` y hacer un pase por idioma, con el coste de arrancar seis veces.

## Estado del temporizador en las capturas

El temporizador se pone a **90 minutos** y se captura de inmediato, de modo que las escenas 03 y 04
muestran siempre **«1 h 30 min»**. Es lo único que hace la cuenta atrás reproducible: cualquier otro
valor depende de cuánto tarde el pase, y dos capturas de la misma escena con tiempos distintos se
notan al ponerlas una al lado de la otra en la ficha.

## Qué comprueba `revisar.py` antes de subir

1. Que estén las **42**, con los nombres esperados.
2. Las **dimensiones exactas** de cada formato, y el aspecto 9:16 en las dos de tablet.
3. Que la captura **`es` y la `en` de la misma escena no sean idénticas** — un idioma colado.
4. Que ninguna esté **negra o a medio pintar** (varianza de píxeles por debajo de un umbral).
5. Que ninguna contenga el aviso del sistema en la sombra en la escena 04.

Devuelve 1 si algo falla, así que sirve tal cual dentro de `todos.sh`.
