/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.modelUncertainty.XORShiftRandom;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFunction;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.RangeFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P(X | Y) built by program. For y0 the column is 0.2 with no distribution, a range and a complement; for y1, a
 * range, 0.3 with no distribution and a complement; for y2 no cell has a distribution.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AColumnWithUncertaintyInSomeCellsKeepsItsFixedNumbersTest {

    private Variable y;
    private UncertainTablePotential table;

    @BeforeEach void build() {
        y = new Variable("Y", "y0", "y1", "y2");
        Variable x = new Variable("X", "x0", "x1", "x2");
        table = new UncertainTablePotential(List.of(x, y), PotentialRole.CONDITIONAL_PROBABILITY,
                                            new double[]{ 0.2, 0.2, 0.6, 0.4, 0.3, 0.3, 0.1, 0.2, 0.7 });
        table.setUncertainValues(new UncertainValue[]{
                null, new UncertainValue(new RangeFunction(0.1, 0.3)), new UncertainValue(new ComplementFunction(1)),
                new UncertainValue(new RangeFunction(0.3, 0.5)), null, new UncertainValue(new ComplementFunction(1)),
                null, null, null });
    }

    private boolean hasUncertainty(int stateOfY) throws Exception {
        return table.hasUncertainty(new EvidenceCase(List.of(new Finding(y, stateOfY))));
    }

    @Test void theColumnIsSampledAndTheFixedNumberStays() {
        boolean theRangeOfY0Varies = false;
        for (int i = 0; i < 20; i++) {
            double[] sampled = ((TablePotential) table.sample(new XORShiftRandom())).getValues();

            assertEquals(0.2, sampled[0]);
            assertEquals(1.0, sampled[0] + sampled[1] + sampled[2], 1E-9);
            assertEquals(0.3, sampled[4]);
            assertEquals(1.0, sampled[3] + sampled[4] + sampled[5], 1E-9);
            assertArrayEquals(new double[]{ 0.1, 0.2, 0.7 }, new double[]{ sampled[6], sampled[7], sampled[8] });
            assertTrue(0.1 <= sampled[1] && sampled[1] <= 0.3);
            assertTrue(0.3 <= sampled[3] && sampled[3] <= 0.5);
            theRangeOfY0Varies |= sampled[1] != 0.2;
        }
        assertTrue(theRangeOfY0Varies);
    }

    @Test void theFixedNumberIsTheOneTheTableHasWhenItIsSampled() {
        table.getValues()[0] = 0.1;

        assertEquals(0.1, ((TablePotential) table.sample(new XORShiftRandom())).getValues()[0]);
    }

    @Test void theColumnsWithSomeDistributionAreTheOnesWithUncertainty() throws Exception {
        assertTrue(hasUncertainty(0));
        assertTrue(hasUncertainty(1));
        assertFalse(hasUncertainty(2));
    }

    @Test void aCellWithoutDistributionComesAsTheExactDistributionAtItsNumber() {
        List<UncertainValue> columnOfY1 = table.getUncertainColumn(3, 3);

        assertEquals(0.3, ((ExactFunction) columnOfY1.get(1).getProbDensFunction()).getNu());
        assertNull(table.getUncertainValues()[4]);
        assertEquals(List.of(), table.getUncertainColumn(6, 3).stream().filter(cell -> cell != null).toList());
    }
}
