package org.openmarkov.core.io.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.UserInputException;

public class NoReaderForExtension extends UserInputException {
    public NoReaderForExtension(String extension) {
        this.extension = extension;
    }
    
    public final String extension;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
