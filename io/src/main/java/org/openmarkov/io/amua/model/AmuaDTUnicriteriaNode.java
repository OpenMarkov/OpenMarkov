package org.openmarkov.io.amua.model;

/**
 * Node for a unicriteria decision tree.
 */

public class AmuaDTUnicriteriaNode extends AmuaDTNode<Double> {

    public AmuaDTUnicriteriaNode() {
        this.cost = 0.0;
        this.payoff = 0.0;
        this.partialUtility = 0.0;
    }
}