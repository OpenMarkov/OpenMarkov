package org.openmarkov.learning.metric.cmi.accuracy;

import org.openmarkov.core.model.database.CaseDatabase;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

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


    private void initializeDatasets(){

        IntStream.range(0, numOfSamples).forEach(it ->{

            int maxCase = random.nextInt(cases.length-sampleSize-1);
            test[it]= Arrays.copyOfRange(cases, maxCase, maxCase+sampleSize);
            training[it] = new int[cases.length-sampleSize][];

            for(int i = 0; i<maxCase;i++){
                training[it][i]=cases[i];
            }
            for(int i=maxCase+sampleSize; i<cases.length;i++){
                training[it][i-sampleSize]=cases[i];
            }
        });
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
