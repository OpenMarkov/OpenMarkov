/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Asking for the projection of a variable nobody has projected says so, instead of handing back a
 * null that travels.
 * <p>
 * It used to return null, and every one of the three callers walks straight on to use the answer. Two
 * put it into a sum, and a sum of one element returns that element - so a super-value node with a
 * single parent whose projection was missing produced <em>null</em> with no complaint at all, from a
 * method declared to return a table. With more than one parent the sum fails instead, with a
 * NullPointerException several frames from the cause. The third caller dereferences it at once.
 * <p>
 * There are two ways to get here, and the message names both: the potentials are being visited out of
 * topological order, or the variable's potential contributed several factors rather than a single
 * table. The second is what a canonical model does when it hands over its factorization instead of
 * multiplying it back together, which is why this had to be made explicit before that becomes
 * possible.
 *
 * @author Manuel Arias
 */
public class MissingProjectionIsReportedTest {

    @Test public void askingForAVariableNobodyProjectedNamesIt() {
        Variable wanted = new Variable("Y", 2);
        List<TablePotential> projectedSoFar = List.of(
                new TablePotential(List.of(new Variable("Z", 2)), PotentialRole.CONDITIONAL_PROBABILITY));

        UnrecoverableException thrown = assertThrows(UnrecoverableException.class,
                                                    () -> Potential.findPotentialByVariable(wanted, projectedSoFar));

        assertTrue(thrown.getMessage().contains("Y"), "the message must name the variable: " + thrown.getMessage());
    }

    /** The message says both reasons, because from the inside they are indistinguishable. */
    @Test public void theMessageOffersBothExplanations() {
        UnrecoverableException thrown = assertThrows(UnrecoverableException.class,
                                                    () -> Potential.findPotentialByVariable(new Variable("Y", 2),
                                                                                            List.of()));

        assertTrue(thrown.getMessage().contains("not been projected yet"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("several factors"), thrown.getMessage());
    }

    /**
     * The silent one, which is the reason this was worth changing on its own: a super-value node with
     * exactly one parent used to come out of its projection as null, from a method declared not to
     * return null.
     */
    @Test public void aSumOverOneMissingParentNoLongerProjectsToNull() {
        Variable utility = new Variable("U", 2);
        Variable parent = new Variable("U1", 2);
        SumPotential sum = new SumPotential(List.of(utility, parent), PotentialRole.UNSPECIFIED);

        assertThrows(UnrecoverableException.class,
                     () -> sum.tableProject(new EvidenceCase(), null, List.of()),
                     "with nothing projected, the parent cannot be found and that must be said");
    }
}
