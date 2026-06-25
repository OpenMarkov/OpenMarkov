package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.InferenceTestsTools;
import org.openmarkov.integrationTests.inference.heuristics.Tools;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class IDResolutionAndPropagationTest {
	
	private ProbNet loadID(String nameSuffix) throws ParserException, IOException {
		Tools t = new Tools();
		return t.loadID(nameSuffix);
	}
	
	private void testBasicInferenceNoCEA(String nameSuffix, boolean checkStrategyTree) throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, IOException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
			testResolutionAndPropagation(loadID(nameSuffix),null,checkStrategyTree);
	}
	
	@Test
	public void testIDOnlyDecisionNoUtility() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, IOException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		testBasicInferenceNoCEA("only-decision-no-utility", false);
	}
	
	@Test
	public void testIDThreeDecTwoUtil() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, IOException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		testBasicInferenceNoCEA("three-dec-two-util", true);
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testIDOnlyImposedUniformDecisionNoUtility() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, IOException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		testBasicInferenceNoCEA("only-imposed-uniform-dec-no-util", false);
	}
	
	@Test
	public void testIDOnlyImposedDecisionAndChanceNoUtility() throws NonProjectablePotentialException, IncompatibleEvidenceException, ParserException, URISyntaxException, IOException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
		testBasicInferenceNoCEA("only-imposed-decision-and-chance-no-utility", false);
	}
	
	private static void testResolutionAndPropagation(ProbNet probNet, EvidenceCase preResolutionEvidence, boolean checkStrategyTree)
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
		InferenceTestsTools.testResolveNetwork(probNet, preResolutionEvidence, checkStrategyTree);

		// TODO - Check propagate errors
		InferenceTestsTools.testPropagateNetwork(probNet, probNet.getVariables(), preResolutionEvidence);
	}
	
}
