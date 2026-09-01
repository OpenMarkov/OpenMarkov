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
 * from the network's own name. These tests pin that any of the four names a network can have — none,
 * one shorter than an extension, one without extension, one with another format's extension — comes
 * through whole and without a file extension of its own.
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

	/** Shorter than an extension. */
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
	void aNameWithAnotherExtensionLosesIt() throws Exception {
		assertNamesStartWith(renamedSubnetworkNames("diabetes.elv"), "diabetes-");
	}

	@Test
	void theUsualNameLosesItsExtensionToo() throws Exception {
		assertNamesStartWith(renamedSubnetworkNames("diabetes.pgmx"), "diabetes-");
	}

	/**
	 * The subnetworks only ever live in memory, so their names are not file names and none of the four
	 * starting points may leave them looking like one.
	 */
	@Test
	void noSubnetworkNameGetsAFileExtension() throws Exception {
		for (String networkName : List.of("Untitled", "id", "diabetes.elv", "diabetes.pgmx")) {
			List<String> names = renamedSubnetworkNames(networkName);

			assertFalse(names.stream().anyMatch(name -> name.endsWith(".pgmx") || name.endsWith(".elv")),
					"Starting from \"" + networkName + "\", a subnetwork name carries an extension: " + names);
		}
	}

	/** The subnetworks the tree renamed after a finding, which are the ones built from the name. */
	private static List<String> renamedSubnetworkNames(String networkName) throws Exception {
		return subnetworkNames(networkName).stream().filter(name -> name.contains("=")).toList();
	}

	private static void assertNamesStartWith(List<String> names, String expectedPrefix) {
		assertFalse(names.isEmpty(), "The tree has no nodes to check");
		assertTrue(names.stream().allMatch(name -> name.startsWith(expectedPrefix)),
				"Some subnetwork name does not start with \"" + expectedPrefix + "\": " + names);
	}
}
