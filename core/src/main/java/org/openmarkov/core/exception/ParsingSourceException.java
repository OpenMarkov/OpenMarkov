package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

//TODO: Catches of this class just show the exception and ignore it, leading to further bugs.
public abstract sealed class ParsingSourceException extends UserInputException {
    
    public static final class CouldNotParseSourceException extends ParsingSourceException {
        public final Exception originException;
        
        public CouldNotParseSourceException(Exception originException) {
            this.originException = originException;
        }
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    
}
