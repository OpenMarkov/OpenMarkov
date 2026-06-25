This article guides the developer in the task of implementing a new metric for the hillclimbing algorithm family. The hillclimbing algorithm is in fact a family of algorithms, each of the members being based on a different metric that assigns a score to each possible atomic modification to the Bayesian network being learned. The algorithm performs a greedy search for the optimal graph, applying on each iteration the modification with the highest score. 

Note: if you are new to OpenMarkov development, please make sure you have your development environment set up by [installing the necessary software](https://bitbucket.org/cisiad/org.openmarkov/wiki/Install_Eclipse,_Maven,_and_Mercurial) and [downloading OpenMarkov's main project](https://bitbucket.org/cisiad/org.openmarkov/wiki/First_download).

## Introductory note on metrics in OpenMarkov ##

The metrics are meant to return a score for each node, given a set of parents, taking into account the conditional frequencies of the child given the parents. These scores are then used by the score-and-search algorithms to select the combinations of child-parents with the highest scores. 

## Set up ##

Create a maven project in Eclipse and make it depend on org.openmarkov.learning.searchAndScore editing its pom.xml file.

## Integrate the new metric in OpenMarkov ##
Metrics are another type of extension point of OpenMarkov. As such,  OpenMarkov detects the set of available metric implementations on runtime, looking for classes and jar files in its classpath. Therefore, there are two ways you can make sure OpenMarkov detects your new learning algorithm. During the development phase, add a dependency in org.openmarkov.full's pom.xml to the maven project where the algorithm is implemented. Once the learning algorithm is implemented, pack it to a jar and include it in the same folder as Openmarkov's main jar. 
If the metric has been correctly identified by OpenMarkov, it will appear in the list of available metrics in the corresponding combo box in the parameters' form for the Hill climbing algorithm.

## Implement a subclass of Metric ##

All metrics must be implemented as a subclass of org.openmarkov.learning.algorithm.scoreAndSearch.metric.Metric and be decorated it with the MetricType annotation, which consists only of a member of type string, representing the name of the metric.
Here is an example of the decoration of the BayesianMetric class with the MetricType annotation:

```
#!java

@MetricType(name = "Bayesian")
public class BayesianMetric extends Metric
{
```
For the time being, there is a limitation on the parameters received by the constructors of the subclasses of Metric. These can either receive no parameters or receive one of type double. This is necessary in order to be able to create instances of the metrics dynamically.

There is only one method that a subclass of Metric has to implement, and that is **score(TablePotential)**. This method is meant to return a double representing the score assigned by the metric to a particular node given its parents. The TablePotential contains the frequencies of the values of the child node for the different combinations of the values of the parents.