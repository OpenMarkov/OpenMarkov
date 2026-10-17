/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The relation of a node that loses a parent keeps the comment and the properties it had, whatever relation it
 * becomes.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class RemovingAParentKeepsTheCommentOfTheRelationTest {

    /** The relation of C, child of A; the numeric ones have numeric variables. */
    enum Relation {
        TABLE(false, NodeType.CHANCE, (c, a) -> new TablePotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY,
                                                                   new double[] { 0.3, 0.7, 0.6, 0.4 })),
        TABLE_THAT_BECOMES_UNIFORM(false, NodeType.CHANCE,
                                   (c, a) -> new TablePotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY,
                                                                new double[] { 0, 0, 0.6, 0.4 })),
        REGRESSION(true, NodeType.CHANCE,
                   (c, a) -> new LinearCombinationPotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY)),
        FORMULA_ON_THE_PARENT(true, NodeType.CHANCE,
                              (c, a) -> new FunctionPotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY,
                                                              new VariableExpression(List.of(c, a), "{A}*2"))),
        TREE(false, NodeType.CHANCE,
             (c, a) -> new TreeADDPotential(List.of(c, a), a, PotentialRole.CONDITIONAL_PROBABILITY)),
        EXACT(true, NodeType.CHANCE,
              (c, a) -> new ExactDistrPotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 5 })),
        UTILITY_TREE(true, NodeType.UTILITY,
                     (c, a) -> new TreeADDPotential(List.of(c, a), a, PotentialRole.UNSPECIFIED));

        final boolean numeric;
        final NodeType nodeType;
        final BiFunction<Variable, Variable, Potential> make;

        Relation(boolean numeric, NodeType nodeType, BiFunction<Variable, Variable, Potential> make) {
            this.numeric = numeric;
            this.nodeType = nodeType;
            this.make = make;
        }
    }

    @ParameterizedTest
    @EnumSource(Relation.class)
    void theCommentAndThePropertiesStay(Relation relation) throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Criterion criterion = new Criterion("Cost", "euro");
        net.setDecisionCriteria(new ArrayList<>(List.of(criterion)));
        Variable a = relation.numeric ? new Variable("A") : new Variable("A", "a0", "a1");
        Variable c = relation.numeric ? new Variable("C") : new Variable("C", "c0", "c1");
        c.setDecisionCriterion(criterion);
        net.addNode(a, NodeType.CHANCE);
        Node child = net.addNode(c, relation.nodeType);
        net.addLink(a, c, true);
        Potential potential = relation.make.apply(c, a);
        potential.setComment("a note");
        potential.properties.put("key", "value");
        child.setPotential(potential);

        new RemoveLinkEdit(net, a, c, true).executeEdit();

        assertEquals("a note", child.getPotential().getComment());
        assertEquals("value", child.getPotential().properties.get("key"));
    }
}
