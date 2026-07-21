package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.WeibullFunction;

/**
 * Parametrization of the Weibull  distribution which scale =lambda and shape= K
 * Here scale = lambda , and shape = k
 * Where f(x)= shape*scale^(-shape)*x^(shape-1)*exp(-(x/scale)^shape), F(x)=1-exp(-(x/scale)^shape), and S(x) = exp(-(x/scale)^shape)
 * That is f(x)= k*lambda^(-k)*x^(k-1)*exp(-(x/lambda)^k), F(x)=1-exp(-(x/lambda)^k), and S(x) = exp(-(x/lambda)^k)
 * @author cmyago
 * @version 1 20/10/2020 - only implemented the methods currently needed
 */
@ParametrizedFunctionType(distributionName = "Weibull", parametrizationName = "Scale (lambda)/ Shape", parameters = {"lambda", "k"}, isValidForTTE = true)
public class WeibullFunctionScaleShape extends WeibullFunction {

    /**
     * Default constructor
     */
    public WeibullFunctionScaleShape() {
        super(0.0, 0);
    }


//	/**
//	 * Sets the parameters of the probability function. There are two parameters lambda and k.
//	 *
//	 * @param params - parameters of the probability function [lambda,k].
//	 */
//	@Override
//	public void setParameters(double[] params) {
//		super.setParameters(params);
//	}


}
