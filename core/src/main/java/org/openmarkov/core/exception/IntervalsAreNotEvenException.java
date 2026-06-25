package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

import java.util.Arrays;
import java.util.List;

public class IntervalsAreNotEvenException extends OpenMarkovException {
    
    public final List<Double> values;
    public final int lenghtOfCycle;
    
    //TODO: Does this really happen in the GUI? It might be a RuntimeException.
    public IntervalsAreNotEvenException(double[] values, int lenghtOfCycle) {
        this.values = Arrays.stream(values).boxed().toList();
        this.lenghtOfCycle = lenghtOfCycle;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
