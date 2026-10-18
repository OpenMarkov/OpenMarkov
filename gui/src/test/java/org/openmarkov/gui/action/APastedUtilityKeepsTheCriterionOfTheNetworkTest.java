/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.DecisionCriteriaEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * An influence diagram with a decision D and a utility «Cost» judged by the criterion «Cost». The copy of «Cost» pasted
 * in the same network is judged by that very criterion, so a later change of its name reaches the pasted utility, also
 * in the saved file (error 295).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APastedUtilityKeepsTheCriterionOfTheNetworkTest {

    @TempDir Path folder;

    @Test void thePastedUtilityIsJudgedByTheCriterionOfTheNetwork() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Criterion cost = new Criterion("Cost", "€");
        net.setDecisionCriteria(new ArrayList<>(List.of(cost)));
        Variable d = new Variable("D", "no", "yes");
        Variable u = new Variable("Cost");
        u.setDecisionCriterion(cost);
        net.addNode(d, NodeType.DECISION);
        Node nodeU = net.addNode(u, NodeType.UTILITY);
        net.addLink(d, u, true);
        nodeU.setPotential(new TablePotential(List.of(u, d), PotentialRole.UNSPECIFIED, new double[] { 0, 100 }));

        VisualNetwork visualNetwork = new VisualNetwork(net, null);
        EditorPanelClipboardAssistant clipboard = new EditorPanelClipboardAssistant();
        visualNetwork.setSelectedAllObjects(true);
        visualNetwork.exportToClipboard(false, clipboard);
        new PasteEdit(net, clipboard.paste(), null).executeEdit();

        assertSame(cost, net.getVariable("Cost'").getDecisionCriterion());
        new DecisionCriteriaEdit(net, StateAction.RENAME, cost, "Coste").executeEdit();
        Path file = folder.resolve("saved.pgmx");
        new PGMXWriter_1_0().write(file.toString(), net, List.of());
        ProbNet opened = new PGMXReader().read(file.toUri().toURL()).probNet();
        assertEquals("Coste", opened.getVariable("Cost'").getDecisionCriterion().getCriterionName());
    }
}
