package org.openmarkov.integrationTests.inference;

import org.openmarkov.core.exception.*;
import org.openmarkov.integrationTests.inference.heuristics.Tools;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecisionTreeComputation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;


public abstract class NetworkEvaluationInferenceTest {
	
	public void testNetworkEvaluation(String networkName, double expectedEU, String... namesVariablesIntervention) throws NotEvaluableNetworkException, ParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		ProbNet network = loadNetwork(networkName);
		System.out.println("*** Evaluating network " + networkName + " ***");
		System.out.println();
		testNetworkEvaluation(network,expectedEU,namesVariablesIntervention);
	}
    
    public void testNetworkEvaluation(ProbNet network, double expectedEU, String... namesVariablesIntervention) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		System.out.println();
		DANEvaluation eval = buildNetworkEvaluation(network);
		testDANEvaluation(eval, network, expectedEU, namesVariablesIntervention);
	}

	protected void testDANEvaluation(DANEvaluation eval, ProbNet network, double expectedEU,
			String... namesVariablesIntervention) {
		TablePotential globalUtility = eval.getUtility();
		Tools.testEvaluationResults(network, expectedEU, globalUtility, namesVariablesIntervention);
	}
	
	protected abstract ProbNet loadNetwork(String networkName) throws ParserException, URISyntaxException, IOException;
    
    protected abstract DANEvaluation buildNetworkEvaluation(ProbNet network) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates;
    
    
    protected abstract DANEvaluation buildNetworkEvaluation(ProbNet network, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates;
    
    public void testNetworkEvaluationAndDecisionTree(ProbNet network, double expectedEU, String... namesVariablesIntervention) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		System.out.println();
		
		boolean computeDTValues []= {true, false};
		for (boolean computeDT: computeDTValues) {
            DANEvaluation eval = buildNetworkEvaluation(network, computeDT);
			testDANEvaluation(eval, network, expectedEU, namesVariablesIntervention);
			Tools.testDecisionTree(network, computeDT, (DecisionTreeComputation) eval);
		}
	}
	
}
