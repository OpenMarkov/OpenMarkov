/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adding or removing a parent, as a link does, left the relations of the networks of events uniform, without their
 * numbers, which the simulation cannot use (error 99 of the pending list). Now they keep them, as a table does: each
 * state of a new parent gets the values the node had, a new event parent those of the first event, and without a
 * parent the values of its first state remain.
 *
 * @author Manuel Arias
 */
class ARelationOfEventsKeepsItsNumbersWhenItsParentsChangeTest {

    private final Variable ill = new Variable("Ill", "no", "yes");
    private final Variable sex = new Variable("Sex", "male", "female");
    private final Variable onset = event("Onset");
    private final Variable death = event("Death");

    private static Variable event(String name) {
        Variable variable = new Variable(name, "event");
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    private static double[] values(Potential potential) {
        return switch (potential) {
            case DistributionTablePotential distribution -> distribution.getTablePotential().getValues();
            case TableWithEvents table -> table.getTablePotential().getValues();
            case ExactDistrPotential exact -> exact.getTablePotential().getValues();
            default -> throw new IllegalArgumentException(potential.toString());
        };
    }

    /** The time of «Death», normal, of mean and deviation 10 and 1 if «Ill» is «no» and 20 and 2 if it is «yes». */
    private DistributionTablePotential timeOfDeath() {
        DistributionTablePotential distribution = new DistributionTablePotential(
                new ArrayList<>(List.of(death, onset, ill)), PotentialRole.CONDITIONAL_PROBABILITY, "Normal", "Mean / Sd");
        distribution.getTablePotential().setValues(new double[]{ 10, 1, 20, 2 });
        return distribution;
    }

    @Test void theParametersOfAnEventAreKeptWithANewParent() {
        Potential withSex = timeOfDeath().addVariable(sex);

        assertInstanceOf(DistributionTablePotential.class, withSex);
        assertEquals(List.of(death, onset, ill, sex), withSex.getVariables());
        assertEquals("Normal", ((DistributionTablePotential) withSex).getDistributionName());
        assertArrayEquals(new double[]{ 10, 1, 20, 2, 10, 1, 20, 2 }, values(withSex));
    }

    @Test void aNewEventParentTakesTheParametersOfTheFirstEvent() {
        Variable relapse = event("Relapse");

        Potential withRelapse = timeOfDeath().addVariable(relapse);

        assertArrayEquals(new double[]{ 10, 1, 20, 2, 10, 1, 20, 2 }, values(withRelapse));
    }

    @Test void withoutAParentTheParametersOfItsFirstStateRemain() {
        assertArrayEquals(new double[]{ 10, 1 }, values(timeOfDeath().removeVariable(ill)));
        assertArrayEquals(new double[]{ 10, 1, 20, 2 }, values(timeOfDeath().removeVariable(onset)));
    }

    /** A link from «Death» to itself makes it a parent too, as a repeated event is written. */
    @Test void anEventLinkedToItselfKeepsItsParameters() {
        DistributionTablePotential distribution = timeOfDeath();
        assertTrue(distribution.keepsItsValuesWithALinkToItself());

        Potential linkedToItself = distribution.addVariable(death);

        assertEquals(List.of(death, onset, ill, death), linkedToItself.getVariables());
        assertArrayEquals(new double[]{ 10, 1, 20, 2, 10, 1, 20, 2 }, values(linkedToItself));
        assertArrayEquals(new double[]{ 10, 1, 20, 2 }, values(linkedToItself.removeVariable(death)));
        assertEquals(List.of(death, onset, ill), linkedToItself.removeVariable(death).getVariables());
    }

    @Test void theValuesOfAUtilityAreKeptWithANewParentAndWithoutOne() {
        Variable qaly = new Variable("QALY");
        ExactDistrPotential exact = new ExactDistrPotential(List.of(qaly, ill), PotentialRole.UNSPECIFIED,
                                                            new double[]{ 1.0, 0.6 });

        assertArrayEquals(new double[]{ 1.0, 0.6, 1.0, 0.6 }, values(exact.addVariable(sex)));
        assertArrayEquals(new double[]{ 1.0 }, values(exact.removeVariable(ill)));
        // A table of values cannot have an event: the simulation asks for another relation
        assertInstanceOf(UniformPotential.class, exact.addVariable(onset));
    }

    @Test void aTransitionTableKeepsItsValuesWithANewParent() {
        TransitionTablePotential transition = new TransitionTablePotential(new ArrayList<>(List.of(ill, onset)),
                                                                           PotentialRole.CONDITIONAL_PROBABILITY);
        transition.setValues(new double[]{ 0, 1 });

        Potential withSex = transition.addVariable(sex);

        assertInstanceOf(TransitionTablePotential.class, withSex);
        assertArrayEquals(new double[]{ 0, 1, 0, 1 }, values(withSex));
    }

    @Test void aFixedValueOrTimeDoesNotDependOnTheParents() {
        Variable cost = new Variable("Cost");
        DeltaPotential fixedCost = new DeltaPotential(List.of(cost), PotentialRole.UNSPECIFIED, 7);
        Potential costWithSex = fixedCost.addVariable(sex);
        assertEquals(7, ((DeltaPotential) costWithSex).getNumericValue());
        assertEquals(List.of(cost), costWithSex.removeVariable(sex).getVariables());

        IndicatorPotential atYear3 = new IndicatorPotential(List.of(death), PotentialRole.CONDITIONAL_PROBABILITY, 3, 1);
        Potential timeWithSex = atYear3.addVariable(sex);
        assertEquals(3, ((IndicatorPotential) timeWithSex).getTte());
        assertEquals(List.of(death, sex), timeWithSex.getVariables());
    }
}
