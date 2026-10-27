/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.gui.configuration.UserPreference;

import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The table of a node shows in red, and does not let edit, the cells that a restriction of one of its links forbids.
 * In the table of a utility, whose variable is not among those of its values, the restriction blocked other cells
 * and left editable the forbidden ones (error 154 of the pending list). The columns are (absent, no), (absent, yes),
 * (present, no) and (present, yes), numbered from 1.
 *
 * @author Manuel Arias
 */
class ALinkRestrictionBlocksTheCellsItForbidsTest {

    private final ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
    private final Variable disease = new Variable("Disease", "absent", "present");
    private final Variable therapy = new Variable("Therapy", "no", "yes");

    ALinkRestrictionBlocksTheCellsItForbidsTest() {
        probNet.addNode(disease, NodeType.CHANCE);
        probNet.addNode(therapy, NodeType.DECISION);
    }

    /** Forbids {@code parent} in {@code state} with the first state of {@code child}. */
    private void forbid(Variable parent, int state, Node child) {
        probNet.addLink(probNet.getNode(parent), child, true);
        Link<Node> link = probNet.getLink(probNet.getNode(parent), child, true);
        LinkOperations.initializesRestrictionsPotential(link);
        LinkOperations.setCompatibilityValue(link, parent.getStates()[state], child.getVariable().getStates()[0], 0);
    }

    /** The blocked cells of the table of {@code node}, as «row column». */
    private static TreeSet<String> blockedCells(Node node) throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            Object[][] positions = new TablePotentialPanel(node, null).getNotEditablePositions();
            TreeSet<String> blocked = new TreeSet<>();
            for (int row = 0; row < positions.length; row++) {
                for (int column = 0; column < positions[row].length; column++) {
                    if (positions[row][column] != null) {
                        blocked.add(row + " " + column);
                    }
                }
            }
            return blocked;
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }

    private Node qualityOfLife() {
        return probNet.addNode(new Variable("Quality of life"), NodeType.UTILITY);
    }

    private void giveValues(Node utility) {
        utility.setPotential(new ExactDistrPotential(List.of(utility.getVariable(), disease, therapy),
                                                     PotentialRole.UNSPECIFIED, new double[]{ 10, 8, 3, 6 }));
    }

    @Test void aTherapyThatIsForbiddenBlocksItsTwoColumnsInAUtility() throws Exception {
        Node utility = qualityOfLife();
        probNet.addLink(probNet.getNode(disease), utility, true);
        forbid(therapy, 1, utility);
        giveValues(utility);

        TreeSet<String> blocked = blockedCells(utility);
        assertEquals(2, blocked.size(), blocked.toString());
        assertEquals(List.of("2", "4"), blocked.stream().map(cell -> cell.split(" ")[1]).toList());
    }

    @Test void aDiseaseThatIsForbiddenBlocksItsTwoColumnsInAUtility() throws Exception {
        Node utility = qualityOfLife();
        probNet.addLink(probNet.getNode(therapy), utility, true);
        forbid(disease, 1, utility);
        giveValues(utility);

        TreeSet<String> blocked = blockedCells(utility);
        assertEquals(List.of("3", "4"), blocked.stream().map(cell -> cell.split(" ")[1]).toList());
    }

    /** In a chance node, whose variable is in its table, only the cell of its first state is blocked. */
    @Test void inAChanceNodeTheCellOfTheForbiddenStateIsBlocked() throws Exception {
        Variable result = new Variable("Result", "negative", "positive");
        Node resultNode = probNet.addNode(result, NodeType.CHANCE);
        probNet.addLink(probNet.getNode(therapy), resultNode, true);
        forbid(disease, 1, resultNode);
        resultNode.setPotential(new TablePotential(List.of(result, disease, therapy), PotentialRole.CONDITIONAL_PROBABILITY));

        TreeSet<String> blocked = blockedCells(resultNode);
        assertEquals(2, blocked.size(), blocked.toString());
        assertEquals(List.of("3", "4"), blocked.stream().map(cell -> cell.split(" ")[1]).sorted().toList());
        assertEquals(1, blocked.stream().map(cell -> cell.split(" ")[0]).distinct().count(), blocked.toString());
    }
}
