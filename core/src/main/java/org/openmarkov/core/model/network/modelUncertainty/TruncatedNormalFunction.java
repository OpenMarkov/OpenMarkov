package org.openmarkov.core.model.network.modelUncertainty;

import org.apache.commons.math3.special.Erf;
import org.openmarkov.core.exception.InvalidArgumentException;

/**
 * Normal distribution truncated to the interval [lowerBound, upperBound], which defaults to
 * R+ ([0, +infinity)).
 * <p>
 * This is a genuine truncation: the density of the underlying normal is restricted to the
 * interval and renormalised by the probability mass it retains. It is NOT censoring, so there
 * is no point mass at the bounds, and consequently {@link #getMean()} is not mu and
 * {@link #getVariance()} is not sigma^2.
 * <p>
 * Writing alpha = (lowerBound - mu) / sigma, beta = (upperBound - mu) / sigma and
 * Z = Phi(beta) - Phi(alpha):
 * <ul>
 *   <li>inverse CDF: mu + sigma * InvPhi(Phi(alpha) + p * Z)</li>
 *   <li>mean: mu + sigma * (phi(alpha) - phi(beta)) / Z</li>
 *   <li>variance: sigma^2 * [1 + (alpha*phi(alpha) - beta*phi(beta)) / Z
 *       - ((phi(alpha) - phi(beta)) / Z)^2]</li>
 * </ul>
 * Sampling is inherited from {@link ProbDensFunctionWithKnownInverseCDF} and is therefore
 * inverse-CDF based, so it never rejects samples and always lands inside the interval.
 * <p>
 * 04/05/2021
 *
 * @author cmyago
 * @author Manuel Arias
 */

public class TruncatedNormalFunction extends NormalFunctionDES {

    /**
     * Standard normal density, needed for the truncated moments.
     */
    private static final double INV_SQRT_2PI = 1.0 / Math.sqrt(2.0 * Math.PI);

    /**
     * Guards the argument of the inverse CDF away from 0 and 1, where it diverges. Only
     * reachable when a bound is infinite.
     */
    private static final double PROBABILITY_MARGIN = 1.0e-15;

    private double lowerBound;
    private double upperBound;

    private final StandardNormalFunction standard = new StandardNormalFunction();

    public TruncatedNormalFunction() {
        this(0.0, 1.0);
    }

    /**
     * Creates a normal truncated to R+, that is, to [0, +infinity).
     *
     * @param mu    mean of the underlying (untruncated) normal
     * @param sigma standard deviation of the underlying (untruncated) normal
     */
    public TruncatedNormalFunction(double mu, double sigma) {
        this(mu, sigma, 0.0, Double.POSITIVE_INFINITY);
    }

    /**
     * @param mu         mean of the underlying (untruncated) normal
     * @param sigma      standard deviation of the underlying (untruncated) normal
     * @param lowerBound lower truncation bound, may be {@link Double#NEGATIVE_INFINITY}
     * @param upperBound upper truncation bound, may be {@link Double#POSITIVE_INFINITY}
     */
    public TruncatedNormalFunction(double mu, double sigma, double lowerBound, double upperBound) {
        super(mu, sigma);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public TruncatedNormalFunction(TruncatedNormalFunction normalFunction) {
        super(normalFunction);
        this.lowerBound = normalFunction.lowerBound;
        this.upperBound = normalFunction.upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public void verifyParametersDomain(boolean isChanceVariable) {
        super.verifyParametersDomain(isChanceVariable);
        if (!(lowerBound < upperBound)) {
            throw new InvalidArgumentException("The lower bound must be smaller than the upper bound");
        }
    }

    @Override
    public double getMinimum() {
        return lowerBound;
    }

    @Override
    public double getMaximum() {
        return upperBound;
    }

    @Override
    public double getInverseCumulativeDistributionFunction(double y) {
        double sigma = getSigma();
        double probabilityAtLowerBound = standardCdf(alpha());
        double retainedMass = standardCdf(beta()) - probabilityAtLowerBound;
        double probability = probabilityAtLowerBound + y * retainedMass;
        // Only bites when a bound is infinite, where the inverse CDF would diverge
        probability = Math.min(Math.max(probability, PROBABILITY_MARGIN), 1.0 - PROBABILITY_MARGIN);
        double value = getMu() + sigma * standard.getInverseCumulativeDistributionFunction(probability);
        // The boundary probability (erfc-based CDF) and the inverse CDF (Odeh-Evans rational
        // approximation) are not exact numerical inverses, so a sample drawn at the very edge can
        // land a few ulps outside the interval. Clamp to keep the documented invariant that a
        // sample always lands inside [lowerBound, upperBound].
        return Math.min(Math.max(value, lowerBound), upperBound);
    }

    /**
     * Restores the generic inverse-CDF interval of
     * {@link ProbDensFunctionWithKnownInverseCDF}. The override inherited from
     * {@link NormalFunctionDES} builds the interval from the untruncated standard normal, which
     * is symmetric around mu and therefore wrong once the distribution is truncated.
     */
    @Override
    public DomainInterval getInterval(double p) {
        double halfP = p / 2.0;
        return new DomainInterval(getInverseCumulativeDistributionFunction(0.5 - halfP),
                getInverseCumulativeDistributionFunction(0.5 + halfP));
    }

    @Override
    public double getMean() {
        double alpha = alpha();
        double beta = beta();
        double retainedMass = standardCdf(beta) - standardCdf(alpha);
        return getMu() + getSigma() * (standardPdf(alpha) - standardPdf(beta)) / retainedMass;
    }

    @Override
    public double getVariance() {
        double alpha = alpha();
        double beta = beta();
        double sigma = getSigma();
        double retainedMass = standardCdf(beta) - standardCdf(alpha);
        double densityDifference = (standardPdf(alpha) - standardPdf(beta)) / retainedMass;
        double weightedDifference = (timesStandardPdf(alpha) - timesStandardPdf(beta)) / retainedMass;
        return sigma * sigma * (1.0 + weightedDifference - densityDifference * densityDifference);
    }

    @Override
    public ProbDensFunction copy() {
        return new TruncatedNormalFunction(this);
    }

    private double getMu() {
        return getParameters()[0];
    }

    private double getSigma() {
        return getParameters()[1];
    }

    /**
     * Standardised lower bound.
     */
    private double alpha() {
        return (lowerBound - getMu()) / getSigma();
    }

    /**
     * Standardised upper bound.
     */
    private double beta() {
        return (upperBound - getMu()) / getSigma();
    }

    /**
     * Standard normal CDF. Phi(-inf) = 0 and Phi(+inf) = 1 fall out of erfc naturally.
     */
    private static double standardCdf(double x) {
        return 0.5 * Erf.erfc(-x / Math.sqrt(2.0));
    }

    /**
     * Standard normal density. Zero at infinite bounds.
     */
    private static double standardPdf(double x) {
        if (Double.isInfinite(x)) {
            return 0.0;
        }
        return INV_SQRT_2PI * Math.exp(-0.5 * x * x);
    }

    /**
     * x * phi(x), which tends to 0 as |x| tends to infinity. Computed separately because the
     * naive product yields infinity * 0 = NaN at an infinite bound.
     */
    private static double timesStandardPdf(double x) {
        if (Double.isInfinite(x)) {
            return 0.0;
        }
        return x * standardPdf(x);
    }
}
