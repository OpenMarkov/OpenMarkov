package org.openmarkov.learning.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.IBundledOpenMarkovException;

/**
 * Sealed exception hierarchy for learning workflow errors (e.g. no database loaded, no variables selected).
 */
public abstract sealed class LearningException extends OpenMarkovException {
    
    /** Thrown when the learning workflow is started without a loaded database. */
    public static final class NoDatabasePresent extends LearningException {
    }
    
    /** Thrown when the user has not selected any variables for learning. */
    public static final class NoChosenVariables extends LearningException {
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
