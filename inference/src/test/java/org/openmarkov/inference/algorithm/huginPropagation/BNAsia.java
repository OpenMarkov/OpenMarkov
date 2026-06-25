/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.huginPropagation;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.Arrays;

class BNAsia {
	public static ProbNet buildBN_asia_java() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
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

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}
}