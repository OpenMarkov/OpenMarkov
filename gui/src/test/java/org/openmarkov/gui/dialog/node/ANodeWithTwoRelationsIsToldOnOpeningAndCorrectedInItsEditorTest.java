/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file with two relations for the same node opened without a warning: the editor showed the first and the inference
 * multiplied both (error 400 of the pending list). Now it opens with a warning that names the node, no algorithm
 * evaluates it, and «OK» in the editor of the relation leaves the node with the one shown.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANodeWithTwoRelationsIsToldOnOpeningAndCorrectedInItsEditorTest {

    @TempDir Path folder;

    /** A Bayesian network read from a file where X has the table (0.9, 0.1) and then the table (0.2, 0.8). */
    private ProbNet readWithTwoTablesInX() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("two-relations");
        Variable x = new Variable("X", "no", "yes");
        Node node = net.addNode(x, NodeType.CHANCE);
        node.addPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] {0.9, 0.1}));
        node.addPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] {0.2, 0.8}));
        Path file = folder.resolve("two-relations.pgmx");
        new PGMXWriter_1_0().write(file.toString(), net, List.of());
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    /** As the dialog of the relation does: the editor works inside a history of its own. */
    private static PotentialEditPanel open(Node node) throws Exception {
        node.getProbNet().getPNESupport().openNewSubEditHistory();
        return new PotentialEditPanel(node, false, true);
    }

    private static double[] onlyTableOf(Node node) {
        assertEquals(1, node.getNumPotentials());
        return ((TablePotential) node.getPotential()).getValues();
    }

    @Test void whatTheWarningOnOpeningListsNamesTheNode() throws Exception {
        ProbNet read = readWithTwoTablesInX();

        List<String> violations = read.getConstraintViolations().stream().map(Object::toString).toList();

        assertEquals(1, violations.size(), violations.toString());
        assertTrue(violations.getFirst().contains("X has 2 probability or utility relations instead of one."),
                   violations.getFirst());
    }

    @Test void noAlgorithmEvaluatesIt() throws Exception {
        ProbNet read = readWithTwoTablesInX();

        ConstraintViolatedException refusal = assertThrows(ConstraintViolatedException.class,
                                                           () -> new VEPropagation(read));

        assertTrue(refusal.toString().contains("X has 2 probability"), refusal.toString());
    }

    @Test @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor builds a dialog for the comment, which needs a screen")
    void okInTheEditorOfTheRelationKeepsTheOneShown() throws Exception {
        ProbNet read = readWithTwoTablesInX();
        Node node = read.getNode("X");

        open(node).commitChanges();

        assertArrayEquals(new double[] {0.9, 0.1}, onlyTableOf(node), 1e-9);
        assertTrue(read.getConstraintViolations().isEmpty(), read.getConstraintViolations().toString());
    }

    @Test @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor builds a dialog for the comment, which needs a screen")
    void cancelInTheEditorOfTheRelationKeepsBoth() throws Exception {
        ProbNet read = readWithTwoTablesInX();
        Node node = read.getNode("X");

        open(node).uncommitChanges();

        assertEquals(2, node.getNumPotentials());
        assertArrayEquals(new double[] {0.9, 0.1}, ((TablePotential) node.getPotentials().get(0)).getValues(), 1e-9);
        assertArrayEquals(new double[] {0.2, 0.8}, ((TablePotential) node.getPotentials().get(1)).getValues(), 1e-9);
    }
}
