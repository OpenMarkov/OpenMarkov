/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.temporalevaluation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.MIDTemporalEvolution;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A treatment that starts in cycle 1 with «Same as previous» has nothing to copy. The temporal evolution of any node
 * says so, naming that node, whatever the horizon.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTemporalEvolutionSaysWhichNodeHasNothingToCopyTest {

    @ParameterizedTest(name = "horizon {0}")
    @ValueSource(ints = { 1, 2 })
    void theEvolutionOfAnotherNodeNamesIt(int horizon) {
        ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());
        Variable age = new Variable("Age", "young", "old");
        age.setTimeSlice(0);
        Variable treatment = new Variable("Treatment", "no", "yes");
        treatment.setTimeSlice(1);
        Node ageNode = probNet.addNode(age, NodeType.CHANCE);
        Node treatmentNode = probNet.addNode(treatment, NodeType.CHANCE);
        ageNode.setPotentials(List.of(new TablePotential(List.of(age), PotentialRole.CONDITIONAL_PROBABILITY,
                                                         new double[] { 0.3, 0.7 })));
        treatmentNode.setPotentials(List.of(new SameAsPrevious(List.of(treatment))));
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(horizon);

        UnrecoverableException refused = assertThrows(UnrecoverableException.class,
                                                      () -> new MIDTemporalEvolution(probNet, age).getTemporalEvolution());
        assertEquals("«Treatment [1]»",
                     assertInstanceOf(NotEvaluableNetworkException.NothingToCopy.class, refused.getCause()).nodes);
    }
}
