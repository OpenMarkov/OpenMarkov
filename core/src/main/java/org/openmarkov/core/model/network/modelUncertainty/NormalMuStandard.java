/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

/**
 * Normal probability density function parameterised by mean ({@code mu}) and
 * standard deviation ({@code standard}). Internally delegates to
 * {@link NormalFunction}, whose second parameter — its field {@code sigma} —
 * is also the standard deviation: it samples {@code sigma * z + mu} and its
 * variance is {@code sigma * sigma}. This class used to square the standard
 * deviation on the way in, so the distribution it sampled had standard
 * deviation s² instead of the s the user asked for — internally consistent,
 * which is why nothing noticed.
 */
@ProbDensFunctionType(name = "NormalMuStandardFunction", univariateName = "Normal", isValidForProbabilities = false, isValidForNumeric = false, parameters = {
        "mu", "standard"}) public class NormalMuStandard extends NormalFunction {
    private double mu;
    private double standard;
    
    public NormalMuStandard() {
        super();
        setMu(0);
        setStandard(1);
    }
    
    public NormalMuStandard(double mu, double standard) {
        super(mu, standard);
        this.setMu(mu);
        this.setStandard(standard);
    }
    
    public NormalMuStandard(NormalMuStandard normalMuStandardFunction) {
        this(normalMuStandardFunction.getMu(), normalMuStandardFunction.getStandard());
    }
    
    //CMI
    //For Univariate
    
    /**
     * @param parameters - parameters[0] = mu and parameters[1] = standard deviation
     * @throws InvalidArgumentException - thrown if the standard deviation is not positive
     */
    @Override public void verifyParameters(double[] parameters) {
        // It used to check parameters[0] - the mean, which may be any real number -
        // with a message copied from the Beta family ("N must be greater than 0").
        if (!(parameters[1] > 0)) {
            throw new InvalidArgumentException(parameters[1], "standard",
                    "the standard deviation must be greater than 0");
        }
    }
    //CMF

    /**
     * Keeps the subclass fields and the base's in step. Without this override,
     * the base's setParameters changed the sigma while mu and standard here kept
     * their old values, so getParameters answered stale numbers and the object's
     * behaviour depended on whether it was built by constructor or by
     * setParameters.
     */
    @Override public void setParameters(double[] args) {
        super.setParameters(args);
        this.setMu(args[0]);
        this.setStandard(args[1]);
    }
    
    @Override public double[] getParameters() {
        double[] a = new double[2];
        a[0] = getMu();
        a[1] = getStandard();
        return a;
    }
    
    public double getMu() {
        return mu;
    }
    
    public void setMu(double mu) {
        this.mu = mu;
    }
    
    public double getStandard() {
        return standard;
    }
    
    public void setStandard(double standard) {
        this.standard = standard;
    }
    
    @Override public ProbDensFunction copy() {
        return new NormalMuStandard(this);
    }
    
}
