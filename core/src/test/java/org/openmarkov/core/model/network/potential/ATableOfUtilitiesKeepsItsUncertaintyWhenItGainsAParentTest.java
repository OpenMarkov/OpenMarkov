/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A table of utilities, or of values, with uncertainty in its numbers keeps it when the node gains a parent: each
 * new cell has its own copy of the uncertain value of the cell whose number it repeats (error 432).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ATableOfUtilitiesKeepsItsUncertaintyWhenItGainsAParentTest {

    private final Variable utility = new Variable("U");
    private final Variable parent = new Variable("P", "no", "yes");
    private final Variable other = new Variable("Q", "a", "b", "c");

    @Test void eachCellOfTheNewParentHasItsOwnCopy() {
        ExactDistrPotential table = new ExactDistrPotential(List.of(utility), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{5});
        UncertainValue uncertain = new UncertainValue(5.0);
        table.setUncertainValues(new UncertainValue[]{uncertain});
        table.setComment("A comment");

        ExactDistrPotential withParent = (ExactDistrPotential) table.addVariable(parent);

        UncertainValue[] copies = withParent.getTablePotential().getUncertainValues();
        assertArrayEquals(new double[]{5, 5}, withParent.getTablePotential().getValues());
        assertNotNull(copies);
        assertEquals(2, copies.length);
        for (UncertainValue copy : copies) {
            assertNotSame(uncertain, copy);
            assertEquals(uncertain.toString(), copy.toString());
        }
        assertNotSame(copies[0], copies[1]);
        assertEquals("A comment", withParent.getComment());
    }

    @Test void aSecondParentRepeatsTheUncertaintyOfEachCell() {
        ExactDistrPotential table =
                new ExactDistrPotential(List.of(utility, parent), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{5, 7});
        UncertainValue five = new UncertainValue(5.0);
        table.setUncertainValues(new UncertainValue[]{five, null});

        ExactDistrPotential withOther = (ExactDistrPotential) table.addVariable(other);

        UncertainValue[] copies = withOther.getTablePotential().getUncertainValues();
        assertArrayEquals(new double[]{5, 7, 5, 7, 5, 7}, withOther.getTablePotential().getValues());
        for (int i = 0; i < 6; i += 2) {
            assertEquals(five.toString(), copies[i].toString());
            assertNull(copies[i + 1]);
        }
    }

    @Test void aTableWithoutUncertaintyStaysWithoutIt() {
        ExactDistrPotential table = new ExactDistrPotential(List.of(utility), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{5});

        ExactDistrPotential withParent = (ExactDistrPotential) table.addVariable(parent);

        assertNull(withParent.getTablePotential().getUncertainValues());
    }
}
