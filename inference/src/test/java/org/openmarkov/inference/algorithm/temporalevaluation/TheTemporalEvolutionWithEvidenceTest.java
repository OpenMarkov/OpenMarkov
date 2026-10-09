/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.temporalevaluation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.MIDTemporalEvolution;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * A Markov influence diagram in which a patient is healthy or ill in each cycle. With the option «cure» the
 * patient is always healthy in the next cycle; with «wait», a healthy patient falls ill with probability 0.2 and
 * an ill one recovers with probability 0.1. The patient is known to be ill in cycle 1, which cannot happen with
 * «cure».
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTemporalEvolutionWithEvidenceTest {

    /** In cycle 2: healthy and ill with «cure», then healthy and ill with «wait». */
    private double[] inCycle2;

    private static Variable patientInCycle(int cycle) {
        Variable variable = new Variable("Patient", "healthy", "ill");
        variable.setBaseName("Patient");
        variable.setTimeSlice(cycle);
        return variable;
    }

    @BeforeEach void askForTheEvolution() throws Exception {
        ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(3);
        Variable now = patientInCycle(0);
        Variable next = patientInCycle(1);
        Variable therapy = new Variable("Therapy", "cure", "wait");
        Node nowNode = probNet.addNode(now, NodeType.CHANCE);
        Node nextNode = probNet.addNode(next, NodeType.CHANCE);
        Node therapyNode = probNet.addNode(therapy, NodeType.DECISION);
        probNet.addLink(nowNode, nextNode, true);
        probNet.addLink(therapyNode, nextNode, true);
        nowNode.setPotential(new TablePotential(List.of(now), PotentialRole.CONDITIONAL_PROBABILITY,
                                                new double[]{ 0.6, 0.4 }));
        nextNode.setPotential(new TablePotential(List.of(next, now, therapy), PotentialRole.CONDITIONAL_PROBABILITY,
                                                 new double[]{ 1, 0, 1, 0, 0.8, 0.2, 0.1, 0.9 }));
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(next, next.getStateIndex("ill")));

        MIDTemporalEvolution evolution = new MIDTemporalEvolution(probNet, now);
        evolution.setPreResolutionEvidence(evidence);
        evolution.setDecisionVariable(therapy);
        Map<Variable, TablePotential> values = evolution.getTemporalEvolution();
        inCycle2 = values.entrySet().stream()
                         .filter(entry -> entry.getKey().getTimeSlice() == 2)
                         .findFirst().orElseThrow().getValue().getValues();
    }

    @Test void theProbabilitiesOfAnOptionAddUpToOne() {
        assertArrayEquals(new double[]{ 0.1, 0.9 }, Arrays.copyOfRange(inCycle2, 2, 4), 1E-9);
    }

    @Test void theOptionWithWhichTheEvidenceIsImpossibleIsLeftAtZero() {
        assertArrayEquals(new double[]{ 0, 0 }, Arrays.copyOfRange(inCycle2, 0, 2), 0);
    }
}
