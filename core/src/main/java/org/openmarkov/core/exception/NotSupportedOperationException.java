package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;

/**
 * An operation that is not supported for a certain target. Mostly used when a class has many
 * inheritors and some cannot implement a certain operation that most should.
 *
 * @author jrico
 */
public final class NotSupportedOperationException extends UnsupportedOperationException implements IOpenMarkovException {

    private final StackTraceElement operation;
    private final @Nullable String reason;

    public NotSupportedOperationException(@Nullable String reason) {
        var trace = Thread.currentThread().getStackTrace();
        this.operation = trace[2];
        this.reason = reason;
    }

    public NotSupportedOperationException() {
        var trace = Thread.currentThread().getStackTrace();
        this.operation = trace[2];
        this.reason = null;
    }

    @Override public @Nullable String getExceptionMessage() {
        return getMessage();
    }
    
    @Override public @Nullable String getExceptionTitle() {
        return "Operation not supported";
    }
    
    @Override public String getMessage() {
        String message = "Operation " + this.operation.getMethodName() + " is not supported";
        if (this.reason != null && !this.reason.isBlank()) {
            message += ": " + this.reason;
        }
        return message;
    }

    public StackTraceElement getOperation() {
        return this.operation;
    }

    public @Nullable String getReason() {
        return this.reason;
    }
    
    @Override public String toString() {
        return this.localize();
    }
}
