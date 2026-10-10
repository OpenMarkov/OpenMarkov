/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.core.model.network;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * The formula table of the network with every kind of potential is projected: each column is a
 * formula and a cell that takes what is left.
 *
 * @author Manuel Arias
 */
public class TheFormulaTableOfTheTestNetworkIsProjectedTest {

    @Tag(TestSpeed.MEDIUM)
    @Test public void theComplementCellsTakeWhatIsLeft() throws Exception {
        ProbNet net = new PGMXReader().read(getClass().getResource(
                "/networks_for_every_potential/Dynamic-LIMID-every-potential.pgmx")).probNet();

        double[] values = net.getNode("AugmentedProbTable [0]").getPotential()
                .tableProject(new EvidenceCase(), null).getValues();

        assertArrayEquals(new double[] { 1, 0, 1, 0, 1, 0, 1, 0 }, values, 0.0);
    }
}
