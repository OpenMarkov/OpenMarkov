package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import org.openmarkov.core.model.network.modelUncertainty.ExactFunctionDES;

/**
 * Parametrization of the Exact distribution which nu as a parameter
 *
 * @author cmyago
 * @version 2 20/08/2022 -  adapting to nuisance variance by inheriting from ProbDensFunctionWithKnownInverseCDF in order to be working with random numbers instead of Random or RandomStream (related to change in signature)
 */

@ParametrizedFunctionType(distributionName = "Exact", parametrizationName = "Nu", parameters = {"nu"}, isValidForTTE = true)
public class ExactFunctionNu extends ExactFunctionDES {

    /**
     * Default constructor
     */
    public ExactFunctionNu() {
        super(0.0);
    }

    /**
     * Sets the parameters of the probability function
     *
     * @param params - parameters of the probability function
     */
    @Override
    public void setParameters(double[] params) {
        super.setParameters(params);
    }

    
    

}
