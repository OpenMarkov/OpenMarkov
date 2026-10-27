/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.core.RevelationStateEdit;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Drawing a link, restricting it or giving it a revealing state, undoing both and redoing both left the link without
 * the restriction or the revelation: redoing the drawing makes another link, and the edits wrote in the old one (error
 * 27 of the pending list).
 *
 * @author Manuel Arias
 */
class ARedoneLinkKeepsItsRestrictionAndItsRevelationTest {

    private ProbNet probNet;
    private Variable a;
    private Variable b;

    private Link<Node> drawTheLinkIn(NetworkType networkType) throws Exception {
        probNet = new ProbNet(networkType);
        a = new Variable("A", "a1", "a2");
        b = new Variable("B", "b1", "b2");
        probNet.addNode(a, NodeType.CHANCE);
        probNet.addNode(b, NodeType.CHANCE);
        probNet.getPNESupport().setWithUndo(true);
        new AddLinkEdit(probNet, a, b, true).executeEdit();
        return probNet.getLink(probNet.getNode(a), probNet.getNode(b), true);
    }

    private Link<Node> undoAndRedoBoth() {
        probNet.getPNESupport().undo();
        probNet.getPNESupport().undo();
        probNet.getPNESupport().redo();
        probNet.getPNESupport().redo();
        return probNet.getLink(probNet.getNode(a), probNet.getNode(b), true);
    }

    @Test void theRestrictionIsWrittenInTheLinkOfTheNetwork() throws Exception {
        Link<Node> link = drawTheLinkIn(BayesianNetworkType.getUniqueInstance());
        LinkOperations.initializesRestrictionsPotential(link);
        // A = a1 is incompatible with B = b2: column 1, row 1
        new LinkRestrictionPotentialValueEdit(link, 0, 1, 1).executeEdit();

        assertArrayEquals(new double[]{ 1, 1, 0, 1 }, undoAndRedoBoth().getRestrictionsPotential().getValues());
    }

    @Test void theRevelationIsWrittenInTheLinkOfTheNetwork() throws Exception {
        Link<Node> link = drawTheLinkIn(DecisionAnalysisNetworkType.getUniqueInstance());
        new RevelationStateEdit(link, a.getStates()[1], true).executeEdit();

        assertEquals(List.of(a.getStates()[1]), undoAndRedoBoth().getRevealingStates());
    }
}
