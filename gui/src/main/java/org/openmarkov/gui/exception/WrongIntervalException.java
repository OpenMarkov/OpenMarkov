package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

public sealed abstract class WrongIntervalException extends OpenMarkovException {
    
    static final public class InfinityInIntervalNotAllowed extends WrongIntervalException {
    }
    
    static final public class LimitsValuesAreWrong extends WrongIntervalException {
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
