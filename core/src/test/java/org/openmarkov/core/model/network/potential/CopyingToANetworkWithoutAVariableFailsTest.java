/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copying a potential to a network that lacks one of its variables must fail naming the
 * variable, instead of putting null in the copy's list of variables.
 *
 * @author Manuel Arias
 */
public class CopyingToANetworkWithoutAVariableFailsTest {

    private final Variable child = new Variable("Child", 2);
    private final Variable parent = new Variable("Parent", 2);
    private final Variable numeric = new Variable("Cost");

    private static ProbNet networkWith(Variable... variables) {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("destination");
        for (Variable variable : variables) {
            net.addNode(new Variable(variable), NodeType.CHANCE);
        }
        return net;
    }

    private TablePotential table() {
        return new TablePotential(List.of(child, parent), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0.1, 0.9, 0.3, 0.7 });
    }

    private UnivariateDistrPotential distribution() {
        return new UnivariateDistrPotential(List.of(numeric, parent), PotentialRole.CONDITIONAL_PROBABILITY);
    }

    @Tag(TestSpeed.FAST)
    @Test public void aTableNamesTheMissingParent() {
        InvalidArgumentException e = assertThrows(InvalidArgumentException.class,
                () -> table().deepCopy(networkWith(child)));
        assertTrue(e.getMessage().contains("Parent"), e.getMessage());
    }

    @Tag(TestSpeed.FAST)
    @Test public void aDistributionNamesTheMissingParent() {
        InvalidArgumentException e = assertThrows(InvalidArgumentException.class,
                () -> distribution().deepCopy(networkWith(numeric)));
        assertTrue(e.getMessage().contains("Parent"), e.getMessage());
    }

    @Tag(TestSpeed.FAST)
    @Test public void aTableCopiedWithAllItsVariablesPointsToTheDestination() {
        ProbNet destination = networkWith(child, parent);
        TablePotential copy = (TablePotential) table().deepCopy(destination);

        assertSame(destination.getVariable("Child"), copy.getVariable(0));
        assertSame(destination.getVariable("Parent"), copy.getVariable(1));
        assertArrayEquals(new double[] { 0.1, 0.9, 0.3, 0.7 }, copy.getValues(), 1E-12);
    }

    @Tag(TestSpeed.FAST)
    @Test public void aDistributionCopiedWithAllItsVariablesPointsToTheDestination() {
        ProbNet destination = networkWith(numeric, parent);
        Potential copy = distribution().deepCopy(destination);

        assertEquals(List.of(destination.getVariable("Cost"), destination.getVariable("Parent")),
                copy.getVariables());
    }

    private final Variable time = new Variable("Time");

    private WeibullHazardPotential weibull() {
        WeibullHazardPotential potential = new WeibullHazardPotential(List.of(child, parent),
                PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setTimeVariable(time);
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void aWeibullNamesTheMissingTimeVariable() {
        InvalidArgumentException e = assertThrows(InvalidArgumentException.class,
                () -> weibull().deepCopy(networkWith(child, parent)));
        assertTrue(e.getMessage().contains("Time"), e.getMessage());
    }

    @Tag(TestSpeed.FAST)
    @Test public void aWeibullCopiedWithItsTimeVariablePointsToTheDestination() {
        ProbNet destination = networkWith(child, parent, time);
        WeibullHazardPotential copy = (WeibullHazardPotential) weibull().deepCopy(destination);

        assertSame(destination.getVariable("Time"), copy.getTimeVariable());
    }
}
