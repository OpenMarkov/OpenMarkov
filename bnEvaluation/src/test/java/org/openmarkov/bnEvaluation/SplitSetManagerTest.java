/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitSetManagerTest {

    /** A database of {@code numCases} cases, each carrying a unique value 0..numCases-1. */
    private static CaseDatabase db(int numCases) {
        Variable v = new Variable("V", numCases);
        int[][] cases = new int[numCases][1];
        for (int i = 0; i < numCases; i++) {
            cases[i][0] = i;
        }
        return new CaseDatabase(List.of(v), cases);
    }

    private static List<Integer> values(int[][] cases) {
        List<Integer> out = new ArrayList<>();
        for (int[] row : cases) {
            out.add(row[0]);
        }
        return out;
    }

    /**
     * Regression for B-CV: crossValidation(K) must produce K folds that form a valid
     * partition of the cases regardless of whether numCases is a multiple of K.
     */
    @Test
    void crossValidationProducesValidPartition() {
        int[][] scenarios = {
                { 12, 4 }, { 10, 5 },            // resto = 0
                { 6, 4 }, { 10, 4 }, { 13, 5 },  // 0 < resto < K-1
                { 7, 4 }, { 9, 5 }, { 11, 4 },   // resto = K-1 (used to crash)
                { 7, 3 }, { 9, 4 }               // resto = 1
        };
        for (int[] sc : scenarios) {
            checkPartition(sc[0], sc[1]);
        }
    }

    private static void checkPartition(int numCases, int K) {
        String tag = "numCases=" + numCases + ", K=" + K;
        SplitSetManager manager = new SplitSetManager(db(numCases));

        SplitSet[] sets = assertDoesNotThrow(() -> manager.crossValidation(K), tag);
        assertEquals(K, sets.length, tag + ": number of folds");

        int n = numCases / K;
        int resto = numCases % K;
        List<Integer> allExpected = IntStream.range(0, numCases).boxed().sorted().toList();

        List<Integer> allTestValues = new ArrayList<>();
        int largeFolds = 0;
        for (SplitSet fold : sets) {
            List<Integer> test = values(fold.getTestDatabase().getCases());
            List<Integer> train = values(fold.getTrainDatabase().getCases());

            // Each fold splits ALL cases into test + train (no case dropped or duplicated within a fold).
            List<Integer> foldAll = new ArrayList<>(test);
            foldAll.addAll(train);
            Collections.sort(foldAll);
            assertEquals(allExpected, foldAll, tag + ": a fold must partition all cases into test+train");

            // Fold test size is balanced: n or n+1.
            assertTrue(test.size() == n || test.size() == n + 1, tag + ": test fold size = " + test.size());
            if (test.size() == n + 1) {
                largeFolds++;
            }
            allTestValues.addAll(test);
        }

        // Exactly `resto` folds carry the extra case.
        assertEquals(resto, largeFolds, tag + ": number of larger folds");

        // The K test sets together form a partition of all cases: disjoint and exhaustive.
        Collections.sort(allTestValues);
        assertEquals(allExpected, allTestValues, tag + ": test sets must be a partition of all cases");
    }

    /**
     * Regression for B-Random: a manager built with a seed must produce reproducible splits,
     * so an evaluation can be repeated and compared. Covers both random sources
     * (getSample via crossValidation, getSplitSample via multipleSamples).
     */
    @Test
    void sameSeedProducesIdenticalSplits() {
        CaseDatabase database = db(10);
        assertSameSplits(new SplitSetManager(database, 42L).crossValidation(4),
                new SplitSetManager(database, 42L).crossValidation(4));
        assertSameSplits(new SplitSetManager(database, 7L).multipleSamples(3, 4),
                new SplitSetManager(database, 7L).multipleSamples(3, 4));
    }

    /**
     * P5: crossValidation must reject a number of folds outside [2, numCases] with a clear
     * exception instead of silently producing folds with an empty training or test set.
     */
    @Test
    void crossValidationRejectsOutOfRangeFolds() {
        CaseDatabase database = db(5);
        assertThrows(IllegalArgumentException.class,
                () -> new SplitSetManager(database).crossValidation(1));   // K=1 leaves an empty training set
        assertThrows(IllegalArgumentException.class,
                () -> new SplitSetManager(database).crossValidation(6));   // more folds than cases
        // Boundary values are valid.
        assertDoesNotThrow(() -> new SplitSetManager(database).crossValidation(2));
        assertDoesNotThrow(() -> new SplitSetManager(database).crossValidation(5));
    }

    /**
     * P5: multipleSamples must reject a sample larger than the population (which used to make the
     * internal sampling return null and fail confusingly later) and a non-positive sample count.
     */
    @Test
    void multipleSamplesRejectsInvalidArguments() {
        CaseDatabase database = db(5);
        assertThrows(IllegalArgumentException.class,
                () -> new SplitSetManager(database).multipleSamples(3, 6));  // sample bigger than population
        assertThrows(IllegalArgumentException.class,
                () -> new SplitSetManager(database).multipleSamples(0, 3));  // no samples requested
        assertDoesNotThrow(() -> new SplitSetManager(database).multipleSamples(3, 5));
    }

    private static void assertSameSplits(SplitSet[] a, SplitSet[] b) {
        assertEquals(a.length, b.length);
        for (int k = 0; k < a.length; k++) {
            assertEquals(values(a[k].getTestDatabase().getCases()), values(b[k].getTestDatabase().getCases()),
                    "test split " + k);
            assertEquals(values(a[k].getTrainDatabase().getCases()), values(b[k].getTrainDatabase().getCases()),
                    "train split " + k);
        }
    }
}
