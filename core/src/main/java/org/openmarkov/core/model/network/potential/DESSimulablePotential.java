package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.EvidenceCase;

/**
 * Interface implemented by Potentials which can be simulated in DES models (DESnets)
 * @author cmyago
 * @version 1.0 2019 - TimeToEvent only for events
 * @version 1.1 cmyago - 05/01/2020
 * @version 2 cmyago - 15/01/2023 refactored TimeToEvent from  to DESSimulablePotential because it is implemented by potentials which can be sampled in DES evaluation.
 * @version 3 cmyago - 24/10/2023 adapted to potentials requiring an indeterminate number of random numbers
 */
public interface DESSimulablePotential {

    // 11/04/2020; 14/08/2022 sampleConditionedVariable(Random randomGenerator, EvidenceCase parents) refactored to  sampleConditionedVariable(double randomNumber, EvidenceCase parents)  for dealing with nuisance variance
    //15/01/2023; moved from Potential

//    /**
//     * Gets a sample of this potential conditioned by its parents the using inverse cumulative distribution method. If this variable is finite-states, it returns the index of
//     * the sampled state. If the variable is numeric, it returns the value sampled.
//     *
//     * @param randomNumber number before 0 an 1 from which inverse cumulative value is computed
//     * @param parents  configuration of parents with their values
//     * @return a sample of the potential when exists; Double.MAX_VALUE otherwise
//     */
//    default double sampleConditionedVariable(double randomNumber, EvidenceCase parents)  {
//        return Double.MAX_VALUE;
//    }

    /**
     * Returns the amount of random numbers required for the potential to get a sample. It is 1 by default
     * @return the number of random numbers required for the potential to get a sample
     */
    default int numRandomNumbersNeeded(){
        return 1;
    }

    /**
     * Gets a sample of this potential conditioned by its parents the using inverse cumulative distribution method. If this variable is finite-states, it returns the index of
     * the sampled state. If the variable is numeric, it returns the value sampled.
     * @param randomNumbers array of numbers before 0 an 1 from which inverse cumulative value is computed
     * @param parents parents configuration with their values
     * @return  a sample of the potential when exists; Double.MAX_VALUE otherwise
     */
    double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException;
//    {
//        return Double.MAX_VALUE;
//    }


    /**
     * Resets the potential for a new simulation
     */
    public default void resetSimulation(){
    }


}
