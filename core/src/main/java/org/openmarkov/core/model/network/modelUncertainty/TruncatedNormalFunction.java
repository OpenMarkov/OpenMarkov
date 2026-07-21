package org.openmarkov.core.model.network.modelUncertainty;

/**
 * Normal distribution with R+ domain.
 * 04/05/2021
 *
 * @author cmyago
 */

public class TruncatedNormalFunction extends NormalFunctionDES {


    public TruncatedNormalFunction() {
        this(0.0, 1.0);
    }

    public TruncatedNormalFunction(double mu, double sigma) {
        super(mu, sigma);

    }

    public TruncatedNormalFunction(TruncatedNormalFunction normalFunction) {
        super(normalFunction);
    }


    @Override
    public ProbDensFunction copy() {
        return new TruncatedNormalFunction(this);
    }
}
