package org.openmarkov.learning.metric.cmi.accuracy;

import org.openmarkov.core.model.database.CaseDatabase;

import java.util.Arrays;
import java.util.Random;

/**
 * Splits a case database into k-fold training and test sets for cross-validation.
 */
public class Dataset {



    private int[][][] training;
    private int[][][] test;
    private int[][] cases;
    private int sampleSize;
    private int numOfSamples;
    private final Random random;


    /**
     * Constructs a Dataset by splitting the case database into k folds.
     *
     * @param cdb            the case database to split
     * @param sampleFraction the number of folds (k)
     */
    public Dataset(CaseDatabase cdb, int sampleFraction){
        this(cdb, sampleFraction, 42L);
    }

    /**
     * Constructs a Dataset by splitting the case database into k folds
     * with a specified random seed for reproducibility.
     *
     * @param cdb            the case database to split
     * @param sampleFraction the number of folds (k)
     * @param seed           the random seed
     */
    public Dataset(CaseDatabase cdb, int sampleFraction, long seed){
        cases=cdb.getCases();
        numOfSamples = sampleFraction;
        training = new int[sampleFraction][][];
        test = new int[sampleFraction][][];
        sampleSize=cases.length/sampleFraction;
        random = new Random(seed);
        initializeDatasets();
    }


    /**
     * Shuffles the cases once and deals them into disjoint test sets that cover them all;
     * the training set of each fold is made of the other test sets.
     */
    private void initializeDatasets(){
        int[][] shuffled = cases.clone();
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] swapped = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = swapped;
        }

        // The cases left over by the division go one to each of the first folds
        int remainder = cases.length % numOfSamples;
        int start = 0;
        for (int it = 0; it < numOfSamples; it++) {
            int end = start + sampleSize + (it < remainder ? 1 : 0);
            test[it] = Arrays.copyOfRange(shuffled, start, end);
            training[it] = new int[cases.length - (end - start)][];
            System.arraycopy(shuffled, 0, training[it], 0, start);
            System.arraycopy(shuffled, end, training[it], start, cases.length - end);
            start = end;
        }
    }


    public int[][][] getTraining() {
        return training;
    }

    public int[][][] getTest() {
        return test;
    }

    /**
     * Returns whether this dataset has no usable data.
     *
     * @return true if cases, test, or training data is null or empty
     */
    public boolean isEmpty(){
        return  this.cases==null ||this.test==null || this.training==null
                ||this.cases.length==0 || this.test.length==0 || this.training.length ==0;
    }

}
