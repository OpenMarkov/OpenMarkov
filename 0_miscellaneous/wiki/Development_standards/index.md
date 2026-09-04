# Developing OpenMarkov

OpenMarkov has a [Working methodology](Working_methodology.md) with good practices for developing
code in OpenMarkov.

# Extension points

OpenMarkov has several extension points for adding code easily, and many just imply creating classes
that extend a certain base class or implement a certain interface.

Many of them just require your implementation without requiring you to manually glue it to the app.
This is done by using
[Java's reflection](https://www.oracle.com/technical-resources/articles/java/javareflection.html)
via our
[Plugin Search](https://github.com/OpenMarkov/OpenMarkov/blob/development/core/src/main/java/org/openmarkov/plugin/PluginSearch.java),
which allows us to find your implementations of certain classes and interface.
Take the [tool plugins](ToolPlugins.md) for example, you only need to create a class extending
``ToolPlugin``, and a new option will appear in the ``Tool`` menu.

The most relevant extension points are the following:

* [Menu items](Menu_items.md).
* [Edits](Edits/Edits.md).
* [Constraints](Constraints.md) and network types.
* [Learning algorithms](Learning_algorithms.md).
* [Metrics](Metrics.md) for search-and-score learning algorithms.
* [Localization](Localization%20of%20languages.md) for localizing text to English and Spanish
  (Outdated and no longer in use).
* [Tool plugins](ToolPlugins.md) for options on the ``Tool`` menu.

[//]: # (Sections to create: Inference algorithms, heuristics)

# Generic functionalities
OpenMarkov also has some functionalities to take into account in every class (they only imply 
modifying other classes):

* [Logger](Logger.md).
* [Error handling](error_handling.md).
* [Testing in OpenMarkov](Testing.md).

[//]: # (Sections to create: Localization)

Using these extension points, you can develop your own plug-ins without modifying any of the 
"official" code. 

If you need any help, contact us through **developers.support@openmarkov.org**. Also please consider
the possibility of contributing your code to OpenMarkov writing to **contributions@openmarkov.org**.