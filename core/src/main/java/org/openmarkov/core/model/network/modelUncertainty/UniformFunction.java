package org.openmarkov.core.model.network.modelUncertainty;

/**
 * This class represents a Uniform(min, max) distribution
 * 16/01/2023 FIXME Use UniformDistribution from Apache Commons; currently it is not used for homogeneity
 * @author cmyago
 * @version 1.0 - 19/01/2025 Adapted from WeibullFunction
 */
public class UniformFunction extends ProbDensFunctionWithKnownInverseCDF {
    /**
     * Minimum value
     */
    private double min;
    
    /**
     * Maximum value
     */
    private double max;


    /**
     * Creates a Uniform function with minumum=0 and maximum=1
     */
    public UniformFunction() {
        this(0.0, 1);
    }

    /**
     * Creates a UniformFunction object with domain [min,max]
     *
     * @param min  
     * @param max 
     */
    public UniformFunction(double min, double max) {
        this.min = min;
        this.max = max;

    }

    /**
     * Creates a UniformFunction object equal to uniformFunction
     *
     * @param uniformFunction 
     */
    public UniformFunction(UniformFunction uniformFunction) {
        super();
        this.min = uniformFunction.min;
        this.max = uniformFunction.max;
    }

    /**
     * Returns the min
     *
     * @return the min parameter
     */
    public double getMin() {
        return min;
    }

    /**
     * Sets the min parameter
     *
     * @param min the min to be set
     */
    public void setMin(double min) {
        this.min = min;
    }


    /**
     * Returns the max parameter
     */
    public double getMax() {
        return max;
    }


    /**
     * Sets the max parameter
     */
    public void setMax(double max) {
        this.max = max;
    }


    /**
     * Returns an array of double where array[1] is max and array[0] is min
     *
     * @return an array of double where array[1] is max and array[0] is min
     */
    @Override
    public double[] getParameters() {
        double[] a = new double[2];
        a[0] = min;
        a[1] = max;
        return a;
    }

    /**
     * Sets params[1] as max and  params[0] as min
     *
     * @param params array of double where params[1]= lambda params[0]=k
     */
    @Override
    public void setParameters(double[] params) {
        min = params[0];
        max = params[1];
    }


    /**
     * @param isChanceVariable - not used, kept form compatibility
     * @return true
     */
    @Override
    public void verifyParametersDomain(boolean isChanceVariable) {
    }


    /**
     * @param parameters - parameters[1]= max and parameters[0] = min
     * @throws IllegalArgumentException - thrown if min>max
     */
    @Override
    public void verifyParameters(double[] parameters) {
        if (parameters[0] > parameters[1]) {
            throw new IllegalArgumentException("Wrong parameters" + this.getClass().getName());
        }
    }


    /**
     * Returns the mean of this UniformFunction distribution. The mean of a Uniform distribution is lambda*Gamma_Funcion(1+1/k)
     *
     * @return the mean of this UniformFunction distribution.
     */
    @Override
    public double getMean() {

        return (min + max)/2 ;
    }


    /**
     * @return the
     * Returns the mean of this UniformFunction distribution. The mean of a Uniform distribution is lambda*Gamma_Funcion(1+1/k)
     * * @return the mean of this UniformFunction distribution.
     */
    @Override
    public double getVariance() {
        return Math.pow(max -min,2)/12;
    }

    /**
     * Returns the minimum of the support of the Uniform distribudion. The Uniform distribucion support is [0, +inf)
     *
     * @return 0
     */
    @Override
    public double getMinimum() {
        return min;
    }


    /**
     * Returns the maximum of the support of the Uniform distribudion. The Uniform distribucion support is [0, +inf)
     *
     * @return Double.POSITIVE_INFINITY;
     */
    @Override
    public double getMaximum() {
        return max;
    }


    /**
     * Returns Q(y,min,max) where Q is the Inverse Cumulative Distribution for a Uniform distribution
     * Q(y,min,max) = min + y* (max -min)
     * @param y 0<=y<=1
     * @return Q(y, lambda, k) where Q is the Inverse Cumulative Distribution for a Uniform distribution
     */
    @Override
    public double getInverseCumulativeDistributionFunction(double y) {
        return min + y * (max - min);
    }


    /**
     * Returns a copy of this object
     *
     * @return a copy of this object
     */
    @Override
    public ProbDensFunction copy() {
        return new UniformFunction(this);
    }


}
