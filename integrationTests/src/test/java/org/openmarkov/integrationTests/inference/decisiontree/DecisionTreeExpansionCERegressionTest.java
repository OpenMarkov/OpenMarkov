/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.decisiontree;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.EvaluationType;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.inference.algorithm.DecisionTreeExpansion;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANDecisionTreeInference;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.IDDecisionTreeInference;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;

/**
 * Regression guard for {@link DecisionTreeExpansion}: on every network it must
 * produce the same decision tree as the reference {@link DANDecisionTreeInference}
 * — same shape, same probability, same utility or cost-effectiveness function —
 * across a range of expansion depths. It also checks the cost-effectiveness
 * invariant that the new implementation adds: node CEPs are canonical (no
 * adjacent partitions of equal cost and effectiveness).
 * <p>
 * The comparison mirrors the stand-alone {@code VerifyExpansion} tool, turned
 * into assertions so the build fails on any divergence.
 */
class DecisionTreeExpansionCERegressionTest {

	private static final Offset<Double> MONEY = Offset.offset(1e-4);
	private static final Offset<Double> RATE = Offset.offset(1e-7);

	@ParameterizedTest(name = "{0} @ profundidad {1}")
	@CsvSource({
			// red, profundidad
			"networks/id/ID-decide-test.pgmx, 0",
			"networks/id/ID-decide-test.pgmx, 2",
			"networks/id/ID-decide-test.pgmx, 6",
			"networks/id/ID-CEA-minimal.pgmx, 0",
			"networks/id/ID-CEA-minimal.pgmx, 3",
			"networks/dan/DAN-CE-2-test-problem.pgmx, 0",
			"networks/dan/DAN-CE-2-test-problem.pgmx, 2",
			"networks/dan/DAN-CE-2-test-problem.pgmx, 4",
			"networks/dan/DAN-CE-2-test-problem.pgmx, 6",
			"networks/dan/DAN-CE-3-test-problem.pgmx, 4",
			"networks/dan/DAN-CE-3-test-problem.pgmx, 6",
	})
	void matchesReference(String resource, int depth) throws Exception {
		ProbNet net = load(resource);
		boolean isCEA = net.getInferenceOptions().getMultiCriteriaOptions()
				.getMulticriteriaType() != MulticriteriaOptions.Type.UNICRITERION;
		boolean isID = net.getNetworkType() instanceof InfluenceDiagramType;

		DecisionTreeNode<?> reference = (isID
				? new IDDecisionTreeInference(net.copy(), depth, true, isCEA)
				: new DANDecisionTreeInference(net.copy(), depth, true, isCEA)).getDecisionTree();

		DecisionTreeNode<?> actual = new DecisionTreeExpansion(isCEA ? EvaluationType.CE : EvaluationType.UNICRITERION)
				.expandTree(net.copy(), new EvidenceCase(), depth);

		assertThat(countNodes(actual)).as("número de nodos").isEqualTo(countNodes(reference));
		assertThat(actual.getScenarioProbability()).as("probabilidad de la raíz")
				.isCloseTo(reference.getScenarioProbability(), RATE);

		if (isCEA) {
			CEP refCep = (CEP) reference.getUtility();
			CEP actCep = (CEP) actual.getUtility();
			assertSameCostEffectiveness(refCep, actCep);
			assertThat(adjacentEqualPartitions(actCep))
					.as("el CEP nuevo no debe tener particiones redundantes").isZero();
		} else {
			assertThat((Double) actual.getUtility()).as("utilidad")
					.isCloseTo((Double) reference.getUtility(), MONEY);
		}
	}

	// ------------------------------------------------------------------

	private static ProbNet load(String resource) throws Exception {
		URL url = DecisionTreeExpansionCERegressionTest.class.getClassLoader().getResource(resource);
		assertThat(url).as("recurso de red %s", resource).isNotNull();
		return new PGMXReader().read(url).probNet();
	}

	/** Asserts the two CEPs describe the same cost-effectiveness function across a dense range of lambdas. */
	private static void assertSameCostEffectiveness(CEP a, CEP b) {
		double hi = 0;
		for (double t : a.getThresholds()) hi = Math.max(hi, t);
		for (double t : b.getThresholds()) hi = Math.max(hi, t);
		hi = hi > 0 ? hi * 1.5 : 1e6;
		for (int i = 0; i <= 500; i++) {
			double lambda = hi * i / 500.0;
			assertThat(a.getCost(lambda)).as("coste en lambda=%s", lambda).isCloseTo(b.getCost(lambda), MONEY);
			assertThat(a.getEffectiveness(lambda)).as("efectividad en lambda=%s", lambda)
					.isCloseTo(b.getEffectiveness(lambda), RATE);
		}
	}

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

	private static int countNodes(DecisionTreeElement element) {
		if (element == null) {
			return 0;
		}
		if (element instanceof DecisionTreeNode<?> node) {
			int n = 1;
			for (DecisionTreeElement child : node.getChildren()) {
				n += countNodes(child);
			}
			return n;
		}
		if (element instanceof DecisionTreeBranch<?> branch) {
			return countNodes(branch.getChild());
		}
		return 0;
	}
}
