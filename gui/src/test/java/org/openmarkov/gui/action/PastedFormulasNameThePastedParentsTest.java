/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.DistributionTablePotential;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pasting a node whose relation is a formula together with the parent the formula names. In the
 * same network the pasted parent gets a new name and the formula kept the old one, so the pasted
 * node could not be computed (error 234 of the pending list).
 */
public class PastedFormulasNameThePastedParentsTest {

    private ProbNet probNet;
    private Node age;
    private Node cost;

    @BeforeEach
    void setUp() {
        probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        age = probNet.addNode(new Variable("Age"), NodeType.CHANCE);
        cost = probNet.addNode(new Variable("Cost"), NodeType.UTILITY);
        probNet.addLink(age, cost, true);
        List<Variable> variables = List.of(cost.getVariable(), age.getVariable());
        cost.setPotentials(List.of(new FunctionPotential(variables, PotentialRole.UNSPECIFIED,
                new VariableExpression(variables, "{Age}*3"))));
    }

    private static double costAt40(ProbNet net, String costName, String ageName) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(net.getVariable(ageName), 40.0));
        return ((FunctionPotential) net.getNode(costName).getPotential()).sampleConditionedVariable(new double[]{0}, evidence);
    }

    @Test
    void aFormulaPastedInTheSameNetworkNamesThePastedParent() throws Exception {
        new PasteEdit(probNet, new SelectedContent(List.of(age, cost), List.of(probNet.getLink(age, cost, true))), null)
                .executeEdit();

        FunctionPotential pasted = (FunctionPotential) probNet.getNode("Cost'").getPotential();
        assertEquals("{Age'}*3", pasted.getFunction().asStringExpression());
        assertEquals(120, costAt40(probNet, "Cost'", "Age'"));
        assertEquals(120, costAt40(probNet, "Cost", "Age"), "the original keeps its own");
    }

    /**
     * Pastes X, X' and Child, whose relation, made by {@code relation} on its variables, names both, in a network
     * that already has an X: X is pasted as X' and X' as X''.
     *
     * @return the network pasted into
     */
    private static ProbNet pasteXAndXPrimeWith(ProbNet source, ProbNet target, NodeType childType,
                                               Function<List<Variable>, Potential> relation) throws Exception {
        Node x = source.addNode(new Variable("X"), NodeType.CHANCE);
        Node xPrime = source.addNode(new Variable("X'"), NodeType.CHANCE);
        Variable childVariable = new Variable("Child");
        if (childType == NodeType.EVENT) {
            childVariable = new Variable("Child", "no", "yes");
            childVariable.setVariableType(VariableType.EVENT);
        }
        Node child = source.addNode(childVariable, childType);
        source.addLink(x, child, true);
        source.addLink(xPrime, child, true);
        child.setPotentials(List.of(relation.apply(new ArrayList<>(List.of(child.getVariable(), x.getVariable(),
                                                                          xPrime.getVariable())))));
        target.addNode(new Variable("X"), NodeType.CHANCE);
        new PasteEdit(target, new SelectedContent(List.of(x, xPrime, child),
                                                  List.of(source.getLink(x, child, true), source.getLink(xPrime, child, true))),
                      null).executeEdit();
        return target;
    }

    /**
     * The variables of a pasted relation used to be replaced in the order of its list: X -> X' left {X'}+2*{X'}, where
     * the two could no longer be told apart, and X' -> X'' turned both into X'' (error 258 of the pending list).
     */
    @Test
    void aFormulaOnXAndXPrimeKeepsThemApart() throws Exception {
        ProbNet target = pasteXAndXPrimeWith(new ProbNet(BayesianNetworkType.getUniqueInstance()),
                                             new ProbNet(BayesianNetworkType.getUniqueInstance()), NodeType.CHANCE,
                                             variables -> new FunctionPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                                                new VariableExpression(variables, "{X}+2*{X'}")));

        FunctionPotential pasted = (FunctionPotential) target.getNode("Child").getPotential();
        assertEquals("{X'}+2*{X''}", pasted.getFunction().asStringExpression());
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(target.getVariable("X'"), 1.0));
        evidence.addFinding(new Finding(target.getVariable("X''"), 10.0));
        assertEquals(21, pasted.sampleConditionedVariable(new double[]{0}, evidence));
    }

    /** The same formula in a distribution table made the paste die half done, with two variables named X'. */
    @Test
    void aDistributionTableOnXAndXPrimeIsPasted() throws Exception {
        ProbNet target = assertDoesNotThrow(() -> pasteXAndXPrimeWith(
                new ProbNet(DESNetworkType.getUniqueInstance()), new ProbNet(DESNetworkType.getUniqueInstance()),
                NodeType.EVENT, variables -> {
                    DistributionTablePotential table = new DistributionTablePotential(
                            variables, PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
                    table.getTableWithEvents().getTableWithFunctions().setFunctionValues(new String[]{"{X}+2*{X'}"});
                    return table;
                }));

        DistributionTablePotential pasted = (DistributionTablePotential) target.getNode("Child").getPotential();
        assertEquals("{X'}+2*{X''}", pasted.getTableWithEvents().getTableWithFunctions().getFunctionValues()[0]);
    }

    @Test
    void aFormulaPastedInAnotherNetworkIsWrittenOnTheNodesOfThatNetwork() throws Exception {
        ProbNet other = new ProbNet(InfluenceDiagramType.getUniqueInstance());

        new PasteEdit(other, new SelectedContent(List.of(age, cost), List.of(probNet.getLink(age, cost, true))), null)
                .executeEdit();

        FunctionPotential pasted = (FunctionPotential) other.getNode("Cost").getPotential();
        assertSame(other.getVariable("Age"), pasted.getFunction().references().getFirst());
        assertEquals(120, costAt40(other, "Cost", "Age"));
    }
}
