/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Projecting a network's potentials for inference can now hand back more than one factor per
 * potential.
 * <p>
 * It could not before: the preprocessing added exactly one table per potential, so a potential that
 * factorizes had nowhere to put its factors and had to multiply them back into one table. That is
 * what makes a noisy-MAX over fourteen parents cost 32768 numbers where its factors hold 62. This is
 * the door, not the walk through it: every potential in the model still contributes exactly one
 * factor, which is the default.
 *
 * @author Manuel Arias
 */
public class ProjectionCanReturnSeveralFactorsTest {

    /** A potential that hands over two factors instead of one, standing in for a canonical model. */
    private static final class TwoFactorPotential extends Potential {

        private TwoFactorPotential(List<Variable> variables) {
            super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        }

        @Override public List<TablePotential> tableProjectToFactors(EvidenceCase evidenceCase,
                                                                   InferenceOptions inferenceOptions,
                                                                   List<TablePotential> alreadyProjected) {
            return List.of(new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY),
                           new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY));
        }

        @Override public Potential project(EvidenceCase evidenceCase) {
            throw new UnsupportedOperationException("this test never projects to a single potential");
        }

        @Override public Potential copy() {
            return new TwoFactorPotential(variables);
        }
    }

    @Test public void aPotentialThatReturnsTwoFactorsPutsBothIntoTheProjection()
            throws NonProjectablePotentialException {
        ProbNet net = new ProbNet();
        Variable variable = new Variable("Y", 2);
        net.addNode(variable, NodeType.CHANCE);
        net.getNode(variable).setPotential(new TwoFactorPotential(List.of(variable)));

        List<TablePotential> projected = net.tableProjectPotentials(new EvidenceCase());

        assertEquals(2, projected.size(), "both factors must reach the projection, not just the first");
    }

    /** And the default is still one factor, so nothing else in the model changes. */
    @Test public void anOrdinaryPotentialStillContributesExactlyOneFactor()
            throws NonProjectablePotentialException {
        ProbNet net = new ProbNet();
        Variable variable = new Variable("Y", 2);
        net.addNode(variable, NodeType.CHANCE);
        net.getNode(variable).setPotential(new UniformPotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY));

        List<TablePotential> projected = net.tableProjectPotentials(new EvidenceCase());

        assertEquals(1, projected.size());
    }

    /** The default really is the table tableProject returns, not a copy of it. */
    @Test public void theDefaultFactorIsTheTableProjectionItself() throws NonProjectablePotentialException {
        Variable variable = new Variable("Y", 2);
        UniformPotential potential = new UniformPotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY);

        List<TablePotential> factors = potential.tableProjectToFactors(new EvidenceCase(), null, List.of());

        assertEquals(1, factors.size());
        assertArrayEquals(potential.tableProject(new EvidenceCase(), null).getValues(),
                          factors.getFirst().getValues(), 1e-12);
    }
}
