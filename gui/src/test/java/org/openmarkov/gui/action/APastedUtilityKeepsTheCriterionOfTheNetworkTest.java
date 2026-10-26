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
import org.openmarkov.gui.window.edition.SelectedContent;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * An influence diagram with a decision D and a utility «Cost» judged by the criterion «Cost», in €. The copy of «Cost»
 * pasted in the same network is judged by that very criterion, so a later change of its name reaches the pasted
 * utility, also in the saved file (error 295). Pasted where no criterion has its name, the utility brings its criterion
 * into the network, and undoing the paste takes it out (error 312).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APastedUtilityKeepsTheCriterionOfTheNetworkTest {

    @TempDir Path folder;

    private final Criterion cost = new Criterion("Cost", "€");
    private final ProbNet net = networkWith(cost);

    /** An influence diagram with the decision D and, if {@code criterion} is «Cost», the utility Cost on D. */
    private static ProbNet networkWith(Criterion criterion) {
        ProbNet network = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        network.setDecisionCriteria(new ArrayList<>(List.of(criterion)));
        Variable d = new Variable("D", "no", "yes");
        network.addNode(d, NodeType.DECISION);
        if (criterion.getCriterionName().equals("Cost")) {
            Variable u = new Variable("Cost");
            u.setDecisionCriterion(criterion);
            Node nodeU = network.addNode(u, NodeType.UTILITY);
            network.addLink(d, u, true);
            nodeU.setPotential(new TablePotential(List.of(u, d), PotentialRole.UNSPECIFIED, new double[] { 0, 100 }));
        }
        return network;
    }

    private SelectedContent copyAll() {
        VisualNetwork visualNetwork = new VisualNetwork(net, null);
        EditorPanelClipboardAssistant clipboard = new EditorPanelClipboardAssistant();
        visualNetwork.setSelectedAllObjects(true);
        visualNetwork.exportToClipboard(false, clipboard);
        return clipboard.paste();
    }

    private ProbNet savedAndOpened(ProbNet network) throws Exception {
        Path file = folder.resolve("saved.pgmx");
        new PGMXWriter_1_0().write(file.toString(), network, List.of());
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    private static List<String> criteria(ProbNet network) {
        return network.getDecisionCriteria().stream().map(Criterion::getCriterionName).toList();
    }

    @Test void thePastedUtilityIsJudgedByTheCriterionOfTheNetwork() throws Exception {
        new PasteEdit(net, copyAll(), null).executeEdit();

        assertSame(cost, net.getVariable("Cost'").getDecisionCriterion());
        new DecisionCriteriaEdit(net, StateAction.RENAME, cost, "Coste").executeEdit();
        assertEquals("Coste", savedAndOpened(net).getVariable("Cost'").getDecisionCriterion().getCriterionName());
    }

    @Test void pastedInANetworkWithoutItsCriterionTheUtilityBringsIt() throws Exception {
        ProbNet other = networkWith(new Criterion("Effectiveness", "QALY"));
        SelectedContent copied = copyAll();

        PasteEdit paste = new PasteEdit(other, copied, null);
        paste.executeEdit();

        Criterion pasted = other.getVariable("Cost").getDecisionCriterion();
        assertSame(other.getDecisionCriteria().get(1), pasted);
        assertEquals(List.of("Effectiveness", "Cost"), criteria(other));
        assertEquals("€", pasted.getCriterionUnit());
        assertEquals("Cost", savedAndOpened(other).getVariable("Cost").getDecisionCriterion().getCriterionName());
        paste.undo();
        assertEquals(List.of("Effectiveness"), criteria(other));
    }

    @Test void aCriterionRenamedBetweenCopyAndPasteComesBackWithItsOldName() throws Exception {
        SelectedContent copied = copyAll();
        new DecisionCriteriaEdit(net, StateAction.RENAME, cost, "Coste").executeEdit();

        new PasteEdit(net, copied, null).executeEdit();

        assertEquals(List.of("Coste", "Cost"), criteria(net));
        assertSame(net.getDecisionCriteria().get(1), net.getVariable("Cost'").getDecisionCriterion());
    }

    @Test void theNameOfTheCriterionIsComparedIgnoringCase() throws Exception {
        Criterion lowerCase = new Criterion("cost", "€");
        ProbNet other = networkWith(lowerCase);
        SelectedContent copied = copyAll();

        new PasteEdit(other, copied, null).executeEdit();

        assertEquals(List.of("cost"), criteria(other));
        assertSame(lowerCase, other.getVariable("Cost").getDecisionCriterion());
    }
}
