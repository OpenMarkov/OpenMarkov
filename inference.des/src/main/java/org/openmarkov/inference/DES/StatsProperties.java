package org.openmarkov.inference.DES;

import org.apache.commons.math3.stat.StatUtils;

/**
 * Statistical properties of one array of data (Simulation Results)
 *
 * @author cmyago
 * @version 1 06/11/2021 - sum, mean, sample variance, sample standard deviation,
 */
public class StatsProperties {


    private double mean = Double.NaN;
    private double sum = Double.NaN;
    private double sampleVariance = Double.NaN;
    private double sampleSD = Double.NaN;

    private double median = Double.NaN;
    private double percentile25 = Double.NaN;
    private double percentile75 = Double.NaN;
    private double max = Double.NaN;

    private double min = Double.NaN;

    /**
     * Creates a new StatsProperties object for values
     *
     * @param values array of double containing the values which statistics are computed
     */
    public StatsProperties(double[] values) {
        sum = StatUtils.sum(values);
        mean = StatUtils.mean(values);
//        Returns 0 for a single-value (i.e. length = 1) sample.
        sampleVariance = StatUtils.variance(values, mean);
        sampleSD = Math.sqrt(sampleVariance);
        median = StatUtils.percentile(values, 0.5);
        percentile25 = StatUtils.percentile(values, 0.25);
        percentile75 = StatUtils.percentile(values, 0.75);
        max = StatUtils.max(values);
        min = StatUtils.min(values);
    }


    public double getSum() {
        return sum;
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

    @Override
    public String toString() {
        return "StatsProperties{" +
                ", mean=" + mean +
                "sum= " + sum +
                ", sampleVariance=" + sampleVariance +
                ", sampleSD=" + sampleSD +
                ", median=" + median +
                ", percentile25=" + percentile25 +
                ", percentile75=" + percentile75 +
                ", max=" + max +
                ", min=" + min +
                '}';
    }
}
