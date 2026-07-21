/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AbstractIndexedPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.inference.algorithm.variableElimination.ChanceVariableElimination;
import org.openmarkov.inference.algorithm.variableElimination.DecisionVariableElimination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class DANInference {
    
    
    final boolean isCEAnalysis;
    
    /**
     * These two attributes are the result of the evaluation
     */
    protected TablePotential probability;
    
    protected final ProbNet probNet;
    
    protected Potential utility;


    /**
     * These two attributes are used for storing the results of evaluating the children in a decomposition scheme
     * The i-th element of each list corresponds to the result of the evaluation of the i-th child
     */
    protected final List<TablePotential> childrenProbability;
    protected final List<Potential> childrenUtility;
    
    public DANInference(ProbNet network, boolean isCEAnalysis2) {
        this.probNet = network.copy();
        childrenProbability = new ArrayList<>();
        childrenUtility = new ArrayList<>();
        isCEAnalysis = isCEAnalysis2;
    }
    
    public void setProbability(TablePotential probability) {
        if (probability == null) {
            probability = DiscretePotentialOperations.createUnityProbabilityPotential();
        }
        this.probability = probability;
    }
    
    protected void setUtility(Potential util) {
        if (util.getCriterion() == null) {
            util.setCriterion(this.probNet.getDecisionCriteria().get(0));
        }
        this.utility = util;
    }
    
    protected void addProbabilityChildEvaluation(TablePotential prob) {
    
    }
    
    /**
     * @param x Variable
     *          This method...:
     *          First, it conditions on Vaiable 'x' the sets 'probabilityPotentials' and 'utilityPotentials'.
     *          Second, it marginalizes 'x' out of the resulting potentials of the first step.
     *          Third, it stores the resulting probability and utility potentials in the corresponding attributes.
     */
    @SuppressWarnings("unchecked")
    protected void conditionEliminateChanceAndSetProbabilityAndUtility(ProbNet dan, Variable x) throws PotentialOperationException.DifferentSizesInPotentialsAndStates {
        TablePotential conditionedProbabilityPotential = (TablePotential) DiscretePotentialOperations.merge(x, childrenProbability);
        Potential conditionedUtilityPotential = (Potential) DiscretePotentialOperations.merge(x, (List<? extends AbstractIndexedPotential>)(List<?>) childrenUtility);
        eliminateChanceVariable(dan, x, conditionedProbabilityPotential, conditionedUtilityPotential);
    }
    
    /**
     * @param dan                  Network
     * @param x the index
     * @param probabilityPotential the probability potential
     * @param utilityPotential     It eliminates the variable 'x' from the sets of 'probability'
     *                             and 'utility' potentials (as in a variable-elimination
     *                             scheme). The result of the method are a probability and
     *                             utility potential that are stored in the attributes
     *                             "probability" and "utility" respectively.
     */
    protected void eliminateChanceVariable(ProbNet dan, Variable x, TablePotential probabilityPotential,
                                           Potential utilityPotential) {
        ChanceVariableElimination elimination =
                new ChanceVariableElimination(x, Arrays.asList(probabilityPotential),
                                              Arrays.asList(utilityPotential));
        setProbability(elimination.getMarginalProbability());
        List<Potential> eliminatedUtilityPotentials = elimination.getUtilityPotentials();
        if (eliminatedUtilityPotentials.size() == 1 && eliminatedUtilityPotentials.get(0) instanceof GTablePotential) {
            setUtility(eliminatedUtilityPotentials.get(0));
        } else {
            @SuppressWarnings("unchecked")
            List<TablePotential> utilityTablePotentials = (List<TablePotential>)(List<?>) eliminatedUtilityPotentials;
            setUtility(DiscretePotentialOperations.sum(utilityTablePotentials));
        }
    }
    
    protected void maximizeAndSetUtility(ProbNet dan, Variable rootDecision, TablePotential probability,
                                         Potential conditionedUtilityPotential) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        DecisionVariableElimination elimination = new DecisionVariableElimination(rootDecision, Arrays.asList(probability),
                                                                                  Arrays.asList(conditionedUtilityPotential));
        
        Potential newUtilityPotential = elimination.getUtility();
        if (newUtilityPotential == null) {
            newUtilityPotential = (Potential) DiscretePotentialOperations.createZeroUtilityPotential(dan);
        }
        setProbability(elimination.getProjectedProbability());
        setUtility(newUtilityPotential);
    }
    
    /**
     * @param dan the dan
     * @param d   First, it conditions on 'd' the sets 'probabilityPotentials'
     *            and 'utilityPotentials'. Second, it maximizes over 'd' the
     *            resulting potentials. Third, it stores the resulting
     *            probability and utility potentials in the corresponding
     *            attributes of the object.
     */
    @SuppressWarnings("unchecked")
    protected void conditionMaximizeAndSetProbabilityAndUtility(ProbNet dan, Variable d) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        // We take the probability of any of the children, as it should be equal
        setProbability(childrenProbability.get(0));
        Potential conditionedUtilityPotential = (Potential) DiscretePotentialOperations.merge(d, (List<? extends AbstractIndexedPotential>)(List<?>) childrenUtility);
        maximizeAndSetUtility(dan, d, childrenProbability.get(0), conditionedUtilityPotential);
    }
    
    protected void addResultsOfChildEvaluation(DANInference auxEval) {
        
        childrenProbability.add(auxEval.getProbability());
        childrenUtility.add(auxEval.getUtility());
    }
    
    /*
     * private TablePotential getDANInferenceProcessProbability() { return
     * probability; }
     */
    
    public void setProbabilityAndUtilityFromEvaluation(DANInference eval) {
        setProbability(eval.getProbability());
        setUtility(eval.getUtility());
        
    }
    
    /*
     * private TablePotential getDANInferenceProcessUtility() { return utility; }
     */
    
    protected List<Variable> getAlwaysObservedVariables(ProbNet dan, List<Variable> conditioningVariablesList,
                                                        EvidenceCase evidenceCase) {
        return DANOperations.getVariablesObservedFromTheBegginning(dan, conditioningVariablesList, evidenceCase, true);
    }
    
    
    public TablePotential getProbability() {
        return probability;
    }
    
    public Potential getUtility() {
        return utility;
    }
    
    
}
