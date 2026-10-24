/*
* Copyright (c) CISIAD, UNED, Spain, 2024. Licensed under the GPLv3 licence
* Unless required by applicable law or agreed to in writing,
* this code is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.inference.decisiontree.operation;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.DecisionTreeVisualNode;
import org.openmarkov.core.model.decisiontree.operation.DecisionTreeManager;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.inference.tasks.GenerateDecisionTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DecisionTreeManager using Inference algorithms.
 */
public class DecisionTreeManagerImpl implements DecisionTreeManager {

    private final GenerateDecisionTreeTaskFactory taskFactory;

    public DecisionTreeManagerImpl() {
        this(new DecisionTreeExpansionTaskFactory());
    }

    public DecisionTreeManagerImpl(GenerateDecisionTreeTaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    @Override
    public DecisionTreeVisualNode buildDecisionTree(ProbNet probNet, int depth)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        return buildDecisionTree(probNet, depth, new EvidenceCase());
    }

    @Override
    public DecisionTreeVisualNode buildDecisionTree(ProbNet probNet, int depth, EvidenceCase branchEvidence)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        NetworkType networkType = probNet.getNetworkType();
        if (!(networkType instanceof InfluenceDiagramType || networkType instanceof DecisionAnalysisNetworkType)) {
            // A decision tree only makes sense for a decision model. This used to
            // return null, which travelled to the caller and failed far from here.
            throw new NotEvaluableNetworkException.NotApplicableNetwork(probNet,
                    List.of(InfluenceDiagramType.getUniqueInstance(),
                            DecisionAnalysisNetworkType.getUniqueInstance()));
        }
        DecisionTreeBranch root = new DecisionTreeBranch();
        GenerateDecisionTree genDT = taskFactory.createTask(probNet, depth);
        genDT.setPreResolutionEvidence(branchEvidence);
        root.setChild(genDT.getDecisionTree());
        return root;
    }

    /**
     * Expands each leaf the tree can still go on from by the given number of
     * levels: the leaf is replaced by the tree built from its own network and
     * evidence, whose root is the variable of the leaf.
     */
    @Override
    public void expandLevels(DecisionTreeVisualNode root, int levels)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        for (DecisionTreeNode leaf : leavesThatCanBeExpanded(root, new ArrayList<>())) {
            DecisionTreeBranch subtree = (DecisionTreeBranch) buildDecisionTree(leaf.getNetwork(), levels,
                                                                                leaf.getEvidence());
            leaf.getParentBranch().setChild(subtree.getChild());
        }
    }

    private static List<DecisionTreeNode> leavesThatCanBeExpanded(DecisionTreeVisualNode element,
                                                                 List<DecisionTreeNode> leaves) {
        if (element instanceof DecisionTreeNode node && node.canBeExpanded()) {
            leaves.add(node);
        }
        for (DecisionTreeVisualNode child : element.getChildren()) {
            leavesThatCanBeExpanded(child, leaves);
        }
        return leaves;
    }
}
