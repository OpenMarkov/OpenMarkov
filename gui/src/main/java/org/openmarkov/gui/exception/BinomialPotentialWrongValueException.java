package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

public abstract sealed class BinomialPotentialWrongValueException extends OpenMarkovException {
    
    public final static class NValuesIsWrong extends BinomialPotentialWrongValueException {
        
        public NValuesIsWrong(int nValue) {
            this.nValue = nValue;
        }
        
        public final int nValue;
    }
    
    public final static class ThetaValueIsWrong extends BinomialPotentialWrongValueException {
        public ThetaValueIsWrong(double thetaValue) {
            this.thetaValue = thetaValue;
        }
        
        public final double thetaValue;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
