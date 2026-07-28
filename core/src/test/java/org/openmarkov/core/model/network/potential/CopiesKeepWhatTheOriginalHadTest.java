/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Three more members of the copy family of the campaign, out of reach of the automatic clone
 * suite because no network stored on disk carries these potentials in the states exercised here.
 *
 * @author Manuel Arias
 */
public class CopiesKeepWhatTheOriginalHadTest {

	private static Variable plain(String name) {
		return new Variable(name, new State[] { new State("a"), new State("b") });
	}

	private static Variable event(String name) {
		Variable variable = new Variable(name, new State[] { new State("no"), new State("yes") });
		variable.setVariableType(VariableType.EVENT);
		return variable;
	}

	/**
	 * The copy constructor recalculated {@code discreteValue} instead of copying it, wiping the
	 * value {@code setDiscreteValue} sets by hand — the discount of cost-effectiveness analyses.
	 * The inconsistency was visible in the class itself: {@code deepCopy} preserved it.
	 */
	@Test
	public void aManuallySetDiscreteValueSurvivesTheCopy() {
		UniformPotential original = new UniformPotential(List.of(plain("A")), PotentialRole.CONDITIONAL_PROBABILITY);
		original.setDiscreteValue(0.97);

		UniformPotential copy = (UniformPotential) original.copy();

		assertEquals(0.97, copy.getDiscreteValue(),
				"The copy recalculated the discrete value instead of copying the one set by hand");
	}

	/**
	 * {@code GTablePotential.copy()} did not route through the copy constructor of the base
	 * class, which is what replicates the comment and clones the criterion: the copy came out
	 * with an empty comment and no criterion.
	 */
	@Test
	public void aGeneralizedTableCopyKeepsCommentAndCriterion() {
		GTablePotential<String> original = new GTablePotential<>(List.of(plain("A")),
				PotentialRole.CONDITIONAL_PROBABILITY);
		original.elementTable.add("an element");
		original.setComment("the comment of the original");
		original.setCriterion(new Criterion("cost"));

		@SuppressWarnings("unchecked")
		GTablePotential<String> copy = (GTablePotential<String>) original.copy();

		assertEquals("the comment of the original", copy.getComment());
		assertEquals("cost", copy.getCriterion().getCriterionName());
		assertEquals(List.of("an element"), copy.elementTable);
		assertNotSame(original.elementTable, copy.elementTable, "the element table must not be shared");
	}

	/**
	 * {@code Potential.deepCopy} looks up by reflection a constructor whose parameter is exactly
	 * the runtime class. {@code GTablePotential} had none, so every deep copy of a generalized
	 * table died with an {@code UnreachableException} wrapping {@code NoSuchMethodException}.
	 */
	@Test
	public void aGeneralizedTableCanBeDeepCopied() {
		Variable a = plain("A");
		GTablePotential<String> original = new GTablePotential<>(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY);
		original.elementTable.add("an element");

		ProbNet destination = new ProbNet(BayesianNetworkType.getUniqueInstance());
		destination.addNode(new Variable(a), NodeType.CHANCE);

		Potential deepCopy = assertDoesNotThrow(() -> original.deepCopy(destination));
		assertEquals(List.of("an element"), ((GTablePotential<?>) deepCopy).elementTable);
	}

	/**
	 * The copy constructor built a copy of the original's tree — and dropped it: the local
	 * variable was never assigned, so the copy kept the fresh tree its delegated constructor
	 * had just built. The mark used to tell the two trees apart is the comment, which every
	 * copy of a potential replicates.
	 */
	@Test
	public void theCopyCarriesTheTreeOfTheOriginalAndNotAFreshOne() {
		List<Variable> variables = new ArrayList<>(List.of(plain("C"), plain("P"), event("E1")));
		TreeWithExcludedEventsPotential original = new TreeWithExcludedEventsPotential(variables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		TreeADDPotential markedTree = new TreeADDPotential(new ArrayList<>(List.of(variables.get(0), variables.get(1))),
				PotentialRole.CONDITIONAL_PROBABILITY);
		markedTree.setComment("the tree of the original");
		original.setNoEventTree(markedTree);

		TreeWithExcludedEventsPotential copy = (TreeWithExcludedEventsPotential) original.copy();

		assertEquals("the tree of the original", copy.getNoEventTree().getComment(),
				"The copy kept a fresh empty tree instead of the tree of the original");
		assertNotSame(original.getNoEventTree(), copy.getNoEventTree(), "the tree must be a copy, not shared");
	}
}
