/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.cmi.accuracy;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/** The accuracy with which the superparent classifier decides whether a link between two features helps. */
class SuperParentLinkAccuracyTest {

    /**
     * The class is 1 when the two features differ, so neither feature tells anything alone
     * and the link between them tells everything.
     */
    @Test
    void aLinkBetweenTwoFeaturesCanImproveTheAccuracy() {
        Variable classVariable = new Variable("Class", 2);
        Variable first = new Variable("First", 2);
        Variable second = new Variable("Second", 2);
        List<Variable> variables = List.of(classVariable, first, second);
        Random random = new Random(7);
        int[][] cases = new int[400][3];
        for (int[] row : cases) {
            row[1] = random.nextInt(2);
            row[2] = random.nextInt(2);
            row[0] = row[1] ^ row[2];
        }
        ProbNet net = new ProbNet();
        variables.forEach(variable -> net.addNode(variable, NodeType.CHANCE));
        Accuracy metric = new Accuracy();
        metric.setClassVariable("Class");
        metric.setAugmentedNet(true);
        metric.setAlpha(0.5);
        metric.init(net, new CaseDatabase(new ArrayList<>(variables), cases));
        metric.getScore();

        double withoutLinks = metric.computeAugmentedNetAccuracy(List.of());
        double withTheLink = metric.computeAugmentedNetAccuracy(List.<Variable[]>of(new Variable[]{first, second}));

        assertThat(withoutLinks).isLessThan(0.65);
        assertThat(withTheLink).isGreaterThan(0.95);
    }
}
