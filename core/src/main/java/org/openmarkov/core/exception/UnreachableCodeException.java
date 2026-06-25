package org.openmarkov.core.exception;

import org.openmarkov.java.exceptionUtils.ThrowableUtils;

/**
 * Exceptions of this class should never happen, but serve to still get a stacktrace in case they do happen due to a
 * programming error.
 * <p>
 * This represent a piece of code that should never be reached.
 *
 * @author jrico
 * @see ThrowableUtils#transferStackTrace(Throwable, Throwable)
 */
public class UnreachableCodeException extends RuntimeException {
    
    public UnreachableCodeException(String message) {
        super(message);
    }
    
    
}
