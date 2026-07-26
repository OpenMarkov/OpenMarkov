/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.UnrecoverableException;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
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
                                                    () -> Potential.findPotentialByVariable(wanted, projectedSoFar, new EvidenceCase(), null));

        assertTrue(thrown.getMessage().contains("Y"), "the message must name the variable: " + thrown.getMessage());
    }

    /** The message says both reasons, because from the inside they are indistinguishable. */
    @Test public void theMessageOffersBothExplanations() {
        UnrecoverableException thrown = assertThrows(UnrecoverableException.class,
                                                    () -> Potential.findPotentialByVariable(new Variable("Y", 2),
                                                                                            List.of(),
                                                                                            new EvidenceCase(), null));

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

    /**
     * The point of the whole arrangement: a potential that hands over factors instead of a table stays
     * usable by whoever needs the table. None of its factors is conditioned on its own variable - that
     * is what makes the factorization cheap - so the table is built on demand, by projecting the
     * potential the old way, and only for the caller that asked.
     */
    @Test public void aFactorizedPotentialIsCollapsedForWhoeverNeedsItsTable() throws Exception {
        ProbNet net = new ProbNet();
        Variable child = new Variable("Y", 2);
        net.addNode(child, NodeType.CHANCE);
        net.getNode(child).setPotential(new FactorizingPotential(List.of(child)));

        List<TablePotential> factors = net.tableProjectPotentials(new EvidenceCase());
        assertEquals(2, factors.size(), "it hands over two factors");
        for (TablePotential factor : factors) {
            assertNotEquals(child, factor.getConditionedVariable(),
                            "and none of them is conditioned on Y, which is why the table has to be built");
        }

        TablePotential table = Potential.findPotentialByVariable(child, factors, new EvidenceCase(),
                                                                new InferenceOptions(net, null));

        assertEquals(child, table.getConditionedVariable(), "the table built on demand is the one asked for");
        assertArrayEquals(new double[]{0.25, 0.75}, table.getValues(), 1e-12);
    }

    /** Stands in for a canonical model: factors over a pseudo variable, and a table when asked. */
    private static final class FactorizingPotential extends Potential {

        private static final Variable PSEUDO = new Variable("pseudo-Y", 2);

        private FactorizingPotential(List<Variable> variables) {
            super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        }

        @Override public List<TablePotential> tableProjectToFactors(EvidenceCase evidenceCase,
                                                                    InferenceOptions inferenceOptions,
                                                                    List<TablePotential> alreadyProjected) {
            return List.of(new TablePotential(List.of(PSEUDO), PotentialRole.CONDITIONAL_PROBABILITY),
                           new TablePotential(List.of(PSEUDO), PotentialRole.CONDITIONAL_PROBABILITY));
        }

        @Override public @NotNull TablePotential tableProject(EvidenceCase evidenceCase,
                                                              InferenceOptions inferenceOptions,
                                                              List<TablePotential> alreadyProjected) {
            TablePotential collapsed = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
            collapsed.getValues()[0] = 0.25;
            collapsed.getValues()[1] = 0.75;
            return collapsed;
        }

        @Override public Potential project(EvidenceCase evidenceCase) {
            throw new UnsupportedOperationException("not needed by this test");
        }

        @Override public Potential copy() {
            return new FactorizingPotential(variables);
        }
    }
}
