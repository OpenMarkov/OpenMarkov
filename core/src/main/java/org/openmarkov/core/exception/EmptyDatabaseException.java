package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

public class EmptyDatabaseException extends UserInputException {
    public final String source;
    
    public EmptyDatabaseException(String source) {
        this.source = source;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
