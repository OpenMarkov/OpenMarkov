package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

public class NotEnoughMemoryException extends OpenMarkovException {
    
    public NotEnoughMemoryException(OutOfMemoryError error) {
        this.error = error;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public final OutOfMemoryError error;
}
