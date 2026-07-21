/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination;

import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.CEUtilityPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.inference.algorithm.variableElimination.action.CreatePotentialUtility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Essential variable elimination algorithm for Bayesian networks and influence diagrams.
 *
 * @author Manuel Arias
 */
public class VariableEliminationCore {
    
    // Attributes
    /**
     * Minimum threshold by default.
     */
    protected static final double defLambdaMin = 0.0;
    
    /**
     * Higher threshold by default.
     */
    protected static final double defLambdaMax = Double.POSITIVE_INFINITY;
    
    /**
     * Minimum threshold applicable to this case.
     */
    protected double lambdaMin;
    
    /**
     * Higher threshold applicable to this case.
     */
    protected double lambdaMax;
    
    private ProbNet markovDecisionNetwork;
    
    private boolean isUnicriterion;
    
    private EliminationHeuristic heuristic;
    
    /**
     * Decision variables with their policies.
     */
    private Map<Variable, TablePotential> optimalPolicies;
    
    /**
     * Whether the utility potentials have been joined into a single CEP potential,
     * which will be of type {@code GTablePotential}. This operation is performed
     * in the case of cost-effectiveness analysis, just before eliminating the first decision.
     */
    private boolean thereIsCEPPotential;
    
    private PNESupport pneSupport;
    
    // Constructors
    
    /**
     * Initialize data structures and executes the algorithm.
     *
     * @param markovDecisionNetwork {@code ProbNet}
     * @param heuristic             {@code EliminationHeuristic}
     * @param isUnicriterion        {@code boolean}
     */
    public VariableEliminationCore(ProbNet markovDecisionNetwork, EliminationHeuristic heuristic,
                                   boolean isUnicriterion) {
        initialize(markovDecisionNetwork, heuristic, isUnicriterion);
        try {
            performVariableElimination();
        } catch (DoEditException | IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther |
                 NonProjectablePotentialException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * Initialize data structures and executes the algorithm.
     * This constructor must be used only in bi-criteria analysis.
     *
     * @param markovDecisionNetwork {@code ProbNet}
     * @param heuristic             {@code EliminationHeuristic}
     * @param isUnicriterion        {@code boolean}
     * @param lambdaMin             {@code double}
     * @param lambdaMax             {@code double}
     */
    public VariableEliminationCore(ProbNet markovDecisionNetwork, EliminationHeuristic heuristic,
                                   boolean isUnicriterion, double lambdaMin, double lambdaMax) {
        
        this.lambdaMin = lambdaMin;
        this.lambdaMax = lambdaMax;
        initialize(markovDecisionNetwork, heuristic, isUnicriterion);
        try {
            performVariableElimination();
        } catch (DoEditException | IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther |
                 NonProjectablePotentialException e) {
            throw new UnreachableException(e);
        }
    }
    
    // Methods
    
    /**
     * Executes the variable elimination loop, repeatedly asking the heuristic for
     * the next variable to eliminate until none remain.
     */
    private void performVariableElimination() throws DoEditException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        while (true) {
            Variable variableToDelete = heuristic.getVariableToDelete();
            if (variableToDelete == null) break;
            eliminateVariable(variableToDelete);
        }
    }
    
    /**
     * Eliminates a single variable from the Markov decision network. For chance variables,
     * performs marginalization (sum out); for decision variables, performs maximization.
     *
     * @param variableToDelete the variable to eliminate
     *
     * @throws DoEditException if the node removal edit fails
     */
    @SuppressWarnings("rawtypes") private void eliminateVariable(Variable variableToDelete)
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, DoEditException {
        NodeType nodeType = markovDecisionNetwork.getNode(variableToDelete).getNodeType();
        
        if (!isUnicriterion && nodeType == NodeType.DECISION && !thereIsCEPPotential) {
            createCEPPotential();
        }
        
        // Extract the potentials that depend on the variable
        List<TablePotential> probPotentials = new ArrayList<>();
        for (Potential potential : markovDecisionNetwork.getProbPotentials(variableToDelete)) {
            probPotentials.add((TablePotential) potential);
            markovDecisionNetwork.removePotential(potential);
        }
        List<Potential> utilityPotentials = new ArrayList<>();
        for (Potential potential : markovDecisionNetwork.getUtilityPotentials(variableToDelete)) {
            utilityPotentials.add(potential);
            markovDecisionNetwork.removePotential(potential);
        }
        RemoveNodeEdit removeNodeEdit = new RemoveNodeEdit(markovDecisionNetwork, variableToDelete);
        removeNodeEdit.executeEdit();
        if (nodeType == NodeType.CHANCE) {
            ChanceVariableElimination elimination = new ChanceVariableElimination(variableToDelete, probPotentials,
                                                                                  utilityPotentials);
            markovDecisionNetwork.addPotential(elimination.getMarginalProbability());
            for (Potential potential : elimination.getUtilityPotentials()) {
                markovDecisionNetwork.addPotential(potential);
            }
            
        } else {
            DecisionVariableElimination elimination = new DecisionVariableElimination(variableToDelete, probPotentials, utilityPotentials);
            markovDecisionNetwork.addPotential(elimination.getProjectedProbability());
            markovDecisionNetwork.addPotential(elimination.getUtility());
            optimalPolicies.put(variableToDelete, elimination.getOptimalPolicy());
        }
    }
    
    //TODO Revisar quitar (Manolo)
	/*private void addNewUtilityPotentialToMarkovDecisionNetwork(
			TablePotential utilityPotential,
			Potential newPotential) {
		newPotential.setCriterion(utilityPotential.getCriterion()); // Set as utility potential
		markovDecisionNetwork.addPotential(newPotential);
	}
*/
    
    /**
     * Collects all the utility potentials, removes them from the network, and joins them
     * into a GTablePotential of {@code CEP}s, which is added to the network and returned.
     *
     * @return a {@code GTablePotential} of {@code CEP}s
     */
    @SuppressWarnings("rawtypes")
    public GTablePotential createCEPPotential() {
        // Join all the utility potentials into a GTablePotential of CEPs
        ArrayList<TablePotential> costPotentials = new ArrayList<TablePotential>();
        ArrayList<TablePotential> effectivenessPotentials = new ArrayList<TablePotential>();
        
        for (Potential potential : markovDecisionNetwork.getAdditivePotentials()) {
            markovDecisionNetwork.removePotential(potential);
            if (potential.getCriterion().getCECriterion() == Criterion.CECriterion.Cost) {
                costPotentials.add((TablePotential) potential);
            } else {
                effectivenessPotentials.add((TablePotential) potential);
            }
        }
        
        TablePotential costPotential = DiscretePotentialOperations.sum(costPotentials);
        TablePotential effectivenessPotential = DiscretePotentialOperations.sum(effectivenessPotentials);
        
        GTablePotential cepUtilityPotential;
        cepUtilityPotential = CreatePotentialUtility
                .createCEPotential(costPotential, effectivenessPotential, lambdaMin, lambdaMax);
        cepUtilityPotential.setCriterion(new Criterion("#{COST-EFFECTIVENESS}#")); // Set as utility potential
        markovDecisionNetwork.addPotential(cepUtilityPotential);
        
        thereIsCEPPotential = true;
        return cepUtilityPotential;
    }
    
    /**
     * Returns the optimal policies computed during variable elimination, one per decision variable.
     *
     * @return map from decision variable to its optimal policy potential
     */
    public Map<? extends Variable, ? extends Potential> getOptimalPolicies() {
        return optimalPolicies;
    }
    
    /**
     * Returns the optimal policy for a specific decision variable.
     *
     * @param decisionVariable the decision variable whose policy is requested
     *
     * @return the optimal policy potential, or {@code null} if not computed
     */
    public Potential getOptimalPolicy(Variable decisionVariable) {
        return optimalPolicies.get(decisionVariable);
    }
    
    
    /**
     * Returns the global expected utility after variable elimination. In unicriterion mode,
     * sums all remaining utility potentials; in bicriteria mode, returns a
     * {@link GTablePotential} of cost-effectiveness partitions.
     *
     * <p>The return type is bounded to {@code Potential & CEUtilityPotential} so that
     * callers receive a value that is simultaneously usable as a potential and as a
     * cost-effectiveness carrier. Concretely the runtime type is always a
     * {@link TablePotential} (unicriterion) or a {@link GTablePotential} (bicriteria).</p>
     *
     * @param <P> inferred potential type — bound to {@link Potential} and {@link CEUtilityPotential}
     *
     * @return the global utility potential
     */
    @SuppressWarnings("unchecked")
    public <P extends Potential & CEUtilityPotential> P getUtility() {
        List<Potential> utilityPotentials = markovDecisionNetwork.getAdditivePotentials();
        int numUtilityPotentials = utilityPotentials.size();
        if (!isUnicriterion) {
            if (numUtilityPotentials == 1) {
                Potential firstPotential = utilityPotentials.get(0);
                return (P) (firstPotential instanceof GTablePotential ? firstPotential : createCEPPotential());
            }
            return (P) createCEPPotential();
        }
        if (numUtilityPotentials == 0) {
            TablePotential utility = new TablePotential(null, PotentialRole.UNSPECIFIED);
            utility.setCriterion(new Criterion()); // Set this potential as additive with the "Default" criterion type.
            return (P) utility;
        }
        if (numUtilityPotentials == 1) {
            return (P) utilityPotentials.get(0);
        }
        List<TablePotential> utilityTablePotentials = new ArrayList<>();
        for (Potential potential : utilityPotentials) {
            utilityTablePotentials.add((TablePotential) potential);
        }
        return (P) DiscretePotentialOperations.sum(utilityTablePotentials);
    }
    
    
    /**
     * Legacy version of {@link #getUtility()}. Retained for backward compatibility.
     *
     * @return the global utility potential
     *
     * @deprecated use {@link #getUtility()} instead
     */
    public Potential getUtilityOld() {
        List<Potential> utilityPotentials = markovDecisionNetwork.getAdditivePotentials();
        
        Potential utility;
        if (utilityPotentials.isEmpty()) {
            utility = isUnicriterion ? new TablePotential(null, PotentialRole.UNSPECIFIED) : createCEPPotential();
            utility.setCriterion(new Criterion()); // Set this potential as additive with the "Default" criterion type.
        } else {
            if (isUnicriterion) {
                // sum the utility potentials
                List<TablePotential> utilityTablePotentials = new ArrayList<>();
                for (Potential potential : utilityPotentials) {
                    utilityTablePotentials.add((TablePotential) potential);
                }
                utility = DiscretePotentialOperations.sum(utilityTablePotentials);
            } else {
                utility = thereIsCEPPotential ? utilityPotentials.get(0) : createCEPPotential();
            }
        }
        return utility;
    }
    
    /**
     * @return A {@code TablePotential} that is the result of the multiplication
     * of all the probability potentials.
     */
    public TablePotential getProbability() {
        List<TablePotential> probPotentials = new ArrayList<>();
        List<Potential> allPotentials = markovDecisionNetwork.getPotentials();
        for (Potential potential : allPotentials) {
            if (!potential.isAdditive()) {
                probPotentials.add((TablePotential) potential);
            }
        }
        TablePotential probability = DiscretePotentialOperations.multiply(probPotentials);
        if (probability == null) {
            probability = new TablePotential(null, PotentialRole.CONDITIONAL_PROBABILITY);
        }
        return probability;
    }
    
    /**
     * Common code of the two constructors. Initializes the network, heuristic,
     * optimal-policies map, and lambda bounds.
     *
     * @param markovDecisionNetwork the Markov decision network to solve
     * @param heuristic             the elimination heuristic to determine variable order
     * @param isUnicriterion        {@code true} for single-criterion analysis, {@code false} for cost-effectiveness
     */
    private void initialize(ProbNet markovDecisionNetwork, EliminationHeuristic heuristic, boolean isUnicriterion) {
        
        thereIsCEPPotential = false;
        this.markovDecisionNetwork = markovDecisionNetwork;
        this.heuristic = heuristic;
        
        pneSupport = markovDecisionNetwork.getPNESupport();
        pneSupport.addListener(heuristic);
        
        this.isUnicriterion = isUnicriterion;
        
        if (!isUnicriterion) {
            lambdaMin = defLambdaMin;
            lambdaMax = defLambdaMax;
        }
        optimalPolicies = new LinkedHashMap<Variable, TablePotential>();
        // The iterators through LinkedHashMap follows the insertion order, which can be useful later
    }
    
    public ProbNet getMarkovDecisionNetwork() {
        return this.markovDecisionNetwork;
    }
    
}
