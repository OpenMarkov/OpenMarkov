/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the contract of {@link PotentialOperations#multiplyAndMaximize}: it maximizes
 * exactly one variable, and both degenerate calls must be refused with an explanation instead of
 * breaking with an index error or answering something wrong in silence.
 *
 * @author Manuel Arias
 */
class PotentialOperationsMaximizeContractTest {

    private static final Variable A = new Variable("A", 2);
    private static final Variable B = new Variable("B", 2);

    private static List<Potential> aPotentialOver(Variable... variables) {
        return List.of(new TablePotential(List.of(variables), PotentialRole.CONDITIONAL_PROBABILITY));
    }

    /** Keeping every variable leaves nothing to maximize; the old code broke with an index error. */
    @Test
    void keepingEveryVariableIsRefusedWithAnExplanation() {
        assertThrows(InvalidArgumentException.class,
                     () -> PotentialOperations.multiplyAndMaximize(aPotentialOver(A, B), List.of(A, B)));
    }

    /**
     * With two variables left to eliminate, the old code maximized only the first and answered a
     * wrong result in silence.
     */
    @Test
    void leavingMoreThanOneVariableToMaximizeIsRefused() {
        assertThrows(NotSupportedOperationException.class,
                     () -> PotentialOperations.multiplyAndMaximize(aPotentialOver(A, B), List.of()));
    }
}
