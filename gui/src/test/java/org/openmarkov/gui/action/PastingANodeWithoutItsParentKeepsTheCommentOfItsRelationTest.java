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
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A node pasted without its parent keeps the comment and the properties of its relation.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class PastingANodeWithoutItsParentKeepsTheCommentOfItsRelationTest {

    @Test void theCommentAndThePropertiesStay() throws Exception {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Node a = probNet.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);
        Node c = probNet.addNode(new Variable("C", "c0", "c1"), NodeType.CHANCE);
        probNet.addLink(a, c, true);
        Potential table = new TablePotential(List.of(c.getVariable(), a.getVariable()),
                                             PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7, 0.6, 0.4 });
        table.setComment("a note");
        table.properties.put("key", "value");
        c.setPotential(table);

        new PasteEdit(probNet, new SelectedContent(List.of(c), List.of()), null).executeEdit();

        Potential pasted = probNet.getNode("C'").getPotential();
        assertEquals("a note", pasted.getComment());
        assertEquals("value", pasted.properties.get("key"));
    }
}
