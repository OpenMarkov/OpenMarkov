/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CEP;

/**
 * Unit tests for {@link DecisionTreeExpansion#normalizeCEP(CEP)} — the merging
 * of adjacent CEP partitions of equal cost and effectiveness into a single
 * partition. The normalization must make the representation minimal without
 * changing the cost-effectiveness function.
 */
class NormalizeCEPTest {

	private static final double TOL = 1e-9;

	/**
	 * Builds a CEP from partitions given as {cost, effectiveness} rows and the
	 * {@code thresholds} between them (one fewer than the number of rows). The
	 * interventions are left null: normalization looks only at cost and
	 * effectiveness.
	 */
	private static CEP cep(double[][] rows, double[] thresholds) {
		CEP.CEPBuilder builder = new CEP.CEPBuilder();
		for (int i = 0; i < rows.length - 1; i++) {
			builder.addRow(null, rows[i][0], rows[i][1], thresholds[i]);
		}
		double[] last = rows[rows.length - 1];
		return builder.build(null, last[0], last[1]);
	}

	/** Number of adjacent partitions with identical cost and effectiveness. */
	private static int adjacentEqualPartitions(CEP c) {
		double[] costs = c.getCosts();
		double[] effs = c.getEffectivities();
		int n = 0;
		for (int i = 1; i < costs.length; i++) {
			if (costs[i] == costs[i - 1] && effs[i] == effs[i - 1]) {
				n++;
			}
		}
		return n;
	}

	/** Asserts that two CEPs describe the same cost-effectiveness function over a range of lambdas. */
	private static void assertSameFunction(CEP a, CEP b) {
		double hi = 0;
		for (double t : a.getThresholds()) hi = Math.max(hi, t);
		for (double t : b.getThresholds()) hi = Math.max(hi, t);
		hi = hi > 0 ? hi * 1.5 : 1e6;
		for (int i = 0; i <= 1000; i++) {
			double lambda = hi * i / 1000.0;
			assertThat(a.getCost(lambda)).as("coste en lambda=%s", lambda)
					.isCloseTo(b.getCost(lambda), org.assertj.core.data.Offset.offset(1e-6));
			assertThat(a.getEffectiveness(lambda)).as("efectividad en lambda=%s", lambda)
					.isCloseTo(b.getEffectiveness(lambda), org.assertj.core.data.Offset.offset(TOL));
		}
	}

	@Test
	void collapsesTrailingRedundantPartition() {
		// Partitions 1 and 2 are identical (10, 2): the boundary between them is redundant.
		CEP original = cep(new double[][] { { 0, 1 }, { 10, 2 }, { 10, 2 } }, new double[] { 5, 8 });
		CEP expected = cep(new double[][] { { 0, 1 }, { 10, 2 } }, new double[] { 5 });

		CEP normalized = DecisionTreeExpansion.normalizeCEP(original);

		assertThat(normalized.getCosts()).hasSize(2);
		assertThat(adjacentEqualPartitions(normalized)).isZero();
		assertThat(normalized.equals(expected)).as("CEP normalizado == esperado").isTrue();
		assertSameFunction(original, normalized);
	}

	@Test
	void collapsesMiddleRedundantPartition() {
		// The redundant pair is in the middle: [A, B, B, C]. Only the B|B boundary is dropped.
		CEP original = cep(new double[][] { { 0, 1 }, { 10, 2 }, { 10, 2 }, { 20, 3 } },
				new double[] { 5, 8, 12 });
		CEP expected = cep(new double[][] { { 0, 1 }, { 10, 2 }, { 20, 3 } }, new double[] { 5, 12 });

		CEP normalized = DecisionTreeExpansion.normalizeCEP(original);

		assertThat(normalized.getCosts()).hasSize(3);
		assertThat(adjacentEqualPartitions(normalized)).isZero();
		assertThat(normalized.equals(expected)).isTrue();
		assertSameFunction(original, normalized);
	}

	@Test
	void collapsesRunOfSeveralEqualPartitions() {
		// Three identical partitions in a row collapse to one.
		CEP original = cep(new double[][] { { 5, 1 }, { 5, 1 }, { 5, 1 } }, new double[] { 3, 7 });

		CEP normalized = DecisionTreeExpansion.normalizeCEP(original);

		assertThat(normalized.getCosts()).hasSize(1);
		assertThat(normalized.getCosts()[0]).isCloseTo(5, org.assertj.core.data.Offset.offset(TOL));
		assertThat(normalized.getThresholds()).isEmpty();
		assertSameFunction(original, normalized);
	}

	@Test
	void leavesACleanCEPUnchanged() {
		// No two adjacent partitions are equal: nothing to merge, same object returned.
		CEP original = cep(new double[][] { { 0, 1 }, { 10, 2 }, { 20, 3 } }, new double[] { 5, 12 });

		CEP normalized = DecisionTreeExpansion.normalizeCEP(original);

		assertThat(normalized).isSameAs(original);
	}

	@Test
	void doesNotMergeNonAdjacentEqualPartitions() {
		// Partitions 0 and 2 are equal but not adjacent: they must stay separate.
		CEP original = cep(new double[][] { { 10, 2 }, { 30, 5 }, { 10, 2 } }, new double[] { 5, 12 });

		CEP normalized = DecisionTreeExpansion.normalizeCEP(original);

		assertThat(normalized.getCosts()).hasSize(3);
		assertThat(normalized).isSameAs(original);
	}

	@Test
	void singlePartitionIsUnchanged() {
		CEP original = cep(new double[][] { { 7, 4 } }, new double[] {});

		CEP normalized = DecisionTreeExpansion.normalizeCEP(original);

		assertThat(normalized).isSameAs(original);
		assertThat(normalized.getCosts()).hasSize(1);
	}

	@Test
	void isIdempotent() {
		CEP original = cep(new double[][] { { 0, 1 }, { 10, 2 }, { 10, 2 }, { 10, 2 } },
				new double[] { 5, 8, 11 });

		CEP once = DecisionTreeExpansion.normalizeCEP(original);
		CEP twice = DecisionTreeExpansion.normalizeCEP(once);

		assertThat(adjacentEqualPartitions(once)).isZero();
		assertThat(twice).isSameAs(once);
	}
}
