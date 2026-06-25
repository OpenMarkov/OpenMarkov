/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.dan;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;

public class DANDecompositionIntoSymmetricDANsEvaluationTest extends DANEvaluationTest {
    
    @Override
    protected DANEvaluation buildNetworkEvaluation(ProbNet network) throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return new DANDecompositionIntoSymmetricDANsEvaluation(network);
	}

	@Override
	protected DANEvaluation buildNetworkEvaluation(ProbNet network, boolean computeDecisionTreeForGUI)
            throws NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		return buildNetworkEvaluation(network);
	}

}
