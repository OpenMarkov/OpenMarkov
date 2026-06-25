/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

/**
 * @author manolo
 * TODO - Adapt these test to the new task approach (or remove them)
 */
public abstract class InferenceAlgorithmDANTest extends InferenceAlgorithmDecTest {
    
    
    public InferenceAlgorithmDANTest() {
        maxError = 0.0001;
    }
    
    //TODO: Many of the code written in this test class reference code no longer existing
    
    /*
    @Override
    public InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet)
            throws NotEvaluableNetworkException {
        return null;
    }
    
    
    @Override
    protected void testMEUAndStrategy(ProbNet net, double expectedMEU,
                                      StrategyTree expectedStrategy)
            throws IncompatibleEvidenceException,
            UnexpectedInferenceException {
        InferenceAlgorithm algorithm = buildInferenceAlgorithmAndSkipTestIfNotEvaluable(net);
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
        assertEquals(expectedMEU, meuEvaluation, maxError);
        assertNotNull(algorithm.getOptimalStrategy());
    }
    
    @Test
    public void testDANUnorderedTwoDecsNoChance() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDANUnorderedTwoDecsNoChance(), 4.0, null);
    }
    
    @Test
    public void testDANUnorderedTwoDecs() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDANUnorderedTwoDecs(), 9.0, null);
    }
    
    @Test
    public void testDANTestAlways() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDANTestAlways(), 9.3929, null);
    }
    
    
    @Test
    public void testDANOneChance() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEU(DANFactory.buildOneChanceDAN(), 90.2);
    }
    
    @Test
    public void testDANNoKnowledge() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDANNoKnowledge(), 9.02, null);
    }
    
    @Test
    public void testDANPerfectKnowledge() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDANPerfectKnowledge(), 9.72, null);
    }
    
    @Test
    public void testDANDecideTest() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDecideTestDAN(), 9.3929, null);
    }
    
    @Test
    public void testDANTwoTest() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildTwoTestDAN(), 9.3324, null);
    }
    
    @Test
    public void testDANDiabetes() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDiabetesDAN(), 9.8261, null);
    }
    
    @Test
    public void testDANDecideTreatmentRestricted() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDecideTreatmentRestrictedDAN(), 88.6, null);
    }
    
    @Test
    public void testDANReactor() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildReactorDAN(), 10.0627, null);
    }
    
    @Test
    public void testDANDatingBranchAcceptSimplified() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDatingBranchAcceptSimplifiedDAN(), 9.88, null);
    }
    
    @Test
    public void testDANDatingAcceptNo() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDatingAcceptNoDAN(), 9.4076, null);
    }
    
    @Test
    public void testDANDating() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildDatingDAN(), 9.4076, null);
    }
    
    @Test
    public void testDANWooer() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildWooerDAN(), 7.73, null);
    }
    
    @Test
    public void testDANUserCarBuyer() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildUsedCarBuyer(), 32.96, null);
    }
    
    @Test
    public void testDANNTest3Tests() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildNTestsDAN(3), 9.80657, null);
    }
    
    @Test
    public void testDANMediastinet() throws IncompatibleEvidenceException, NodeNotFoundException {
        testMEUAndStrategy(DANFactory.buildMediastinetDAN(), 1.4709741803076086, null);
    }
     */
    
}