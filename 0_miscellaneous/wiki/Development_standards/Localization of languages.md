OpenMarkov helps you to localize your functionality's output to both the **English** and 
**Spanish (Spain)** languages.

<!-- TOC -->
* [Creating localizations](#creating-localizations)
* [Linking the localizations to OpenMarkov](#linking-the-localizations-to-openmarkov)
* [Using localizations](#using-localizations)
  * [Using Bindings](#using-bindings)
    * [Parametrization of Localization Strings](#parametrization-of-localization-strings)
    * [Binding a directory](#binding-a-directory)
    * [Excluding bindings from suggestions / completions (IntelliJ)](#excluding-bindings-from-suggestions--completions-intellij)
<!-- TOC -->

# Creating localizations

For this, create a directory named ``localize`` anywhere in the ``resources`` directory of your Java
project for your localizations, like ``org.my_domain_name.my_functionality.localize``, in there, 
OpenMarkov will allow you to create localization files in the ```.XML``` file format.

Each of the localization files must specify the language they are, this is done by adding a suffix
in the file name, which is ```_en``` for English, and ``_es`` for Spanish (Spain), so the file 
``org/domain/funct/localize/Test_en.xml`` is a localization file for writing English localizations.

In these localization files you write an element ```properties```, and inside it, you can write any
XML elements you want, and by attaching a ```value``` attribute you can specify how that String will
translate into your language, for example, this file:

````xml
<?xml version="1.0" encoding="UTF-8"?>
<properties>
    <Test>
        <AString value="This string is in English"/>
    </Test>
    <AStringInTopLevel value="This string is in the top level"/>
</properties>
````

... Contains two localization keys: ``Test.AString``, Whose value is *'This string is in English'*,
and ``AStringInTopLevel``: Whose value is *'This string is in the top level'*.

# Linking the localizations to OpenMarkov

OpenMarkov will load these localization files for you, but you need to specify it where OpenMarkov 
should locate them, for this matter, you can create a class that implements
``org.openmarkov.core.localize.spi.LocalizeResourcesProvider`` and specify the path to the
``localize`` directory in there by implementing the ``getRootOfResources`` method with said path,
for example:

````java
public class LocalizationsProvider implements LocalizeResourcesProvider {
    @Override
    public @NotNull String getRootOfResources() {
        return "/org/domain/funct"; //<- 'org/domain/funct' actually points to:
                                    //'src/main/resources/org/domain/funct/localize'.
    }
}
````

And to give OpenMarkov access to this class, you need to make it public, and in case you are using 
[Modules / JPMS(Java Platform Module System;)](https://openjdk.org/projects/jigsaw/spec/) 
this would mean you also need to export this class (Which is done by exporting its package).

# Using localizations

The ``StringDatabase`` gives you a method ```getString(String path)``` that allows you to recover
said string for the current language, so calling:

````java
System.out.println(StringDatabase.getString("Test.AString"));
System.out.println(StringDatabase.getString("AStringInTopLevel"));
````

Would show (If OpenMarkov is set in English):

````cmd
This string is in English
This string is in the top level
````

## Using Bindings

Calling ``StringDatabase.getString(String path)`` can be daunting as you are required to remember
the name of the Paths to said Strings, for this matter, OpenMarkov offers you a utility called
``@BindLocalizations`` that allows to automatically generate a class hierarchy out of your XML
localization files, as creating this manually would be tedious and very consuming regarding 
maintenance.

To use it, you need to add the ```@BindLocalizations``` annotation in a class (Preferably in the
same one where you implement ``LocalizeResourcesProvider``) and specify the localization files to
bind, in the example from before it would look like this:

````java
@BindLocalizations(filePath = "org/domain/funct/localize/Test_en.xml")
public class LocalizationsProvider implements LocalizeResourcesProvider {
    @Override
    public @NotNull String getRootOfResources() {
        return "/org/domain/funct"; //<- 'org/domain/funct' actually points to:
                                    //'src/main/resources/org/domain/funct/localize'.
    }
}
````

...you would automatically get a class binding that localization file, and said would look like:

````java
import org.openmarkov.core.stringformat.StringFormat;

public final class Nls {
    public final class Test {
        public static final class AStringInTopLevel {
            public static String stringify() {
                return org.openmarkov.core.localize.StringDatabase.getUniqueInstance()
                                                                 .getString("TestBindings", "AStringInTopLevel");
            }
        }
        
        public static final class Test {
            public static final class AString {
                public static String stringify() {
                    return org.openmarkov.core.localize.StringDatabase.getUniqueInstance()
                                                                     .getString("TestBindings", "Test.AString");
                }
            }
        }
    }
}
````

Now calling it is similar to how OpenMarkov used to do in the core functionality:

````java
System.out.println(StringDatabase.getString("AStringInTopLevel"));
System.out.println(Nls.Test.AStringInTopLevel.stringify());
````

This prints

````cmd
This string is in the top level
This string is in the top level
````

Note that bindings uses the Java Annotation Processing API, meaning the automatically generated 
binding class will be added to your code, but you won't see it in the ```src``` directory, instead, 
you can find it by inspecting the ``target/generated-sources`` directory.

This also means you cannot modify the binding class, as it is automatically generated, but this also
means it will be updated to any changes in your localization file when you **re-build** the project.

### Parametrization of Localization Strings

When using bindings, you can add parameters to your localization strings to customize these strings 
in runtime, this is done by enclosing a key name in brackets, such as ``{userName}`` or 
``{currentDate, date}``.

Note: This parametrization style is based on Java's 
[MessageFormat](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/MessageFormat.html),
with the sole difference parameters are specified by name (like ``userName`` and ```currentDate```) 
instead of by index.

As an example, you could write a salute in your XML localization file like this:

````xml
<?xml version="1.0" encoding="UTF-8"?>
<properties>
    <GreetUser value="Welcome {userName}, we are going to open {favouriteNetwork}."/>
</properties>
````

And now the ``stringify`` function for GreetUser will have two parameters:

````java
public final class Nls {
    public final class Test {
        public static final class GreetUser {
            public static String stringify(Object vuserName, Object vfavouriteNetwork) {
                return StringFormat.apply(org.openmarkov.core.localize.StringDatabase.getUniqueInstance()
                                                                                    .getString("TestBindings", "Edit.StringWithParameters"),
                                          java.util.Map.ofEntries(java.util.Map.entry("userName", vuserName), java.util.Map.entry("favouriteNetwork", vfavouriteNetwork)));            }
        }
    }
}
````

Now, calling the binding with parametrization is hassle-free, as you only need to fill these
parameters:

````java
System.out.println(Nls.Test.GreetUser.stringify("John Doe", "favourite_network.pgmx"));
````

This prints:

````cmd
Welcome John Doe, we are going to open favourite_network.pgmx.
````

### Binding a directory
``@BindLocalizations`` allows you to create bindings for multiple files at the same time, but you 
might want to bind all the localization files inside a directory, which is useful when you have a 
directory full of localization files, for example, a directory such as 
``resources/org/domain/funct/localize``.

Now, it is tempting to make @BindLocalization to receive said directory, like 
````java
@BindLocalizations(filePath = "org/domain/funct/localize")
public class LocalizationsProvider implements ...
````

And while this will work while you are programming, it is actually invalid, as if your code turns to
become part of OpenMarkov, we will compile it with ``mvn compile`` (Maven), and when generating your
bindings, it will fail as it cannot access directories directly.

To avoid this restriction, we require you to point to a valid file inside that directory, for 
example, the ``Test_en.xml`` created before, and in the BindLocalization annotation add 
``fileIsDirectoryChild = true`` to indicate you are not creating a Binding just from Test_en.xml, 
but for all files inside the same directory where Test_en.xml is located at:

````java
@BindLocalizations(filePath = "org/domain/funct/localize/Test_en.xml", fileIsDirectoryChild = true)
public class LocalizationsProvider implements ...
````

### Excluding bindings from suggestions / completions (IntelliJ)
Your binding elements can have similar names to classes inside your project, for example, if you
have a class named ``Network`` and in the localization file that is bound there are many XML 
elements with names containing ``Network``, such as ``NetworkDialog``, ``AddNetworkFile``...

This means the auto-completion of IDEs such as IntelliJ will show these other elements, while most
of the time, you don't want them to appear.

![LocalizationGuide_Bindings_Suggestions_Wrong.png](https://bitbucket.org/repo/x8Mz4kE/images/2531584587-LocalizationGuide_Bindings_Suggestions_Wrong.png)

To avoid this in IntelliJ you can go to ```File -> Settings -> Editor -> General -> Auto-import```
and find the box saying ``Exclude from auto-import and completion`` and add this exclusion 
``*.Nls.*``.

![LocalizationGuide_Bindings_Suggestions_ExcludeNls.png](https://bitbucket.org/repo/x8Mz4kE/images/3082317991-LocalizationGuide_Bindings_Suggestions_ExcludeNls.png)

This will make so classes named as ``Nls`` will show up in the completion, but nothing inside this
classes will be shown in code completion.

![LocalizationGuide_Bindings_Suggestions_Right.png](https://bitbucket.org/repo/x8Mz4kE/images/3700098535-LocalizationGuide_Bindings_Suggestions_Right.png)

The exception to this is when you write ``Nls.`` and press ``Ctrl + Enter``, which will show you the
contents of the class, and this mean you can still access the contents of ``Nls`` through 
suggestions, but when you are actually trying to use a class of your code, the suggestions won't 
show the contents of ``Nls``

![LocalizationGuide_Bindings_Suggestions_SuggestionsOfNls.png](https://bitbucket.org/repo/x8Mz4kE/images/1683408052-LocalizationGuide_Bindings_Suggestions_SuggestionsOfNls.png)