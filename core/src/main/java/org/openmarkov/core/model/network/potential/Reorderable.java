/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Capability interface for potentials whose variable ordering can be permuted.
 *
 * <p>Reordering is needed in variable elimination and cost-effectiveness analysis to
 * align the variable indices of two potentials before multiplying or marginalising them.
 * Only potentials backed by an indexed table ({@link TablePotential} and its subclasses,
 * plus {@link GTablePotential}) support this operation in a well-defined way.</p>
 *
 * <p>Declaring this interface instead of forcing an abstract method on every
 * {@link Potential} subclass eliminates the pattern of {@code return null} seen in
 * {@code UniformPotential}, {@code DeltaPotential} and others.</p>
 *
 * @author Manuel Arias
 * @see Projectable
 * @see Scalable
 */
public interface Reorderable {

    /**
     * Returns a copy of this potential with its variables reordered according to
     * {@code newOrderOfVariables}.
     *
     * <p>The returned potential represents the same distribution as the original,
     * but with a different internal variable ordering.  The caller is responsible
     * for ensuring that {@code newOrderOfVariables} contains exactly the same
     * variables as the original (possibly in a different order).</p>
     *
     * @param newOrderOfVariables the desired variable ordering
     * @return a new potential with the variables in the requested order
     */
    Potential reorder(List<Variable> newOrderOfVariables);
}
