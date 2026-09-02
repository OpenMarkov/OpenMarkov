package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.OpenMarkovException;

public class WrongClassException extends OpenMarkovException {
    public WrongClassException(Class<?> expectedClass, Class<?> foundClass) {
        super();
        this.expectedClass = expectedClass;
        this.foundClass = foundClass;
    }
    
    public final Class<?> expectedClass;
    public final Class<?> foundClass;
    
    @Override public String toString() {
        return this.localize();
    }
    
}
