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

/** A feature that hangs from another one counts once when the superparent classifier measures its accuracy. */
class SuperParentChildCountedOnceTest {

    private static final int NUM_CASES = 2000;

    /**
     * The copy is always equal to the original, so given the original it tells nothing about the class:
     * with the link between them, the classifier must predict as if the copy were not there.
     */
    @Test
    void aChildThatCopiesItsParentDoesNotChangeTheAccuracy() {
        Variable classVariable = new Variable("Class", 2);
        Variable original = new Variable("Original", 2);
        Variable copy = new Variable("Copy", 2);
        Random random = new Random(7);
        int[][] withCopy = new int[NUM_CASES][3];
        int[][] withoutCopy = new int[NUM_CASES][2];
        for (int i = 0; i < NUM_CASES; i++) {
            // An unbalanced class: counting the evidence twice outweighs the prior and changes the prediction
            int classValue = random.nextDouble() < 0.8 ? 0 : 1;
            int value = random.nextDouble() < (classValue == 0 ? 0.2 : 0.6) ? 1 : 0;
            withCopy[i] = new int[]{classValue, value, value};
            withoutCopy[i] = new int[]{classValue, value};
        }

        double withTheLink = metric(List.of(classVariable, original, copy), withCopy)
                .computeAugmentedNetAccuracy(List.<Variable[]>of(new Variable[]{copy, original}));
        double withoutTheCopy = metric(List.of(classVariable, original), withoutCopy)
                .computeAugmentedNetAccuracy(List.of());

        assertThat(withTheLink).isEqualTo(withoutTheCopy);
    }

    private static Accuracy metric(List<Variable> variables, int[][] cases) {
        ProbNet net = new ProbNet();
        variables.forEach(variable -> net.addNode(variable, NodeType.CHANCE));
        Accuracy metric = new Accuracy();
        metric.setClassVariable("Class");
        metric.setAugmentedNet(true);
        metric.init(net, new CaseDatabase(new ArrayList<>(variables), cases));
        metric.getScore();
        return metric;
    }
}
