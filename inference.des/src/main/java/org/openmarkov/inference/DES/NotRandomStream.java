package org.openmarkov.inference.DES;

import umontreal.ssj.rng.RandomStream;

/**
 * Test class for nuisance variance. Provides a deterministic stream.
 */
public class NotRandomStream implements RandomStream {
    private double sequence = 0.00001;

    /**
     * Reinitializes the stream to its initial state @f$I_g@f$: @f$C_g@f$
     * and @f$B_g@f$ are set to @f$I_g@f$.
     */
    @Override
    public void resetStartStream() {
        sequence = 0.00001;

    }

    /**
     * Reinitializes the stream to the beginning of its current substream:
     *
     * @f$C_g@f$ is set to @f$B_g@f$.
     */
    @Override
    public void resetStartSubstream() {

    }

    /**
     * Reinitializes the stream to the beginning of its next substream:
     *
     * @f$N_g@f$ is computed, and @f$C_g@f$ and @f$B_g@f$ are set to
     * @f$N_g@f$.
     */
    @Override
    public void resetNextSubstream() {

    }

    /**
     * Returns a string containing the current state of this stream.
     *
     * @return the state of the generator formated as a string
     */
    @Override
    public String toString() {
        return null;
    }

    /**
     * Returns a (pseudo)random number from the uniform distribution over
     * the interval @f$(0,1)@f$, using this stream, after advancing its
     * state by one step. The generators programmed in SSJ never return the
     * values 0 or 1.
     *
     * @return the next generated uniform
     */
    @Override
    public double nextDouble() {
        sequence += 0.00001;
        return sequence;
    }

    /**
     * Generates `n` (pseudo)random numbers from the uniform distribution
     * and stores them into the array `u` starting at index `start`.
     *
     * @param u     array that will contain the generated uniforms
     * @param start starting index, in the array `u`, to write
     *              uniforms from
     * @param n     number of uniforms to generate
     */
    @Override
    public void nextArrayOfDouble(double[] u, int start, int n) {

        for (int i = 0; i < n; i++) {
            u[i + start] = nextDouble();

        }
    }

    /**
     * Returns a (pseudo)random number from the discrete uniform
     * distribution over the integers @f$\{i,i+1,…,j\}@f$, using this
     * stream. (Calls `nextDouble` once.)
     *
     * @param i smallest integer that can be generated
     * @param j greatest integer that can be generated
     * @return the generated integer
     */
    @Override
    public int nextInt(int i, int j) {
        return i;
    }

    /**
     * Generates `n` (pseudo)random numbers from the discrete uniform
     * distribution over the integers @f$\{i,i+1,…,j\}@f$, using this
     * stream and stores the result in the array `u` starting at index
     * `start`. (Calls `nextInt` `n` times.)
     *
     * @param i     smallest integer that can be generated
     * @param j     greatest integer that can be generated
     * @param u     array that will contain the generated values
     * @param start starting index, in the array `u`, to write
     *              integers from
     * @param n     number of values being generated
     */
    @Override
    public void nextArrayOfInt(int i, int j, int[] u, int start, int n) {

    }
}
