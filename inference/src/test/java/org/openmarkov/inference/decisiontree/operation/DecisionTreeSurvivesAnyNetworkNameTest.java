/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.decisiontree.operation;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * While building the tree, each subnetwork is renamed after the finding that produced it, starting
 * from the network's own name with the last five characters chopped off — which assumes every
 * network is called something ending in {@code .pgmx}.
 *
 * <p>Measured against the previous code: a network with no name threw
 * {@code NullPointerException} and a name shorter than the extension threw
 * {@code StringIndexOutOfBoundsException}. Neither is exotic — every network built in memory has no
 * name, which is why the older tests of this package had to christen theirs {@code simple-id.pgmx}
 * before they could get past it. Any other name simply lost its last five characters without a
 * word: {@code Untitled}, which is what a network not yet saved is called, became {@code Unt}.
 *
 * @author Manuel Arias
 */
class DecisionTreeSurvivesAnyNetworkNameTest {

	private static final int DEPTH = 3;

	/** The names of the subnetworks the tree hangs on its nodes, which is where the chop showed. */
	private static List<String> subnetworkNames(String networkName) throws Exception {
		ProbNet id = TestNetworks.buildSimpleID();
		id.setName(networkName);
		List<String> names = new ArrayList<>();
		collect(new DecisionTreeManagerImpl().buildDecisionTree(id, DEPTH), names);
		return names;
	}

	private static void collect(DecisionTreeElement element, List<String> names) {
		if (element instanceof DecisionTreeNode<?> node) {
			names.add(node.getNetwork().getName());
		}
		element.getChildren().forEach(child -> collect(child, names));
	}

	/** Every network built in memory, rather than read from a file, is in this state. */
	@Test
	void aNetworkWithNoNameDoesNotBringTheTreeDown() {
		assertDoesNotThrow(() -> subnetworkNames(null));
	}

	/** Shorter than the five characters the old code chopped off. */
	@Test
	void aNameShorterThanTheExtensionDoesNotBringTheTreeDown() {
		assertDoesNotThrow(() -> subnetworkNames("id"));
	}

	/** What the application calls a network that has not been saved yet. */
	@Test
	void aNameWithNoExtensionIsKeptWhole() throws Exception {
		assertNamesStartWith(subnetworkNames("Untitled"), "Untitled");
	}

	/** A network read from a file of one of the other formats the application understands. */
	@Test
	void aNameWithAnotherExtensionIsKeptWhole() throws Exception {
		assertNamesStartWith(subnetworkNames("diabetes.elv"), "diabetes.elv");
	}

	/**
	 * The usual name loses its extension once, and only once: the subnetwork names get one appended,
	 * and starting from the whole name would leave them ending in two.
	 */
	@Test
	void theUsualNameLosesItsExtensionExactlyOnce() throws Exception {
		List<String> names = subnetworkNames("diabetes.pgmx");

		assertNamesStartWith(names, "diabetes");
		assertFalse(names.stream().anyMatch(name -> name.endsWith(".pgmx.pgmx")),
				"A subnetwork name ends in two extensions: " + names);
	}

	private static void assertNamesStartWith(List<String> names, String expectedPrefix) {
		assertFalse(names.isEmpty(), "The tree has no nodes to check");
		assertTrue(names.stream().allMatch(name -> name.startsWith(expectedPrefix)),
				"Some subnetwork name does not start with \"" + expectedPrefix + "\": " + names);
	}
}
