/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.dan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.integrationTests.inference.NetworkEvaluationInferenceTest;
import org.openmarkov.integrationTests.inference.heuristics.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class DANEvaluationTest extends NetworkEvaluationInferenceTest {
    
    
    @BeforeEach public void setUp() {
    }
    
    
    @Override
    protected ProbNet loadNetwork(String networkName) throws ProbNetParserException, IOException {
        Tools t = new Tools();
        return t.loadDAN(networkName);
    }
    
    
    @Test
    public void testDANOnlyDecisionNoUtility() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-decision-no-utility", 0.0, "D");
    }
    
    @Test
    public void testDANOnlyUtility() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-utility", 10.0);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testDANOneChance() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("one-chance", 83.7);
    }
    
    @Test
    public void testDANOneDecision() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("one-decision", 87.4, "D");
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testDANNoKnowledge() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("no-knowledge", 9.16, "D");
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testDANPerfectKnowledge() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("perfect-knowledge", 9.72, "A", "D");
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANTest2Therapies() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("test-2therapies", 9.39366, "Test", "Therapy");
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANTest2TherapiesNoCostSymmetrizedOrderForced() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("decide-test-2therapies-no-cost-symmetrized-order-forced", 9.39366, "Do test?",
                              "Result of test", "Therapy");
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANTest2TherapiesNoCostOrderForced() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("decide-test-2therapies-no-cost-order-forced", 9.39366, "Do test?", "Result of test",
                              "Therapy");
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANTest2TherapiesNoCost() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("decide-test-2therapies-no-cost", 9.39366, "Do test?", "Result of test", "Therapy");
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANUIDsPaper() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("UID-luque2016-OM-0-2-0", 10, "OD", "D", "X", "E");
        
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANDiabetes() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("diabetes", 979.8337, "Symptom", "OD", "Dec: Blood Test", "Dec: Urine test",
                              "Blood test result", "Urine test result", "Therapy");
        
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANSimplifiedUsedCarBuyer() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("simplified-used-car-buyer", 32.96, "Dec: First Test", "First Result", "Dec: Purchase");
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANUsedCarBuyer() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        //testDANEvaluation("used-car-buyer",32.96,"Dec: First Test","First Result","Dec: Second Test","Dec: Purchase");
        testNetworkEvaluation("used-car-buyer", 32.96);
        
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANReactor() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("reactor", 8.1280, "Test decision", "Result of test", "Build decision");
        
    }
    
    @Test
    public void testDANKingNobleDescentYesFirstTask1() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("king-noble-descent-yes-first-task-1", 9.03);
        
    }
    
    @Test
    public void testDANKingNobleDescentYesFirstTask1SecondTask2() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("king-noble-descent-yes-first-task-1-second-task-2", 9.03);
    }
    
    @Disabled @Test
    public void testDANSimplifiedTwoTasksKingNobleDescentYes() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("simplified-two-tasks-king-noble-descent-yes", 9.08);
    }
    
    /**
     * This network is as "simplified-two-tasks-king-noble-descent-yes", but removing zero utility potentials.
     */
    @Disabled @Test public void testDANSimplified2TwoTasksKingNobleDescentYes()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("simplified-2-two-tasks-king-noble-descent-yes", 9.08);
    }
    
    /**
     * This network is as "simplified-two-tasks-king-noble-descent-yes", but removing zero utility potentials.
     */
    @Disabled @Tag(TestSpeed.SLOW)
    @Test public void testDANSimplifiedOneTaskKingNobleDescentYes()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("simplified-one-task-king-noble-descent-yes", 9.28);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testDANKingNobleDescentNo()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("king-noble-descent-no", 6.43);
        
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testDANKingNobleDescentYes()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("king-noble-descent-yes", 9.03);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testDANKing()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("king", 7.73);
    }
    
    @Disabled @Tag(TestSpeed.SLOW)
    @Test public void testDAN3Tests()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("3-test-problem", 9.6162, "Symptom", "OD", "Dec: Test 0", "Dec: Test 1", "Dec: Test 2",
                              "Test Result 1", "Test Result 2", "Therapy");
    }
    
    
    @Disabled @Test public void testDANTutorial33()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("tutorial-3-3", 7.73);
    }
    
    
    @Disabled @Test public void testDANDatingAskNo()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        //TODO I still have to find out the exact value of the evaluation, because I have found that different algorithms return different expected utilities
        testNetworkEvaluation("dating-ask-no", 8.1632);
        
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testDANDatingAskNoNClubNo()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("dating-ask-no-nclub-no", -7);
        
    }
    
    
    @Disabled @Test
    public void testDANDating()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("dating", 9.4076);
        
    }
    
    //@Test
    //TODO DAN-mediastinet has super-value nodes. It must be converted into a DAN with only ordinary utility nodes.
    public void testDANMediastinet()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("mediastinet", 1.4710368294106826);
        
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testDANOnlyTwoUtilitySumSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-two-utility-sum-sv", 5);
    }
    
    @Test public void testDANOnlyTwoUtilityProductSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-two-utility-product-sv", 6);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void testDANNestedSumSV() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        // 2+3+4+5 = 14
        testNetworkEvaluation("nested-sum-sv", 14);
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testDANNestedProductSV() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("nested-product-sv", 120);
    }
    
    @Test
    public void testDANNestedSumOfProductsSV() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("nested-sum-of-products-sv", 26);
    }
    
    @Test
    public void testDANNestedProductOfSumsSV() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("nested-product-of-sums-sv", 45);
    }

    @Test
    public void testDANNestedSumSVNonTree() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        // Issue #520: Non-tree super-value structure where U2 is shared by U5 and U6
        // U1=2, U2=3, U4=5, U5=Sum(U1,U2)=5, U6=Sum(U2,U4)=8, U0=Sum(U5,U6)=13
        testNetworkEvaluation("nested-sum-sv-non-tree", 13);
    }
    
    @Test public void testDANOnlyOneUtilityAbsSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-one-utility-abs-sv", 2);
    }
    
    @Test
    public void testDANWith2UtilityNodes() throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        // Issue #486: Error evaluating DAN with 2 utility nodes
        testNetworkEvaluation("with-2-utility-nodes", 2251.25);
    }

    @Test public void testDANOnlyOneUtilitySumAbsSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-one-utility-sum-abs-sv", 4);
    }
    
    @Test public void testDANOnlyTwoUtilityProductAbsSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-two-utility-product-abs-sv", -6);
    }
    
    @Test public void testDANUtilityAndChanceFunctionSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("utility-and-chance-function-sv", Math.abs(3) * 0.7 + Math.abs(-9) * 0.3);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testDANDecUtilProductSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        //for (int x:Arrays.asList(2,5)) {
        for (int x : Arrays.asList(2)) {
            testNetworkEvaluation("dec-util-product-0-" + x + "-0", 15.0);
        }
    }
    
    @Disabled @Test public void testDANOnlyTwoUtilityAndChanceFunctionSV()
            throws
            NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        testNetworkEvaluation("only-two-utility-and-chance-function-sv", Math.abs(12 * 0.7 + (-20) * 0.3) + 3 * Math.abs(-2));
    }
		


	/*	

		@Test
		public void testDatingDAN() throws NodeNotFoundException, NotEvaluableNetworkException, IncompatibleEvidenceException {
			ProbNet datingDAN = DANFactory.buildDatingDAN();
			long startTime = System.nanoTime();
			DecompositionAlgorithm recursiveEvaluation = new DecompositionAlgorithm(datingDAN);
			TablePotential globalUtility = recursiveEvaluation.getGlobalUtility();
			long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
			System.out.println(" Execution time =" +ellapsedTime);
			Assert.assertEquals(9.4076, globalUtility.getValues()[0], 0.0001);
			Assert.assertNotNull(recursiveEvaluation.getOptimalStrategy());
		}


		@Test
		public void testMediastiNetDAN() throws NodeNotFoundException, IncompatibleEvidenceException,
				UnexpectedInferenceException, NotEvaluableNetworkException {

			ProbNet mediastiNetDAN = DANFactory.buildMediastinetDAN();
			long startTime = System.nanoTime();
			mediastiNetDAN = BasicOperations.removeSuperValueNodes(mediastiNetDAN, new EvidenceCase());
			DecompositionAlgorithm recursiveEvaluation = new DecompositionAlgorithm(mediastiNetDAN);
			TablePotential globalUtility = recursiveEvaluation.getGlobalUtility();
			long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
			System.out.println(" Execution time =" +ellapsedTime);
			Assert.assertEquals(1.4710368294106826, globalUtility.getValues()[0], 0.000001);
		}	

		@Test
		public void testNtests() throws NodeNotFoundException, IncompatibleEvidenceException,
				UnexpectedInferenceException, NotEvaluableNetworkException {

			ProbNet nTestsDAN = DANFactory.buildNTestsDAN(4);
			long startTime = System.nanoTime();
			DecompositionAlgorithm recursiveEvaluation = new DecompositionAlgorithm(nTestsDAN);
			TablePotential globalUtility = recursiveEvaluation.getGlobalUtility();
			long ellapsedTime = (System.nanoTime() - startTime) / 1000000;
			System.out.println(" Execution time =" +ellapsedTime);
		}

		@Test
		public void testTwoPhasesOfTests() throws NodeNotFoundException, IncompatibleEvidenceException,
		UnexpectedInferenceException, NotEvaluableNetworkException {
			ProbNet twoPhasesOfTestsDAN = DANFactory.buildTwoPhasesOfTestsDAN(2);
			DecompositionAlgorithm recursiveEvaluation = new DecompositionAlgorithm(twoPhasesOfTestsDAN);
			TablePotential globalUtility = recursiveEvaluation.getGlobalUtility();

		}
	 */
  
}
