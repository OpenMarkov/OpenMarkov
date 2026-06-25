/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANDecompositionIntoSymmetricDANsInference;

public class DANDecompositionIntoSymmetricDANsEvaluation extends DANEvaluation {
    
    public DANDecompositionIntoSymmetricDANsEvaluation(ProbNet network) throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		inferenceProcess = new DANDecompositionIntoSymmetricDANsInference(network, false);
	}

	public DANDecompositionIntoSymmetricDANsEvaluation(ProbNet probNet, EvidenceCase evidenceCase)
            throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		inferenceProcess = new DANDecompositionIntoSymmetricDANsInference(probNet, evidenceCase, false);

	}


}
