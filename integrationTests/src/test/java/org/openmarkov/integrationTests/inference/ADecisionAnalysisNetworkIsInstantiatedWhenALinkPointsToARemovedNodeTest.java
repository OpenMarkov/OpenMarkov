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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Not doing the implant rules out the nodes that describe it, and their descendants. Other links of the same
 * decision point to some of those nodes.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class ADecisionAnalysisNetworkIsInstantiatedWhenALinkPointsToARemovedNodeTest {

    private static ProbNet arthronet() throws Exception {
        return new PGMXReader().read(IntegrationTest.class.getResource("/networks/dan/DAN-arthronet.pgmx")).probNet();
    }

    private static ProbNet instantiate(ProbNet net, String decision, String option) throws Exception {
        Variable variable = net.getVariable(decision);
        return DANOperations.instantiate(net, variable, variable.getStates()[variable.getStateIndex(option)]);
    }

    @Test
    void theNetworkWithoutTheImplantHasNoneOfTheNodesThatDescribeIt() throws Exception {
        ProbNet withoutImplant = instantiate(arthronet(), "Realizar Implante", "no");

        assertNull(withoutImplant.getNode("Infeccion PTR"));
        assertNull(withoutImplant.getNode("Isquemia"));
        assertNull(withoutImplant.getNode("CC_Drenaje"));
    }

    @Test
    void theWholeNetworkIsEvaluated() {
        assertDoesNotThrow(() -> new DANDecompositionIntoSymmetricDANsEvaluation(arthronet()).getUtility());
    }

    /**
     * With the implant and nothing else, the cost is 6865.52 x 3.33E-5, the implant is worth 4.64 and the
     * infection, which has probability 0.467539, takes 14.4 away.
     */
    @Test
    void theUtilityOfAPolicyIsTheOneWorkedOutByHand() throws Exception {
        ProbNet net = instantiate(arthronet(), "Realizar Implante", "si");
        net = instantiate(net, "Realizar Gammagrafias", "no");
        net = instantiate(net, "Realizar Biopsia Sinovial", "no");
        net = instantiate(net, "Tratar Infeccion PTR", "no");

        assertEquals(-6865.52 * 3.33E-5 + 4.64 - 14.4 * 0.467539,
                     new DANDecompositionIntoSymmetricDANsEvaluation(net).getUtility().getValues()[0], 1E-5);
    }
}
