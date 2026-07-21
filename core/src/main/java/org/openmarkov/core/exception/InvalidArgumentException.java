package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;

public class InvalidArgumentException extends IllegalArgumentException implements IOpenMarkovException {

    public final boolean valueIsSet;
    public final @Nullable Object value;
    public final @Nullable String argumentName;
    public final String reason;

    public InvalidArgumentException(@Nullable Object value, @Nullable String argumentName, String reason) {
        super(buildMessage(value, argumentName, reason));
        this.valueIsSet = true;
        this.value = value;
        this.argumentName = argumentName;
        this.reason = reason;
    }

    public InvalidArgumentException(@Nullable String argumentName, String reason) {
        super(buildMessage(null, argumentName, reason));
        this.valueIsSet = false;
        this.value = null;
        this.argumentName = argumentName;
        this.reason = reason;
    }

    public InvalidArgumentException(@Nullable Object value, String reason) {
        super(buildMessage(value, null, reason));
        this.valueIsSet = true;
        this.value = value;
        this.argumentName = null;
        this.reason = reason;
    }

    public InvalidArgumentException(String reason) {
        super(reason);
        this.valueIsSet = false;
        this.value = null;
        this.argumentName = null;
        this.reason = reason;
    }

    @Override public @Nullable String getExceptionMessage() {
        return getMessage();
    }
    
    @Override public @Nullable String getExceptionTitle() {
        return "Argument is invalid";
    }
    
    private static String buildMessage(@Nullable Object value, @Nullable String argumentName, String reason) {
        String valuePart = value != null ? "(" + value + ") " : "";
        String namePart = argumentName != null ? argumentName + " " : "";
        return "Argument " + namePart + valuePart + "is invalid because " + reason + ".";
    }
    
    @Override public String toString() {
        return this.localize();
    }
}
