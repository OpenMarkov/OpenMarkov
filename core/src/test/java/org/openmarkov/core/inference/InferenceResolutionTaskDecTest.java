/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;


/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 */
public abstract class InferenceResolutionTaskDecTest extends InferenceTaskTest {
    
    //TODO: Many of the code written in this test class reference code no longer existing
    /*
    
    // Builds an InferenceAlgorithm object with 'probNet'.
    // This method must be implemented by each inference test class.
    public abstract Evaluation buildInferenceTask(ProbNet probNet)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException;
    
    // Checks if the values of the potential 'x' are equal to 'v' and if the number
    // of values in 'x' is 1.
    protected void checkUtility(TablePotential x, double v) {
        assertEquals(1, x.getTableSize());
        assertEquals(v, x.getValues()[0], maxError);
    }
    
    public void checkUtilityPotential(Map<Variable, TablePotential> aPrioriProbabilities, Variable variableU,
                                      double u) {
        TablePotential U = (TablePotential) aPrioriProbabilities.get(variableU);
        checkUtility(U, u);
    }
    
    // Test for diagnosis problem
    @Test
    public void testEvaluationIDDecisionTestProblem() {
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
