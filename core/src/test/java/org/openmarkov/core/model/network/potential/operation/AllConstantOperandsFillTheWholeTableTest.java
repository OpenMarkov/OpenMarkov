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

/**
 * When every operand is a constant, the result is that constant in every cell. The table starts
 * out uniform, so filling only the first cell leaves the rest holding one over the number of
 * states.
 *
 * @author Manuel Arias
 */
public class AllConstantOperandsFillTheWholeTableTest {

    private static final double PRECISION = 1e-12;
    private static final double[] HALF_EVERYWHERE = { 0.5, 0.5, 0.5 };

    private final Variable kept = new Variable("K", 3);
    private final Variable eliminated = new Variable("E", 2);

    private TablePotential aConstantWorthHalf() {
        return DiscretePotentialOperations.createOneValuePotential(PotentialRole.CONDITIONAL_PROBABILITY, 0.5);
    }

    @Tag(TestSpeed.FAST)
    @Test public void marginalizingFillsEveryCell() {
        TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(
                List.of(aConstantWorthHalf()), List.of(kept), List.of(eliminated));

        assertArrayEquals(HALF_EVERYWHERE, result.getValues(), PRECISION,
                "the cells past the first kept the uniform value the constructor left");
    }

    @Tag(TestSpeed.FAST)
    @Test public void maximizingFillsEveryCell() {
        Object[] result = DiscretePotentialOperations.multiplyAndMaximize(
                List.of(aConstantWorthHalf()), List.of(kept), eliminated);

        assertArrayEquals(HALF_EVERYWHERE, ((TablePotential) result[0]).getValues(), PRECISION,
                "the cells past the first kept the uniform value the constructor left");
    }

    @Tag(TestSpeed.FAST)
    @Test public void maximizingUniformlyFillsEveryCell() {
        TablePotential[] result = DiscretePotentialOperations.multiplyAndMaximizeUniformly(
                List.of(aConstantWorthHalf()), List.of(kept), eliminated);

        assertArrayEquals(HALF_EVERYWHERE, result[0].getValues(), PRECISION,
                "the cells past the first kept the uniform value the constructor left");
    }
}
