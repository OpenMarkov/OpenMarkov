/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link ConstraintManager} builds the constraints of a network type by reflection, through the
 * no-argument constructor of every class annotated with {@link Constraint}. These tests pin what
 * the list must contain: no repeated constraint, nothing the network type forbids, and the optional
 * ones when they are asked for — including when a constraint keeps data inside and can only be
 * created by its caller.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ConstraintManagerTest {

	private static List<NetworkType> allNetworkTypes() {
		return NetworkTypeUtils.NETWORK_TYPE_CLASSES.stream().map(NetworkTypeUtils::safeInstanciate).toList();
	}

	private static Map<Class<?>, Long> countByClass(List<PNConstraint> constraints) {
		return constraints.stream().collect(Collectors.groupingBy(PNConstraint::getClass, Collectors.counting()));
	}

	/**
	 * A constraint that keeps data inside, such as {@code MaxNumParents} or
	 * {@code ModelNetworkConstraint}, cannot be built without arguments: the manager leaves it out
	 * instead of failing.
	 */
	@Test public void everyNetworkTypeCanBuildItsOptionalConstraints() {
		for (NetworkType type : allNetworkTypes()) {
			Assertions.assertDoesNotThrow(
					() -> ConstraintManager.getUniqueInstance().buildConstraintList(type, true),
					"Building the optional constraints of " + type.getClass().getSimpleName() + " failed");
		}
	}

	/**
	 * Asking for the optionals has to give more than asking only for the mandatory ones; otherwise the
	 * previous test would also pass on a version that quietly returned nothing.
	 */
	@Test public void theOptionalsAddSomethingToTheMandatoryOnes() {
		for (NetworkType type : allNetworkTypes()) {
			List<PNConstraint> mandatory = ConstraintManager.getUniqueInstance().buildConstraintList(type, false);
			List<PNConstraint> withOptionals = ConstraintManager.getUniqueInstance().buildConstraintList(type, true);
			Assertions.assertTrue(withOptionals.size() > mandatory.size(),
					type.getClass().getSimpleName() + " gained no constraint when asked for the optional ones");
		}
	}

	/**
	 * MDP re-declares as mandatory {@code NoCycle}, {@code NoSelfLoop} and {@code OnlyDirectedLinks},
	 * which already are mandatory by default, so its list came out with 21 entries of which 3 were
	 * repeated. Harmless further downstream — the network stores its constraints in a sorted set, which
	 * collapses them — but the list is what other callers read, and it was announcing a network with more
	 * rules than it has.
	 */
	@Test public void noNetworkTypeGetsTheSameConstraintTwice() {
		Map<String, List<String>> duplicatesByType = new HashMap<>();
		for (NetworkType type : allNetworkTypes()) {
			List<String> duplicates = countByClass(ConstraintManager.getUniqueInstance().buildConstraintList(type))
					.entrySet()
					.stream()
					.filter(entry -> entry.getValue() > 1)
					.map(entry -> entry.getKey().getSimpleName())
					.sorted()
					.toList();
			if (!duplicates.isEmpty()) {
				duplicatesByType.put(type.getClass().getSimpleName(), duplicates);
			}
		}
		Assertions.assertEquals(Map.of(), duplicatesByType, "Constraints repeated in the list of a network type");
	}

	/**
	 * A network type that forbids a constraint must not receive it. Nothing observed was breaking this,
	 * but the removal walked the list by position and deleted without stepping back, so it would have kept
	 * one copy of any repeated constraint.
	 */
	@Test public void aConstraintForbiddenByTheNetworkTypeIsNotInItsList() {
		for (NetworkType type : allNetworkTypes()) {
			List<String> forbidden = type.getOverwrittenConstraints()
										 .entrySet()
										 .stream()
										 .filter(entry -> entry.getValue() == ConstraintBehavior.NO)
										 .map(entry -> entry.getKey().getSimpleName())
										 .toList();
			List<String> present = ConstraintManager.getUniqueInstance()
													.buildConstraintList(type)
													.stream()
													.map(constraint -> constraint.getClass().getSimpleName())
													.toList();
			List<String> survivors = forbidden.stream().filter(present::contains).sorted().toList();
			Assertions.assertEquals(List.of(), survivors,
					"Constraints forbidden by " + type.getClass().getSimpleName() + " that are in its list anyway");
		}
	}
}
