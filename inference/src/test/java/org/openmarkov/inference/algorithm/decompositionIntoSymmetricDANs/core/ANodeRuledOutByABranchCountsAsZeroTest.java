/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A node that a branch rules out is worth zero in the nodes that combine utilities (error 200).
 *
 * @author Manuel Arias
 */
class ANodeRuledOutByABranchCountsAsZeroTest {

	/**
	 * Treating (D = yes) makes X possible, and X has a cost, which "Adjusted" multiplies by the factor F. The net
	 * benefit adds the adjusted cost to the effect of D, which is 1 without treatment.
	 */
	private static ProbNet dan() {
		ProbNet dan = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		Variable d = new Variable("D", "no", "yes");
		Variable x = new Variable("X", "mild", "severe");
		Variable cost = new Variable("Cost");
		Variable factor = new Variable("F");
		Variable adjusted = new Variable("Adjusted");
		Variable effect = new Variable("Effect");
		Variable benefit = new Variable("Benefit");
		dan.addNode(d, NodeType.DECISION);
		dan.addNode(x, NodeType.CHANCE);
		Criterion criterion = new Criterion("Benefit");
		dan.setDecisionCriteria(List.of(criterion));
		for (Variable utility : List.of(cost, factor, adjusted, effect, benefit)) {
			utility.setDecisionCriterion(criterion);
			dan.addNode(utility, NodeType.UTILITY);
		}
		dan.addLink(d, x, true);
		dan.addLink(x, cost, true);
		dan.addLink(cost, adjusted, true);
		dan.addLink(factor, adjusted, true);
		dan.addLink(d, effect, true);
		dan.addLink(adjusted, benefit, true);
		dan.addLink(effect, benefit, true);
		for (var state : x.getStates()) {
			LinkOperations.setCompatibilityValue(dan.getLink(dan.getNode(d), dan.getNode(x), true),
					d.getStates()[0], state, 0);
		}

		dan.addPotential(table(List.of(x, d), PotentialRole.CONDITIONAL_PROBABILITY, 0.5, 0.5, 0.5, 0.5));
		dan.addPotential(table(List.of(cost, x), PotentialRole.UNSPECIFIED, 100, 300));
		dan.addPotential(table(List.of(factor), PotentialRole.UNSPECIFIED, -0.01));
		dan.addPotential(table(List.of(effect, d), PotentialRole.UNSPECIFIED, 1, 5));
		dan.addPotential(new ProductPotential(List.of(adjusted, cost, factor), PotentialRole.UNSPECIFIED));
		dan.addPotential(new SumPotential(List.of(benefit, adjusted, effect), PotentialRole.UNSPECIFIED));
		return dan;
	}

	private static TablePotential table(List<Variable> variables, PotentialRole role, double... values) {
		TablePotential potential = new TablePotential(variables, role);
		potential.setValues(values);
		return potential;
	}

	@Test
	void aProductWithARuledOutFactorGoesWithItsOtherFactors() throws Exception {
		ProbNet dan = dan();
		Variable d = dan.getVariable("D");
		ProbNet withoutTreatment = DANOperations.instantiate(dan, d, d.getStates()[0]);

		assertNull(withoutTreatment.getNode("Adjusted"));
		assertNull(withoutTreatment.getNode("F"), "The factor was left as a utility of its own");
		assertEquals(1, new DANDecompositionIntoSymmetricDANsEvaluation(withoutTreatment).getUtility()
				.getValues()[0], 1E-12);
	}
}
