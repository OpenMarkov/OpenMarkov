/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * A numeric chance node whose relation is an exact distribution, pasted without its parent, keeps the value it had
 * with that parent in its first state.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class PastingAnExactDistributionWithoutItsParentKeepsItsValuesTest {

    @Test void theValueWithTheParentInItsFirstStateIsKept() throws Exception {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Node a = probNet.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);
        Node n = probNet.addNode(new Variable("N"), NodeType.CHANCE);
        probNet.addLink(a, n, true);
        n.setPotential(new ExactDistrPotential(List.of(n.getVariable(), a.getVariable()),
                                               PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 5, 7 }));

        new PasteEdit(probNet, new SelectedContent(List.of(n), List.of()), null).executeEdit();

        Potential pasted = probNet.getNode("N'").getPotential();
        assertInstanceOf(ExactDistrPotential.class, pasted);
        assertEquals(List.of(probNet.getVariable("N'")), pasted.getVariables());
        assertArrayEquals(new double[] { 5 }, pasted.getCPT().getValues());
    }
}
