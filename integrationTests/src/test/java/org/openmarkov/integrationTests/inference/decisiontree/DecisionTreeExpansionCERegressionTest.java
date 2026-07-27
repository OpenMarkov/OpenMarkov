/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.decisiontree;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Regression guard for {@link DecisionTreeExpansion}: on every network it must
 * produce the same decision tree as the reference {@link DANDecisionTreeInference}
 * — same shape, same probability, same utility or cost-effectiveness function —
 * across a range of expansion depths. It also checks the cost-effectiveness
 * invariant that the new implementation adds: node CEPs are canonical (no
 * adjacent partitions of equal cost and effectiveness).
 * <p>
 * The comparison walks both trees in step, node by node and branch by branch,
 * rather than only counting nodes and looking at the root. Anything the tree
 * carries and the interface displays is compared, the probability of each
 * branch included: it is what the tree editor prints next to the branch, and
 * leaving it unset went unnoticed while only the root was checked.
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
			"networks/dan/DAN-2therapies-always-observed.pgmx, 0",
			"networks/dan/DAN-2therapies-always-observed.pgmx, 1",
			"networks/dan/DAN-2therapies-always-observed.pgmx, 2",
			"networks/dan/DAN-2therapies-always-observed.pgmx, 3",
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

		SoftAssertions soft = new SoftAssertions();
		assertSameTree(soft, reference, actual, isCEA, "raíz");
		soft.assertAll();
	}

	// ------------------------------------------------------------------

	/**
	 * Asserts that the two trees agree at this node and, recursively, at every
	 * branch below it. Children are matched by the state of their branch instead
	 * of by position, so that a difference in the order of the branches is
	 * reported as such and does not make every node below look wrong.
	 */
	private static void assertSameTree(SoftAssertions soft, DecisionTreeNode<?> reference, DecisionTreeNode<?> actual,
			boolean isCEA, String path) {
		soft.assertThat(actual.getVariable().getName()).as("variable en %s", path)
				.isEqualTo(reference.getVariable().getName());
		soft.assertThat(actual.getNodeType()).as("tipo de nodo en %s", path).isEqualTo(reference.getNodeType());
		soft.assertThat(actual.getScenarioProbability()).as("probabilidad del escenario en %s", path)
				.isCloseTo(reference.getScenarioProbability(), RATE);
		assertSameUtility(soft, reference.getUtility(), actual.getUtility(), isCEA, path);

		Map<String, DecisionTreeBranch<?>> referenceBranches = branchesByState(reference);
		Map<String, DecisionTreeBranch<?>> actualBranches = branchesByState(actual);
		soft.assertThat(actualBranches.keySet()).as("ramas de %s", path)
				.containsExactlyElementsOf(referenceBranches.keySet());

		for (Map.Entry<String, DecisionTreeBranch<?>> entry : referenceBranches.entrySet()) {
			DecisionTreeBranch<?> referenceBranch = entry.getValue();
			DecisionTreeBranch<?> actualBranch = actualBranches.get(entry.getKey());
			if (actualBranch == null) {
				continue;
			}
			String branchPath = path + " → " + entry.getKey();
			soft.assertThat(actualBranch.getScenarioProbability()).as("probabilidad del escenario en %s", branchPath)
					.isCloseTo(referenceBranch.getScenarioProbability(), RATE);
			soft.assertThat(actualBranch.getBranchProbability()).as("probabilidad de la rama %s", branchPath)
					.isCloseTo(referenceBranch.getBranchProbability(), RATE);
			if (referenceBranch.getChild() != null && actualBranch.getChild() != null) {
				assertSameTree(soft, referenceBranch.getChild(), actualBranch.getChild(), isCEA, branchPath);
			}
		}
	}

	private static void assertSameUtility(SoftAssertions soft, Object reference, Object actual, boolean isCEA,
			String path) {
		if (isCEA) {
			CEP referenceCep = (CEP) reference;
			CEP actualCep = (CEP) actual;
			assertSameCostEffectiveness(soft, referenceCep, actualCep, path);
			soft.assertThat(adjacentEqualPartitions(actualCep))
					.as("el CEP nuevo no debe tener particiones redundantes en %s", path).isZero();
		} else {
			soft.assertThat((Double) actual).as("utilidad en %s", path).isCloseTo((Double) reference, MONEY);
		}
	}

	private static Map<String, DecisionTreeBranch<?>> branchesByState(DecisionTreeNode<?> node) {
		Map<String, DecisionTreeBranch<?>> branches = new LinkedHashMap<>();
		for (DecisionTreeElement child : node.getChildren()) {
			if (child instanceof DecisionTreeBranch<?> branch) {
				branches.put(branch.getBranchState() != null ? branch.getBranchState().getName() : "—", branch);
			}
		}
		return branches;
	}

	private static ProbNet load(String resource) throws Exception {
		URL url = DecisionTreeExpansionCERegressionTest.class.getClassLoader().getResource(resource);
		assertThat(url).as("recurso de red %s", resource).isNotNull();
		return new PGMXReader().read(url).probNet();
	}

	/** Asserts the two CEPs describe the same cost-effectiveness function across a dense range of lambdas. */
	private static void assertSameCostEffectiveness(SoftAssertions soft, CEP a, CEP b, String path) {
		double hi = 0;
		for (double t : a.getThresholds()) hi = Math.max(hi, t);
		for (double t : b.getThresholds()) hi = Math.max(hi, t);
		hi = hi > 0 ? hi * 1.5 : 1e6;
		for (int i = 0; i <= 500; i++) {
			double lambda = hi * i / 500.0;
			soft.assertThat(a.getCost(lambda)).as("coste en %s con lambda=%s", path, lambda)
					.isCloseTo(b.getCost(lambda), MONEY);
			soft.assertThat(a.getEffectiveness(lambda)).as("efectividad en %s con lambda=%s", path, lambda)
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
