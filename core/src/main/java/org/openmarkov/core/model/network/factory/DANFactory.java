/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.factory;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DANFactory extends NetsFactory {

	public static ProbNet buildOneChanceDAN() {
		ProbNet oneChanceDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		oneChanceDAN.setName(NamesNetworks.ONE_CHANCE_DAN.toString());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableU = new Variable("U");

		Node nodeX = oneChanceDAN.addNode(variableX, NodeType.CHANCE);
		Node nodeU = oneChanceDAN.addNode(variableU, NodeType.UTILITY);

		oneChanceDAN.makeLinksExplicit(false);
		oneChanceDAN.addLink(variableX, variableU, true);

		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.setValues(new double[] { 0.86, 0.14 });
		nodeX.setPotential(potentialX);

		ExactDistrPotential potentialU = new ExactDistrPotential(Arrays.asList(variableU, variableX));
		potentialU.getTablePotential().setValues(new double[] { 100, 30 });
		nodeU.setPotential(potentialU);

		return oneChanceDAN;
	}

	public static ProbNet buildDANPerfectKnowledge() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables
		Variable varDisease = new Variable("Disease", "absent", "present");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varHealth_state = new Variable("Health state");

		// Nodes
		Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDisease, nodeHealth_state, true);
		probNet.addLink(nodeTherapy, nodeHealth_state, true);

		// Potentials
		TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDisease.setValues(new double[] { 0.86, 0.14 });
		nodeDisease.setPotential(potDisease);

		ExactDistrPotential potHealth_state = new ExactDistrPotential(
				Arrays.asList(varHealth_state, varDisease, varTherapy));
		potHealth_state.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeHealth_state.setPotential(potHealth_state);

		// Link restrictions and revealing states
		// Always observed nodes
		nodeDisease.setAlwaysObserved(true);

		return probNet;

	}

	public static ProbNet buildDANNoKnowledge() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables
		Variable varDisease = new Variable("Disease", "absent", "present");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varHealth_state = new Variable("Health state");

		// Nodes
		Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDisease, nodeHealth_state, true);
		probNet.addLink(nodeTherapy, nodeHealth_state, true);

		// Potentials
		TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDisease.setValues(new double[] { 0.86, 0.14 });
		nodeDisease.setPotential(potDisease);

		ExactDistrPotential potHealth_state = new ExactDistrPotential(
				Arrays.asList(varHealth_state, varDisease, varTherapy));
		potHealth_state.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeHealth_state.setPotential(potHealth_state);

		// Link restrictions and revealing states
		// Always observed nodes
		nodeDisease.setAlwaysObserved(false);

		return probNet;

	}

	public static ProbNet buildDANTestAlways() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables
		Variable varDisease = new Variable("Disease", "absent", "present");
		Variable varResult_of_test = new Variable("Result of test", "negative", "positive");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varHealth_state = new Variable("Health state");
		Variable varCost_of_therapy = new Variable("Cost of therapy");

		// Nodes
		Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
		Node nodeResult_of_test = probNet.addNode(varResult_of_test, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);
		Node nodeCost_of_therapy = probNet.addNode(varCost_of_therapy, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDisease, nodeHealth_state, true);
		probNet.addLink(nodeDisease, nodeResult_of_test, true);
		probNet.addLink(nodeTherapy, nodeHealth_state, true);
		probNet.addLink(nodeTherapy, nodeCost_of_therapy, true);

		// Potentials
		TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDisease.setValues(new double[] { 0.86, 0.14 });
		nodeDisease.setPotential(potDisease);

		TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_of_test.setValues(new double[] { 0.97, 0.03, 0.09, 0.91 });
		nodeResult_of_test.setPotential(potResult_of_test);

		ExactDistrPotential potHealth_state = new ExactDistrPotential(
				Arrays.asList(varHealth_state, varDisease, varTherapy));
		potHealth_state.getTablePotential().setValues(new double[] { 9.8, 2.8, 8.8, 7.8 });
		nodeHealth_state.setPotential(potHealth_state);

		ExactDistrPotential potCost_of_therapy = new ExactDistrPotential(Arrays.asList(varCost_of_therapy, varTherapy));
		potCost_of_therapy.getTablePotential().setValues(new double[] { 0, -0.25 });
		nodeCost_of_therapy.setPotential(potCost_of_therapy);

		// Link restrictions and revealing states
		// Always observed nodes
		nodeResult_of_test.setAlwaysObserved(true);

		return probNet;
	}

	public static ProbNet buildDecideTreatmentRestrictedDAN() {
		ProbNet dan = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		dan.setName(NamesNetworks.PERFECT_INFORMATION_TREATMENT_RESTRICTED_DAN.toString());
		Variable variableX = new Variable("X", "absent", "present");
		Variable variableT = new Variable("T", "no", "yes");
		Variable variableU = new Variable("U");

		Node nodeX = dan.addNode(variableX, NodeType.CHANCE);
		Node nodeT = dan.addNode(variableT, NodeType.DECISION);
		Node nodeU = dan.addNode(variableU, NodeType.UTILITY);

		dan.makeLinksExplicit(false);
		dan.addLink(variableX, variableU, true);
		dan.addLink(variableT, variableU, true);
		dan.addLink(variableX, variableT, true);

		TablePotential potentialX = new TablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
		potentialX.setValues(new double[] { 0.86, 0.14 });
		nodeX.setPotential(potentialX);

		ExactDistrPotential potentialU = new ExactDistrPotential(Arrays.asList(variableU, variableX, variableT));
		potentialU.getTablePotential().setValues(new double[] { 100, 30, 90, 80 });
		nodeU.setPotential(potentialU);

		nodeX.setAlwaysObserved(true);

		Link<Node> link = dan.getLink(nodeX, nodeT, true);
		link.initializesRestrictionsPotential();
		TablePotential restrictionsPotential = (TablePotential) link.getRestrictionsPotential();
		restrictionsPotential.setValues(new double[] { 0, 1, 1, 1 });

		return dan;
	}

	public static ProbNet buildDecideTestDAN() {

		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables
		Variable varDisease = new Variable("Disease", "absent", "present");
		Variable varResult_of_test = new Variable("Result of test", "negative", "positive");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varDo_test_ = new Variable("Do test?", "no", "yes");
		Variable varHealth_state = new Variable("Health state");
		Variable varCost_of_test = new Variable("Cost of test");
		Variable varCost_of_Therapy = new Variable("Cost of Therapy");

		// Nodes
		Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
		Node nodeResult_of_test = probNet.addNode(varResult_of_test, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeDo_test_ = probNet.addNode(varDo_test_, NodeType.DECISION);
		Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);
		Node nodeCost_of_test = probNet.addNode(varCost_of_test, NodeType.UTILITY);
		Node nodeCost_of_Therapy = probNet.addNode(varCost_of_Therapy, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDisease, nodeHealth_state, true);
		probNet.addLink(nodeDisease, nodeResult_of_test, true);
		probNet.addLink(nodeTherapy, nodeHealth_state, true);
		probNet.addLink(nodeTherapy, nodeCost_of_Therapy, true);
		probNet.addLink(nodeDo_test_, nodeCost_of_test, true);
		probNet.addLink(nodeDo_test_, nodeResult_of_test, true);

		// Potentials
		TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDisease.setValues(new double[] { 0.86, 0.14 });
		nodeDisease.setPotential(potDisease);

		TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varDisease, varDo_test_),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_of_test.setValues(new double[] { 0, 0, 0, 0, 0.97, 0.03, 0.09, 0.91 });
		nodeResult_of_test.setPotential(potResult_of_test);

		ExactDistrPotential potHealth_state = new ExactDistrPotential(
				Arrays.asList(varHealth_state, varDisease, varTherapy));
		potHealth_state.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeHealth_state.setPotential(potHealth_state);

		ExactDistrPotential potCost_of_test = new ExactDistrPotential(Arrays.asList(varCost_of_test, varDo_test_));
		potCost_of_test.getTablePotential().setValues(new double[] { 0, -0.2 });
		nodeCost_of_test.setPotential(potCost_of_test);

		ExactDistrPotential potCost_of_Therapy = new ExactDistrPotential(Arrays.asList(varCost_of_Therapy, varTherapy));
		potCost_of_Therapy.getTablePotential().setValues(new double[] { 0, -0.25 });
		nodeCost_of_Therapy.setPotential(potCost_of_Therapy);

		// Link restrictions and revealing states
		Link link_nodeDo_test__nodeResult_of_test = probNet.getLink(nodeDo_test_, nodeResult_of_test, true);
		link_nodeDo_test__nodeResult_of_test.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDo_test__nodeResult_of_test = (TablePotential) link_nodeDo_test__nodeResult_of_test
				.getRestrictionsPotential();
		restrictions_nodeDo_test__nodeResult_of_test.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDo_test__nodeResult_of_test.setRevealingStates(Arrays.asList(varDo_test_.getStates()[1]));

		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildDatingDAN() {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		datingDAN.setName(NamesNetworks.DATING_DAN.toString());
		Variable variableAsk = new Variable("Ask", "no", "yes");
		Variable variableNClub = new Variable("NClub", "no", "yes");
		Variable variableAccept = new Variable("Accept", "no", "yes");
		Variable variableLikesMe = new Variable("LikesMe", "no", "yes");
		Variable variableToDo = new Variable("ToDo", "restaurant", "movie");
		Variable variableTV = new Variable("TV", "good", "bad");
		Variable variableTVExp = new Variable("TVExp", "negative", "positive");
		Variable variableClub = new Variable("Club", "negative", "positive");
		Variable variableMeetFr = new Variable("MeetFr", "negative", "positive");
		Variable variableNCExp = new Variable("NCExp", "negative", "positive");
		Variable variableMovie = new Variable("Movie", "romantic", "action");
		Variable variableRest = new Variable("Rest", "cheap", "expensive");
		Variable variableMMood = new Variable("mMood", "bad", "good");
		Variable variableRMood = new Variable("rMood", "bad", "good");
		Variable variableMExp = new Variable("mExp", "negative", "positive");
		Variable variableRExp = new Variable("rExp", "negative", "positive");
		Variable variableUTVExp = new Variable("U TVExp");
		Variable variableUNCExp = new Variable("U NCExp");
		Variable variableUmExp = new Variable("U mExp");
		Variable variableUrExp = new Variable("U rExp");

		Node nodeAsk = datingDAN.addNode(variableAsk, NodeType.DECISION);
		Node nodeNClub = datingDAN.addNode(variableNClub, NodeType.DECISION);
		Node nodeAccept = datingDAN.addNode(variableAccept, NodeType.CHANCE);
		Node nodeLikesMe = datingDAN.addNode(variableLikesMe, NodeType.CHANCE);
		Node nodeToDo = datingDAN.addNode(variableToDo, NodeType.CHANCE);
		Node nodeTV = datingDAN.addNode(variableTV, NodeType.CHANCE);
		Node nodeTVExp = datingDAN.addNode(variableTVExp, NodeType.CHANCE);
		Node nodeClub = datingDAN.addNode(variableClub, NodeType.CHANCE);
		Node nodeMeetFr = datingDAN.addNode(variableMeetFr, NodeType.CHANCE);
		Node nodeNCExp = datingDAN.addNode(variableNCExp, NodeType.CHANCE);
		Node nodeMovie = datingDAN.addNode(variableMovie, NodeType.DECISION);
		Node nodeRest = datingDAN.addNode(variableRest, NodeType.DECISION);
		Node nodeMMood = datingDAN.addNode(variableMMood, NodeType.CHANCE);
		Node nodeRMood = datingDAN.addNode(variableRMood, NodeType.CHANCE);
		Node nodeMExp = datingDAN.addNode(variableMExp, NodeType.CHANCE);
		Node nodeRExp = datingDAN.addNode(variableRExp, NodeType.CHANCE);
		Node nodeUTVExp = datingDAN.addNode(variableUTVExp, NodeType.UTILITY);
		Node nodeUNCExp = datingDAN.addNode(variableUNCExp, NodeType.UTILITY);
		Node nodeUmExp = datingDAN.addNode(variableUmExp, NodeType.UTILITY);
		Node nodeUrExp = datingDAN.addNode(variableUrExp, NodeType.UTILITY);

		datingDAN.makeLinksExplicit(false);
		datingDAN.addLink(variableAsk, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableToDo, true);
		datingDAN.addLink(variableAccept, variableNClub, true);
		datingDAN.addLink(variableAccept, variableToDo, true);
		datingDAN.addLink(variableTV, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableClub, true);
		datingDAN.addLink(variableNClub, variableMeetFr, true);
		datingDAN.addLink(variableClub, variableNCExp, true);
		datingDAN.addLink(variableMeetFr, variableNCExp, true);
		datingDAN.addLink(variableTVExp, variableUTVExp, true);
		datingDAN.addLink(variableNCExp, variableUNCExp, true);
		datingDAN.addLink(variableToDo, variableMovie, true);
		datingDAN.addLink(variableToDo, variableRest, true);
		datingDAN.addLink(variableMovie, variableMMood, true);
		datingDAN.addLink(variableMovie, variableMExp, true);
		datingDAN.addLink(variableMMood, variableMExp, true);
		datingDAN.addLink(variableMExp, variableUmExp, true);
		datingDAN.addLink(variableRest, variableRMood, true);
		datingDAN.addLink(variableRest, variableRExp, true);
		datingDAN.addLink(variableRMood, variableRExp, true);
		datingDAN.addLink(variableRExp, variableUrExp, true);

		TablePotential potentialAccept = new TablePotential(Arrays.asList(variableAccept, variableAsk, variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialAccept.setValues(new double[] { 1, 0, 0.99, 0.01, 1, 0, 0.25, 0.75 });
		nodeAccept.setPotential(potentialAccept);

		UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);

		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableAccept, variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.setValues(new double[] { 0, 0, 0.65, 0.35, 0, 0, 0.15, 0.85 });
		nodeToDo.setPotential(potentialToDo);

		TablePotential potentialMMood = new TablePotential(Arrays.asList(variableMMood, variableMovie),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMMood.setValues(new double[] { 0.25, 0.75, 0.88, 0.12 });
		nodeMMood.setPotential(potentialMMood);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMovie, variableMMood),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.setValues(new double[] { 0.99, 0.01, 0.95, 0.05, 0.15, 0.85, 0.01, 0.99 });
		nodeMExp.setPotential(potentialMExp);

		ExactDistrPotential potentialUmExp = new ExactDistrPotential(Arrays.asList(variableUmExp, variableMExp));
		potentialUmExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUmExp.setPotential(potentialUmExp);

		TablePotential potentialRMood = new TablePotential(Arrays.asList(variableRMood, variableRest),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRMood.setValues(new double[] { 0.5, 0.5, 0.2, 0.8 });
		nodeRMood.setPotential(potentialRMood);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRest, variableRMood),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRExp.setValues(new double[] { 0.95, 0.05, 1, 0, 0.01, 0.99, 0.08, 0.92 });
		nodeRExp.setPotential(potentialRExp);

		ExactDistrPotential potentialUrExp = new ExactDistrPotential(Arrays.asList(variableUrExp, variableRExp));
		potentialUrExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUrExp.setPotential(potentialUrExp);

		TablePotential potentialTV = new TablePotential(Arrays.asList(variableTV),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTV.setValues(new double[] { 0.15, 0.85 });
		nodeTV.setPotential(potentialTV);

		TablePotential potentialTVExp = new TablePotential(Arrays.asList(variableTVExp, variableNClub, variableTV),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTVExp.setValues(new double[] { 0, 1, 0, 0, 1, 0, 0, 0 });
		nodeTVExp.setPotential(potentialTVExp);

		TablePotential potentialClub = new TablePotential(Arrays.asList(variableClub, variableNClub),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialClub.setValues(new double[] { 0, 0, 0.22, 0.78 });
		nodeClub.setPotential(potentialClub);

		TablePotential potentialMeetFr = new TablePotential(Arrays.asList(variableMeetFr, variableNClub),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMeetFr.setValues(new double[] { 0, 0, 0.16, 0.84 });
		nodeMeetFr.setPotential(potentialMeetFr);

		ICIPotential potentialNCExp = new MaxPotential(Arrays.asList(variableNCExp, variableMeetFr, variableClub));
		potentialNCExp.setLeakyParameters(new double[] { 0.99, 0.01 });
		nodeNCExp.setPotential(potentialNCExp);

		ExactDistrPotential potentialUTVExp = new ExactDistrPotential(Arrays.asList(variableUTVExp, variableTVExp));
		potentialUTVExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUTVExp.setPotential(potentialUTVExp);

		ExactDistrPotential potentialUNCExp = new ExactDistrPotential(Arrays.asList(variableUNCExp, variableNCExp));
		potentialUNCExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUNCExp.setPotential(potentialUNCExp);

		Link<Node> linkAskAccept = datingDAN.getLink(nodeAsk, nodeAccept, true);
		linkAskAccept.initializesRestrictionsPotential();
		TablePotential restrictionsAskAccept = (TablePotential) linkAskAccept.getRestrictionsPotential();
		restrictionsAskAccept.setValues(new double[] { 1, 1, 0, 1 });
		linkAskAccept.setRevealingStates(Arrays.asList(variableAsk.getStates()[0], variableAsk.getStates()[1]));

		Link<Node> linkAcceptNClub = datingDAN.getLink(nodeAccept, nodeNClub, true);
		linkAcceptNClub.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptNClub = (TablePotential) linkAcceptNClub.getRestrictionsPotential();
		restrictionsAcceptNClub.setValues(new double[] { 1, 0, 1, 0 });

		Link<Node> linkAcceptToDo = datingDAN.getLink(nodeAccept, nodeToDo, true);
		linkAcceptToDo.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptToDo = (TablePotential) linkAcceptToDo.getRestrictionsPotential();
		restrictionsAcceptToDo.setValues(new double[] { 0, 1, 0, 1 });
		linkAcceptToDo.setRevealingStates(Arrays.asList(variableAccept.getStates()[1]));

		Link<Node> linkToDoMovie = datingDAN.getLink(nodeToDo, nodeMovie, true);
		linkToDoMovie.initializesRestrictionsPotential();
		TablePotential restrictionsToDoMovie = (TablePotential) linkToDoMovie.getRestrictionsPotential();
		restrictionsToDoMovie.setValues(new double[] { 0, 1, 0, 1 });

		Link<Node> linkToDoRest = datingDAN.getLink(nodeToDo, nodeRest, true);
		linkToDoRest.initializesRestrictionsPotential();
		TablePotential restrictionsToDoRest = (TablePotential) linkToDoRest.getRestrictionsPotential();
		restrictionsToDoRest.setValues(new double[] { 1, 0, 1, 0 });

		Link<Node> linkMovieMEXp = datingDAN.getLink(nodeMovie, nodeMExp, true);
		linkMovieMEXp.setRevealingStates(Arrays.asList(variableMovie.getStates()[0], variableMovie.getStates()[1]));

		Link<Node> linkRestREXp = datingDAN.getLink(nodeRest, nodeRExp, true);
		linkRestREXp.setRevealingStates(Arrays.asList(variableRest.getStates()[0], variableRest.getStates()[1]));

		Link<Node> linkNClubTVExp = datingDAN.getLink(nodeNClub, nodeTVExp, true);
		linkNClubTVExp.initializesRestrictionsPotential();
		TablePotential restrictionsNClubTVExp = (TablePotential) linkNClubTVExp.getRestrictionsPotential();
		restrictionsNClubTVExp.setValues(new double[] { 1, 0, 1, 0 });
		linkNClubTVExp.setRevealingStates(Arrays.asList(variableNClub.getStates()[0]));

		Link<Node> linkNClubClub = datingDAN.getLink(nodeNClub, nodeClub, true);
		linkNClubClub.initializesRestrictionsPotential();
		TablePotential restrictionsNClubClub = (TablePotential) linkNClubClub.getRestrictionsPotential();
		restrictionsNClubClub.setValues(new double[] { 0, 1, 0, 1 });
		linkNClubClub.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		Link<Node> linkNClubMeetFr = datingDAN.getLink(nodeNClub, nodeMeetFr, true);
		linkNClubMeetFr.initializesRestrictionsPotential();
		TablePotential restrictionsNClubMeetFr = (TablePotential) linkNClubMeetFr.getRestrictionsPotential();
		restrictionsNClubMeetFr.setValues(new double[] { 0, 1, 0, 1 });
		linkNClubMeetFr.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		nodeTV.setAlwaysObserved(true);

		return datingDAN;
	}

	public static ProbNet buildDatingAcceptNoDAN() {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		datingDAN.setName(NamesNetworks.DATING_ACCEPT_NO_DAN.toString());
		Variable variableNClub = new Variable("NClub", "no", "yes");
		Variable variableTV = new Variable("TV", "good", "bad");
		Variable variableTVExp = new Variable("TVExp", "negative", "positive");
		Variable variableClub = new Variable("Club", "negative", "positive");
		Variable variableMeetFr = new Variable("MeetFr", "negative", "positive");
		Variable variableNCExp = new Variable("NCExp", "negative", "positive");
		Variable variableUTVExp = new Variable("U TVExp");
		Variable variableUNCExp = new Variable("U NCExp");

		Node nodeNClub = datingDAN.addNode(variableNClub, NodeType.DECISION);
		Node nodeTV = datingDAN.addNode(variableTV, NodeType.CHANCE);
		Node nodeTVExp = datingDAN.addNode(variableTVExp, NodeType.CHANCE);
		Node nodeClub = datingDAN.addNode(variableClub, NodeType.CHANCE);
		Node nodeMeetFr = datingDAN.addNode(variableMeetFr, NodeType.CHANCE);
		Node nodeNCExp = datingDAN.addNode(variableNCExp, NodeType.CHANCE);
		Node nodeUTVExp = datingDAN.addNode(variableUTVExp, NodeType.UTILITY);
		Node nodeUNCExp = datingDAN.addNode(variableUNCExp, NodeType.UTILITY);

		datingDAN.makeLinksExplicit(false);
		datingDAN.addLink(variableTV, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableClub, true);
		datingDAN.addLink(variableNClub, variableMeetFr, true);
		datingDAN.addLink(variableClub, variableNCExp, true);
		datingDAN.addLink(variableMeetFr, variableNCExp, true);
		datingDAN.addLink(variableTVExp, variableUTVExp, true);
		datingDAN.addLink(variableNCExp, variableUNCExp, true);

		TablePotential potentialTV = new TablePotential(Arrays.asList(variableTV),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTV.setValues(new double[] { 0.15, 0.85 });
		nodeTV.setPotential(potentialTV);

		TablePotential potentialTVExp = new TablePotential(Arrays.asList(variableTVExp, variableNClub, variableTV),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTVExp.setValues(new double[] { 0, 1, 0, 0, 1, 0, 0, 0 });
		nodeTVExp.setPotential(potentialTVExp);

		TablePotential potentialClub = new TablePotential(Arrays.asList(variableClub, variableNClub),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialClub.setValues(new double[] { 0, 0, 0.22, 0.78 });
		nodeClub.setPotential(potentialClub);

		TablePotential potentialMeetFr = new TablePotential(Arrays.asList(variableMeetFr, variableNClub),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMeetFr.setValues(new double[] { 0, 0, 0.16, 0.84 });
		nodeMeetFr.setPotential(potentialMeetFr);

		ICIPotential potentialNCExp = new MaxPotential(Arrays.asList(variableNCExp, variableMeetFr, variableClub));
		potentialNCExp.setLeakyParameters(new double[] { 0.99, 0.01 });
		nodeNCExp.setPotential(potentialNCExp);

		ExactDistrPotential potentialUTVExp = new ExactDistrPotential(Arrays.asList(variableUTVExp, variableTVExp));
		potentialUTVExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUTVExp.setPotential(potentialUTVExp);

		ExactDistrPotential potentialUNCExp = new ExactDistrPotential(Arrays.asList(variableUNCExp, variableNCExp));
		potentialUNCExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUNCExp.setPotential(potentialUNCExp);

		Link<Node> linkNClubTVExp = datingDAN.getLink(nodeNClub, nodeTVExp, true);
		linkNClubTVExp.initializesRestrictionsPotential();
		TablePotential restrictionsNClubTVExp = (TablePotential) linkNClubTVExp.getRestrictionsPotential();
		restrictionsNClubTVExp.setValues(new double[] { 1, 0, 1, 0 });
		linkNClubTVExp.setRevealingStates(Arrays.asList(variableNClub.getStates()[0]));

		Link<Node> linkNClubClub = datingDAN.getLink(nodeNClub, nodeClub, true);
		linkNClubClub.initializesRestrictionsPotential();
		TablePotential restrictionsNClubClub = (TablePotential) linkNClubClub.getRestrictionsPotential();
		restrictionsNClubClub.setValues(new double[] { 0, 1, 0, 1 });
		linkNClubClub.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		Link<Node> linkNClubMeetFr = datingDAN.getLink(nodeNClub, nodeMeetFr, true);
		linkNClubMeetFr.initializesRestrictionsPotential();
		TablePotential restrictionsNClubMeetFr = (TablePotential) linkNClubMeetFr.getRestrictionsPotential();
		restrictionsNClubMeetFr.setValues(new double[] { 0, 1, 0, 1 });
		linkNClubMeetFr.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		nodeTV.setAlwaysObserved(true);

		return datingDAN;
	}

	public static ProbNet buildDatingTVBadDAN() {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		datingDAN.setName(NamesNetworks.DATING_TV_BAD_DAN.toString());
		Variable variableAsk = new Variable("Ask", "no", "yes");
		Variable variableNClub = new Variable("NClub", "no", "yes");
		Variable variableAccept = new Variable("Accept", "no", "yes");
		Variable variableLikesMe = new Variable("LikesMe", "no", "yes");
		Variable variableToDo = new Variable("ToDo", "restaurant", "movie");
		//Variable variableTV = new Variable("TV","good","bad");
		Variable variableTVExp = new Variable("TVExp", "negative", "positive");
		Variable variableClub = new Variable("Club", "negative", "positive");
		Variable variableMeetFr = new Variable("MeetFr", "negative", "positive");
		Variable variableNCExp = new Variable("NCExp", "negative", "positive");
		Variable variableMovie = new Variable("Movie", "romantic", "action");
		Variable variableRest = new Variable("Rest", "cheap", "expensive");
		Variable variableMMood = new Variable("mMood", "bad", "good");
		Variable variableRMood = new Variable("rMood", "bad", "good");
		Variable variableMExp = new Variable("mExp", "negative", "positive");
		Variable variableRExp = new Variable("rExp", "negative", "positive");
		Variable variableUTVExp = new Variable("U TVExp");
		Variable variableUNCExp = new Variable("U NCExp");
		Variable variableUmExp = new Variable("U mExp");
		Variable variableUrExp = new Variable("U rExp");

		Node nodeAsk = datingDAN.addNode(variableAsk, NodeType.DECISION);
		Node nodeNClub = datingDAN.addNode(variableNClub, NodeType.DECISION);
		Node nodeAccept = datingDAN.addNode(variableAccept, NodeType.CHANCE);
		Node nodeLikesMe = datingDAN.addNode(variableLikesMe, NodeType.CHANCE);
		Node nodeToDo = datingDAN.addNode(variableToDo, NodeType.CHANCE);
		//Node nodeTV = datingDAN.addNode(variableTV, NodeType.CHANCE);
		Node nodeTVExp = datingDAN.addNode(variableTVExp, NodeType.CHANCE);
		Node nodeClub = datingDAN.addNode(variableClub, NodeType.CHANCE);
		Node nodeMeetFr = datingDAN.addNode(variableMeetFr, NodeType.CHANCE);
		Node nodeNCExp = datingDAN.addNode(variableNCExp, NodeType.CHANCE);
		Node nodeMovie = datingDAN.addNode(variableMovie, NodeType.DECISION);
		Node nodeRest = datingDAN.addNode(variableRest, NodeType.DECISION);
		Node nodeMMood = datingDAN.addNode(variableMMood, NodeType.CHANCE);
		Node nodeRMood = datingDAN.addNode(variableRMood, NodeType.CHANCE);
		Node nodeMExp = datingDAN.addNode(variableMExp, NodeType.CHANCE);
		Node nodeRExp = datingDAN.addNode(variableRExp, NodeType.CHANCE);
		Node nodeUTVExp = datingDAN.addNode(variableUTVExp, NodeType.UTILITY);
		Node nodeUNCExp = datingDAN.addNode(variableUNCExp, NodeType.UTILITY);
		Node nodeUmExp = datingDAN.addNode(variableUmExp, NodeType.UTILITY);
		Node nodeUrExp = datingDAN.addNode(variableUrExp, NodeType.UTILITY);

		datingDAN.makeLinksExplicit(false);
		datingDAN.addLink(variableAsk, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableAccept, true);
		datingDAN.addLink(variableLikesMe, variableToDo, true);
		datingDAN.addLink(variableAccept, variableNClub, true);
		datingDAN.addLink(variableAccept, variableToDo, true);
		//datingDAN.addLink(variableTV, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableTVExp, true);
		datingDAN.addLink(variableNClub, variableClub, true);
		datingDAN.addLink(variableNClub, variableMeetFr, true);
		datingDAN.addLink(variableClub, variableNCExp, true);
		datingDAN.addLink(variableMeetFr, variableNCExp, true);
		datingDAN.addLink(variableTVExp, variableUTVExp, true);
		datingDAN.addLink(variableNCExp, variableUNCExp, true);
		datingDAN.addLink(variableToDo, variableMovie, true);
		datingDAN.addLink(variableToDo, variableRest, true);
		datingDAN.addLink(variableMovie, variableMMood, true);
		datingDAN.addLink(variableMovie, variableMExp, true);
		datingDAN.addLink(variableMMood, variableMExp, true);
		datingDAN.addLink(variableMExp, variableUmExp, true);
		datingDAN.addLink(variableRest, variableRMood, true);
		datingDAN.addLink(variableRest, variableRExp, true);
		datingDAN.addLink(variableRMood, variableRExp, true);
		datingDAN.addLink(variableRExp, variableUrExp, true);

		TablePotential potentialAccept = new TablePotential(Arrays.asList(variableAccept, variableAsk, variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialAccept.setValues(new double[] { 1, 0, 0.99, 0.01, 1, 0, 0.25, 0.75 });
		nodeAccept.setPotential(potentialAccept);

		UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);

		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableAccept, variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.setValues(new double[] { 0, 0, 0.65, 0.35, 0, 0, 0.15, 0.85 });
		nodeToDo.setPotential(potentialToDo);

		TablePotential potentialMMood = new TablePotential(Arrays.asList(variableMMood, variableMovie),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMMood.setValues(new double[] { 0.25, 0.75, 0.88, 0.12 });
		nodeMMood.setPotential(potentialMMood);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMovie, variableMMood),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.setValues(new double[] { 0.99, 0.01, 0.95, 0.05, 0.15, 0.85, 0.01, 0.99 });
		nodeMExp.setPotential(potentialMExp);

		ExactDistrPotential potentialUmExp = new ExactDistrPotential(Arrays.asList(variableUmExp, variableMExp));
		potentialUmExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUmExp.setPotential(potentialUmExp);

		TablePotential potentialRMood = new TablePotential(Arrays.asList(variableRMood, variableRest),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRMood.setValues(new double[] { 0.5, 0.5, 0.2, 0.8 });
		nodeRMood.setPotential(potentialRMood);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRest, variableRMood),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRExp.setValues(new double[] { 0.95, 0.05, 1, 0, 0.01, 0.99, 0.08, 0.92 });
		nodeRExp.setPotential(potentialRExp);

		ExactDistrPotential potentialUrExp = new ExactDistrPotential(Arrays.asList(variableUrExp, variableRExp));
		potentialUrExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUrExp.setPotential(potentialUrExp);

		TablePotential potentialTVExp = new TablePotential(Arrays.asList(variableTVExp, variableNClub),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialTVExp.setValues(new double[] { 1.0, 0.0, 0.0, 0.0 });
		nodeTVExp.setPotential(potentialTVExp);

		TablePotential potentialClub = new TablePotential(Arrays.asList(variableClub, variableNClub),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialClub.setValues(new double[] { 0, 0, 0.22, 0.78 });
		nodeClub.setPotential(potentialClub);

		TablePotential potentialMeetFr = new TablePotential(Arrays.asList(variableMeetFr, variableNClub),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMeetFr.setValues(new double[] { 0, 0, 0.16, 0.84 });
		nodeMeetFr.setPotential(potentialMeetFr);

		ICIPotential potentialNCExp = new MaxPotential(Arrays.asList(variableNCExp, variableMeetFr, variableClub));
		potentialNCExp.setLeakyParameters(new double[] { 0.99, 0.01 });
		nodeNCExp.setPotential(potentialNCExp);

		ExactDistrPotential potentialUTVExp = new ExactDistrPotential(Arrays.asList(variableUTVExp, variableTVExp));
		potentialUTVExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUTVExp.setPotential(potentialUTVExp);

		ExactDistrPotential potentialUNCExp = new ExactDistrPotential(Arrays.asList(variableUNCExp, variableNCExp));
		potentialUNCExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUNCExp.setPotential(potentialUNCExp);

		Link<Node> linkAskAccept = datingDAN.getLink(nodeAsk, nodeAccept, true);
		linkAskAccept.initializesRestrictionsPotential();
		TablePotential restrictionsAskAccept = (TablePotential) linkAskAccept.getRestrictionsPotential();
		restrictionsAskAccept.setValues(new double[] { 1, 1, 0, 1 });
		linkAskAccept.setRevealingStates(Arrays.asList(variableAsk.getStates()[0], variableAsk.getStates()[1]));

		Link<Node> linkAcceptNClub = datingDAN.getLink(nodeAccept, nodeNClub, true);
		linkAcceptNClub.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptNClub = (TablePotential) linkAcceptNClub.getRestrictionsPotential();
		restrictionsAcceptNClub.setValues(new double[] { 1, 0, 1, 0 });

		Link<Node> linkAcceptToDo = datingDAN.getLink(nodeAccept, nodeToDo, true);
		linkAcceptToDo.initializesRestrictionsPotential();
		TablePotential restrictionsAcceptToDo = (TablePotential) linkAcceptToDo.getRestrictionsPotential();
		restrictionsAcceptToDo.setValues(new double[] { 0, 1, 0, 1 });
		linkAcceptToDo.setRevealingStates(Arrays.asList(variableAccept.getStates()[1]));

		Link<Node> linkToDoMovie = datingDAN.getLink(nodeToDo, nodeMovie, true);
		linkToDoMovie.initializesRestrictionsPotential();
		TablePotential restrictionsToDoMovie = (TablePotential) linkToDoMovie.getRestrictionsPotential();
		restrictionsToDoMovie.setValues(new double[] { 0, 1, 0, 1 });

		Link<Node> linkToDoRest = datingDAN.getLink(nodeToDo, nodeRest, true);
		linkToDoRest.initializesRestrictionsPotential();
		TablePotential restrictionsToDoRest = (TablePotential) linkToDoRest.getRestrictionsPotential();
		restrictionsToDoRest.setValues(new double[] { 1, 0, 1, 0 });

		Link<Node> linkMovieMEXp = datingDAN.getLink(nodeMovie, nodeMExp, true);
		linkMovieMEXp.setRevealingStates(Arrays.asList(variableMovie.getStates()[0], variableMovie.getStates()[1]));

		Link<Node> linkRestREXp = datingDAN.getLink(nodeRest, nodeRExp, true);
		linkRestREXp.setRevealingStates(Arrays.asList(variableRest.getStates()[0], variableRest.getStates()[1]));

		Link<Node> linkNClubTVExp = datingDAN.getLink(nodeNClub, nodeTVExp, true);
		linkNClubTVExp.initializesRestrictionsPotential();
		TablePotential restrictionsNClubTVExp = (TablePotential) linkNClubTVExp.getRestrictionsPotential();
		restrictionsNClubTVExp.setValues(new double[] { 1, 0, 1, 0 });
		linkNClubTVExp.setRevealingStates(Arrays.asList(variableNClub.getStates()[0]));

		Link<Node> linkNClubClub = datingDAN.getLink(nodeNClub, nodeClub, true);
		linkNClubClub.initializesRestrictionsPotential();
		TablePotential restrictionsNClubClub = (TablePotential) linkNClubClub.getRestrictionsPotential();
		restrictionsNClubClub.setValues(new double[] { 0, 1, 0, 1 });
		linkNClubClub.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		Link<Node> linkNClubMeetFr = datingDAN.getLink(nodeNClub, nodeMeetFr, true);
		linkNClubMeetFr.initializesRestrictionsPotential();
		TablePotential restrictionsNClubMeetFr = (TablePotential) linkNClubMeetFr.getRestrictionsPotential();
		restrictionsNClubMeetFr.setValues(new double[] { 0, 1, 0, 1 });
		linkNClubMeetFr.setRevealingStates(Arrays.asList(variableNClub.getStates()[1]));

		return datingDAN;
	}

	public static ProbNet buildDatingBranchAcceptSimplifiedDAN() {
		ProbNet datingDAN = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		datingDAN.setName(NamesNetworks.BRANCH_ACCEPT_DATING_SIMPLIFIED_DAN.toString());
		Variable variableToDo = new Variable("ToDo", "restaurant", "movie");
		Variable variableMovie = new Variable("Movie", "romantic", "action");
		Variable variableRest = new Variable("Rest", "cheap", "expensive");
		Variable variableMExp = new Variable("mExp", "negative", "positive");
		Variable variableRExp = new Variable("rExp", "negative", "positive");
		Variable variableUmExp = new Variable("U mExp");
		Variable variableUrExp = new Variable("U rExp");
		Variable variableLikesMe = new Variable("LikesMe", "no", "yes");

		Node nodeToDo = datingDAN.addNode(variableToDo, NodeType.CHANCE);
		Node nodeMovie = datingDAN.addNode(variableMovie, NodeType.DECISION);
		Node nodeRest = datingDAN.addNode(variableRest, NodeType.DECISION);
		Node nodeMExp = datingDAN.addNode(variableMExp, NodeType.CHANCE);
		Node nodeRExp = datingDAN.addNode(variableRExp, NodeType.CHANCE);
		Node nodeUmExp = datingDAN.addNode(variableUmExp, NodeType.UTILITY);
		Node nodeUrExp = datingDAN.addNode(variableUrExp, NodeType.UTILITY);
		Node nodeLikesMe = datingDAN.addNode(variableLikesMe, NodeType.CHANCE);

		datingDAN.makeLinksExplicit(false);
		datingDAN.addLink(variableToDo, variableMovie, true);
		datingDAN.addLink(variableToDo, variableRest, true);
		datingDAN.addLink(variableMovie, variableMExp, true);
		datingDAN.addLink(variableMExp, variableUmExp, true);
		datingDAN.addLink(variableRest, variableRExp, true);
		datingDAN.addLink(variableRExp, variableUrExp, true);
		datingDAN.addLink(variableLikesMe, variableToDo, true);

		UniformPotential potentialLikesMe = new UniformPotential(Arrays.asList(variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeLikesMe.setPotential(potentialLikesMe);

		TablePotential potentialToDo = new TablePotential(Arrays.asList(variableToDo, variableLikesMe),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialToDo.setValues(new double[] { 0.65, 0.35, 0.15, 0.85 });
		nodeToDo.setPotential(potentialToDo);

		TablePotential potentialMExp = new TablePotential(Arrays.asList(variableMExp, variableMovie),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialMExp.setValues(new double[] { 0.01, 0.99, 0.95, 0.05 });
		nodeMExp.setPotential(potentialMExp);

		ExactDistrPotential potentialUmExp = new ExactDistrPotential(Arrays.asList(variableUmExp, variableMExp));
		potentialUmExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUmExp.setPotential(potentialUmExp);

		TablePotential potentialRExp = new TablePotential(Arrays.asList(variableRExp, variableRest),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potentialRExp.setValues(new double[] { 0.95, 0.05, 0.0, 1.0 });
		nodeRExp.setPotential(potentialRExp);

		ExactDistrPotential potentialUrExp = new ExactDistrPotential(Arrays.asList(variableUrExp, variableRExp));
		potentialUrExp.getTablePotential().setValues(new double[] { -10, 10 });
		nodeUrExp.setPotential(potentialUrExp);

		Link<Node> linkToDoMovie = datingDAN.getLink(nodeToDo, nodeMovie, true);
		linkToDoMovie.initializesRestrictionsPotential();
		TablePotential restrictionsToDoMovie = (TablePotential) linkToDoMovie.getRestrictionsPotential();
		restrictionsToDoMovie.setValues(new double[] { 0, 1, 0, 1 });

		Link<Node> linkToDoRest = datingDAN.getLink(nodeToDo, nodeRest, true);
		linkToDoRest.initializesRestrictionsPotential();
		TablePotential restrictionsToDoRest = (TablePotential) linkToDoRest.getRestrictionsPotential();
		restrictionsToDoRest.setValues(new double[] { 1, 0, 1, 0 });

		Link<Node> linkMovieMEXp = datingDAN.getLink(nodeMovie, nodeMExp, true);
		linkMovieMEXp.setRevealingStates(Arrays.asList(variableMovie.getStates()[0], variableMovie.getStates()[1]));

		Link<Node> linkRestREXp = datingDAN.getLink(nodeRest, nodeRExp, true);
		linkRestREXp.setRevealingStates(Arrays.asList(variableRest.getStates()[0], variableRest.getStates()[1]));

		nodeToDo.setAlwaysObserved(true);

		return datingDAN;
	}

	public static ProbNet buildDANUnorderedTwoDecsNoChance() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables
		Variable varU = new Variable("U");
		Variable varD = new Variable("D", "no", "yes");
		Variable varE = new Variable("E", "no", "yes");

		// Nodes
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeE = probNet.addNode(varE, NodeType.DECISION);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeD, nodeU, true);
		probNet.addLink(nodeE, nodeU, true);

		// Potentials
		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varD, varE));
		potU.getTablePotential().setValues(new double[] { 1, 2, 3, 4 });
		nodeU.setPotential(potU);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildDANUnorderedTwoDecs() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables
		Variable varY = new Variable("Y", "negative", "positive");
		Variable varX = new Variable("X", "negative", "positive");
		Variable varD = new Variable("D", "absent", "present");
		Variable varE = new Variable("E", "absent", "present");
		Variable varU = new Variable("U");

		// Nodes
		Node nodeY = probNet.addNode(varY, NodeType.CHANCE);
		Node nodeX = probNet.addNode(varX, NodeType.CHANCE);
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeE = probNet.addNode(varE, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeY, nodeU, true);
		probNet.addLink(nodeX, nodeU, true);
		probNet.addLink(nodeD, nodeX, true);
		probNet.addLink(nodeD, nodeU, true);
		probNet.addLink(nodeE, nodeY, true);
		probNet.addLink(nodeE, nodeU, true);

		// Potentials
		TablePotential potY = new TablePotential(Arrays.asList(varY, varE), PotentialRole.CONDITIONAL_PROBABILITY);
		potY.setValues(new double[] { 0.9, 0.1, 0.1, 0.9 });
		nodeY.setPotential(potY);

		TablePotential potX = new TablePotential(Arrays.asList(varX, varD), PotentialRole.CONDITIONAL_PROBABILITY);
		potX.setValues(new double[] { 0.6, 0.4, 0.4, 0.6 });
		nodeX.setPotential(potX);

		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varE, varD, varY, varX));
		potU.getTablePotential().setValues(new double[] { 0, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 10, 10 });
		nodeU.setPotential(potU);

		// Link restrictions and revealing states
		Link link_nodeD_nodeX = probNet.getLink(nodeD, nodeX, true);
		link_nodeD_nodeX.setRevealingStates(Arrays.asList(varD.getStates()[1], varD.getStates()[0]));

		Link link_nodeE_nodeY = probNet.getLink(nodeE, nodeY, true);
		link_nodeE_nodeY.setRevealingStates(Arrays.asList(varE.getStates()[1], varE.getStates()[0]));

		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildReactorDAN() {

		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		probNet.setName(NamesNetworks.REACTOR_DAN.toString());
		// Variables
		Variable varResult_of_advanced_reactor = new Variable("Result of advanced reactor", "success",
				"limited accident", "major accident");
		Variable varResult_of_test = new Variable("Result of test", "bad", "good", "excellent");
		Variable varResult_of_conventional_reactor = new Variable("Result of conventional reactor", "success",
				"failure");
		Variable varAdvanced_reactor_reliability = new Variable("Advanced reactor reliability", "success",
				"limited accident", "major accident");
		Variable varTest_decision = new Variable("Test decision", "test", "notest");
		Variable varBuild_decision = new Variable("Build decision", "build advanced", "build conventional",
				"build none");
		Variable varCost_of_test = new Variable("Cost of test");
		Variable varBenefit_of_advanced_reactor = new Variable("Benefit of advanced reactor");
		Variable varBenefit_of_conventional_reactor = new Variable("Benefit of conventional reactor");

		// Nodes
		Node nodeResult_of_advanced_reactor = probNet.addNode(varResult_of_advanced_reactor, NodeType.CHANCE);
		Node nodeResult_of_test = probNet.addNode(varResult_of_test, NodeType.CHANCE);
		Node nodeResult_of_conventional_reactor = probNet.addNode(varResult_of_conventional_reactor, NodeType.CHANCE);
		Node nodeAdvanced_reactor_reliability = probNet.addNode(varAdvanced_reactor_reliability, NodeType.CHANCE);
		Node nodeTest_decision = probNet.addNode(varTest_decision, NodeType.DECISION);
		Node nodeBuild_decision = probNet.addNode(varBuild_decision, NodeType.DECISION);
		Node nodeCost_of_test = probNet.addNode(varCost_of_test, NodeType.UTILITY);
		Node nodeBenefit_of_advanced_reactor = probNet.addNode(varBenefit_of_advanced_reactor, NodeType.UTILITY);
		Node nodeBenefit_of_conventional_reactor = probNet
				.addNode(varBenefit_of_conventional_reactor, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeResult_of_advanced_reactor, nodeBenefit_of_advanced_reactor, true);
		probNet.addLink(nodeResult_of_test, nodeBuild_decision, true);
		probNet.addLink(nodeResult_of_conventional_reactor, nodeBenefit_of_conventional_reactor, true);
		probNet.addLink(nodeAdvanced_reactor_reliability, nodeResult_of_test, true);
		probNet.addLink(nodeAdvanced_reactor_reliability, nodeResult_of_advanced_reactor, true);
		probNet.addLink(nodeTest_decision, nodeCost_of_test, true);
		probNet.addLink(nodeTest_decision, nodeResult_of_test, true);
		probNet.addLink(nodeTest_decision, nodeBuild_decision, true);
		probNet.addLink(nodeBuild_decision, nodeResult_of_advanced_reactor, true);
		probNet.addLink(nodeBuild_decision, nodeResult_of_conventional_reactor, true);

		// Potentials
		TablePotential potResult_of_advanced_reactor = new TablePotential(
				Arrays.asList(varResult_of_advanced_reactor, varBuild_decision, varAdvanced_reactor_reliability),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_of_advanced_reactor.setValues(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 1, 0, 0, 0, 0, 0, 0 });
		nodeResult_of_advanced_reactor.setPotential(potResult_of_advanced_reactor);

		TablePotential potResult_of_test = new TablePotential(
				Arrays.asList(varResult_of_test, varTest_decision, varAdvanced_reactor_reliability),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_of_test.setValues(new double[] { 0.05, 0.6, 0.35, 0, 0, 0, 0.85, 0.1, 0.05, 0, 0, 0, 0.9, 0.08, 0.02,
				0, 0, 0 });
		nodeResult_of_test.setPotential(potResult_of_test);

		TablePotential potResult_of_conventional_reactor = new TablePotential(
				Arrays.asList(varResult_of_conventional_reactor, varBuild_decision),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_of_conventional_reactor.setValues(new double[] { 0, 0, 0.98, 0.02, 0, 0 });
		nodeResult_of_conventional_reactor.setPotential(potResult_of_conventional_reactor);

		TablePotential potAdvanced_reactor_reliability = new TablePotential(
				Arrays.asList(varAdvanced_reactor_reliability), PotentialRole.CONDITIONAL_PROBABILITY);
		potAdvanced_reactor_reliability.setValues(new double[] { 0.88, 0.1, 0.02 });
		nodeAdvanced_reactor_reliability.setPotential(potAdvanced_reactor_reliability);

		ExactDistrPotential potCost_of_test = new ExactDistrPotential(Arrays.asList(varCost_of_test, varTest_decision));
		potCost_of_test.getTablePotential().setValues(new double[] { -1, 0 });
		nodeCost_of_test.setPotential(potCost_of_test);

		ExactDistrPotential potBenefit_of_advanced_reactor = new ExactDistrPotential(
				Arrays.asList(varBenefit_of_advanced_reactor, varResult_of_advanced_reactor));
		potBenefit_of_advanced_reactor.getTablePotential().setValues(new double[] { 12, -6, -10 });
		nodeBenefit_of_advanced_reactor.setPotential(potBenefit_of_advanced_reactor);

		ExactDistrPotential potBenefit_of_conventional_reactor = new ExactDistrPotential(
				Arrays.asList(varBenefit_of_conventional_reactor, varResult_of_conventional_reactor));
		potBenefit_of_conventional_reactor.getTablePotential().setValues(new double[] { 8, -4 });
		nodeBenefit_of_conventional_reactor.setPotential(potBenefit_of_conventional_reactor);

		// Link<Node> restrictions and revealing states
		Link<Node> link_nodeResult_of_test_nodeBuild_decision = probNet
				.getLink(nodeResult_of_test, nodeBuild_decision, true);
		link_nodeResult_of_test_nodeBuild_decision.initializesRestrictionsPotential();
		TablePotential restrictions_nodeResult_of_test_nodeBuild_decision = (TablePotential) link_nodeResult_of_test_nodeBuild_decision
				.getRestrictionsPotential();
		restrictions_nodeResult_of_test_nodeBuild_decision.setValues(new double[] { 0, 1, 1, 1, 1, 1, 1, 1, 1 });

		Link<Node> link_nodeTest_decision_nodeResult_of_test = probNet
				.getLink(nodeTest_decision, nodeResult_of_test, true);
		link_nodeTest_decision_nodeResult_of_test.initializesRestrictionsPotential();
		TablePotential restrictions_nodeTest_decision_nodeResult_of_test = (TablePotential) link_nodeTest_decision_nodeResult_of_test
				.getRestrictionsPotential();
		restrictions_nodeTest_decision_nodeResult_of_test.setValues(new double[] { 1, 0, 1, 0, 1, 0 });
		link_nodeTest_decision_nodeResult_of_test.setRevealingStates(Arrays.asList(varTest_decision.getStates()[0]));

		Link<Node> link_nodeBuild_decision_nodeResult_of_advanced_reactor = probNet
				.getLink(nodeBuild_decision, nodeResult_of_advanced_reactor, true);
		link_nodeBuild_decision_nodeResult_of_advanced_reactor.initializesRestrictionsPotential();
		TablePotential restrictions_nodeBuild_decision_nodeResult_of_advanced_reactor = (TablePotential) link_nodeBuild_decision_nodeResult_of_advanced_reactor
				.getRestrictionsPotential();
		restrictions_nodeBuild_decision_nodeResult_of_advanced_reactor.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0,
				0 });
		link_nodeBuild_decision_nodeResult_of_advanced_reactor
				.setRevealingStates(Arrays.asList(varBuild_decision.getStates()[0]));

		Link<Node> link_nodeBuild_decision_nodeResult_of_conventional_reactor = probNet
				.getLink(nodeBuild_decision, nodeResult_of_conventional_reactor, true);
		link_nodeBuild_decision_nodeResult_of_conventional_reactor.initializesRestrictionsPotential();
		TablePotential restrictions_nodeBuild_decision_nodeResult_of_conventional_reactor = (TablePotential) link_nodeBuild_decision_nodeResult_of_conventional_reactor
				.getRestrictionsPotential();
		restrictions_nodeBuild_decision_nodeResult_of_conventional_reactor.setValues(new double[] { 0, 1, 0, 0, 1, 0 });
		link_nodeBuild_decision_nodeResult_of_conventional_reactor
				.setRevealingStates(Arrays.asList(varBuild_decision.getStates()[1]));

		return probNet;
	}

	public static ProbNet buildWooerDAN() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		probNet.setName(NamesNetworks.WOOER_DAN.toString());
		// Variables
		Variable varResult_1 = new Variable("Result 1", "fail", "pass");
		Variable varResult_2 = new Variable("Result 2", "fail", "pass");
		Variable varResult_3 = new Variable("Result 3", "fail", "pass");
		Variable varQuality_of_wooer = new Variable("Quality of wooer", "bad", "good");
		Variable varWedding = new Variable("Wedding", "no", "yes");
		Variable varQuality_of_general = new Variable("Quality of general", "bad", "good");
		Variable varWealth = new Variable("Wealth", "poor", "wealthy");
		Variable varOffspring = new Variable("Offspring", "no", "yes");
		Variable varNoble_descent = new Variable("Noble descent", "no", "yes");
		Variable varTask_1 = new Variable("Task 1", "kill dragon", "kill unicorn");
		Variable varTask_2 = new Variable("Task 2", "night in tower", "night in tomb");
		Variable varDec_Task_3 = new Variable("Dec Task 3", "climb", "swim");
		Variable varMarriage = new Variable("Marriage", "no", "yes");
		Variable varWar = new Variable("War", "no", "yes");
		Variable varRetire = new Variable("Retire", "no", "yes");
		Variable varU1 = new Variable("U1");
		Variable varU2 = new Variable("U2");
		Variable varU3 = new Variable("U3");
		Variable varcost_task_1 = new Variable("cost task 1");
		Variable varcost_task_2 = new Variable("cost task 2");
		Variable varcost_task_3 = new Variable("cost task 3");
		Variable varcost_marriage = new Variable("cost marriage");
		Variable varcost_war = new Variable("cost war");
		Variable varcost_retirement = new Variable("cost retirement");

		// Nodes
		Node nodeResult_1 = probNet.addNode(varResult_1, NodeType.CHANCE);
		Node nodeResult_2 = probNet.addNode(varResult_2, NodeType.CHANCE);
		Node nodeResult_3 = probNet.addNode(varResult_3, NodeType.CHANCE);
		Node nodeQuality_of_wooer = probNet.addNode(varQuality_of_wooer, NodeType.CHANCE);
		Node nodeWedding = probNet.addNode(varWedding, NodeType.CHANCE);
		Node nodeQuality_of_general = probNet.addNode(varQuality_of_general, NodeType.CHANCE);
		Node nodeWealth = probNet.addNode(varWealth, NodeType.CHANCE);
		Node nodeOffspring = probNet.addNode(varOffspring, NodeType.CHANCE);
		Node nodeNoble_descent = probNet.addNode(varNoble_descent, NodeType.CHANCE);
		Node nodeTask_1 = probNet.addNode(varTask_1, NodeType.DECISION);
		Node nodeTask_2 = probNet.addNode(varTask_2, NodeType.DECISION);
		Node nodeDec_Task_3 = probNet.addNode(varDec_Task_3, NodeType.DECISION);
		Node nodeMarriage = probNet.addNode(varMarriage, NodeType.DECISION);
		Node nodeWar = probNet.addNode(varWar, NodeType.DECISION);
		Node nodeRetire = probNet.addNode(varRetire, NodeType.DECISION);
		Node nodeU1 = probNet.addNode(varU1, NodeType.UTILITY);
		Node nodeU2 = probNet.addNode(varU2, NodeType.UTILITY);
		Node nodeU3 = probNet.addNode(varU3, NodeType.UTILITY);
		Node nodecost_task_1 = probNet.addNode(varcost_task_1, NodeType.UTILITY);
		Node nodecost_task_2 = probNet.addNode(varcost_task_2, NodeType.UTILITY);
		Node nodecost_task_3 = probNet.addNode(varcost_task_3, NodeType.UTILITY);
		Node nodecost_marriage = probNet.addNode(varcost_marriage, NodeType.UTILITY);
		Node nodecost_war = probNet.addNode(varcost_war, NodeType.UTILITY);
		Node nodecost_retirement = probNet.addNode(varcost_retirement, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeQuality_of_wooer, nodeQuality_of_general, true);
		probNet.addLink(nodeQuality_of_wooer, nodeOffspring, true);
		probNet.addLink(nodeQuality_of_wooer, nodeU1, true);
		probNet.addLink(nodeQuality_of_wooer, nodeResult_1, true);
		probNet.addLink(nodeQuality_of_wooer, nodeResult_2, true);
		probNet.addLink(nodeQuality_of_wooer, nodeResult_3, true);
		probNet.addLink(nodeWedding, nodeQuality_of_general, true);
		probNet.addLink(nodeWedding, nodeOffspring, true);
		probNet.addLink(nodeQuality_of_general, nodeWealth, true);
		probNet.addLink(nodeWealth, nodeU3, true);
		probNet.addLink(nodeOffspring, nodeU2, true);
		probNet.addLink(nodeOffspring, nodeWealth, true);
		probNet.addLink(nodeNoble_descent, nodeU2, true);
		probNet.addLink(nodeTask_1, nodecost_task_1, true);
		probNet.addLink(nodeTask_1, nodeMarriage, true);
		probNet.addLink(nodeTask_1, nodeResult_1, true);
		probNet.addLink(nodeTask_2, nodecost_task_2, true);
		probNet.addLink(nodeTask_2, nodeMarriage, true);
		probNet.addLink(nodeTask_2, nodeResult_2, true);
		probNet.addLink(nodeDec_Task_3, nodecost_task_3, true);
		probNet.addLink(nodeDec_Task_3, nodeMarriage, true);
		probNet.addLink(nodeDec_Task_3, nodeResult_3, true);
		probNet.addLink(nodeMarriage, nodeWedding, true);
		probNet.addLink(nodeMarriage, nodecost_marriage, true);
		probNet.addLink(nodeMarriage, nodeWar, true);
		probNet.addLink(nodeWar, nodeU3, true);
		probNet.addLink(nodeWar, nodeWealth, true);
		probNet.addLink(nodeWar, nodeRetire, true);
		probNet.addLink(nodeWar, nodecost_war, true);
		probNet.addLink(nodeRetire, nodeU2, true);
		probNet.addLink(nodeRetire, nodeU3, true);
		probNet.addLink(nodeRetire, nodecost_retirement, true);

		// Potentials
		TablePotential potResult_1 = new TablePotential(Arrays.asList(varResult_1, varQuality_of_wooer, varTask_1),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_1.setValues(new double[] { 0.97, 0.03, 0.75, 0.25, 0.75, 0.25, 0.25, 0.75 });
		nodeResult_1.setPotential(potResult_1);

		TablePotential potResult_2 = new TablePotential(Arrays.asList(varResult_2, varQuality_of_wooer, varTask_2),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_2.setValues(new double[] { 0.9, 0.1, 0.2, 0.8, 0.75, 0.25, 0.25, 0.75 });
		nodeResult_2.setPotential(potResult_2);

		TablePotential potResult_3 = new TablePotential(Arrays.asList(varResult_3, varQuality_of_wooer, varDec_Task_3),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_3.setValues(new double[] { 0.6, 0.4, 0.4, 0.6, 0.55, 0.45, 0.45, 0.55 });
		nodeResult_3.setPotential(potResult_3);

		UniformPotential potQuality_of_wooer = new UniformPotential(Arrays.asList(varQuality_of_wooer),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeQuality_of_wooer.setPotential(potQuality_of_wooer);

		TablePotential potWedding = new TablePotential(Arrays.asList(varWedding, varMarriage),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potWedding.setValues(new double[] { 1, 0, 0, 1 });
		nodeWedding.setPotential(potWedding);

		UniformPotential potQuality_of_general = new UniformPotential(
				Arrays.asList(varQuality_of_general, varQuality_of_wooer, varWedding),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeQuality_of_general.setPotential(potQuality_of_general);

		TablePotential potWealth = new TablePotential(
				Arrays.asList(varWealth, varQuality_of_general, varWar, varOffspring),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potWealth.setValues(new double[] { 0.5, 0.5, 0.5, 0.5, 0.85, 0.15, 0.15, 0.85, 0.6, 0.4, 0.6, 0.4, 0.9, 0.1, 0.2,
				0.8 });
		nodeWealth.setPotential(potWealth);

		TablePotential potOffspring = new TablePotential(Arrays.asList(varOffspring, varQuality_of_wooer, varWedding),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potOffspring.setValues(new double[] { 1, 0, 1, 0, 0.6, 0.4, 0.2, 0.8 });
		nodeOffspring.setPotential(potOffspring);

		UniformPotential potNoble_descent = new UniformPotential(Arrays.asList(varNoble_descent),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeNoble_descent.setPotential(potNoble_descent);

		UniformPotential potU1 = new UniformPotential(Arrays.asList(varU1, varQuality_of_wooer),
				PotentialRole.UNSPECIFIED);
		nodeU1.setPotential(potU1);

		ExactDistrPotential potU2 = new ExactDistrPotential(
				Arrays.asList(varU2, varNoble_descent, varOffspring, varRetire));
		potU2.getTablePotential().setValues(new double[] { 0, 0, 2, 3, 1, 3, 4, 7 });
		nodeU2.setPotential(potU2);

		ExactDistrPotential potU3 = new ExactDistrPotential(Arrays.asList(varU3, varRetire, varWar, varWealth));
		potU3.getTablePotential().setValues(new double[] { 0, 1, 2, 3, 6, 8, 7, 10 });
		nodeU3.setPotential(potU3);

		ExactDistrPotential potcost_task_1 = new ExactDistrPotential(Arrays.asList(varcost_task_1, varTask_1));
		potcost_task_1.getTablePotential().setValues(new double[] { -1, -0.2 });
		nodecost_task_1.setPotential(potcost_task_1);

		ExactDistrPotential potcost_task_2 = new ExactDistrPotential(Arrays.asList(varcost_task_2, varTask_2));
		potcost_task_2.getTablePotential().setValues(new double[] { -0.5, -0.2 });
		nodecost_task_2.setPotential(potcost_task_2);

		ExactDistrPotential potcost_task_3 = new ExactDistrPotential(Arrays.asList(varcost_task_3, varDec_Task_3));
		potcost_task_3.getTablePotential().setValues(new double[] { -0.1, -0.05 });
		nodecost_task_3.setPotential(potcost_task_3);

		UniformPotential potcost_marriage = new UniformPotential(Arrays.asList(varcost_marriage, varMarriage),
				PotentialRole.UNSPECIFIED);
		nodecost_marriage.setPotential(potcost_marriage);

		ExactDistrPotential potcost_war = new ExactDistrPotential(Arrays.asList(varcost_war, varWar));
		potcost_war.getTablePotential().setValues(new double[] { 0, -5 });
		nodecost_war.setPotential(potcost_war);

		UniformPotential potcost_retirement = new UniformPotential(Arrays.asList(varcost_retirement, varRetire),
				PotentialRole.UNSPECIFIED);
		nodecost_retirement.setPotential(potcost_retirement);

		// Link<Node> restrictions and revealing states
		Link<Node> link_nodeWedding_nodeOffspring = probNet.getLink(nodeWedding, nodeOffspring, true);
		link_nodeWedding_nodeOffspring.initializesRestrictionsPotential();
		TablePotential restrictions_nodeWedding_nodeOffspring = (TablePotential) link_nodeWedding_nodeOffspring
				.getRestrictionsPotential();
		restrictions_nodeWedding_nodeOffspring.setValues(new double[] { 1, 1, 0, 1 });
		link_nodeWedding_nodeOffspring
				.setRevealingStates(Arrays.asList(varWedding.getStates()[1], varWedding.getStates()[0]));

		Link<Node> link_nodeTask_1_nodeResult_1 = probNet.getLink(nodeTask_1, nodeResult_1, true);
		link_nodeTask_1_nodeResult_1
				.setRevealingStates(Arrays.asList(varTask_1.getStates()[1], varTask_1.getStates()[0]));

		Link<Node> link_nodeTask_2_nodeResult_2 = probNet.getLink(nodeTask_2, nodeResult_2, true);
		link_nodeTask_2_nodeResult_2
				.setRevealingStates(Arrays.asList(varTask_2.getStates()[1], varTask_2.getStates()[0]));

		Link<Node> link_nodeDec_Task_3_nodeResult_3 = probNet.getLink(nodeDec_Task_3, nodeResult_3, true);
		link_nodeDec_Task_3_nodeResult_3
				.setRevealingStates(Arrays.asList(varDec_Task_3.getStates()[1], varDec_Task_3.getStates()[0]));

		Link<Node> link_nodeMarriage_nodeWedding = probNet.getLink(nodeMarriage, nodeWedding, true);
		link_nodeMarriage_nodeWedding.initializesRestrictionsPotential();
		TablePotential restrictions_nodeMarriage_nodeWedding = (TablePotential) link_nodeMarriage_nodeWedding
				.getRestrictionsPotential();
		restrictions_nodeMarriage_nodeWedding.setValues(new double[] { 1, 0, 0, 1 });
		link_nodeMarriage_nodeWedding
				.setRevealingStates(Arrays.asList(varMarriage.getStates()[1], varMarriage.getStates()[0]));

		Link<Node> link_nodeWar_nodeWealth = probNet.getLink(nodeWar, nodeWealth, true);
		link_nodeWar_nodeWealth.setRevealingStates(Arrays.asList(varWar.getStates()[1], varWar.getStates()[0]));

		// Always observed nodes
		nodeNoble_descent.setAlwaysObserved(true);

		return probNet;
	}

	public static ProbNet buildDiabetesDAN() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		probNet.setName(NamesNetworks.DIABETES_DAN.toString());
		// Variables
		Variable varUrine_test_result = new Variable("Urine test result", "negative", "positive");
		Variable varSymptom = new Variable("Symptom", "absent", "present");
		Variable varDiabetes = new Variable("Diabetes", "absent", "present");
		Variable varBlood_test_result = new Variable("Blood test result", "negative", "positive");
		Variable varDec_Blood_Test = new Variable("Dec: Blood Test", "no", "yes");
		Variable varDec_Urine_test = new Variable("Dec: Urine test", "no", "yes");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varCost_of_blood_test = new Variable("Cost of blood test");
		Variable varCost_of_urine_test = new Variable("Cost of urine test");
		Variable varQuality_of_life = new Variable("Quality of life");

		// Nodes
		Node nodeUrine_test_result = probNet.addNode(varUrine_test_result, NodeType.CHANCE);
		Node nodeSymptom = probNet.addNode(varSymptom, NodeType.CHANCE);
		Node nodeDiabetes = probNet.addNode(varDiabetes, NodeType.CHANCE);
		Node nodeBlood_test_result = probNet.addNode(varBlood_test_result, NodeType.CHANCE);
		Node nodeDec_Blood_Test = probNet.addNode(varDec_Blood_Test, NodeType.DECISION);
		Node nodeDec_Urine_test = probNet.addNode(varDec_Urine_test, NodeType.DECISION);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeCost_of_blood_test = probNet.addNode(varCost_of_blood_test, NodeType.UTILITY);
		Node nodeCost_of_urine_test = probNet.addNode(varCost_of_urine_test, NodeType.UTILITY);
		Node nodeQuality_of_life = probNet.addNode(varQuality_of_life, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDiabetes, nodeSymptom, true);
		probNet.addLink(nodeDiabetes, nodeUrine_test_result, true);
		probNet.addLink(nodeDiabetes, nodeBlood_test_result, true);
		probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		probNet.addLink(nodeDec_Blood_Test, nodeBlood_test_result, true);
		probNet.addLink(nodeDec_Blood_Test, nodeTherapy, true);
		probNet.addLink(nodeDec_Blood_Test, nodeCost_of_blood_test, true);
		probNet.addLink(nodeDec_Urine_test, nodeUrine_test_result, true);
		probNet.addLink(nodeDec_Urine_test, nodeTherapy, true);
		probNet.addLink(nodeDec_Urine_test, nodeCost_of_urine_test, true);
		probNet.addLink(nodeTherapy, nodeQuality_of_life, true);

		// Potentials
		TablePotential potUrine_test_result = new TablePotential(
				Arrays.asList(varUrine_test_result, varDec_Urine_test, varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potUrine_test_result.setValues(new double[] { 0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97 });
		nodeUrine_test_result.setPotential(potUrine_test_result);

		TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potSymptom.setValues(new double[] { 0.999, 0.001, 0.15, 0.85 });
		nodeSymptom.setPotential(potSymptom);

		TablePotential potDiabetes = new TablePotential(Arrays.asList(varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDiabetes.setValues(new double[] { 0.93, 0.07 });
		nodeDiabetes.setPotential(potDiabetes);

		TablePotential potBlood_test_result = new TablePotential(
				Arrays.asList(varBlood_test_result, varDec_Blood_Test, varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potBlood_test_result.setValues(new double[] { 0, 0, 0.98, 0.02, 0, 0, 0.04, 0.96 });
		nodeBlood_test_result.setPotential(potBlood_test_result);

		ExactDistrPotential potCost_of_blood_test = new ExactDistrPotential(
				Arrays.asList(varCost_of_blood_test, varDec_Blood_Test));
		potCost_of_blood_test.getTablePotential().setValues(new double[] { 0, -0.05 });
		nodeCost_of_blood_test.setPotential(potCost_of_blood_test);

		ExactDistrPotential potCost_of_urine_test = new ExactDistrPotential(
				Arrays.asList(varCost_of_urine_test, varDec_Urine_test));
		potCost_of_urine_test.getTablePotential().setValues(new double[] { 0, -0.03 });
		nodeCost_of_urine_test.setPotential(potCost_of_urine_test);

		ExactDistrPotential potQuality_of_life = new ExactDistrPotential(
				Arrays.asList(varQuality_of_life, varDiabetes, varTherapy));
		potQuality_of_life.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeQuality_of_life.setPotential(potQuality_of_life);

		// Link<Node> restrictions and revealing states
		Link<Node> link_nodeDec_Blood_Test_nodeBlood_test_result = probNet
				.getLink(nodeDec_Blood_Test, nodeBlood_test_result, true);
		link_nodeDec_Blood_Test_nodeBlood_test_result.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDec_Blood_Test_nodeBlood_test_result = (TablePotential) link_nodeDec_Blood_Test_nodeBlood_test_result
				.getRestrictionsPotential();
		restrictions_nodeDec_Blood_Test_nodeBlood_test_result.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDec_Blood_Test_nodeBlood_test_result
				.setRevealingStates(Arrays.asList(varDec_Blood_Test.getStates()[1]));

		Link<Node> link_nodeDec_Urine_test_nodeUrine_test_result = probNet
				.getLink(nodeDec_Urine_test, nodeUrine_test_result, true);
		link_nodeDec_Urine_test_nodeUrine_test_result.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDec_Urine_test_nodeUrine_test_result = (TablePotential) link_nodeDec_Urine_test_nodeUrine_test_result
				.getRestrictionsPotential();
		restrictions_nodeDec_Urine_test_nodeUrine_test_result.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDec_Urine_test_nodeUrine_test_result
				.setRevealingStates(Arrays.asList(varDec_Urine_test.getStates()[1]));

		nodeSymptom.setAlwaysObserved(true);

		return probNet;
	}

	public static ProbNet buildTwoTestDAN() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		probNet.setName(NamesNetworks.TWO_TEST_DAN.toString());
		// Variables
		Variable varDisease = new Variable("Disease", "absent", "present");
		Variable varR_T1 = new Variable("R T1", "negative", "positive");
		Variable varR_T2 = new Variable("R T2", "negative", "positive");
		Variable varT1 = new Variable("T1", "no", "yes");
		Variable varT2 = new Variable("T2", "no", "yes");
		Variable varTh = new Variable("Th", "no", "yes");
		Variable varU = new Variable("U");
		Variable varU1 = new Variable("U1");
		Variable varU2 = new Variable("U2");

		// Nodes
		Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
		Node nodeR_T1 = probNet.addNode(varR_T1, NodeType.CHANCE);
		Node nodeR_T2 = probNet.addNode(varR_T2, NodeType.CHANCE);
		Node nodeT1 = probNet.addNode(varT1, NodeType.DECISION);
		Node nodeT2 = probNet.addNode(varT2, NodeType.DECISION);
		Node nodeTh = probNet.addNode(varTh, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);
		Node nodeU1 = probNet.addNode(varU1, NodeType.UTILITY);
		Node nodeU2 = probNet.addNode(varU2, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDisease, nodeR_T1, true);
		probNet.addLink(nodeDisease, nodeR_T2, true);
		probNet.addLink(nodeDisease, nodeU, true);
		probNet.addLink(nodeT1, nodeR_T1, true);
		probNet.addLink(nodeT1, nodeU1, true);
		probNet.addLink(nodeT1, nodeTh, true);
		probNet.addLink(nodeT2, nodeR_T2, true);
		probNet.addLink(nodeT2, nodeU2, true);
		probNet.addLink(nodeT2, nodeTh, true);
		probNet.addLink(nodeTh, nodeU, true);

		// Potentials
		TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDisease.setValues(new double[] { 0.85, 0.15 });
		nodeDisease.setPotential(potDisease);

		TablePotential potR_T1 = new TablePotential(Arrays.asList(varR_T1, varDisease, varT1),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potR_T1.setValues(new double[] { 0, 0, 0, 0, 0.85, 0.15, 0.1, 0.9 });
		nodeR_T1.setPotential(potR_T1);

		TablePotential potR_T2 = new TablePotential(Arrays.asList(varR_T2, varDisease, varT2),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potR_T2.setValues(new double[] { 0, 0, 0, 0, 0.99, 0.01, 0.005, 0.995 });
		nodeR_T2.setPotential(potR_T2);

		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varTh, varDisease));
		potU.getTablePotential().setValues(new double[] { 10, 8, 2, 7 });
		nodeU.setPotential(potU);

		ExactDistrPotential potU1 = new ExactDistrPotential(Arrays.asList(varU1, varT1));
		potU1.getTablePotential().setValues(new double[] { 0, -0.05 });
		nodeU1.setPotential(potU1);

		ExactDistrPotential potU2 = new ExactDistrPotential(Arrays.asList(varU2, varT2));
		potU2.getTablePotential().setValues(new double[] { 0, -0.33 });
		nodeU2.setPotential(potU2);

		// Link<Node> restrictions and revealing states
		Link<Node> link_nodeT1_nodeR_T1 = probNet.getLink(nodeT1, nodeR_T1, true);
		link_nodeT1_nodeR_T1.initializesRestrictionsPotential();
		TablePotential restrictions_nodeT1_nodeR_T1 = (TablePotential) link_nodeT1_nodeR_T1.getRestrictionsPotential();
		restrictions_nodeT1_nodeR_T1.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeT1_nodeR_T1.setRevealingStates(Arrays.asList(varT1.getStates()[1]));

		Link<Node> link_nodeT2_nodeR_T2 = probNet.getLink(nodeT2, nodeR_T2, true);
		link_nodeT2_nodeR_T2.initializesRestrictionsPotential();
		TablePotential restrictions_nodeT2_nodeR_T2 = (TablePotential) link_nodeT2_nodeR_T2.getRestrictionsPotential();
		restrictions_nodeT2_nodeR_T2.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeT2_nodeR_T2.setRevealingStates(Arrays.asList(varT2.getStates()[1]));

		// Always observed nodes

		return probNet;
	}

	/**
	 * @return A DAN with 5 tests, similarly to the diabetes problem, but where the partial order of tests Ti is: {T0,T1}&lt;{T2}&lt;{T3,T4}.
	 * Thus, the SDAG has 3 phases of tests.
	 */
	public static ProbNet buildThreePhasesOfTestsDAN() {
		int numTests = 5;

		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables

		Variable varSymptom = new Variable("Symptom", "absent", "present");
		Variable varDiabetes = new Variable("Diabetes", "absent", "present");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varQuality_of_life = new Variable("Quality of life");
        Variable[] varDec_Test = new Variable[numTests];
        Variable[] varTest_Result = new Variable[numTests];
        Variable[] varCost_of_Test = new Variable[numTests];
		for (int i = 0; i < numTests; i++) {
			varDec_Test[i] = new Variable("Dec: Test " + i, "no", "yes");
			varTest_Result[i] = new Variable("Test Result " + i, "negative", "positive");
			varCost_of_Test[i] = new Variable("Cost of test " + i);
		}

		// Nodes
		Node nodeSymptom = probNet.addNode(varSymptom, NodeType.CHANCE);
		Node nodeDiabetes = probNet.addNode(varDiabetes, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeQuality_of_life = probNet.addNode(varQuality_of_life, NodeType.UTILITY);
        Node[] nodeDecTest = new Node[numTests];
        Node[] nodeTestResult = new Node[numTests];
        Node[] nodeCostOfTest = new Node[numTests];
		for (int i = 0; i < numTests; i++) {
			nodeTestResult[i] = probNet.addNode(varTest_Result[i], NodeType.CHANCE);
			nodeDecTest[i] = probNet.addNode(varDec_Test[i], NodeType.DECISION);
			nodeCostOfTest[i] = probNet.addNode(varTest_Result[i], NodeType.UTILITY);
		}

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDiabetes, nodeSymptom, true);
		for (int i = 0; i < numTests; i++) {
			probNet.addLink(nodeDiabetes, nodeTestResult[i], true);
			probNet.addLink(nodeDecTest[i], nodeTestResult[i], true);
			probNet.addLink(nodeDecTest[i], nodeCostOfTest[i], true);
		}
		probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		probNet.addLink(nodeTherapy, nodeQuality_of_life, true);
		//Create the three phases of tests
		for (int i = 0; i < 2; i++) {
			for (int j = 2; j < 5; j++) {
				probNet.addLink(nodeDecTest[i], nodeDecTest[j], true);
			}
		}
		for (int j = 3; j < 5; j++) {
			probNet.addLink(nodeDecTest[2], nodeDecTest[j], true);
		}

		//TODO Assign different numbers to potentials.
		// Potentials for test results
        TablePotential[] potentialTestResult = new TablePotential[numTests];
		for (int i = 0; i < numTests; i++) {
			potentialTestResult[i] = new TablePotential(Arrays.asList(varTest_Result[i], varDec_Test[i], varDiabetes),
					PotentialRole.CONDITIONAL_PROBABILITY);
			potentialTestResult[i].setValues(new double[] { 0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97 });
			nodeTestResult[i].setPotential(potentialTestResult[i]);
		}

		//TODO Assign different numbers to potentials.
		//Potentials for costs of tests
        ExactDistrPotential[] potentialCostOfTest = new ExactDistrPotential[numTests];
		for (int i = 0; i < numTests; i++) {
			List<Variable> variableList = new ArrayList<>();
			variableList.add(varCost_of_Test[i]);
			variableList.addAll(Arrays.asList(varDec_Test));
			potentialCostOfTest[i] = new ExactDistrPotential(variableList);
			potentialCostOfTest[i].getTablePotential().setValues(new double[] { 0, 50 });
			nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		}

		ExactDistrPotential potQuality_of_life = new ExactDistrPotential(
				Arrays.asList(varQuality_of_life, varDiabetes, varTherapy));
		potQuality_of_life.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeQuality_of_life.setPotential(potQuality_of_life);

		// Link<Node> restrictions and revealing states
		for (int i = 0; i < numTests; i++) {
			Link<Node> link_Dec_To_Test_Result = probNet.getLink(nodeDecTest[i], nodeTestResult[i], true);
			link_Dec_To_Test_Result.initializesRestrictionsPotential();
			TablePotential restrictions_nodeDec_Test_node_test_result = (TablePotential) link_Dec_To_Test_Result
					.getRestrictionsPotential();
			restrictions_nodeDec_Test_node_test_result.setValues(new double[] { 0, 1, 0, 1 });
			link_Dec_To_Test_Result.setRevealingStates(Arrays.asList(varDec_Test[i].getStates()[1]));
		}

		nodeSymptom.setAlwaysObserved(true);

		return probNet;
	}

	/**
	 * @return A DAN with 2*numTestsPerPhase tests, similarly to the diabetes problem, but where the partial order of tests Ti is: {T0,T1,...,Tn-1}&lt;{Tn,Tn+1,T2n-1}.
	 */
	public static ProbNet buildTwoPhasesOfTestsDAN(int numTestsPerPhase) {

		int numTests = numTestsPerPhase * 2;

		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables

		Variable varSymptom = new Variable("Symptom", "absent", "present");
		Variable varDiabetes = new Variable("Diabetes", "absent", "present");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varQuality_of_life = new Variable("Quality of life");
        Variable[] varDec_Test = new Variable[numTests];
        Variable[] varTest_Result = new Variable[numTests];
        Variable[] varCost_of_Test = new Variable[numTests];
		for (int i = 0; i < numTests; i++) {
			varDec_Test[i] = new Variable("Dec: Test " + i, "no", "yes");
			varTest_Result[i] = new Variable("Test Result " + i, "negative", "positive");
			varCost_of_Test[i] = new Variable("Cost of test " + i);
		}

		// Nodes
		Node nodeSymptom = probNet.addNode(varSymptom, NodeType.CHANCE);
		Node nodeDiabetes = probNet.addNode(varDiabetes, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeQuality_of_life = probNet.addNode(varQuality_of_life, NodeType.UTILITY);
        Node[] nodeDecTest = new Node[numTests];
        Node[] nodeTestResult = new Node[numTests];
        Node[] nodeCostOfTest = new Node[numTests];
		for (int i = 0; i < numTests; i++) {
			nodeTestResult[i] = probNet.addNode(varTest_Result[i], NodeType.CHANCE);
			nodeDecTest[i] = probNet.addNode(varDec_Test[i], NodeType.DECISION);
			nodeCostOfTest[i] = probNet.addNode(varCost_of_Test[i], NodeType.UTILITY);
		}

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDiabetes, nodeSymptom, true);
		for (int i = 0; i < numTests; i++) {
			probNet.addLink(nodeDiabetes, nodeTestResult[i], true);
			probNet.addLink(nodeDecTest[i], nodeTestResult[i], true);
			probNet.addLink(nodeDecTest[i], nodeCostOfTest[i], true);
		}
		probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		probNet.addLink(nodeTherapy, nodeQuality_of_life, true);
		//Create the three phases of tests
		for (int i = 0; i < numTests / 2; i++) {
			for (int j = numTests / 2; j < numTests; j++) {
				probNet.addLink(nodeDecTest[i], nodeDecTest[j], true);
			}
		}

		//TODO Assign different numbers to potentials.
		// Potentials for test results
        TablePotential[] potentialTestResult = new TablePotential[numTests];
		for (int i = 0; i < numTests; i++) {
			potentialTestResult[i] = new TablePotential(Arrays.asList(varTest_Result[i], varDec_Test[i], varDiabetes),
					PotentialRole.CONDITIONAL_PROBABILITY);
			potentialTestResult[i].setValues(new double[] { 0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97 });
			nodeTestResult[i].setPotential(potentialTestResult[i]);
		}

		//TODO Assign different numbers to potentials.
		//Potentials for costs of tests
        ExactDistrPotential[] potentialCostOfTest = new ExactDistrPotential[numTests];
		for (int i = 0; i < numTests; i++) {
			potentialCostOfTest[i] = new ExactDistrPotential(Arrays.asList(varCost_of_Test[i], varDec_Test[i]));
			potentialCostOfTest[i].getTablePotential().setValues(new double[] { 0, -0.5 });
			nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		}

		ExactDistrPotential potQuality_of_life = new ExactDistrPotential(
				Arrays.asList(varQuality_of_life, varDiabetes, varTherapy));
		potQuality_of_life.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeQuality_of_life.setPotential(potQuality_of_life);

		TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potSymptom.setValues(new double[] { 0.999, 0.001, 0.15, 0.85 });
		nodeSymptom.setPotential(potSymptom);

		TablePotential potDiabetes = new TablePotential(Arrays.asList(varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDiabetes.setValues(new double[] { 0.93, 0.07 });
		nodeDiabetes.setPotential(potDiabetes);

		// Link<Node> restrictions and revealing states
		for (int i = 0; i < numTests; i++) {
			Link<Node> link_Dec_To_Test_Result = probNet.getLink(nodeDecTest[i], nodeTestResult[i], true);
			link_Dec_To_Test_Result.initializesRestrictionsPotential();
			TablePotential restrictions_nodeDec_Test_node_test_result = (TablePotential) link_Dec_To_Test_Result
					.getRestrictionsPotential();
			restrictions_nodeDec_Test_node_test_result.setValues(new double[] { 0, 1, 0, 1 });
			link_Dec_To_Test_Result.setRevealingStates(Arrays.asList(varDec_Test[i].getStates()[1]));
		}

		nodeSymptom.setAlwaysObserved(true);

		return probNet;
	}

	/**
	 * @return A DAN with n tests. The diabetes problem is an instance where n=2.
	 */
	public static ProbNet buildNTestsDAN(int numTests) {

		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		probNet.setName(NamesNetworks.N_TESTS.toString());
		// Variables

		Variable varSymptom = new Variable("Symptom", "absent", "present");
		Variable varDiabetes = new Variable("Disease", "absent", "present");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varQuality_of_life = new Variable("Quality of life");
        Variable[] varDec_Test = new Variable[numTests];
        Variable[] varTest_Result = new Variable[numTests];
        Variable[] varCost_of_Test = new Variable[numTests];
		for (int i = 0; i < numTests; i++) {
			varDec_Test[i] = new Variable("Dec: Test " + i, "no", "yes");
			varTest_Result[i] = new Variable("Test Result " + i, "negative", "positive");
			varCost_of_Test[i] = new Variable("Cost of test " + i);
		}

		// Nodes
		Node nodeSymptom = probNet.addNode(varSymptom, NodeType.CHANCE);
		Node nodeDiabetes = probNet.addNode(varDiabetes, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeQuality_of_life = probNet.addNode(varQuality_of_life, NodeType.UTILITY);
        Node[] nodeDecTest = new Node[numTests];
        Node[] nodeTestResult = new Node[numTests];
        Node[] nodeCostOfTest = new Node[numTests];
		for (int i = 0; i < numTests; i++) {
			nodeTestResult[i] = probNet.addNode(varTest_Result[i], NodeType.CHANCE);
			nodeDecTest[i] = probNet.addNode(varDec_Test[i], NodeType.DECISION);
			nodeCostOfTest[i] = probNet.addNode(varCost_of_Test[i], NodeType.UTILITY);
		}

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDiabetes, nodeSymptom, true);
		for (int i = 0; i < numTests; i++) {
			probNet.addLink(nodeDiabetes, nodeTestResult[i], true);
			probNet.addLink(nodeDecTest[i], nodeTestResult[i], true);
			probNet.addLink(nodeDecTest[i], nodeCostOfTest[i], true);
		}
		probNet.addLink(nodeDiabetes, nodeQuality_of_life, true);
		probNet.addLink(nodeTherapy, nodeQuality_of_life, true);

		//TODO Assign different numbers to potentials.
		// Potentials for test results
        TablePotential[] potentialTestResult = new TablePotential[numTests];
		for (int i = 0; i < numTests; i++) {
			potentialTestResult[i] = new TablePotential(Arrays.asList(varTest_Result[i], varDec_Test[i], varDiabetes),
					PotentialRole.CONDITIONAL_PROBABILITY);
			potentialTestResult[i].setValues(new double[] { 0, 0, 0.99, 0.01, 0, 0, 0.03, 0.97 });
			nodeTestResult[i].setPotential(potentialTestResult[i]);
		}

		//TODO Assign different numbers to potentials.
		//Potentials for costs of tests
        ExactDistrPotential[] potentialCostOfTest = new ExactDistrPotential[numTests];
		for (int i = 0; i < numTests; i++) {
			potentialCostOfTest[i] = new ExactDistrPotential(Arrays.asList(varCost_of_Test[i], varDec_Test[i]));
			potentialCostOfTest[i].getTablePotential().setValues(new double[] { 0, -0.5 });
			nodeCostOfTest[i].setPotential(potentialCostOfTest[i]);
		}

		ExactDistrPotential potQuality_of_life = new ExactDistrPotential(
				Arrays.asList(varQuality_of_life, varDiabetes, varTherapy));
		potQuality_of_life.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeQuality_of_life.setPotential(potQuality_of_life);

		TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potSymptom.setValues(new double[] { 0.999, 0.001, 0.15, 0.85 });
		nodeSymptom.setPotential(potSymptom);

		TablePotential potDiabetes = new TablePotential(Arrays.asList(varDiabetes),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDiabetes.setValues(new double[] { 0.93, 0.07 });
		nodeDiabetes.setPotential(potDiabetes);

		// Link<Node> restrictions and revealing states
		for (int i = 0; i < numTests; i++) {
			Link<Node> link_Dec_To_Test_Result = probNet.getLink(nodeDecTest[i], nodeTestResult[i], true);
			link_Dec_To_Test_Result.initializesRestrictionsPotential();
			TablePotential restrictions_nodeDec_Test_node_test_result = (TablePotential) link_Dec_To_Test_Result
					.getRestrictionsPotential();
			restrictions_nodeDec_Test_node_test_result.setValues(new double[] { 0, 1, 0, 1 });
			link_Dec_To_Test_Result.setRevealingStates(Arrays.asList(varDec_Test[i].getStates()[1]));
		}

		nodeSymptom.setAlwaysObserved(true);

		return probNet;
	}

	public static ProbNet buildMediastinetDAN() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());

		probNet.setName(NamesNetworks.MEDIASTINET_DAN.toString());
		// Variables
		Variable varN2_N3 = new Variable("N2 N3", "negative", "positive");
		Variable varCT_scan = new Variable("CT scan", "negative", "positive");
		Variable varTBNA = new Variable("TBNA", "negative", "positive");
		Variable varPET = new Variable("PET", "no result", "negative", "positive");
		Variable varEBUS = new Variable("EBUS", "negative", "positive");
		Variable varEUS = new Variable("EUS", "negative", "positive");
		Variable varMED = new Variable("MED", "negative", "positive");
		Variable varMED_Sv = new Variable("MED Sv", "no", "yes");
		Variable varDecTBNA = new Variable("Dec:TBNA", "no", "yes");
		Variable varDecPET = new Variable("Dec:PET", "no", "yes");
		Variable varDecMED = new Variable("Dec:MED", "no", "yes");
		Variable varTreatment = new Variable("Treatment", "palliative", "chemotherapy", "thoracotomy");
		Variable varDecEBUS = new Variable("Dec:EBUS", "no", "yes");
		Variable varDecEUS = new Variable("Dec:EUS", "no", "yes");
		Variable varSurvivors_QALE = new Variable("Survivors QALE");
		Variable varInmediate_Survival = new Variable("Inmediate Survival");
		Variable varMED_Survival = new Variable("MED Survival");
		Variable varNet_QALE = new Variable("Net QALE");
		Variable varTBNA_Morbidity = new Variable("TBNA Morbidity");
		Variable varMED_Morbidity = new Variable("MED Morbidity");
		Variable varEUS_Morbidity = new Variable("EUS Morbidity");
		Variable varEBUS_Morbidity = new Variable("EBUS Morbidity");
		Variable varTotal_QALE = new Variable("Total QALE");
		Variable varCostCT_scan = new Variable("Cost:CT scan");
		Variable varCostTBNA = new Variable("Cost:TBNA");
		Variable varCostEBUS = new Variable("Cost:EBUS");
		Variable varCostEUS = new Variable("Cost:EUS");
		Variable varCostMED = new Variable("Cost:MED");
		Variable varCostPET = new Variable("Cost:PET");
		Variable varCostTreatment = new Variable("Cost:Treatment");
		Variable varTotal_Economic_Cost = new Variable("Total Economic Cost");
		Variable varC2E = new Variable("C2E");
		Variable varWeighted_Economic_Cost = new Variable("Weighted Economic Cost");
		Variable varNet_Effectiveness = new Variable("Net Effectiveness");

		// Nodes
		Node nodeN2_N3 = probNet.addNode(varN2_N3, NodeType.CHANCE);
		Node nodeCT_scan = probNet.addNode(varCT_scan, NodeType.CHANCE);
		Node nodeTBNA = probNet.addNode(varTBNA, NodeType.CHANCE);
		Node nodePET = probNet.addNode(varPET, NodeType.CHANCE);
		Node nodeEBUS = probNet.addNode(varEBUS, NodeType.CHANCE);
		Node nodeEUS = probNet.addNode(varEUS, NodeType.CHANCE);
		Node nodeMED = probNet.addNode(varMED, NodeType.CHANCE);
		Node nodeMED_Sv = probNet.addNode(varMED_Sv, NodeType.CHANCE);
		Node nodeDecTBNA = probNet.addNode(varDecTBNA, NodeType.DECISION);
		Node nodeDecPET = probNet.addNode(varDecPET, NodeType.DECISION);
		Node nodeDecMED = probNet.addNode(varDecMED, NodeType.DECISION);
		Node nodeTreatment = probNet.addNode(varTreatment, NodeType.DECISION);
		Node nodeDecEBUS = probNet.addNode(varDecEBUS, NodeType.DECISION);
		Node nodeDecEUS = probNet.addNode(varDecEUS, NodeType.DECISION);
		Node nodeSurvivors_QALE = probNet.addNode(varSurvivors_QALE, NodeType.UTILITY);
		Node nodeInmediate_Survival = probNet.addNode(varInmediate_Survival, NodeType.UTILITY);
		Node nodeMED_Survival = probNet.addNode(varMED_Survival, NodeType.UTILITY);
		Node nodeNet_QALE = probNet.addNode(varNet_QALE, NodeType.UTILITY);
		Node nodeTBNA_Morbidity = probNet.addNode(varTBNA_Morbidity, NodeType.UTILITY);
		Node nodeMED_Morbidity = probNet.addNode(varMED_Morbidity, NodeType.UTILITY);
		Node nodeEUS_Morbidity = probNet.addNode(varEUS_Morbidity, NodeType.UTILITY);
		Node nodeEBUS_Morbidity = probNet.addNode(varEBUS_Morbidity, NodeType.UTILITY);
		Node nodeTotal_QALE = probNet.addNode(varTotal_QALE, NodeType.UTILITY);
		Node nodeCostCT_scan = probNet.addNode(varCostCT_scan, NodeType.UTILITY);
		Node nodeCostTBNA = probNet.addNode(varCostTBNA, NodeType.UTILITY);
		Node nodeCostEBUS = probNet.addNode(varCostEBUS, NodeType.UTILITY);
		Node nodeCostEUS = probNet.addNode(varCostEUS, NodeType.UTILITY);
		Node nodeCostMED = probNet.addNode(varCostMED, NodeType.UTILITY);
		Node nodeCostPET = probNet.addNode(varCostPET, NodeType.UTILITY);
		Node nodeCostTreatment = probNet.addNode(varCostTreatment, NodeType.UTILITY);
		Node nodeTotal_Economic_Cost = probNet.addNode(varTotal_Economic_Cost, NodeType.UTILITY);
		Node nodeC2E = probNet.addNode(varC2E, NodeType.UTILITY);
		Node nodeWeighted_Economic_Cost = probNet.addNode(varWeighted_Economic_Cost, NodeType.UTILITY);
		Node nodeNet_Effectiveness = probNet.addNode(varNet_Effectiveness, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeN2_N3, nodeCT_scan, true);
		probNet.addLink(nodeN2_N3, nodeEBUS, true);
		probNet.addLink(nodeN2_N3, nodeEUS, true);
		probNet.addLink(nodeN2_N3, nodeMED, true);
		probNet.addLink(nodeN2_N3, nodePET, true);
		probNet.addLink(nodeN2_N3, nodeSurvivors_QALE, true);
		probNet.addLink(nodeN2_N3, nodeTBNA, true);
		probNet.addLink(nodeCT_scan, nodeEBUS, true);
		probNet.addLink(nodeCT_scan, nodeMED, true);
		probNet.addLink(nodeCT_scan, nodePET, true);
		probNet.addLink(nodeCT_scan, nodeTBNA, true);
		probNet.addLink(nodeCT_scan, nodeEUS, true);
		probNet.addLink(nodePET, nodeEBUS, true);
		probNet.addLink(nodePET, nodeEUS, true);
		probNet.addLink(nodePET, nodeMED, true);
		probNet.addLink(nodeMED_Sv, nodeMED_Morbidity, true);
		probNet.addLink(nodeMED_Sv, nodeMED_Survival, true);
		probNet.addLink(nodeDecTBNA, nodeCostTBNA, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA_Morbidity, true);
		probNet.addLink(nodeDecPET, nodeCostPET, true);
		probNet.addLink(nodeDecPET, nodePET, true);
		probNet.addLink(nodeDecPET, nodeDecEBUS, true);
		probNet.addLink(nodeDecPET, nodeDecEUS, true);
		probNet.addLink(nodeDecPET, nodeDecMED, true);
		probNet.addLink(nodeDecMED, nodeCostMED, true);
		probNet.addLink(nodeDecMED, nodeMED, true);
		probNet.addLink(nodeDecMED, nodeMED_Sv, true);
		probNet.addLink(nodeTreatment, nodeCostTreatment, true);
		probNet.addLink(nodeTreatment, nodeInmediate_Survival, true);
		probNet.addLink(nodeTreatment, nodeSurvivors_QALE, true);
		probNet.addLink(nodeDecEBUS, nodeEBUS, true);
		probNet.addLink(nodeDecEBUS, nodeCostEBUS, true);
		probNet.addLink(nodeDecEBUS, nodeEBUS_Morbidity, true);
		probNet.addLink(nodeDecEUS, nodeEUS, true);
		probNet.addLink(nodeDecEUS, nodeEUS_Morbidity, true);
		probNet.addLink(nodeDecEUS, nodeCostEUS, true);
		probNet.addLink(nodeSurvivors_QALE, nodeNet_QALE, true);
		probNet.addLink(nodeInmediate_Survival, nodeNet_QALE, true);
		probNet.addLink(nodeMED_Survival, nodeNet_QALE, true);
		probNet.addLink(nodeNet_QALE, nodeTotal_QALE, true);
		probNet.addLink(nodeTBNA_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeMED_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEUS_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEBUS_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeTotal_QALE, nodeNet_Effectiveness, true);
		probNet.addLink(nodeCostCT_scan, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeCostTBNA, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeCostEBUS, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeCostEUS, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeCostMED, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeCostPET, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeCostTreatment, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeTotal_Economic_Cost, nodeWeighted_Economic_Cost, true);
		probNet.addLink(nodeC2E, nodeWeighted_Economic_Cost, true);
		probNet.addLink(nodeWeighted_Economic_Cost, nodeNet_Effectiveness, true);

		// Potentials
		TablePotential potN2_N3 = new TablePotential(Arrays.asList(varN2_N3), PotentialRole.CONDITIONAL_PROBABILITY);
		potN2_N3.setValues(new double[] { 0.7193, 0.2807 });
		nodeN2_N3.setPotential(potN2_N3);

		TablePotential potCT_scan = new TablePotential(Arrays.asList(varCT_scan, varN2_N3),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potCT_scan.setValues(new double[] { 0.85676, 0.14324, 0.48966, 0.51034 });
		nodeCT_scan.setPotential(potCT_scan);

		TablePotential potTBNA = new TablePotential(Arrays.asList(varTBNA, varCT_scan, varN2_N3, varDecTBNA),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potTBNA.setValues(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0.92143, 0.07857, 0.90435, 0.09565, 0.98, 0.02, 0.54032,
				0.45968 });
		nodeTBNA.setPotential(potTBNA);

		TablePotential potPET = new TablePotential(Arrays.asList(varPET, varCT_scan, varN2_N3, varDecPET),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potPET.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92473, 0.07527, 0, 0.775, 0.225, 0,
				0.25974, 0.74026, 0, 0.09524, 0.90476 });
		nodePET.setPotential(potPET);

		TablePotential potEBUS = new TablePotential(Arrays.asList(varEBUS, varPET, varCT_scan, varN2_N3, varDecEBUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEBUS.setValues(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.97778,
				0.02222, 0.975, 0.025, 0.96667, 0.03333, 0.97368, 0.02632, 0.97561, 0.02439, 0.96552, 0.03448, 0.10811,
				0.89189, 0.11905, 0.88095, 0.10811, 0.89189, 0.08108, 0.91892, 0.11111, 0.88889, 0.12121, 0.87879 });
		nodeEBUS.setPotential(potEBUS);

		TablePotential potEUS = new TablePotential(Arrays.asList(varEUS, varPET, varCT_scan, varN2_N3, varDecEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEUS.setValues(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.92308,
				0.07692, 0.9375, 0.0625, 0.92593, 0.07407, 0.92857, 0.07143, 0.93333, 0.06667, 0.93548, 0.06452, 0.2381,
				0.7619, 0.43333, 0.56667, 0.41935, 0.58065, 0.14286, 0.85714, 0.13158, 0.86842, 0.13889, 0.86111 });
		nodeEUS.setPotential(potEUS);

		TablePotential potMED = new TablePotential(Arrays.asList(varMED, varPET, varCT_scan, varN2_N3, varDecMED),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potMED.setValues(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.94444,
				0.05556, 0.9375, 0.0625, 0.94737, 0.05263, 0.92857, 0.07143, 0.94118, 0.05882, 0.95, 0.05, 0.27273,
				0.72727, 0.2, 0.8, 0.21429, 0.78571, 0.1875, 0.8125, 0.1875, 0.8125, 0.2, 0.8 });
		nodeMED.setPotential(potMED);

		TablePotential potMED_Sv = new TablePotential(Arrays.asList(varMED_Sv, varDecMED),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potMED_Sv.setValues(new double[] { 0, 1, 0.03704, 0.96296 });
		nodeMED_Sv.setPotential(potMED_Sv);

		ExactDistrPotential potSurvivors_QALE = new ExactDistrPotential(
				Arrays.asList(varSurvivors_QALE, varN2_N3, varTreatment));
		potSurvivors_QALE.getTablePotential().setValues(new double[] { 1.25, 0.5, 2, 0.83, 3, 0.66 });
		nodeSurvivors_QALE.setPotential(potSurvivors_QALE);

		ExactDistrPotential potInmediate_Survival = new ExactDistrPotential(
				Arrays.asList(varInmediate_Survival, varTreatment));
		potInmediate_Survival.getTablePotential().setValues(new double[] { 0.98113, 0.98039, 0.90909 });
		nodeInmediate_Survival.setPotential(potInmediate_Survival);

		ExactDistrPotential potMED_Survival = new ExactDistrPotential(Arrays.asList(varMED_Survival, varMED_Sv));
		potMED_Survival.getTablePotential().setValues(new double[] { 0, 1 });
		nodeMED_Survival.setPotential(potMED_Survival);

		ProductPotential potNet_QALE = new ProductPotential(
				Arrays.asList(varNet_QALE, varInmediate_Survival, varMED_Survival, varSurvivors_QALE),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeNet_QALE.setPotential(potNet_QALE);

		ExactDistrPotential potTBNA_Morbidity = new ExactDistrPotential(Arrays.asList(varTBNA_Morbidity, varDecTBNA));
		potTBNA_Morbidity.getTablePotential().setValues(new double[] { 0, -0.0001 });
		nodeTBNA_Morbidity.setPotential(potTBNA_Morbidity);

		ExactDistrPotential potMED_Morbidity = new ExactDistrPotential(Arrays.asList(varMED_Morbidity, varMED_Sv));
		potMED_Morbidity.getTablePotential().setValues(new double[] { 0, -0.05 });
		nodeMED_Morbidity.setPotential(potMED_Morbidity);

		ExactDistrPotential potEUS_Morbidity = new ExactDistrPotential(Arrays.asList(varEUS_Morbidity, varDecEUS));
		potEUS_Morbidity.getTablePotential().setValues(new double[] { 0, -0.03 });
		nodeEUS_Morbidity.setPotential(potEUS_Morbidity);

		ExactDistrPotential potEBUS_Morbidity = new ExactDistrPotential(Arrays.asList(varEBUS_Morbidity, varDecEBUS));
		potEBUS_Morbidity.getTablePotential().setValues(new double[] { 0, -0.03 });
		nodeEBUS_Morbidity.setPotential(potEBUS_Morbidity);

		SumPotential potTotal_QALE = new SumPotential(
				Arrays.asList(varTotal_QALE, varEBUS_Morbidity, varEUS_Morbidity, varMED_Morbidity, varNet_QALE,
						varTBNA_Morbidity), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal_QALE.setPotential(potTotal_QALE);

		ExactDistrPotential potCostCT_scan = new ExactDistrPotential(Arrays.asList(varCostCT_scan));
		potCostCT_scan.getTablePotential().setValues(new double[] { 670 });
		nodeCostCT_scan.setPotential(potCostCT_scan);

		ExactDistrPotential potCostTBNA = new ExactDistrPotential(Arrays.asList(varCostTBNA, varDecTBNA));
		potCostTBNA.getTablePotential().setValues(new double[] { 0, 80 });
		nodeCostTBNA.setPotential(potCostTBNA);

		ExactDistrPotential potCostEBUS = new ExactDistrPotential(Arrays.asList(varCostEBUS, varDecEBUS));
		potCostEBUS.getTablePotential().setValues(new double[] { 0, 620 });
		nodeCostEBUS.setPotential(potCostEBUS);

		ExactDistrPotential potCostEUS = new ExactDistrPotential(Arrays.asList(varCostEUS, varDecEUS));
		potCostEUS.getTablePotential().setValues(new double[] { 0, 620 });
		nodeCostEUS.setPotential(potCostEUS);

		ExactDistrPotential potCostMED = new ExactDistrPotential(Arrays.asList(varCostMED, varDecMED));
		potCostMED.getTablePotential().setValues(new double[] { 0, 1620 });
		nodeCostMED.setPotential(potCostMED);

		ExactDistrPotential potCostPET = new ExactDistrPotential(Arrays.asList(varCostPET, varDecPET));
		potCostPET.getTablePotential().setValues(new double[] { 0, 2250 });
		nodeCostPET.setPotential(potCostPET);

		ExactDistrPotential potCostTreatment = new ExactDistrPotential(Arrays.asList(varCostTreatment, varTreatment));
		potCostTreatment.getTablePotential().setValues(new double[] { 3000, 11242, 19646 });
		nodeCostTreatment.setPotential(potCostTreatment);

		SumPotential potTotal_Economic_Cost = new SumPotential(
				Arrays.asList(varTotal_Economic_Cost, varCostCT_scan, varCostEBUS, varCostEUS, varCostMED, varCostPET,
						varCostTBNA, varCostTreatment), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal_Economic_Cost.setPotential(potTotal_Economic_Cost);

		ExactDistrPotential potC2E = new ExactDistrPotential(Arrays.asList(varC2E));
		potC2E.getTablePotential().setValues(new double[] { -0.00003333 });
		nodeC2E.setPotential(potC2E);

		ProductPotential potWeighted_Economic_Cost = new ProductPotential(
				Arrays.asList(varWeighted_Economic_Cost, varC2E, varTotal_Economic_Cost),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeWeighted_Economic_Cost.setPotential(potWeighted_Economic_Cost);

		SumPotential potNet_Effectiveness = new SumPotential(
				Arrays.asList(varNet_Effectiveness, varTotal_QALE, varWeighted_Economic_Cost),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeNet_Effectiveness.setPotential(potNet_Effectiveness);

		// Link<Node> restrictions and revealing states
		Link<Node> link_nodeDecTBNA_nodeTBNA = probNet.getLink(nodeDecTBNA, nodeTBNA, true);
		link_nodeDecTBNA_nodeTBNA.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDecTBNA_nodeTBNA = (TablePotential) link_nodeDecTBNA_nodeTBNA
				.getRestrictionsPotential();
		restrictions_nodeDecTBNA_nodeTBNA.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDecTBNA_nodeTBNA.setRevealingStates(Arrays.asList(varDecTBNA.getStates()[1]));

		Link<Node> link_nodeDecPET_nodePET = probNet.getLink(nodeDecPET, nodePET, true);
		link_nodeDecPET_nodePET.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDecPET_nodePET = (TablePotential) link_nodeDecPET_nodePET
				.getRestrictionsPotential();
		restrictions_nodeDecPET_nodePET.setValues(new double[] { 1, 0, 0, 1, 0, 1 });
		link_nodeDecPET_nodePET.setRevealingStates(Arrays.asList(varDecPET.getStates()[0], varDecPET.getStates()[1]));

		Link<Node> link_nodeDecMED_nodeMED = probNet.getLink(nodeDecMED, nodeMED, true);
		link_nodeDecMED_nodeMED.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDecMED_nodeMED = (TablePotential) link_nodeDecMED_nodeMED
				.getRestrictionsPotential();
		restrictions_nodeDecMED_nodeMED.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDecMED_nodeMED.setRevealingStates(Arrays.asList(varDecMED.getStates()[1]));

		Link<Node> link_nodeDecEBUS_nodeEBUS = probNet.getLink(nodeDecEBUS, nodeEBUS, true);
		link_nodeDecEBUS_nodeEBUS.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDecEBUS_nodeEBUS = (TablePotential) link_nodeDecEBUS_nodeEBUS
				.getRestrictionsPotential();
		restrictions_nodeDecEBUS_nodeEBUS.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDecEBUS_nodeEBUS.setRevealingStates(Arrays.asList(varDecEBUS.getStates()[1]));

		Link<Node> link_nodeDecEUS_nodeEUS = probNet.getLink(nodeDecEUS, nodeEUS, true);
		link_nodeDecEUS_nodeEUS.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDecEUS_nodeEUS = (TablePotential) link_nodeDecEUS_nodeEUS
				.getRestrictionsPotential();
		restrictions_nodeDecEUS_nodeEUS.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDecEUS_nodeEUS.setRevealingStates(Arrays.asList(varDecEUS.getStates()[1]));

		// Always observed nodes
		nodeCT_scan.setAlwaysObserved(true);

		return probNet;
	}

	public static ProbNet buildUsedCarBuyer() {
		ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		// Variables
		probNet.setName(NamesNetworks.USED_CAR_BUYER_DAN.toString());

		Variable varCars_Condition = new Variable("Car's Condition", "lemon", "peach");
		Variable varFirst_Result = new Variable("First Result", "no result", "no defect", "one defect", "two defects");
		Variable varSecond_result = new Variable("Second result", "no defect", "one defect");
		Variable varDec_Purchase = new Variable("Dec: Purchase", "do not buy", "buy without guarantee",
				"buy with guarantee");
		Variable varDec_Second_Test = new Variable("Dec: Second Test", "no test", "differential");
		Variable varDec_First_Test = new Variable("Dec: First Test", "no test", "fuel-electrical", "transmission",
				"steering");
		Variable varCost_First_test = new Variable("Cost: First test");
		Variable varCost_Second_Test = new Variable("Cost: Second Test");
		Variable varBuySell_difference = new Variable("Buy-sell difference");
		Variable varCost_Guarantee = new Variable("Cost: Guarantee");
		Variable varCost_Repair = new Variable("Cost: Repair");
		Variable varTotal = new Variable("Total");

		// Nodes
		Node nodeCars_Condition = probNet.addNode(varCars_Condition, NodeType.CHANCE);
		Node nodeFirst_Result = probNet.addNode(varFirst_Result, NodeType.CHANCE);
		Node nodeSecond_result = probNet.addNode(varSecond_result, NodeType.CHANCE);
		Node nodeDec_Purchase = probNet.addNode(varDec_Purchase, NodeType.DECISION);
		Node nodeDec_Second_Test = probNet.addNode(varDec_Second_Test, NodeType.DECISION);
		Node nodeDec_First_Test = probNet.addNode(varDec_First_Test, NodeType.DECISION);
		Node nodeCost_First_test = probNet.addNode(varCost_First_test, NodeType.UTILITY);
		Node nodeCost_Second_Test = probNet.addNode(varCost_Second_Test, NodeType.UTILITY);
		Node nodeBuySell_difference = probNet.addNode(varBuySell_difference, NodeType.UTILITY);
		Node nodeCost_Guarantee = probNet.addNode(varCost_Guarantee, NodeType.UTILITY);
		Node nodeCost_Repair = probNet.addNode(varCost_Repair, NodeType.UTILITY);
		Node nodeTotal = probNet.addNode(varTotal, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeCars_Condition, nodeFirst_Result, true);
		probNet.addLink(nodeCars_Condition, nodeCost_Repair, true);
		probNet.addLink(nodeCars_Condition, nodeSecond_result, true);
		probNet.addLink(nodeFirst_Result, nodeSecond_result, true);
		probNet.addLink(nodeDec_Purchase, nodeCost_Guarantee, true);
		probNet.addLink(nodeDec_Purchase, nodeBuySell_difference, true);
		probNet.addLink(nodeDec_Purchase, nodeCost_Repair, true);
		probNet.addLink(nodeDec_Second_Test, nodeDec_Purchase, true);
		probNet.addLink(nodeDec_Second_Test, nodeCost_Second_Test, true);
		probNet.addLink(nodeDec_Second_Test, nodeSecond_result, true);
		probNet.addLink(nodeDec_First_Test, nodeFirst_Result, true);
		probNet.addLink(nodeDec_First_Test, nodeDec_Second_Test, true);
		probNet.addLink(nodeDec_First_Test, nodeCost_First_test, true);
		probNet.addLink(nodeDec_First_Test, nodeSecond_result, true);
		probNet.addLink(nodeCost_First_test, nodeTotal, true);
		probNet.addLink(nodeCost_Second_Test, nodeTotal, true);
		probNet.addLink(nodeBuySell_difference, nodeTotal, true);
		probNet.addLink(nodeCost_Guarantee, nodeTotal, true);
		probNet.addLink(nodeCost_Repair, nodeTotal, true);

		// Potentials
		TablePotential potCars_Condition = new TablePotential(Arrays.asList(varCars_Condition),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potCars_Condition.setValues(new double[] { 0.2, 0.8 });
		nodeCars_Condition.setPotential(potCars_Condition);

		TablePotential potFirst_Result = new TablePotential(
				Arrays.asList(varFirst_Result, varCars_Condition, varDec_First_Test),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potFirst_Result.setValues(new double[] { 1, 0, 0, 0, 1, 0, 0, 0, 0, 0.13, 0.53, 0.34, 0, 0.8, 0.2, 0, 0, 0.4,
				0.6, 0, 0, 0.9, 0.1, 0, 0, 0.4, 0.6, 0, 0, 0.9, 0.1, 0 });
		nodeFirst_Result.setPotential(potFirst_Result);

		TablePotential potSecond_result = new TablePotential(
				Arrays.asList(varSecond_result, varCars_Condition, varDec_Second_Test, varFirst_Result,
						varDec_First_Test), PotentialRole.CONDITIONAL_PROBABILITY);
		potSecond_result.setValues(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.67, 0.33, 0.89, 0.11, 0, 0, 0, 0, 0.44, 0.56, 1, 0,
				0, 0, 0, 0, 0.44, 0.56, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0 });
		nodeSecond_result.setPotential(potSecond_result);

		ExactDistrPotential potCost_First_test = new ExactDistrPotential(
				Arrays.asList(varCost_First_test, varDec_First_Test));
		potCost_First_test.getTablePotential().setValues(new double[] { 0, -13, -10, -9 });
		nodeCost_First_test.setPotential(potCost_First_test);

		ExactDistrPotential potCost_Second_Test = new ExactDistrPotential(
				Arrays.asList(varCost_Second_Test, varDec_Second_Test));
		potCost_Second_Test.getTablePotential().setValues(new double[] { 0, -4 });
		nodeCost_Second_Test.setPotential(potCost_Second_Test);

		ExactDistrPotential potBuySell_difference = new ExactDistrPotential(
				Arrays.asList(varBuySell_difference, varDec_Purchase));
		potBuySell_difference.getTablePotential().setValues(new double[] { 0, 100, 100 });
		nodeBuySell_difference.setPotential(potBuySell_difference);

		ExactDistrPotential potCost_Guarantee = new ExactDistrPotential(
				Arrays.asList(varCost_Guarantee, varDec_Purchase));
		potCost_Guarantee.getTablePotential().setValues(new double[] { 0, 0, -60 });
		nodeCost_Guarantee.setPotential(potCost_Guarantee);

		ExactDistrPotential potCost_Repair = new ExactDistrPotential(
				Arrays.asList(varCost_Repair, varCars_Condition, varDec_Purchase));
		potCost_Repair.getTablePotential().setValues(new double[] { 0, 0, -200, -40, 0, -20 });
		nodeCost_Repair.setPotential(potCost_Repair);

		SumPotential potTotal = new SumPotential(
				Arrays.asList(varTotal, varCost_First_test, varCost_Second_Test, varCost_Guarantee, varCost_Repair,
						varBuySell_difference), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal.setPotential(potTotal);

		// Link<Node> restrictions and revealing states
		Link<Node> link_nodeFirst_Result_nodeSecond_result = probNet.getLink(nodeFirst_Result, nodeSecond_result, true);
		link_nodeFirst_Result_nodeSecond_result.initializesRestrictionsPotential();
		TablePotential restrictions_nodeFirst_Result_nodeSecond_result = (TablePotential) link_nodeFirst_Result_nodeSecond_result
				.getRestrictionsPotential();
		restrictions_nodeFirst_Result_nodeSecond_result.setValues(new double[] { 0, 1, 1, 1, 0, 1, 1, 1 });

		Link<Node> link_nodeDec_Second_Test_nodeSecond_result = probNet
				.getLink(nodeDec_Second_Test, nodeSecond_result, true);
		link_nodeDec_Second_Test_nodeSecond_result.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDec_Second_Test_nodeSecond_result = (TablePotential) link_nodeDec_Second_Test_nodeSecond_result
				.getRestrictionsPotential();
		restrictions_nodeDec_Second_Test_nodeSecond_result.setValues(new double[] { 0, 1, 0, 1 });
		link_nodeDec_Second_Test_nodeSecond_result.setRevealingStates(Arrays.asList(varDec_Second_Test.getStates()[1]));

		Link<Node> link_nodeDec_First_Test_nodeFirst_Result = probNet
				.getLink(nodeDec_First_Test, nodeFirst_Result, true);
		link_nodeDec_First_Test_nodeFirst_Result.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDec_First_Test_nodeFirst_Result = (TablePotential) link_nodeDec_First_Test_nodeFirst_Result
				.getRestrictionsPotential();
		restrictions_nodeDec_First_Test_nodeFirst_Result.setValues(new double[] { 1, 0, 0, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0,
				1, 0, 0 });
		link_nodeDec_First_Test_nodeFirst_Result.setRevealingStates(
				Arrays.asList(varDec_First_Test.getStates()[3], varDec_First_Test.getStates()[2],
						varDec_First_Test.getStates()[1]));

		Link<Node> link_nodeDec_First_Test_nodeDec_Second_Test = probNet
				.getLink(nodeDec_First_Test, nodeDec_Second_Test, true);
		link_nodeDec_First_Test_nodeDec_Second_Test.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDec_First_Test_nodeDec_Second_Test = (TablePotential) link_nodeDec_First_Test_nodeDec_Second_Test
				.getRestrictionsPotential();
		restrictions_nodeDec_First_Test_nodeDec_Second_Test.setValues(new double[] { 1, 1, 1, 1, 0, 0, 1, 0 });

		Link<Node> link_nodeDec_First_Test_nodeSecond_result = probNet
				.getLink(nodeDec_First_Test, nodeSecond_result, true);
		link_nodeDec_First_Test_nodeSecond_result.initializesRestrictionsPotential();
		TablePotential restrictions_nodeDec_First_Test_nodeSecond_result = (TablePotential) link_nodeDec_First_Test_nodeSecond_result
				.getRestrictionsPotential();
		restrictions_nodeDec_First_Test_nodeSecond_result.setValues(new double[] { 0, 0, 1, 0, 0, 0, 1, 0 });

		// Always observed nodes

		return probNet;
	}

}
