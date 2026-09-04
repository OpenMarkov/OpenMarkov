# Redes para comprobar a mano dos arreglos

Dos arreglos del lote de la evidencia imposible no tienen prueba automática, porque las clases que
tocan sólo se construyen con la ventana entera. Estas dos redes existen para comprobarlos en la
aplicación. Cada una lleva la secuencia exacta y qué se ve antes y después del arreglo.

Los números están elegidos para que el defecto salte; no pretenden modelar nada real.

---

## `propagar-al-editar.pgmx` — editar un nodo ya no propaga en modo edición

Red bayesiana de dos nodos, `Cause` → `Effect`:

- P(Cause) = (no 0.5, yes 0.5)
- P(Effect | Cause=no) = (no 1, yes 0) · P(Effect | Cause=yes) = (no 0.5, yes 0.5)

Con esos números, `Effect=yes` sólo es posible si `Cause=yes`.

**Pasos:**

1. Abrir la red y entrar en **modo inferencia**.
2. Poner evidencia doble-pulsando `yes` en `Cause` y `yes` en `Effect`. Es evidencia posible
   (probabilidad 0,25), así que la propagación va bien.
3. Volver a **modo edición**.
4. Abrir la tabla de probabilidad de `Cause` y ponerla en (no **1**, yes **0**). Aceptar.

En ese momento la aplicación borra la evidencia de `Cause` y queda sólo `Effect=yes`, que con la
tabla nueva es imposible.

**Antes del arreglo:** se propagaba de todas formas, así que aparecía un diálogo de error que no se
puede relacionar con lo que se acaba de hacer —cambiar una probabilidad— y, de paso, la propagación
automática quedaba desactivada en silencio.

**Después:** no se propaga nada, porque no se está en modo inferencia. La evidencia se limpia y no
aparece ningún diálogo.

---

## `escenario-y-evidencia.pgmx` — el escenario del análisis de sensibilidad se relee

Diagrama de influencia: `Disease` → `Test result` → `Therapy` → `U`, con `Disease` también hacia `U`.
`Test result` es el predecesor informativo de la decisión, que es el que aparece como desplegable en
el escenario, y la prevalencia de `Disease` lleva incertidumbre (distribución Beta) para que el
análisis tenga algún parámetro que variar.

**Pasos:**

1. Abrir la red y lanzar el análisis de sensibilidad.
2. Elegir el ámbito **decisión**, con la decisión `Therapy`. Aparece un desplegable para
   `Test result`, habilitado, porque esa variable no tiene evidencia.
3. Pulsar **Aceptar**: el análisis se ejecuta y el diálogo **se queda abierto** (no es modal).
4. Sin cerrarlo, poner evidencia en `Test result` en la red.
5. Volver a pulsar **Aceptar**.

**Antes del arreglo:** en ese segundo Aceptar se mezclaban la evidencia nueva y el escenario viejo,
que se contradicen en `Test result`, y salía un error. Y volvía a salir con cada Aceptar: desde el
diálogo no había manera de arreglarlo salvo cerrarlo.

**Después:** antes de mezclar nada se relee la evidencia y se reconstruye el escenario sobre ella,
así que `Test result` aparece con el estado observado y su desplegable deshabilitado, igual que si
el diálogo se acabara de abrir. El análisis se ejecuta con normalidad.
