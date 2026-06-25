/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

import org.apache.commons.lang3.ArrayUtils;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.StrategyTree;

import java.util.Arrays;
import java.util.List;

public abstract sealed class CostEffectivenessException extends OpenMarkovException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    //TODO: Does this really happen in the GUI? It might be a RuntimeException.
    //Cost should be one more than thresholds
	public static final class WrongNumberOfThresholds extends CostEffectivenessException{
		
		public WrongNumberOfThresholds(double[] costs, double[] thresholds) {
            this.costs = Arrays.asList(ArrayUtils.toObject(costs));
            this.thresholds = Arrays.asList(ArrayUtils.toObject(thresholds));
        }
        
        public final List<Double> costs;
        public final List<Double> thresholds;
    }
    
    //TODO: Does this really happen in the GUI? It might be a RuntimeException.
	//Their lengths should be the same
	public static final class WrongNumberOfCostsEffectivitiesAndInterventions extends CostEffectivenessException {
		public WrongNumberOfCostsEffectivitiesAndInterventions(double[] costs, double[] effectivities, StrategyTree[] strategyTrees) {
            this.costs = Arrays.asList(ArrayUtils.toObject(costs));
            this.effectivities = Arrays.asList(ArrayUtils.toObject(effectivities));
            this.strategyTrees = List.of(strategyTrees);
        }
        
        public final List<Double> costs;
        public final List<Double> effectivities;
        public final List<StrategyTree> strategyTrees;
    }
    
    //TODO: This exception is only used in a method that is never used.
	public static final class PotentialsMustBeOfSameType extends CostEffectivenessException {
		public PotentialsMustBeOfSameType(Potential firstPotential, Potential secondPotential) {
            this.firstPotential = firstPotential;
            this.secondPotential = secondPotential;
        }
        
        public final Potential firstPotential;
        public final Potential secondPotential;
    }
    
    //TODO: Does this really happen in the GUI? It might be a RuntimeException.
	//Should have the same
	public static final class PartitionsAndProbabilitiesHaveDifferentSizes extends CostEffectivenessException {
		public PartitionsAndProbabilitiesHaveDifferentSizes(List<CEP> partitions, double[] probabilities) {
            this.partitions = partitions;
            this.probabilities = Arrays.asList(ArrayUtils.toObject(probabilities));
        }
        
        public final List<CEP> partitions;
        public final List<Double> probabilities;
    }
}
