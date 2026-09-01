/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The projection hands each potential the options of the network being projected, so a switch set
 * on the network — such as {@code iciAwareVE} — reaches {@code tableProjectToFactors}.
 *
 * @author Manuel Arias
 */
public class ProjectionCarriesTheNetworkOptionsTest {

    /** A potential that records the options the projection hands it. */
    private static final class RecordingPotential extends Potential {

        private InferenceOptions received;

        private RecordingPotential(List<Variable> variables) {
            super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        }

        @Override public List<TablePotential> tableProjectToFactors(EvidenceCase evidenceCase,
                                                                    InferenceOptions inferenceOptions,
                                                                    List<TablePotential> alreadyProjected) {
            received = inferenceOptions;
            return List.of(new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY));
        }

        @Override public Potential project(EvidenceCase evidenceCase) {
            throw new UnsupportedOperationException("this test never projects to a single potential");
        }

        @Override public Potential copy() {
            return new RecordingPotential(variables);
        }
    }

    @Test public void aSwitchSetOnTheNetworkReachesTheProjection() throws NonProjectablePotentialException {
        ProbNet net = new ProbNet();
        RecordingPotential potential = spyOn(net);
        net.getInferenceOptions().setIciAwareVE(true);

        net.tableProjectPotentials(new EvidenceCase());

        assertNotNull(potential.received, "the projection must hand the potential some options");
        assertTrue(potential.received.isIciAwareVE(), "the switch set on the network was lost on the way");
    }

    @Test public void aNetworkThatSaysNothingProjectsWithTheSwitchOff() throws NonProjectablePotentialException {
        ProbNet net = new ProbNet();
        RecordingPotential potential = spyOn(net);

        net.tableProjectPotentials(new EvidenceCase());

        assertNotNull(potential.received);
        assertFalse(potential.received.isIciAwareVE());
    }

    private static RecordingPotential spyOn(ProbNet net) {
        Variable variable = new Variable("Y", 2);
        net.addNode(variable, NodeType.CHANCE);
        RecordingPotential potential = new RecordingPotential(List.of(variable));
        net.getNode(variable).setPotential(potential);
        return potential;
    }
}
