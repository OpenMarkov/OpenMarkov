/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Read-only view of a probabilistic network's graph structure and potentials.
 *
 * <p>Extends {@link GraphNetwork} with potential-access methods. Inference
 * algorithms that need both the graph topology and the probability/utility
 * tables should accept a {@code PotentialNetwork} rather than the full
 * {@link ProbNet}.
 *
 * @see GraphNetwork
 * @see ProbNet
 */
public interface PotentialNetwork extends GraphNetwork {

    // ---------------------------------------------------------------
    // Potential access
    // ---------------------------------------------------------------

    List<Potential> getPotentials();
    List<Potential> getPotentials(Predicate<Potential> predicate);
    List<Potential> getPotentials(Variable variable);
    List<Potential> getPotentialsByRole(PotentialRole role);
    List<Potential> getPotentialsByType(NodeType nodeType);
    List<Potential> getAdditivePotentials();
    List<Potential> getSortedPotentials();
    Set<Potential>  getConstantPotentials();
    int             getNumPotentials();

    List<Potential> getProbPotentials(Variable variable);
    List<Potential> getUtilityPotentials(Variable variable);
}
