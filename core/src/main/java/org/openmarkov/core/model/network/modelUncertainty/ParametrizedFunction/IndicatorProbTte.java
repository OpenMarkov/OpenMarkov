package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.IndicatorFunction;

/**
 * Parametrization of Indicator
 * FIXME provisional??
 * @author cmyago
 * @version 1 19/03/2023 - only implemented the methods currently needed
 */
@ParametrizedFunctionType(distributionName = "Indicator", parametrizationName = "Prob / TTE", parameters = {"probability", "tte"}, isValidForTTE = true)
public class IndicatorProbTte extends IndicatorFunction {

    /**
     * Default constructor
     */
    public IndicatorProbTte() {
        super(1, 0);
    }


}
