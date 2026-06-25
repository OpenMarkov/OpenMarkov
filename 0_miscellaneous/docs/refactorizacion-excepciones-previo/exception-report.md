---
title: "Análisis y rediseño del sistema de excepciones de OpenMarkov"
date: "Marzo 2026"
geometry: "left=2.5cm,right=2.5cm,top=2.5cm,bottom=2.5cm"
fontsize: 11pt
mainfont: "DejaVu Serif"
monofont: "DejaVu Sans Mono"
toc: true
toc-depth: 3
numbersections: true
colorlinks: true
---

\newpage

# Estado actual

## Inventario

El proyecto define **61 clases de excepción** propias, todas checked salvo dos (`UnreacheableException` y `UnrecoverableException`):

| Módulo | Clases | Paquete |
|--------|-------:|--------|
| `core` | 27 | `org.openmarkov.core.exception` |
| `gui` | 24 | `org.openmarkov.gui.exception` |
| `io` | 1 | `org.openmarkov.io.probmodel.exception` |
| `learning.core` | 2 | `org.openmarkov.learning.core.exception` |
| `learning.gui` | 2 | `org.openmarkov.learning.exception` |
| `stochasticPropagationOutput` | 1 | (módulo propio) |

## Patrón de clases internas

El proyecto usa extensamente el patrón de **excepción sellada con subclases internas estáticas**. Doce clases base actúan como namespaces de variantes:

| Excepción base | Inner classes |
|----------------|--------------|
| `ConstraintViolatedException` | 42 |
| `ParserException` | 16 |
| `PGMXParserException` | 14 |
| `NonProjectablePotentialException` | 8 |
| `WriterException` | 6 |
| `DoEditException` | 5 |
| `IncompatibleEvidenceException` | 3 |
| `NotEvaluableNetworkException` | 3 |

## Interfaces de localización

Existe un sistema dual de interfaces:

- `IOpenMarkovException` — interfaz base para todas las excepciones propias
- `IBundledOpenMarkovException` — para excepciones con mensajes localizados en `.properties`

No todas las excepciones las implementan de forma consistente.

\newpage

# Problemas identificados

## Excepciones checked que el propio código señala como incorrectas

El archivo fuente de cada excepción contiene un comentario `TODO` indicando que debería ser unchecked. Son **19 casos documentados**:

| Excepción | Argumento del TODO |
|-----------|-------------------|
| `DoEditException` | "Either design is poor, or the exceptions it encloses could be turned into RuntimeExceptions" |
| `IncompatibleEvidenceException` | "Caught and ignored in almost every catch block, probably leading to unexpected bugs" |
| `NonProjectablePotentialException` | "Almost every exception is wrapped into UnreacheableException or shown with JOptionPane" |
| `NotEvaluableNetworkException` | "Only caught to show a generic message, hindering the real reason" |
| `NotSupportedOperationException` | "Always caught and then ignored" |
| `InvalidArgumentException` | "This might be a RuntimeException" |
| `UnexpectedInferenceException` | "Catches are always wrapped with UnreacheableException" |
| `InvalidNetworkTypeException` | "Always wrapped in UnreacheableException" |
| `CannotNormalizePotentialException` | "Does this really happen in the GUI?" |
| `CostEffectivenessException` | "Does this really happen in the GUI?" |
| `NotAllNodesHavePoliciesException` | "Caught and thrown as UnreacheableException" |
| `PotentialOperationException` | "Catches just show it and ignore it, leading to further bugs" |
| `WrongClassException` | "Should probably be UnreacheableException" |
| `UnexpectedMenuActionException` | "Should probably be UnreacheableException" |
| `NoSelectedNodeException` | "Should probably be a RuntimeException" |
| `IntervalsAreNotEvenException` | "Does this really happen in the GUI?" |
| `IntervalsAreNotMultipleOf3Exception` | "Does this really happen in the GUI?" |
| `NonProjectablePotentialException.CannotEvaluate` | "Only used once and wrapped into UnreacheableException" |
| `ThereIsNoPotentialsInNodeException` | "Caught and rethrown as unchecked" |

Esto indica que una porción mayoritaria del sistema de excepciones checked **no aporta valor**: el caller no puede recuperarse de ellas, y simplemente las envuelve en `UnreacheableException` o las muestra en un diálogo.

## Propagación de `throws Exception` genérica

**82 firmas de método** declaran `throws Exception` sin especificar tipo. Esto:

- Obliga al caller a capturar `Exception` o a seguir propagándola
- Oculta el contrato real de cada método
- Impide el análisis estático

Los casos más frecuentes están en el ciclo de aprendizaje (`LearningAlgorithm.getBestEdit()`, `LearningAlgorithm.getNextEdit()`) y en callbacks de plugins.

## Antipatrón: `catch` → `return null`

`FormatManager.java` (líneas 63–78) atrapa `InstantiationException`, `NoSuchMethodException`, `IllegalAccessException` e `InvocationTargetException` de reflexión y **devuelve `null`** sin registrar nada:

```java
// FormatManager.java — ANTIPATRÓN
.map(readerClass -> {
    try {
        return readerClass.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | NoSuchMethodException | ...) {
        return null;  // silenciado; el reader queda null en la lista
    }
})
```

Esto hace que un plugin mal configurado falle silenciosamente más tarde con un `NullPointerException` sin traza útil.

## Conflictos de nombres entre clases internas

Varias inner classes tienen el mismo nombre que otras excepciones del proyecto:

- `WriterException.NonProjectablePotentialException` sombrea a `core.exception.NonProjectablePotentialException`
- `PGMXParserException.EvidenceIsIncompatibleWithOther` sombrea a `IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther`

En ambos casos la inner class representa el mismo concepto que la clase raíz, lo que sugiere código duplicado.

## Typos en nombres de clase (8 casos)

| Nombre actual | Nombre correcto |
|---------------|-----------------|
| `UnsatisfiedContraints` | `UnsatisfiedConstraints` |
| `SamplesWeigthIsZero` | `SamplesWeightIsZero` |
| `MissingPropertiesOfContiousVariable` | `MissingPropertiesOfContinuousVariable` |
| `CannotAsignPotentialToStaticVariable` | `CannotAssignPotentialToStaticVariable` |
| `TriedToSplitInvervalOutsideBoundsException` | `TriedToSplitIntervalOutsideBoundsException` |
| `ValueOutOfDomaingRangeException` | `ValueOutOfDomainRangeException` |
| `NotEnoughtMemoryException` | `NotEnoughMemoryException` |
| `MissingPropertiesOfContiousVariable` | `MissingPropertiesOfContinuousVariable` |

## `IOException` y excepciones de infraestructura en APIs públicas

Varios métodos públicos de la GUI y de los módulos I/O declaran `throws IOException`, `throws SAXException` o `throws ParserConfigurationException`. Esto expone detalles de implementación (que se usa SAX, que se usa JDOM) al llamador, acoplando las capas innecesariamente.

## Excepciones checked cruzando capas incorrectas

`DoEditException` (definida en `core`) aparece en **113 firmas**, incluyendo código de GUI y de aprendizaje. Esto obliga a todas las capas superiores a conocer y declarar una excepción de la capa más baja, invirtiendo el sentido de la dependencia.

\newpage

# Propuestas de rediseño

## Propuesta A — Convertir a unchecked las excepciones no recuperables

**Impacto: alto. Riesgo: medio.**

Las excepciones que el código propio ya identifica como candidatas a unchecked deben convertirse a `RuntimeException`. El criterio es: **si el caller nunca puede hacer nada útil al capturarla más que mostrar un mensaje de error, debe ser unchecked**.

Candidatas inmediatas:

- `NonProjectablePotentialException` → nueva `PotentialComputationException` (unchecked)
- `NotEvaluableNetworkException` → unchecked
- `InvalidArgumentException` → `IllegalArgumentException` estándar de Java
- `NotSupportedOperationException` → `UnsupportedOperationException` estándar de Java
- `InvalidNetworkTypeException` → unchecked
- `UnexpectedInferenceException` → unchecked
- `CannotNormalizePotentialException` → unchecked

El impacto es eliminar miles de líneas de `throws` en cascada y simplificar todos los callers.

## Propuesta B — Reducir la superficie de excepciones checked a las verdaderamente recuperables

**Impacto: alto. Riesgo: bajo.**

Mantener como checked **solo** las excepciones ante las cuales el llamador tiene una acción correctiva concreta:

| Excepción | Acción correctiva posible |
|-----------|--------------------------|
| `ParserException` | Mostrar mensaje, ofrecer abrir otro fichero |
| `WriterException` | Reintentar en otra ruta |
| `NoReaderForFileException` | Pedir al usuario que instale el plugin |
| `EmptyDatabaseException` | Pedir al usuario otro fichero |
| `IncompatibleEvidenceException` | Retirar la evidencia conflictiva (acción GUI) |
| `DoEditException` (solo algunas subclases) | Mostrar mensaje al usuario |

Todo lo demás debería ser unchecked.

## Propuesta C — Eliminar `throws Exception` genérica en las APIs de aprendizaje

**Impacto: medio. Riesgo: bajo.**

Los métodos `getBestEdit()` y `getNextEdit()` de `LearningAlgorithm` declaran `throws Exception`. La propuesta es:

1. Identificar qué excepciones concretas lanzan las implementaciones actuales.
2. Declarar esas excepciones específicas, o
3. Si son todas no recuperables, convertirlas a unchecked y eliminar el `throws`.

## Propuesta D — Corregir el antipatrón `catch → null` en `FormatManager`

**Impacto: bajo. Riesgo: muy bajo.**

```java
// ANTES
} catch (InstantiationException | ... e) {
    return null;
}

// DESPUÉS
} catch (InstantiationException | ... e) {
    throw new PluginConfigurationException(
        "Cannot instantiate reader " + readerClass.getName(), e);
}
```

Donde `PluginConfigurationException` sería una nueva unchecked exception que falla rápido con contexto útil.

## Propuesta E — Reestructurar jerarquía con tres categorías claras

**Impacto: alto. Riesgo: alto.**

Actualmente las excepciones están organizadas por módulo pero no por naturaleza. Propuesta de jerarquía semántica:

```
OpenMarkovException (base checked)
├── UserInputException          ← el usuario hizo algo inválido
│   ├── IncompatibleEvidenceException
│   ├── ParserException
│   └── WriterException
└── NetworkStructureException   ← la red está mal formada
    ├── NotEvaluableNetworkException
    └── ConstraintViolatedException

OpenMarkovRuntimeException (base unchecked)
├── InternalConsistencyException  ← reemplaza UnreacheableException
├── PluginException               ← error en sistema de plugins
└── PotentialComputationException ← reemplaza NonProjectablePotentialException
```

## Propuesta F — Corregir typos y conflictos de nombres

**Impacto: bajo. Riesgo: muy bajo.**

Renombrar las 8 clases con errores ortográficos. Al ser inner classes o clases poco usadas en APIs públicas, el impacto es mínimo y puede hacerse con refactor automático.

Resolver los conflictos de sombra eliminando las inner classes duplicadas (`WriterException.NonProjectablePotentialException`, `PGMXParserException.EvidenceIsIncompatibleWithOther`) y usando directamente las clases raíz correspondientes.

## Propuesta G — Ocultar excepciones de infraestructura en las APIs de I/O

**Impacto: medio. Riesgo: bajo.**

Los métodos que hoy declaran `throws SAXException, IOException` deben envolver esas excepciones en tipos propios del dominio:

```java
// ANTES
public ProbNetReader getProbNetReader(URL url)
    throws SAXException, IOException, NoReaderForFileException, ParserException;

// DESPUÉS
public ProbNetReader getProbNetReader(URL url)
    throws NoReaderForFileException, ParserException;
    // SAXException e IOException quedan como causa interna
```

\newpage

# Plan de migración

| Fase | Cambios | Riesgo |
|------|---------|--------|
| **1 — Inmediata** | Typos, conflictos de nombres (Propuesta F), antipatrón `FormatManager` (Propuesta D) | Muy bajo |
| **2 — Corto plazo** | Convertir a unchecked las 7 candidatas de Propuesta A | Medio |
| **3 — Medio plazo** | Eliminar `throws Exception` genérica; especificar APIs de aprendizaje (Propuesta C) | Bajo |
| **4 — Largo plazo** | Reestructuración jerárquica completa (Propuesta E) | Alto |

Las fases 1–3 son independientes entre sí y mejoran la situación sin requerir un rediseño global. La fase 4 es una refactorización mayor que conviene abordar cuando el resto del sistema esté estabilizado.
