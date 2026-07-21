package org.openmarkov.sensitivityanalysis.exceptions;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.IBundledOpenMarkovException;

/**
 * Sealed exception hierarchy for errors related to uncertain parameters in
 * sensitivity analysis (e.g. insufficient number of named parameters).
 */
public abstract sealed class UncertainParameterException extends OpenMarkovException {
    
    /** Thrown when the network has fewer named uncertain parameters than required. */
    public static final class FewUncertainParameters extends UncertainParameterException {
        public FewUncertainParameters(int required, int actual) {
            this.required = required;
            this.actual = actual;
        }
        
        public final int required;
        public final int actual;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
