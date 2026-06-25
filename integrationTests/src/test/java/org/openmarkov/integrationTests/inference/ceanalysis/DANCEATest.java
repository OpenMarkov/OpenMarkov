package org.openmarkov.integrationTests.inference.ceanalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.integrationTests.inference.heuristics.Tools;

import java.io.IOException;
import java.net.URISyntaxException;


/**
 * Abstract base for DAN cost-effectiveness analysis integration tests.
 *
 * @author Manuel Arias
 */
public abstract class DANCEATest {
	
	public void testCEADANEvaluation(String danName, int globalNumberOfCEPIntervals, double... expectedThreshods) throws NonProjectablePotentialException, NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		Tools t = new Tools();
		ProbNet network = t.loadDAN(danName);
		CEAnalysis eval = buildCEAnalysis(network);
		testCEADANEvaluation(globalNumberOfCEPIntervals, eval, expectedThreshods);
	}
    
    protected void testCEADANEvaluation(int globalNumberOfCEPIntervals, CEAnalysis eval, double... expectedThreshods) throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        CEP cep = eval.getCEP();
		Assertions.assertNotNull(cep);
		Assertions.assertEquals(globalNumberOfCEPIntervals, cep.getNumIntervals());
		int numThresholds = globalNumberOfCEPIntervals - 1;
		Assertions.assertEquals(numThresholds, expectedThreshods.length);
		double[] obtainedThresholds = cep.getThresholds();
		for (int i = 0; i < numThresholds; i++) {
			Assertions.assertEquals(expectedThreshods[i], obtainedThresholds[i], 0.1);
		}

		// Structural assertions: every interval must have finite cost and effectiveness
		for (int i = 0; i < globalNumberOfCEPIntervals; i++) {
			double cost = cep.getCost(i);
			double eff  = cep.getEffectiveness(i);
			Assertions.assertFalse(Double.isNaN(cost),
					"Interval " + i + ": cost must not be NaN");
			Assertions.assertFalse(Double.isInfinite(cost),
					"Interval " + i + ": cost must not be Infinite");
			Assertions.assertFalse(Double.isNaN(eff),
					"Interval " + i + ": effectiveness must not be NaN");
			Assertions.assertFalse(Double.isInfinite(eff),
					"Interval " + i + ": effectiveness must not be Infinite");
		}

		// ICER consistency: for a single-threshold CEP the increment ratio must
		// equal the threshold value (within tolerance).
		if (numThresholds == 1 && expectedThreshods[0] > 0) {
			double cost0 = cep.getCost(0), eff0 = cep.getEffectiveness(0);
			double cost1 = cep.getCost(1), eff1 = cep.getEffectiveness(1);
			double deltaE = eff1 - eff0;
			if (Math.abs(deltaE) > 1e-10) {
				double icer = Math.abs((cost1 - cost0) / deltaE);
				Assertions.assertEquals(expectedThreshods[0], icer,
						expectedThreshods[0] * 0.01 + 0.01, // 1 % + absolute tolerance
						"ICER between the two intervals should equal the threshold");
			}
		}
	}
    
    protected abstract CEAnalysis buildCEAnalysis(ProbNet network) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates;
	
	@Test
	public void testDANOnlyNonZeroUtility() throws
			NonProjectablePotentialException, NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates, IOException {
		testCEADANEvaluation("only-non-zero-utility-ce", 1);
	}
	
	@Test
	public void testDANOnlyZeroyUtility() throws
			NonProjectablePotentialException, NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates, IOException {
		testCEADANEvaluation("only-zero-utility-ce", 1);
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testDANOneDecisionCE() throws
			NonProjectablePotentialException, NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates, IOException {
		testCEADANEvaluation("one-decision-CE", 2, 1.333333333);
	}
	
	@Tag(TestSpeed.SLOW)
	@Test
	public void testDANOneChanceCE() throws
			NonProjectablePotentialException, NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates, IOException {
		testCEADANEvaluation("one-chance-ce", 1);
	}
	
	@Tag(TestSpeed.SLOW)
	@Test
	public void testDANDecideTest() throws
			NonProjectablePotentialException, NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates, IOException {
		testCEADANEvaluation("decide-test-ce", 3, 11171.347828594418, 33383.5);
	}
		
	

}
