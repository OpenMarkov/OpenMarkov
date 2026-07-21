package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANDecompositionIntoSymmetricDANsInference;

public class DANDecompositionIntoSymmetricDANsCEA extends DANCEAnalysis {
    
    public DANDecompositionIntoSymmetricDANsCEA(ProbNet network) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        this(network, null);
    }
    
    public DANDecompositionIntoSymmetricDANsCEA(ProbNet probNet, EvidenceCase evidenceCase) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecompositionIntoSymmetricDANsInference(probNet,evidenceCase, true);
    }
    
}
