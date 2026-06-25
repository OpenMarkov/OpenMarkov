package org.openmarkov.core.inference;

// TODO - Adapt these test to the new task approach (or remove them)
public abstract class InferenceEvaluationTaskIDTest extends InferenceResolutionTaskDecTest {
    
    //TODO: Many of the code written in this test class reference code no longer existing
    
    /*
    
    protected ProbNet iD_DiagnosisProblem;
    protected ProbNet iD_UniformDiagnosisProblem;
    protected ProbNet iD_DecisionTestProblemWithoutSV;
    protected ProbNet iD_DecisionTestProblemWithSV;
    
    public InferenceEvaluationTaskIDTest() {
        iD_DiagnosisProblem = NetsFactory
                .createInfluenceDiagramDiagnosisProblem();
        iD_UniformDiagnosisProblem = IDFactory
                .createUniformInfluenceDiagramDiagnosisProblem();
        iD_DecisionTestProblemWithoutSV = IDFactory
                .createInfluenceDiagramDecisionTestProblemWithoutSV(0.07, 0.91,
                                                                    0.97);
        iD_DecisionTestProblemWithSV = IDFactory
                .createInfluenceDiagramDecisionTestProblem(0.07, 0.91, 0.97);
        
    }
    
    protected void testMEU(ProbNet net, double expectedMEU, StrategyTree expectedStrategy) throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        Evaluation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(net);
        Double meuEvaluation = algorithm.getUtility().getValues()[0];
        assertEquals(expectedMEU, meuEvaluation, maxError);
    }
    
    // Test for diagnosis problem
    //TODO: check this test and enable it
    public void testEvaluationIDDecisionTestProblem(ProbNet diagram)
            throws
            Exception {
        
        Evaluation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);
        
        
        // test max expected utility
        
        Double meuEvaluation = algorithm.getUtility().getValues()[0];
        assertEquals(96.006, meuEvaluation, maxError);
        
        // Test optimal policy
        Variable variableT = getVariableAndAssertNotNull(diagram, "T");
        Variable variableD = getVariableAndAssertNotNull(diagram, "D");
        
        Potential policyT = algorithm.getOptimalPolicy(variableT);
        Potential policyD = algorithm.getOptimalPolicy(variableD);
        assertNotNull(policyT);
        assertNotNull(policyD);
        
        // Test the size of the domain of the policy of T
        assertTrue(checkPolicy(getTablePotential(policyT), variableT, 0));
        
        // Test the size of the domain of the policy of D
        assertTrue(checkPolicy(getTablePotential(policyD), variableD, 2));
        
        // Test the a priori case
        Map<Variable, TablePotential> aPrioriProbabilities = null; //algorithm.getProbsAndUtilities();
        // Read the variables
        Variable variableX = getVariableAndAssertNotNull(diagram, "X");
        Variable variableY = getVariableAndAssertNotNull(diagram, "Y");
        Variable variableU1 = getVariableAndAssertNotNull(diagram, "U1");
        Variable variableU2 = getVariableAndAssertNotNull(diagram, "U2");
        
        
        //euPotT
        TablePotential euPotT = constructExpectedUtilitiesPolicyTDecisionTestProblem(variableT);
        //assertTrue(areEqualPotentials(euPotT,(TablePotential) algorithm.getExpectedUtilities(variableT)));
        
        //euPotT
        TablePotential euPotD = constructExpectedUtilitiesPolicyDDecisionTestProblem(variableT, variableY, variableD);
        //assertTrue(areEqualPotentials(euPotD,(TablePotential) algorithm.getExpectedUtilities(variableD)));
        
        checkProbabilityPotential(aPrioriProbabilities, variableX, 0.07);
        checkProbabilityPotential(aPrioriProbabilities, variableY, 0.0916,
                                  0.9084);
        checkProbabilityPotential(aPrioriProbabilities, variableD, 0.0916);
        checkProbabilityPotential(aPrioriProbabilities, variableT, 1.0);
        checkUtilityPotential(aPrioriProbabilities, variableU1, 98.006);
        checkUtilityPotential(aPrioriProbabilities, variableU2, -2.0);
        
        
    }
    
    //@Test
    public void testEvaluationSimpleIDWithoutDecisions() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        
        testMEU(IDFactory.createSimpleIDWithoutDecisions(), 83.7);
    }
    
    // @return An Intervention with the assignment 'decision = state'
    protected StrategyTree createSimpleIntervention(ProbNet id, String decision, String state) throws InvalidStateException, NodeNotFoundException {
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
    
    protected void testMEU(ProbNet diagram, double expectedMeu) throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        
        Evaluation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);
        
        
        // test max expected utility
        Double meuEvaluation = algorithm.getUtility().getValues()[0];
        assertEquals(expectedMeu, meuEvaluation, maxError);
        
        
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDOneDecision() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDOneDecision(), 87.4, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDPerfectKnowledge() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDPerfectKnowledge(), 9.72, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDPerfectKnowledgeCostTherapy() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDPerfectKnowledgeCostTherapy(), 9.685, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDNoKnowledge() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDNoKnowledge(), 9.02, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDTestAlways() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDTestAlways(), 9.3929, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDDecideTest() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(getIDDecideTest(), 9.3929, null);
    }
    
    protected ProbNet getIDDecideTest() {
        return IDFactory.buildIDDecideTest();
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDDecideTestSymptom() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDDecideTestSymptom(), 9.9143, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDQaleMediastinet() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDQaleMediastinet(), 2.1154194051058286, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDMediastinetWithoutSV() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDMediastinetWithoutSV(), 1.4709741803092176, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDMediastinetWithoutMediastinoscopy() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDMediastinetWithoutMediastinoscopy(), 1.5209741803092172, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDMediastinet() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDMediastinet(), 1.4709741803092176, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDArthronet() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDArthronet(), 0.4960714549037456, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDRedundantChance() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDRedundantChance(), 175.0, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDTwoIndependentDecisions() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDTwoIndependentDecisions(), 4.0, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDConcatenateOrderTwoDecisions() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDConcatenateOrderTwoDecisions(), 8.15, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDThreeIndependentDecisions() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDThreeIndependentDecisions(), 37.63, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDStatesTies() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDStatesTies(), 13.7, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDStatesTiesPerfectKnowledge() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDStatesTiesPerfectKnowledge(), 1.5, null);
    }
    
    @Disabled("Old tests with an AssertionError") @Test
    public void testIDConsecutiveDecisions() throws IncompatibleEvidenceException, NotEvaluableNetworkException {
        testMEU(IDFactory.buildIDConsecutiveDecisions(), 4.57501894, null);
    }
    
    // Test for diagnosis problem
    @Disabled("Old tests with an AssertionError") @Test
    public void testOptimizedPolicyIDPerfectKnowledge() throws Exception {
        
        ProbNet network = IDFactory.buildIDPerfectKnowledge();
        
        Evaluation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);
        
        // Test optimal policy
        Variable D = network.getVariable("Therapy");
        
        Potential policy = algorithm.getOptimalPolicy(D);
        assertNotNull(policy);
        
        // Test the size of the domain of the policy
        List<Variable> domainPolicy = policy.getVariables();
        domainPolicy.remove(D);
        assertEquals(1, domainPolicy.size());
        
        // Test the optimal choice of the policy
        double[] truePolicy = {1.0, 0.0, 0.0, 1.0};
        assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
    }
    
    // Test for diagnosis problem
    //TODO: this test should be moved, shouldn't it?
    //@Test
    public void testExpectedUtilitiesIDPerfectKnowledge() throws Exception {
        
        ProbNet network = IDFactory.buildIDPerfectKnowledge();
        
        Evaluation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(network);
        
        // Test optimal policy
        Variable D = network.getVariable("Therapy");
        
        Potential utilities = null; //algorithm.getExpectedUtilities(D);
        assertNotNull(utilities);
        
        // Test the size of the domain of the utilities table
        assertEquals(2, utilities.getVariables().size());
        
        Variable X = network.getVariable("Disease");
        
        TablePotential expectedPotential = new TablePotential(Arrays.asList(X, D), PotentialRole.CONDITIONAL_PROBABILITY);
        double[] values = {10.0, 3.0, 9.0, 8.0};
        expectedPotential.setValues(values);
        
        assertTrue(areEqualPotentials(getTablePotential(utilities), expectedPotential));
        
    }
    
    protected StrategyTree getStrategyDiagnosisProblem(
            ProbNet id,
            String resultTestName,
            String decisionName,
            String positiveResult,
            String negativeResult,
            String yesTherapy,
            String noTherapy) throws InvalidStateException, NodeNotFoundException {
        List<Variable> vars = new ArrayList<>();
        List<State> states = new ArrayList<>();
        String statesResultTestNames[] = new String[2];
        String statesTherapyNames[] = new String[2];
        
        statesResultTestNames[0] = positiveResult;
        statesResultTestNames[1] = negativeResult;
        statesTherapyNames[0] = yesTherapy;
        statesTherapyNames[1] = noTherapy;
        Variable dec = id.getVariable(decisionName);
        Variable resultTest = id.getVariable(resultTestName);
        vars.add(dec);
        vars.add(resultTest);
        List<State> statesRoot = new ArrayList<>();
        for (String nameState : statesResultTestNames) {
            statesRoot.add(resultTest.getState(nameState));
        }
        
        List<StrategyTree> interventionsChildren = new ArrayList<>();
        for (String nameState : statesTherapyNames) {
            interventionsChildren.add(createSimpleIntervention(id, decisionName, nameState));
        }
        
        
        StrategyTree interv = new StrategyTree(resultTest, statesRoot, interventionsChildren);
        
        return interv;
    }
    
    // @return An InferenceAlgorithm for 'network'. If the network is not evaluable
    // with the algorithm then the test calling this method is skipped.
    protected Evaluation buildInferenceTaskAndSkipTestIfNotEvaluable(
            ProbNet network) throws IncompatibleEvidenceException {
        
        //If the network is not evaluable then the test is skipped
        boolean isEvaluable = true;
        Evaluation task = buildInferenceTask(network);
        assumeTrue(isEvaluable);
        return task;
    }
    
    // Test for diagnosis problem
    @Test
    //TODO: review how to refactor it
    public void testEvaluationIDUniformDiagnosisProblem() throws Exception {
        
        ProbNet diagram = iD_UniformDiagnosisProblem;
        
        Evaluation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);
        // test max expected utility
        Double meuEvaluation = algorithm.getUtility().getValues()[0];
        assertEquals(10.0, meuEvaluation, maxError);
        
        // Test optimal policy
        Variable D = diagram.getVariable("D");
        
        Potential policy = algorithm.getOptimalPolicy(D);
        assertNotNull(policy);
        
        // Test the size of the domain of the policy
        List<Variable> domainPolicy = policy.getVariables();
        domainPolicy.remove(D);
        assertEquals(1, domainPolicy.size());
        
        // Test the optimal choice of the policy
        double[] truePolicy = {0.5, 0.5, 0.5, 0.5};
        
        assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
    }
    
    // Test for diagnosis problem
    @Test
    //TODO: has this test sense here?
    public void testAPrioriProbabilitiesIDTestAlways() throws Exception {
        
        ProbNet diagram = buildIDTestAlways();
        
        Evaluation algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);
        
        Variable variableX = diagram.getVariable(diseaseName);
        assertNotNull(variableX);
        Variable variableY = diagram.getVariable(testResultName);
        assertNotNull(variableY);
        Variable variableD = diagram.getVariable(therapyName);
        assertNotNull(variableD);
        
        HashMap<Variable, TablePotential> aPrioriProbabilities = null; //(HashMap<Variable, TablePotential>) algorithm.getProbsAndUtilities();
        // test potential probabilities
        checkProbabilityPotential(aPrioriProbabilities, variableX, 0.14);
        checkProbabilityPotential(aPrioriProbabilities, variableY, 0.1532, 0.8468);
        checkProbabilityPotential(aPrioriProbabilities, variableD, 0.1532);
    }
    
     */
    
}
