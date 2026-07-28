package org.openmarkov.core.exception;

import org.openmarkov.java.exceptionUtils.ThrowableUtils;

public class UnrecoverableException extends RuntimeException {
    
    public UnrecoverableException(Throwable cause) {
        super(extractUnderlyingCause(cause));
        cause = this.getCause();
        ThrowableUtils.transferStackTrace(this, cause);
    }
    
    private static Throwable extractUnderlyingCause(Throwable cause) {
        // Flattens nestings of THIS type, like its sibling UnreachableException
        // flattens its own. This one unwrapped UnreachableException too - line for
        // line the sibling's loop, a copy-paste - so wrapping an
        // UnrecoverableException in another kept the wrapper as the effective
        // cause instead of the real failure.
        while (cause instanceof UnrecoverableException) {
            cause = cause.getCause();
        }
        return cause;
    }
}
