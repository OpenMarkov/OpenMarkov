/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two constraints are the same when they are of the same class.
 *
 * @author Manuel Arias
 */
class PNConstraintEqualsTest {

	/**
	 * What {@code equals} promises for null is false, not an exception.
	 */
	@Test
	void aConstraintIsNotEqualToNull() {
		assertFalse(new NoCycle().equals(null));
	}

	@Test
	void twoConstraintsOfTheSameClassAreEqual() {
		assertEquals(new NoCycle(), new NoCycle());
	}

	@Test
	void constraintsOfDifferentClassesAreNotEqual() {
		assertNotEquals(new NoCycle(), new NoSelfLoop());
	}

	@Test
	void equalConstraintsShareTheirHashCode() {
		assertEquals(new NoCycle().hashCode(), new NoCycle().hashCode());
	}

	@Test
	void aConstraintIsEqualToItself() {
		NoCycle constraint = new NoCycle();

		assertTrue(constraint.equals(constraint));
	}
}
