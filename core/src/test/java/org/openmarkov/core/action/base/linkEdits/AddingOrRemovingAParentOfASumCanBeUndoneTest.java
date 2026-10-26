/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * An influence diagram with A, V and U, utility nodes, U being a sum. Removing or adding a parent of
 * U gives it a new sum, so that undoing the edit gives back the sum it had (error 296).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AddingOrRemovingAParentOfASumCanBeUndoneTest {

    private ProbNet net;
    private Variable a;
    private Variable v;
    private Variable u;
    private Node nodeU;

    @BeforeEach void setUp() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        a = new Variable("A");
        v = new Variable("V");
        u = new Variable("U");
        net.addNode(a, NodeType.UTILITY);
        net.addNode(v, NodeType.UTILITY);
        nodeU = net.addNode(u, NodeType.UTILITY);
        net.addLink(v, u, true);
        net.getPNESupport().setWithUndo(true);
    }

    @Test void removingTheSummedUtilityAndUndoingGivesBackTheWholeSum() throws Exception {
        net.addLink(a, u, true);
        SumPotential sum = new SumPotential(List.of(u, v, a), PotentialRole.UNSPECIFIED);
        sum.setComment("a note");
        nodeU.setPotential(sum);

        new RemoveLinkEdit(net, v, u, true).executeEdit();
        assertEquals(List.of(u, a), nodeU.getPotential().getVariables());
        assertEquals("a note", nodeU.getPotential().getComment());
        net.getPNESupport().undo();

        assertInstanceOf(SumPotential.class, nodeU.getPotential());
        assertEquals(List.of(u, v, a), nodeU.getPotential().getVariables());
    }

    @Test void addingAParentAndUndoingGivesBackTheSum() throws Exception {
        nodeU.setPotential(new SumPotential(List.of(u, v), PotentialRole.UNSPECIFIED));

        new AddLinkEdit(net, a, u, true).executeEdit();
        assertEquals(List.of(u, v, a), nodeU.getPotential().getVariables());
        net.getPNESupport().undo();

        assertInstanceOf(SumPotential.class, nodeU.getPotential());
        assertEquals(List.of(u, v), nodeU.getPotential().getVariables());
    }
}
