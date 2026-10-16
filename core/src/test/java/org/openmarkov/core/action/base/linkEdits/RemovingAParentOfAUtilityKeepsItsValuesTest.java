/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A utility node that loses a parent keeps the values it had with that parent in its first state, as a
 * chance node does, whether its relation is a table or an exact distribution.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class RemovingAParentOfAUtilityKeepsItsValuesTest {

    private ProbNet net;
    private Variable a;
    private Variable b;
    private Variable u;
    private Node nodeU;

    /** A → U, B → U, with U(A, B) = (0, 10, 20, 30), given by a table or by an exact distribution. */
    private void anInfluenceDiagram(boolean exact) {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        a = new Variable("A", "a0", "a1");
        b = new Variable("B", "b0", "b1");
        u = new Variable("U");
        net.addNode(a, NodeType.CHANCE);
        net.addNode(b, NodeType.CHANCE);
        nodeU = net.addNode(u, NodeType.UTILITY);
        Criterion criterion = new Criterion("Cost", "euro");
        net.setDecisionCriteria(new ArrayList<>(List.of(criterion)));
        u.setDecisionCriterion(criterion);
        net.addLink(a, u, true);
        net.addLink(b, u, true);
        double[] values = { 0, 10, 20, 30 };
        nodeU.setPotential(exact ? new ExactDistrPotential(List.of(u, a, b), PotentialRole.UNSPECIFIED, values)
                                 : new TablePotential(List.of(u, a, b), PotentialRole.UNSPECIFIED, values));
    }

    @ParameterizedTest(name = "exact distribution: {0}")
    @ValueSource(booleans = { false, true })
    void theValuesWithTheParentInItsFirstStateAreKept(boolean exact) throws Exception {
        anInfluenceDiagram(exact);
        Class<? extends Potential> kind = nodeU.getPotential().getClass();

        new RemoveLinkEdit(net, a, u, true).executeEdit();
        assertEquals(kind, nodeU.getPotential().getClass());
        assertEquals(List.of(u, b), nodeU.getPotential().getVariables());
        assertArrayEquals(new double[] { 0, 20 }, nodeU.getPotential().getCPT().getValues());

        new RemoveLinkEdit(net, b, u, true).executeEdit();
        assertArrayEquals(new double[] { 0 }, nodeU.getPotential().getCPT().getValues());
    }

    @ParameterizedTest(name = "exact distribution: {0}")
    @ValueSource(booleans = { false, true })
    void undoingGivesBackTheRelation(boolean exact) throws Exception {
        anInfluenceDiagram(exact);
        net.getPNESupport().setWithUndo(true);

        new RemoveLinkEdit(net, a, u, true).executeEdit();
        net.getPNESupport().undo();

        assertArrayEquals(new double[] { 0, 10, 20, 30 }, nodeU.getPotential().getCPT().getValues());
    }
}
