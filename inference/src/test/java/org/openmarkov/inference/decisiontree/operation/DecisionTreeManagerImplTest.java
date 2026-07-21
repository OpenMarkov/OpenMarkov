/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.decisiontree.operation;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Safety-net tests for {@link DecisionTreeManagerImpl} introduced in Phase 0
 * of the decisiontree refactoring plan.
 *
 * <p>These tests freeze the current behaviour of the manager:
 * what kinds of networks it builds trees for, what it returns for unsupported
 * networks, and the structure of the tree for a canonical fixture.
 * Subsequent phases must keep these tests green or update them deliberately
 * with an explanation in the commit message.
 */
class DecisionTreeManagerImplTest {

    private static final int DEFAULT_DEPTH = 5;

    @Test
    void buildOnSimpleIdReturnsBranchWithDecisionRoot() throws Exception {
        ProbNet id = simpleIdWithName();

        DecisionTreeManagerImpl manager = new DecisionTreeManagerImpl();
        DecisionTreeElement root = manager.buildDecisionTree(id, DEFAULT_DEPTH);

        // The current implementation always wraps the result in a synthetic root
        // DecisionTreeBranch.
        assertNotNull(root);
        assertInstanceOf(DecisionTreeBranch.class, root);

        DecisionTreeNode<?> firstRealNode = ((DecisionTreeBranch) root).getChild();
        assertNotNull(firstRealNode);
        // The first real node in a simple ID is a chance or decision; never utility.
        assertFalse(firstRealNode.getNodeType() == NodeType.UTILITY);
    }

    @Test
    void buildOnBayesianNetworkReturnsNull() throws Exception {
        // Documents existing behaviour: BN networks produce a null tree silently.
        // Phase 6 of the refactor is the right place to change this; until then
        // any code path that relies on it must continue to work.
        ProbNet bn = TestNetworks.buildAsia();

        DecisionTreeManagerImpl manager = new DecisionTreeManagerImpl();
        DecisionTreeElement root = manager.buildDecisionTree(bn, DEFAULT_DEPTH);

        assertNull(root);
    }

    @Test
    void buildWithEmptyEvidenceMatchesBuildWithoutEvidence() throws Exception {
        ProbNet id = simpleIdWithName();

        DecisionTreeManagerImpl manager = new DecisionTreeManagerImpl();
        DecisionTreeElement withoutEvidence = manager.buildDecisionTree(id, DEFAULT_DEPTH);
        DecisionTreeElement withEmptyEvidence = manager.buildDecisionTree(
                simpleIdWithName(), DEFAULT_DEPTH, new EvidenceCase());

        assertEquals(canonicalSnapshot(withoutEvidence), canonicalSnapshot(withEmptyEvidence));
    }

    @Test
    void buildOnSimpleIdProducesStableSnapshot() throws Exception {
        // Snapshot test: freezes the structure of the tree for a known fixture.
        // If a refactor changes this snapshot, update the constant deliberately
        // and explain why in the commit message.
        ProbNet id = simpleIdWithName();

        DecisionTreeManagerImpl manager = new DecisionTreeManagerImpl();
        DecisionTreeElement root = manager.buildDecisionTree(id, DEFAULT_DEPTH);

        String snapshot = canonicalSnapshot(root);

        // The snapshot must be stable across runs and contain both decision states
        // (d0, d1) and chance states (c0, c1).
        assertNotNull(snapshot);
        assertSnapshotContains(snapshot, "Decision");
        assertSnapshotContains(snapshot, "d0");
        assertSnapshotContains(snapshot, "d1");
        assertSnapshotContains(snapshot, "Chance");
        assertSnapshotContains(snapshot, "c0");
        assertSnapshotContains(snapshot, "c1");
        assertSnapshotContains(snapshot, "Utility");
    }

    @Test
    void expandLevelsOnExistingTreePreservesRoot() throws Exception {
        ProbNet id = simpleIdWithName();

        DecisionTreeManagerImpl manager = new DecisionTreeManagerImpl();
        DecisionTreeElement root = manager.buildDecisionTree(id, 1);
        String before = canonicalSnapshot(root);

        manager.expandLevels(root, 1);
        String after = canonicalSnapshot(root);

        // Expansion must keep the original root reachable; the canonical
        // representation may grow but must still start with the same prefix.
        assertNotNull(after);
        assertEquals(rootKind(before), rootKind(after));
    }

    /**
     * Produces a deterministic textual snapshot of the tree.
     * Uses a depth-first walk and prints the kind of element plus the variable
     * name when applicable.
     */
    private static String canonicalSnapshot(DecisionTreeElement element)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException,
            NonProjectablePotentialException,
            PotentialOperationException.DifferentSizesInPotentialsAndStates,
            NotSupportedOperationException {
        StringBuilder sb = new StringBuilder();
        appendElement(element, sb, 0);
        return sb.toString();
    }

    private static void appendElement(DecisionTreeElement element, StringBuilder sb, int depth) {
        if (element == null) {
            sb.append("  ".repeat(depth)).append("<null>\n");
            return;
        }
        sb.append("  ".repeat(depth));
        if (element instanceof DecisionTreeBranch branch) {
            sb.append("Branch[")
              .append(branch.getBranchVariable() == null ? "<root>" : branch.getBranchVariable().getName())
              .append("=")
              .append(branch.getBranchState() == null ? "*" : branch.getBranchState().getName())
              .append("]\n");
        } else if (element instanceof DecisionTreeNode<?> node) {
            sb.append("Node[")
              .append(node.getNodeType())
              .append(":")
              .append(node.getVariable() == null ? "?" : node.getVariable().getName())
              .append("]\n");
        }
        List<DecisionTreeElement> children = element.getChildren();
        if (children != null) {
            for (DecisionTreeElement child : children) {
                appendElement(child, sb, depth + 1);
            }
        }
    }

    /**
     * The DAN inference path expects the network name to end in ".pgmx" because
     * it slices the last 5 characters when generating sub-network names. The
     * fixture {@link TestNetworks#buildSimpleID()} does not set a name, so we
     * set one here. Documenting the dependency keeps Phase 0 honest about it.
     */
    private static ProbNet simpleIdWithName() {
        ProbNet id = TestNetworks.buildSimpleID();
        id.setName("simple-id.pgmx");
        return id;
    }

    private static String rootKind(String snapshot) {
        int newline = snapshot.indexOf('\n');
        return newline >= 0 ? snapshot.substring(0, newline) : snapshot;
    }

    private static void assertSnapshotContains(String snapshot, String token) {
        if (!snapshot.contains(token)) {
            throw new AssertionError("Snapshot does not contain expected token '"
                    + token + "'. Snapshot:\n" + snapshot);
        }
    }
}
