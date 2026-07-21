/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InvertLinkEdit}.
 *
 * @author Manuel Arias
 */
class InvertLinkEditTest {

    /**
     * Regression test for B-InvertDec. When the child of the inverted link is a DECISION node,
     * {@code doEdit()} skips the potential updates (guarded by {@code child.getNodeType() != DECISION}),
     * so {@code parentOldPotentials} / {@code childOldPotentials} are never assigned and stay null.
     * {@code undo()} used to call {@code setPotentials} with those null lists unconditionally, which
     * wiped the parent's potentials to an empty list.
     * <p>
     * Here C (chance) -> D (decision) is inverted and then undone; C must keep its potential.
     */
    @Test void undoOfInversionIntoDecisionNodeKeepsParentPotential() throws DoEditException {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        Variable c = new Variable("C", 2);
        Variable d = new Variable("D", 2);
        new AddNodeEdit(net, c, NodeType.CHANCE, null).executeEdit();
        new AddNodeEdit(net, d, NodeType.DECISION, null).executeEdit();
        new AddLinkEdit(net, c, d, true).executeEdit();

        Node nodeC = net.getNode(c);
        Potential cPotentialBefore = nodeC.getPotentials().getFirst();
        assertThat(nodeC.getPotentials()).hasSize(1);

        InvertLinkEdit edit = new InvertLinkEdit(net, c, d, true);
        edit.executeEdit();
        edit.undo();

        assertThat(nodeC.getPotentials())
                .as("undoing an inversion whose child is a decision node must not wipe the parent's potentials")
                .hasSize(1);
        assertThat(nodeC.getPotentials().getFirst()).isSameAs(cPotentialBefore);
    }
}
