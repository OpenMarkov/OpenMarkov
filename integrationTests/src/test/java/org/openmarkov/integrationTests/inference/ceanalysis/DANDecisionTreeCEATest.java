package org.openmarkov.integrationTests.inference.ceanalysis;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.MulticriteriaOptions.Type;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecisionTreeComputation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis.DANDecisionTreeCEA;
import org.openmarkov.integrationTests.inference.heuristics.Tools;

import java.io.IOException;

public class DANDecisionTreeCEATest extends DANCEATest {

	@Override
    protected CEAnalysis buildCEAnalysis(ProbNet network) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		return buildCEAnalysis(network, true);
	}
    
    
    protected CEAnalysis buildCEAnalysis(ProbNet network, boolean computeDTForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return new DANDecisionTreeCEA(network, computeDTForGUI);
	}

	@Override
	public void testCEADANEvaluation(String danName, int globalNumberOfCEPIntervals, double... expectedThreshods) throws NonProjectablePotentialException, NotEvaluableNetworkException, ProbNetParserException, IOException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		Tools t = new Tools();
		ProbNet network = t.loadDAN(danName);
		MulticriteriaOptions options = new MulticriteriaOptions();
		options.setMulticriteriaType(Type.COST_EFFECTIVENESS);
		network.getInferenceOptions().setMultiCriteriaOptions(options);
		boolean computeDTValues[] = {true, false};
		for (boolean computeDT: computeDTValues) {
			CEAnalysis eval = buildCEAnalysis(network, computeDT);
			testCEADANEvaluation(globalNumberOfCEPIntervals, eval, expectedThreshods);
			Tools.testDecisionTree(network, computeDT, (DecisionTreeComputation) eval);
		}
	}
	
	

}
