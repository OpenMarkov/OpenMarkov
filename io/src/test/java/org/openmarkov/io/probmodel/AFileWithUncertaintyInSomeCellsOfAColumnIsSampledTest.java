/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.modelUncertainty.XORShiftRandom;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * P(X | Y) in a file written by hand, where the format lets a cell have no distribution. For y0 the column is 0.2
 * with no distribution, a range and a complement; for y1, a range, 0.3 with no distribution and a complement.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFileWithUncertaintyInSomeCellsOfAColumnIsSampledTest {

    @Test void theFixedNumbersStay() throws Exception {
        ProbNet probNet = new PGMXReader().read(
                getClass().getClassLoader().getResource("column-with-uncertainty-in-some-cells.pgmx")).probNet();
        TablePotential table = (TablePotential) probNet.getNode("X").getPotential();

        double[] sampled = ((TablePotential) table.sample(new XORShiftRandom())).getValues();

        assertEquals(0.2, sampled[0]);
        assertEquals(0.3, sampled[4]);
        assertEquals(1.0, sampled[0] + sampled[1] + sampled[2], 1E-9);
        assertEquals(1.0, sampled[3] + sampled[4] + sampled[5], 1E-9);
    }
}
