/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Absorbing the intermediate numeric nodes replaces a super-value structure — utilities of
 * utilities — with a single utility node whose table is computed from those of the absorbed
 * parents. These are the first direct tests of that operation (P7 of the inference report);
 * the expected values are computed by hand from the definition, not from the code.
 *
 * <p>The fixture: a chance variable A with two states, two numeric nodes U1 and U2 that
 * depend on A, and a super-value node S combining U1 and U2.
 *
 * <pre>
 *   A = a0:  U1 = 10,  U2 = 5     A = a1:  U1 = 3,  U2 = 7
 * </pre>
 *
 * @author Manuel Arias
 */
class AbsorbingNumericNodesTest {

	private final ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
	private final Variable a = new Variable("A", "a0", "a1");
	private final Variable u1 = new Variable("U1");
	private final Variable u2 = new Variable("U2");
	private final Variable sv = new Variable("S");

	private void buildFixture(Potential superValuePotential) {
		Node nodeA = net.addNode(a, NodeType.CHANCE);
		Node nodeU1 = net.addNode(u1, NodeType.UTILITY);
		Node nodeU2 = net.addNode(u2, NodeType.UTILITY);
		Node nodeS = net.addNode(sv, NodeType.UTILITY);
		net.addLink(nodeA, nodeU1, true);
		net.addLink(nodeA, nodeU2, true);
		net.addLink(nodeU1, nodeS, true);
		net.addLink(nodeU2, nodeS, true);

		TablePotential potA = new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY);
		potA.setValues(new double[] { 0.4, 0.6 });
		nodeA.setPotential(potA);
		ExactDistrPotential potU1 = new ExactDistrPotential(Arrays.asList(u1, a));
		potU1.getTablePotential().setValues(new double[] { 10, 3 });
		nodeU1.setPotential(potU1);
		ExactDistrPotential potU2 = new ExactDistrPotential(Arrays.asList(u2, a));
		potU2.getTablePotential().setValues(new double[] { 5, 7 });
		nodeU2.setPotential(potU2);
		nodeS.setPotential(superValuePotential);
	}

	private double[] valuesOfSAfterAbsorbing() {
		ProbNet result = BasicOperations.absorbAllIntermediateNumericNodes(net, new EvidenceCase());

		assertEquals(2, result.getNodes().size(), "Only A and S should remain after absorbing U1 and U2");
		Node absorbedS = result.getNode("S");
		assertNotNull(absorbedS);
		ExactDistrPotential potential = assertInstanceOf(ExactDistrPotential.class, absorbedS.getPotentials().get(0));
		assertEquals(a, potential.getVariable(1), "The absorbed utility must depend directly on A");
		return potential.getTablePotential().getValues();
	}

	/** A sum of utilities: S(a0) = 10 + 5 = 15 and S(a1) = 3 + 7 = 10, by hand. */
	@Test
	void aSumOfUtilitiesIsAbsorbedIntoTheirSum() {
		buildFixture(new SumPotential(Arrays.asList(sv, u1, u2), PotentialRole.CONDITIONAL_PROBABILITY));

		assertArrayEquals(new double[] { 15, 10 }, valuesOfSAfterAbsorbing());
	}

	/** A product of utilities: S(a0) = 10 · 5 = 50 and S(a1) = 3 · 7 = 21, by hand. */
	@Test
	void aProductOfUtilitiesIsAbsorbedIntoTheirProduct() {
		buildFixture(new ProductPotential(Arrays.asList(sv, u1, u2), PotentialRole.CONDITIONAL_PROBABILITY));

		assertArrayEquals(new double[] { 50, 21 }, valuesOfSAfterAbsorbing());
	}

	/** The operation answers with a new network; the one it receives keeps its four nodes. */
	@Test
	void theSourceNetworkIsLeftUntouched() {
		buildFixture(new SumPotential(Arrays.asList(sv, u1, u2), PotentialRole.CONDITIONAL_PROBABILITY));

		BasicOperations.absorbAllIntermediateNumericNodes(net, new EvidenceCase());

		assertEquals(4, net.getNodes().size());
		assertEquals(1, net.getNode("U1").getPotentials().size(), "U1 must keep its potential in the source network");
	}
}
