package org.openmarkov.learning.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.IBundledOpenMarkovException;

import java.util.List;

/**
 * Thrown when a file format is not supported for learning operations.
 */
public class FormatNotSupportedException extends OpenMarkovException {
    
    public FormatNotSupportedException(String extension, List<String> allowedNetFormats) {
        this.extension = extension;
        this.allowedNetFormats = allowedNetFormats;
    }
    
    public final String extension;
    public final List<String> allowedNetFormats;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
