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
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.operation.DecisionTreeManager;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.inference.tasks.GenerateDecisionTree;

/**
 * Implementation of DecisionTreeManager using Inference algorithms.
 */
public class DecisionTreeManagerImpl implements DecisionTreeManager {

    private final GenerateDecisionTreeTaskFactory taskFactory;

    public DecisionTreeManagerImpl() {
        this(new DecompositionGenerateDecisionTreeTaskFactory());
    }

    public DecisionTreeManagerImpl(GenerateDecisionTreeTaskFactory taskFactory) {
        this.taskFactory = taskFactory;
    }

    @Override
    public DecisionTreeElement buildDecisionTree(ProbNet probNet, int depth)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        return buildDecisionTree(probNet, depth, new EvidenceCase());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DecisionTreeElement buildDecisionTree(ProbNet probNet, int depth, EvidenceCase branchEvidence)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        // The branch's utility type is unknown at this layer (it depends on the
        // factory: Double for plain evaluation, CEP for CEA). Phase 4 will type
        // the factory chain end-to-end.
        DecisionTreeBranch root = null;
        NetworkType networkType = probNet.getNetworkType();
        if (networkType instanceof InfluenceDiagramType || networkType instanceof DecisionAnalysisNetworkType) {
            root = new DecisionTreeBranch(probNet);
            GenerateDecisionTree genDT = taskFactory.createTask(probNet, depth);
            genDT.setPreResolutionEvidence(branchEvidence);
            root.setChild(genDT.getDecisionTree());
        }
        return root;
    }

    @Override
    public void expandLevels(DecisionTreeElement root, int levels)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        inferenceExpandLevels(root, null, levels, new EvidenceCase());
    }

    /**
     * Recursive expansion logic moved from DecisionTreePanel.
     *
     * <p>Uses raw {@link DecisionTreeNode} / {@link DecisionTreeBranch} because the
     * utility type carried by the tree is unknown at this layer. Phase 4 of the
     * decisiontree refactor will type the factory chain end-to-end.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void inferenceExpandLevels(DecisionTreeElement root,
            DecisionTreeNode parent, int n, EvidenceCase branchEvidence)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        if (root instanceof DecisionTreeBranch || ((DecisionTreeNode) root).getNodeType() != NodeType.UTILITY) {
            if (root instanceof DecisionTreeNode) {
                parent = (DecisionTreeNode) root;
            }
            for (DecisionTreeElement branch : root.getChildren()) {
                EvidenceCase newEvi;
                if (root instanceof DecisionTreeBranch) {
                    newEvi = createEvidenceBranchPath(branchEvidence, (DecisionTreeBranch<?>) root);
                } else {
                    newEvi = branchEvidence;
                }
                inferenceExpandLevels(branch, parent, n, newEvi);
            }
        } else {
            DecisionTreeNode rootDT = (DecisionTreeNode) root;
            // Recursively build the next part of the tree
            DecisionTreeElement newSubTree = buildDecisionTree(rootDT.getNetwork(), n, branchEvidence);
            if (newSubTree instanceof DecisionTreeBranch) {
                DecisionTreeNode auxRoot = ((DecisionTreeBranch) newSubTree).getChild();
                if (parent != null) {
                    if (parent.getNodeType() == NodeType.DECISION
                            || (!(parent.getVariable().getName().equalsIgnoreCase(auxRoot.getVariable().getName())))) {
                        rootDT.copy(auxRoot);
                    }
                }
            }
        }
    }

    private EvidenceCase createEvidenceBranchPath(EvidenceCase branchEvidence, DecisionTreeBranch<?> branch) {
        EvidenceCase newEvi = new EvidenceCase(branchEvidence);
        if (branch != null) {
            Variable branchVariable = branch.getBranchVariable();
            if (branchVariable != null
                    && (!branchVariable.getName().equalsIgnoreCase("OD"))
                    && !newEvi.contains(branchVariable)) {
                try {
                    newEvi.addFinding(new Finding(branchVariable, branch.getBranchState()));
                } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther e) {
                    throw new UnreachableException(e);
                }
            }
        }
        return newEvi;
    }
}
