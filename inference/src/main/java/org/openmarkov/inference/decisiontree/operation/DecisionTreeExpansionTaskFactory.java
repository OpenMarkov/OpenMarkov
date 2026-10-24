/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.decisiontree.operation;

import org.openmarkov.core.inference.tasks.GenerateDecisionTree;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.DecisionTreeExpansionGenerateDecisionTree;

/**
 * Produces the tasks that build the decision tree with the algorithm agreed with F. J. Díez.
 *
 * @author Manuel Arias
 */
public class DecisionTreeExpansionTaskFactory implements GenerateDecisionTreeTaskFactory {

    @Override
    public GenerateDecisionTree createTask(ProbNet probNet, int depth) {
        return new DecisionTreeExpansionGenerateDecisionTree(probNet, depth);
    }
}
