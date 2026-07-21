package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.ExponentialFunction;

/**
 * Parametrization of the Exponential distribution which the rate (lambda) as parameter
 *
 * @author cmyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */

@ParametrizedFunctionType(distributionName = "Exponential", parametrizationName = "Lambda", parameters = {"lambda"}, isValidForTTE = true)
public class ExponentialFunctionLambda extends ExponentialFunction {

    /**
     * Default constructor
     */
    public ExponentialFunctionLambda() {
        super(0.0);
    }

    /**
     * Sets the parameters of the probability function. There is only one parameter, the rate lambda
     *
     * @param params - parameters of the probability function [lambda].
     */
    @Override
    public void setParameters(double[] params) {
        super.setParameters(params);
    }


}
