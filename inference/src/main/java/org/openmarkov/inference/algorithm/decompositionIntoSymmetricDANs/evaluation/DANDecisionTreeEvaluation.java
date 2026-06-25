package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation;

import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecisionTreeComputation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANDecisionTreeInference;

public class DANDecisionTreeEvaluation extends DANEvaluation implements DecisionTreeComputation {
    
    public DANDecisionTreeEvaluation(ProbNet network, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(network, computeDecisionTreeForGUI, false);
    }
    
    public DANDecisionTreeEvaluation(ProbNet probNet, EvidenceCase evidence) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(probNet, evidence, false);
    }
    
    
    public DANDecisionTreeEvaluation() {
        // TODO Auto-generated constructor stub
    }
    
    public DANDecisionTreeEvaluation(ProbNet probNet, int depth, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(probNet, depth, computeDecisionTreeForGUI, false);
    }
    
    public DANDecisionTreeEvaluation(ProbNet probNet, int depth, boolean computeDecisionTreeForGUI, EvidenceCase branchEvidence) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        inferenceProcess = new DANDecisionTreeInference(probNet, branchEvidence, depth, computeDecisionTreeForGUI, false);
    }
    
    @Override
    public DecisionTreeNode getDecisionTree() {
        return ((DecisionTreeComputation) inferenceProcess).getDecisionTree();
    }
    
}
