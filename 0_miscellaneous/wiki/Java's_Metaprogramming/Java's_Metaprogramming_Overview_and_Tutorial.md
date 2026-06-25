Table of contents:
<!-- TOC -->
* [Meta-programming](#meta-programming)
* [Meta-programming in Java](#meta-programming-in-java)
* [Creating custom Annotations for Annotation Processing](#creating-custom-annotations-for-annotation-processing)
  * [Adding fields to an annotation](#adding-fields-to-an-annotation)
  * [Limiting the elements of code the annotation can be applied to](#limiting-the-elements-of-code-the-annotation-can-be-applied-to)
  * [Setting the scope where this annotation is available](#setting-the-scope-where-this-annotation-is-available)
* [Creating custom Annotation Processors](#creating-custom-annotation-processors)
  * [Common Dependencies for Annotation Processing](#common-dependencies-for-annotation-processing)
  * [Defining an annotation processor](#defining-an-annotation-processor)
  * [Generating the logic of the Processor](#generating-the-logic-of-the-processor)
  * [Advice on defining processor logic](#advice-on-defining-processor-logic)
  * [Registering the processor](#registering-the-processor)
  * [Using the processor from another project](#using-the-processor-from-another-project)
* [Tutorial](#tutorial)
  * [Defining the Annotation Processing project](#defining-the-annotation-processing-project)
  * [Defining the annotation](#defining-the-annotation)
  * [Specifying the processor](#specifying-the-processor)
  * [Registering the processor](#registering-the-processor-1)
  * [Using the processor in a different project](#using-the-processor-in-a-different-project)
<!-- TOC -->

# Meta-programming

Meta-programming allows to create programs where source-code acts as input, allowing to process it
in order create new code, verify it follows some standards, or to modify the original code.

In most programming languages, meta-programming is implemented as program written in the native
language, that process pieces of some code of the same programming language, meaning it is possible
to create Java code that creates Java code.

Meta-programming is composed of a series of different techniques, and most of the programming
languages just implement a handful of them, such as Macros being implemented in Rust or Scala,
Meta-Classes in Python or Ruby, or template-programming in C++ or D.

# Meta-programming in Java

Java's main metaprogramming system is the
[Annotation Processor API](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html)
that allows developers to create custom
[Annotations](https://docs.oracle.com/javase/8/docs/technotes/guides/language/annotations.html)
and attach them to pieces of code like a class, a method, or a parameter, where they can create a
[Processor](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html)
to take said components that have the annotation attached as an input and allowing to alter the way
said code behaves or to emit information about it.

It's main use is to reduce code by auto-generating it, ex: If we have a ``Person`` class with the
fields ``age`` and ``name``, and we want to apply the [Builder pattern](https://en.wikipedia.org/wiki/Builder_pattern)
to it by creating a class named ```PersonBuilder``` with the methods ``withAge(age)``,
``withName(name)`` and ``build()``, it would lead us to a very long class whose definition is 
obvious by just looking at the ```Person``` class, so instead, we can attach a  ```@Builder``` 
annotation to it that will create the boilerplate code for us, meaning once we declare our 
``Person`` class as follows:

````java
@Builder
public class Person {
  private int age;
  private String name;

  public Person(int age, String name){
    this.age = age;
    this.name = name;
  }
}
````

... And then the ``@Builder`` annotation would be process to give a ``BuilderPerson`` class like 
this one:

````java
import java.util.Optional;

/**
 * Builder constructor for the [Person] class.
 */
public class PersonBuilder {
  private Optional<Integer> age;
  private Optional<String> name;

  /**
   * Adds the parameter age.
   *
   * @param age The value of age.
   */
  public withAge(int age){
    this.age=Optional.of(age);
  }

  /**
   * Adds the parameter name.
   *
   * @param name The value of name.
   */
  public withName(String name){
    this.name=Optional.of(name);
  }

  /**
   * Creates an instance of [Person] with age and name.
   *
   * @return A Person with age and name. 
   * @throws Exception if age or name is not indicated.
   */
  public Person build() throws Exception {
    if(age.isEmpty()){
      throw new Exception("The field age is not indicated for this Person");
    }
    if(name.isEmpty()){
      throw new Exception("The field name is not indicated for this Person");
    }
    return new Person(age.get(), name.get());
  }
}
````

This way, the source code would contain a ``Person`` class, but we don't have to define the
``PersonBuilder`` class in our code, as the ```@Builder```'s processor will generate it in our stead
when compiling the code, this also means that if our ```Person``` class was to be modified, for
example, to add a new field like ``gender`` or ``id``, the ```PersonBuilder``` would also be
automatically be modified, without requiring to manually change it.

However, the Annotation Processor API has a limitation: It allows you to create new files at
compile-time where you can create new code, but **it doesn't let you modify the already existing
code at compile time**, for that matter, libraries such as [lombok](https://projectlombok.org/)
allow to modify code at compile time, but for doing so, they use
[certain APIs](https://docs.oracle.com/en/java/javase/21/docs/api/jdk.compiler/module-summary.html)
of the [Java compiler](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html) that
allow them to modify the AST
of the code during the compilation phase, specifically after the compiler verifies the syntax and
generates the AST, and before the compiler begins to create the Java bytecode; This has been around
since the year 2011, meaning it is unlikely to change, but if Java's compiler was to change or the
Compiler's API was to change either the way it works or its visibility, projects such as this one 
could be exposed.

Java also has another metaprogramming system called
[Reflections](https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/index.html), in
which information about classes and their structure can be accessed at runtime, and also proves to
make easier to replace existing classes with dynamically created classes, but this happens at
runtime rather than compile-time, meaning programmers will likely not receive any help by the own
programming language or their IDE when creating code with said dynamically created classes, as they
usually don't have access to these new classes until the program is run, unlike compile-time
generated classes, which can be discovered after compiling it; For such matter, we won't be covering
Reflections here.

The Annotation Processing API is a system exclusive to Java, but it resembles to some of the common
techniques of meta-programming that are implemented in other programming languages, such as the more
powerful [Procedural macros ](https://doc.rust-lang.org/reference/procedural-macros.html) from Rust.

# Creating custom Annotations for Annotation Processing

Annotations, also known as
[Annotation Interfaces](https://docs.oracle.com/javase/specs/jls/se21/html/jls-9.html#jls-9.6), act
as entry points to Annotation Processor(s) as shown earlier, and they are usually defined in a
separate file, similar to a class, following this pattern:

````java
package my.projectpackage;

@interface MyAnnotationName {
  // Fields
}
````

## Adding fields to an annotation

Fields (properly called
[Annotation Interface Elements](https://docs.oracle.com/javase/specs/jls/se21/html/jls-9.html#jls-9.6.1)
) can be assigned for Annotations, as long as their type is a primitive, a ``String``, a ``class``,
an ``enum``, a sub-``annotation``, or an array of the previous (This means multidimensional arrays 
are <span style="color:red">**not**</span> allowed), this is useful when there is information that
is needed for the developer using the annotation to provide, or to allow them more refined
configuration about how their annotation will be used, for example:

````java
public @interface XMLToConstants {
  String inPackage() default "";
  String inBaseClass();
  int maxRecursion();
}
````

This ```XMLToConstants``` annotation has three fields:

- ``inPackage``: It's a ``String`` whose default value is `` ``, meaning the user of the annotation
  is not forced to write this field.
- ``inBaseClass``: It's a ``String`` without a default value, so the user is forced to write a value
  for this field.
- ``maxRecursion``(): It's an ``int`` without a default value, so the user is forced to write a
  value for this field.


## Limiting the elements of code the annotation can be applied to

It is possible to limit the elements an annotation can be applied to just certain elements such as
classes, methods, constructors or fields, among others; This makes it simple for the developer of
the Annotation Processor to reduce its scope and therefore to be able to expand information on what
source code components it can use.

For doing this, the annotation has to be annotated with
[``@Target({ *types here* })`` ](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/Target.html)
specifying said elements, for example:

````java
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface NotNull { }
````

Here, the annotation ```NotNull``` can be applied to a field of a class (
[``ElementType.FIELD``](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/ElementType.html#FIELD)
), or to a parameter of a method (
[``ElementType.PARAMETER``](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/ElementType.html#PARAMETER)
), you can find more information about every target possible either in the
[@Target Annotation Reference](https://docs.oracle.com/javase/specs/jls/se21/html/jls-9.html#jls-9.6.4.1),
where every possible target is defined, or in the
[ElementType's Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/ElementType.html).

## Setting the scope where this annotation is available

By attaching the
[``@Retention(*Retention policy*)``](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/Retention.html)
annotation it is possible to limit how long an annotation will be recorded, having the following
[Retention Policies](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/RetentionPolicy.html):

- [``RetentionPolicy.SOURCE``](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/RetentionPolicy.html#SOURCE):
  The annotation will only be available to the compiler, and once the compilation is done, the
  annotation is discarded, meaning nor Java's Virtual Machine nor us as programmers can access the
  annotation using reflections, as they won't exist.
- [``RetentionPolicy.CLASS``](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/RetentionPolicy.html#CLASS):
  The annotation will be saved once the compilation is done, but won't be read by the Java's Virtual
  Machine, meaning we still cannot access it using reflections, as, just  like before, they won't
  exist.
  <br>This is the default RetentionPolicy when none is set.
- [``RetentionPolicy.RUNTIME``](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/RetentionPolicy.html#RUNTIME):
  The annotation will be saved once the compilation is done and also by the Java's Virtual Machine,
  meaning we can access them using
  [Reflections](https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/index.html), which
  allows other means of meta-programming, but those are different to the Annotation Processor
  described here, and whose main difference is that Annotation Processing happens during compilation,
  while Reflections' metaprogramming happens at runtime.

If an annotation will only be used by an Annotation Processor and Reflections won't be used, using
[``RetentionPolicy.SOURCE``](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/RetentionPolicy.html#SOURCE)
is advised to both prevent others from accessing the annotation at runtime and saving (a bit) of
resources, this can be done as follows:

````java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface MyCompileTimeAnnotation {
  //...
}
````

# Creating custom Annotation Processors

An Annotation Processor is a class implementing the
[Processor](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html)
interface, this class is able to receive the fulfilled
[Annotation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/Annotation.html)s
the user wrote, and to call a
[process](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html#process(java.util.Set,javax.annotation.processing.RoundEnvironment))
method where an output to said annotations is created, such as the generation of code.

When generating code, some programming languages require you to write code that is syntactically
correct by giving you convenient structures, such as giving you structures that represent an
[AST](https://en.wikipedia.org/wiki/Abstract_syntax_tree), but Java's Annotation Processor allows
you to write simple ``String``s, this is because it allows you to create new files, then these files
are compiled, meaning you aren't required to follow strict rules, as Java's Compiler will handle
verifying the correctness of said file later when compiling.

## Common Dependencies for Annotation Processing

In order to make the creation of an Annotation Processor, there is two dependencies that allow
making this process more comfortable and less boilerplate:

- [Google's Auto-Service](https://mvnrepository.com/artifact/com.google.auto.service/auto-service):
  Annotation Processors follow
  [Java's Service System](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html), and
  as such, projects are required to register their processor(s) as service(s), this library
  automatically handles registering most of the information about it as a service in our stead.
  <br>This act as a dependency, meaning it should appear inside the ```depencencies``` tag inside the
  ``pom.xml`` file.
- [Apache Maven's Compiler Plugin](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-compiler-plugin):
  This allows for Maven to compile the processor(s)'s code at compile time and to use it when
  compiling the user's source code.
  <br>This act as a plugin, meaning it should appear inside the ```build``` => ```plugins``` tags
  inside the ``pom.xml`` file.

When using them in an Annotation Processing project, its ``pom.xml`` would look like this:

````xml
<?xml?> <!-- ... -->
<project> <!-- ... -->

  <!-- ... -->

  <dependencies>
    <!-- ... -->

    <!-- This generates annotations metadata -->
    <dependency>
      <groupId>com.google.auto.service</groupId>
      <artifactId>auto-service</artifactId>
      <version>1.1.1</version>
    </dependency>

    <!-- ... -->
  </dependencies>
  <build>
    <plugins>
      <!-- ... -->
      <!-- This automatically compiles the sources -->
      <plugin>

        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.14.0</version>
        <configuration>
          <source>${maven.compiler.source}</source>
          <target>${maven.compiler.target}</target>
        </configuration>

        <!-- ... -->
      </plugin>
      <!-- ... -->
    </plugins>
  </build>
</project>

````

## Defining an annotation processor

When defining an Annotation Processor, it should implement the
[Processor](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html),
interface, which can also be made easier by extending the
[Abstract Processor](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/AbstractProcessor.html)
class; Since the case where processor extends a different class from ``AbstractProcessor`` (Or any
subclass of it) is not a usual case, we will continue explaining how ``AbstractProcessor`` extension
works.

When defining an Annotation Processor, it is needed to specify the path to the annotations this
Processor will process, this can be done in two ways, one is by overriding the
[getSupportedAnnotationTypes](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html#getSupportedAnnotationTypes())
method, and the other is by attaching a
[@SupportedAnnotationTypes](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/SupportedAnnotationTypes.html)
annotation, in both it is needed specifying the paths to the annotations.

If the use Google's Auto-Service is possible, this would be the step to attach the ``@AutoService``
annotation specifying the service to load is a
[Processor](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html).

A base for an ``AbstractProcessor`` could look like this:

````java
package our.processor.processing;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("our.processor.annotation.OurAnnotation")
@AutoService(Processor.class)
public class OurProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Your processing logic here.

    return true; // When returning true, no more processing of this annotation type will happen,
    //  this is usually the behaviour you are looking for.
  }

}
````

<span style="color:blue">**Note**</span>: ``SupportedAnnotationTypes`` is not the only configuration
that can set via both a method or an annotation, the list of all the options following this is:

- ``SupportedAnnotationTypes``: A series of annotations this Processor process.
- ``SupportedOptions``: A series of options that can be attached to the Processor.
- ``SupportedSourceVersion``: A single
  [SourceVersion](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/lang/model/SourceVersion.html)
  specifying the source version the Processor supports.
  <br>A common practice is to override
  [getSupportedSourceVersion](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html#getSupportedSourceVersion())
  and return
  [SourceVersion.latestSupported](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/lang/model/SourceVersion.html#latestSupported())
  to specify the latest supported version that is fully supported by the environment, or just
  [SourceVersion.latest](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/lang/model/SourceVersion.html#latest())
  to specify the last source version available.
  <br>This is primarily used when the Processor uses new features of Java that are subject to
  change, but it is also good practice to include the number version of the JDK that is mainly
  getting target, such as
  [SourceVersion.RELEASE_21](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/lang/model/SourceVersion.html#RELEASE_21)
  to specify JDK21 is the target.

## Generating the logic of the Processor

The
[process](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Processor.html#process(java.util.Set,javax.annotation.processing.RoundEnvironment))
method is where most of the logic of the Processor happens, the goal here is usually to create new
source files from the annotations, for doing so, the AbstractProcessor includes a series of
utilities:

- ``process`` method's
  [RoundEnvironment](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/RoundEnvironment.html)
  parameter: processing annotations happens in rounds, as when generating new source code it might
  lead to new code with annotations from the processors in it.
  <br>This variable given by the ``process`` method allows to get information of both the elements
  from this round annotated with the annotations set in the ``SupportedAnnotationTypes``, and also
  those from the last round.
- ``process`` method's
  [Set<? extends TypeElement>](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/lang/model/element/TypeElement.html)
  parameter: A ``TypeElement`` contains the annotations that are defined, this is usually used to
  call
  ``roundEnv.getElementsAnnotatedWith(`*Any annotation of the indicated in SupportedAnnotationTypes*`)``
  in order to get all elements marked with an annotation for the current round.
- ``AbstractProcessor``'s
  [ProcessingEnvironment](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/ProcessingEnvironment.html)
  *processingEnv* field: This protected field of ``AbstractProcessor`` gives plenty of utilities
  aimed to interact with the developer, creating new source files, or finding either source code's
  elements or information about them, among others.
  <br>Said utilities are given by multiple
  [getters](https://docs.oracle.com/javaee/6/tutorial/doc/gjbbp.html) inside
  ``ProcessingEnvironment``, they are:
  - [ProcessingEnvironment.getElementUtils()](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/ProcessingEnvironment.html#getElementUtils())
    -> [Elements](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/lang/model/util/Elements.html):
    Contains implementations of utility methods for operating on elements, such as
    [getPackageOf(Element element)](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/lang/model/util/Elements.html#getPackageOf(javax.lang.model.element.Element))
    to get the full name of the package where an element is originated from.
  - [ProcessingEnvironment.getFiler()](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/ProcessingEnvironment.html#getFiler())
    -> [Filer](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Filer.html):
    Contains implementations for creating new source files, including new classes and new resources,
    for example:
    [createSourceFile(CharSequence name, Element... originatingElements)](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Filer.html#createSourceFile(java.lang.CharSequence,javax.lang.model.element.Element...))
    allows to create a new source file with the indicated name for the source code file.
    Ex: A class ``Human`` could be created in the package ``org.project`` with the following code:

    ````java
    import javax.annotation.processing.AbstractProcessor;
    import javax.tools.JavaFileObject;
    import java.io.Writer;
    //...
    public class OurProcessor extends AbstractProcessor {
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
          JavaFileObject fileObject = processingEnv.getFiler().createSourceFile("org.project.Human");
          try (Writer writer = fileObject.openWriter()) {
            writer.write("package org.project.Human; public class Human{ public Human(){ " +
             "System.out.println(\"You called the default constructor!\") } }");
          }
          return true;
        }
        //...
    }
    ````
  - [ProcessingEnvironment.getMessager()](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/ProcessingEnvironment.html#getMessager())
    -> [Messager](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Messager.html):
    Allows to print information to the developer when compiling its source code, each one of these
    messages have a diagnostic level indicated either by calling
    [Messager.printMessage](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Messager.html#printMessage(javax.tools.Diagnostic.Kind,java.lang.CharSequence))
    with the level indicated through a [Diagnostic.Kind](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/tools/Diagnostic.Kind.html),
    such as [Diagnostic.Kind.ERROR](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/tools/Diagnostic.Kind.html#ERROR)
    or [Diagnostic.Kind.WARNING](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/tools/Diagnostic.Kind.html#WARNING),
    or through the concise methods created from Java 18, such as [Messager.printError](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Messager.html#printError(java.lang.CharSequence))
    or [Messager.printWarning](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/Messager.html#printWarning(java.lang.CharSequence)).
    <br>When printing a message, you can also attach the message to an element of the source code,
    this is extremely useful to tell the user when something needs to change.
    <br>The following code serves to check whether a class follows a name convention or not, and in
    case it doesn't, it shows a warning on the class, so the developer can check it:

    ````java
    import javax.annotation.processing.*;
    import javax.lang.model.element.Element;
    import javax.lang.model.element.TypeElement;
    import java.util.Set;
      
    //...
    public class ClassNameVerifierProcessor extends AbstractProcessor {
          
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            for (TypeElement annotation : annotations) {
                for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                    var className = element.getSimpleName().toString();
                    var firstChar =  String.valueOf(className.charAt(0)) ;
                    if (!firstChar.equals(firstChar.toUpperCase())){
                        processingEnv.getMessager().printWarning(
                              "This class name doesn't start with a capitalised letter", element);
                    }
                }
            }
            return true;
        }
        //...   
    }
    ````
    Note: Printing an error message will prevent the code from being compiled, it is pretty much the
    same as allowing to throw an error that a compiler would throw.

## Advice on defining processor logic

Debugging an annotation processor can prove challenging, and your IDE might not support it, because
of this, you can separate the logic in two:

- Annotation information extraction: This is where your processor extracts the annotation, this
  happens in the ``AbstractProcessor.process`` we discussed earlier.
- Logic in creating the output: This is where you take the information your processor has gathered
  and create the output.

By doing this separation, you will end up with components completely abstracted of the Annotation
Processing logic, which means you can easily create tests over your logic and also to debug it as
executable programs, but it also means you lose the ability of accessing the Annotation Processing
environment, so choose wisely according to your program's needs.

## Registering the processor

Processors need to be registered in order for the compiler to discover them as services and to use
them during compilation, the registration is made by creating a
``META-INF/services/javax.annotation.processing.Processor`` file and writing the path to every
processor in separate lines on said file.

Example: If there was a processor class named ``ClassNameVerifierProcessor`` in the package
``org.project``, then the file should contain a single line with
``org.project.ClassNameVerifierProcessor``.

## Using the processor from another project

Processors and annotations are usually defined in a different project from the one they are used in,
for this matter, the user project needs to depend on the annotation processor project; When doing
so, if annotations are marked to be retained just during compilation (This is, when they are marked
with ```@Retention(RetentionPolicy.SOURCE)```), it will be also possible to use the annotation
processor project as a compile dependency, meaning the annotation project won't be loaded by saved
in the final executable, and therefore it won't be loaded by the JVM, saving resources.

For doing this, a user project could depend on an annotation processing project having the following
in their ``pom.xml``:

````xml
<?xml?> <!-- ... -->
<project> <!-- ... -->

  <!-- ... -->

  <dependencies>
    <!-- ... -->

    <dependency>
      <groupId>org.annotation.company</groupId>
      <artifactId>annotationproject</artifactId>
      <version>X.X.X</version>
      <scope>provided</scope> <!-- With the 'provided' scope, the dependency will only be 
                                         available during compilation -->
    </dependency>

    <!-- ... -->
  </dependencies>

</project>
````


# Tutorial

In this tutorial, we will create an annotation ``@Builder`` that once attached to a class, it will
generate a builder class for it.

If you are unfamiliar with the [Builder pattern](https://en.wikipedia.org/wiki/Builder_pattern), you
can basically reduce it to a pattern where you have a series of methods that allow you to specify
how you want a class to be constructed, for example, a ```DialogBuilder``` would give you methods
such as ```withTitle(title)```, ``withMessage(message)``, and after using them, a ``build()`` method
will give you the ```Dialog``` object; This pattern is more complex and powerful, but for the sake
of making this as simple as possible and not to strive away from the goal of learning the Annotation
Processor API, we will continue with this definition.

In this case, we are twisting one matter to make this Builder simpler, instead of creating builder
methods out of the fields of the class, we will generate them from the single constructor of the
class, throwing an error to the developer if the class doesn't have exactly one single constructor.

## Defining the Annotation Processing project

To allow saving resources, the Annotation Processors are usually defined in a different project,
from which the developers will depend on, by making this a different project, we can allow them to
use our annotations and processors **just at compile-time**, this means nor the final executable nor
the JVM have the need of retain our project, hence saving resources and creating a smaller
executable.

As specified [earlier](#common-dependencies-for-annotation-processing), we will be using
[Google's Auto-Service](https://mvnrepository.com/artifact/com.google.auto.service/auto-service) and
[Apache Maven's Compiler Plugin](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-compiler-plugin)
to leverage the creation of metadata and source files' compilation of our project and processor(s).

This means our ```pom.xml``` would look like this:

````xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.openmarkov</groupId> <!-- Replace for your groupId -->
  <artifactId>TestAnnotation</artifactId> <!-- Replace for your artifactId -->
  <version>1.0-SNAPSHOT</version> <!-- Replace for your versioning -->
  <packaging>pom</packaging>

  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- This generates annotations metadata -->
    <dependency>
      <groupId>com.google.auto.service</groupId>
      <artifactId>auto-service</artifactId>
      <version>1.1.1</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <!-- This automatically compiles the sources -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.14.0</version>
        <configuration>
          <source>${maven.compiler.source}</source>
          <target>${maven.compiler.target}</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
````

## Defining the annotation

This is the point where we establish the contract with the developer, as they only interact with the
annotation, so we should define what information we want them to provide for us and place them here.

In our case, we want to know what we should prefix every builder method with, they are usually
prefixed with '*with*', so applying it to a field named ```age``` would produce a ``withAge``
method, but the developer might have another prefix in mind, meaning we should ask them for it,
making our annotation to be as follows:

```java
package org.openmarkov;

import java.lang.annotation.*;

/**
 * Classes annotated with the Builder annotation will have a class implementing the builder pattern 
 * automatically generated.
 */
@Target(ElementType.TYPE) // This target means the annotation can only be attached to classes, 
// enums, interfaces, or records.
// Since our goal is to make the annotation limited to classes, we will
// throw a compile-time error if the developer attaches it to an enum, an
// interface, or a record.
@Retention(RetentionPolicy.SOURCE) // The retention is set to Source, meaning the Java Virtual 
// Machine won't hold this annotation, not allowing to use it for
// reflections.
public @interface Builder {
  /**
   * Specifies a string with which every setter is built, for example, having a field 'age' and the 
   * prefix 'with', a method called withAge would be provided.
   *
   * @return a prefixed string to prefix every setter method with.
   */
  String prefix() default "with";
}
```

## Specifying the processor

The processor is where we will take the annotation and then to process it to generate an output;
First we need to declare the processor, for doing so we will create a Processor class extending
[AbstractProcessor](https://docs.oracle.com/en/java/javase/21/docs/api/java.compiler/javax/annotation/processing/AbstractProcessor.html),
and on said processor we will specify the annotations it ``process``es (The annotation we created
before), the source version it supports, and we will mark it with
[Google's Auto-Service](https://mvnrepository.com/artifact/com.google.auto.service/auto-service) to
leverage the creation of metadata.

This far, our code would look like this.

````java
package org.openmarkov;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

/**
 * This processor receives classes annotated with Builder, and creates a Builder class for them in  
 * the same package where they are.
 * <p>
 * The processor will throw a compile-time error if the class has more than 1 constructor, or if the 
 * target isn't a class (Meaning it is an enum, an interface, or a record).
 */
@SupportedAnnotationTypes("org.openmarkov.Builder")  // This processor processes the annotations
// marked with the builder annotation.
@SupportedSourceVersion(SourceVersion.RELEASE_21) // This processor specifies it's supported version
// is that of Java 21.
@AutoService(Processor.class) // This AutoService automatically feeds the project with metadata of 
// our processor without requiring us to do it manually.
public class BuilderProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    //TODO
  }
}
````

On this point, we start to define how the processor will generate the output, for this, we will take
every source code's  element marked with our annotation, and then we will extract information about
it, like its constructors, the name of the class, or it's package.

We also should be aware some of these inputs might be incorrect, for example, if the element is not
a class, as the target we chose earlier was also available to enums, interfaces and records, or if
the class has more than one constructor, in any of those cases, we should **not** throw an
exception, but instead show a compile-time error to the user and continue processing other builders,
or else, he might find more errors than it should.

- Example to showcase the importance of continuing generating code on the Processor when having
  failed could be if the programmer had two classes, A and B, and he was using A with a builder in
  is project, but now adds a builder annotation to B, if during the compilation, B gets processed
  earlier than A and fails, then A won't get processed either, even if A could have generated a
  valid builder, but now he gets errors about B's builder not being compiled, and errors in every
  place where A's builder was used, as the compilation won't get its builder, even when it should
  have.
  <br>By printing errors through the Messager class, we can prevent the code from being compiled and
  show them the errors that happened along descriptive messages to bring its attention to solve
  them.

Once we extracted the required information from the source code, we will call to a method with said
information to generate the builder's code as a String; Remember Java's compiler is the one to
process our String as valid Java code, that's why we are free to generate the code as a raw String.

This far the ``process`` method looks like this:

````java
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This process method is where the logic happens, our goal here is to find the classes annotated 
 * with Builder, and to find out if they are a class with a single constructor or not, in a negative
 * case, a compile-time error is thrown through the messager.
 * <p>
 * In a positive case, we find the first constructor, and out of it, we get its parameters, and from
 * there we just need to create a String where we write a class implementing the Builder pattern for
 * the base class.
 *
 * @param annotations the annotation interfaces requested to be processed, in this case, it can just
 *                    be Builder annotations, but we have to be aware we might receive an empty set,
 *                    this is a requirement made by Java.
 * @param roundEnv  environment for information about the current and prior round, it allows us to
 *                  access the elements.
 */
@Override
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
  processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing Builders");

  annotations.stream()
             // We get all the annotations annotated with builder (Remember the annotation set can 
             // only be either empty, or to contain our Builder annotation).
             .flatMap((TypeElement annotation) ->
                              roundEnv.getElementsAnnotatedWith(annotation).stream())
             // We filter the elements to be classes, otherwise, we print an error and ignore the 
             // flow of said element.
             .filter((Element element) -> {
               boolean isClass = ElementKind.CLASS.equals(element.getKind());
               if (!isClass) {
                 processingEnv.getMessager()
                              .printError("The @Builder annotation cannot be applied to this type,"
                                                  + " is only allowed for classes", element);
               }
               return isClass;
             })
             // Classes match the TypeElement, so we can fearlessly cast from Element to 
             // TypeElement.
             .map(classElement -> (TypeElement) classElement)
             // Now, for each class annotated with Builder, we will find its constructors, if it has
             // more than 1 constructor, then it will throw a compile-time error and stop the flow;
             // otherwise, we will create the builder class via String concatenations and then to 
             // write it in a source file.
             .forEach(classElement -> {
               // We find the builder annotation for this element in order to get the information 
               // inside our annotation, this is to say, the prefix.
               // We can do this gracefully as our processor doesn't process more than one root
               // annotation.
               Builder builderInfo = classElement.getAnnotation(Builder.class);
               String prefix = builderInfo.prefix();
               // Through the elements utils we can get information of the elements, for example, 
               // the package's whole path in which the class is defined.
               String definedPackage = processingEnv.getElementUtils()
                                                    .getPackageOf(classElement).toString();

               // Classes have a series of enclosed elements, such as fields, methods, 
               // constructors... Our goal now is to find a single constructor.
               List<ExecutableElement> constructors = classElement
                       .getEnclosedElements()
                       .stream()
                       // We filter out the contained elements that are constructors.
                       .filter(element -> ElementKind.CONSTRUCTOR.equals(element.getKind()))
                       // Methods and constructors are ExecutableElements, so we can gracefully 
                       // cast them.
                       .map(field -> (ExecutableElement) field)
                       .toList();

               // If there is more than one constructor, we throw an error to the user explaining
               // this problem.
               if (constructors.size() != 1) {
                 String errorMessage = "Class is annotated with @Builder, but there is "
                         + constructors.size()
                         + " constructors, while it was expected to only have 1.";
                 // The error is printed over the class that has more than 1 constructor.
                 processingEnv.getMessager().printError(errorMessage, classElement);
                 // We return, allowing other builders to be processed, this is done so it can also
                 // find correct ones to prevent false-positive errors where he built classes with
                 // correct builders, and also to find other builders that are wrongly defined.
                 return;
               }

               List<? extends VariableElement> constructorParameters =
                       constructors.getFirst().getParameters();
               String builderClassName = classElement.getSimpleName() + "Builder";
               // The builder class will be place at the same location as the base class.
               String builderClassSourceFileLocation = definedPackage + "." + builderClassName;
               // The call to 'createBuilder' is a custom-made function tasked with generating the
               // class as a raw String, and it is where we define how we want the builder class to
               // look like.
               String builderClassDefinition = createBuilder(
                       definedPackage,
                       classElement.getSimpleName().toString(),
                       builderClassName,
                       prefix,
                       constructorParameters);

               try {
                 // We create a javaFileObject were we will write our generated class contents.
                 JavaFileObject javaFileObject = processingEnv
                         .getFiler()
                         .createSourceFile(builderClassSourceFileLocation);
                 // We use a 'try' to ensure the file is closed after writing.
                 try (Writer writer = javaFileObject.openWriter()) {
                   // We write the contents now.
                   writer.write(builderClassDefinition);
                 }
               } catch (IOException e) {
                 // If an IO error happened, we can't no longer do anything about it, so we show it
                 // to the developer, but we don't throw it, we print it through printError so it 
                 // still process other builders.
                 processingEnv.getMessager().printError(e.toString(), classElement);
               }
             });

  return true; // No further processing of this annotation type
}
````

Now the last step is to create the output code, for doing so, we have the ``createBuilder`` method
we used earlier in the code, in here we will generate the builder's code as a simple String, we
don't need to use special structures for this matter as Java's compiler will be the one to
the contents of said String and to parse it as a proper class.

The creation of this method won't result in very clean code as it does when using other
Meta-Programming techniques like Macros, so it is very important to document this method thoroughly,
and showing what each step of generating the output String is can somewhat help to that matter.

````java
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is where we generate the builder's code as a simple String, this String is literally nothing
 * more than a string with the contents of the class, it doesn't need to make anything special, as 
 * Java's compiler will be the one to interpret the contents of said String and to parse it as a 
 * proper class.
 * <p>
 * Generating the final String is usually not the cleanest step, so it's usually very important to
 * document this thoroughly, especially showing what each step of generating the String is.
 * <p>
 * For the making the documentation cleaner, we are going to consider the following values:
 *
 * @param packagePath org.openmarkov
 * @param originalClassName Human
 * @param builderClassName HumanBuilder
 * @param prefix with
 * @param constructorParameters String name, int age
 */
private String createBuilder(
        String packagePath,
        String originalClassName,
        String builderClassName,
        String prefix,
        List<? extends VariableElement> constructorParameters
) {
  String originalClassPath = packagePath + "." + originalClassName;
  // This defines the fields inside the Builder class.
  // In the example, it would create a String containing:
  //
  // private String name;
  // private int age;
  String builderFields = constructorParameters
          .stream()
          .map(constructorParameter -> "private "
                  + constructorParameter.asType()
                  + " " + constructorParameter.getSimpleName() + ";")
          .collect(Collectors.joining(System.lineSeparator()));

  // This creates the builder methods for the builder class.
  // In the example, it would create a String containing:
  //
  // public HumanBuilder withName(String value) { this.name = value; return this; }
  // public HumanBuilder withAge(int value) { this.age = value; return this; }
  String constructorMethods = constructorParameters
          .stream()
          .map(constructorParameter -> {
            String fieldNameWithCapitalization = constructorParameter.getSimpleName()
                                                                     .toString();
            fieldNameWithCapitalization = fieldNameWithCapitalization.substring(0, 1).toUpperCase()
                    + fieldNameWithCapitalization.substring(1);
            return "public " + builderClassName + " " + prefix + fieldNameWithCapitalization +
                    "(" + constructorParameter.asType() + " value)" +
                    "{this." + constructorParameter.getSimpleName()
                    + "=value; return this;}";
          })
          .collect(Collectors.joining(System.lineSeparator()));

  // This verifies every value is set, and this would be called before constructing the value with 
  // the build method.
  // In the example, it would create a String containing:
  //
  // if (this.name == null) {
  //     throw new RuntimeException("Value 'name' is not set in builder");
  // }
  //
  // Note: There is no verifier for 'age' because it is a primitive, meaning they cannot be null.
  String fieldsVerifications = constructorParameters
          .stream()
          .filter(constructorParameter -> !constructorParameter.asType().getKind().isPrimitive())
          .map(constructorParameter -> "if (this." + constructorParameter.getSimpleName()
                  + " == null) {\n throw new RuntimeException(\"Value '"
                  + constructorParameter.getSimpleName()
                  + "' is not set in builder\"); \n}")
          .collect(Collectors.joining(System.lineSeparator()));

  // This generates the names for constructing the class, in the example, it would create a String 
  // containing:
  // this.age, this.name
  //
  // The goal is for it to be called afterward in the Human's constructor, so it will later turn to:
  // new Human(this.age, this.name)
  String constructorThisFields = constructorParameters
          .stream()
          .map(constructorParameter -> "this." + constructorParameter.getSimpleName())
          .collect(Collectors.joining(", "));

  // This creates the 'build' method of the builder class.
  // in the example, it would create a String containing:
  //
  // public org.openmarkov.Human build() {
  //     if (this.name == null) {
  //         throw new RuntimeException("Value 'name' is not set in builder");
  //     }
  //     return new org.openmarkov.Human(this.name, this.age);
  // }
  String buildMethod =
          "public " + originalClassPath + " build() {"
                  + System.lineSeparator()
                  + fieldsVerifications
                  + System.lineSeparator()
                  + "return new " + originalClassPath + "(" + constructorThisFields + ");"
                  + System.lineSeparator()
                  + "}";

  // This generates the whole class using the information we got this far.
  // in the example, it would create a String containing:
  //
  // package org.openmarkov;
  //
  // public class HumanBuilder {
  //     private java.lang.String name;
  //     private int age;
  //
  //     public HumanBuilder() {
  //     }
  //
  //     public HumanBuilder withName(java.lang.String value) {
  //         this.name = value;
  //         return this;
  //     }
  //
  //     public HumanBuilder withAge(int value) {
  //         this.age = value;
  //         return this;
  //     }
  //
  //     public org.openmarkov.Human build() {
  //         if (this.name == null) {
  //             throw new RuntimeException("Value 'name' is not set in builder");
  //         }
  //         return new org.openmarkov.Human(this.name, this.age);
  //     }
  // }
  return "package " + packagePath + " ;"
          + "public class " + builderClassName + "{"
          + System.lineSeparator()
          + builderFields
          + System.lineSeparator()
          + "public " + builderClassName + "(){} "
          + System.lineSeparator()
          + constructorMethods
          + System.lineSeparator()
          + buildMethod
          + System.lineSeparator()
          + "}";
}
````

Now we finished creating the Processor, which is the hardest step, this is a snippet contains the
whole processor class:

````java
package org.openmarkov;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This processor receives classes annotated with Builder, and creates a Builder class for them in 
 * the same package  where they are.
 * <p>
 * The processor will throw a compile-time error if the class has more than 1 constructor, or if the
 * target isn't a class (Meaning it is an enum, an interface, or a record).
 */
@SupportedAnnotationTypes("org.openmarkov.Builder")  // This processor process the annotations
// marked with the builder annotation.
@SupportedSourceVersion(SourceVersion.RELEASE_21) // This processor specifies it's supported version
// is that of Java 21.
@AutoService(Processor.class) // This AutoService automatically feeds the project with metadata of
// our processor without requiring us to do it manually.
public class BuilderProcessor extends AbstractProcessor {

  /**
   * This process method is where the logic happens, our goal here is to find the classes annotated 
   * with Builder, and to find out if they are a class with a single constructor or not, in a 
   * negative case, a compile-time error is thrown through the messager.
   * <p>
   * In a positive case, we find the first constructor, and out of it, we get its parameters, and 
   * from there we just need to create a String where we write a class implementing the Builder 
   * pattern for the base class.
   *
   * @param annotations the annotation interfaces requested to be processed, in this case, it can 
   *                    just be Builder annotations, but we have to be aware we might receive an 
   *                    empty set, this is a requirement made by Java.
   * @param roundEnv  environment for information about the current and prior round, it allows us
   *                  to access the elements.
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing Builders");

    annotations.stream()
               // We get all the annotations annotated with builder (Remember the annotation set can
               // only be either empty, or to contain our Builder annotation).
               .flatMap((TypeElement annotation) ->
                                roundEnv.getElementsAnnotatedWith(annotation).stream())
               // We filter the elements to be classes, otherwise, we print an error and ignore the
               // flow of said element.
               .filter((Element element) -> {
                 boolean isClass = ElementKind.CLASS.equals(element.getKind());
                 if (!isClass) {
                   processingEnv.getMessager()
                                .printError("The @Builder annotation cannot be applied to this type,"
                                                    + " is only allowed for classes", element);
                 }
                 return isClass;
               })
               // Classes match the TypeElement, so we can fearlessly cast from Element to 
               // TypeElement.
               .map(classElement -> (TypeElement) classElement)
               // Now, for each class annotated with Builder, we will find its constructors, if it 
               // has more than 1 constructor, then it will throw a compile-time error and stop the
               // flow; otherwise, we will create the builder class via String concatenations and
               // then to write it in a source file.
               .forEach(classElement -> {
                 // We find the builder annotation for this element in order to get the information
                 // inside our annotation, this is to say, the prefix.
                 // We can do this gracefully as our processor doesn't process more than one root
                 // annotation.
                 Builder builderInfo = classElement.getAnnotation(Builder.class);
                 String prefix = builderInfo.prefix();
                 // Through the elements utils we can get information of the elements, for example,
                 // the package's whole path in which the class is defined.
                 String definedPackage = processingEnv.getElementUtils()
                                                      .getPackageOf(classElement).toString();

                 // Classes have a series of enclosed elements, such as fields, methods, 
                 // constructors... Our goal now is to find a single constructor.
                 List<ExecutableElement> constructors = classElement
                         .getEnclosedElements()
                         .stream()
                         // We filter out the contained elements that are constructors.
                         .filter(element -> ElementKind.CONSTRUCTOR.equals(element.getKind()))
                         // Methods and constructors are ExecutableElements, so we can gracefully
                         // cast them.
                         .map(field -> (ExecutableElement) field)
                         .toList();

                 // If there is more than one constructor, we throw an error to the user explaining 
                 // this problem.
                 if (constructors.size() != 1) {
                   String errorMessage = "Class is annotated with @Builder, but there is "
                           + constructors.size()
                           + " constructors, while it was expected to only have 1.";
                   // The error is printed over the class that has more than 1 constructor.
                   processingEnv.getMessager().printError(errorMessage, classElement);
                   // We return, allowing other builders to be processed, this is done so it can 
                   // also find correct ones to prevent false-positive errors where he built classes
                   // with correct builders, and also to find other builders that are wrongly
                   // defined.
                   return;
                 }

                 List<? extends VariableElement> constructorParameters
                         = constructors.getFirst().getParameters();
                 String builderClassName = classElement.getSimpleName() + "Builder";
                 // The builder class will be place at the same location as the base class.
                 String builderClassSourceFileLocation = definedPackage + "." + builderClassName;
                 // The call to 'createBuilder' is a custom-made function tasked with generating the
                 // class as a raw String, and it is where we define how we want the builder class 
                 // to look like.
                 String builderClassDefinition = createBuilder(
                         definedPackage,
                         classElement.getSimpleName().toString(),
                         builderClassName,
                         prefix,
                         constructorParameters);

                 try {
                   // We create a javaFileObject were we will write our generated class contents.
                   JavaFileObject javaFileObject = processingEnv
                           .getFiler()
                           .createSourceFile(builderClassSourceFileLocation);
                   // We use a 'try' to ensure the file is closed after writing.
                   try (Writer writer = javaFileObject.openWriter()) {
                     // We write the contents now.
                     writer.write(builderClassDefinition);
                   }
                 } catch (IOException e) {
                   // If an IO error happened, we can't no longer do anything about it, so we show 
                   // it to the developer, but we don't throw it, we print it through printError so
                   // it still process other builders.
                   processingEnv.getMessager().printError(e.toString(), classElement);
                 }
               });

    return true; // No further processing of this annotation type
  }

  /**
   * This is where we generate the builder's code as a simple String, this String is literally
   * nothing more than a string with the contents of the class, it doesn't need to make anything 
   * special, as Java's compiler will be the one to interpret the contents of said String and to
   * parse it as a proper class.
   * <p>
   * Generating the final String is usually not the cleanest step, so it's usually very important to
   * document this thoroughly, especially showing what each step of generating the String is.
   * <p>
   * For the making the documentation cleaner, we are going to consider the following values:
   *
   * @param packagePath org.openmarkov
   * @param originalClassName Human
   * @param builderClassName HumanBuilder
   * @param prefix with
   * @param constructorParameters String name, int age
   */
  private String createBuilder(
          String packagePath,
          String originalClassName,
          String builderClassName,
          String prefix,
          List<? extends VariableElement> constructorParameters
  ) {
    String originalClassPath = packagePath + "." + originalClassName;
    // This defines the fields inside the Builder class.
    // In the example, it would create a String containing:
    //
    // private String name;
    // private int age;
    String builderFields = constructorParameters
            .stream()
            .map(constructorParameter -> "private "
                    + constructorParameter.asType()
                    + " " + constructorParameter.getSimpleName() + ";")
            .collect(Collectors.joining(System.lineSeparator()));

    // This creates the builder methods for the builder class.
    // In the example, it would create a String containing:
    //
    // public HumanBuilder withName(String value) { this.name = value; return this; }
    // public HumanBuilder withAge(int value) { this.age = value; return this; }
    String constructorMethods = constructorParameters
            .stream()
            .map(constructorParameter -> {
              String fieldNameWithCapitalization = constructorParameter.getSimpleName()
                                                                       .toString();
              fieldNameWithCapitalization = fieldNameWithCapitalization.substring(0, 1).toUpperCase()
                      + fieldNameWithCapitalization.substring(1);
              return "public " + builderClassName + " " + prefix + fieldNameWithCapitalization +
                      "(" + constructorParameter.asType() + " value){this."
                      + constructorParameter.getSimpleName()
                      + "=value; return this;}";
            })
            .collect(Collectors.joining(System.lineSeparator()));

    // This verifies every value is set, and this would be called before constructing the value with
    // the build method.
    // In the example, it would create a String containing:
    //
    // if (this.name == null) {
    //     throw new RuntimeException("Value 'name' is not set in builder");
    // }
    //
    // Note: There is no verifier for 'age' because it is a primitive, meaning they cannot be null.
    String fieldsVerifications = constructorParameters
            .stream()
            .filter(constructorParameter -> !constructorParameter.asType().getKind().isPrimitive())
            .map(constructorParameter -> "if (this." + constructorParameter.getSimpleName()
                    + " == null) {\n throw new RuntimeException(\"Value '"
                    + constructorParameter.getSimpleName()
                    + "' is not set in builder\"); \n}")
            .collect(Collectors.joining(System.lineSeparator()));

    // This generates the names for constructing the class, in the example, it would create a String
    // containing:
    // this.age, this.name
    //
    // The goal is for it to be called afterward in the Human's constructor, so it will later turn
    // to:
    // new Human(this.age, this.name)
    String constructorThisFields = constructorParameters
            .stream()
            .map(constructorParameter -> "this." + constructorParameter.getSimpleName())
            .collect(Collectors.joining(", "));

    // This creates the 'build' method of the builder class.
    // in the example, it would create a String containing:
    //
    // public org.openmarkov.Human build() {
    //     if (this.name == null) {
    //         throw new RuntimeException("Value 'name' is not set in builder");
    //     }
    //     return new org.openmarkov.Human(this.name, this.age);
    // }
    String buildMethod =
            "public " + originalClassPath + " build() {"
                    + System.lineSeparator()
                    + fieldsVerifications
                    + System.lineSeparator()
                    + "return new " + originalClassPath + "(" + constructorThisFields + ");"
                    + System.lineSeparator()
                    + "}";

    // This generates the whole class using the information we got this far.
    // in the example, it would create a String containing:
    //
    // package org.openmarkov;
    //
    // public class HumanBuilder {
    //     private java.lang.String name;
    //     private int age;
    //
    //     public HumanBuilder() {
    //     }
    //
    //     public HumanBuilder withName(java.lang.String value) {
    //         this.name = value;
    //         return this;
    //     }
    //
    //     public HumanBuilder withAge(int value) {
    //         this.age = value;
    //         return this;
    //     }
    //
    //     public org.openmarkov.Human build() {
    //         if (this.name == null) {
    //             throw new RuntimeException("Value 'name' is not set in builder");
    //         }
    //         return new org.openmarkov.Human(this.name, this.age);
    //     }
    // }
    return "package " + packagePath + " ;"
            + "public class " + builderClassName + "{"
            + System.lineSeparator()
            + builderFields
            + System.lineSeparator()
            + "public " + builderClassName + "(){} "
            + System.lineSeparator()
            + constructorMethods
            + System.lineSeparator()
            + buildMethod
            + System.lineSeparator()
            + "}";
  }
}
````

## Registering the processor

Registering the processor allows for the compiler to discover the processor during the compilation
phase in order to use it on the early stages of compilation, for doing this, we need to create a
file in the path ``META-INF/services`` called ``javax.annotation.processing.Processor``, this is a
file where every line is a reference to a processor in our project, hence, we should write
``org.openmarkov.BuilderProcessor`` in that file.

And then... we are done generating the processor project!

## Using the processor in a different project

To use the processor in a different project, you only need to depend on it as any other dependency,
with the difference that, since we made our annotation to be discarded by the compiler when we set
it ``@Retention(RetentionPolicy.SOURCE)``, we can set the scope of this processor library to
``provided``, meaning it will be used in compilation, but it won't be saved in the final executable,
and therefore it won't be loaded by the JVM, saving resources, preventing the annotations from being
used for reflections and making the resulting ```.jar``` slightly smaller.

This means our user's ```pom.xml``` could look like this:

````xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.openmarkov</groupId> <!-- Replace for your groupId -->
  <artifactId>AnnotationUser</artifactId> <!-- Replace for your artifactId -->
  <version>1.0-SNAPSHOT</version> <!-- Replace for your versioning -->
  <packaging>pom</packaging>

  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.openmarkov</groupId> <!-- Replace for your processor's groupId -->
      <artifactId>TestAnnotation</artifactId> <!-- Replace for your processor's artifactId -->
      <version>1.0-SNAPSHOT</version> <!-- Replace for your processor's versioning -->
      <scope>provided</scope>
    </dependency>
  </dependencies>
</project>
````

To use it, they only need to call the annotation we defined earlier, and then to re-compile the
code, meaning if they have a class such as:

````java
package org.openmarkov.using_annotation;

@Builder
public class Human {
  private String name;
  private int age;

  public Human(String name, int age) {
    this.name = name;
    this.age = age;
  }
}
````

They can re-compile the project, and when so, they can find the generated class in two different
locations:

- ```target/generated-sources/annotations``` Generated class are found here as they are outputted by
  the processor, with no refining made by the compiler.
  <br>They would look for the class in here, meaning they need to go to
  ``org.openmarkov.using_annotation.HumanBuilder`` and they would find this:
````java
package org.openmarkov.annotation_using;

public class HumanBuilder {
  private java.lang.String name;
  private int age;

  public HumanBuilder() {
  }

  public HumanBuilder withName(java.lang.String value) {
    this.name = value;
    return this;
  }

  public HumanBuilder withAge(int value) {
    this.age = value;
    return this;
  }

  public org.openmarkov.annotation_using.Human build() {
    if (this.name == null) {
      throw new RuntimeException("Value 'name' is not set in builder");
    }
    return new org.openmarkov.annotation_using.Human(this.name, this.age);
  }
}
````

- ```target/classes```: Generated class are found here, but with the complete expansion Java does,
  for example, if a class had no constructors, Java automatically generates an empty constructor,
  which can be found here.
  <br>They would look for the class in here, meaning they need to go to
  ``org.openmarkov.using_annotation.HumanBuilder`` and they would find this (Notice IntelliJ's
  header and the lack of calling the ```Human``` class by its full path):
````java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.openmarkov.annotation_using;

public class HumanBuilder {
  private String name;
  private int age;

  public HumanBuilder() {
  }

  public HumanBuilder withName(String value) {
    this.name = value;
    return this;
  }

  public HumanBuilder withAge(int value) {
    this.age = value;
    return this;
  }

  public Human build() {
    if (this.name == null) {
      throw new RuntimeException("Value 'name' is not set in builder");
    } else {
      return new Human(this.name, this.age);
    }
  }
}
````