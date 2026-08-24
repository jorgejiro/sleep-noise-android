# ADR 004 — El icono de la app: «Luna de grano»

- **Fecha**: 2026-08-24
- **Estado**: aceptada
- **Contexto**: elección del icono de la app entre cuatro propuestas.

## Decisión

Se adopta **«Luna de grano»**: un creciente compuesto de 230 puntos de grano ámbar sobre el fondo
oscuro cálido de la dirección «Noche profunda». La luna sitúa la app en su categoría de un vistazo; el
grano es literalmente lo que la app genera.

Alternativas descartadas, de forma definitiva:

- **El aro** — el aro de la pantalla principal recortado a icono. Daba continuidad total entre icono y
  producto, pero un arco incompleto es el dibujo universal de «cargando».
- **La onda que se apaga** — una onda cuya amplitud decae. Era la más explicativa y la menos propia:
  onda sobre fondo oscuro es el territorio de las apps de música y las grabadoras de voz.
- **El campo de ruido** — grano sin metáfora. La más memorable y la más frágil: a 48 dp es una mancha.
  Se reserva la idea para el gráfico de cabecera de la ficha de Play, donde hay 1024 × 500 y la
  textura sí se ve.

## El problema que traía la propuesta, y cómo se resuelve

Esta era la propuesta que peor sobrevivía al monocromo. Android 13+ pide una capa en una sola tinta
para los iconos temáticos, y con puntos de 0,5 a 1,65 dp esa capa quedaba en nada a tamaño pequeño.

La salida no es una luna lisa, que sería justo el icono genérico que se quería evitar. El creciente
pasa a ser **sólido** —lo que garantiza que se lea a cualquier tamaño— y la textura se traslada a
**74 puntos gruesos, de 1,2 a 2,2 dp, pegados al borde exterior**. En grande se ve el grano; en
pequeño se compacta en un contorno ligeramente irregular. Es la misma marca con otra técnica.

## Consecuencias

- **Hacen falta cuatro piezas, no una**: fondo, primer plano, capa monocroma y el icono de
  notificación de 24 dp, que es un dibujo aparte —a 24 dp el grano no existe— y el que más se verá:
  estará en la barra de estado toda la noche mientras suena el ruido.
- **El grano va como puntos vectoriales, no como un filtro.** Un `VectorDrawable` de Android no
  soporta filtros SVG, así que `feTurbulence` no era una opción. Los 230 puntos van como subpaths
  repartidos en **seis** `path`: en uno solo daban un `pathData` de casi 13.000 caracteres y lint lo
  marcaba con razón —un path muy largo es caro de parsear—, y un `path` por círculo serían 230
  elementos. Seis es el punto intermedio en el que ninguno pasa el umbral.
- **Los assets se generan, no se editan.** `scripts/generar-iconos.py` calcula el creciente por
  intersección de circunferencias y siembra el grano con un PRNG determinista, para que el mismo grano
  salga en cada regeneración: con un generador aleatorio, el icono cambiaría solo entre commits.
  Retocar un XML a mano se perdería en la siguiente pasada.
- **El creciente se deriva, no se dibuja.** Escrito a mano, el segundo arco tenía un radio menor que
  la mitad de la cuerda y SVG lo reescalaba hasta convertir la luna en una lente. Es un fallo
  silencioso: no da error, solo sale mal.
- Queda por hacer, en H8: rasterizar el PNG de 512 × 512 para Play, verlo en un dispositivo real
  —los lanzadores de Samsung y Xiaomi aplican máscaras propias y escalan distinto del emulador— y
  comprobar el icono de notificación en la barra de estado a un brazo de distancia.

## Referencias

- Propuestas y familia completa: `docs/design/icono/` y
  https://claude.ai/code/artifact/8efa24c0-9ab9-49e3-a95b-a1e4348ef4e1
- Generador: `scripts/generar-iconos.py`
- Dirección visual de la que hereda la paleta: ADR 002
