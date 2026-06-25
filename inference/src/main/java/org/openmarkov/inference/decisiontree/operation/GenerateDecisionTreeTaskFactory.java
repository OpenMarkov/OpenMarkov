package org.openmarkov.inference.decisiontree.operation;

import org.openmarkov.core.inference.tasks.GenerateDecisionTree;
import org.openmarkov.core.model.network.ProbNet;

/**
 * Factory interface for creating Decision Tree generation tasks.
 * Decouples the Manager from specific algorithms.
 */
public interface GenerateDecisionTreeTaskFactory {
    GenerateDecisionTree createTask(ProbNet probNet, int depth);
}
