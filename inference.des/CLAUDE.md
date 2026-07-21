# inference.DES

Inferencia por **simulación de eventos discretos** (Discrete-Event Simulation) sobre redes DES de OpenMarkov. En lugar de propagación exacta, ejecuta una simulación Monte Carlo guiada por una cola de eventos: cada individuo recorre el tiempo saltando de evento a evento hasta el horizonte temporal, acumulando utilidades por criterio. Pensado para análisis coste-efectividad con tiempo continuo (modelos de eventos sanitarios y similares).

`artifactId` Maven: `inference.DES`. Nombre de módulo JPMS: `inference.DES`. Paquete: `org.openmarkov.inference.DES`.

## Dependencias

| Artefacto | Para qué |
|---|---|
| `org.openmarkov:core` | Modelo de dominio: `ProbNet`, `Node`, `DESNetworkType`, `DESSimulablePotential`, `MonteCarloOptions`, `Criterion` |
| `ca.umontreal.iro.simul:ssj` (3.3.1) | Generadores de números aleatorios (`MRG31k3p`, `RandomStream`) para los flujos de cada nodo |
| `commons-math3` | Estadística sobre los resultados |
| `org.apache.poi` (ooxml/poi) | Exportación de resultados a Excel (`template.xlsx`, `DESResultsExcelWriter`) |
| `org.jfree.jfreechart` | Gráficos en la ventana de resultados (CE/PSA) |
| `java.desktop`, `java.datatransfer` | Swing (la salida es una ventana/diálogo) |

A diferencia de otros módulos de inferencia, **no depende de `inference`** (la propagación exacta) y **no usa `auto-service`**.

## Diferencia clave: no es un `InferenceAlgorithm`

`DESInference` **no** extiende `InferenceAlgorithm` de core ni se registra por SPI. No hay `provides` en `module-info.java` ni `META-INF/services`. El módulo es `open module inference.DES` y solo hace `exports org.openmarkov.inference.DES`.

La invocación es directa, desde el GUI (`gui/.../window/MainPanelListenerAssistant.java`):

```java
new org.openmarkov.inference.DES.DESInference(probNet, simulationProgressMonitor);
```

Es decir, no pasa por `InferenceManager.getDefaultAlgorithm(...)`; el constructor de `DESInference` ejecuta toda la simulación y presenta los resultados en una ventana Swing. Consecuencia: este módulo no se descubre como plugin de inferencia; se enlaza explícitamente desde `gui` (que lo declara como dependencia).

## Tipo de red soportado

Solo redes **DES** (`DESNetworkType`, definido en core). El constructor valida y lanza `OnlyDESNetsAllowedException` si `probNet.getNetworkType()` no es `DESNetworkType`. La novedad respecto a otros tipos de red es el `NodeType.EVENT` (además de CHANCE / DECISION / UTILITY) y los potenciales que implementan `DESSimulablePotential`.

Restricciones de la implementación actual (validadas en `DESInference.initialize`, con avisos por `JOptionPane`):
- Se requieren **al menos dos criterios de decisión** (`probNet.getDecisionCriteria()`).
- Se admite **un único nodo de decisión** (si hay más, aborta; si no hay ninguno, usa una variable de decisión ficticia "No Decision").

## Potenciales simulables: `DESSimulablePotential`

Interfaz de core (`org.openmarkov.core.model.network.potential.DESSimulablePotential`) que deben implementar los potenciales muestreables en DES:

```java
double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents);
default int numRandomNumbersNeeded() { ... }
```

Cada `DESRecord` toma el primer potencial de su nodo como `DESSimulablePotential` y lo muestrea consumiendo números del `DESRandomProvider`. Potenciales típicos en DES (definidos en core): `DistributionTablePotential`, `TransitionTablePotential`, `ExactDistrPotential` (todos exponen un `TablePotential` interno, usado al re-muestrear para PSA).

## Mapa del paquete `org.openmarkov.inference.DES`

```
DESInference            Orquestador. Constructor = ejecutar simulación completa.
                        Bucle: series → individuos → estados de decisión → evaluateIndividual()

GenericEvaluation<T extends DESRecord>   Base abstracta de las tres fases por NodeType.
                        Construye un HashMap<Node,T> por reflexión y detecta nodos huérfanos.
 ├─ ChanceEvaluation    → ChanceRecord    (nodos CHANCE)
 ├─ EventEvaluation     → EventRecord     (nodos EVENT; gestiona la cola de eventos)
 └─ UtilityEvaluation   → UtilityRecord   (nodos UTILITY; acumula utilidad inmediata/continua)

DESRecord               Estado de simulación de un nodo (valor, reloj, padres/ancestros,
                        DESSimulablePotential, su DESRandomProvider). Subclases por tipo.
 ├─ ChanceRecord
 ├─ EventRecord         Lleva además el tiempo de ocurrencia (TTE) y si es terminal.
 └─ UtilityRecord

DESRandomProvider       Flujo de aleatorios por nodo (SSJ MRG31k3p). Memoriza los números
                        consumidos por un individuo para reutilizarlos en cada intervención
                        (varianza común entre alternativas de decisión).
NotRandomStream         RandomStream determinista (testing / reproducibilidad).

CriteriaValues          Valores por criterio de UNA simulación.
SimulationResults       Resultados de un conjunto (serie).
SimulationSummaryResults Acumula todas las series; calcula propiedades estadísticas.
StatsProperties         Media, desviación, intervalos, etc.
EqualCriterion          (en core) criterio de decisión usado aquí; envuelve Criterion.

DataFromFile            Datos externos por individuo (fichero de entrada); DataFromFileOld legacy.

DESLogTextWriter        Log textual de cada simulación (cola de eventos, actualizaciones).
DESResultsWindow        Ventana Swing con los resultados.
DESResultsExcelWriter   Exporta a .xlsx (usa src/main/resources/template.xlsx).
CEDESDialog             Diálogo coste-efectividad / PSA (gráficos JFreeChart).

exception/              OnlyDESNetsAllowedException, NodeMustBeChance/Event/Utility,
                        EventIsNotParentOf.
```

Recursos: `src/main/resources/template.xlsx` (plantilla Excel) y `resultStyle.css` (estilo del informe HTML).

## Cómo funciona una simulación (`evaluateIndividual`)

1. `chanceEvaluation.startSimulation` calcula primero los nodos huérfanos (sin ancestro EVENT).
2. `eventEvaluation.startSimulation` encola los eventos huérfanos en `clock = 0` (desde v5.2 ya no se exige un *Initial Event*).
3. Bucle mientras haya eventos en la cola (`EventEvaluation` mantiene una lista ordenada por tiempo de ocurrencia):
   - extrae el siguiente evento, avanza el reloj a su `timeOfOccurrence`;
   - acumula utilidad continua hasta ese instante (`accrueCumulativeUtility`);
   - actualiza valores de CHANCE y UTILITY (`update`);
   - acumula utilidad inmediata; si el evento es terminal vacía la cola, si no, programa los eventos hijos.
4. Al salir (o al evento terminal) acumula utilidad hasta `timeHorizon` y calcula `CriteriaValues`.

El horizonte temporal sale de `probNet.getInferenceOptions().getTemporalOptions().getHorizon()`; nº de series, nº de simulaciones, PSA y fichero de datos salen de `MonteCarloOptions` (en `InferenceOptions`).

**PSA** (Probabilistic Sensitivity Analysis): si `monteCarloOptions.isPsa()`, antes de cada serie re-muestrea los `UncertainValue` de los `TablePotential` internos de los potenciales del modelo.

## Notas de build

```bash
mvn install                 # tras tener core en ~/.m2
mvn install -DskipTests
```

`src/test/` no existe: el módulo **no tiene tests**. La dependencia SSJ se resuelve desde los repos Nexus configurados en `root/pom.xml`. Al añadir dependencias entre módulos hay que actualizar `requires`/`exports` en `module-info.java`, no solo el POM. Como el módulo es `open`, `GenericEvaluation` puede instanciar los `DESRecord` por reflexión.
