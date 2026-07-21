# Informe de mantenimiento de pom.xml

## Contexto

Durante el desarrollo de las nuevas heurísticas de eliminación y la actualización del módulo de
inferencia se detectaron tres problemas de configuración Maven que impedían la compilación o la
ejecución de los tests. A continuación se describe cada corrección.

---

## 1. POM raíz (`org.openmarkov`): dependencia de test `assertj-swing`

**Problema.** Los tests del módulo `org.openmarkov.gui` utilizan AssertJ-Swing para probar
componentes Swing. La librería no estaba declarada en el POM raíz, por lo que Maven no la
descargaba y los tests de GUI fallaban al compilar.

**Corrección.** Se añadió la dependencia en la sección `<dependencyManagement>` del POM raíz con
alcance `test`:

```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-swing</artifactId>
    <version>3.17.1</version>
    <scope>test</scope>
</dependency>
```

Al declararla en el POM raíz, cualquier módulo descendiente puede usarla sin necesidad de repetir
la versión.

---

## 2. Módulo `org.openmarkov.full`: conflicto de `module-info` en el JAR ensamblado

**Problema.** El plugin `maven-assembly-plugin` estaba configurado con
`<appendAssemblyId>false</appendAssemblyId>`, lo que hacía que el artefacto ensamblado ("fat JAR")
se publicara con el mismo nombre de artefacto que el JAR normal del módulo. Esto provocaba un
conflicto de `module-info.class` durante la compilación de los tests de integración (`integrationtests`),
que dependen de `org.openmarkov.full` y encontraban dos entradas incompatibles para el mismo módulo
JPMS en el classpath.

**Corrección.** Se cambió el valor a `true`:

```xml
<appendAssemblyId>true</appendAssemblyId>
```

Con esto el artefacto ensamblado recibe el sufijo del descriptor de ensamblado (p. ej. `-jar-with-dependencies`),
diferenciándolo del JAR normal y eliminando el conflicto.

---

## 3. Módulo `org.openmarkov.io`: dependencia faltante de `org.openmarkov.inference`

**Problema.** El módulo `io` utiliza clases del módulo `inference` (en particular tipos relacionados
con la propagación y el acceso a potenciales durante la escritura/lectura de redes). Al no estar
declarada la dependencia en su `pom.xml`, Maven no exponía el módulo `inference` al compilador al
procesar `io`, produciendo errores de "package not visible" y de símbolo no encontrado.

**Corrección.** Se añadió la dependencia explícita:

```xml
<dependency>
    <groupId>org.openmarkov</groupId>
    <artifactId>org.openmarkov.inference</artifactId>
    <version>0.3.0-SNAPSHOT</version>
</dependency>
```

Esto refleja la dependencia real que ya existía implícitamente en tiempo de ejecución (a través del
classpath del módulo `full`) pero que no estaba declarada para la compilación del módulo en
aislamiento.

---

## Resumen

| Módulo | Cambio | Motivo |
|---|---|---|
| `org.openmarkov` (raíz) | Añadir `assertj-swing:3.17.1` (test) | Tests de GUI no compilaban |
| `org.openmarkov.full` | `appendAssemblyId` → `true` | Conflicto `module-info` en integrationtests |
| `org.openmarkov.io` | Añadir dependencia `org.openmarkov.inference` | Errores de compilación por visibilidad de paquetes |
