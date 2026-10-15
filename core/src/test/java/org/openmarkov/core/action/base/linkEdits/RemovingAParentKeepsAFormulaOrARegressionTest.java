/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.action.core.CRemoveNodeEdit;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Removing the link from a parent drops that parent from the relation of the child as the relation itself says.
 * A formula or a regression on numeric variables cannot be projected to a table, and removing a link replaced them
 * by a uniform relation, without a warning, even when they did not use that parent; a formula that did not use it
 * also kept it among its variables (error 239 of the pending list).
 *
 * <p>The network has three numeric chance nodes, {@code A}, {@code B} and {@code N}, with the links A → N and
 * B → N.
 *
 * @author Manuel Arias
 */
public class RemovingAParentKeepsAFormulaOrARegressionTest {

    private ProbNet network;
    private Variable a;
    private Variable b;
    private Variable n;

    @BeforeEach
    public void setUp() throws Exception {
        network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        a = new Variable("A", true, 0, 10, true, 0.1);
        b = new Variable("B", true, 0, 10, true, 0.1);
        n = new Variable("N", true, 0, 100, true, 0.1);
        for (Variable variable : List.of(a, b, n)) {
            new AddNodeEdit(network, variable, NodeType.CHANCE, null).executeEdit();
        }
        new AddLinkEdit(network, a, n, true).executeEdit();
        new AddLinkEdit(network, b, n, true).executeEdit();
    }

    private FunctionPotential formula(String expression) {
        List<Variable> variables = List.of(n, a, b);
        FunctionPotential formula = new FunctionPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                          new VariableExpression(variables, expression));
        network.getNode(n).setPotential(formula);
        return formula;
    }

    private Potential relationOfN() {
        return network.getNode(n).getPotentials().getFirst();
    }

    @Test public void aFormulaThatDoesNotUseTheParentStays() throws Exception {
        formula("{A} + 1");

        new RemoveLinkEdit(network, b, n, true).executeEdit();

        FunctionPotential relation = assertInstanceOf(FunctionPotential.class, relationOfN());
        assertEquals("{A} + 1", relation.getFunction().asStringExpression());
        assertEquals(List.of(n, a), relation.getVariables());
    }

    @Test public void aFormulaThatUsesTheParentBecomesTheDefaultOne() throws Exception {
        formula("{A} + {B}");

        new RemoveLinkEdit(network, b, n, true).executeEdit();

        FunctionPotential relation = assertInstanceOf(FunctionPotential.class, relationOfN());
        assertEquals(FunctionPotential.DEFAULT_FUNCTION.asStringExpression(), relation.getFunction().asStringExpression());
        assertEquals(List.of(n, a), relation.getVariables());
    }

    @Test public void aRegressionKeepsTheTermsThatDoNotUseTheParent() throws Exception {
        List<Variable> variables = List.of(n, a, b);
        network.getNode(n).setPotential(new LinearCombinationPotential(
                variables, PotentialRole.CONDITIONAL_PROBABILITY,
                new VariableExpression[]{new VariableExpression(variables, "{A}"), new VariableExpression(variables, "{B}")},
                new double[]{1, 2}));

        new RemoveLinkEdit(network, b, n, true).executeEdit();

        LinearCombinationPotential relation = assertInstanceOf(LinearCombinationPotential.class, relationOfN());
        assertEquals(List.of(n, a), relation.getVariables());
        assertEquals(1, relation.getCovariates().length);
        assertEquals("{A}", relation.getCovariates()[0].asStringExpression());
    }

    @Test public void undoingBringsTheFormulaBack() throws Exception {
        FunctionPotential original = formula("{A} + 1");
        RemoveLinkEdit removal = new RemoveLinkEdit(network, b, n, true);
        removal.executeEdit();

        removal.undo();

        assertSame(original, relationOfN());
    }

    /** Issue 609: removing, as the window does, a node whose child cannot be projected to a table used to fail. */
    @Test public void removingAParentNodeOfAFormulaDoesNotFail() {
        formula("{A} + {B}");

        assertDoesNotThrow(() -> new CRemoveNodeEdit(network, network.getNode(b)).executeEdit());

        assertEquals(List.of(n, a), relationOfN().getVariables());
    }
}
