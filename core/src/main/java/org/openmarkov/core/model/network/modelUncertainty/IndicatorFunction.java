package org.openmarkov.core.model.network.modelUncertainty;

import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * This class represents an indicator distribution multiplied by a constant
 * X~ c*I[E]; When E happens with probability p X=c
 * 18/03/2023 FIXME; this distribution is only for events; should be kept?
 * @author cmyago
 * @version 1.0 - 19/03/2020 Adapted from WeibullFunction
 */
public class IndicatorFunction extends ProbDensFunctionWithKnownInverseCDF {
    /**
     * Scale
     */
    private double probability;


    /**
     * Shape
     */
    private double tte;



    /**
     * Creates a WeibullFuncion object with scale lambda=0 and shape k=0
     */
    public IndicatorFunction() {
        this(1, 0);
    }

    /**
     * Creates a IndicatorFunction object
     *
     * @param probability - probability of occurrence
     * @param tte      - time-to-event
     */
    public IndicatorFunction(double probability, double tte) {
        this.probability = probability;
        this.tte = tte;

    }

    /**
     * Creates a WeibullFuncion object equal to weibullFuncion
     *
     * @param indicatorFunction Weibull Funcion use to create an equal WeibullFunction object
     */
    public IndicatorFunction(IndicatorFunction indicatorFunction) {
        super();
        this.probability = indicatorFunction.probability;
        this.tte = indicatorFunction.tte;
    }

    /**
     *
     * @return
     */
    public double getProbability() {
        return probability;
    }

    /**
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }


    /**

     */
    public double getTte() {
        return tte;
    }


    /**
     *
     */
    public void setTte(double tte) {
        this.tte = tte;
    }


    /**
     *
     */
    @Override
    public double[] getParameters() {
        double[] params = new double[2];
        params[0] = probability;
        params[1] = tte;
        return params;
    }

    /**
     *
     */
    @Override
    public void setParameters(double[] params) {
        probability = params[0];
        tte = params[1];
    }


    /**
     *
     */
    @Override
    public void verifyParametersDomain(boolean isChanceVariable) {
        if(!((probability<=1) && (probability >= 0) && (tte >= 0))){
            throw new IllegalArgumentException("Probability out of range 0=..=1 or tte out of range 0=..");
        }
    }

    /**
     * @throws IllegalArgumentException - thrown if
     */
    @Override
    public void verifyParameters(double[] parameters) {
        if (!(probability<=1) && (probability >= 0) && (tte >= 0)) {
            throw new IllegalArgumentException("Wrong parameters" + this.getClass().getName());
        }
    }


    /**
     * FIXME
     */
    @Override
    public double getMean() {
      return -1;
    }


    /**
     * FIXME
     */
    @Override
    public double getVariance() {
        return -1;
    }

    /**
     *
     * @return 0
     */
    @Override
    public double getMinimum() {
        return 0;
    }


    /**
     * Returns the maximun of the support of the Weibull distribudion. The Weibull distribucion support is [0, +inf)
     *
     * @return Double.POSITIVE_INFINITY;
     */
    @Override
    public double getMaximum() {
        return Double.POSITIVE_INFINITY;
    }


    /**
     * Returns Q(y, lambda, k) where Q is the Inverse Cumulative Distribution for a Weibull distribution
     *
     * @param y 0<=y<=1
     * @return Q(y, lambda, k) where Q is the Inverse Cumulative Distribution for a Weibull distribution
     */
    @Override
    public double getInverseCumulativeDistributionFunction(double y) {
        if (y < 0.0 || y > 1.0) {
            throw new OutOfRangeException(y, 0.0, 1.0);
        } else if (y<=probability) return tte;
        return Double.POSITIVE_INFINITY;

    }


    /**
     * Returns a copy of this object
     *
     * @return a copy of this object
     */
    @Override
    public ProbDensFunction copy() {
        return new IndicatorFunction(this);
    }


}
