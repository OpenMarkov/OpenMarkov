package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;

public class InvalidLimitInTreeADDException extends OpenMarkovException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public InvalidLimitInTreeADDException(Threshold interval, float introducedLimit) {
        this.interval = interval;
        this.introducedLimit = introducedLimit;
    }
    
    public final Threshold interval;
    public final float introducedLimit;
}
