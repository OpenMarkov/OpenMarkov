package org.openmarkov.core.exception;

import org.openmarkov.java.exceptionUtils.ThrowableUtils;

/**
 * Exceptions of this class should never happen, but serve to still get a stacktrace in case they do happen due to a
 * programming error.
 * <p>
 * Calling {@link UnreachableException#getCause()} will return the underlying cause of this exception, and it is always
 * an instance of a class different from {@link UnreachableException}, even if you instantiated
 * {@link UnreachableException} with another {@link UnreachableException}.
 * <p>
 * An instance of {@link UnreachableException} has no stacktrace, as said it is transferred to the underlying cause.
 *
 * @author jrico
 * @see ThrowableUtils#transferStackTrace(Throwable, Throwable)
 */
public class UnreachableException extends RuntimeException {
    
    public UnreachableException(String message) {
        super(message);
    }
    
    public UnreachableException(Throwable cause) {
        super(extractUnderlyingCause(cause));
        cause = this.getCause();
        ThrowableUtils.transferStackTrace(this, cause);
    }
    
    public UnreachableException(String message, Throwable cause) {
        super(message, extractUnderlyingCause(cause));
        cause = this.getCause();
        ThrowableUtils.transferStackTrace(this, cause);
    }
    
    private static Throwable extractUnderlyingCause(Throwable cause) {
        while (cause instanceof UnreachableException) {
            cause = cause.getCause();
        }
        return cause;
    }
    
    
}
