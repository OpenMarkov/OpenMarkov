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

/** The accuracy that the selective naive Bayes uses to choose features. */
class NaiveBayesAccuracyTest {

    private static final String CLASS = "Class";

    /** With many informative features the product of probabilities must not vanish. */
    @Test
    void manyInformativeFeaturesAreNotPredictedAsTheFirstClass() {
        int numCases = 2000;
        int numFeatures = 150;
        Random random = new Random(7);
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable(CLASS, 2));
        for (int f = 0; f < numFeatures; f++) {
            variables.add(new Variable("F" + f, 2));
        }
        int[][] cases = new int[numCases][numFeatures + 1];
        for (int[] row : cases) {
            row[0] = random.nextInt(2);
            for (int f = 1; f <= numFeatures; f++) {
                // Each feature equals the class with probability 0.7
                row[f] = random.nextDouble() < 0.7 ? row[0] : 1 - row[0];
            }
        }

        double accuracy = naiveBayesAccuracy(variables, cases);

        assertThat(accuracy).isGreaterThan(0.95);
    }

    /** A feature with a different state in every case tells nothing about an unseen case. */
    @Test
    void aFeatureThatIdentifiesEachCaseDoesNotBeatGuessing() {
        int numCases = 400;
        Random random = new Random(7);
        List<Variable> variables = List.of(new Variable(CLASS, 2), new Variable("Id", numCases));
        int[][] cases = new int[numCases][2];
        for (int i = 0; i < numCases; i++) {
            cases[i][0] = random.nextInt(2);
            cases[i][1] = i;
        }

        double accuracy = naiveBayesAccuracy(variables, cases);

        assertThat(accuracy).isLessThan(0.65);
    }

    /** Accuracy of the naive Bayes whose features are all the variables but the first, which is the class. */
    private static double naiveBayesAccuracy(List<Variable> variables, int[][] cases) {
        ProbNet net = new ProbNet();
        for (Variable variable : variables) {
            net.addNode(variable, NodeType.CHANCE);
        }
        Accuracy metric = new Accuracy();
        metric.setClassVariable(CLASS);
        metric.init(net, new CaseDatabase(new ArrayList<>(variables), cases));
        metric.getScore();
        return metric.computeNBNetAccuracy(variables.subList(1, variables.size()));
    }
}
