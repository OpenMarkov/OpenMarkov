package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;

/**
 * Adapts a Java's {@link Exception} to {@link IOpenMarkovException}.
 * <p>
 * You might get a {@link LocalizedJavaException} using {@link IOpenMarkovException#of(Exception)} if said exception is
 * written in an Exception Bundle.
 */
public final class LocalizedJavaException extends Exception implements IOpenMarkovException {
    
    /**
     * The name of the class associated with this exception.
     * <p>
     * It is used to retrieve the localized title and message for this exception from
     * {@link StringDatabase#getUniqueInstance()}.
     */
    private final String className;
    
    /**
     * Constructs a new {@code LocalizedJavaException} with the specified class name.
     *
     * @param className the name of the class associated with this exception
     */
    LocalizedJavaException(String className) {
        this.className = className;
    }
    
    /**
     * Retrieves the localized title associated with this exception.
     * This title is fetched using the class name of this exception as a key postpended with .title and then taken
     * from the {@link StringDatabase#getUniqueInstance()}.
     *
     * @return the localized title as a string, this might be null if the {@link LocalizedJavaException} was constructed
     * manually rather than using {@link IOpenMarkovException#of(Exception)}.
     */
    @Override @Nullable public String getExceptionTitle() {
        return StringDatabase.getUniqueInstance().getNullableString(this.className + ".title");
    }
    
    /**
     * Retrieves the localized message associated with this exception.
     * This message is fetched using the class name of this exception as a key postpended with .message and then taken
     * from the {@link StringDatabase#getUniqueInstance()}.
     *
     * @return the localized message as a string, this might be null if the {@link LocalizedJavaException} was
     * constructed manually rather than using {@link IOpenMarkovException#of(Exception)}.
     */
    @Override @Nullable public String getExceptionMessage() {
        return StringDatabase.getUniqueInstance().getNullableString(this.className + ".message");
    }
    
    @Override public String toString() {
        return IOpenMarkovException.toString(this);
    }
}
