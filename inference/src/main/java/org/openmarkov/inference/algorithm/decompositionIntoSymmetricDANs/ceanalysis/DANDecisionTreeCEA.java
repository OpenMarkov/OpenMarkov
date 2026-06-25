package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis;

import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecisionTreeComputation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANDecisionTreeInference;

public class DANDecisionTreeCEA extends DANCEAnalysis implements DecisionTreeComputation {
    
    public DANDecisionTreeCEA(ProbNet network, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(network, computeDecisionTreeForGUI, true);
    }
    
    public DANDecisionTreeCEA(ProbNet probNet, EvidenceCase evidence) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(probNet, evidence, true);
    }
    
    
    public DANDecisionTreeCEA() {
        // TODO Auto-generated constructor stub
    }
    
    public DANDecisionTreeCEA(ProbNet probNet, int depth, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(probNet, depth, computeDecisionTreeForGUI, true);
    }
    
    public DANDecisionTreeCEA(ProbNet probNet, int depth, boolean computeDecisionTreeForGUI, EvidenceCase branchEvidence) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(probNet, branchEvidence, depth, computeDecisionTreeForGUI, true);
    }
    
    
    public DANDecisionTreeCEA(ProbNet probNet) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        this(probNet, null);
    }
    
    @Override
    public DecisionTreeNode getDecisionTree() {
        return ((DecisionTreeComputation) inferenceProcess).getDecisionTree();
    }
    
}
