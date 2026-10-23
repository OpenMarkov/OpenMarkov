/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.decisiontree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import networks.Networks;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.exception.NonProjectablePotentialException.NumericVariableNeedsAFinding;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.decisiontree.DecisionModelEvaluation;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.DecisionTreeVisualNode;
import org.openmarkov.core.model.decisiontree.EvaluationType;
import org.openmarkov.core.model.decisiontree.ProbAndCEP;
import org.openmarkov.core.model.decisiontree.ProbAndUtilityAndStrategy;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

	/** The example networks of F. J. Díez's course, as he sent them on 2026-10-16. */
	private static final String EXAMPLES = "networks/decision-tree-examples/";

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
		assertSameTree(net, reference(net, depth), actual(net, depth));
	}

	/**
	 * Networks saved for a unicriterion analysis but with a cost and an effectiveness criterion,
	 * analysed for cost-effectiveness, as the user can choose in the window. The scenario of
	 * each impossible branch, the result of a test that is not done, has the partition of
	 * probability zero, on which the new algorithm failed until fc8ee0c.
	 */
	@ParameterizedTest(name = "{0} @ profundidad {1}, en coste-efectividad")
	@CsvSource({
			// red, profundidad
			"networks/id/ID-decide-test.pgmx, 4",
			"networks/id/ID-decide-test.pgmx, 6",
			"networks/id/ID-CEA-test-2therapies.pgmx, 4",
			"networks/id/ID-CEA-test-2therapies-new-test.pgmx, 2",
			"networks/id/ID-CEA-test-2therapies-new-test.pgmx, 4",
	})
	void matchesReferenceInCostEffectiveness(String resource, int depth) throws Exception {
		ProbNet net = load(resource);
		net.getInferenceOptions().getMultiCriteriaOptions()
				.setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);
		assertSameTree(net, reference(net, depth), actual(net, depth));
	}

	/** The example networks of F. J. Díez's course, at every depth up to the whole tree. */
	@ParameterizedTest(name = "{0} @ profundidad {1}")
	@MethodSource("exampleNetworksAndDepths")
	void matchesReferenceOnTheExampleNetworks(String resource, int depth) throws Exception {
		ProbNet net = load(resource);
		assertSameTree(net, reference(net, depth), actual(net, depth));
	}

	/** The two mediastinet networks are too large for a deeper tree. */
	private static Stream<Arguments> exampleNetworksAndDepths() {
		return Stream.of("03.1-ID-perfect-information.pgmx", "03.2-ID-blind-decision.pgmx",
						"03.3-ID-indirect-evidence.pgmx", "03.4-ID-decide-test.pgmx", "03.5-DAN-decide-two-tests.pgmx",
						"04.1-ID-CEA-disease-absent.pgmx", "04.2-ID-CEA-disease-present.pgmx",
						"04.3-ID-CEA-disease-known.pgmx", "04.4-ID-CEA-disease-uncertain.pgmx",
						"04.5-ID-CEA-one-test.pgmx", "04.6-ID-CEA-two-tests.pgmx", "04.7-DAN-CEA-two-tests.pgmx",
						"04.8-ID-mediastinet.pgmx", "04.9-DAN-mediastinet.pgmx")
				.flatMap(name -> IntStream.rangeClosed(0, name.contains("mediastinet") ? 2 : 6)
						.mapToObj(depth -> Arguments.of(EXAMPLES + name, depth)));
	}

	/**
	 * Values computed by hand, which do not depend on the old algorithm. The disease is absent with
	 * probability 0.86; its utility is 10 if absent and 3 if present without therapy, 9 and 8 with it.
	 * Deciding blindly, no therapy gives 0.86·10 + 0.14·3 = 9.02 and therapy 0.86·9 + 0.14·8 = 8.86.
	 */
	@Test
	void theBlindDecisionHasTheValueComputedByHand() throws Exception {
		DecisionTreeNode root = actual(load(EXAMPLES + "03.2-ID-blind-decision.pgmx"), 6);

		assertThat(utility(root)).isCloseTo(9.02, MONEY);
		assertThat(utility(child(root, "no"))).isCloseTo(9.02, MONEY);
		assertThat(utility(child(root, "yes"))).isCloseTo(8.86, MONEY);
		assertThat(root.isBestDecision(branch(root, "no"))).as("no tratar es la mejor decisión").isTrue();
		assertThat(root.isBestDecision(branch(root, "yes"))).isFalse();
	}

	/**
	 * Knowing the disease before deciding, the best is no therapy if it is absent (10) and therapy if
	 * it is present (8), so the decision is worth 0.86·10 + 0.14·8 = 9.72.
	 */
	@Test
	void thePerfectInformationHasTheValueComputedByHand() throws Exception {
		DecisionTreeNode root = actual(load(EXAMPLES + "03.1-ID-perfect-information.pgmx"), 6);
		DecisionTreeNode ifAbsent = child(root, "absent");
		DecisionTreeNode ifPresent = child(root, "present");

		assertThat(utility(root)).isCloseTo(9.72, MONEY);
		assertThat(utility(ifAbsent)).isCloseTo(10, MONEY);
		assertThat(utility(ifPresent)).isCloseTo(8, MONEY);
		assertThat(ifAbsent.isBestDecision(branch(ifAbsent, "no"))).as("sin enfermedad, no tratar").isTrue();
		assertThat(ifPresent.isBestDecision(branch(ifPresent, "yes"))).as("con enfermedad, tratar").isTrue();
	}

	private static double utility(DecisionTreeNode node) {
		return ((ProbAndUtilityAndStrategy) node.getEvaluation()).getUtility();
	}

	private static DecisionTreeBranch branch(DecisionTreeNode node, String state) {
		DecisionTreeBranch branch = branchesByState(node).get(state);
		assertThat(branch).as("rama %s de %s", state, node.getVariable().getName()).isNotNull();
		return branch;
	}

	private static DecisionTreeNode child(DecisionTreeNode node, String state) {
		return branch(node, state).getChild();
	}

	/**
	 * In this example network the utility node «Cost of test Z» has a uniform potential, so both
	 * algorithms refuse it naming that node.
	 */
	@Test
	void anExampleNetworkWithAUniformUtilityIsRefusedByBothAlgorithms() throws Exception {
		ProbNet net = load(EXAMPLES + "03.4-ID-decide-two-tests.pgmx");

		NumericVariableNeedsAFinding byTheOldOne = catchThrowableOfType(NumericVariableNeedsAFinding.class,
				() -> reference(net, 6));
		NumericVariableNeedsAFinding byTheNewOne = catchThrowableOfType(NumericVariableNeedsAFinding.class,
				() -> actual(net, 6));

		assertThat(byTheOldOne).as("aviso del algoritmo antiguo").isNotNull();
		assertThat(byTheNewOne).as("aviso del algoritmo nuevo").isNotNull();
		assertThat(byTheOldOne.variable.getName()).isEqualTo("Cost of test Z");
		assertThat(byTheNewOne.variable.getName()).isEqualTo("Cost of test Z");
	}

	/**
	 * A large asymmetric network, at the depth the plan uses for it. Besides giving the same
	 * tree, the new algorithm must not be slower than twice the old one: evaluating each leaf
	 * by expanding it level by level, as the pseudocode says literally, made it three and a
	 * half times slower here (28 s against 8 s). Its leaves are now decomposed into symmetric
	 * models, as the old algorithm does, and it takes about three quarters of its time. The
	 * old one runs first, in a colder virtual machine, so the comparison does not favour it.
	 */
	@Test
	void aLargeNetworkGivesTheSameTreeAboutAsFastAsTheOldAlgorithm() throws Exception {
		URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith("/dan/DAN-mediastinet.pgmx"))
				.findFirst().orElseThrow();
		ProbNet net = new PGMXReader().read(url).probNet();
		long start = System.nanoTime();
		DecisionTreeNode reference = reference(net, 2);
		long old = System.nanoTime() - start;
		start = System.nanoTime();
		DecisionTreeNode actual = actual(net, 2);
		long now = System.nanoTime() - start;

		assertSameTree(net, reference, actual);
		assertThat(now).as("tiempo del algoritmo nuevo (%d ms) frente al antiguo (%d ms)", now / 1_000_000,
				old / 1_000_000).isLessThan(2 * old);
	}

	private static boolean isCEA(ProbNet net) {
		return net.getInferenceOptions().getMultiCriteriaOptions()
				.getMulticriteriaType() != MulticriteriaOptions.Type.UNICRITERION;
	}

	/** The tree of the old algorithm, the one the window uses. */
	private static DecisionTreeNode reference(ProbNet net, int depth) throws Exception {
		boolean isID = net.getNetworkType() instanceof InfluenceDiagramType;
		return (isID
				? new IDDecisionTreeInference(net.copy(), depth, true, isCEA(net))
				: new DANDecisionTreeInference(net.copy(), depth, true, isCEA(net))).getDecisionTree();
	}

	private static DecisionTreeNode actual(ProbNet net, int depth) throws Exception {
		return new DecisionTreeExpansion(isCEA(net) ? EvaluationType.CE : EvaluationType.UNICRITERION)
				.expandTree(net.copy(), new EvidenceCase(), depth);
	}

	private static void assertSameTree(ProbNet net, DecisionTreeNode reference, DecisionTreeNode actual) {
		assertThat(countNodes(actual)).as("número de nodos").isEqualTo(countNodes(reference));

		SoftAssertions soft = new SoftAssertions();
		assertSameTree(soft, reference, actual, isCEA(net), "raíz");
		soft.assertAll();
	}

	// ------------------------------------------------------------------

	/**
	 * Asserts that the two trees agree at this node and, recursively, at every
	 * branch below it. Children are matched by the state of their branch instead
	 * of by position, so that a difference in the order of the branches is
	 * reported as such and does not make every node below look wrong.
	 */
	private static void assertSameTree(SoftAssertions soft, DecisionTreeNode reference, DecisionTreeNode actual,
			boolean isCEA, String path) {
		soft.assertThat(actual.getVariable().getName()).as("variable en %s", path)
				.isEqualTo(reference.getVariable().getName());
		soft.assertThat(actual.getNodeType()).as("tipo de nodo en %s", path).isEqualTo(reference.getNodeType());
		soft.assertThat(actual.getScenarioProbability()).as("probabilidad del escenario en %s", path)
				.isCloseTo(reference.getScenarioProbability(), RATE);
		assertSameUtility(soft, reference.getEvaluation(), actual.getEvaluation(), isCEA, path);

		Map<String, DecisionTreeBranch> referenceBranches = branchesByState(reference);
		Map<String, DecisionTreeBranch> actualBranches = branchesByState(actual);
		soft.assertThat(actualBranches.keySet()).as("ramas de %s", path)
				.containsExactlyElementsOf(referenceBranches.keySet());

		for (Map.Entry<String, DecisionTreeBranch> entry : referenceBranches.entrySet()) {
			DecisionTreeBranch referenceBranch = entry.getValue();
			DecisionTreeBranch actualBranch = actualBranches.get(entry.getKey());
			if (actualBranch == null) {
				continue;
			}
			String branchPath = path + " → " + entry.getKey();
			// The probability of the scenario of the branch is the one of its child, compared below
			soft.assertThat(actualBranch.getBranchProbability()).as("probabilidad de la rama %s", branchPath)
					.isCloseTo(referenceBranch.getBranchProbability(), RATE);
			if (referenceBranch.getChild() != null && actualBranch.getChild() != null) {
				assertSameTree(soft, referenceBranch.getChild(), actualBranch.getChild(), isCEA, branchPath);
			}
		}
	}

	private static void assertSameUtility(SoftAssertions soft, DecisionModelEvaluation reference,
			DecisionModelEvaluation actual, boolean isCEA, String path) {
		if (isCEA) {
			CEP referenceCep = ((ProbAndCEP) reference).getCep();
			CEP actualCep = ((ProbAndCEP) actual).getCep();
			if (referenceCep.isZero() || actualCep.isZero()) {
				// The scenario of an impossible branch has no intervals to compare
				soft.assertThat(actualCep.isZero()).as("partición de probabilidad cero en %s", path)
						.isEqualTo(referenceCep.isZero());
				return;
			}
			assertSameCostEffectiveness(soft, referenceCep, actualCep, path);
			soft.assertThat(adjacentEqualPartitions(actualCep))
					.as("el CEP nuevo no debe tener particiones redundantes en %s", path).isZero();
		} else {
			soft.assertThat(((ProbAndUtilityAndStrategy) actual).getUtility()).as("utilidad en %s", path)
					.isCloseTo(((ProbAndUtilityAndStrategy) reference).getUtility(), MONEY);
		}
	}

	private static Map<String, DecisionTreeBranch> branchesByState(DecisionTreeNode node) {
		Map<String, DecisionTreeBranch> branches = new LinkedHashMap<>();
		for (DecisionTreeVisualNode child : node.getChildren()) {
			if (child instanceof DecisionTreeBranch branch) {
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

	private static int countNodes(DecisionTreeVisualNode element) {
		if (element == null) {
			return 0;
		}
		if (element instanceof DecisionTreeNode node) {
			int n = 1;
			for (DecisionTreeVisualNode child : node.getChildren()) {
				n += countNodes(child);
			}
			return n;
		}
		if (element instanceof DecisionTreeBranch branch) {
			return countNodes(branch.getChild());
		}
		return 0;
	}
}
