package org.openmarkov.bnEvaluation;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the splitting of a {@link CaseDatabase} into training and test sets
 * using various strategies: random selection, first/last cases, cross-validation,
 * and multiple random samples.
 */
public class SplitSetManager {
    
    private CaseDatabase caseDatabase;
    private int numCases;
    
    /**
     * Creates a manager for the given case database.
     *
     * @param caseDatabase the database to split
     */
    public SplitSetManager(CaseDatabase caseDatabase) {
        this.caseDatabase = caseDatabase;
        numCases = caseDatabase.getNumCases();
    }
    
    /**
     * This method creates the test and training caseDatabases by
     * random selection
     *
     * @param numTest number of test cases
     * @return the resulting split containing test and training databases
     */
    public SplitSet generateRandomTestSet(int numTest) {
        //database information
        ArrayList<Variable> listaVariables = (ArrayList<Variable>) caseDatabase.getVariables();
        int numVariables = listaVariables.size();
        int[][] cases = caseDatabase.getCases();
        //declare test and train cases
        int numTrain = numCases - numTest;
        int[][] test_cases = new int[numTest][numVariables];
        int[][] train_cases = new int[numTrain][numVariables];
        // sample (of size numTest) of the numCases
        int[] sample = getSample(numTest, numCases);
        // test_index is a indicator: test_index[i]=1 if the i-th element is selected for test set
        int[] test_index = new int[numCases];
        for (int i = 0; i < numTest; i++) {
            test_index[sample[i]] = 1;
        }
        // split the set in test-set and train-set with test_index
        int itest = 0;
        int itrain = 0;
        for (int i = 0; i < numCases; i++) {
            if (test_index[i] == 1) {
                for (int j = 0; j < numVariables; j++) {
                    test_cases[itest][j] = cases[i][j];
                }
                itest = itest + 1;
            } else {
                for (int j = 0; j < numVariables; j++) {
                    train_cases[itrain][j] = cases[i][j];
                }
                itrain = itrain + 1;
            }
        }
        CaseDatabase testDatabase = new CaseDatabase(listaVariables, test_cases);
        CaseDatabase trainDatabase = new CaseDatabase(listaVariables, train_cases);
        SplitSet splitSet = new SplitSet(testDatabase, trainDatabase);
        return splitSet;
    }
    
    /**
     * This method calculates a random sample of size n of the m first numbers
     * (starting from 0)
     *
     * @param n: size of sample
     * @param m: size of population
     *
     * @return []int : array (of size n) of the first m numbers (starting from): 0,1...,m-1
     * randomly ordered
     */
    private int[] getSample(int n, int m) {
        if (n > m) return null;
        // array to save the sample
        int[] sample = new int[n];
        // the first m numbers are entered into the population
        List<Integer> population = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            population.add(i);
        }
        // Crear un generador de números aleatorios
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            // random index from 0 to population.size()-1
            int index = random.nextInt(population.size());
            sample[i] = population.get(index);
            population.remove(index);
        }
        return sample;
    }
    
    /**
     * This method randomly assigns n values of 1 and m-n values of 0
     *
     * @param n: number of 1
     * @param m: size of population (number of 1 and 0)
     *
     * @return []int : vector of size m whit 0 or 1
     */
    private int[] getSplitSample(int n, int m) {
        if (n > m) return null;
        // array to save the sample. inicialize to 0
        int[] sample = new int[m];
        for (int i = 0; i < m; i++) {
            sample[i] = 0;
        }
        // the first m numbers are entered into the population
        List<Integer> population = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            population.add(i);
        }
        // random number generator
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            // random index from 0 to population.size()-1
            int index = random.nextInt(population.size());
            sample[population.get(index)] = 1;
            population.remove(index);
        }
        return sample;
    }
    
    /**
     * This method creates the test and training databases by
     * selecting the first cases for tne test-set.
     *
     * @param numTest number of cases to use as the test set
     * @return the resulting split containing test and training databases
     */
    public SplitSet generateFirstTestSet(int numTest) {
        ArrayList<Variable> listaVariables = (ArrayList<Variable>) caseDatabase.getVariables();
        int numVariables = listaVariables.size();
        int[][] cases = caseDatabase.getCases();
        //declare test and train cases
        int numTrain = numCases - numTest;
        int[][] test_cases = new int[numTest][numVariables];
        int[][] train_cases = new int[numTrain][numVariables];
        for (int i = 0; i < numTest; i++) {
            for (int j = 0; j < numVariables; j++) {
                test_cases[i][j] = cases[i][j];
            }
        }
        for (int i = 0; i < numTrain; i++) {
            for (int j = 0; j < numVariables; j++) {
                train_cases[i][j] = cases[i + numTest][j];
            }
        }
        CaseDatabase testDatabase = new CaseDatabase(listaVariables, test_cases);
        CaseDatabase trainDatabase = new CaseDatabase(listaVariables, train_cases);
        SplitSet splitSet = new SplitSet(testDatabase, trainDatabase);
        return splitSet;
        
    }
    
    /**
     * This method creates the test and training databases by
     * selecting the last cases for tne test-set.
     *
     * @param numTest number of cases to use as the test set
     * @return the resulting split containing test and training databases
     */
    public SplitSet generateLastTestSet(int numTest) {
        ArrayList<Variable> listaVariables = (ArrayList<Variable>) caseDatabase.getVariables();
        int numVariables = listaVariables.size();
        int[][] cases = caseDatabase.getCases();
        //declare test and train cases
        int numTrain = numCases - numTest;
        int[][] test_cases = new int[numTest][numVariables];
        int[][] train_cases = new int[numTrain][numVariables];
        for (int i = 0; i < numTrain; i++) {
            for (int j = 0; j < numVariables; j++) {
                train_cases[i][j] = cases[i][j];
            }
        }
        for (int i = 0; i < numTest; i++) {
            for (int j = 0; j < numVariables; j++) {
                test_cases[i][j] = cases[numTrain + i][j];
            }
        }
        CaseDatabase testDatabase = new CaseDatabase(listaVariables, test_cases);
        CaseDatabase trainDatabase = new CaseDatabase(listaVariables, train_cases);
        SplitSet splitSet = new SplitSet(testDatabase, trainDatabase);
        return splitSet;
    }
    
    /**
     * This method make k partitions of the caseDatabase.
     * Splits the set of cases into k sets; for each k,
     * stores the k-th set as test-set and the rest as a training-set.
     *
     * @param K number of partitions (folders)
     *
     * @return array with K pairs of test-set and train-set
     */
    public SplitSet[] crossValidation(int K) {
        SplitSet[] sets = new SplitSet[K];
        List<Variable> listaVariables = caseDatabase.getVariables();
        int numVariables = listaVariables.size();
        int n = numCases / K;
        int resto = numCases - n * K;
        int numTrainCases = 0;
        int numTestCases = 0;
        if (resto == 0) {
            numTestCases = n;
            numTrainCases = numCases - numTestCases;
        }
        int[][] cases = caseDatabase.getCases();
        //random index
        int[] random_index = getSample(numCases, numCases);
        // loop in k (folders)
        for (int k = 0; k < K; k++) {
            if (resto > 0) {
                numTestCases = (k <= resto) ? (n + 1) : n;
                numTrainCases = numCases - numTestCases;
            }
            int[][] testCases = new int[numTestCases][numVariables];
            int[][] trainCases = new int[numTrainCases][numVariables];
            int ik_test_index = 0;
            int ik_train_index = 0;
            // split the loop in cases in three loops: before-test, test, after-test
            int indexStarsTest = (k <= resto) ? (k * numTestCases) : (k * n + resto);
            int indexEndsTest = indexStarsTest + numTestCases;
            for (int i = 0; i < indexStarsTest; i++) {
                for (int j = 0; j < numVariables; j++) {
                    trainCases[ik_train_index][j] = cases[random_index[i]][j];
                }
                ik_train_index = ik_train_index + 1;
            }
            for (int i = indexStarsTest; i < indexEndsTest; i++) {
                for (int j = 0; j < numVariables; j++) {
                    testCases[ik_test_index][j] = cases[random_index[i]][j];
                }
                ik_test_index = ik_test_index + 1;
            }
            for (int i = indexEndsTest; i < numCases; i++) {
                for (int j = 0; j < numVariables; j++) {
                    trainCases[ik_train_index][j] = cases[random_index[i]][j];
                }
                ik_train_index = ik_train_index + 1;
            }
            //end loop for the numCases of k-fold
            CaseDatabase testDatabase = new CaseDatabase(listaVariables, testCases);
            CaseDatabase trainDatabase = new CaseDatabase(listaVariables, trainCases);
            sets[k] = new SplitSet(testDatabase, trainDatabase);
        }// look in k
        return sets;
    }
    
    /**
     * This method generate n samples of size m from the data set; for each sample,
     * generates two subsets: the sample (test cases) and the rest (training cases)
     *
     * @param n number of samples
     * @param m sample size
     *
     * @return array with n pairs of test-set and train-set
     */
    public SplitSet[] multipleSamples(int n, int m) {
        SplitSet[] sets = new SplitSet[n];
        List<Variable> listaVariables = caseDatabase.getVariables();
        int numVariables = listaVariables.size();
        int[][] cases = caseDatabase.getCases();
        int numTestCases = m;
        int numTrainCases = numCases - m;
        // loop in number of samples
        for (int isample = 0; isample < n; isample++) {
            int[][] testCases = new int[numTestCases][numVariables];
            int[][] trainCases = new int[numTrainCases][numVariables];
            int itest = 0;
            int itrain = 0;
            int[] random_index = getSplitSample(m, numCases);
            //loop in cases
            for (int i = 0; i < numCases; i++) {
                if (random_index[i] == 1) {
                    for (int j = 0; j < numVariables; j++) {
                        testCases[itest][j] = cases[i][j];
                    }
                    itest = itest + 1;
                } else {
                    for (int j = 0; j < numVariables; j++) {
                        trainCases[itrain][j] = cases[i][j];
                    }
                    itrain = itrain + 1;
                }
            }
            CaseDatabase testDatabase = new CaseDatabase(listaVariables, testCases);
            CaseDatabase trainDatabase = new CaseDatabase(listaVariables, trainCases);
            sets[isample] = new SplitSet(testDatabase, trainDatabase);
        }
        return sets;
    }
    
}
