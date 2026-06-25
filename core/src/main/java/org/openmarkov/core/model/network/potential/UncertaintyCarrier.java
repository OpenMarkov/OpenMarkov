/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;

/**
 * Capability interface for potentials that carry parametric uncertainty distributions over
 * their numeric values.
 *
 * <p>The {@code uncertainValues} array stores one {@link UncertainValue} per entry of the
 * potential's numeric table.  It is used exclusively during probabilistic sensitivity
 * analysis (PSA), where each parameter is treated as a random variable with a known
 * distribution rather than a fixed point estimate.</p>
 *
 * <p>Only {@link UncertainTablePotential} (and its subclass {@code AugmentedProbTable})
 * implement this interface.  Plain {@link TablePotential} objects never carry uncertainty
 * distributions, so they do not implement this interface.  This eliminates the
 * {@code if (uncertainValues != null)} guards that previously appeared throughout the
 * operation classes.</p>
 *
 * @author Manuel Arias
 * @see StrategyCarrier
 */
public interface UncertaintyCarrier {

    /**
     * Returns the array of parametric uncertainty distributions, one per table entry.
     *
     * @return the uncertain-values array; must not be {@code null} for objects that
     *         implement this interface
     */
    UncertainValue[] getUncertainValues();

    /**
     * Replaces the array of parametric uncertainty distributions.
     *
     * @param values the new uncertain-values array
     */
    void setUncertainValues(UncertainValue[] values);

    /**
     * Returns {@code true} if this potential carries at least one parametric uncertainty
     * distribution.
     *
     * <p>The default implementation delegates to {@link #getUncertainValues()}: any
     * non-{@code null} return value is considered evidence of uncertainty.  Implementing
     * classes may override this method to provide a more specific check (e.g. verifying
     * that at least one entry is non-{@code null}).</p>
     *
     * @return {@code true} if this potential has parametric uncertainty distributions
     */
    default boolean isUncertain() {
        return getUncertainValues() != null;
    }
}
