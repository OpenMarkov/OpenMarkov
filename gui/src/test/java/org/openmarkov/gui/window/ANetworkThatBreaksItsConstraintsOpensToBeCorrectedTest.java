/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.action.ChangeNodeTypeEdit;
import org.openmarkov.gui.action.PasteEdit;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;
import org.openmarkov.gui.window.edition.SelectedContent;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file may hold a network that breaks the constraints of its type, a cycle in a Bayesian network say. It opened
 * without a warning (error 37 of the pending list), and then pasting, changing the type of a node and inverting a link
 * were always refused, with a message about the cycle that was already there (error 31). Now it opens with a warning
 * that says where it breaks them, so that it can be corrected, and an edit is refused only for what it breaks.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANetworkThatBreaksItsConstraintsOpensToBeCorrectedTest {

    @TempDir Path folder;

    /** A network of this type with the cycle A → B → A, which no edit makes, and the link C → D. */
    static ProbNet withACycle(NetworkType type) {
        ProbNet net = new ProbNet(type);
        net.setName("cycle");
        for (String name : List.of("A", "B", "C", "D")) {
            net.addNode(new Variable(name, "no", "yes"), NodeType.CHANCE);
        }
        net.addLink(net.getVariable("A"), net.getVariable("B"), true);
        net.addLink(net.getVariable("B"), net.getVariable("A"), true);
        net.addLink(net.getVariable("C"), net.getVariable("D"), true);
        for (Node node : net.getNodes()) {
            List<Variable> variables = new ArrayList<>();
            variables.add(node.getVariable());
            node.getParents().forEach(parent -> variables.add(parent.getVariable()));
            node.setPotential(new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY));
        }
        return net;
    }

    private static SelectedContent aLonelyNodeZ() {
        ProbNet other = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable z = new Variable("Z", "no", "yes");
        other.addNode(z, NodeType.CHANCE).setPotential(new TablePotential(List.of(z), PotentialRole.CONDITIONAL_PROBABILITY));
        VisualNetwork visualNetwork = new VisualNetwork(other, null);
        EditorPanelClipboardAssistant clipboard = new EditorPanelClipboardAssistant();
        visualNetwork.setSelectedAllObjects(true);
        visualNetwork.exportToClipboard(false, clipboard);
        return clipboard.paste();
    }

    @Test void readFromAFileItSaysWhereItBreaksThem() throws Exception {
        Path file = folder.resolve("cycle.pgmx");
        new PGMXWriter_1_0().write(file.toString(), withACycle(BayesianNetworkType.getUniqueInstance()), List.of());
        ProbNet read = new PGMXReader().read(file.toUri().toURL()).probNet();

        List<String> violations = NetworkFileHandler.constraintsNotMet(read);

        // One for each link of the cycle, by the names of its nodes
        assertEquals(2, violations.size(), violations.toString());
        assertTrue(violations.get(0).contains("The link A → B closes a cycle."), violations.get(0));
        assertTrue(violations.get(1).contains("The link B → A closes a cycle."), violations.get(1));
    }

    @Test void aNodeIsPastedInIt() throws Exception {
        ProbNet net = withACycle(BayesianNetworkType.getUniqueInstance());

        new PasteEdit(net, aLonelyNodeZ(), null).executeEdit();

        assertNotNull(net.getVariable("Z"));
    }

    @Test void anotherLinkIsInverted() throws Exception {
        ProbNet net = withACycle(BayesianNetworkType.getUniqueInstance());

        new InvertLinkEdit(net, net.getVariable("C"), net.getVariable("D"), true).executeEdit();

        assertNotNull(net.getLink(net.getNode("D"), net.getNode("C"), true));
    }

    @Test void aNodeChangesItsType() throws Exception {
        ProbNet net = withACycle(InfluenceDiagramType.getUniqueInstance());

        new ChangeNodeTypeEdit(net.getNode("C"), NodeType.DECISION).executeEdit();

        assertEquals(NodeType.DECISION, net.getNode("C").getNodeType());
    }

    @Test void whatAnEditBreaksIsStillRefused() {
        ProbNet net = withACycle(BayesianNetworkType.getUniqueInstance());

        Exception refusal = assertThrows(Exception.class,
                                         () -> new ChangeNodeTypeEdit(net.getNode("C"), NodeType.DECISION).executeEdit());

        List<Throwable> causes = new ArrayList<>();
        for (Throwable cause = refusal; cause != null; cause = cause.getCause()) {
            causes.add(cause);
        }
        assertTrue(causes.stream().anyMatch(ConstraintViolatedException.OnlyChanceNodesAllowed.class::isInstance),
                   causes.toString());
        assertFalse(causes.stream().anyMatch(ConstraintViolatedException.ThereIsACycle.class::isInstance),
                    causes.toString());
        assertEquals(NodeType.CHANCE, net.getNode("C").getNodeType());
    }
}
