/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.action;

import org.openmarkov.core.exception.CostEffectivenessException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Given an influence diagram with several cost and effectiveness nodes, creates a new influence diagram
 * with a single utility node, which has an associated cost-effectiveness potential.
 *
 * @author Manuel Arias
 */
public class CreatePotentialUtility {
    
    public final static String defaultCEVariableName = "CostEffectiveness";
    
    /**
     * Creates a {@code GeneralizedTablePotential} of
     * {@code CEPartition} using the cost and effectiveness potentials.
     *
     * @param costPotential          {@code TablePotential}.
     * @param effectivenessPotential {@code TablePotential}.
     * @param lambdaMin              used in {@code PartitionLCE}.
     * @param lambdaMax              used in {@code PartitionLCE}.
     *
     * @throws CostEffectivenessException CostEffectivenessException
     *                                    {@code fsVariables} must be equal to {@code effectiveness.getVariables()} union
     *                                    {@code cost.getVariables()}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) public static GTablePotential createCEPotential(
            TablePotential costPotential, TablePotential effectivenessPotential, double lambdaMin, double lambdaMax) {
        
        // Gets the union of the variables of cost and effectiveness potential
        Set<Variable> aux = new LinkedHashSet<>();
        if (costPotential != null) {
            aux.addAll(costPotential.getVariables());
        }
        if (effectivenessPotential != null) {
            aux.addAll(effectivenessPotential.getVariables());
        }
        ArrayList<Variable> ceVariables = new ArrayList<Variable>(aux);
        
        // Get cost and effectiveness potential
        GTablePotential gPotential = new GTablePotential(new ArrayList<>(ceVariables));
        gPotential.setCriterion(new Criterion());
        
        if (ceVariables.isEmpty()) {
            // Cost and effectiveness potentials are constants, so also the cost-effectiveness potential
            gPotential.elementTable.add(new CEP.CEPBuilder()
                                                .thresholdBounds(lambdaMin, lambdaMax)
                                                .build(null, costPotential.getValues()[0], effectivenessPotential.getValues()[0]));
        } else {
            int[] dimensionsResult = gPotential.getDimensions();
            
            // Offsets accumulate algorithm
            // 1. Set up variables
            int[] coordinate = new int[dimensionsResult.length];
            int[][] accumulatedOffsets = new int[2][];
            accumulatedOffsets[0] = gPotential.getAccumulatedOffsets(costPotential.getVariables());
            accumulatedOffsets[1] = gPotential.getAccumulatedOffsets(effectivenessPotential.getVariables());
            int[] positions = {costPotential.getInitialPosition(), effectivenessPotential.getInitialPosition()};
            int increasedVariable;
            int tableSize = gPotential.getTableSize();
            
            // 2. Potential initialization operation
            for (int i = 0; i < tableSize; i++) {
                CEP partition = new CEP.CEPBuilder()
                        .thresholdBounds(lambdaMin, lambdaMax)
                        .build(null, costPotential.getValues()[positions[0]], effectivenessPotential.getValues()[positions[1]]);
                gPotential.elementTable.add(partition);
                // calculate next position using accumulated offsets
                increasedVariable = 0;
                for (int j = 0; j < coordinate.length; j++) {
                    coordinate[j]++;
                    if (coordinate[j] < dimensionsResult[j]) {
                        increasedVariable = j;
                        break;
                    }
                    coordinate[j] = 0;
                }
                
                // 3. Update the positions of the potentials we are multiplying
                for (int j = 0; j < positions.length; j++) {
                    positions[j] += accumulatedOffsets[j][increasedVariable];
                }
            }
        }
        
        Variable ceVariable = new Variable(defaultCEVariableName);
        ceVariable.setVariableType(VariableType.NUMERIC);
        gPotential.setCriterion(ceVariable.getDecisionCriterion());
        return gPotential;
    }
    
    //	/** Get potentials attached to a variable whose criterion is cost and sums them.
    //	 * @param influenceDiagram <code>ProbNet</code>
    //	 * @return <code>TablePotential</code>
    //	 * @throws CostEffectivenessException   */
    //	private static TablePotential getCostPotential(ProbNet influenceDiagram)
    //			throws CostEffectivenessException {
    //
    //		List<Variable> variables = influenceDiagram.getVariables();
    //		ArrayList<TablePotential> costPotentials = new ArrayList<TablePotential>();
    //		for (Variable variable : variables) {
    //			if (CEBaseOperations.isCostVariable(variable))	{
    //				Collection<Potential> potentialsVariable = influenceDiagram.getPotentials(variable);
    //				for (Potential potential : potentialsVariable) {
    //					if (potential.isAdditive()) {
    //						try {
    //							costPotentials.add(potential.getCPT());
    //						} catch (NonProjectablePotentialException | WrongCriterionException e) {
    //							throw new CostEffectivenessException(e.getMessage());
    //						}
    //					}
    //				}
    //			}
    //		}
    //		TablePotential costPotential = DiscretePotentialOperations.sum(costPotentials);
    //		return costPotential;
    //	}
    //
    //	/** Get potentials attached to a variable whose criterion is effectiveness and adds them.
    //	 * @param influenceDiagram <code>ProbNet</code>
    //	 * @return <code>TablePotential</code>
    //	 * @throws CostEffectivenessException   */
    //	private static TablePotential getEffectivenessPotential(ProbNet influenceDiagram)
    //			throws CostEffectivenessException {
    //		List<Variable> variables = influenceDiagram.getVariables();
    //		ArrayList<TablePotential> effectivenessPotentials = new ArrayList<TablePotential>();
    //		for (Variable variable : variables) {
    //			if (CEBaseOperations.isEffectivenessVariable(variable)) {
    //				Collection<Potential> potentialsVariable = influenceDiagram.getPotentials(variable);
    //				for (Potential potential : potentialsVariable) {
    //					if (potential.isAdditive()) {
    //						try {
    //							effectivenessPotentials.add(potential.getCPT());
    //						} catch (NonProjectablePotentialException | WrongCriterionException e) {
    //							throw new CostEffectivenessException(e.getMessage());
    //						}
    //					}
    //				}
    //			}
    //		}
    //		TablePotential effectivenessPotential = DiscretePotentialOperations.sum(effectivenessPotentials);
    //		return effectivenessPotential;
    //	}
    
}
