#Developing OpenMarkov#

OpenMarkov has a [Working methodology](Working_methodology.md) with good practices for developing
code in OpenMarkov.

#### Extension points:

OpenMarkov has several extension points for adding code easily, and they imply creating specific classes.

* [Menu items](Menu_items.md).
* [Edits](Edits/Edits.md).
* [Constraints](Constraints.md) and network types.
* [Learning algorithms](Learning_algorithms.md).
* [Metrics](Metrics.md) for search-and-score learning algorithms.
* [Localization](Localization%20of%20languages.md) for localizing text to English and Spanish.
[//]: # (Sections to create: Inference algorithms, heuristics)

### Generic functionalities
OpenMarkov also has some functionalities to take into account in every class (they only imply 
modifying other classes):

* [Logger](Logger.md).
* [OpenMarkov exceptions](OpenMarkov_exceptions.md).
* [Testing in OpenMarkov](Testing.md).
[//]: # (Sections to create: Localization)

Using these extension points, you can develop your own plug-ins without modifying any of the 
"official" code. 

If you need any help, contact us through **developers.support@openmarkov.org**. Also please consider
the possibility of contributing your code to OpenMarkov writing to **contributions@openmarkov.org**.