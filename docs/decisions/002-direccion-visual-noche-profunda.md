# ADR 002 — Dirección visual: «Noche profunda»

- **Fecha**: 2026-08-24
- **Estado**: aceptada
- **Contexto**: elección de dirección visual para la v1, entre cuatro propuestas.

## Decisión

Se adopta la dirección **A, «Noche profunda»**, con una corrección respecto al mockup original.

Rasgos vinculantes:

- **Tema oscuro único**, de base cálida. No hay modo claro en la 1.0.
- **Un solo elemento dominante**: un aro de 264 px que representa el volumen, con el botón de
  play/pausa en su centro. El resto de la pantalla es subordinado.
- **Tipografía de trazo fino** (Sora 200/300/400) a gran tamaño para el nombre del sonido.
- **Un halo que respira** con ciclo de 9 s detrás del aro, desactivado cuando el sistema pide
  animaciones reducidas.
- **Acento cálido ámbar** como único color saturado de la interfaz.

**Corrección acordada**: el aro se mantiene como control de volumen, pero se añade **un slider
convencional debajo**. El gesto sobre el aro pasa a ser un atajo, no el único camino. Un control que
hay que descubrir no puede ser la única forma de hacer algo.

## Alternativas descartadas

Las tres se descartan **de forma definitiva**; no se retoman en revisiones posteriores.

- **B · Editorial de papel** — serif de display sobre fondo claro con textura de grano. Era la más
  atractiva en una captura de pantalla y la mejor para la ficha de Play, pero un fondo claro a las
  tres de la mañana deslumbra: obligaba a diseñar un modo oscuro aparte, es decir, dos diseños.
- **C · Expressive de libro** — Material 3 Expressive sin retocar. Era la más barata de construir y
  la que mejor encajaba con dynamic color, pero indistinguible de cualquier otra app Material bien
  hecha, y con dynamic color el color de la app lo decide el fondo de pantalla del usuario.
- **D · Espectro** — el ruido como densidad espectral. Explicaba el producto sin palabras, pero es
  demasiada información para alguien que solo quiere dormirse, y una cuenta atrás con segundos invita
  a mirar el reloj, que es justo lo contrario de lo que busca la app.

## Nota posterior (2026-08-24, tras el ADR 006)

Esta decisión se tomó cuando la app tenía un solo propósito: dormir. El argumento para renunciar al
modo claro era que «el contexto de uso de esta app es la oscuridad», y con el segundo propósito —tapar
el ruido de alrededor con auriculares, muchas veces de día y en un sitio iluminado— **ese argumento ya
no es completo**.

La decisión se mantiene, por dos razones que sí siguen en pie: una interfaz oscura no molesta de día,
mientras que una clara sí deslumbra de noche; y un solo tema es la mitad de superficie de diseño y de
pruebas. Pero conviene tenerlo escrito: si algún día llegan quejas por usar la app con luz, **la
respuesta correcta es reconsiderar el modo claro**, no defender un argumento que ya solo vale para la
mitad de los usos.

## Consecuencias

**A favor:**

- Es la única dirección diseñada para el momento real de uso: la cama, a oscuras, con los ojos medio
  cerrados. Nada compite por la atención y el objetivo táctil central se acierta sin mirar.
- Un tema único significa la mitad de superficie de diseño y de pruebas visuales.
- El acento ámbar es reconocible en una captura: la app se identifica sin leer el nombre.

**En contra / a vigilar:**

- **Se renuncia al modo claro y a dynamic color**, apartándose de una recomendación explícita de
  Google. Es una decisión deliberada: el contexto de uso de esta app es la oscuridad. Hay que
  documentarlo en la ficha de Play para que nadie lo lea como un olvido.
- El aro-volumen necesita apoyo: el valor numérico visible junto a él no es decoración, es la
  instrucción de uso, y el slider de abajo es la salida para quien no descubra el gesto.
- El halo animado consume GPU de forma continua mientras la pantalla está encendida. Debe pararse
  cuando la app pasa a segundo plano y respetar la escala de animaciones del sistema.

## Referencias

- Propuestas completas: `docs/design/` y https://claude.ai/code/artifact/5a8146ce-34d6-4516-9860-d83f60357280
- Especificación derivada: `docs/especificacion-release-1.0.md`
