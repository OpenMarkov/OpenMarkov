/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFunction;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.RangeFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Columns of three or four states, with the distributions that can and cannot share them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class DistributionsOfAColumnTest {

    private static final List<String> STATES = List.of("a", "b", "c", "d");

    private static final UncertainValue COMPLEMENT = new UncertainValue(new ComplementFunction(1));
    private static final UncertainValue ZERO = new UncertainValue(0.0);

    private static UncertainValue beta() {
        return new UncertainValue(new BetaFunction(2, 5));
    }

    private static UncertainValue dirichlet(double alpha) {
        return new UncertainValue(new DirichletFunction(alpha));
    }

    private static UncertainValue range(double from, double to) {
        return new UncertainValue(new RangeFunction(from, to));
    }

    private static String whatIsWrong(UncertainValue... column) {
        return DistributionsOfAColumn.whatIsWrong(List.of(column), STATES.subList(0, column.length));
    }

    @Test void theColumnsThatCanBeSampledHaveNothingWrong() {
        assertNull(whatIsWrong(beta(), COMPLEMENT));
        assertNull(whatIsWrong(beta(), ZERO, COMPLEMENT));
        assertNull(whatIsWrong(new UncertainValue(0.2), range(0.1, 0.3), COMPLEMENT));
        // a column of MID-Chancellor-corrected, node «State [1]»
        assertNull(whatIsWrong(dirichlet(15), dirichlet(512), dirichlet(731), ZERO));
        assertNull(whatIsWrong(COMPLEMENT, COMPLEMENT));
    }

    @Test void aFixedValueNextToABetaIsNamed() {
        assertEquals("State \"a\" has the fixed value 0.2 next to the Beta distribution of state \"b\".\n"
                             + "With a fixed value other than 0, a Range or a Triangular, the other states can only "
                             + "be Exact, Range, Triangular or Complement.",
                     whatIsWrong(new UncertainValue(0.2), beta(), COMPLEMENT));
    }

    @Test void aFixedValueOrARangeNeedsAComplement() {
        assertEquals("State \"a\" has the fixed value 0.2, so another state must be Complement, to take the rest of "
                             + "the probability.", whatIsWrong(new UncertainValue(0.2), range(0.1, 0.3)));
    }

    @Test void theMaximaCannotAddUpToMoreThanOne() {
        assertEquals("The maxima of the states that are not Complement add up to 1.3, more than 1.",
                     whatIsWrong(new UncertainValue(0.5), range(0.1, 0.8), COMPLEMENT));
    }

    @Test void aBetaGoesAlone() {
        assertEquals("States \"a\" and \"b\" both have a Beta distribution.\nOnly one state can have a Beta; the "
                             + "others can only be Complement or the exact value 0.",
                     whatIsWrong(beta(), beta(), COMPLEMENT));
        assertEquals("State \"b\" has a Dirichlet distribution next to the Beta distribution of state \"a\".\n"
                             + "With a Beta, the other states can only be Complement or the exact value 0.",
                     whatIsWrong(beta(), dirichlet(3), COMPLEMENT));
        assertEquals("State \"a\" has a Beta distribution, so another state must be Complement, to take the rest "
                             + "of the probability.", whatIsWrong(beta(), ZERO));
    }

    @Test void theDirichletGoTwoOrMoreWithNothingElseButZeros() {
        assertEquals("State \"a\" is the only one with a Dirichlet distribution.\nA Dirichlet needs at least "
                             + "another state with a Dirichlet.", whatIsWrong(dirichlet(15), ZERO, ZERO));
        assertEquals("State \"c\" has a Complement distribution next to the Dirichlet distributions of the "
                             + "column.\nWith Dirichlet distributions, the other states can only be Dirichlet or the "
                             + "exact value 0.", whatIsWrong(dirichlet(15), dirichlet(512), COMPLEMENT));
    }

    /** P(X | Y): for y0, 0.2 with no distribution, a Beta and a complement; for y1, a Beta and a complement. */
    @Test void theWrongColumnsOfANetworkAreNamedWithTheirNode() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable y = new Variable("Y", "y0", "y1");
        Variable x = new Variable("X", "x0", "x1", "x2");
        Node parent = probNet.addNode(y, NodeType.CHANCE);
        Node child = probNet.addNode(x, NodeType.CHANCE);
        probNet.addLink(parent, child, true);
        parent.setPotential(new TablePotential(List.of(y), PotentialRole.CONDITIONAL_PROBABILITY));
        UncertainTablePotential table = new UncertainTablePotential(
                List.of(x, y), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{ 0.2, 0.3, 0.5, 0.3, 0.0, 0.7 });
        table.setUncertainValues(new UncertainValue[]{ null, beta(), COMPLEMENT, beta(), ZERO, COMPLEMENT });
        child.setPotential(table);

        assertEquals(List.of("X (Y = y0): State \"x0\" has the fixed value 0.2 next to the Beta distribution of "
                                     + "state \"x1\". With a fixed value other than 0, a Range or a Triangular, "
                                     + "the other states can only be Exact, Range, Triangular or Complement."),
                     DistributionsOfAColumn.wrongIn(probNet));
    }
}
