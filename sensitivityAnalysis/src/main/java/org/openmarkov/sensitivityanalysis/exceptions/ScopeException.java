package org.openmarkov.sensitivityanalysis.exceptions;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;

/**
 * Sealed exception hierarchy for the scopes that an analysis cannot have in a network.
 */
public abstract sealed class ScopeException extends OpenMarkovException {

    /** Thrown when the analysis can only be made for a decision and the network has none. */
    public static final class NoDecisionInTheNetwork extends ScopeException {
        public NoDecisionInTheNetwork(String analysis) {
            this.analysis = analysis;
        }

        public final String analysis;
    }

    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
