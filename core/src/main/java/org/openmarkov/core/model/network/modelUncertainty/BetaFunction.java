/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.List;
import java.util.Random;

/**
 * Beta probability density function on {@code [0, 1]} parameterised by the
 * shape parameters {@code alpha} and {@code beta}. Sampling is delegated to a
 * two-component {@link DirichletFamily}.
 */
@ProbDensFunctionType(name = "Beta", parameters = {"alpha", "beta"}) public class BetaFunction
        extends ProbDensFunction {
    private double alpha;
    private double beta;
    
    private DirichletFamily dirichletForSampling;
    
    public BetaFunction() {
        this.alpha = 0;
        this.beta = 0;
        initializePdfForSampling();
    }
    
    public BetaFunction(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
        initializePdfForSampling();
    }
    
    public BetaFunction(BetaFunction betaFunction) {
        super();
        this.alpha = betaFunction.alpha;
        this.beta = betaFunction.beta;
        if (betaFunction.dirichletForSampling != null) {
            this.dirichletForSampling = betaFunction.dirichletForSampling;
        }
    }
    
    //CMI
    //For Univariate
    
    /**
     * @param parameters - parameters[1]= alpha and parameters[0] = beta
     *
     * @throws IllegalArgumentException - thrown if the alpha or beta &#60;0
     */
    @Override public void verifyParameters(double[] parameters) {
        if (!((parameters[0] > 0) && (parameters[1] > 0))) {
            throw new InvalidArgumentException(List.of(parameters[0], parameters[1]), "N", "both should be greater than 0");
        }
    }
    //CMF
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (alpha <= 0) {
            throw new InvalidArgumentException(alpha, "alpha", "should be a number bigger than 0");
        }
        if (beta <= 0) {
            throw new InvalidArgumentException(beta, "beta", "should be a number bigger than 0");
        }
    }
    
    @Override public double[] getParameters() {
        double[] a = new double[2];
        a[0] = alpha;
        a[1] = beta;
        return a;
    }
    
    @Override public void setParameters(double[] params) {
        alpha = params[0];
        beta = params[1];
        initializePdfForSampling();
    }
    
    @Override public double getMaximum() {
        return 1;
    }
    
    @Override public double getMean() {
        return alpha / (alpha + beta);
    }
    
    @Override public double getSample(Random randomGenerator) {
        return dirichletForSampling.getSample(randomGenerator)[0];
    }
    
    @Override public double getVariance() {
        double sumAlphaBeta = alpha + beta;
        return (alpha * beta) / (Math.pow(sumAlphaBeta, 2.0) * (sumAlphaBeta + 1));
    }
    
    @Override public double getMinimum() {
        return 0;
    }
    
    @Override public DomainInterval getInterval(double p) {
        BetaDistribution auxBeta = new BetaDistribution(alpha, beta);
        double l;
        double u;
        double halfP = p / 2.0;
        if (getVariance() > 0.0) {
            l = auxBeta.inverseCumulativeProbability(0.5 - halfP);
            u = auxBeta.inverseCumulativeProbability(0.5 + halfP);
        } else {
            double mean = getMean();
            l = mean;
            u = mean;
        }
        return new DomainInterval(l, u);
    }
    
    private void initializePdfForSampling() {
        
        dirichletForSampling = new DirichletFamily(getParameters());
    }
    
    @Override public ProbDensFunction copy() {
        return new BetaFunction(this);
    }
    
    public double getAlpha() {
        return alpha;
    }
    
    public double getBeta() {
        return beta;
    }
}
