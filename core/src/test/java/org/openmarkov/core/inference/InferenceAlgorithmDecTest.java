package org.openmarkov.core.inference;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 * TODO - Adapt these test to the new task approach (or remove them)
 */
public abstract class InferenceAlgorithmDecTest extends InferenceAlgorithmTest {
    
    
    //TODO: Many of the code written in this test class reference code no longer existing
    /*
    
    
    protected void testMEUAndStrategy(ProbNet net, double expectedMEU, StrategyTree expectedStrategy) throws IncompatibleEvidenceException {
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(net);
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
        assertEquals(expectedMEU, meuEvaluation, maxError);
        testScenariosIntervention(net, algorithm);
    }
    
     // Checks that the Intervention (optimal strategy) obtained from the evaluation optimal is not null
     // and that it is consistent with the Cooper policy network (CPN) built using the policies obtained
     // from the method getOptimizedPolicies
    private void testScenariosIntervention(ProbNet net,
                                           InferenceAlgorithm algorithm) throws IncompatibleEvidenceException, InvalidStateException {
        StrategyTree interv = algorithm.getOptimalStrategy();
        assertNotNull(interv);
        testIntervention(algorithm, interv, new EvidenceCase());
    }
    
    
    
    // Checks that the Intervention 'interv' rooted at the evidence scenario 'parentEvi' is consistent with the
    // Cooper policy network (CPN) that has been obtained by the inference algorithm 'algorithm'
    // The  method checks correctness and completeness:
    // - Correctness: Every scenario in the intervention has non-zero probability in the CPN
    // - Completeness: The Intervention covers all the non-zero probability states of the CPN
    private void testIntervention(InferenceAlgorithm algorithm, StrategyTree interv, EvidenceCase parentEvi)
            throws IncompatibleEvidenceException, InvalidStateException {
        
        if (interv != null) {
            interv.getRootVariable();
            List<TreeADDBranch> branches = interv.getBranches();
            Variable rootVariable = interv.getRootVariable();
            algorithm.setPostResolutionEvidence(parentEvi);
            List<Variable> interestVariables = new ArrayList<>();
            interestVariables.add(rootVariable);
            TablePotential probs = algorithm.getProbsAndUtilities().get(rootVariable);
            if (branches != null) {
                // Check that the number of branches is equal to the non-zero
                // probability states
                assertEquals(getNumStatesBranches(branches), getNumProbsNotZero(probs));
                for (int i = 0; i < branches.size(); i++) {
                    TreeADDBranch auxBranch = branches.get(i);
                    StrategyTree auxStrategyTreeBranch = StrategyTree.getInterventionBranch(auxBranch);
                    for (State state : auxBranch.getStates()) {
                        // Check that 'state' has non-zero probability in the
                        // CPN
                        assertTrue(probs.getValues()[rootVariable.getStateIndex(state)] > 0);
                        EvidenceCase newEvi = new EvidenceCase(parentEvi.getFindings());
                        
                        Finding finding = new Finding(rootVariable, state);
                        newEvi.addFinding(finding);
                        testIntervention(algorithm, auxStrategyTreeBranch, newEvi);
                    }
                }
            }
        }
    }
    
        // Test for diagnosis problem
    @Test
    public void testEvaluationIDDecisionTestProblem()
            throws FileNotFoundException,
            IOException, ProbNetParserException, NodeNotFoundException,
            ConstraintViolatedException, NotEvaluableNetworkException {
        
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(iD_DecisionTestProblemWithoutSV);
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
    
    //Test for diagnosis problem
    public void testEvaluationIDDecisionTestProblem(ProbNet diagram)
            throws
            IOException, ProbNetParserException, NodeNotFoundException,
            ConstraintViolatedException, NotEvaluableNetworkException {
        Variable variableX = null;
        Variable variableY = null;
        Variable variableT = null;
        Variable variableD = null;
        Variable variableU1 = null;
        Variable variableU2 = null;
        
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
        
        // test max expected utility
        
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
        assertEquals(96.006, meuEvaluation, maxError);
        
        // Test optimal policy
        variableT = getVariableAndAssertNotNull(diagram, "T");
        variableD = getVariableAndAssertNotNull(diagram, "D");
        
        
        StrategyTree optimalStrategy = algorithm.getOptimalStrategy();
        
        Potential policyT = algorithm.getOptimizedPolicy(variableT);
        Potential policyD = algorithm.getOptimizedPolicy(variableD);
        assertNotNull(policyT);
        assertNotNull(policyD);
        
        // Test the size of the domain of the policy of T
        assertTrue(checkPolicy(getTablePotential(policyT), variableT, 0));
        
        // Test the size of the domain of the policy of D
        assertTrue(checkPolicy(getTablePotential(policyD), variableD, 2));
        
        // Test the a priori case
        Map<Variable, TablePotential> aPrioriProbabilities = algorithm
                .getProbsAndUtilities();
        // Read the variables
        variableX = getVariableAndAssertNotNull(diagram, "X");
        variableY = getVariableAndAssertNotNull(diagram, "Y");
        variableU1 = getVariableAndAssertNotNull(diagram, "U1");
        variableU2 = getVariableAndAssertNotNull(diagram, "U2");
        
        
        //euPotT
        TablePotential euPotT = constructExpectedUtilitiesPolicyTDecisionTestProblem(variableT);
        assertTrue(areEqualPotentials(euPotT, (TablePotential) algorithm.getExpectedUtilities(variableT)));
        
        //euPotT
        TablePotential euPotD = constructExpectedUtilitiesPolicyDDecisionTestProblem(variableT, variableY, variableD);
        assertTrue(areEqualPotentials(euPotD, (TablePotential) algorithm.getExpectedUtilities(variableD)));
        
        checkProbabilityPotential(aPrioriProbabilities, variableX, 0.07);
        checkProbabilityPotential(aPrioriProbabilities, variableY, 0.0916,
                                  0.9084);
        checkProbabilityPotential(aPrioriProbabilities, variableD, 0.0916);
        checkProbabilityPotential(aPrioriProbabilities, variableT, 1.0);
        checkUtilityPotential(aPrioriProbabilities, variableU1, 98.006);
        checkUtilityPotential(aPrioriProbabilities, variableU2, -2.0);
    }
    
    protected void testMEU(ProbNet diagram, double expectedMeu) throws IncompatibleEvidenceException {
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
        // test max expected utility
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
        assertEquals(expectedMeu, meuEvaluation, maxError);
    }
    
    @Test
    public void testEvaluationSimpleIDWithoutDecisions() throws IncompatibleEvidenceException {
        testMEU(IDFactory.createSimpleIDWithoutDecisions(), 83.7);
    }
    
    private StrategyTree getOptimalStrategy(ProbNet id) throws IncompatibleEvidenceException {
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(id);
        StrategyTree strategy = algorithm.getOptimalStrategy();
        return strategy;
    }
    */
    
    
    // @return The total number of states in 'branches'
    private int getNumStatesBranches(List<TreeADDBranch> branches) {
        Set<State> states = new HashSet<>();
        if (branches != null) {
            for (int i = 0; i < branches.size(); i++) {
                TreeADDBranch auxBranch = branches.get(i);
                if (auxBranch != null) {
                    states.addAll(auxBranch.getStates());
                }
            }
        }
        int numStates = states.size();
        return numStates;
    }
    
    
    // @return The number of values in the potential that are greater than zero
    private int getNumProbsNotZero(TablePotential probs) {
        int numNotZero = 0;
        double[] values = probs.getValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i] > 0.0) {
                numNotZero = numNotZero + 1;
            }
        }
        return numNotZero;
    }
    
    
    private TablePotential constructExpectedUtilitiesPolicyTDecisionTestProblem(Variable variableT) {
        
        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(variableT);
        
        TablePotential pot = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double values[] = {96.006, 95.1};
        pot.setValues(values);
        return pot;
    }
    
    
    private TablePotential constructExpectedUtilitiesPolicyDDecisionTestProblem(Variable variableT,
                                                                                Variable variableY, Variable variableD) {
        
        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(variableT);
        variables.add(variableY);
        variables.add(variableD);
        
        TablePotential pot = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double values[] = {81.04585153, 0.0, 87.93064729, 0.0, -2.0, 89.3, 49.3209607, 0.0, 97.51453104, 0.0, -2.0, 95.1};
        pot.setValues(values);
        return pot;
    }
    
    public void checkUtilityPotential(
            Map<Variable, TablePotential> aPrioriProbabilities,
            Variable variableU, double u) {
        TablePotential U = (TablePotential) aPrioriProbabilities.get(variableU);
        checkUtility(U, u);
        
    }
    
    
    private boolean checkPolicy(TablePotential policy, Variable d, int numVar) {
        List<Variable> domainPolicy = policy.getVariables();
        domainPolicy.remove(d);
        return (numVar == domainPolicy.size());
    }
    
    
    // @return An Intervention with the assignment 'decision = state'
    protected StrategyTree createSimpleIntervention(ProbNet id, String decision, String state) {
        List<Variable> vars = new ArrayList<>();
        List<State> states = new ArrayList<>();
        Variable dec = id.getVariable(decision);
        vars.add(dec);
        
        StrategyTree interv = new StrategyTree(dec, states);
        interv.setRootVariable(dec);
        states.add(dec.getState(state));
        TreeADDBranch branch = new TreeADDBranch(states, dec, vars);
        interv.addBranch(branch);
        return interv;
    }
    
    
    // Checks the posterior utilities of a list of utility nodes.
    // The utilities are ordered according the order in 'variables'.
    protected void checkUtilities(
            HashMap<Variable, TablePotential> aPosterioriUtils,
            ArrayList<Variable> variables, double[] expectedUtils) {
        int size = variables.size();
        for (int i = 0; i < size; i++) {
            checkUtilityPotential(aPosterioriUtils, variables.get(i), expectedUtils[i]);
        }
        
    }
    
    // Checks if the values of the potential 'x' are equal to 'v' and if the number
    // of values in 'x' is 1.
    protected void checkUtility(TablePotential x, double v) {
        
        assertEquals(1, x.getTableSize());
        assertEquals(v, x.getValues()[0], maxError);
        
    }
    
    
}
