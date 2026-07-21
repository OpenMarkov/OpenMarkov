/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;

/**
 * Capability interface for potentials that can be projected onto a partial evidence case to
 * produce a {@link TablePotential}.
 *
 * <p>Projection is the core operation of variable-elimination inference: given an evidence
 * assignment for some of the variables in the potential, it returns a new
 * {@code TablePotential} whose remaining variables are conditioned on that assignment.</p>
 *
 * <p>Not every {@link Potential} subclass supports projection (e.g. {@code FunctionPotential}
 * does not); only those that implement this interface are guaranteed to do so.
 * This eliminates the pattern of abstract methods that many subclasses satisfy with
 * {@code throw new UnsupportedOperationException()} or {@code return null}.</p>
 *
 * @author Manuel Arias
 * @see Reorderable
 * @see Scalable
 */
public interface Projectable {
    
    /**
     * Projects this potential onto the given evidence, returning a discrete table potential.
     *
     * @param evidence evidence case containing the observed variable–state assignments
     * @param options  inference options; may be {@code null} for default behaviour
     *
     * @return a {@link TablePotential} conditioned on the supplied evidence
     */
    TablePotential tableProject(EvidenceCase evidence, InferenceOptions options) throws NonProjectablePotentialException;
}
