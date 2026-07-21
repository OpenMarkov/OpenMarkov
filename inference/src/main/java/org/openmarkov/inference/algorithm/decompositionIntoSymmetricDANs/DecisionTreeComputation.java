package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs;

import org.openmarkov.core.model.decisiontree.DecisionTreeNode;

/**
 * Interface for inference algorithms that produce a decision tree as their output.
 */
public interface DecisionTreeComputation {

    /**
     * Returns the computed decision tree.
     *
     * @return the root node of the decision tree
     */
    DecisionTreeNode getDecisionTree();

}
