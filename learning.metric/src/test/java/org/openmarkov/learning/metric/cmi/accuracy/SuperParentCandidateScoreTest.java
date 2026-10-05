/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.cmi.accuracy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/** The superparent classifier scores a change with the accuracy that the network has once the change is made. */
class SuperParentCandidateScoreTest {

    private final Variable classVariable = new Variable("Class", 2);
    private final Variable x = new Variable("X", 3);
    private final Variable y = new Variable("Y", 2);
    private final Variable z = new Variable("Z", 4);
    private ProbNet net;
    private Accuracy metric;

    /** A naive Bayes over features that depend on each other, with different numbers of states. */
    @BeforeEach
    void setUp() {
        List<Variable> variables = List.of(classVariable, x, y, z);
        Random random = new Random(7);
        int[][] cases = new int[600][4];
        for (int[] row : cases) {
            row[0] = random.nextInt(2);
            row[1] = (row[0] + random.nextInt(2)) % 3;
            row[2] = random.nextDouble() < 0.8 ? (row[1] + row[0]) % 2 : random.nextInt(2);
            row[3] = random.nextDouble() < 0.8 ? row[1] + row[2] : random.nextInt(4);
        }
        net = new ProbNet();
        variables.forEach(variable -> net.addNode(variable, NodeType.CHANCE));
        for (Variable feature : List.of(x, y, z)) {
            link(classVariable, feature);
        }
        metric = new Accuracy();
        metric.setClassVariable("Class");
        metric.setAugmentedNet(true);
        metric.setAlpha(0.5);
        metric.init(net, new CaseDatabase(new ArrayList<>(variables), cases));
    }

    @Test
    void aCandidateLinkScoresTheSameOnceAdded() {
        // Y and Z get a second parent, so that only the link under test changes
        link(y, z);
        double asCandidate = metric.getScore(new AddLinkEdit(net, x, y, true));

        link(x, y);

        assertThat(accuracyOfTheNet()).isEqualTo(asCandidate);
    }

    @Test
    void aCandidateSuperParentScoresTheSameOnceItsLinksAreAdded() {
        double asCandidate = metric.getScore(new AddLinkEdit(net, classVariable, x, true));

        link(x, y);
        link(x, z);

        assertThat(accuracyOfTheNet()).isEqualTo(asCandidate);
    }

    /**
     * Accuracy with the links that the network already has: the score of choosing X as superparent
     * when every other feature has a second parent, which adds nothing.
     */
    private double accuracyOfTheNet() {
        return metric.getScore(new AddLinkEdit(net, classVariable, x, true));
    }

    private void link(Variable from, Variable to) {
        net.addLink(net.getNode(from), net.getNode(to), true);
    }
}
