package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Adapts a Java's {@link Exception} to {@link IOpenMarkovException}.
 * <p>
 * You might get a {@link LocalizedJavaException} using {@link IOpenMarkovException#of(Exception)}.
 */
public final class UnlocalizedJavaException extends Exception implements IOpenMarkovException {
    
    /**
     * Represents the original {@link Exception}.
     */
    private final Exception exception;
    
    /**
     * Constructs an instance of {@code UnlocalizedJavaException} with the provided {@link Exception}.
     *
     * @param exception the original exception to be wrapped and adapted to {@code OpenMarkovException}.
     */
    UnlocalizedJavaException(Exception exception) {
        this.exception = exception;
    }
    
    /**
     * Retrieves the title, which is static for {@link UnlocalizedJavaException}s.
     *
     * @return a string representing the title of the {@link UnlocalizedJavaException}.
     */
    @Override @Nullable public String getExceptionTitle() {
        return StringDatabase.getUniqueInstance().getString("UnlocalizedJavaException.title");
    }
    
    /**
     * Constructs a detailed message of the exception, including a localized header message followed by the Exception's
     * information, such as its name and stack trace.
     *
     * @return a string detailing the original Exception.
     */
    @Override @Nullable public String getExceptionMessage() {
        String indentedStackTrace = Arrays.stream(this.exception.getStackTrace()).map(StackTraceElement::toString)
                                          .filter(s -> !s.isBlank())
                                          .map(s -> "\tat " + s)
                                          .collect(Collectors.joining("\n"));
        
        return StringDatabase.getUniqueInstance().getString("UnlocalizedJavaException.message") + "\n" + this.exception + "\n" + indentedStackTrace;
    }
    
    @Override public String toString() {
        return IOpenMarkovException.toString(this);
    }
    
}
