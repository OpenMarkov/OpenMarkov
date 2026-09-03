package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;

public class ThereMustBeJustOneEventInConfiguration extends OpenMarkovException {
    
    public final TreeWithEventsPotential treeWithEventsPotential;
    public final int eventsAmount;
    
    public ThereMustBeJustOneEventInConfiguration(TreeWithEventsPotential treeWithEventsPotential, int eventsAmount) {
        this.treeWithEventsPotential = treeWithEventsPotential;
        this.eventsAmount = eventsAmount;
    }
    
    
}
