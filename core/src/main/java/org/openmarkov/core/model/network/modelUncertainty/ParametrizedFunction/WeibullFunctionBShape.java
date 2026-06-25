package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.WeibullFunction;

/**
 * Parametrization of the Weibull  distribution which scale = b = lambda^(-k) and shape= k
 * f(x)= k*b*x^(k-1)*exp(-b*x^k), F(x)=1-exp(-b*x^k), and S(x) = exp(-b*x^k)
 * That is f(x)= k*lambda^(-k)*x^(k-1)*exp(-(x/lambda)^k), F(x)=1-exp(-(x/lambda)^k), and S(x) = exp(-(x/lambda)^k)
 *
 * @author cmyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 * @see WeibullFunction
 */

@ParametrizedFunctionType(distributionName = "Weibull", parametrizationName = "Scale (b)/ Shape", parameters = {"b", "k"}, isValidForTTE = true)
public class WeibullFunctionBShape extends WeibullFunction {

    /**
     * Default constructor
     */
    public WeibullFunctionBShape() {
        super(0.0, 0);
    }


    /**
     * Sets the parameters of the probability function. There are two parameters b and lambda.
     * b = lambda^-k ; lambda = b^k
     *
     * @param params - parameters of the probability function [b, lambda].
     */
    @Override
    public void setParameters(double[] params) {
        params[0] = Math.pow(1 / params[0], 1 / params[1]);
        super.setParameters(params);
    }


}
