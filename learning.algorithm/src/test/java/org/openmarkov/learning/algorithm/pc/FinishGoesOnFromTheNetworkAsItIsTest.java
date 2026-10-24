/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.pc;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.OrientLinkEdit;
import org.openmarkov.core.action.core.COrientLinksEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.algorithm.pc.independencetester.ANMCausalDirectionTester;
import org.openmarkov.learning.algorithm.pc.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.util.LearningEditProposal;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Finish» of the interactive learning with PC goes on from the network as the user has left it: it keeps the
 * orientations applied from the table and the links removed by hand, and right after opening it learns the network of
 * the automatic learning.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class FinishGoesOnFromTheNetworkAsItIsTest {

    private LearningManager manager;

    /** PC as the learning window builds it by default, with the causal direction test. */
    private PCAlgorithm open() throws Exception {
        List<Variable> variables = new ArrayList<>();
        var database = PCAlgorithmIntegrationTest.loadCsvDatabase("network/BN-asia10k.csv", variables);
        manager = new LearningManager(database, LearningAlgorithm.class, null, null);
        PCAlgorithm pc = new PCAlgorithm(manager.getLearnedNet(), database, 0.5, new CrossEntropyIndependenceTester(),
                                         0.05, new ANMCausalDirectionTester());
        manager.init(pc);
        return pc;
    }

    /** What the dialog does after each change: the best edit, and then the next ones, up to 50. */
    private static List<LearningEditProposal> table(PCAlgorithm pc) {
        List<LearningEditProposal> table = new ArrayList<>();
        LearningEditProposal proposal = pc.getBestEdit(true, true);
        while (table.size() < 50 && proposal != null) {
            table.add(proposal);
            proposal = pc.getNextEdit(true, true);
        }
        return table;
    }

    private static boolean hasLink(ProbNet net, String from, String to, boolean directed) {
        Node nodeFrom = net.getNode(from), nodeTo = net.getNode(to);
        return net.getLinks().stream().anyMatch(link -> link.isDirected() == directed
                && (link.getFrom() == nodeFrom && link.getTo() == nodeTo
                    || !directed && link.getFrom() == nodeTo && link.getTo() == nodeFrom));
    }

    private static List<String> links(ProbNet net) {
        List<String> links = new ArrayList<>();
        net.getLinks().forEach(link -> links.add(link.getFrom().getName() + (link.isDirected() ? "->" : "--")
                                                         + link.getTo().getName()));
        links.sort(null);
        return links;
    }

    @Test void rightAfterOpeningItLearnsTheNetworkOfTheAutomaticLearning() throws Exception {
        open();
        manager.learn();
        List<String> automatic = links(manager.getLearnedNet());

        open();
        manager.learnFromTheCurrentState();

        assertEquals(automatic, links(manager.getLearnedNet()));
    }

    @Test void itKeepsAnOrientationAppliedFromTheTable() throws Exception {
        open();
        manager.learn();
        List<String> automatic = links(manager.getLearnedNet());

        PCAlgorithm pc = open();
        List<LearningEditProposal> table = table(pc);
        while (!table.isEmpty() && (table.getFirst().getEdit() instanceof RemoveLinkEdit
                || table.getFirst().getEdit() instanceof COrientLinksEdit)) {
            table.getFirst().getEdit().executeEdit();
            table = table(pc);
        }
        OrientLinkEdit againstTheAutomatic = table.stream()
                .map(LearningEditProposal::getEdit)
                .filter(edit -> edit instanceof OrientLinkEdit orientation && automatic.contains(
                        orientation.getVariableTo().getName() + "->" + orientation.getVariableFrom().getName()))
                .map(edit -> (OrientLinkEdit) edit)
                .findFirst().orElse(null);
        assertNotNull(againstTheAutomatic, table.toString());
        againstTheAutomatic.executeEdit();

        manager.learnFromTheCurrentState();

        String from = againstTheAutomatic.getVariableFrom().getName(), to = againstTheAutomatic.getVariableTo().getName();
        assertTrue(hasLink(manager.getLearnedNet(), from, to, true), from + "->" + to + " in " + links(manager.getLearnedNet()));
    }

    @Test void aLinkRemovedByHandDoesNotComeBack() throws Exception {
        open();
        ProbNet net = manager.getLearnedNet();
        new RemoveLinkEdit(net, net.getVariable("Smoker"), net.getVariable("LungCancer"), false).executeEdit();

        manager.learnFromTheCurrentState();

        assertFalse(hasLink(net, "Smoker", "LungCancer", true) || hasLink(net, "LungCancer", "Smoker", true)
                            || hasLink(net, "Smoker", "LungCancer", false), links(net).toString());
    }
}
