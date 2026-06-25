package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

//TODO: This should probably be a UnrecheableException instead of being wrapped on it when used.
public class ResourceNotFoundException extends OpenMarkovException {
    public ResourceNotFoundException(String resource) {
        this.resource = resource;
    }
    
    public final String resource;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
