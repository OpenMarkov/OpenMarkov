/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANOperations;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DAN-dating. «Ask?» is the first decision; «NClub?» is only made in some scenarios, after it. What is known
 * beforehand is the evidence that «Show optimal strategy» gives to the evaluation.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ADecisionAnalysisNetworkCountsWhatIsKnownOfADecisionTest {

    private static ProbNet dating() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/dan/DAN-dating.pgmx"))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static DANDecompositionIntoSymmetricDANsEvaluation knowing(String decision, String option)
            throws Exception {
        ProbNet probNet = dating();
        EvidenceCase known = new EvidenceCase();
        if (decision != null) {
            Variable variable = probNet.getVariable(decision);
            known.addFinding(new Finding(variable, variable.getStateIndex(option)));
        }
        return new DANDecompositionIntoSymmetricDANsEvaluation(probNet, known);
    }

    private static double utility(DANDecompositionIntoSymmetricDANsEvaluation evaluation) {
        return evaluation.getUtility().getValues()[0];
    }

    /** The utility of the network once «Ask?» is fixed in an option, with nothing known. */
    private static double utilityOfTheBranch(String option) throws Exception {
        ProbNet probNet = dating();
        Variable ask = probNet.getVariable("Ask?");
        ProbNet branch = DANOperations.instantiate(probNet, ask, ask.getStates()[ask.getStateIndex(option)]);
        return utility(new DANDecompositionIntoSymmetricDANsEvaluation(branch, new EvidenceCase()));
    }

    @Test void theUtilityIsTheOneOfTheOptionThatIsKnown() throws Exception {
        assertEquals(utilityOfTheBranch("yes"), utility(knowing("Ask?", "yes")), 1E-9);
        assertEquals(utilityOfTheBranch("no"), utility(knowing("Ask?", "no")), 1E-9);
        assertEquals(utility(knowing(null, null)), Math.max(utilityOfTheBranch("yes"), utilityOfTheBranch("no")),
                     1E-9);
        assertTrue(utilityOfTheBranch("yes") < utilityOfTheBranch("no"));
    }

    @Test void theStrategyDoesNotChooseTheDecisionThatIsKnown() throws Exception {
        assertTrue(knowing(null, null).getOptimalStrategyTree().toString().contains("Ask?"));
        assertFalse(knowing("Ask?", "yes").getOptimalStrategyTree().toString().contains("Ask?"));
        assertFalse(knowing("NClub?", "yes").getOptimalStrategyTree().toString().contains("NClub?"));
    }

    /** Fixing a decision cannot give more than choosing it freely. */
    @Test void aDecisionThatIsMadeLaterIsFixedWhenItsTurnComes() throws Exception {
        double choosingFreely = utility(knowing(null, null));

        assertTrue(utility(knowing("NClub?", "yes")) < choosingFreely);
        assertTrue(utility(knowing("NClub?", "no")) < choosingFreely);
    }
}
