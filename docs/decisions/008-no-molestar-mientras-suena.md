# ADR 008 — No molestar mientras suena, y dónde se pide su permiso

- **Fecha**: 2026-09-08
- **Estado**: aceptada.
- **Contexto**: evolutivo posterior a la 1.0.1. Dos peticiones, y una de ellas choca con la
  plataforma y con el principio de cero fricción.

## Problema

Una app cuyo trabajo es tapar los ruidos que despiertan deja pasar el que más despierta: **el propio
teléfono**. Un mensaje a las tres de la madrugada suena por encima del ruido blanco, y encima llega
por el mismo altavoz. Lo lógico es que la app active No molestar mientras suena y lo desactive al
parar.

Y ahí aparece la plataforma, en dos capas:

1. **`ACCESS_NOTIFICATION_POLICY` es un acceso especial, no un permiso normal.** No hay diálogo que
   pedir: declararlo en el manifest solo hace que la app aparezca en una lista de Ajustes del
   sistema, y quien lo concede es el usuario a mano. `ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS`
   abre esa lista, y **no la fila de esta app**: el intent que va directo (
   `ACTION_NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS`) está marcado `@hide` y `@SystemApi` en las
   fuentes del SDK —verificado en `android-31/android/provider/Settings.java:1640`—, o sea reservado
   a aplicaciones del sistema. Por eso el texto que lo pide nombra la app: quien llegue allí tiene
   que encontrarse a sí mismo en una lista.
2. **Desde Android 15 la llamada ya no cambia el estado global** ([behavior changes 15][bc15]). Con
   `targetSdk` 36, `setInterruptionFilter` se traduce en una **regla zen automática implícita** de
   esta app, que el sistema activa y desactiva con nuestras propias llamadas y combina con las de
   los demás bajo «gana la más restrictiva».

Esa segunda capa, que suena a limitación, es justo lo que hacía falta: pedir silencio ya no puede
pisar el No molestar que el usuario puso él, y devolverlo ya no puede apagar el de otro.

Queda el conflicto de producto: la funcionalidad se pide «activada por defecto», pero **no puede
funcionar la primera noche** sin que alguien conceda ese acceso. Y explicarlo en una pantalla de
bienvenida está descartado por el principio uno —«abrir la app ya es escuchar. Sin bienvenida»— y por
la especificación §2, que deja el onboarding fuera de alcance.

## Decisión

**Tres piezas.**

1. **El filtro es `INTERRUPTION_FILTER_PRIORITY`, no `NONE`.** Prioridad es lo que hace el
   interruptor de los ajustes rápidos del sistema: silencia con las reglas que el usuario ya eligió,
   y esas dejan pasar las alarmas. El silencio total se tragaría el despertador, y una app para
   dormir que hace que la gente no se levante no tiene defensa posible.
2. **La explicación va en una hoja modal sobre el reproductor, la primera vez que suena.** No es un
   onboarding: es el mismo sitio y el mismo razonamiento que ya estaba escrito para el permiso de
   notificaciones —se pide donde significa algo, cuando ya hay sonido—. Se muestra **una sola vez**, y
   descartarla cuenta como respuesta: una app que vuelve a pedir lo que ya le negaron es una app que
   da la lata. Quien cambie de opinión tiene el interruptor en Ajustes, con su fila de «conceder
   acceso» al lado mientras falte.
3. **La app solo devuelve lo que ella cogió.** `DoNotDisturbController.isHeld` es toda la clase: sin
   esa bandera, pausar el ruido apagaría el No molestar de quien lo había activado él antes de abrir
   la app.

La excepción a esa última regla está escrita a propósito: si el proceso muere a mitad de la noche
—una muerte por memoria, un «forzar detención»— nadie ejecuta la devolución y el teléfono se queda
mudo. Por eso la sesión deja una miga de pan en DataStore (`dnd_held`) y el siguiente arranque la
devuelve aunque no pueda demostrar que fue suya. **Un teléfono silencioso durante días es un fallo
peor que un No molestar apagado una vez**, y esa comparación es la decisión.

## Lo que salió al medirlo

Dos cosas que no se ven leyendo la documentación, ambas comprobadas en un emulador API 37 con
`dumpsys notification` y `settings get global zen_mode`.

**Devolver el silencio no es «poner el filtro que había».** Ese es el idioma de antes de Android 15, y
allí es correcto porque el filtro *es* el estado del teléfono. Desde 15 no lo es: volver a poner el
`PRIORITY` recordado no restaura el No molestar de nadie, **vuelve a encender la regla de esta app** —
y entonces la app cree que no tiene nada cogido mientras su regla sigue activa. Medido: con el No
molestar del usuario puesto, al pausar quedaba `implicit_com.jjrapps.sleepnoise` en `state=STATE_TRUE`.
Desde 15 la devolución es siempre `INTERRUPTION_FILTER_ALL`, que apaga la regla propia y, por ese mismo
cambio de comportamiento, no puede tocar la de nadie más. Por debajo de 15 se sigue recordando el
filtro anterior, que allí sí es lo correcto. Las dos ramas están comprobadas: API 37 y API 31.

**Y hay un callejón sin salida que es de la plataforma, no nuestro.** Si el usuario revoca el acceso
*mientras* la app tiene cogido el silencio, la regla se queda activa y el sistema no la apaga al
revocar —medido: `zen_mode` seguía en 1—. La app ya no tiene ninguna API para apagarla: lo intenta
igual, porque la llamada va envuelta y probar no cuesta nada, pero fallará. La salida es del usuario,
desde los ajustes rápidos del sistema, y no hay nada que esta app pueda hacer al respecto. Se deja
escrito para que nadie lo persiga como si fuera un bug propio.

## Consecuencias

- Un permiso más en el manifest, y el primero que la app no puede conseguir por sí misma. Si no se
  concede, **la app suena exactamente igual**: `acquire()` devuelve `false` y ahí acaba.
- El acceso es revocable en cualquier momento desde una pantalla que la app no controla. Todas las
  llamadas al sistema van envueltas, y perderlo a mitad de sesión no le cuesta nada al audio —pero sí
  deja el silencio colgado, ver arriba.
- Cada pausa devuelve el teléfono, incluidas las del sistema. Una llamada entrante que el usuario no
  puede oír es peor que una noche de ruido interrumpida; al reanudar se vuelve a pedir el silencio.
- La lógica de quién tiene el silencio se testea en JVM contra un `ZenGateway` falso
  (`DoNotDisturbControllerTest`), igual que el temporizador contra un reloj falso. Lo que **no** se
  puede testear así es el efecto real en el teléfono: eso va en §7 de la especificación, con
  hardware.

## Alternativas descartadas

- **Una `AutomaticZenRule` explícita**, con nombre e icono visibles en los ajustes del sistema. Es lo
  que Android 15 pide «de verdad», pero su `Builder` es API 35 y el `minSdk` es 31: harían falta dos
  caminos para el mismo efecto, cuando el camino viejo ya produce la regla implícita en 35+.
- **Pedir el acceso al abrir la app**, antes de que suene nada. Es exactamente la bienvenida que el
  producto no tiene.
- **Nacer apagada**. El interruptor nunca mentiría, pero deja la función escondida en Ajustes para
  algo que casi todo el mundo quiere en una app que se llama Sleep Noise.

[bc15]: https://developer.android.com/about/versions/15/behavior-changes-15
