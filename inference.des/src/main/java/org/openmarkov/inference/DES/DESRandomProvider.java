package org.openmarkov.inference.DES;

import umontreal.ssj.rng.MRG31k3p;
import umontreal.ssj.rng.RandomStream;

import java.util.ArrayList;
import java.util.Random;

/**
 * Manages the use of the random numbers used by a node in a DESNet to ensure the same numbers are used in each intervention
 * For each individual keeps memory of the random numbers used in every intervention to ensure the same random numbers are used
 * Each time the node needs a random number it uses one from the list until it exhaust it. Then a new number is added to the list from a Random Stream
 *
 * @author cmyago
 * @version 1.0 21/08/2022
 * @version 1.1 24/10/2023 - adapted for potentitials requiring an indetermed number of random numbers
 */
public class DESRandomProvider {
    /**
     * Random generator for the simulations
     */
    private final static Random RANDOM = new Random();
    /**
     * Stores the random numbers consumed by one individual;
     * ensuring the same numbers are consumed in each intervention
     */
    private final ArrayList<Double> randomNumbers = new ArrayList<>();
    /**
     * Random Stream seed
     */
    protected int[] seed;
    /**
     * Index of the next number to be consumed from randomNumbers
     */
    private int nextIndex;
    private RandomStream randomStream;

    /**
     * Resets the seed of randomGenerator
     */
    public void setRandomGenerator() {
        generateSeed();
        MRG31k3p.setPackageSeed(seed);
        randomStream = new MRG31k3p();
    }

    /**
     * Generates a new seed
     */
    private void generateSeed() {
        seed = new int[]{
                RANDOM.nextInt() % 2147483647,
                RANDOM.nextInt() % 2147483647,
                RANDOM.nextInt() % 2147483647,
                RANDOM.nextInt() % 2147462579,
                RANDOM.nextInt() % 2147462579,
                RANDOM.nextInt() % 2147462579
        };
    }

    /**
     * Returns generated random number
     */
    public double getRandomNumber() {
        if (randomNumbers.size() == nextIndex) {
            randomNumbers.add(randomStream.nextDouble());
        }
        return randomNumbers.get(nextIndex++);
    }


    /**
     * Returns an numNumbers random numbers from the random stream
     * @param numNumbers amount of numbers required
     * @return  an array of generated random numbers
     */
    public double[] getRandomNumbers(int numNumbers) {
        double[] randomNumbers = new double[numNumbers];
        for (int i = 0; i < randomNumbers.length; i++) {
            randomNumbers[i] =getRandomNumber();
        }
        return randomNumbers;
    }

    /**
     * Clears the random number ArrayList to be used with new random numbers for the next individual
     */
    public void nextIndividual() {
        randomNumbers.clear();
        nextIndex = 0;
    }

    public void resetIndex() {
        nextIndex = 0;
    }
}
