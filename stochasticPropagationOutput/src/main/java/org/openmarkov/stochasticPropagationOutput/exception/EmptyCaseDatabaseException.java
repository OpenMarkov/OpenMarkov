package org.openmarkov.stochasticPropagationOutput.exception;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.inference.algorithm.likelihoodWeighting.StochasticPropagation;

/**
 * Exception thrown when a stochastic propagation algorithm produces no samples
 * (i.e., the case database is empty).
 */
public class EmptyCaseDatabaseException extends OpenMarkovException {

    /**
     * Creates an exception referencing the algorithm that produced no samples.
     *
     * @param algorithm the stochastic propagation algorithm that failed to produce samples
     */
    public EmptyCaseDatabaseException(StochasticPropagation algorithm) {
        this.algorithm = algorithm;
    }
    
    private final StochasticPropagation algorithm;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
