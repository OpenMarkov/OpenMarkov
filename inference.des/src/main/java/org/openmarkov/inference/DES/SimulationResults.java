package org.openmarkov.inference.DES;

import org.apache.commons.math3.stat.StatUtils;
import org.openmarkov.core.inference.MonteCarloOptions;

import javax.swing.*;

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
    private final double sum = Double.NaN;
    private double sampleVariance = Double.NaN;
    private double sampleSD = Double.NaN;

    private final double median = Double.NaN;
    private final double percentile25 = Double.NaN;
    private final double percentile75 = Double.NaN;
    private final double max = Double.NaN;
    private final double min = Double.NaN;


    private double discountedMean = Double.NaN;
    private final double discountedSum = Double.NaN;
    private double discountedSampleVariance = Double.NaN;
    private double discountedSampleSD = Double.NaN;

    private final double discountedMedian = Double.NaN;
    private final double discountedPercentile25 = Double.NaN;
    private final double discountedPercentile75 = Double.NaN;
    private final double discountedMax = Double.NaN;

    private final double discountedMin = Double.NaN;


    SimulationResults() {
        this(1);
    }


    public SimulationResults(int numberOfValues) {
        values = new double[numberOfValues];
        discountedValues = new double[numberOfValues];
    }


    /**
     * Computes statistics from values and dicountedValues
     *
     * @param monteCarloOptions - TODO sets which statistics are computed
     */
    public void calculateStatisticalProperties(MonteCarloOptions monteCarloOptions) {
//        sum =  StatUtils.sum(values);
        mean = StatUtils.mean(values);
//        Returns 0 for a single-value (i.e. length = 1) sample.
        sampleVariance = StatUtils.variance(values, mean);
        sampleSD = Math.sqrt(sampleVariance);
//        median = StatUtils.percentile(values,0.5);
//        percentile25 =StatUtils.percentile(values,0.25);
//        percentile75 =StatUtils.percentile(values,0.75);
//        max = StatUtils.max(values);
//        min = StatUtils.min(values);

//        discountedSum =  StatUtils.sum(discountedValues);
        discountedMean = StatUtils.mean(discountedValues);
//        Returns 0 for a single-value (i.e. length = 1) sample.
        discountedSampleVariance = StatUtils.variance(discountedValues, mean);
        discountedSampleSD = Math.sqrt(discountedSampleVariance);
//        discountedMedian = StatUtils.percentile(discountedValues,0.5);
//        discountedPercentile25 =StatUtils.percentile(discountedValues,0.25);
//        discountedPercentile75 =StatUtils.percentile(discountedValues,0.75);
//        discountedMax = StatUtils.max(discountedValues);
//        discountedMean = StatUtils.min(discountedValues);
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

    public double getSum() {
        return sum;
    }

    public double getSampleVariance() {
        return sampleVariance;
    }

    public double getSampleSD() {
        return sampleSD;
    }

    public double getMedian() {
        return median;
    }

    public double getPercentile25() {
        return percentile25;
    }

    public double getPercentile75() {
        return percentile75;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getDiscountedmin() {
        return discountedMin;
    }

    public double getDiscountedMean() {
        return discountedMean;
    }

    public double getDiscountedSum() {
        return discountedSum;
    }

    public double getDiscountedSampleVariance() {
        return discountedSampleVariance;
    }

    public double getDiscountedSampleSD() {
        return discountedSampleSD;
    }

    public double getDiscountedMedian() {
        return discountedMedian;
    }

    public double getDiscountedPercentile25() {
        return discountedPercentile25;
    }

    public double getDiscountedPercentile75() {
        return discountedPercentile75;
    }

    public double getDiscountedMax() {
        return discountedMax;
    }

    public double getDiscountedMin() {
        return discountedMin;
    }

    @Override
    public String toString() {
//        return "SimulationResults{"  +
        return "" +
                "Mean;" + mean +
//                ", sum=" + sum +
//                ", sampleVariance=" + sampleVariance +
//                ", sampleSD=" + sampleSD +
//                ", median=" + median +
//                ", percentile25=" + percentile25 +
//                ", percentile75=" + percentile75 +
//                ", max=" + max +
//                ", min=" + min + "\n" +
                ";DiscountedMean;" + discountedMean +
//                ", discountedSum=" + discountedSum +
//                ", discountedSampleVariance=" + discountedSampleVariance +
//                ", discountedSampleSD=" + discountedSampleSD +
//                ", discountedMedian=" + discountedMedian +
//                ", discountedPercentile25=" + discountedPercentile25 +
//                ", discountedPercentile75=" + discountedPercentile75 +
//                ", discountedMax=" + discountedMax +
//                ", discountedMin=" + discountedMin +
//                ", discountedMin=" + discountedMin +
//                '}';
                "";
    }
}
