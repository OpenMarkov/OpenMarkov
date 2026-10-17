/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * A numeric chance node whose relation is an exact distribution keeps, when it loses a parent, the value it had with
 * that parent in its first state, also when that value is zero, as in a new exact distribution.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class RemovingAParentOfAnExactDistributionKeepsItsValuesTest {

    private ProbNet net;
    private Variable a;
    private Variable n;
    private Node nodeN;

    /** A → N, with N numeric and N(A) = (first, 7). */
    private void aNetwork(double first) {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        a = new Variable("A", "a0", "a1");
        n = new Variable("N");
        net.addNode(a, NodeType.CHANCE);
        nodeN = net.addNode(n, NodeType.CHANCE);
        net.addLink(a, n, true);
        nodeN.setPotential(new ExactDistrPotential(List.of(n, a), PotentialRole.CONDITIONAL_PROBABILITY,
                                                   new double[] { first, 7 }));
    }

    @ParameterizedTest(name = "N(A) = ({0}, 7)")
    @ValueSource(doubles = { 5, 0 })
    void theValueWithTheParentInItsFirstStateIsKept(double first) throws Exception {
        aNetwork(first);

        new RemoveLinkEdit(net, a, n, true).executeEdit();

        assertInstanceOf(ExactDistrPotential.class, nodeN.getPotential());
        assertEquals(List.of(n), nodeN.getPotential().getVariables());
        assertArrayEquals(new double[] { first }, nodeN.getPotential().getCPT().getValues());
    }

    @Test void undoingGivesBackTheRelation() throws Exception {
        aNetwork(5);
        net.getPNESupport().setWithUndo(true);

        new RemoveLinkEdit(net, a, n, true).executeEdit();
        net.getPNESupport().undo();

        assertArrayEquals(new double[] { 5, 7 }, nodeN.getPotential().getCPT().getValues());
    }
}
