package org.openmarkov.integrationTests.inference.ceanalysis;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis.DANDecompositionIntoSymmetricDANsCEA;

public class DANDecompositionIntoSymmetricDANsCEATest extends DANCEATest {
    
    @Override
    protected CEAnalysis buildCEAnalysis(ProbNet network) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return new DANDecompositionIntoSymmetricDANsCEA(network);
	}

}
