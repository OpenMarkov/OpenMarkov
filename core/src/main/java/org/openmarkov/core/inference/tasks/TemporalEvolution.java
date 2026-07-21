/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference.tasks;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;

/**
 * @author jperez-martin
 * @author artasom
 * @version 2 cmyago 03/11/2022; added methods getTemporalEvolutionWithDiscount, getTemporalEvolutionWithDiscount(Criterion) and getAtemporalUtility()
 */
public interface TemporalEvolution extends Task {

    HashMap<Variable, TablePotential> getTemporalEvolution()
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException;


    /**
     * Returns the temporal evolution of a node or a set of utility nodes where discounting is applied.
     * If there is no discounting non-discounted temporal evolution is returned
     * To be used with one variable or for CE
     *
     * @return temporal evolution of a node or a set of utility nodes where discounting is applied
     * @throws IncompatibleEvidenceException if the evidence is incompatible with the network
     * @throws NotEvaluableNetworkException if the network cannot be evaluated
     */
    default HashMap<Variable, TablePotential> getTemporalEvolutionWithDiscount()
            throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        return getTemporalEvolution();
    }

    /**
     * Returns the temporal evolution of a node or a set of utility nodes where discounting given by {@code criterion} is applied.
     * To be used with one variable or for CE
     *
     * @param criterion @link{Criterion} used to compute discount
     * @return temporal evolution of a node or a set of utility nodes where discounting is applied
     */
    default HashMap<Variable, TablePotential> getTemporalEvolutionWithDiscount(Criterion criterion) {
        return null;
    }

    /**
     * Returns timeless results  after evaluating the network. Discounting is not applied here.
     *
     * @return @link{TablePotential} with timeless results
     */
    default TablePotential getAtemporalUtility() {
        return null;
    }


    void setDecisionVariable(Variable decisionSelected);

    ProbNet getExpandedNetwork();
}