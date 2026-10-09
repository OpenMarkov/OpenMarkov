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
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The decision analysis network of the reactor problem. Its optimal strategy is not to do the test and to build the
 * conventional reactor, so the result of the test and the result of the advanced reactor do not exist.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AVariableThatDoesNotExistWithTheOptimalStrategyTest {

    private ProbNet probNet;

    private double[] probabilityOf(String variable, EvidenceCase evidence) throws Exception {
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(evidence);
        Map<Variable, TablePotential> probabilities = propagation.getPosteriorValues();
        return probabilities.get(probNet.getVariable(variable)).getValues();
    }

    private EvidenceCase finding(String variable, int state) throws Exception {
        probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith("/dan/DAN-reactor.pgmx"))
                        .findFirst().orElseThrow()).probNet();
        EvidenceCase evidence = new EvidenceCase();
        if (variable != null) {
            evidence.addFinding(new Finding(probNet.getVariable(variable), state));
        }
        return evidence;
    }

    @Test void withoutFindingsItsStatesHaveProbabilityZero() throws Exception {
        EvidenceCase none = finding(null, 0);

        assertArrayEquals(new double[]{ 0, 0, 0 }, probabilityOf("Result of test", none));
        assertArrayEquals(new double[]{ 0.98, 0.02 }, probabilityOf("Result of conventional reactor", none), 1E-9);
    }

    @Test void withAPossibleFindingItsStatesStillHaveProbabilityZero() throws Exception {
        EvidenceCase conventionalReactorWorks = finding("Result of conventional reactor", 0);

        assertArrayEquals(new double[]{ 0, 0, 0 }, probabilityOf("Result of test", conventionalReactorWorks));
    }

    @Test void aFindingOnItIsImpossible() throws Exception {
        EvidenceCase theTestGaveAResult = finding("Result of test", 0);

        assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class,
                     () -> probabilityOf("Quality of components", theTestGaveAResult));
    }
}
