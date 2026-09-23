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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.GLMPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The network with every kind of potential had a Weibull hazard without Gamma, which could not
 * be projected. Every regression in it is projected now.
 *
 * @author Manuel Arias
 */
public class EveryRegressionOfTheTestNetworkIsProjectedTest {

    @Tag(TestSpeed.MEDIUM)
    @Test public void everyRegressionIsProjected() throws Exception {
        ProbNet net = new PGMXReader().read(getClass().getResource(
                "/networks_for_every_potential/Dynamic-LIMID-every-potential.pgmx")).probNet();
        List<String> failures = new ArrayList<>();
        int regressions = 0;
        for (Node node : net.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                // A formula needs its numeric parents observed
                if (potential instanceof GLMPotential && !(potential instanceof FunctionPotential)) {
                    regressions++;
                    try {
                        potential.tableProject(new EvidenceCase(), null);
                    } catch (Exception e) {
                        failures.add(node.getName() + ": " + e);
                    }
                }
            }
        }
        assertFalse(regressions == 0);
        assertEquals(List.of(), failures);
    }
}
