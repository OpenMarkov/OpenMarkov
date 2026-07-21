package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

public class EvaluationDecisionTreeNode extends DecisionTreeNode<Double> {

	// protected double utility = Double.NEGATIVE_INFINITY;

	public EvaluationDecisionTreeNode(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public EvaluationDecisionTreeNode(Node node, ProbNet dan) {
		super(node, dan);
	}

	public EvaluationDecisionTreeNode(Variable variable, ProbNet dan) {
		super(variable, dan);
	}

	@Override
	public boolean isBestDecision(DecisionTreeBranch<Double> branch) {
		boolean isBestDecision = false;
		if (nodeType == NodeType.DECISION) {
			isBestDecision = true;
			double thisUtility = branch.getUtility();
			for (DecisionTreeElement otherBranch : children) {
				@SuppressWarnings("unchecked")
				DecisionTreeBranch<Double> typedBranch = (DecisionTreeBranch<Double>) otherBranch;
				isBestDecision &= thisUtility >= typedBranch.getUtility();
			}
		}
		return isBestDecision;
	}

	@Override
	public void setOnlyValueForUtility(Potential tablePotential) {
		this.setUtility(DANOperations.getOnlyValuePotential((TablePotential) tablePotential));
	}

}
