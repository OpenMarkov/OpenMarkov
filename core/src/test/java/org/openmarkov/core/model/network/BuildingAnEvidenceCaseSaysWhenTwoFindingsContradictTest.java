/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An evidence case built from a list of findings keeps every one of them. Two findings that give
 * the same variable different states cannot both be kept, and saying so is the only honest answer:
 * dropping one of them evaluates data the caller never gave.
 *
 * @author Manuel Arias
 */
class BuildingAnEvidenceCaseSaysWhenTwoFindingsContradictTest {

	private final Variable disease = new Variable("Disease", "absent", "present");
	private final Variable test = new Variable("Test", "negative", "positive");

	@Test
	void twoFindingsOfTheSameVariableWithDifferentStatesAreRefused() {
		List<Finding> findings = List.of(new Finding(disease, 0), new Finding(disease, 1));

		IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther thrown = assertThrows(
				IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther.class,
				() -> new EvidenceCase(findings),
				"One of the two findings was dropped instead of being reported");

		assertEquals(disease, thrown.newFinding.getVariable(),
				"The report does not name the variable of the finding that could not be added");
	}

	@Test
	void findingsOfDifferentVariablesAreAllKept() throws Exception {
		EvidenceCase evidence = new EvidenceCase(List.of(new Finding(disease, 1), new Finding(test, 1)));

		assertEquals(2, evidence.getFindings().size(), "A finding was lost");
	}

	@Test
	void theSameFindingTwiceIsNotAContradiction() throws Exception {
		EvidenceCase evidence = new EvidenceCase(List.of(new Finding(disease, 1), new Finding(disease, 1)));

		assertEquals(1, evidence.getFindings().size(), "The repeated finding was counted twice");
	}
}
