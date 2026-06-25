/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Variable;

/**
 * Capability interface for potentials that carry intervention strategy trees.
 *
 * <p>Strategy trees ({@link StrategyTree}) encode, for each configuration of a utility
 * potential, the optimal intervention policy produced during influence-diagram resolution.
 * They are attached to potentials only during the resolution phase and are absent from
 * all ordinary (non-strategic) potentials.</p>
 *
 * <p>Only {@link StrategicTablePotential} currently implements this interface.  The
 * interface encapsulates the {@code strategyTrees} field that was previously declared
 * directly in {@code TablePotential} (before Rediseño 1), eliminating the need for
 * callers to check {@code if (potential.strategyTrees != null)} or to cast to
 * {@code StrategicTablePotential} before accessing the trees.</p>
 *
 * @author Manuel Arias
 * @see UncertaintyCarrier
 */
public interface StrategyCarrier {

    /**
     * Returns the strategy-tree array, one entry per table position.
     *
     * @return the strategy trees; must not be {@code null} for objects that implement
     *         this interface
     */
    StrategyTree[] getStrategyTrees();

    /**
     * Returns {@code true} if this potential has at least one non-{@code null} strategy tree
     * that encodes an intervention policy.
     *
     * @return {@code true} if intervention policies are present
     */
    boolean hasInterventions();

    /**
     * Returns {@code true} if this potential contains an intervention policy for the
     * specified decision variable.
     *
     * @param decisionVariable the decision variable to check
     * @return {@code true} if an intervention policy for {@code decisionVariable} is present
     */
    boolean hasInterventionForDecision(Variable decisionVariable);
}
