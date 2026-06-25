#Constraints#

## Use of constraints to control the properties of a network ##

A constraint is basically a condition that a probabilistic network may fulfill or not. 

A probabilistic network has a list of associated constraints. Some of them are **imposed by the 
network type**; for example, a constraint of the network ``type influence diagram`` is that it 
cannot contain cycles; a constraint of ``Bayesian network`` is that it cannot have utility nodes.
There may also be **additional constraints** imposed by the user; for example, the user may decide
that the network only contains finite-states variables (in this context, "finite-states" is almost
synonymous with "categorical" or "discrete", as opposed to "numerical" or "continuous").

One use of constraints is to control the operations that the user can perform in the GUI. For
example, if he/she tries to draw a link that would create a cycle in an influence diagrams, the 
corresponding constraint will veto the action. This behavior guarantees that the network always 
fulfills its associated constraints.

Another use of constraints is to help each inference algorithm decide whether it can evaluate a 
particular network. For example, an algorithm may require that the network contains only 
finite-states variables. In this case, the algorithm can invoke the method checkProbNet(probNet)
of the corresponding constraint. If this constraint is one of the //additional constraints// of the
network, the method returns "true" immediately; else, the constraint must examine the network to 
decide whether all its variables are finite-states or not.

### Implementation ###
Constraints are subclasses of org.openmarkov.core.model.network.constraint.PNConstraint and must
implement the following methods:

* boolean checkProbNet(ProbNet probNet) - returns whether the given ProbNet fulfills the constraint

* boolean checkEdit(ProbNet probNet, PNEdit edit) - returns whether the ProbNet will still fulfill
the constraint after applying the action.

## Relation between constraints and network types ##
Each network type is characterized by a list of the constraints it must fulfill and those it cannot
or need not fulfill. The relation between the different network types and their list of constraints
is defined in an appendix of the [ProbModelXML format's technical report](http://www.cisiad.uned.es/techreports/ProbModelXML.php).

In OpenMarkov, each constraint has a default behavior that specifies whether that constraint will 
**always be applied**, **never**, or **optionally** to all the network types, unless this default
behavior is overridden in the definition of a particular network type. 

This has two advantages. First, each network type does not need to list all the constraints that
define it, but only those whose default behavior must be overridden. For example, the default 
behavior of the constraint ``OnlyDirectedLinks`` is **always**. The only network type that does not
use this constraint is ``MarkovNetwork``, as it is specified in its definition. In general, this 
approach reduces drastically the number of code lines needed to specify the list of constraints 
that define each network type.

The second advantage is the flexibility in the addition of new constraints. For example, let us 
assume that we define a new constraint that applies to all the network types except to a new type we
are going to introduce. Instead of adding the new constraint explicitly in all the previous network
types, if suffices to declare that the default behavior of the new constraint is **always**, but its
behavior for the new network type is **never** or **optional**. 
 
### Implementation ###

The default behavior is specified in the Constraint annotation. For example, the ``OnlyChanceNodes``
declaration is preceded by the following annotation:

```java
@PNConstraint(name = "OnlyChanceNodes", defaultBehavior = ConstraintBehavior.NO)
```

And the constructor of ``BayesianNetworkType`` looks like this:

```java
private BayesianNetworkType () {
    super();
    overrideConstraintBehavior(OnlyChanceNodes.class, ConstraintBehavior.YES);
}
```