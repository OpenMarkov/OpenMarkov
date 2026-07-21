package org.openmarkov.core.exception;

import org.openmarkov.java.exceptionUtils.ThrowableUtils;

public class UnrecoverableException extends RuntimeException {
    
    public UnrecoverableException(Throwable cause) {
        super(extractUnderlyingCause(cause));
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
