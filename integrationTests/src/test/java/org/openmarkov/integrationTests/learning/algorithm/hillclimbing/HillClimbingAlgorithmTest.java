/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.learning.algorithm.hillclimbing;

import org.junit.jupiter.api.*;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.algorithm.hillclimbing.HillClimbingAlgorithm;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.core.util.ModelNetUse;
import org.openmarkov.learning.metric.bayesian.BayesianMetric;

import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class HillClimbingAlgorithmTest {

	private final double maxError = 1E-6;
	private String dbFilename = "/learnTestDataBase.dbc";
	private double alpha = 0.5;

	private HillClimbingAlgorithm learningAlgorithm;
	private ProbNet learned;
	private Metric metric;

    @Disabled
	@BeforeEach public void setUp() {

		//TODO Commented and making CaseDatabase = null until fixing Elvira database parser with antlr4
		//ElviraDataBaseIO databaseIO = new ElviraDataBaseIO();
		//CaseDatabase database = databaseIO.load(getClass().getResource(dbFilename).getFile());
		CaseDatabase database = null;
		learned = new ProbNet();
		for (Variable variable : database.getVariables()) {
			learned.addNode(variable, NodeType.CHANCE);
		}

		metric = new BayesianMetric(alpha);

		learningAlgorithm = new HillClimbingAlgorithm(learned, database, alpha, metric);
	}
    @Disabled
    @Test
    public void testLearning() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, CannotNormalizePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		double[] probabilities;
		learningAlgorithm.run(new ModelNetUse());
		Node nodeA = learned.getNode("A");
		Node nodeB = learned.getNode("B");
		Node nodeC = learned.getNode("C");
		Node nodeD = learned.getNode("D");
		Node nodeE = learned.getNode("E");
		Node nodeF = learned.getNode("F");
		Variable variableA = nodeA.getVariable();
		Variable variableB = nodeB.getVariable();
		Variable variableC = nodeC.getVariable();
		Variable variableD = nodeD.getVariable();
		Variable variableE = nodeE.getVariable();
		Variable variableF = nodeF.getVariable();

		// check the structure of the learned net
		// present links
		Assertions.assertTrue(nodeA.isParent(nodeB));
		Assertions.assertTrue(nodeC.isParent(nodeB));
		Assertions.assertTrue(nodeC.isParent(nodeA));
		Assertions.assertTrue(nodeA.isParent(nodeD));
		Assertions.assertTrue(nodeC.isParent(nodeD));
		Assertions.assertTrue(nodeD.isParent(nodeE));
		// non-present links
		Assertions.assertFalse(nodeB.isParent(nodeA));
		Assertions.assertFalse(nodeA.isParent(nodeC));
		Assertions.assertFalse(nodeD.isParent(nodeA));
		Assertions.assertFalse(nodeE.isParent(nodeA));
		Assertions.assertFalse(nodeF.isParent(nodeA));
		Assertions.assertFalse(nodeD.isParent(nodeB));
		Assertions.assertFalse(nodeE.isParent(nodeB));
		Assertions.assertFalse(nodeF.isParent(nodeB));
		Assertions.assertFalse(nodeB.isParent(nodeC));
		Assertions.assertFalse(nodeD.isParent(nodeC));
		Assertions.assertFalse(nodeE.isParent(nodeC));
		Assertions.assertFalse(nodeF.isParent(nodeC));
		Assertions.assertFalse(nodeB.isParent(nodeD));
		Assertions.assertFalse(nodeE.isParent(nodeD));
		Assertions.assertFalse(nodeF.isParent(nodeD));
		Assertions.assertFalse(nodeA.isParent(nodeE));
		Assertions.assertFalse(nodeB.isParent(nodeE));
		Assertions.assertFalse(nodeC.isParent(nodeE));
		Assertions.assertFalse(nodeF.isParent(nodeE));
		Assertions.assertFalse(nodeA.isParent(nodeF));
		Assertions.assertFalse(nodeB.isParent(nodeF));
		Assertions.assertFalse(nodeC.isParent(nodeF));
		Assertions.assertFalse(nodeD.isParent(nodeF));
		Assertions.assertFalse(nodeE.isParent(nodeF));
		// chek the CPTs
		TablePotential aGivenBDPotential = (TablePotential) nodeA.getPotentials().get(0);
		List<Variable> aGivenBDVariables = Arrays.asList(variableA, variableB, variableD);
		aGivenBDPotential = (TablePotential) aGivenBDPotential.reorder(aGivenBDVariables);
		probabilities = aGivenBDPotential.getValues();
		Assertions.assertEquals(0.022135, probabilities[0], maxError);
		Assertions.assertEquals(0.977864, probabilities[1], maxError);
		Assertions.assertEquals(0.316326, probabilities[2], maxError);
		Assertions.assertEquals(0.683673, probabilities[3], maxError);
		Assertions.assertEquals(0.776018, probabilities[4], maxError);
		Assertions.assertEquals(0.223981, probabilities[5], maxError);
		Assertions.assertEquals(0.321428, probabilities[6], maxError);
		Assertions.assertEquals(0.678571, probabilities[7], maxError);
		probabilities = ((TablePotential) nodeB.getPotentials().get(0)).getValues();
		Assertions.assertEquals(0.602897, probabilities[0], maxError);
		Assertions.assertEquals(0.397102, probabilities[1], maxError);
		TablePotential cGivenBDAPotential = (TablePotential) nodeC.getPotentials().get(0);
		List<Variable> cGivenBDAVariables = Arrays.asList(variableC, variableB, variableD, variableA);
		cGivenBDAPotential = (TablePotential) cGivenBDAPotential.reorder(cGivenBDAVariables);
		probabilities = cGivenBDAPotential.getValues();
		Assertions.assertEquals(0.277777, probabilities[0], maxError);
		Assertions.assertEquals(0.722222, probabilities[1], maxError);
		Assertions.assertEquals(0.698717, probabilities[2], maxError);
		Assertions.assertEquals(0.301282, probabilities[3], maxError);
		Assertions.assertEquals(0.287790, probabilities[4], maxError);
		Assertions.assertEquals(0.712209, probabilities[5], maxError);
		Assertions.assertEquals(0.19, probabilities[6], maxError);
		Assertions.assertEquals(0.81, probabilities[7], maxError);
		Assertions.assertEquals(0.780585, probabilities[8], maxError);
		Assertions.assertEquals(0.219414, probabilities[9], maxError);
		Assertions.assertEquals(0.205357, probabilities[10], maxError);
		Assertions.assertEquals(0.794642, probabilities[11], maxError);
		Assertions.assertEquals(0.87, probabilities[12], maxError);
		Assertions.assertEquals(0.13, probabilities[13], maxError);
		Assertions.assertEquals(0.719047, probabilities[14], maxError);
		Assertions.assertEquals(0.280952, probabilities[15], maxError);
		probabilities = ((TablePotential) nodeD.getPotentials().get(0)).getValues();
		Assertions.assertEquals(0.177685, probabilities[0], maxError);
		Assertions.assertEquals(0.822314, probabilities[1], maxError);
		Assertions.assertEquals(0.881846, probabilities[2], maxError);
		Assertions.assertEquals(0.118153, probabilities[3], maxError);
		probabilities = ((TablePotential) nodeE.getPotentials().get(0)).getValues();
		Assertions.assertEquals(0.362137, probabilities[0], maxError);
		Assertions.assertEquals(0.637862, probabilities[1], maxError);
		probabilities = ((TablePotential) nodeF.getPotentials().get(0)).getValues();
		Assertions.assertEquals(0.500999, probabilities[0], maxError);
		Assertions.assertEquals(0.499000, probabilities[1], maxError);
	}
}
