/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DynamicBayesianNetwork;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Same as previous» takes the relation of the same variable in an earlier cycle. Looking for it used to break when an
 * earlier cycle did not have the variable, or its node had no relation, as with a treatment that starts in cycle 1
 * (error 70).
 *
 * @author Manuel Arias
 */
class SameAsPreviousLooksForTheRelationOfAnEarlierCycleTest {

    private ProbNet probNet;

    @BeforeEach void aDynamicNetwork() {
        probNet = new ProbNet(DynamicBayesianNetwork.getUniqueInstance());
    }

    private Node node(String baseName, int timeSlice) {
        Variable variable = new Variable(baseName, "no", "yes");
        variable.setTimeSlice(timeSlice);
        Node node = probNet.addNode(variable, NodeType.CHANCE);
        node.clearPotentials();
        return node;
    }

    private Node withATable(Node node) {
        node.setPotentials(List.of(new TablePotential(List.of(node.getVariable()),
                                                      PotentialRole.CONDITIONAL_PROBABILITY)));
        return node;
    }

    private SameAsPrevious sameAsPrevious(Node node) {
        SameAsPrevious relation = new SameAsPrevious(List.of(node.getVariable()));
        node.setPotentials(List.of(relation));
        return relation;
    }

    @Test void aVariableThatStartsInCycleOneHasNothingToCopy() {
        SameAsPrevious relation = sameAsPrevious(node("Treatment", 1));

        assertNull(relation.getOriginalPotential(probNet));
    }

    @Test void anEarlierNodeWithoutRelationHasNothingToCopy() {
        node("A", 0);
        SameAsPrevious relation = sameAsPrevious(node("A", 1));

        assertNull(relation.getOriginalPotential(probNet));
    }

    @Test void twoInARowWithoutAnEarlierCycleHaveNothingToCopy() {
        sameAsPrevious(node("B", 2));
        SameAsPrevious relation = sameAsPrevious(node("B", 3));

        assertNull(relation.getOriginalPotential(probNet));
    }

    @Test void theRelationOfThePreviousCycleIsCopied() {
        Node first = withATable(node("C", 0));
        SameAsPrevious relation = sameAsPrevious(node("C", 1));

        assertSame(first.getPotential(), relation.getOriginalPotential(probNet));
    }

    @Test void aCycleWithoutTheVariableIsSkipped() {
        Node first = withATable(node("D", 0));
        SameAsPrevious relation = sameAsPrevious(node("D", 2));

        assertSame(first.getPotential(), relation.getOriginalPotential(probNet));
    }

    @Test void expandingTheModelSaysWhichNodeHasNothingToCopy() {
        withATable(node("Age", 0));
        sameAsPrevious(node("B", 2));
        sameAsPrevious(node("B", 3));

        NotSupportedOperationException refused = assertThrows(NotSupportedOperationException.class,
                                                              () -> TemporalNetOperations.expandNetwork(probNet));
        assertTrue(refused.getMessage().contains("B [3]"), refused.getMessage());
    }
}
