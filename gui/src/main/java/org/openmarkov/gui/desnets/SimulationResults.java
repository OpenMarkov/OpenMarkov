package org.openmarkov.gui.desnets;

import org.apache.commons.math3.stat.StatUtils;

import javax.swing.JOptionPane;

/**
 * Simulation results stores a set of results of n simulations and calculates statistics
 * Using StatUtils
 *
 * @author cmyago
 * @version 1 30/12/2021
 */
public class SimulationResults {
    private final double[] values;

    private final double[] discountedValues;

    private double mean = Double.NaN;
    private double sampleVariance = Double.NaN;
    private double sampleSD = Double.NaN;

    private double discountedMean = Double.NaN;
    private double discountedSampleVariance = Double.NaN;
    private double discountedSampleSD = Double.NaN;


    SimulationResults() {
        this(1);
    }


    public SimulationResults(int numberOfValues) {
        values = new double[numberOfValues];
        discountedValues = new double[numberOfValues];
    }


    /**
     * Computes the mean and the variance of values and discountedValues, the only statistics the results show
     */
    public void calculateStatisticalProperties() {
        mean = StatUtils.mean(values);
//        Returns 0 for a single-value (i.e. length = 1) sample.
        sampleVariance = StatUtils.variance(values, mean);
        sampleSD = Math.sqrt(sampleVariance);

        discountedMean = StatUtils.mean(discountedValues);
//        Returns 0 for a single-value (i.e. length = 1) sample.
        discountedSampleVariance = StatUtils.variance(discountedValues, discountedMean);
        discountedSampleSD = Math.sqrt(discountedSampleVariance);
    }

    /**
     * This method return the stored values
     *
     * @return
     */
    public double[] getValues() {
        return values;
    }

    /**
     * This method returns the first value. To be used when there is only one value
     *
     * @return
     */
    public double getValue() {
        return getValue(0);
    }


    /**
     * This method returns the value stored in position
     *
     * @param position
     * @return
     */
    public double getValue(int position) {
        try {
            return values[position];
        } catch (ArrayIndexOutOfBoundsException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "SimulationResultsOD#getValue()", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    public double getDiscountedValue() {
        return getDiscountedValue(0);
    }

    /**
     * This method returns the value stored in position
     *
     * @param position
     * @return
     */
    public double getDiscountedValue(int position) {
        try {
            return discountedValues[position];
        } catch (ArrayIndexOutOfBoundsException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "SimulationResultsOD#getValue()", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }


    /**
     * This method set the value in the first position (position 0). To be used when there is only one value
     *
     * @param value
     * @param discountedValue
     */
    public void setValue(double value, double discountedValue) {
        setValue(0, value, discountedValue);
    }

    /**
     * This method set the value in position
     *
     * @param position
     * @param value
     * @return
     */
    public void setValue(int position, double value, double discountedValue) {
        try {
            values[position] = value;
            discountedValues[position] = discountedValue;
        } catch (ArrayIndexOutOfBoundsException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "SimulationResultsOD#setValue()", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This methods increments the first position of values in valueToAdd
     *
     * @param valueToAdd
     * @param discountedValueToAdd
     */
    public void sumValue(double valueToAdd, double discountedValueToAdd) {

        sumValue(0, valueToAdd, discountedValueToAdd);
    }

    /**
     * This methods increments the value stored in postion  in valueToAdd
     *
     * @param position
     * @param valueToAdd
     * @param discountedValueToAdd
     */
    public void sumValue(int position, double valueToAdd, double discountedValueToAdd) {
        try {
            values[position] += valueToAdd;
            discountedValues[position] += discountedValueToAdd;
        } catch (ArrayIndexOutOfBoundsException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "SimulationResultsOD#sumValue()", JOptionPane.ERROR_MESSAGE);
        }

    }

    public double getMean() {
        return mean;
    }

    public double getSampleVariance() {
        return sampleVariance;
    }

    public double getSampleSD() {
        return sampleSD;
    }

    public double getDiscountedMean() {
        return discountedMean;
    }

    public double getDiscountedSampleVariance() {
        return discountedSampleVariance;
    }

    public double getDiscountedSampleSD() {
        return discountedSampleSD;
    }

    @Override
    public String toString() {
        return "Mean;" + mean + ";DiscountedMean;" + discountedMean;
    }
}
