package org.openmarkov.io.amua.model;

/**
 * Node for a Cost-Effectiveness (CE) decision tree.
 */

public class AmuaDTCENode extends AmuaDTNode<AmuaCEvalue> {
    public AmuaDTCENode() {
        this.cost = new AmuaCEvalue(0, 0);
        this.payoff = new AmuaCEvalue(0, 0);
        this.partialUtility = new AmuaCEvalue(0, 0);
    }
}