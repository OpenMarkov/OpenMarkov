package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.exception.*;

public interface GenerateDecisionTree extends Task {
	
	/**
	 * @return the decision tree
	 * @throws NotEvaluableNetworkException NotEvaluableNetworkException
	 */
    DecisionTreeNode getDecisionTree() throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates;

}
