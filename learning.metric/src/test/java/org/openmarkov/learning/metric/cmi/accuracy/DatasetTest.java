/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.cmi.accuracy;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The ten folds with which the selective and the superparent classifiers measure their accuracy. */
class DatasetTest {

    private static final int FOLDS = 10;

    @Test
    void eachCaseIsTestedOnceAndTrainsTheOtherFolds() {
        // 1003 is not a multiple of ten, so the folds cannot all have the same size
        int numCases = 1003;

        Dataset dataset = dataset(numCases);

        int[] timesTested = new int[numCases];
        int[] timesTrained = new int[numCases];
        for (int fold = 0; fold < FOLDS; fold++) {
            assertThat(dataset.getTest()[fold].length).as("size of fold %d", fold).isBetween(100, 101);
            boolean[] testedInThisFold = new boolean[numCases];
            for (int[] row : dataset.getTest()[fold]) {
                timesTested[row[0]]++;
                testedInThisFold[row[0]] = true;
            }
            for (int[] row : dataset.getTraining()[fold]) {
                timesTrained[row[0]]++;
                assertThat(testedInThisFold[row[0]]).as("case %d trains the fold that tests it", row[0]).isFalse();
            }
        }
        assertThat(timesTested).containsOnly(1);
        assertThat(timesTrained).containsOnly(FOLDS - 1);
    }

    @Test
    void fewerCasesThanFoldsLeaveSomeFoldsEmpty() {
        Dataset dataset = dataset(3);

        int tested = 0;
        for (int fold = 0; fold < FOLDS; fold++) {
            tested += dataset.getTest()[fold].length;
            assertThat(dataset.getTest()[fold].length + dataset.getTraining()[fold].length).isEqualTo(3);
        }
        assertThat(tested).isEqualTo(3);
    }

    @Test
    void theSplitIsRepeatable() {
        assertThat(dataset(1003).getTest()).isDeepEqualTo(dataset(1003).getTest());
    }

    /** A database with a single variable whose value in each case is the position of the case. */
    private static Dataset dataset(int numCases) {
        int[][] cases = new int[numCases][1];
        for (int i = 0; i < numCases; i++) {
            cases[i][0] = i;
        }
        return new Dataset(new CaseDatabase(new ArrayList<>(List.of(new Variable("Id", numCases))), cases), FOLDS);
    }
}
