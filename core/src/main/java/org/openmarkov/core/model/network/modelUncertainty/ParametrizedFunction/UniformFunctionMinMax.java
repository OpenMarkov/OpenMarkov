package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.UniformFunction;

/**
 * Parametrization of the Uniform distribution with between na min and a  max value
 *
 * @author cmyago
 * @version 1 19/01/2025 - only implemented the methods currently needed
 * @see UniformFunction
 */

@ParametrizedFunctionType(distributionName = "Uniform", parametrizationName = "Min/ Max", parameters = {"min", "max"}, isValidForTTE = true)
public class UniformFunctionMinMax extends UniformFunction {

    /**
     * Default constructor
     */
    public UniformFunctionMinMax() {
        super(0.0, 1.0);
    }

    public UniformFunctionMinMax(double min, double max){
        super(min,max);
    }
}
