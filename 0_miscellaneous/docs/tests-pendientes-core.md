# Tests pendientes en `org.openmarkov.core`

> Análisis previo a la refactorización de las clases críticas del módulo core.
> Objetivo: identificar qué pruebas hay que escribir antes de modificar código para evitar regresiones.

---

## Contexto

El módulo `core` tiene 84 ficheros de test (JUnit 5 + AssertJ), pero la cobertura es muy desigual.
Las cinco clases candidatas a refactorización acumulan la mayoría de las lagunas:

| Clase | Líneas | Tests `@Test` | Métodos públicos | Cobertura aprox. |
|---|---|---|---|---|
| `ProbNet` | 1 583 | 29 | 98 | ~30 % |
| `DiscretePotentialOperations` | 2 495 | 23 | 42 | ~55 % |
| `Node` | 1 038 | 0 propios | 55+ | ~0 % |
| `Variable` | 1 018 | 4 | 50+ | ~8 % |
| `TablePotential` | 1 413 | 9 (1 `@Disabled`) | 45 | ~20 % |

`Node` no tiene ningún fichero de test propio; el `NodeTest.java` existente pertenece
al grafo genérico (`model/graph/`), no al nodo de red probabilística.

---

## Tier 1 — Crítico (escribir antes de tocar código)

Estos métodos son exactamente los que la refactorización va a mover o dividir.
Sin tests que los cubran, cualquier cambio es ciego.

### `ProbNet`

| Método | Problema |
|---|---|
| `deepCopy()` | No existe ningún test. El `testCopy()` solo prueba `copy()` con una red mínima. |
| `copy()` con redes complejas | Sin test con agentes, criterios de decisión ni `LinkRestrictionPotential`. |
| `addNodeConsistently()` | Inicialización automática de potenciales según tipo de nodo; sin test. |
| `setNetworkType()` | Cambia restricciones activas al cambiar de BN a ID; sin test. |
| `getPotentials(PotentialRole)` | Una de las 6 variantes sin test. |
| `getProbPotentials(Variable)` | Sin test. |
| `getAdditivePotentials()` | Sin test. |
| `getSortedPotentials()` | Sin test. |
| `getConstantPotentials()` | Sin test. |

Casos de borde adicionales que faltan en métodos ya testados:

- `copy()` / `deepCopy()` con `constantPotentials`, agentes multiagente y criterios de decisión.
- `addNodeConsistently()` con nodos que violarían restricciones (`MaxNumParents`, `NoLoops`).
- `getPotentials(NodeType)` con todos los valores de `NodeType` (CHANCE, DECISION, UTILITY, SV_PRODUCT, SV_SUM).
- `checkConstraints(Iterable)` con subconjunto vacío.
- `setNetworkType()` cuando el nuevo tipo introduce restricciones conflictivas con la red actual.

### `Node` — crear `NodeDomainTest` desde cero

| Método | Problema |
|---|---|
| `absorbNodeConsistently()` | Absorción en IDs (multiply/marginalize para CHANCE, maximize para DECISION). Sin test. |
| `setVariableTypeConsistently()` | Actualización recursiva de potenciales en nodos hijo. Sin test. |
| `setPotentialConsistently()` | Actualización con restricciones de link. Sin test. |
| `setPotential()` / `addPotential()` / `removePotential()` | Gestión básica de potenciales. Solo cubiertos indirectamente. |
| `setUniformPotential()` | Inicialización uniforme. Sin test. |
| `isSuperValueNode()` / `getUtilityParents()` | Predicados de nodo super-valor. Sin test. |
| `getUtilityFunction()` | Recursión con `SumPotential`/`ProductPotential`. Sin test. |
| `clone(ProbNet)` | Clonación profunda. Sin test. |

### `Variable`

| Método / caso | Problema |
|---|---|
| `modifyState(ADD, ...)` | Sin test en absoluto. |
| `modifyState(REMOVE, ...)` | Sin test; ¿qué pasa si quedan 0 estados? |
| `modifyState(RENAME, ...)` | `testRenameState` existe pero no prueba renombre a nombre duplicado ni índice inválido. |
| `modifyState(UP, ...)` | Sin test. |
| `modifyState(DOWN, ...)` | Sin test. |
| `setStates()` | Reemplazo completo de estados. Sin test. |
| `getState(String)` con duplicados | ¿Qué devuelve si dos estados tienen el mismo nombre? Sin test. |
| `setVariableType()` (transiciones) | FINITE_STATES → NUMERIC, NUMERIC → DISCRETIZED. Sin test. |
| `clone()` | Sin test. |
| `equals()` / `hashCode()` | Sin test. |
| Variables temporales | `setTimeSlice()`, `getBaseName()`, `isTemporal()`. Sin test. |

### `DiscretePotentialOperations`

| Método / sobrecarga | Problema |
|---|---|
| `marginalize(TablePotential, Variable)` | Sin test directo. |
| `marginalize(TablePotential, List<Variable>)` | Sin test directo. |
| `marginalize(TablePotential, List, Variable)` | Sin test directo. |
| `multiplyAndMarginalize(TablePotential, TablePotential, Variable)` | Sin test. |
| `multiplyAndMarginalize(List, List)` | Sin test. |
| `multiplyAndMarginalize(List, Variable)` | Sin test. |
| `multiplyAndEliminate()` | Sin test. |
| `normalize()` | Sin test. |
| `divide()` con denominador cero | Sin test de este caso especial. |
| `divide()` con variables en distinto orden | Sin test. |
| `multiply()` con `reorder=true` vs `false` | Sin verificación de equivalencia. |
| `sumByCriterion()` | Sin test. |
| `projectOutVariable()` | Sin test. |
| `getAccumulatedOffsets()` (2 sobrecargas) | Sin test. |
| `merge()` | Sin test. |
| `createZeroUtilityPotential()` / `createUnityProbabilityPotential()` | Sin test. |

### `TablePotential`

| Método | Problema |
|---|---|
| `tableProject(EvidenceCase, ...)` | El único test (`testProject1`) está `@Disabled`. |
| `getValue(EvidenceCase)` | Sin test directo. |
| `multiplyByInverseAndMarginalize()` | Sin test. |
| `deepCopy()` | Sin test completo. |
| `reorder()` | Sin test propio (hay tests indirectos en `DiscretePotentialOperations`). |
| `sample()` / `sampleConditional()` | Sin test. |
| `setUniform()` | Sin test. |
| `equals()` | Sin test. |
| Valores especiales | NaN, Infinity, tabla con suma cero antes de normalizar. Sin test. |

---

## Tier 2 — Importante

### Sistema de ediciones (`action/`)

El módulo tiene ~55 ediciones concretas en `action/core/` y solo **un fichero de test**
(`EditsHistoryTest`). Si la refactorización de `ProbNet` o `Node` rompe una edición,
no hay red de seguridad.

Ediciones prioritarias a testear:

- `AddNodeEdit` / `RemoveNodeEdit`
- `AddLinkEdit` / `RemoveLinkEdit`
- `AddPotentialEdit` / `RemovePotentialEdit`
- `ChangeNodeTypeEdit`
- `ChangeVariableTypeEdit`
- `RenameVariableEdit`

`PNESupport` (gestión de listeners y undo/redo) tampoco tiene tests de concurrencia
ni de listeners. El campo `listeners` es un `HashSet` sin sincronización; la iteración
concurrente puede lanzar `ConcurrentModificationException`.

### Redes temporales

Ningún test de `ProbNet` usa variables temporales (`[t]`, `[t+1]`):

- `getShiftedVariable()` cuando la variable desplazada no existe.
- `addShiftedNode()` con desplazamientos distintos.
- `containsShiftedVariable()`.

### Redes multiagente

- `modifyAgent()` con los diferentes `StateAction`.
- `getAgents()` / `setAgents()`.
- `isMultiagent()`.

---

## Tier 3 — Deseable a medio plazo

Clases sin ningún test propio y no triviales:

| Clase | Paquete | Interés |
|---|---|---|
| `NodeTypeDepot` | `model/network/` | Estructura auxiliar que gestiona índices por tipo; usada en `ProbNet` |
| `PartitionedInterval` | `model/network/` | Discretización de variables continuas |
| `CEP` | `model/network/` | Potenciales para coste-efectividad |
| `Criterion` | `model/network/` | Criterios de decisión multicriterio |
| `CycleLength` | `model/network/` | Metadatos de redes temporales |
| `StringWithProperties` | `model/network/` | Almacén de metadatos de estados |
| `UniqueStack` | `model/network/` | Estructura auxiliar usada en `ProbNet` |
| `UniformPotential` | `model/network/potential/` | Potencial uniforme inicial |
| `DeltaPotential` | `model/network/potential/` | Evidencia dura |
| `SumPotential` / `ProductPotential` | `model/network/potential/` | Nodos super-valor |

---

## Tests con dependencia de ficheros externos (más frágiles)

Los tests de inferencia (`InferenceAlgorithmBNTest`, `InferenceAlgorithmIDTest`,
`InferenceAlgorithmDANTest`) cargan ficheros `.pgmx` desde el classpath.
Son aceptables para inferencia pero no son el modelo a seguir para los tests
nuevos de `core`: los tests del Tier 1 deben construir las redes programáticamente,
sin depender de ficheros externos.

---

## Orden de implementación recomendado

1. `ProbNet` — `deepCopy()` con redes complejas (agentes + criterios + restricciones de link).
   Es la red de seguridad para unificar `auxCopy`/`deepCopy`, que es el ítem crítico 5
   del plan de refactorización.
2. `Node` — crear `NodeDomainTest` con `absorbNodeConsistently()` y `setVariableTypeConsistently()`.
3. `Variable` — completar `modifyState()` con los 5 `StateAction`.
4. `TablePotential` — rehabilitar y ampliar `testProject1`.
5. `DiscretePotentialOperations` — añadir las sobrecargas faltantes de `marginalize()`
   y `multiplyAndMarginalize()`.
6. Ediciones concretas — al menos `AddNodeEdit`, `RemoveNodeEdit`, `AddLinkEdit`.
