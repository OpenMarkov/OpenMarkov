package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class FactoryDecisionTree {

	
	public static DecisionTreeNode createInstanceDecisionTreeNode(boolean isCEA, Node node, ProbNet dan) {
		return !isCEA?new EvaluationDecisionTreeNode(node,dan):new CEADecisionTreeNode(node,dan);
	}

	public static DecisionTreeNode createInstanceDecisionTreeNode(boolean isCEA, Node node) {
		return !isCEA?new EvaluationDecisionTreeNode(node):new CEADecisionTreeNode(node);
	}

	public static DecisionTreeNode createInstanceDecisionTreeNode(boolean isCEA, Variable variable,
			ProbNet dan) {
		return !isCEA?new EvaluationDecisionTreeNode(variable,dan):new CEADecisionTreeNode(variable,dan);
	}
}
