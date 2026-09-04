/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tuning model says of itself that it works with variables of three states: each parent pushes
 * the child down, up, or leaves it where it is. Its own validation already asks for that, but the
 * file reader builds the potential without asking it, so the check has to be in the constructor
 * too. Without it the potential was built without a complaint and died on the first question, with
 * an index out of bounds.
 *
 * @author Manuel Arias
 */
public class TheTuningModelRefusesVariablesThatAreNotOfThreeStatesTest {

    @Tag(TestSpeed.FAST)
    @Test public void aChildOfTwoStatesIsRefused() {
        UnrecoverableException refused = assertThrows(UnrecoverableException.class,
                () -> new TuningPotential(List.of(new Variable("C", 2), new Variable("P", 3))),
                "a child of two states leaves no middle state to stay in");

        assertTrue(refused.getMessage().contains("C"), "the message must name the variable: " + refused.getMessage());
        assertTrue(refused.getMessage().contains("3"), "and the number of states it needs: " + refused.getMessage());
    }

    @Tag(TestSpeed.FAST)
    @Test public void aParentOfFourStatesIsRefused() {
        assertThrows(UnrecoverableException.class,
                () -> new TuningPotential(List.of(new Variable("C", 3), new Variable("P", 4))),
                "a parent of four states does not push in three directions");
    }

    @Tag(TestSpeed.FAST)
    @Test public void threeStatesEverywhereIsBuiltAndAnswers() {
        TuningPotential potential = assertDoesNotThrow(
                () -> new TuningPotential(List.of(new Variable("C", 3), new Variable("P", 3))));

        assertDoesNotThrow(potential::getFFunctionPotential, "the table of the rule that combines");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theOtherConstructorsAreGuardedTheSameWay() {
        assertThrows(UnrecoverableException.class,
                () -> new TuningPotential(new Variable("C", 2), new Variable("P", 2)),
                "the constructor taking the variables one by one");

        TuningPotential good = new TuningPotential(List.of(new Variable("C", 3), new Variable("P", 3)));
        assertThrows(UnrecoverableException.class, () -> good.addVariable(new Variable("Q", 5)),
                "adding a parent of five states");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theDefaultLeakStaysInTheMiddleState() {
        TuningPotential potential = new TuningPotential(List.of(new Variable("C", 3), new Variable("P", 3)));

        double[] leak = potential.getDefaultLeakyParameters(3);

        assertTrue(leak[1] == 1.0 && leak[0] == 0.0 && leak[2] == 0.0,
                "with no parent pushing, the effect stays where it is");
    }
}
