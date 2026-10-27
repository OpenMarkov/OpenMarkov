/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.link;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.gui.component.ValuesTable;
import org.openmarkov.gui.configuration.UserPreference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the window of the restrictions of a link, marking the pair VisitToAsia = «no», Tuberculosis = «yes» as
 * incompatible puts that cell of the table of Tuberculosis at zero and spreads its column. That change was made
 * outside the history, so neither «Cancel» nor undo gave the table back (error 25 of the pending list), and the
 * click wrote the value in the table of VisitToAsia too (error 155). The network is the corner of asia.pgmx.
 *
 * @author Manuel Arias
 */
class MarkingAPairAsIncompatibleCanBeUndoneTest {

    private static final double[] TUBERCULOSIS = { 0.99, 0.01, 0.95, 0.05 };
    private static final double[] RESTRICTED = { 1.0, 0.0, 0.95, 0.05 };
    private static final double[] VISIT = { 0.99, 0.01 };

    private ProbNet probNet;
    private Node visitToAsia;
    private Node tuberculosis;
    private Link<Node> link;

    @BeforeEach void theLinkOfAsia() {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable visit = new Variable("VisitToAsia", "no", "yes");
        Variable tb = new Variable("Tuberculosis", "no", "yes");
        visitToAsia = probNet.addNode(visit, NodeType.CHANCE);
        tuberculosis = probNet.addNode(tb, NodeType.CHANCE);
        probNet.addLink(visitToAsia, tuberculosis, true);
        visitToAsia.setPotential(new TablePotential(List.of(visit), PotentialRole.CONDITIONAL_PROBABILITY, VISIT.clone()));
        tuberculosis.setPotential(new TablePotential(List.of(tb, visit), PotentialRole.CONDITIONAL_PROBABILITY,
                                                     TUBERCULOSIS.clone()));
        probNet.getPNESupport().setWithUndo(true);
        link = probNet.getLink(visitToAsia, tuberculosis, true);
        LinkOperations.initializesRestrictionsPotential(link);
    }

    private static double[] values(Node node) {
        return ((TablePotential) node.getPotential()).getValues();
    }

    /** Opens the window, as «Edit restrictions» does, and marks the pair; returns the table of the window. */
    private ValuesTable markTheVisitWithoutTuberculosis() {
        probNet.getPNESupport().openNewSubEditHistory();
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            ValuesTable table = new LinkRestrictionPanel(link).getLinkRestrictionValuesTable();
            // Column 1 is the first state of the parent, «no»; row 1, the last state of the child, «yes»
            table.setValueAt(0, 1, 1, null);
            return table;
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }

    @Test void theTableOfTheChildChangesAndThatOfTheParentDoesNot() {
        markTheVisitWithoutTuberculosis();

        assertArrayEquals(RESTRICTED, values(tuberculosis), 1e-12);
        assertArrayEquals(VISIT, values(visitToAsia), 1e-12);
    }

    @Test void cancelGivesTheTableBack() {
        markTheVisitWithoutTuberculosis();

        probNet.getPNESupport().cancelLastSubEditHistory();

        assertArrayEquals(TUBERCULOSIS, values(tuberculosis), 1e-12);
        assertFalse(probNet.getLink(visitToAsia, tuberculosis, true).hasRestrictions());
    }

    @Test void undoGivesTheTableBackAndRedoChangesItAgain() {
        ValuesTable table = markTheVisitWithoutTuberculosis();
        probNet.getPNESupport().closeSubEditHistory();

        probNet.getPNESupport().undo();
        assertArrayEquals(TUBERCULOSIS, values(tuberculosis), 1e-12);
        assertEquals(1, ((Number) table.getModel().getValueAt(1, 1)).intValue(), "The cell does not show the value back");

        probNet.getPNESupport().redo();
        assertArrayEquals(RESTRICTED, values(tuberculosis), 1e-12);
        assertTrue(probNet.getLink(visitToAsia, tuberculosis, true).hasRestrictions());
        assertArrayEquals(VISIT, values(visitToAsia), 1e-12);
    }
}
