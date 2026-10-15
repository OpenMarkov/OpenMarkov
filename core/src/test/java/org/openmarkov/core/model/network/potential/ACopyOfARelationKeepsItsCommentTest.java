/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.action.core.NodeReplaceStatesEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A copy of a relation keeps its comment, its criterion and its properties, as pasting a node and undoing a change
 * of the number of states of a parent need (error 260).
 *
 * @author Manuel Arias
 */
class ACopyOfARelationKeepsItsCommentTest {

    private static final PotentialRole ROLE = PotentialRole.CONDITIONAL_PROBABILITY;

    private ProbNet probNet;
    private Variable x;
    private Variable e;
    private Variable y;
    private Variable time;
    private Variable age;

    @BeforeEach void aNetworkOfEvents() {
        probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        x = new Variable("X", "x0", "x1");
        e = new Variable("E", "no", "yes");
        e.setVariableType(VariableType.EVENT);
        y = new Variable("Y", "y0", "y1");
        time = new Variable("Time");
        age = new Variable("Age");
        Node node = probNet.addNode(x, NodeType.CHANCE);
        probNet.addLink(probNet.addNode(e, NodeType.EVENT), node, true);
        probNet.addLink(probNet.addNode(y, NodeType.CHANCE), node, true);
        probNet.addNode(time, NodeType.CHANCE);
        probNet.addNode(age, NodeType.CHANCE);
    }

    /** A relation of the given class, with a comment, a criterion and a property. */
    private Potential markedRelation(String relationClass) {
        Potential relation = switch (relationClass) {
            case "TreeWithEventsPotential" -> new TreeWithEventsPotential(List.of(x, e, y), ROLE);
            case "TreeWithExcludedEventsPotential" -> new TreeWithExcludedEventsPotential(List.of(x, e, y), ROLE);
            case "DistributionTablePotential" -> new DistributionTablePotential(new ArrayList<>(List.of(x, e, y)));
            case "PiecewiseExponentialPotential" -> new PiecewiseExponentialPotential(new ArrayList<>(List.of(x, time)),
                                                                                      ROLE);
            case "AugmentedProbTablePotential" -> new AugmentedProbTablePotential(new ArrayList<>(List.of(x, age)),
                                                                                  ROLE);
            default -> throw new IllegalArgumentException(relationClass);
        };
        relation.setComment("a comment");
        relation.setCriterion(new Criterion("cost"));
        relation.properties.put("key", "value");
        return relation;
    }

    private static void assertMarked(Potential relation) {
        assertEquals("a comment", relation.getComment());
        assertEquals("cost", relation.getCriterion().getCriterionName());
        assertEquals(Map.of("key", "value"), relation.properties);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TreeWithEventsPotential", "TreeWithExcludedEventsPotential", "DistributionTablePotential",
                            "PiecewiseExponentialPotential", "AugmentedProbTablePotential"})
    void theCopyAndTheDeepCopyKeepTheCommentTheCriterionAndTheProperties(String relationClass) {
        Potential relation = markedRelation(relationClass);

        assertMarked(relation.copy());
        assertMarked(relation.deepCopy(probNet.deepCopy()));
    }

    @Test void undoingAChangeOfTheNumberOfStatesOfAParentKeepsTheComment() throws Exception {
        Node node = probNet.getNode(x);
        node.setPotential(markedRelation("TreeWithEventsPotential"));
        probNet.getNode(y).setPotential(new TablePotential(List.of(y), ROLE));
        probNet.getPNESupport().setWithUndo(true);

        new NodeReplaceStatesEdit(probNet.getNode(y), new State[]{new State("y0"), new State("y1"), new State("y2")})
                .executeEdit();
        probNet.getPNESupport().undo();

        assertEquals(2, y.getNumStates());
        assertEquals("a comment", node.getPotential().getComment());
    }
}
