/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.exception.CostEffectivenessException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.potential.StrategyTree;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A CEP is a set of <b>n</b> intervals, each one with a cost, an effectiveness and possibly, an intervention.
 * Intervals are separated by <b>n-1</b> thresholds. The whole partition is delimited by the minimum and the maximum
 * threshold.
 *
 * @author Manuel Arias
 */
public class CEP implements Cloneable {
    
    public static final class CEPBuilder {
        
        private double minThreshold = CEP.DEFAULT_MINIMAL_THRESHOLD;
        private double maxThreshold = CEP.DEFAULT_MAXIMAL_THRESHOLD;
        
        private final ArrayList<StrategyTree> strategyTrees = new ArrayList<>();
        private final ArrayList<Double> costs = new ArrayList<>();
        private final ArrayList<Double> effectiveness = new ArrayList<>();
        private final ArrayList<Double> thresholds = new ArrayList<>();
        
        public CEPBuilder thresholdBounds(double minThreshold, double maxThreshold) {
            this.minThreshold = minThreshold;
            this.maxThreshold = maxThreshold;
            return this;
        }
        
        public CEPBuilder addRow(StrategyTree strategyTree, double cost, double effectivity, double thresholds) {
            this.strategyTrees.add(strategyTree);
            this.costs.add(cost);
            this.effectiveness.add(effectivity);
            this.thresholds.add(thresholds);
            return this;
        }
        
        public CEP build(StrategyTree strategyTree, double cost, double effectivity) {
            this.strategyTrees.add(strategyTree);
            this.costs.add(cost);
            this.effectiveness.add(effectivity);
            try {
                return new CEP(
                        this.strategyTrees.toArray(StrategyTree[]::new),
                        this.costs.stream().mapToDouble(Double::doubleValue).toArray(),
                        this.effectiveness.stream().mapToDouble(Double::doubleValue).toArray(),
                        this.thresholds.stream().mapToDouble(Double::doubleValue).toArray(),
                        this.minThreshold,
                        this.maxThreshold
                );
            } catch (CostEffectivenessException.WrongNumberOfThresholds |
                     CostEffectivenessException.WrongNumberOfCostsEffectivitiesAndInterventions e) {
                throw new UnreachableException(e);
            }
        }
    }
    
    /**
     * Used to save memory and time in case of partitions corresponding to configurations with zero probability.
     */
    private static final CEP ZERO_PARTITION = new CEP();
    private static final double DEFAULT_MINIMAL_THRESHOLD = 0.0;
    private static final double DEFAULT_MAXIMAL_THRESHOLD = Double.POSITIVE_INFINITY;
    final DecimalFormat decimalFormat3afterComa = new DecimalFormat("#.###");
    final DecimalFormat decimalFormat2afterComa = new DecimalFormat("#.##");
    final DecimalFormat decimalFormat1afterComa = new DecimalFormat("#.#");
    final DecimalFormat decimalFormatNoDecimalsAfterComa = new DecimalFormat("#");
    
    // Attributes
    private double[] costs;
    private double[] effectiveness;
    /**
     * An intervention is a potential. If it is a decision, its value is a {@code DeltaPotential}, otherwise,
     * a {@code TreeADDPotential}
     */
    private StrategyTree[] strategyTrees;
    
    // Constructors
    /**
     * Divisions between intervals.
     */
    private double[] thresholds;
    
    public void setMinThreshold(double minThreshold) {
        this.minThreshold = minThreshold;
    }
    
    public void setMaxThreshold(double maxThreshold) {
        this.maxThreshold = maxThreshold;
    }
    
    private double minThreshold;
    private double maxThreshold;
    /**
     * When true, this partition must not be taken in consideration.
     */
    private boolean zeroProbability;
    
    // Methods
    // From this point: set of attributes and methods related with method toString()
    private String indent = "";
    private int indentLevel; // TODO Remove?
    
    /**
     * @param strategyTrees {@code Potential[]}
     * @param costs         {@code double[]}
     * @param effectiveness {@code double[]}
     * @param thresholds    {@code double[]}
     * @param minThreshold  {@code double}
     * @param maxThreshold  {@code double}
     *
     */
    public CEP(
            StrategyTree[] strategyTrees, double[] costs, double[] effectiveness, double[] thresholds,
            double minThreshold, double maxThreshold)
            throws CostEffectivenessException.WrongNumberOfThresholds, CostEffectivenessException.WrongNumberOfCostsEffectivitiesAndInterventions {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        if (!(costs.length == effectiveness.length && costs.length == strategyTrees.length)) {
            throw new CostEffectivenessException.WrongNumberOfCostsEffectivitiesAndInterventions(costs, effectiveness, strategyTrees);
        }
        if (!(costs.length == 1 && thresholds == null) && (thresholds.length != (costs.length - 1))) {
            throw new CostEffectivenessException.WrongNumberOfThresholds(costs, thresholds);
        }
        if (thresholds == null) {
            thresholds = new double[0];
        }
        this.thresholds = thresholds;
        this.costs = costs;
        this.effectiveness = effectiveness;
        this.strategyTrees = strategyTrees;
    }
    
    /**
     * Creates a CEPartition with zero probability
     */
    private CEP() {
        zeroProbability = true;
    }
    
    /**
     * Singleton for partitions with probability zero
     *
     * @return CEPartition
     */
    public static synchronized CEP getZeroPartition() {
        return ZERO_PARTITION;
    }
    
    /**
     * @param lambda {@code double}
     *
     * @return Interval index corresponding to lambda. {@code int}
     */
    public int index(double lambda) {
        int numThresholds = costs.length - 1;
        int i = 0;
        while (i < numThresholds && lambda > thresholds[i])
            i++;
        return i;
    }
    
    /**
     * Multiplies costs and effectiveness per factor.
     *
     * @param factor {@code double}
     */
    public void multiply(double factor) {
        if (!zeroProbability) {
            for (int i = 0; i < costs.length; i++) {
                costs[i] *= factor;
                effectiveness[i] *= factor;
            }
        }
    }
    
    /**
     * Divides costs and effectiveness per factor.
     *
     * @param factor {@code double}
     */
    public void divide(double factor) {
        if (!zeroProbability) {
            for (int i = 0; i < costs.length; i++) {
                costs[i] /= factor;
                effectiveness[i] /= factor;
            }
        }
    }
    
    /**
     * Change the indentation in {@code toString()}. Used for nested interventions.
     *
     * @param indentLevel {@code int}
     */
    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
        if (indentLevel == 0) {
            indent = "";
        } else {
            indent = " ";
            for (int i = 1; i < indentLevel; i++)
                indent = indent + " ";
        }
    }
    
    /**
     * @return The intervention corresponding to this CEP with a discretized continuous variable on root called lambda
     */
    public StrategyTree getIntervention() {
        Variable lambda = getLambda();
        return new StrategyTree(lambda, getListOfStates(lambda), getListOfInterventions());
    }
    
    public int getNumIntervals() {
        return strategyTrees.length;
    }
    
    public boolean hasStrategyTrees(){
        return strategyTrees != null && strategyTrees.length > 0;
    }
    
    /**
     * @param lambda {@code double}
     *
     * @return Cost corresponding to lambda. {@code double}
     */
    public double getCost(double lambda) {
        return costs[index(lambda)];
    }
    
    /**
     * @param interval {@code int}
     *
     * @return Cost corresponding to interval. {@code double}
     */
    public double getCost(int interval) {
        return costs[interval];
    }
    
    /**
     * @param lambda {@code double}
     *
     * @return Effectiveness corresponding to lambda. {@code double}
     */
    public double getEffectiveness(double lambda) {
        return effectiveness[index(lambda)];
    }
    
    /**
     * @param interval {@code int}
     *
     * @return Effectiveness corresponding to interval. {@code double}
     */
    public double getEffectiveness(int interval) {
        return effectiveness[interval];
    }
    
    /**
     * @param lambda {@code double}
     *
     * @return TreeADDPotential corresponding to lambda. {@code TreeADDPotential}
     */
    public StrategyTree getIntervention(double lambda) {
        return strategyTrees[index(lambda)];
    }
    
    /**
     * @param interval {@code int}
     *
     * @return TreeADDPotential corresponding to interval. {@code TreeADDPotential}
     */
    public StrategyTree getIntervention(int interval) {
        return strategyTrees[interval];
    }
    
    /**
     * @return All the interventions. {@code Intervention[]}
     */
    public StrategyTree[] getStrategyTrees() {
        return strategyTrees;
    }
    
    /**
     * @return {@code boolean}
     */
    public boolean isZero() {
        return zeroProbability;
    }
    
    public void setZero() {
        zeroProbability = true;
    }
    
    /**
     * @param interval {@code int}
     *
     * @return threshold between interval and interval + 1. {@code double}
     */
    public double getThreshold(int interval) {
        return thresholds[interval];
    }
    
    /**
     * @return {@code double[]}
     */
    public double[] getThresholds() {
        return thresholds;
    }
    
    /**
     * @param cost     {@code double}
     * @param interval {@code int}
     */
    public void setCost(double cost, int interval) {
        costs[interval] = cost;
    }
    
    /**
     * @param eff      {@code double}
     * @param interval {@code int}
     */
    public void setEffectiveness(double eff, int interval) {
        effectiveness[interval] = eff;
    }
    
    /**
     * @return minThreshold. {@code double}
     */
    public double getMinThreshold() {
        return minThreshold;
    }
    
    /**
     * @return maxThreshold. {@code double}
     */
    public double getMaxThreshold() {
        return maxThreshold;
    }
    
    /**
     * @return effectiveness. {@code double[]}
     */
    public double[] getEffectivities() {
        return effectiveness;
    }
    
    /**
     * @return costs. {@code double[]}
     */
    public double[] getCosts() {
        return costs;
    }
    
    private List<StrategyTree> getListOfInterventions() {
        List<StrategyTree> listOfStrategyTrees = new ArrayList<StrategyTree>(strategyTrees.length);
        Collections.addAll(listOfStrategyTrees, this.strategyTrees);
        return listOfStrategyTrees;
    }
    
    private static List<State> getListOfStates(Variable variable) {
        State[] statesVariable = variable.getStates();
        int numStates = statesVariable.length;
        List<State> listOfStates = new ArrayList<State>(numStates);
        listOfStates.addAll(Arrays.asList(statesVariable).subList(0, numStates));
        return listOfStates;
    }
    
    private Variable getLambda() {
        State[] states = new State[strategyTrees.length];
        double[] limits = new double[strategyTrees.length + 1];
        limits[0] = minThreshold;
        boolean[] belongsToLeftSide = new boolean[strategyTrees.length + 2];
        belongsToLeftSide[belongsToLeftSide.length - 1] = false;
        belongsToLeftSide[0] = true;
        // build state names: state[i] = lambda in (min, max)
        for (int i = 0; i < states.length; i++) {
            if (i < thresholds.length) {
                limits[i + 1] = thresholds[i];
            }
            belongsToLeftSide[i + 1] = true;
            String stateName = "\u03BB in [";
            if (i == 0) {
                stateName = stateName + minThreshold;
            } else {
                stateName = stateName + thresholds[i - 1];
            }
            stateName = stateName + " , ";
            if (i == states.length - 1) {
                stateName = stateName + maxThreshold;
            } else {
                stateName = stateName + thresholds[i];
            }
            stateName = stateName + ") Cost = " + costs[i] + " Effectiveness = " + effectiveness[i];
            states[i] = new State(stateName);
        }
        limits[limits.length - 1] = maxThreshold;
        return new Variable("lambda", states, new PartitionedInterval(limits, belongsToLeftSide), 0.1);
    }
    
    public String toString() {
        if (zeroProbability) {
            return indent + "CEP with probability zero.\n";
        }
        String asString = indent + "Number of intervals: " + (thresholds.length + 1);
        for (int i = 0; i < thresholds.length + 1; i++) {
            asString += "\nInterval " + i + ":\n" + indent + "lambda in (";
            if (i == 0) {
                asString += formatNum(minThreshold);
            } else {
                asString += formatNum(thresholds[i - 1]);
            }
            asString += " and ";
            if (i == thresholds.length) {
                if (maxThreshold == Double.POSITIVE_INFINITY || maxThreshold > 1.0E300) {
                    asString += "+Infinity";
                } else {
                    asString += maxThreshold;
                }
            } else {
                asString += formatNum(thresholds[i]);
            }
            asString += ") Cost: " + formatNum(costs[i]) + "   Eff: " + formatNum(effectiveness[i]) + "\n" + indent + "optimal intervention:";
            if (strategyTrees[i] != null) {
                strategyTrees[i].setIndentLevel(indentLevel + 2);
                asString += ("\n");
            } else {
                asString += (" ");
            }
            asString += strategyTrees[i];
        }
        return asString;
    }
    
    private String formatNum(double number) {
        if (Math.abs(number) < 10.0) {
            return decimalFormat3afterComa.format(number);
        }
        if (Math.abs(number) < 100.0) {
            return decimalFormat2afterComa.format(number);
        }
        if (Math.abs(number) < 1000.0) {
            return decimalFormat1afterComa.format(number);
        }
        return decimalFormatNoDecimalsAfterComa.format(number);
    }
    
    /**
     * Calculate net monetary benefit given the willigness to pay (lambda)
     *
     * @param lambda {@code double}
     *
     * @return net monetary benefit given lambda
     */
    public double getNetMonetaryBenefit(double lambda) {
        return getEffectiveness(lambda) * lambda - getCost(lambda);
    }
    
    public boolean equals(CEP cep) {
        if (cep != null) {
            boolean areEquals = Arrays.equals(this.thresholds, cep.thresholds) && Arrays.equals(this.costs, cep.costs) && Arrays
                    .equals(this.effectiveness, cep.effectiveness);
            return areEquals;
        }
        return false;
    }
    
    @Override public CEP clone() {
        var newCEP = new CEP();
        newCEP.costs = Arrays.stream(this.costs).toArray();
        newCEP.effectiveness = Arrays.stream(this.effectiveness).toArray();
        newCEP.strategyTrees = Arrays.stream(this.strategyTrees).toArray(StrategyTree[]::new);
        newCEP.thresholds = Arrays.stream(this.thresholds).toArray();
        newCEP.minThreshold = this.minThreshold;
        newCEP.maxThreshold = this.maxThreshold;
        newCEP.zeroProbability = this.zeroProbability;
        newCEP.indent = this.indent;
        newCEP.indentLevel = this.indentLevel;
        return newCEP;
    }
}
