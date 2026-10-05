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

/** The counts of (feature, feature, class) that the superparent classifier uses to weigh its links. */
class SuperParentCountsTest {

    /** Three different numbers of states, so that a wrong order of the variables cannot go unnoticed. */
    @Test
    void eachCellHoldsTheCountOfItsOwnCombination() {
        Variable classVariable = new Variable("Class", 2);
        Variable first = new Variable("First", 3);
        Variable second = new Variable("Second", 4);
        List<Variable> variables = List.of(classVariable, first, second);
        Random random = new Random(7);
        int[][] cases = new int[200][3];
        for (int[] row : cases) {
            row[0] = random.nextInt(2);
            row[1] = random.nextInt(3);
            row[2] = random.nextInt(4);
        }
        CaseDatabase database = new CaseDatabase(new ArrayList<>(variables), cases);
        ProbNet net = new ProbNet();
        variables.forEach(variable -> net.addNode(variable, NodeType.CHANCE));
        Accuracy metric = new Accuracy();
        metric.setClassVariable("Class");
        metric.setAugmentedNet(true);
        metric.init(net, database);
        metric.getScore();

        double[][][][] counts = metric.build2ndLevelProbDistribution(net.getNode(first), net.getNode(second));

        // The metric splits the cases with the default seed, so this split is the same one
        int[][][] training = new Dataset(database, Accuracy.KFOLD).getTraining();
        for (int fold = 0; fold < Accuracy.KFOLD; fold++) {
            double[][][] expected = new double[3][4][2];
            for (int[] row : training[fold]) {
                expected[row[1]][row[2]][row[0]]++;
            }
            assertThat(counts[fold]).as("fold %d", fold).isDeepEqualTo(expected);
        }
    }
}
