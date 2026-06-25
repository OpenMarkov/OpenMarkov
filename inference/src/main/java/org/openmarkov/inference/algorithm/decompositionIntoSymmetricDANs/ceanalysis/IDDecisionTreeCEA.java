package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.IDDecisionTreeInference;

public class IDDecisionTreeCEA extends DANDecisionTreeCEA {
    
    
    public IDDecisionTreeCEA(ProbNet network, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		inferenceProcess = new IDDecisionTreeInference(network, computeDecisionTreeForGUI, true);
	}

	public IDDecisionTreeCEA(ProbNet probNet, int depth, boolean computeDecisionTreeForGUI)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		inferenceProcess = new IDDecisionTreeInference(probNet, depth, computeDecisionTreeForGUI);
	}

	/**
	 * @param probNet the prob net
	 * @param depth                     Number of levels expanded in the decision
	 *                                  tree (the rest of levels are summarized as
	 *                                  utilities in the leaves)
	 * @param computeDecisionTreeForGUI the compute decision tree for gui
	 * @param evidence the evidence
	 * @throws NotEvaluableNetworkException if the network cannot be evaluated
	 */
	public IDDecisionTreeCEA(ProbNet probNet, int depth, boolean computeDecisionTreeForGUI, EvidenceCase evidence)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		inferenceProcess = new IDDecisionTreeInference(probNet, evidence, depth, computeDecisionTreeForGUI, true);
	}

}
