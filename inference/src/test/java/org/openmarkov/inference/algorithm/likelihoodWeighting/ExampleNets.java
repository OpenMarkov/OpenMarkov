/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.likelihoodWeighting;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Example nets for JUnit testing purposes.
 *
 * @author ibermejo
 * @author fjdiez
 * @author iagoparis - spring 2018
 * @version 1.1
 */

class ExampleNets {

	public static ArrayList<Variable> variables; // An auxiliary variables list

	/**
	 * A one node bayesian net, meant for testing sampling alone.
	 *
	 * @return itself as a ProbNet
	 */
	public static ProbNet oneNet() {
		ProbNet one = new ProbNet();
		one.setName("One node net");

		variables = new ArrayList<Variable>();
		variables.add(new Variable("A", 2));

		TablePotential aPotential = new TablePotential(PotentialRole.JOINT_PROBABILITY, variables.get(0));
		aPotential.setValues(new double[] { 0.750001, 0.249999 });
		one.addPotential(aPotential);

		return one;
	}

	/**
	 * A linked two node bayesian net.
	 *
	 * @return itself as a ProbNet
	 */
	public static ProbNet twoNet() {
		// Net construction.
		ProbNet two = new ProbNet();
		two.setName("Two node net");

		variables = new ArrayList<Variable>();
		variables.add(new Variable("A", 2));
		variables.add(new Variable("B", 2));
		for (Variable variable : variables) {
			two.addNode(variable, NodeType.CHANCE);
		}

		// Link A->B
		two.addLink(two.getNode("A"), two.getNode("B"), true);

		// Potentials
		TablePotential aPotential = new TablePotential(PotentialRole.JOINT_PROBABILITY, variables.get(0));
		aPotential.setValues(new double[] { 0.5, 0.5 });
		two.addPotential(aPotential);
		TablePotential abPotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(1),
				variables.get(0));
		abPotential.setValues(new double[] { 0.93, 0.07, 0.1, 0.9 });
		two.addPotential(abPotential);

		return two;
	}

	/**
	 * A linked five node bayesian net:
	 * 5 nodes
	 * 5 links
	 *
	 * @return itself as a ProbNet
	 */
	public static ProbNet bigNet() {
		ProbNet testNet = new ProbNet();
		testNet.setName("Five node net");

		variables = new ArrayList<Variable>();
		variables.add(new Variable("A", 2));
		variables.add(new Variable("B", 2));
		variables.add(new Variable("C", 2));
		variables.add(new Variable("D", 2));
		variables.add(new Variable("E", 2));
		for (Variable variable : variables) {
			testNet.addNode(variable, NodeType.CHANCE);
		}

		//Links
		testNet.addLink(testNet.getNode("A"), testNet.getNode("B"), true);
		testNet.addLink(testNet.getNode("A"), testNet.getNode("C"), true);
		testNet.addLink(testNet.getNode("B"), testNet.getNode("D"), true);
		testNet.addLink(testNet.getNode("B"), testNet.getNode("E"), true);
		testNet.addLink(testNet.getNode("C"), testNet.getNode("D"), true);
		// A
		UniformPotential aPotential = new UniformPotential(PotentialRole.JOINT_PROBABILITY, variables.get(0));
		testNet.addPotential(aPotential);
		// A -> B
		TablePotential abPotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(1),
				variables.get(0));
		abPotential.setValues(new double[] { 0.8, 0.2, 0.3, 0.7 }); // States (a,b) -> +a+b, +a-b, -a+b, -a-b
		testNet.addPotential(abPotential);
		// A -> C
		TablePotential acPotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(2),
				variables.get(0));
		acPotential.setValues(new double[] { 0.75, 0.25, 0.4, 0.6 });
		testNet.addPotential(acPotential);
		// B, C -> D
		TablePotential bcdPotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(3),
				variables.get(2), variables.get(1)); // The order of the variables as arguments is:
		// variable of the node,
		// variable of second father and
		// variable of first father (the highest by default in OpenMarkov table).
		// It goes backwards and if you fill an OpenMarkov table through the numbers in the code, you also have to do
		// it downwards then backwards.

		// As an example, the states assigned to the bcd potential are in the following order:
		// States (b,c,d) -> 	+b+c+d, +b+c-d, +b-c+d, +b-c-d, -b+c+d, -b+c-d, -b-c+d, -b-c-d
		bcdPotential.setValues(new double[] {0.4, 0.6, 0.3, 0.7, 0.99, 0.01, 0.9, 0.1 });
		testNet.addPotential(bcdPotential);
		// C -> E
		TablePotential cePotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(4),
				variables.get(2));
		cePotential.setValues(new double[] { 0.75, 0.25, 0.4, 0.6 });
		testNet.addPotential(cePotential);
		return testNet;
	}

	/**
	 * Bayesian net about diseases and Asia:
	 * 8 nodes
	 * 8 links
	 *
	 * @return itself as a ProbNet
	 */
	public static ProbNet buildBN_asia_java() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		probNet.setName("Asia diseases net");

		// Variables
		Variable varX_ray = new Variable("X-ray", "no", "yes");
		Variable varBronchitis = new Variable("Bronchitis", "no", "yes");
		Variable varDyspnea = new Variable("Dyspnea", "no", "yes");
		Variable varVisitToAsia = new Variable("VisitToAsia", "no", "yes");
		Variable varSmoker = new Variable("Smoker", "no", "yes");
		Variable varLungCancer = new Variable("LungCancer", "no", "yes");
		Variable varTuberculosis = new Variable("Tuberculosis", "no", "yes");
		Variable varTuberculosisOrCancer = new Variable("TuberculosisOrCancer", "no", "yes");

		// Nodes
		Node nodeX_ray = probNet.addNode(varX_ray, NodeType.CHANCE);
		Node nodeBronchitis = probNet.addNode(varBronchitis, NodeType.CHANCE);
		Node nodeDyspnea = probNet.addNode(varDyspnea, NodeType.CHANCE);
		Node nodeVisitToAsia = probNet.addNode(varVisitToAsia, NodeType.CHANCE);
		Node nodeSmoker = probNet.addNode(varSmoker, NodeType.CHANCE);
		Node nodeLungCancer = probNet.addNode(varLungCancer, NodeType.CHANCE);
		Node nodeTuberculosis = probNet.addNode(varTuberculosis, NodeType.CHANCE);
		Node nodeTuberculosisOrCancer = probNet.addNode(varTuberculosisOrCancer, NodeType.CHANCE);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeBronchitis, nodeDyspnea, true);
		probNet.addLink(nodeVisitToAsia, nodeTuberculosis, true);
		probNet.addLink(nodeSmoker, nodeBronchitis, true);
		probNet.addLink(nodeSmoker, nodeLungCancer, true);
		probNet.addLink(nodeLungCancer, nodeTuberculosisOrCancer, true);
		probNet.addLink(nodeTuberculosis, nodeTuberculosisOrCancer, true);
		probNet.addLink(nodeTuberculosisOrCancer, nodeDyspnea, true);
		probNet.addLink(nodeTuberculosisOrCancer, nodeX_ray, true);

		// Potentials
		TablePotential potX_ray = new TablePotential(Arrays.asList(varX_ray, varTuberculosisOrCancer),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potX_ray.setValues(new double[] { 0.95, 0.05, 0.02, 0.98 });
		nodeX_ray.setPotential(potX_ray);

		TablePotential potBronchitis = new TablePotential(Arrays.asList(varBronchitis, varSmoker),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potBronchitis.setValues(new double[] { 0.7, 0.3, 0.4, 0.6 });
		nodeBronchitis.setPotential(potBronchitis);

		TablePotential potDyspnea = new TablePotential(
				Arrays.asList(varDyspnea, varTuberculosisOrCancer, varBronchitis),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDyspnea.setValues(new double[] { 0.9, 0.1, 0.3, 0.7, 0.2, 0.8, 0.1, 0.9 });
		nodeDyspnea.setPotential(potDyspnea);

		TablePotential potVisitToAsia = new TablePotential(Arrays.asList(varVisitToAsia),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potVisitToAsia.setValues(new double[] { 0.99, 0.01 });
		nodeVisitToAsia.setPotential(potVisitToAsia);

		TablePotential potSmoker = new TablePotential(Arrays.asList(varSmoker), PotentialRole.CONDITIONAL_PROBABILITY);
		potSmoker.setValues(new double[] { 0.5, 0.5 });
		nodeSmoker.setPotential(potSmoker);

		TablePotential potLungCancer = new TablePotential(Arrays.asList(varLungCancer, varSmoker),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potLungCancer.setValues(new double[] { 0.99, 0.01, 0.9, 0.1 });
		nodeLungCancer.setPotential(potLungCancer);

		TablePotential potTuberculosis = new TablePotential(Arrays.asList(varTuberculosis, varVisitToAsia),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potTuberculosis.setValues(new double[] { 0.99, 0.01, 0.95, 0.05 });
		nodeTuberculosis.setPotential(potTuberculosis);

		TablePotential potTuberculosisOrCancer = new TablePotential(
				Arrays.asList(varTuberculosisOrCancer, varLungCancer, varTuberculosis),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potTuberculosisOrCancer.setValues(new double[] { 1, 0, 0, 1, 0, 1, 0, 1 });
		nodeTuberculosisOrCancer.setPotential(potTuberculosisOrCancer);

		// [ibermejo or fjdiez]
		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}
}