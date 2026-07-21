/*
* Copyright (c) CISIAD, UNED, Spain, 2024. Licensed under the GPLv3 licence
* Unless required by applicable law or agreed to in writing,
* this code is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.decisiontree.operation;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

/**
 * Interface that defines the operations for building and expanding a decision
 * tree.
 * This separates the model/building logic from the view/GUI.
 */
public interface DecisionTreeManager {

    /**
     * Builds a decision tree for the given probabilistic network.
     * 
     * @param probNet The network.
     * @param depth   Maximum depth.
     * @return The root element of the tree.
     */
    DecisionTreeElement buildDecisionTree(ProbNet probNet, int depth)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException;

    /**
     * Builds a decision tree considering existing evidence (branch).
     * 
     * @param probNet        The network.
     * @param depth          Maximum depth.
     * @param branchEvidence Evidence accumulated in the branch.
     * @return The decision tree branch/root.
     */
    DecisionTreeElement buildDecisionTree(ProbNet probNet, int depth, EvidenceCase branchEvidence)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException;

    /**
     * Expands the decision tree by a specified number of levels.
     * 
     * @param root   The current root or branch to expand.
     * @param levels The number of levels to expand.
     */
    void expandLevels(DecisionTreeElement root, int levels)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException;
}
