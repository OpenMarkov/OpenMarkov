package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

//TODO: This should probably be a UnrecheableException instead of being wrapped on it when used.
public class WrongClassException extends RuntimeException implements IBundledOpenMarkovException {
    public WrongClassException(Class<?> expectedClass, Class<?> foundClass) {
        this.expectedClass = expectedClass;
        this.foundClass = foundClass;
    }
    
    public final Class<?> expectedClass;
    public final Class<?> foundClass;
    
    @Override public String toString() {
        return this.localize();
    }
    
}
