package org.openmarkov.inference.decisiontree.operation;

import org.openmarkov.core.inference.tasks.GenerateDecisionTree;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecompositionGenerateDecisionTree;

/**
 * Concrete factory that produces DecompositionGenerateDecisionTree tasks.
 */
public class DecompositionGenerateDecisionTreeTaskFactory implements GenerateDecisionTreeTaskFactory {

    @Override
    public GenerateDecisionTree createTask(ProbNet probNet, int depth) {
        return new DecompositionGenerateDecisionTree(probNet, depth);
    }
}
