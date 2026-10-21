/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A covariate of a regression holds the variables it is written on. When the network was copied, or a numeric parent
 * was discretized, the covariates kept the former variables, which were found only by their names; expanding a Markov
 * model renames its variables at the end, and the expanded network could not be computed (error 71).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheCovariatesOfARegressionHoldTheVariablesOfItsNetworkTest {

    private final ProbNet net = new ProbNet();
    private final Variable age = new Variable("Age");
    private final Variable cost = new Variable("Cost");

    /** Cost = 1 + 2 Age, in the node of Cost. */
    private LinearCombinationPotential regression() {
        net.addNode(age, NodeType.CHANCE);
        List<Variable> variables = List.of(cost, age);
        LinearCombinationPotential regression = new LinearCombinationPotential(variables,
                PotentialRole.CONDITIONAL_PROBABILITY,
                new VariableExpression[]{VariableExpression.Common.CONSTANT, new VariableExpression(variables, "{Age}")},
                new double[]{1, 2});
        net.addNode(cost, NodeType.CHANCE).setPotential(regression);
        return regression;
    }

    private static Variable onlyReference(GLMPotential regression) {
        List<Variable> references = regression.getCovariates()[1].references();
        assertEquals(1, references.size());
        return references.getFirst();
    }

    @Test void aCopiedNetworkHasTheCovariatesOnItsOwnVariables() {
        regression();

        ProbNet copy = net.deepCopy();

        GLMPotential copied = (GLMPotential) copy.getNode("Cost").getPotentials().getFirst();
        assertSame(copy.getVariable("Age"), onlyReference(copied));
    }

    @Test void discretizingANumericParentReplacesItInTheCovariates() {
        LinearCombinationPotential regression = regression();
        Variable discretized = new Variable("Age", "40");

        regression.replaceNumericVariable(discretized);

        assertSame(discretized, regression.getVariable(1));
        assertSame(discretized, onlyReference(regression));
    }
}
