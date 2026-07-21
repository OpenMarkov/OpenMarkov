/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

/**
 * Capability interface for potentials whose numeric values can be multiplied by a scalar factor.
 *
 * <p>Scaling is used in temporal discounting of utilities and in normalisation steps during
 * inference.  Only potentials that store explicit numeric values ({@link TablePotential} and
 * its subclasses, {@link DeltaPotential}, {@link LinearCombinationPotential}) support this
 * operation in a meaningful way.</p>
 *
 * <p>This interface introduces the canonical name {@link #scale(double)} for the operation.
 * Implementations that also override the legacy {@link Potential#scalePotential(double)}
 * should delegate one to the other to keep behaviour consistent during the migration period.</p>
 *
 * @author Manuel Arias
 * @see Projectable
 * @see Reorderable
 */
public interface Scalable {

    /**
     * Multiplies every numeric value in this potential by {@code factor}, modifying
     * the potential in place.
     *
     * @param factor the scalar multiplier; may be any finite {@code double} value
     */
    void scale(double factor);
}
