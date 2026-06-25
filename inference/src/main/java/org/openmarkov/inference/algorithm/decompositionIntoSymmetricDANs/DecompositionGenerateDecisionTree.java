package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs;

import java.util.List;

import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.tasks.GenerateDecisionTree;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis.DANDecisionTreeCEA;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis.IDDecisionTreeCEA;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecisionTreeEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.IDDecisionTreeEvaluation;

/**
 * Generates a decision tree from a probabilistic network (ID or DAN) by decomposing it
 * into symmetric sub-problems. Supports both unicriterion and cost-effectiveness analysis.
 */
public class DecompositionGenerateDecisionTree implements GenerateDecisionTree {
	
	private final ProbNet probNet;
	EvidenceCase preresolutionEvidence;
	private final int depth;
	private final boolean isUnicriterion;

	@Override
	public void setPreResolutionEvidence(EvidenceCase preresolutionEvidence) {
		this.preresolutionEvidence = preresolutionEvidence;
		
	}

	@Override
	public void setConditioningVariables(List<Variable> conditioningVariables) {
		throw new UnsupportedOperationException(
				"setConditioningVariables not implemented for " + getClass().getSimpleName());
	}
	
	/**
	 * @param probNet a network (usually containing decisions and utility nodes)
	 */
	public DecompositionGenerateDecisionTree(ProbNet probNet)  {
		 this(probNet,5);
	}
	
	
	/**
	 * @param probNet a network (usually containing decisions and utility nodes)
	 */
	public DecompositionGenerateDecisionTree(ProbNet probNet, int depth)  {
		 this.probNet = probNet.copy();
		 this.depth = depth;
        
        this.isUnicriterion = probNet.getInferenceOptions()
                                     .getMultiCriteriaOptions()
                                     .getMulticriteriaType() == MulticriteriaOptions.Type.UNICRITERION;
	}

	

	@Override
    public DecisionTreeNode getDecisionTree() throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		NetworkType networkType = probNet.getNetworkType();
						
		DecisionTreeComputation computation;
		
		boolean isInfluenceDiagram = networkType instanceof InfluenceDiagramType;
		
		if (isUnicriterion) {
			computation = isInfluenceDiagram?new IDDecisionTreeEvaluation(probNet,depth,true,preresolutionEvidence):
				new DANDecisionTreeEvaluation(probNet,depth,true,preresolutionEvidence);
		}
		else {
			computation = isInfluenceDiagram?new IDDecisionTreeCEA(probNet,depth,true,preresolutionEvidence):
				new DANDecisionTreeCEA(probNet,depth,true,preresolutionEvidence);
		}
		 			
		
		return computation.getDecisionTree();
	}

}
