/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.testutils;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.Arrays;
import java.util.List;

/**
 * Reusable reference networks for inference module tests.
 *
 * @author Manuel Arias
 */
public class TestNetworks {

	/**
	 * Classic Asia network (8 nodes, BN).
	 * Known values without evidence:
	 *   P(VisitToAsia=no) = 0.99, P(Smoker=no) = 0.50
	 *   P(Dyspnea=no) ~ 0.5640, P(Bronchitis=no) = 0.55
	 * With evidence Dyspnea=yes:
	 *   P(Bronchitis=no) ~ 0.2116, P(Smoker=no) ~ 0.3577
	 */
	public static ProbNet buildAsia() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

		Variable varXRay = new Variable("X-ray", "no", "yes");
		Variable varBronchitis = new Variable("Bronchitis", "no", "yes");
		Variable varDyspnea = new Variable("Dyspnea", "no", "yes");
		Variable varVisitToAsia = new Variable("VisitToAsia", "no", "yes");
		Variable varSmoker = new Variable("Smoker", "no", "yes");
		Variable varLungCancer = new Variable("LungCancer", "no", "yes");
		Variable varTuberculosis = new Variable("Tuberculosis", "no", "yes");
		Variable varTubOrCancer = new Variable("TuberculosisOrCancer", "no", "yes");

		Node nodeXRay = probNet.addNode(varXRay, NodeType.CHANCE);
		Node nodeBronchitis = probNet.addNode(varBronchitis, NodeType.CHANCE);
		Node nodeDyspnea = probNet.addNode(varDyspnea, NodeType.CHANCE);
		Node nodeVisitToAsia = probNet.addNode(varVisitToAsia, NodeType.CHANCE);
		Node nodeSmoker = probNet.addNode(varSmoker, NodeType.CHANCE);
		Node nodeLungCancer = probNet.addNode(varLungCancer, NodeType.CHANCE);
		Node nodeTuberculosis = probNet.addNode(varTuberculosis, NodeType.CHANCE);
		Node nodeTubOrCancer = probNet.addNode(varTubOrCancer, NodeType.CHANCE);

		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeBronchitis, nodeDyspnea, true);
		probNet.addLink(nodeVisitToAsia, nodeTuberculosis, true);
		probNet.addLink(nodeSmoker, nodeBronchitis, true);
		probNet.addLink(nodeSmoker, nodeLungCancer, true);
		probNet.addLink(nodeLungCancer, nodeTubOrCancer, true);
		probNet.addLink(nodeTuberculosis, nodeTubOrCancer, true);
		probNet.addLink(nodeTubOrCancer, nodeDyspnea, true);
		probNet.addLink(nodeTubOrCancer, nodeXRay, true);

		TablePotential potXRay = new TablePotential(
				Arrays.asList(varXRay, varTubOrCancer), PotentialRole.CONDITIONAL_PROBABILITY);
		potXRay.setValues(new double[]{0.95, 0.05, 0.02, 0.98});
		nodeXRay.setPotential(potXRay);

		TablePotential potBronchitis = new TablePotential(
				Arrays.asList(varBronchitis, varSmoker), PotentialRole.CONDITIONAL_PROBABILITY);
		potBronchitis.setValues(new double[]{0.7, 0.3, 0.4, 0.6});
		nodeBronchitis.setPotential(potBronchitis);

		TablePotential potDyspnea = new TablePotential(
				Arrays.asList(varDyspnea, varTubOrCancer, varBronchitis), PotentialRole.CONDITIONAL_PROBABILITY);
		potDyspnea.setValues(new double[]{0.9, 0.1, 0.3, 0.7, 0.2, 0.8, 0.1, 0.9});
		nodeDyspnea.setPotential(potDyspnea);

		TablePotential potVisitToAsia = new TablePotential(
                List.of(varVisitToAsia), PotentialRole.CONDITIONAL_PROBABILITY);
		potVisitToAsia.setValues(new double[]{0.99, 0.01});
		nodeVisitToAsia.setPotential(potVisitToAsia);

		TablePotential potSmoker = new TablePotential(
                List.of(varSmoker), PotentialRole.CONDITIONAL_PROBABILITY);
		potSmoker.setValues(new double[]{0.5, 0.5});
		nodeSmoker.setPotential(potSmoker);

		TablePotential potLungCancer = new TablePotential(
				Arrays.asList(varLungCancer, varSmoker), PotentialRole.CONDITIONAL_PROBABILITY);
		potLungCancer.setValues(new double[]{0.99, 0.01, 0.9, 0.1});
		nodeLungCancer.setPotential(potLungCancer);

		TablePotential potTuberculosis = new TablePotential(
				Arrays.asList(varTuberculosis, varVisitToAsia), PotentialRole.CONDITIONAL_PROBABILITY);
		potTuberculosis.setValues(new double[]{0.99, 0.01, 0.95, 0.05});
		nodeTuberculosis.setPotential(potTuberculosis);

		TablePotential potTubOrCancer = new TablePotential(
				Arrays.asList(varTubOrCancer, varLungCancer, varTuberculosis), PotentialRole.CONDITIONAL_PROBABILITY);
		potTubOrCancer.setValues(new double[]{1, 0, 0, 1, 0, 1, 0, 1});
		nodeTubOrCancer.setPotential(potTubOrCancer);

		return probNet;
	}

	/**
	 * Simple BN: A -> B -> C (chain of 3 binary nodes).
	 * Useful for heuristic and basic elimination tests.
	 */
	public static ProbNet buildChain3() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

		Variable varA = new Variable("A", "a0", "a1");
		Variable varB = new Variable("B", "b0", "b1");
		Variable varC = new Variable("C", "c0", "c1");

		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
		Node nodeC = probNet.addNode(varC, NodeType.CHANCE);

		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeA, nodeB, true);
		probNet.addLink(nodeB, nodeC, true);

		TablePotential potA = new TablePotential(
                List.of(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		potA.setValues(new double[]{0.6, 0.4});
		nodeA.setPotential(potA);

		TablePotential potB = new TablePotential(
				Arrays.asList(varB, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		potB.setValues(new double[]{0.8, 0.2, 0.3, 0.7});
		nodeB.setPotential(potB);

		TablePotential potC = new TablePotential(
				Arrays.asList(varC, varB), PotentialRole.CONDITIONAL_PROBABILITY);
		potC.setValues(new double[]{0.9, 0.1, 0.4, 0.6});
		nodeC.setPotential(potC);

		return probNet;
	}

	/**
	 * Simple Influence Diagram: C -> D -> U
	 * C: chance node (binary), D: decision node (binary), U: utility node.
	 * Utility: U(C=0,D=0)=10, U(C=0,D=1)=5, U(C=1,D=0)=3, U(C=1,D=1)=8
	 * Optimal policy: D=0 if C=0, D=1 if C=1.
	 * Expected utility = P(C=0)*10 + P(C=1)*8 = 0.7*10 + 0.3*8 = 9.4
	 */
	public static ProbNet buildSimpleID() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		Variable varC = new Variable("Chance", "c0", "c1");
		Variable varD = new Variable("Decision", "d0", "d1");
		Variable varU = new Variable("Utility");

		Node nodeC = probNet.addNode(varC, NodeType.CHANCE);
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);

		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeC, nodeD, true);
		probNet.addLink(nodeC, nodeU, true);
		probNet.addLink(nodeD, nodeU, true);

		TablePotential potC = new TablePotential(
                List.of(varC), PotentialRole.CONDITIONAL_PROBABILITY);
		potC.setValues(new double[]{0.7, 0.3});
		nodeC.setPotential(potC);

		// Utility variable needs a decision criterion for VE to recognize it as additive
		varU.setDecisionCriterion(new Criterion());

		// U(C, D): order is [U, D, C] due to potential variable ordering
		// Values: U(C=0,D=0)=10, U(C=1,D=0)=3, U(C=0,D=1)=5, U(C=1,D=1)=8
		ExactDistrPotential potU = new ExactDistrPotential(
				Arrays.asList(varU, varD, varC), PotentialRole.UNSPECIFIED);
		potU.setValues(new double[]{10, 3, 5, 8});
		nodeU.setPotential(potU);

		return probNet;
	}

	/**
	 * Diamond BN: A -> B, A -> C, B -> D, C -> D (4 nodes).
	 * Useful for heuristic tests with fill-in.
	 */
	public static ProbNet buildDiamond() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

		Variable varA = new Variable("A", "a0", "a1");
		Variable varB = new Variable("B", "b0", "b1");
		Variable varC = new Variable("C", "c0", "c1");
		Variable varD = new Variable("D", "d0", "d1");

		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
		Node nodeC = probNet.addNode(varC, NodeType.CHANCE);
		Node nodeD = probNet.addNode(varD, NodeType.CHANCE);

		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeA, nodeB, true);
		probNet.addLink(nodeA, nodeC, true);
		probNet.addLink(nodeB, nodeD, true);
		probNet.addLink(nodeC, nodeD, true);

		TablePotential potA = new TablePotential(
                List.of(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		potA.setValues(new double[]{0.5, 0.5});
		nodeA.setPotential(potA);

		TablePotential potB = new TablePotential(
				Arrays.asList(varB, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		potB.setValues(new double[]{0.8, 0.2, 0.3, 0.7});
		nodeB.setPotential(potB);

		TablePotential potC = new TablePotential(
				Arrays.asList(varC, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		potC.setValues(new double[]{0.6, 0.4, 0.1, 0.9});
		nodeC.setPotential(potC);

		TablePotential potD = new TablePotential(
				Arrays.asList(varD, varB, varC), PotentialRole.CONDITIONAL_PROBABILITY);
		potD.setValues(new double[]{0.9, 0.1, 0.4, 0.6, 0.7, 0.3, 0.2, 0.8});
		nodeD.setPotential(potD);

		return probNet;
	}
}
