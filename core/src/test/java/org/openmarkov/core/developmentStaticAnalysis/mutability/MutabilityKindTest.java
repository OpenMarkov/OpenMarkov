/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.developmentStaticAnalysis.mutability;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * The mutability checker had never been run against classes with real fields: the only class of the
 * application that claims to be immutable, {@code PNConstraint}, has no fields at all, so the
 * verification passed without ever exercising the checking itself. These are classes with fields, one
 * shape per test, and the verdict each one deserves.
 *
 * @author Manuel Arias
 */
class MutabilityKindTest {

	// -----------------------------------------------------------------------
	// Exterior: can a field be reassigned after the object is built?
	// -----------------------------------------------------------------------

	@SuppressWarnings("unused") static class NonFinalPrimitive {
		int age;
	}

	/**
	 * The example the package documentation itself gives of exterior mutable. It used to be reported as
	 * immutable: the check asked about the type of the field, and a primitive type answered "nothing to
	 * see here" before anyone looked at whether the field was final.
	 */
	@Test final void aFieldThatIsNotFinalStopsExteriorImmutability() {
		Mutability mutability = MutabilityKind.EXTERIOR.mutabilityOf(NonFinalPrimitive.class);

		Assertions.assertTrue(mutability.isMutable());
		Assertions.assertEquals("age", mutability.nonFinalFields()[0].getName());
	}

	@SuppressWarnings("unused") static class FinalArray {
		final int[] data = { 1, 2 };
	}

	/** The other way round: a final array cannot be reassigned, so it does not stop it. */
	@Test final void aFinalArrayDoesNotStopExteriorImmutability() {
		Assertions.assertTrue(MutabilityKind.EXTERIOR.mutabilityOf(FinalArray.class).isImmutable());
	}

	static class InheritsANonFinalField extends NonFinalPrimitive {
	}

	/** A field that can be reassigned in the parent can be reassigned in the child as well. */
	@Test final void anInheritedFieldThatIsNotFinalStopsExteriorImmutability() {
		Assertions.assertTrue(MutabilityKind.EXTERIOR.mutabilityOf(InheritsANonFinalField.class).isMutable());
	}

	@SuppressWarnings("unused") static class WithAStaticCounter {
		static int howMany;
		final int number = 1;
	}

	/** A static field is state of the class, not of the object. */
	@Test final void aStaticFieldDoesNotStopExteriorImmutability() {
		Assertions.assertTrue(MutabilityKind.EXTERIOR.mutabilityOf(WithAStaticCounter.class).isImmutable());
	}

	@SuppressWarnings("unused") static class Exempted {
		@ConsiderFieldAsExteriorImmutable int age;
	}

	/** The exemption annotation now waives the check its name refers to; it used to waive the other one. */
	@Test final void anExemptedFieldDoesNotStopExteriorImmutability() {
		Assertions.assertTrue(MutabilityKind.EXTERIOR.mutabilityOf(Exempted.class).isImmutable());
	}

	// -----------------------------------------------------------------------
	// Interior: can the contents of a field change?
	// -----------------------------------------------------------------------

	@SuppressWarnings("unused") static class FinalList {
		final ArrayList<String> names = new ArrayList<>();
	}

	/** The documented example: the reference cannot be reassigned, but the list can be added to. */
	@Test final void aFinalListStopsInteriorImmutabilityButNotExteriorOne() {
		Assertions.assertTrue(MutabilityKind.EXTERIOR.mutabilityOf(FinalList.class).isImmutable());
		Assertions.assertTrue(MutabilityKind.INTERIOR.mutabilityOf(FinalList.class).isMutable());
	}

	/**
	 * An array is the plainest mutable content there is, and it was being missed: the interior walk
	 * started from the <em>types</em> of the fields and an array type has no fields of its own to walk
	 * into, so nothing was ever examined. The fields of the class itself are looked at now.
	 */
	@Test final void aFinalArrayStopsInteriorImmutability() {
		Assertions.assertTrue(MutabilityKind.INTERIOR.mutabilityOf(FinalArray.class).isMutable());
	}

	@SuppressWarnings("unused") static class OnlyImmutableContents {
		final int number = 1;
		final String name = "a";
	}

	@Test final void contentsThatCannotChangeDoNotStopInteriorImmutability() {
		Assertions.assertTrue(MutabilityKind.INTERIOR.mutabilityOf(OnlyImmutableContents.class).isImmutable());
	}
}
