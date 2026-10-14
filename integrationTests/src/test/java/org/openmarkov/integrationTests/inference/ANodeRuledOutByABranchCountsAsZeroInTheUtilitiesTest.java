/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANOperations;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Not doing the implant rules out the treatment and its cost. "Beneficio neto" used to be removed with them, and the
 * factor "C2E", left as a utility of its own, was added to the result (error 200).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class ANodeRuledOutByABranchCountsAsZeroInTheUtilitiesTest {

    private static ProbNet withoutImplant() throws Exception {
        ProbNet net = new PGMXReader().read(IntegrationTest.class.getResource("/networks/dan/DAN-arthronet.pgmx"))
                                      .probNet();
        Variable variable = net.getVariable("Realizar Implante");
        return DANOperations.instantiate(net, variable, variable.getStates()[variable.getStateIndex("no")]);
    }

    @Test
    void theBranchWithoutTheImplantIsWorthZero() throws Exception {
        assertEquals(0, new DANDecompositionIntoSymmetricDANsEvaluation(withoutImplant()).getUtility()
                                                                                       .getValues()[0], 1E-12);
    }

    /** The sums only lose the costs and the effects that the branch rules out. */
    @Test
    void theNodesThatCombineTheUtilitiesAreKept() throws Exception {
        ProbNet net = withoutImplant();

        for (String name : new String[]{"Beneficio neto", "EVAC Total", "Coste total", "Coste ajustado", "C2E"}) {
            assertNotNull(net.getNode(name), name);
        }
    }
}
