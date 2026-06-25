package org.openmarkov.core.model.network.modelUncertainty;

/**
 * Normal distribution where a real number of a random stream is provided for sampling in order to cope with nuisance variance
 *
 * @author cmyago
 * @version 1.0 - 11/12/2022 copied from NormalFunction for testing nuisance variance in DESnets
 * FIXME merge with use NormalFunction; Problem: I need to control random number sequence in order to avoid nuisance variance
 */

public class NormalFunctionDES extends ProbDensFunctionWithKnownInverseCDF {
    private double mu;
    private double sigma;
    private StandardNormalFunction standard;

    public NormalFunctionDES() {
        this(0.0, 1.0);
    }

    public NormalFunctionDES(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
        standard = new StandardNormalFunction();
    }

    public NormalFunctionDES(NormalFunctionDES normalFunction) {
        super();
        this.mu = normalFunction.mu;
        this.sigma = normalFunction.sigma;
        if (normalFunction.standard != null) {
            this.standard = (StandardNormalFunction) normalFunction.standard.copy();
        }
    }

    /**
     * @param parameters - parameters[1]= mu and parameters[0] = sigma^2
     * @throws IllegalArgumentException - thrown if sigma<0
     */
    @Override
    public void verifyParameters(double[] parameters) {
        if (!(parameters[0] > 0)) {
            throw new IllegalArgumentException("Wrong parameters" + this.getClass().getName());
        }
    }

    //
    //For Univariate

    @Override
    public void verifyParametersDomain(boolean isChanceVariable) {
        if(!(sigma>0)){
            throw new IllegalArgumentException("Sigma should be in range 0..");
        }
    }
    //

    @Override
    public double[] getParameters() {
        double[] a = new double[2];
        a[0] = mu;
        a[1] = sigma;
        return a;
    }

    @Override
    public void setParameters(double[] args) {
        mu = args[0];
        sigma = args[1];
    }

    @Override
    public double getMaximum() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getMean() {
        // TODO Auto-generated method stub
        return mu;
    }


    private double translationFromStandardNormal(double x) {
        return sigma * x + mu;
    }

    @Override
    public double getVariance() {
        return Math.pow(sigma, 2.0);
    }

    @Override
    public double getMinimum() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public DomainInterval getInterval(double p) {
        DomainInterval standardInterval = standard.getInterval(p);
        return new DomainInterval(translationFromStandardNormal(standardInterval.min()),
                translationFromStandardNormal(standardInterval.max()));
    }

    @Override
    public double getInverseCumulativeDistributionFunction(double y) {
        return translationFromStandardNormal(standard.getInverseCumulativeDistributionFunction(y));
    }

    @Override
    public ProbDensFunction copy() {
        return new NormalFunctionDES(this);
    }
}
