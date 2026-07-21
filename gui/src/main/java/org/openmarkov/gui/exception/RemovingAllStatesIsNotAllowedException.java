package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

public class RemovingAllStatesIsNotAllowedException extends OpenMarkovException {
    
    public RemovingAllStatesIsNotAllowedException(TreeADDPotential parentTreeADD) {
        this.parentTreeADD = parentTreeADD;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public final TreeADDPotential parentTreeADD;
}
