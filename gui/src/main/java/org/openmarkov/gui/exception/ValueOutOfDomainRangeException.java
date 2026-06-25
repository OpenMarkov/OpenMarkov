package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.PartitionedInterval;

public class ValueOutOfDomainRangeException extends OpenMarkovException {
    
    public ValueOutOfDomainRangeException(double numericValue, PartitionedInterval domain) {
        this.numericValue = numericValue;
        this.domain = domain;
    }
    
    public final double numericValue;
    public final PartitionedInterval domain;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
