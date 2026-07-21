package org.openmarkov.bnEvaluation.exceptions;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.ProbNet;

/**
 * Thrown when a network cannot be evaluated because it is not a Bayesian network
 * with defined potentials.
 */
public class NetworkIsNotEvaluable extends OpenMarkovException {
    
    public final ProbNet networkToEvaluate;

    // Only Bayesian networks with potentials can be evaluated, but network is {networkToEvaluate}.
    public NetworkIsNotEvaluable(ProbNet networkToEvaluate) {
        this.networkToEvaluate = networkToEvaluate;
    }
    
    @Override public String toString() {
        return super.toString();
    }
    
    
}
