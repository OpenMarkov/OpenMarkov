/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Multiplying a utility by a probability with no variables and summing a variable out must leave
 * that variable out of the result, which matters because its node has just been removed from the
 * network the result goes back into.
 *
 * @author Manuel Arias
 */
public class MarginalizingAgainstAConstantRemovesTheVariableTest {

    private static final double PRECISION = 1e-12;

    private final Variable a = new Variable("A", 2);
    private final Variable b = new Variable("B", 2);

    private TablePotential utilityOver(Variable variable, double... values) {
        return new TablePotential(List.of(variable), PotentialRole.UNSPECIFIED, values);
    }

    @Tag(TestSpeed.FAST)
    @Test public void theVariableIsSummedOutOfAUtilityThatCarriesIt() {
        TablePotential utility = utilityOver(b, 4, 6);

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                DiscretePotentialOperations.createUnityProbabilityPotential(), utility, b);

        assertFalse(result.getVariables().contains(b), "the variable summed out is still in the result");
        assertArrayEquals(new double[] { 10.0 }, result.getValues(), PRECISION, "4 plus 6");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aConstantOtherThanOneScalesWhatIsSummed() {
        TablePotential utility = utilityOver(b, 4, 6);

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                DiscretePotentialOperations.createOneValuePotential(PotentialRole.CONDITIONAL_PROBABILITY, 0.5),
                utility, b);

        assertFalse(result.getVariables().contains(b), "the variable summed out is still in the result");
        assertArrayEquals(new double[] { 5.0 }, result.getValues(), PRECISION, "half of 4 plus 6");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aUtilityWithoutTheVariableIsLeftAlone() {
        TablePotential utility = utilityOver(a, 3, 7);

        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                DiscretePotentialOperations.createUnityProbabilityPotential(), utility, b);

        assertSame(utility, result, "there is nothing to sum out of a utility that does not carry it");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theConstantIsTheSameAnswerAsAProbabilityOfOnes() {
        TablePotential utility = utilityOver(b, 4, 6);
        TablePotential onesOverB = new TablePotential(List.of(b), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 1.0, 1.0 });

        TablePotential againstOnes = DiscretePotentialOperations.multiplyAndMarginalize(
                onesOverB, utility, b);
        TablePotential againstAConstant = DiscretePotentialOperations.multiplyAndMarginalize(
                DiscretePotentialOperations.createUnityProbabilityPotential(), utility, b);

        assertArrayEquals(againstOnes.getValues(), againstAConstant.getValues(), PRECISION,
                "a probability of ones and a constant one must agree");
    }
}
