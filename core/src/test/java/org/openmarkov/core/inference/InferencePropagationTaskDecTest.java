/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 */
public abstract class InferencePropagationTaskDecTest extends InferenceTaskTest {
    
    public abstract Propagation buildInferenceTask(ProbNet probNet, List<Variable> variablesOfInterest,
                                                   EvidenceCase preResolutionEvidence, EvidenceCase postResolutionEvidence)
            ;
    
    public void checkUtilityPotential(Map<Variable, TablePotential> aPrioriProbabilities, Variable variableU,
                                      double u) {
        TablePotential U = aPrioriProbabilities.get(variableU);
        checkUtility(U, u);
    }
    
    // Checks the posterior utilities of a list of utility nodes.
    // The utilities are ordered according the order in 'variables'.
    protected void checkUtilities(HashMap<Variable, TablePotential> aPosterioriUtils, ArrayList<Variable> variables,
                                  double[] expectedUtils) {
        int size = variables.size();
        for (int i = 0; i < size; i++) {
            checkUtilityPotential(aPosterioriUtils, variables.get(i), expectedUtils[i]);
        }
    }
    
    // Checks if the values of the potential 'x' are equal to 'v' and if the number
    //          of values in 'x' is 1.
    protected void checkUtility(TablePotential x, double v) {
        assertEquals(1, x.getTableSize());
        assertEquals(v, x.getValues()[0], maxError);
    }
    
    /*TODO: Many of the code written in this test class reference code no longer existing
    
    // Test for diagnosis problem
    @Test
    public void testEvaluationIDDecisionTestProblem()
            throws FileNotFoundException,
            IOException, ParserException, NodeNotFoundException,
            ConstraintViolatedException, NotEvaluableNetworkException {
        Task algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(iD_DecisionTestProblemWithoutSV);
        
        // test max expected utility
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
        assertEquals(96.006, meuEvaluation, maxError);
        
        // Test optimal policy
        Variable T = diagram.getVariable("T");
        Variable D = diagram.getVariable("D");
        HashMap<Variable, TablePotential> optimalStrategy = algorithm
                .getOptimizedPolicies();
        TablePotential policyT = optimalStrategy.get(T);
        TablePotential policyD = optimalStrategy.get(D);
        assertNotNull(policyT);
        assertNotNull(policyD);
        
        // Test the size of the domain of the policy of T
        assertTrue(checkPolicy(policyT, T, 0));
        
        // Test the size of the domain of the policy of D
        assertTrue(checkPolicy(policyD, D, 2));
        
        // Test the expected utilities of the policy
        // StrategyUtilities strategyUtilities =
        // variableElimination.getUtilityTables();
        // TablePotential policyUtilities =
        // strategyUtilities.getUtilities(D);
        
        // Test the optimal choice of the policy
        // double[] truePolicy = {1.0, 0.0, 0.0, 1.0};
        
        // assertTrue(areEquals(policy.getValues(),truePolicy));
    }
    */
    
}
