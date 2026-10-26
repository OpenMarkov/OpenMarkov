/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A sum of a numeric node X absorbs X when the distribution of X gives a table of values, as an "Exact" one does, and
 * is not offered to absorb it when it does not, as a normal one (error 9).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AbsorbParentsNeedsParentsThatAreTablesTest {

    private final ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
    private final Variable a = new Variable("A", "a0", "a1");
    private final Variable x = new Variable("X");
    private final Variable s = new Variable("S");

    /** A, X depending on A with a distribution of the class {@code function}, and S, the sum of X. */
    private Node sumOfX(Class<? extends ProbDensFunction> function) {
        Node nodeA = net.addNode(a, NodeType.CHANCE);
        Node nodeX = net.addNode(x, NodeType.CHANCE);
        Node nodeS = net.addNode(s, NodeType.UTILITY);
        net.addLink(nodeA, nodeX, true);
        net.addLink(nodeX, nodeS, true);
        TablePotential potentialA = new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY);
        potentialA.setValues(new double[] { 0.4, 0.6 });
        nodeA.setPotential(potentialA);
        UnivariateDistrPotential potentialX = new UnivariateDistrPotential(List.of(x, a), function,
                                                                           PotentialRole.CONDITIONAL_PROBABILITY);
        if (function == ExactFunction.class) {
            potentialX.getDistributionTable().setValues(new double[] { 10, 3 });
        }
        nodeX.setPotential(potentialX);
        nodeS.setPotential(new SumPotential(List.of(s, x), PotentialRole.UNSPECIFIED));
        return nodeS;
    }

    @Test
    void anExactParentIsAbsorbed() {
        assertTrue(BasicOperations.haveParentsAndAreAllAbsorbable(sumOfX(ExactFunction.class)));

        assertEquals(2, BasicOperations.absorbAllIntermediateNumericNodes(net, new EvidenceCase()).getNodes().size());
    }

    @Test
    void aNormalParentIsNotOfferedNorAbsorbed() {
        assertFalse(BasicOperations.haveParentsAndAreAllAbsorbable(sumOfX(NormalFunction.class)));

        assertEquals(3, BasicOperations.absorbAllIntermediateNumericNodes(net, new EvidenceCase()).getNodes().size());
    }
}
