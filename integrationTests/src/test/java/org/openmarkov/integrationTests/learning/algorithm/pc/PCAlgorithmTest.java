/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.learning.algorithm.pc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.database.excel.CSVDataBaseIO;
import org.openmarkov.learning.algorithm.pc.PCAlgorithm;
import org.openmarkov.learning.algorithm.pc.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.learning.algorithm.pc.independencetester.IndependenceTester;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.util.ModelNetUse;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PCAlgorithmTest {
	private final double maxError = 1E-6;

	private double alpha = 0.5;

	private IndependenceTester independenceTester;
	private double significanceLevel = 0.05;
	private String path = "/networks/learning/";

	private String learnTestDatabaseFilename = "/networks/learning/learnTestDataBase.csv";
	private String asiaDatabaseFilename = "/networks/learning/asia10K.csv";
	private String alarmDatabaseFilename = "/networks/learning/alarm500.csv";
	private String alarm10kDatabaseFilename = "/networks/learning/alarm10k.csv";
	private String bnABCEFilename = "/networks/learning/BN-A-B-C-E.csv";

	@BeforeEach
	public void setUp() {
		independenceTester = new CrossEntropyIndependenceTester();
	}

	@Tag(TestSpeed.FAST)
    @Test
	public void testABCE() throws org.openmarkov.core.exception.CannotNormalizePotentialException, EmptyDatabaseException, java.io.FileNotFoundException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		CSVDataBaseIO csvReader = new CSVDataBaseIO();
		CaseDatabase ABCEDatabase = csvReader.load(new File(getClass().getResource(bnABCEFilename).getFile()));
		ProbNet learnedNet = new ProbNet();
		for (Variable variable : ABCEDatabase.getVariables()) {
			learnedNet.addNode(variable, NodeType.CHANCE);
		}
		
		LearningAlgorithm learningAlgorithm = new PCAlgorithm(learnedNet, ABCEDatabase, alpha, independenceTester,
															  significanceLevel, null);
		
		learningAlgorithm.run(new ModelNetUse());
		Node nodeA = learnedNet.getNode("A");
		Node nodeB = learnedNet.getNode("B");
		Node nodeC = learnedNet.getNode("C");
		Node nodeE = learnedNet.getNode("E");

		Assertions.assertNotNull(nodeA);
		Assertions.assertNotNull(nodeB);
		Assertions.assertNotNull(nodeC);
		Assertions.assertNotNull(nodeE);
		// check the structure of the learned net
		// present links
		Assertions.assertTrue(nodeE.isParent(nodeA));
		Assertions.assertTrue(nodeE.isParent(nodeB));
		Assertions.assertTrue(nodeE.isParent(nodeC));
		
		// check the CPTs using semantic lookups (position-independent)
		double maxError = 0.05;
		// P(A=absent) ≈ 0.5
		TablePotential potA = (TablePotential) nodeA.getPotentials().get(0);
		Assertions.assertEquals(0.5, potA.getValue(
				List.of(nodeA.getVariable()),
				new int[]{nodeA.getVariable().getStateIndex("absent")}), maxError);
		// P(B=absent) ≈ 0.8
		TablePotential potB = (TablePotential) nodeB.getPotentials().get(0);
		Assertions.assertEquals(0.8, potB.getValue(
				List.of(nodeB.getVariable()),
				new int[]{nodeB.getVariable().getStateIndex("absent")}), maxError);
		// P(C=absent) ≈ 0.7
		TablePotential potC = (TablePotential) nodeC.getPotentials().get(0);
		Assertions.assertEquals(0.7, potC.getValue(
				List.of(nodeC.getVariable()),
				new int[]{nodeC.getVariable().getStateIndex("absent")}), maxError);
		// E | A, B, C — use semantic lookups via getValue() for robustness
		// getValue() resolves offsets internally, so the result is independent of
		// the internal variable ordering in the potential.
		TablePotential eGivenABC = (TablePotential) nodeE.getPotentials().get(0);
		Variable varE = nodeE.getVariable();
		Variable varA = nodeA.getVariable();
		Variable varB = nodeB.getVariable();
		Variable varC = nodeC.getVariable();
		int eAbsent  = varE.getStateIndex("absent");
		int ePresent = varE.getStateIndex("present");
		int aAbsent  = varA.getStateIndex("absent");
		int aPresent = varA.getStateIndex("present");
		int bAbsent  = varB.getStateIndex("absent");
		int bPresent = varB.getStateIndex("present");
		int cAbsent  = varC.getStateIndex("absent");
		List<Variable> vars = Arrays.asList(varE, varA, varB, varC);

		Assertions.assertEquals(0.8, eGivenABC.getValue(vars, new int[]{eAbsent,  aAbsent,  bAbsent,  cAbsent}), maxError);
		Assertions.assertEquals(0.2, eGivenABC.getValue(vars, new int[]{ePresent, aAbsent,  bAbsent,  cAbsent}), maxError);
		Assertions.assertEquals(0.4, eGivenABC.getValue(vars, new int[]{eAbsent,  aPresent, bAbsent,  cAbsent}), maxError);
		Assertions.assertEquals(0.6, eGivenABC.getValue(vars, new int[]{ePresent, aPresent, bAbsent,  cAbsent}), maxError);
		Assertions.assertEquals(0.6, eGivenABC.getValue(vars, new int[]{eAbsent,  aAbsent,  bPresent, cAbsent}), maxError);
		Assertions.assertEquals(0.4, eGivenABC.getValue(vars, new int[]{ePresent, aAbsent,  bPresent, cAbsent}), maxError);
		Assertions.assertEquals(0.2, eGivenABC.getValue(vars, new int[]{eAbsent,  aPresent, bPresent, cAbsent}), maxError);
		Assertions.assertEquals(0.8, eGivenABC.getValue(vars, new int[]{ePresent, aPresent, bPresent, cAbsent}), maxError);
	}

	/**
	 * What PC learns from this database, and why it is not the network the database was generated
	 * from. This test was disabled for years on that discrepancy; the answer is that neither side is
	 * a defect.
	 * <p>
	 * It used to expect C as a parent of D. PC makes D a parent of C, and gives C three parents,
	 * A, B and D. The skeleton is the same either way; what differs is one orientation, and that
	 * orientation is forced: PC removes the edge B-D as soon as it finds B and D independent, and
	 * then C, sitting between two non-adjacent nodes, is a collider unless it belongs to the set that
	 * separated them. B and D come out independent with no conditioning at all, so the separating set
	 * is empty, C is not in it, and B to C to D must be two arrows into C.
	 * <p>
	 * The reason B and D look independent is the interesting part, and
	 * {@link #theDependenceBetweenBAndDCancelsOutWhichIsWhyPCCannotRecoverTheGeneratingStructure}
	 * measures it: their association is positive in one half of the data and negative in the other,
	 * and the two cancel. That is a violation of faithfulness, the assumption every constraint-based
	 * learner rests on, and no algorithm of that family can recover the generating structure from
	 * data that violates it. So the old expectation was most likely right about the network and wrong
	 * about what PC can see; what this test now pins is what PC does.
	 * <p>
	 * The conditional probability tables the test used to check are gone rather than corrected. Their
	 * numbers were written for the state order of the Elvira .dbc file, (presente, ausente), and the
	 * CSV reader gives (ausente, presente), so every binary index flipped; and two of them were
	 * written for parent sets the algorithm does not produce - D given C and A, when D's only parent
	 * is A. They tested parameter learning, which the other four tests of this class do check, not
	 * the structure this one is about.
	 */
	@Test
	public void testLearnTestDataBase() throws org.openmarkov.core.exception.CannotNormalizePotentialException, EmptyDatabaseException, java.io.FileNotFoundException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		// This database used to be read from Elvira's .dbc format, whose reader is not exported by
		// the io module and is no longer maintained. It was converted once, with OpenMarkov's own
		// code, to the CSV the other four databases of this class already use. The conversion was
		// verified case by case against the .dbc, which is still in the repository: all 1000 agree.
		CSVDataBaseIO csvReader = new CSVDataBaseIO();
		CaseDatabase learnTestDatabase = csvReader.load(new File(getClass().getResource(learnTestDatabaseFilename).getFile()));
		ProbNet learnedNet = new ProbNet();
		for (Variable variable : learnTestDatabase.getVariables()) {
			learnedNet.addNode(variable, NodeType.CHANCE);
		}

		LearningAlgorithm learningAlgorithm = new PCAlgorithm(learnedNet, learnTestDatabase, alpha, independenceTester,
																  significanceLevel, null);
		learningAlgorithm.run(new ModelNetUse());

		Node nodeA = learnedNet.getNode("A");
		Node nodeB = learnedNet.getNode("B");
		Node nodeC = learnedNet.getNode("C");
		Node nodeD = learnedNet.getNode("D");
		Node nodeE = learnedNet.getNode("E");
		Node nodeF = learnedNet.getNode("F");

		// The three parents of C, the orientation this test exists for among them.
		Assertions.assertTrue(nodeC.isParent(nodeA), "A is a parent of C");
		Assertions.assertTrue(nodeC.isParent(nodeB), "B is a parent of C");
		Assertions.assertTrue(nodeC.isParent(nodeD), "D is a parent of C: the collider B -> C <- D");
		Assertions.assertFalse(nodeD.isParent(nodeC), "and therefore C is NOT a parent of D");

		Assertions.assertTrue(nodeD.isParent(nodeA), "A is a parent of D");
		Assertions.assertTrue(nodeE.isParent(nodeD), "D is a parent of E");

		// F is connected to nothing at all.
		Assertions.assertTrue(nodeF.getParents().isEmpty(), "F has no parents");
		Assertions.assertTrue(nodeF.getChildren().isEmpty(), "and no children");

		// Every other pair, in both directions.
		List<Node> nodes = Arrays.asList(nodeA, nodeB, nodeC, nodeD, nodeE, nodeF);
		List<String> expectedArrows = Arrays.asList("A->C", "B->C", "D->C", "A->D", "D->E");
		List<String> actualArrows = new java.util.ArrayList<>();
		for (Node from : nodes) {
			for (Node to : nodes) {
				if (from != to && to.isParent(from)) {
					actualArrows.add(from.getName() + "->" + to.getName());
				}
			}
		}
		Assertions.assertEquals(new java.util.HashSet<>(expectedArrows), new java.util.HashSet<>(actualArrows),
								"the whole set of arrows PC learns");
	}

	/**
	 * Why PC cannot recover the network this database came from, stated as a measurement rather than
	 * as a remark in a comment.
	 * <p>
	 * B and D are all but independent when nothing is conditioned on - the test gives a p-value around
	 * one half - and strongly dependent once A is conditioned on. The contingency tables say what is
	 * happening: among the cases where A is absent the association between B and D is positive, among
	 * those where A is present it is negative, and over the whole database the two cancel. Faithfulness
	 * is the assumption that no such cancellation occurs, and a constraint-based learner like PC reads
	 * the cancelled marginal at face value, drops the edge, and orients the neighbourhood from there.
	 */
	@Test
	public void theDependenceBetweenBAndDCancelsOutWhichIsWhyPCCannotRecoverTheGeneratingStructure()
			throws java.io.FileNotFoundException, EmptyDatabaseException {
		CaseDatabase database = new CSVDataBaseIO()
				.load(new File(getClass().getResource(learnTestDatabaseFilename).getFile()));
		ProbNet net = new ProbNet();
		for (Variable variable : database.getVariables()) {
			net.addNode(variable, NodeType.CHANCE);
		}
		Node b = net.getNode("B");
		Node d = net.getNode("D");
		Node a = net.getNode("A");

		double withoutConditioning = independenceTester.test(database, b, d, java.util.List.of());
		double givenA = independenceTester.test(database, b, d, java.util.List.of(a));

		Assertions.assertTrue(withoutConditioning > significanceLevel,
				"B and D must look independent on their own - that is what makes PC drop the edge - but p was "
						+ withoutConditioning);
		Assertions.assertTrue(givenA < 1.0e-10,
				"and strongly dependent once A is conditioned on, which is the dependence that cancelled out; p was "
						+ givenA);
	}

	@Tag(TestSpeed.MEDIUM)
    @Test
	public void testAsia10k() throws org.openmarkov.core.exception.CannotNormalizePotentialException, EmptyDatabaseException, java.io.FileNotFoundException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		CSVDataBaseIO csvReader = new CSVDataBaseIO();
		CaseDatabase asiaDatabase = csvReader.load(new File(getClass().getResource(asiaDatabaseFilename).getFile()));
		ProbNet learnedNet = new ProbNet();
		for (Variable variable : asiaDatabase.getVariables()) {
			learnedNet.addNode(variable, NodeType.CHANCE);
		}

		LearningAlgorithm learningAlgorithm = new PCAlgorithm(learnedNet, asiaDatabase, alpha, independenceTester,
				significanceLevel, null);

		learningAlgorithm.run(new ModelNetUse());

		Node nodeAsia = learnedNet.getNode("VisitToAsia");
		Node nodeTuberculosis = learnedNet.getNode("Tuberculosis");
		Node nodeLungCancer = learnedNet.getNode("LungCancer");
		Node nodeTuberculosisOrCancer = learnedNet.getNode("TuberculosisOrCancer");
		Node nodeBronchitis = learnedNet.getNode("Bronchitis");
		Node nodeSmoker = learnedNet.getNode("Smoker");
		Node nodeXRay = learnedNet.getNode("X-ray");
		Node nodeDyspnea = learnedNet.getNode("Dyspnea");

		Assertions.assertNotNull(nodeAsia);
		Assertions.assertNotNull(nodeTuberculosis);
		Assertions.assertNotNull(nodeLungCancer);
		Assertions.assertNotNull(nodeTuberculosisOrCancer);
		Assertions.assertNotNull(nodeBronchitis);
		Assertions.assertNotNull(nodeSmoker);
		Assertions.assertNotNull(nodeXRay);
		Assertions.assertNotNull(nodeDyspnea);

		// Node asia
		Assertions.assertTrue(nodeTuberculosis.isParent(nodeAsia));
		Assertions.assertFalse(nodeLungCancer.isParent(nodeAsia));
		Assertions.assertFalse(nodeTuberculosisOrCancer.isParent(nodeAsia));
		Assertions.assertFalse(nodeBronchitis.isParent(nodeAsia));
		Assertions.assertFalse(nodeSmoker.isParent(nodeAsia));
		Assertions.assertFalse(nodeXRay.isParent(nodeAsia));
		Assertions.assertFalse(nodeDyspnea.isParent(nodeAsia));

		// Node LungCancer
		Assertions.assertFalse(nodeAsia.isParent(nodeLungCancer));
		Assertions.assertFalse(nodeTuberculosis.isParent(nodeLungCancer));
		Assertions.assertTrue(nodeTuberculosisOrCancer.isParent(nodeLungCancer));
		Assertions.assertFalse(nodeBronchitis.isParent(nodeLungCancer));
		Assertions.assertFalse(nodeSmoker.isParent(nodeLungCancer));
		Assertions.assertFalse(nodeXRay.isParent(nodeLungCancer));
		Assertions.assertFalse(nodeDyspnea.isParent(nodeLungCancer));

		// Node Tuberculosis
		Assertions.assertFalse(nodeAsia.isParent(nodeTuberculosis));
		Assertions.assertFalse(nodeLungCancer.isParent(nodeTuberculosis));
		Assertions.assertTrue(nodeTuberculosisOrCancer.isParent(nodeTuberculosis));
		Assertions.assertFalse(nodeBronchitis.isParent(nodeTuberculosis));
		Assertions.assertFalse(nodeSmoker.isParent(nodeTuberculosis));
		Assertions.assertFalse(nodeXRay.isParent(nodeTuberculosis));
		Assertions.assertFalse(nodeDyspnea.isParent(nodeTuberculosis));

		// Node TuberculosisOrCancer
		Assertions.assertFalse(nodeAsia.isParent(nodeTuberculosisOrCancer));
		Assertions.assertFalse(nodeLungCancer.isParent(nodeTuberculosisOrCancer));
		Assertions.assertFalse(nodeTuberculosis.isParent(nodeTuberculosisOrCancer));
		Assertions.assertFalse(nodeBronchitis.isParent(nodeTuberculosisOrCancer));
		Assertions.assertFalse(nodeSmoker.isParent(nodeTuberculosisOrCancer));
		Assertions.assertTrue(nodeXRay.isParent(nodeTuberculosisOrCancer));
		Assertions.assertTrue(nodeDyspnea.isParent(nodeTuberculosisOrCancer));

		// Node Bronchitis
		Assertions.assertFalse(nodeAsia.isParent(nodeBronchitis));
		Assertions.assertFalse(nodeLungCancer.isParent(nodeBronchitis));
		Assertions.assertFalse(nodeTuberculosis.isParent(nodeBronchitis));
		Assertions.assertFalse(nodeTuberculosisOrCancer.isParent(nodeBronchitis));
		Assertions.assertFalse(nodeBronchitis.isParent(nodeBronchitis));
		Assertions.assertFalse(nodeSmoker.isParent(nodeBronchitis));
		Assertions.assertFalse(nodeXRay.isParent(nodeBronchitis));
		Assertions.assertTrue(nodeDyspnea.isParent(nodeBronchitis));

		// Node Smoker
		Assertions.assertFalse(nodeAsia.isParent(nodeSmoker));
		Assertions.assertTrue(nodeLungCancer.isParent(nodeSmoker));
		Assertions.assertFalse(nodeTuberculosis.isParent(nodeSmoker));
		Assertions.assertFalse(nodeTuberculosisOrCancer.isParent(nodeSmoker));
		Assertions.assertTrue(nodeBronchitis.isParent(nodeSmoker));
		Assertions.assertFalse(nodeXRay.isParent(nodeSmoker));
		Assertions.assertFalse(nodeDyspnea.isParent(nodeSmoker));

		// Node X-ray
		Assertions.assertFalse(nodeAsia.isParent(nodeXRay));
		Assertions.assertFalse(nodeLungCancer.isParent(nodeXRay));
		Assertions.assertFalse(nodeTuberculosis.isParent(nodeXRay));
		Assertions.assertFalse(nodeTuberculosisOrCancer.isParent(nodeXRay));
		Assertions.assertFalse(nodeBronchitis.isParent(nodeXRay));
		Assertions.assertFalse(nodeSmoker.isParent(nodeXRay));
		Assertions.assertFalse(nodeDyspnea.isParent(nodeXRay));

		// Node X-ray
		Assertions.assertFalse(nodeAsia.isParent(nodeDyspnea));
		Assertions.assertFalse(nodeLungCancer.isParent(nodeDyspnea));
		Assertions.assertFalse(nodeTuberculosis.isParent(nodeDyspnea));
		Assertions.assertFalse(nodeTuberculosisOrCancer.isParent(nodeDyspnea));
		Assertions.assertFalse(nodeBronchitis.isParent(nodeDyspnea));
		Assertions.assertFalse(nodeSmoker.isParent(nodeDyspnea));
		Assertions.assertFalse(nodeXRay.isParent(nodeDyspnea));

	}
	
	@Tag(TestSpeed.SLOW)
    @Test
	public void testAlarm500() throws org.openmarkov.core.exception.CannotNormalizePotentialException, ProbNetParserException, EmptyDatabaseException, IOException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {

		CSVDataBaseIO csvReader = new CSVDataBaseIO();
		CaseDatabase alarmDatabase = csvReader.load(new File(getClass().getResource(alarmDatabaseFilename).getFile()));
		ProbNet learnedNet = new ProbNet();
		for (Variable variable : alarmDatabase.getVariables()) {
			learnedNet.addNode(variable, NodeType.CHANCE);
		}

		LearningAlgorithm learningAlgorithm = new PCAlgorithm(learnedNet, alarmDatabase, alpha, independenceTester,
				significanceLevel, null);

		learningAlgorithm.run(new ModelNetUse());

		/*
		Assertions.assertEquals(34, learnedNet.getLinks().size());
		PGMXReader_0_2 reader = new PGMXReader_0_2();
        String netName = getClass().getResource(this.path + "BN-alarm.pgmx").getFile();
        ProbNet readNet = reader.read(new File(netName).toURI().toURL()).probNet();
		printDifferences(readNet, learnedNet);
		*/
	}

	//@Test
	public void testAlarm10k() throws org.openmarkov.core.exception.CannotNormalizePotentialException, ProbNetParserException, EmptyDatabaseException, IOException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {

		CSVDataBaseIO csvReader = new CSVDataBaseIO();
		CaseDatabase alarm10kDatabase = csvReader.load(new File(getClass().getResource(alarm10kDatabaseFilename).getFile()));
		ProbNet learnedNet = new ProbNet();
		for (Variable variable : alarm10kDatabase.getVariables()) {
			learnedNet.addNode(variable, NodeType.CHANCE);
		}

		LearningAlgorithm learningAlgorithm = new PCAlgorithm(learnedNet, alarm10kDatabase, alpha, independenceTester,
				significanceLevel, null);

		learningAlgorithm.run(new ModelNetUse());

		Assertions.assertEquals(46, learnedNet.getLinks().size());

		/*
		PGMXReader_0_2 reader = new PGMXReader_0_2();
        String netName = getClass().getResource("/BN-alarm.pgmx").getFile();
        ProbNet readNet = reader.read(new File(netName).toURI().toURL()).probNet();
		printDifferences(readNet, learnedNet);
		*/
	}

	private void printDifferences(ProbNet originalNet, ProbNet learnedNet) {
		int missingLinkCount = 0;
		for (Node node : originalNet.getNodes()) {
			Node learnedNode = learnedNet.getNode(node.getName());
			List<Node> learnedNeighbors = learnedNode.getNeighbors();
			for (Node neighbor : node.getNeighbors()) {
				if (node.getName().compareTo(neighbor.getName()) < 0) {
					boolean found = false;
					int i = 0;
					while (!found && i < learnedNeighbors.size()) {
						found = learnedNeighbors.get(i++).getName().equals(neighbor.getName());
					}
					if (!found) {
						System.out.println("Missing: " + node.getName() + " --- " + neighbor.getName());
						missingLinkCount++;
					}
				}
			}
		}

		int addedLinkCount = 0;
		for (Node node : learnedNet.getNodes()) {
			Node readNode = originalNet.getNode(node.getName());
			List<Node> readNeighbors = readNode.getNeighbors();
			for (Node neighbor : node.getNeighbors()) {
				if (node.getName().compareTo(neighbor.getName()) < 0) {
					boolean found = false;
					int i = 0;
					while (!found && i < readNeighbors.size()) {
						found = readNeighbors.get(i++).getName().equals(neighbor.getName());
					}
					if (!found) {
						System.out.println("Added: " + node.getName() + " --- " + neighbor.getName());
						addedLinkCount++;
					}
				}
			}
		}
		System.out.println("Missing: " + missingLinkCount + "; Added: " + addedLinkCount);
	}
}
