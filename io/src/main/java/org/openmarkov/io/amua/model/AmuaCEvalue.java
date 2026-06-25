package org.openmarkov.io.amua.model;

/**
 * Encapsulates the cost and effectiveness values for a node in a
 * Cost-Effectiveness (CE) decision tree.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public class AmuaCEvalue {

    private double cost;
    private double effectiveness;

    public AmuaCEvalue(double cost, double effectiveness) {
        this.cost = cost;
        this.effectiveness = effectiveness;
    }

    public double getCost() { return
        cost;
    }

    public double getEffectiveness() {
        return effectiveness;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setEffectiveness(double effectiveness) {
        this.effectiveness = effectiveness;
    }
}