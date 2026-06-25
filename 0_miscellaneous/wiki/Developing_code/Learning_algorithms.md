This article guides the developer in the task of implementing a new learning algorithm for OpenMarkov.

Note: if you are new to OpenMarkov development, please make sure you have your development environment set up by [installing the necessary software](https://bitbucket.org/cisiad/org.openmarkov/wiki/Install_Eclipse,_Maven,_and_Mercurial) and [downloading OpenMarkov's main project](https://bitbucket.org/cisiad/org.openmarkov/wiki/First_download).

## Introductory note on learning algorithms in OpenMarkov ##

There are two separate aspects of learning Bayesian networks. One is *structural learning* and consists in finding the graph that better represents the independence relations implicit in the database. The other is `parametric learning` and consists in finding the set of conditional probabilities that better fits the database we are learning from. 

Structural learning consists therefore in the construction of the optimal graph, be it from scratch or from a given graph. As most learning algorithms do, OpenMarkov iteratively builds that graph applying a change to it in each iteration. We call these changes "edits"; each edit usually involves the addition, removal or inversion of an arc. It is worth noting that some edits are not allowed given a certain network: for example, OpenMarkov will veto an edit consisting of a link addition that would create a cycle. OpenMarkov has a set of so called [constraints](Constraints.md) implemented that prevent such situations from happening. These will be relevant for the behavior of the learning algorithm.

OpenMarkov has currently two learning algorithms implemented: the **PC algorithm**, which is based on the detection of conditional independences, and the **hill climbing algorithm**, of the search-and-score family. The latter is in fact a family of algorithms, each member being based on a different metric that assigns a score to each possible atomic modification of the Bayesian network being learned. The algorithm performs a greedy search for the optimal graph, applying in each iteration the modification with the highest score. There is another article in this wiki explaining [how to implement a metric](Metrics.md#implement-a-subclass-of-metric-).

Follow these steps in order to implement a learning algorithm for OpenMarkov:

## Set up##

Create a maven project in Eclipse and make it depend on `org.openmarkov.learning.core` editing its `pom.xml` file.

## Integrate the new algorithm in OpenMarkov ##
OpenMarkov detects the set of available learning algorithm implementations at runtime, looking for classes and jar files in its classpath. Therefore, there are two ways you can make sure OpenMarkov detects your new learning algorithm. During the development phase, add a dependency in the `pom.xml`  of `org.openmarkov.full` to the maven project where the algorithm is implemented. Once the learning algorithm is implemented, pack it to a jar file and include it in the same folder as Openmarkov's main jar.
If the algorithm has been correctly detected, it will appear in the list of available algorithms in the corresponding combo box in the Learning dialog, inside the General tab.

## Implement a subclass of `LearningAlgorithm` ##
In order to be detected by OpenMarkov as a learning algorithm, the class must be decorated with an **annotation** of type `LearningAlgorithmType`. This annotation has two components: a string representing the name of the algorithm and a boolean specifying whether the algorithm supports missing values (as, for example, the Expectation Maximization algorithm does).

Example: decoration of the `HillClimbingAlgorithm` class:
```
#!java

@LearningAlgorithmType (name = "Hill climbing", supportsLatentVariables = false)
public class HillClimbingAlgorithm extends ScoreAndSearchAlgorithm{	


```

The `LearningAlgorithm` class has two methods for obtaining the edits:

* `getBestEdit`: This method is responsible of finding and returning at each moment the best edit and a justification of why it has been selected. Thus, `getBestEdit` should return the edit with the highest `LearningEditMotivation`, while other methods return edits with lower-ranked motivations.


* `getNextEdit`: It is similar to `getBestEdit`, but returns the next best edit, taking into account the edits served in the previous calls of these two methods.

If the new algorithm is to be used for automatic learning, it suffices to implement `getBestEdit`, but interactive learning also needs the method `getNextEdit` in order to compose the list of proposed edits.

Each of these methods returns an object of type `LearningEditProposal`, which basically consists of a `PNEdit` and a `LearningEditMotivation`. The `LearningEditMotivation` represents the reason for applying the edit. It is specific of the algorithm. For example, in the case of the hill climbing algorithm, the `LearningEditMotivation` is the score assigned by the metric. In the PC algorithm, the motivation may be the list of variables involved in the statistical test and the *p value*. The `LearningEditMotivation` class implements `Comparable` so that edits can be ranked.  

Each of these methods receives two boolean parameters: `onlyAllowedEdits` and `onlyPositiveEdits`. When the former is `true`, the method returns an edit only if there exists one compatible with the [constraints](Constraints.md) of the network; if there is none, it returns null. When this parameter is `false`, the method may return an incompatible edit so that the user can see it, but if the algorithm or the user tries to execute it, the constraint will veto it.

When using a metric, if the parameter `onlyPositiveEdits` is `true`, the method returns an edit only if there exists one having a positive score; if there is none, it returns null. When this parameter is `false`, the method may return an edit with a negative score.

Other methods that can be implemented to override their default behavior are:

* **init**: The purpose of this callback is to give the algorithm the chance to be initialized before starting with the actual execution of the algorithm.

### Parametric learning ###
Parametric learning (as opposed to structural learning, which deals with the graph's structure), consists in the computation of the set of conditional probabilities that define the model. The default implementation of parametric learning uses a Laplace-like correction depending on a parameter called alpha. When alpha = 0, then it amounts to computing the parameter having the maximum likelihood. When alpha = 1, we have the Laplace correction. This behavior can be changed by overriding the LearningAlgorithm's `parametricLearning()` method.

### Multi-phase learning algorithms ###
Some algorithms have more than one phases. For example, the first phase of the PC algorithm removes some links based on the conditional independences it detects, the second one orients some pairs of links head-to-head, and the third one orients the rest of the links. `LearningAlgorithm` has an integer attribute, `phase`, and its subclasses are responsible of updating this attribute according to the current state of the algorithm. Following with the PC algorithm's example, when there are no more links to remove, the  value of `phase` is increased from 0 to 1. This is useful for interactive learning, where the user can choose to run the algorithm until the the end of the current phase.

## Using your algorithm from OpenMarkov's GUI ##

If you want to use your algorithm from OpenMarkov's GUI (for example, in order to use OpenMarkov's interactive learning functionality or data preprocessing options), you have to implement a subclass of `AlgorithmParametersDialog`, which will gather the parameters specific of the learning algorithm (for example, the metric of a hill climbing method or the significance threshold of the PC algorithm). The `getInstance` method of this dialog will create an instance of the subclass of `LearningAlgorithm` that you have implemented.


## Using your algorithm from OpenMarkov's API ##
OpenMarkov's API àllows the user to interact with `LearningManager`, which offers almost the same functionality as the GUI but programmatically: use the model network, run the algorithm automactically or step by step, etc.

# Developer support #
If you need any help, write to [developers.support@openmarkov.org](mailto:developers.support@openmarkov.org.remove.this).