package org.openmarkov.core.inference;

// TODO - Adapt these test to the new task approach (or remove them)
public abstract class InferenceAlgorithmIDTest extends InferenceAlgorithmDecTest {
    
    //TODO: Many of the code written in this test class reference code no longer existing
    /*
    
    //protected ProbNet iD_DiagnosisProblem;
    protected ProbNet iD_UniformDiagnosisProblem;
    protected ProbNet iD_DecisionTestProblemWithoutSV;
    protected ProbNet iD_DecisionTestProblemWithSV;
    protected ProbNet iD_DiagnosisProblem;
    
    
    public InferenceAlgorithmIDTest() {
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
    
    @Test
    public void testIDOneDecision() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDOneDecision(), 87.4, null);
    }
    
    @Test
    public void testIDPerfectKnowledge() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDPerfectKnowledge(), 9.72, null);
    }
    
    @Test
    public void testIDPerfectKnowledgeCostTherapy() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDPerfectKnowledgeCostTherapy(), 9.685, null);
    }
    
    @Test
    public void testIDNoKnowledge() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDNoKnowledge(), 9.02, null);
    }
    
    @Test
    public void testIDTestAlways() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDTestAlways(), 9.3929, null);
    }
    
    @Test
    public void testIDDecideTest() throws IncompatibleEvidenceException {
        testMEUAndStrategy(getIDDecideTest(), 9.3929, null);
    }
    
    protected ProbNet getIDDecideTest() {
        return IDFactory.buildIDDecideTest();
    }
    
    @Test
    public void testIDDecideTestSymptom() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDDecideTestSymptom(), 9.9143, null);
    }
    
    @Test
    public void testIDQaleMediastinet() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDQaleMediastinet(), 2.1154194051058286, null);
    }
    
    
    @Test
    public void testIDMediastinetWithoutSV() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDMediastinetWithoutSV(), 1.4709741803092176, null);
    }
    
    @Test
    public void testIDMediastinetWithoutMediastinoscopy() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDMediastinetWithoutMediastinoscopy(), 1.5209741803092172, null);
    }
    
    
    @Test
    public void testIDMediastinet() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDMediastinet(), 1.4709741803092176, null);
    }
    
    @Test
    public void testIDArthronet() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDArthronet(), 0.4960714549037456, null);
    }
    
    @Test
    public void testIDRedundantChance() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDRedundantChance(), 175.0, null);
    }
    
    
    @Test
    public void testIDTwoIndependentDecisions() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDTwoIndependentDecisions(), 4.0, null);
    }
    
    @Test
    public void testIDConcatenateOrderTwoDecisions() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDConcatenateOrderTwoDecisions(), 8.15, null);
    }
    
    @Test
    public void testIDThreeIndependentDecisions() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDThreeIndependentDecisions(), 37.63, null);
    }
    
    @Test
    public void testIDStatesTies() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDStatesTies(), 13.7, null);
    }
    
    @Test
    public void testIDStatesTiesPerfectKnowledge() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDStatesTiesPerfectKnowledge(), 1.5, null);
    }
    
    
    @Test
    public void testIDConsecutiveDecisions() throws IncompatibleEvidenceException {
        testMEUAndStrategy(IDFactory.buildIDConsecutiveDecisions(), 4.57501894, null);
    }
    
    
    //Test for diagnosis problem
    @Test
    public void testPosteriorProbsAndUtilsIDDecisionTestProblem()
            throws
            Exception {
        ProbNet network = IDFactory.buildIDDecideTest();
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
        
        checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
                                                  network, IDFactory.therapyName, "no", 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
                                                  9.895843174303259, 0.0, -0.2);
        checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
                                                  network, IDFactory.testResultName, "negative", 1.0, 0.0, 1.0, 0.0, 0.014879546528105,
                                                  9.895843174303259, 0.0, -0.2);
        checkPosteriorProbsAndUtilitiesDecideTest(algorithm,
                                                  network, IDFactory.decTestName, "yes", 1.0, 0.1532, 0.8468, 0.1532, 0.14, 9.6312,
                                                  -0.0383, -0.2);
        
        EvidenceCase evi = new EvidenceCase();
        evi.addFinding(network, IDFactory.decTestName, "yes");
        evi.addFinding(network, IDFactory.testResultName, "positive");
        checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
                algorithm, network, evi, 1.0, 1.0, 0.0, 1.0,
                0.8316, 8.1684, -0.25, -0.2);
        evi.addFinding(network, IDFactory.diseaseName, "present");
    }
    
    protected void checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
            InferenceAlgorithm algorithm, ProbNet diagram,
            EvidenceCase evi, double t, double y1, double y2, double d,
            double x, double uHealthState, double uCostOfTherapy, double uCostOfTest) throws Exception {
        
        Variable variableT = diagram.getVariable(IDFactory.decTestName);
        Variable variableD = diagram.getVariable(IDFactory.therapyName);
        Variable variableX = diagram.getVariable(IDFactory.diseaseName);
        Variable variableY = diagram.getVariable(IDFactory.testResultName);
        Variable variableU1 = diagram.getVariable(IDFactory.healthStateName);
        Variable variableU2 = diagram.getVariable(IDFactory.therapyCostName);
        Variable variableU3 = diagram.getVariable(IDFactory.testCostName);
        
        algorithm.setPostResolutionEvidence(evi);
        Map<Variable, TablePotential> aPosterioriProbabilities = null;
        aPosterioriProbabilities = algorithm.getProbsAndUtilities();
        
        
        checkProbabilityPotential(aPosterioriProbabilities, variableX, x);
        checkProbabilityPotential(aPosterioriProbabilities, variableY, y1,
                                  y2);
        checkProbabilityPotential(aPosterioriProbabilities, variableD, d);
        checkProbabilityPotential(aPosterioriProbabilities, variableT, t);
        checkUtilityPotential(aPosterioriProbabilities, variableU1, uHealthState);
        checkUtilityPotential(aPosterioriProbabilities, variableU2, uCostOfTherapy);
        checkUtilityPotential(aPosterioriProbabilities, variableU3, uCostOfTest);
    }
    
    
    protected void checkPosteriorProbsAndUtilitiesDecideTest(
            InferenceAlgorithm algorithm, ProbNet diagram,
            String nameVariable, String state, double t, double y1, double y2,
            double d, double x, double uHealthState, double uCostOfTherapy, double uCostOfTest)
            throws Exception {
        EvidenceCase evi = new EvidenceCase();
        evi.addFinding(diagram, nameVariable, state);
        checkPosteriorProbsAndUtilitiesEvidenceIDDecideTest(
                algorithm, diagram, evi, t, y1, y2, d, x, uHealthState, uCostOfTherapy, uCostOfTest);
    }
    
    // Test for diagnosis problem
    @Test
    public void testEvaluationIDUniformDiagnosisProblem()
            throws
            IOException, ParserException, NodeNotFoundException,
            ConstraintViolatedException, NotEvaluableNetworkException, NonProjectablePotentialException, WrongCriterionException {
        
        ProbNet diagram = iD_UniformDiagnosisProblem;
        
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
        
        // test max expected utility
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
        assertEquals(10.0, meuEvaluation, maxError);
        
        // Test optimal policy
        Variable D = diagram.getVariable("D");
        
        Potential policy = algorithm.getOptimizedPolicy(D);
        assertNotNull(policy);
        
        // Test the size of the domain of the policy
        List<Variable> domainPolicy = policy.getVariables();
        domainPolicy.remove(D);
        assertEquals(1, domainPolicy.size());
        
        // Test the optimal choice of the policy
        double[] truePolicy = {0.5, 0.5, 0.5, 0.5};
        
        assertTrue(areEquals(getTablePotential(policy).getValues(), truePolicy));
        
    }
    
    
    //TODO Review the minor error in test
    @Test
    public void testPreAndPostResolutionEvidenceIDDecisionTestProblem() throws Exception {
        ProbNet diagram = iD_DecisionTestProblemWithSV;
        
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
        
        //TODO Test combination of pre and post resolution findings.
        
        //Variable variableT = diagram.getVariable("T");
        //Variable variableD = diagram.getVariable("D");
        
        EvidenceCase preResolutionEvidence = new EvidenceCase();
        preResolutionEvidence.addFinding(diagram, IDFactory.diseaseName, "present");
        algorithm.setPreResolutionEvidence(preResolutionEvidence);
        // test max expected utility
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
        assertEquals(80.0, meuEvaluation, maxError);
        
        // Test optimal policy
        
        // 	HashMap<Variable, TablePotential> optimalStrategy = variableElimination
        //		.getOptimizedPolicies();
        //TablePotential policyT = optimalStrategy.get(variableT);
        //TablePotential policyD = optimalStrategy.get(variableD);
        //assertNotNull(policyT);
        //assertNotNull(policyD);
        
        // Test the size of the domain of the policy of T
        //assertTrue(checkPolicy(policyT, variableT, 0));
        
        // Test the size of the domain of the policy of D
        //assertTrue(checkPolicy(policyD, variableD, 2));
        
        // Test the a priori case
        //HashMap<Variable, TablePotential> aPrioriProbabilities = variableElimination
        //		.getProbsAndUtilities();
        
        // Read the variables
        // variableX = diagram.getVariable("X");
        // assertNotNull(variableX);
        // variableY = diagram.getVariable("Y");
        // assertNotNull(variableY);
        // variableU1 = diagram.getVariable("U1");
        // assertNotNull(variableU1);
        // variableU2 = diagram.getVariable("U2");
        // assertNotNull(variableU2);
        
        // checkProbabilityPotential(aPrioriProbabilities, variableX, 0.07);
        // checkProbabilityPotential(aPrioriProbabilities, variableY, 0.0916,
        // 		0.9084);
        // checkProbabilityPotential(aPrioriProbabilities, variableD, 0.0916);
        // checkProbabilityPotential(aPrioriProbabilities, variableT, 1.0);
        // checkUtilityPotential(aPrioriProbabilities, variableU1, 98.006);
        // checkUtilityPotential(aPrioriProbabilities, variableU2, -2.0);
        
        // Test the expected utilities of the policy
        // StrategyUtilities strategyUtilities =
        // variableElimination.getUtilityTables();
        // TablePotential policyUtilities =
        // strategyUtilities.getUtilities(D);
        
        // Test the optimal choice of the policy
        // double[] truePolicy = {1.0, 0.0, 0.0, 1.0};
        
        // assertTrue(areEquals(policy.getValues(),truePolicy));
        
    }
    
    
    // Test for diagnosis problem
    @Test
    public void testOptimizedPolicyIDPerfectKnowledge() throws Exception {
        ProbNet network = IDFactory.buildIDPerfectKnowledge();
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
        
        // Test optimal policy
        Variable D = network.getVariable("Therapy");
        
        Potential policy = algorithm.getOptimizedPolicy(D);
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
    @Test
    public void testExpectedUtilitiesIDPerfectKnowledge()
            throws
            IOException, ParserException, NodeNotFoundException,
            ConstraintViolatedException, NotEvaluableNetworkException, NonProjectablePotentialException, WrongCriterionException {
        
        ProbNet network = IDFactory.buildIDPerfectKnowledge();
        
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(network);
        
        // Test optimal policy
        Variable D = network.getVariable("Therapy");
        
        Potential utilities = algorithm.getExpectedUtilities(D);
        assertNotNull(utilities);
        
        // Test the size of the domain of the utilities table
        assertEquals(2, utilities.getVariables().size());
        
        Variable X = network.getVariable("Disease");
        
        TablePotential expectedPotential = new TablePotential(Arrays.asList(X, D), PotentialRole.CONDITIONAL_PROBABILITY);
        double[] values = {10.0, 3.0, 9.0, 8.0};
        expectedPotential.setValues(values);
        
        assertTrue(areEqualPotentials(getTablePotential(utilities), expectedPotential));
        
    }
    
    
    // Test for diagnosis problem
    @Test
    public void testAPrioriProbabilitiesIDTestAlways()
            throws
            IOException, ParserException, NodeNotFoundException,
            ConstraintViolatedException, NotEvaluableNetworkException {
        
        ProbNet diagram = IDFactory.buildIDTestAlways();
        
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(diagram);
        
        Variable variableX = diagram.getVariable(IDFactory.diseaseName);
        assertNotNull(variableX);
        Variable variableY = diagram.getVariable(IDFactory.testResultName);
        assertNotNull(variableY);
        Variable variableD = diagram.getVariable(IDFactory.therapyName);
        assertNotNull(variableD);
        
        // A priori probabilities
        Map<Variable, TablePotential> aPrioriProbabilities = algorithm.getProbsAndUtilities();
        // test potential probabilities
        checkProbabilityPotential(aPrioriProbabilities, variableX, 0.14);
        checkProbabilityPotential(aPrioriProbabilities, variableY, 0.1532, 0.8468);
        checkProbabilityPotential(aPrioriProbabilities, variableD, 0.1532);
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
        Variable dec = null;
        Variable resultTest = null;
        String statesResultTestNames[] = new String[2];
        String statesTherapyNames[] = new String[2];
        
        statesResultTestNames[0] = positiveResult;
        statesResultTestNames[1] = negativeResult;
        statesTherapyNames[0] = yesTherapy;
        statesTherapyNames[1] = noTherapy;
        dec = id.getVariable(decisionName);
        resultTest = id.getVariable(resultTestName);
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
    
    */
    
}
