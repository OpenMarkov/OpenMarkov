package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.ExponentialFunction;

/**
 * Parametrization of the Exponential distribution which the transition probability as parameter
 *
 * @author cmyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */
@ParametrizedFunctionType(distributionName = "Exponential", parametrizationName = "Probability per time unit", parameters = {"probability"}, isValidForTTE = true)
public class ExponentialFunctionProbability extends ExponentialFunction {

    /**
     * Constructor
     */
    public ExponentialFunctionProbability() {
        super(0.0);
    }

    /**
     * Sets the parameters of the probability function. There is only one parameter, the transition probability
     * Transition probability p=1-e^(-lambda) ; lambda =-LN(1 -p)
     *
     * @param params - parameters of the probability function [probability].
     */
    @Override
    public void setParameters(double[] params) {
        params[0] = -Math.log(1 - params[0]);
        super.setParameters(params);
    }

}
