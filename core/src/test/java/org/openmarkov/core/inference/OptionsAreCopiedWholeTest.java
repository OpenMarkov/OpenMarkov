/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The inference options of a network are copied whenever the network is copied
 * ({@code ProbNetCopier}), so anything their copy constructors forget is a choice of the user that
 * a copy of the network silently drops.
 *
 * @author Manuel Arias
 */
class OptionsAreCopiedWholeTest {

	/**
	 * Including {@code psa}, which says whether a probabilistic sensitivity analysis is being run and
	 * which the discrete-event simulation reads in three places.
	 */
	@Test
	void theMonteCarloOptionsKeepEveryFieldWhenCopied() {
		MonteCarloOptions original = new MonteCarloOptions();
		original.setNumSimulations(123);
		original.setNumSeries(7);
		original.setResultsToExcel(true);
		original.setMean(false);
		original.setTrimmedMean(false);
		original.setMedian(true);
		original.setInputFilePath(Path.of("values.csv"));
		original.setPsa(true);
		original.setTextualLog(true);
		original.setSum(true);

		MonteCarloOptions copy = new MonteCarloOptions(original);

		assertAll("Fields lost when copying the Monte Carlo options",
				() -> assertEquals(123, copy.getNumSimulations()),
				() -> assertEquals(7, copy.getNumSeries()),
				() -> assertEquals(true, copy.isResultsToExcel()),
				() -> assertEquals(false, copy.isMean()),
				() -> assertEquals(false, copy.isTrimmedMean()),
				() -> assertEquals(true, copy.isMedian()),
				() -> assertEquals(Path.of("values.csv"), copy.getInputFilePath()),
				() -> assertEquals(true, copy.isPsa(), "psa: the sensitivity analysis is switched off"),
				() -> assertEquals(true, copy.isTextualLog(), "textualLog"),
				() -> assertEquals(true, copy.isSum(), "sum"));
	}

	/** {@code clone()} delegates on the copy constructor, so it lost the same three. */
	@Test
	void cloningTheMonteCarloOptionsKeepsTheSameFields() {
		MonteCarloOptions original = new MonteCarloOptions();
		original.setPsa(true);
		original.setTextualLog(true);
		original.setSum(true);

		MonteCarloOptions clone = (MonteCarloOptions) original.clone();

		assertAll(() -> assertEquals(true, clone.isPsa()),
				() -> assertEquals(true, clone.isTextualLog()),
				() -> assertEquals(true, clone.isSum()));
	}

	/** Whichever constructor built them, the sub-options are there for the copy to carry. */
	@Test
	void optionsBuiltFromANetworkCanBeCopied() {
		InferenceOptions original = new InferenceOptions(new ProbNet(), new Variable("index", 3));

		InferenceOptions copy = assertDoesNotThrow(() -> new InferenceOptions(original));

		assertAll(() -> assertNotNull(copy.getMultiCriteriaOptions()),
				() -> assertNotNull(copy.getTemporalOptions()),
				() -> assertNotNull(copy.getMonteCarloOptions()));
	}

	/** A copy that does not know which network it belongs to is not a copy of these options. */
	@Test
	void theCopyKeepsTheNetworkAndTheSimulationVariable() {
		ProbNet probNet = new ProbNet();
		Variable simulationIndex = new Variable("index", 3);
		InferenceOptions original = new InferenceOptions(probNet, simulationIndex);

		InferenceOptions copy = new InferenceOptions(original);

		assertAll(() -> assertSame(probNet, copy.probNet),
				() -> assertSame(simulationIndex, copy.simulationIndexVariable));
	}

	/** {@code iciAwareVE} switches the factorized projection of canonical models on. */
	@Test
	void theCopyKeepsTheIciAwareSwitch() {
		InferenceOptions original = new InferenceOptions();
		original.setIciAwareVE(true);
		original.setIciMinParentsToFactorize(2);

		InferenceOptions copy = new InferenceOptions(original);

		assertAll(() -> assertEquals(true, copy.isIciAwareVE()),
				() -> assertEquals(2, copy.getIciMinParentsToFactorize()));
	}

	/** {@code ProbNet.copy()} is how inference prunes a network, so the options must survive it. */
	@Test
	void theShallowCopyOfANetworkCarriesItsOptions() {
		ProbNet network = new ProbNet();
		network.getInferenceOptions().setIciAwareVE(true);
		Variable simulationIndex = new Variable("index", 3);
		network.getInferenceOptions().simulationIndexVariable = simulationIndex;

		ProbNet copy = network.copy();

		assertAll(() -> assertEquals(true, copy.getInferenceOptions().isIciAwareVE()),
				() -> assertSame(simulationIndex, copy.getInferenceOptions().simulationIndexVariable));
	}

	/** The sub-options must be copies, not the very same objects the original holds. */
	@Test
	void theSubOptionsOfTheCopyAreNotSharedWithTheOriginal() {
		InferenceOptions original = new InferenceOptions();

		InferenceOptions copy = new InferenceOptions(original);

		assertAll(() -> assertNotSameObject(original.getMultiCriteriaOptions(), copy.getMultiCriteriaOptions()),
				() -> assertNotSameObject(original.getTemporalOptions(), copy.getTemporalOptions()),
				() -> assertNotSameObject(original.getMonteCarloOptions(), copy.getMonteCarloOptions()));
	}

	private static void assertNotSameObject(Object original, Object copy) {
		if (original == copy) {
			throw new AssertionError("The copy shares this object with the original: " + original.getClass());
		}
	}
}
