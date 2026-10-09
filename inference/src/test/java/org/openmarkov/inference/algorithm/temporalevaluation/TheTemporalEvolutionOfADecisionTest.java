/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.temporalevaluation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.MIDTemporalEvolution;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A Markov influence diagram of five cycles in which, in each cycle, the patient is ill with probability 0.3 and
 * a therapy is decided. The utility is 0 and 5 for the healthy patient without and with the therapy, and 10 and 8
 * for the ill one. What is asked is the probability of each option of the therapy in each cycle.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTemporalEvolutionOfADecisionTest {

    private ProbNet probNet;
    private Node illness;
    private Node therapy;

    private static Variable inCycle0(String baseName, String... states) {
        Variable variable = states.length == 0 ? new Variable(baseName) : new Variable(baseName, states);
        variable.setBaseName(baseName);
        variable.setTimeSlice(0);
        return variable;
    }

    private void build() {
        probNet = new ProbNet(MIDType.getUniqueInstance());
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(5);
        illness = probNet.addNode(inCycle0("Illness", "healthy", "ill"), NodeType.CHANCE);
        therapy = probNet.addNode(inCycle0("Therapy", "no", "yes"), NodeType.DECISION);
        Node utility = probNet.addNode(inCycle0("Utility"), NodeType.UTILITY);
        utility.getVariable().setDecisionCriterion(probNet.getDecisionCriteria().getFirst());
        probNet.addLink(illness, utility, true);
        probNet.addLink(therapy, utility, true);
        illness.setPotential(new TablePotential(List.of(illness.getVariable()),
                                                PotentialRole.CONDITIONAL_PROBABILITY, new double[]{ 0.7, 0.3 }));
        utility.setPotential(new ExactDistrPotential(
                List.of(utility.getVariable(), illness.getVariable(), therapy.getVariable()),
                PotentialRole.UNSPECIFIED, new double[]{ 0, 10, 5, 8 }));
    }

    /** The probabilities of «no» and «yes» in a cycle. */
    private double[] therapyInCycle(int cycle, Variable conditioningDecision) throws Exception {
        MIDTemporalEvolution evolution = new MIDTemporalEvolution(probNet, therapy.getVariable());
        evolution.setPreResolutionEvidence(new EvidenceCase());
        evolution.setDecisionVariable(conditioningDecision);
        Map<Variable, TablePotential> values = evolution.getTemporalEvolution();
        assertEquals(6, values.size());
        return values.entrySet().stream()
                     .filter(entry -> entry.getKey().getTimeSlice() == cycle)
                     .findFirst().orElseThrow().getValue().getValues();
    }

    /** Without knowing whether the patient is ill, the therapy is worth 5.9 and not giving it, 3. */
    @Test void withoutPolicyItIsTheOptimalOne() throws Exception {
        build();

        assertArrayEquals(new double[]{ 0, 1 }, therapyInCycle(3, null), 1E-9);
        assertArrayEquals(new double[]{ 0, 1 }, therapyInCycle(3, therapy.getVariable()), 1E-9);
    }

    /** Knowing it, the healthy patient gets the therapy (5 against 0) and the ill one does not (8 against 10). */
    @Test void theOptimalPolicyMayDependOnWhatIsKnownWhenDeciding() throws Exception {
        build();
        probNet.addLink(illness, therapy, true);

        assertArrayEquals(new double[]{ 0.3, 0.7 }, therapyInCycle(3, null), 1E-9);
    }

    @Test void withAPolicyItIsThatPolicy() throws Exception {
        build();
        therapy.setPolicyType(PolicyType.PROBABILISTIC);
        therapy.setPotential(new TablePotential(List.of(therapy.getVariable()), PotentialRole.POLICY,
                                                new double[]{ 0.25, 0.75 }));

        assertArrayEquals(new double[]{ 0.25, 0.75 }, therapyInCycle(3, null), 1E-9);
    }
}
