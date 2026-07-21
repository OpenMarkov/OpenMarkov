/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.factory;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.MIDType;

import java.util.ArrayList;
import java.util.List;

public class MIDFactory extends NetsFactory {
	/**
	 * @return A Markov influence diagram proposed for jdiez for testing cost-effectiveness analysis and inference
	 */
	public static ProbNet createMIDDeadAlive() {
		return createMIDWithStateVariable(0.8, 1.0, 40000, 0, 0.7, 0.5);
	}

	public static ProbNet createMIDWithStateVariable(double qoLTreat, double qoLNoTreat, double costTreat,
			double costNoTreat, double probAliveIfTreat, double probAliveIfNoTreat) {
        double[] tableQoL = {0.0, qoLTreat, 0.0, qoLNoTreat};
		double[] tableCostOfTreatment = { costTreat, costNoTreat };
		String[] statesStateVariable = { "dead", "alive" };

		//Decision criteria
		ArrayList<Criterion> decisionCriteria = new ArrayList<>();
		Criterion cost = new Criterion("cost");
		Criterion effectiveness = new Criterion("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);

		Variable variableTreatment = new Variable("Treatment", yesNoStates);
		Variable variableCostOfTreatment = new Variable("Cost of treatment");
		variableCostOfTreatment.setDecisionCriterion(cost);
		Variable variableQoL = createTemporalVariable("QoL", 0);
		variableQoL.setDecisionCriterion(effectiveness);
		Variable variableState0 = createTemporalVariable("State", 0, statesStateVariable);
		Variable variableState1 = createTemporalVariable("State", 1, statesStateVariable);

		ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());

		//set decision criteria to the network
		probNet.setDecisionCriteria(decisionCriteria);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, variableState0, variableState1);
		addVariables(probNet, NodeType.DECISION, variableTreatment);
		addVariables(probNet, NodeType.UTILITY, variableQoL, variableCostOfTreatment);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableState0, variableState1, variableTreatment, variableQoL,
				variableCostOfTreatment);

		//Potential State0
		double[] probabilitiesState0 = { 0.0, 1.0 };
		TablePotential potentialState0 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,
				probabilitiesState0, variableState0);

		//Potential State1
		double[] probabilitiesState1 = { 1.0, 0.0, 1.0 - probAliveIfTreat, probAliveIfTreat, 1.0, 0.0,
				1.0 - probAliveIfNoTreat, probAliveIfNoTreat };
		TablePotential potentialState1 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,
				probabilitiesState1, variableState1, variableState0, variableTreatment);

		//Potential Treatment
        ExactDistrPotential potentialCostOfTreatment = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY,
                                                                                 tableCostOfTreatment, variableCostOfTreatment, variableTreatment);

		//Potential QoL
        ExactDistrPotential potentialQoL = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableQoL, variableQoL,
                                                                     variableState0, variableTreatment);

		//Links throws NodeNotFoundException
        probNet.addLink(variableTreatment, variableCostOfTreatment, true);
        probNet.addLink(variableTreatment, variableQoL, true);
        probNet.addLink(variableTreatment, variableState1, true);
        probNet.addLink(variableState0, variableQoL, true);
        probNet.addLink(variableState0, variableState1, true);
        
        addPotentials(probNet, potentialQoL, potentialCostOfTreatment, potentialState0, potentialState1);

		return probNet;
	}

	public static ProbNet createSemiMarkovOnlyChanceNet() {
		ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());
		//Decision criteria
		ArrayList<Criterion> decisionCriteria = new ArrayList<>();
		Criterion cost = new Criterion("cost");
		Criterion effectiveness = new Criterion("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);

		//set decision criteria to the network
		probNet.setDecisionCriteria(decisionCriteria);

		//Variables
		Variable duration0 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration0.setBaseName("Duration");
		duration0.setName("Duration [0]");
		duration0.setTimeSlice(0);
		Variable duration1 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration1.setBaseName("Duration");
		duration1.setName("Duration [1]");
		duration1.setTimeSlice(1);

		Variable state0 = new Variable("State", "dead", "alive");
		state0.setBaseName("State");
		state0.setName("State [0]");
		state0.setTimeSlice(0);
		Variable state1 = new Variable("State", "dead", "alive");
		state1.setBaseName("State");
		state1.setName("State [1]");
		state1.setTimeSlice(1);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, duration0, duration1, state0, state1);
		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, duration0, duration1, state0, state1);
		//Potential State0
		double[] probabilitiesState0 = { 0.0, 1.0 };
		TablePotential potentialState0 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,
				probabilitiesState0, state0);
		//table
		double[] branch1 = { 0.5, 0.5, 0.0, 1.0 };
		TablePotential table1 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch1, state1, state0);
		double[] branch2 = { 0.3, 0.7, 0.0, 1.0 };
		TablePotential table2 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch2, state1, state0);
		//Potential state1
		ArrayList<Variable> variables = new ArrayList<>();
		variables.add(state1);
		variables.add(state0);
		variables.add(duration0);
		ArrayList<TreeADDBranch> branches = new ArrayList<>();
		branches.add(new TreeADDBranch(new Threshold(0, false), new Threshold(2, true), duration0, table1, variables));
		branches.add(new TreeADDBranch(new Threshold(2, true), new Threshold(20, true), duration0, table2, variables));
		TreeADDPotential potentialState1 = new TreeADDPotential(variables, duration0,
				PotentialRole.CONDITIONAL_PROBABILITY, branches);
		//potential duratio0
		ArrayList<Variable> variablesDuration0 = new ArrayList<>();
		variablesDuration0.add(duration0);
		UniformPotential potentialduration0 = new UniformPotential(variablesDuration0,
				PotentialRole.CONDITIONAL_PROBABILITY);
		//potential duration1
		ArrayList<Variable> variablesDuration1 = new ArrayList<>();
		variablesDuration1.add(duration1);
		variablesDuration1.add(duration0);
		CycleLengthShift potetialDuration1 = new CycleLengthShift(variablesDuration1, probNet.getCycleLength());

		//links
        probNet.addLink(state0, state1, true);
        probNet.addLink(duration0, duration1, true);
        probNet.addLink(duration0, state1, true);
        
        //adding potentials to network
		addPotentials(probNet, potentialState0, potentialState1, potentialduration0, potetialDuration1);

		return probNet;
	}

	public static ProbNet createSemiMarkovModelNet() {
		ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());
		//Decision criteria
		ArrayList<Criterion> decisionCriteria = new ArrayList<>();
		Criterion cost = new Criterion("cost");
		Criterion effectiveness = new Criterion("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);

		//set decision criteria to the network
		probNet.setDecisionCriteria(decisionCriteria);

		//Variables
		Variable duration0 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration0.setTimeSlice(0);
		duration0.setBaseName("Duration");
		duration0.setName("Duration [0]");
		Variable duration1 = new Variable("Duration", true, 0.0, 20.0, true, 1);
		duration1.setTimeSlice(1);
		duration1.setBaseName("Duration");
		duration1.setName("Duration [1]");

		Variable state0 = createTemporalVariable("State", 0, "dead", "alive");
		Variable state1 = createTemporalVariable("State", 1, "dead", "alive");

		Variable variableTreatment = new Variable("Treatment", yesNoStates);
		Variable variableCost = createTemporalVariable("Cost", 0);
		variableCost.setDecisionCriterion(cost);
		Variable variableQoL = createTemporalVariable("QoL", 0);
		variableQoL.setDecisionCriterion(effectiveness);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, duration0, duration1, state0, state1, variableTreatment, variableCost,
				variableQoL);
		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, duration0, duration1, state0, state1, variableTreatment, variableCost,
				variableQoL);
		//Potential State0
		double[] probabilitiesState0 = { 0.0, 1.0 };
		TablePotential potentialState0 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY,
				probabilitiesState0, state0);

		//potential State1
		ArrayList<Variable> variablesTree = new ArrayList<>();
		variablesTree.add(state1);
		variablesTree.add(variableTreatment);
		variablesTree.add(state0);
		variablesTree.add(duration0);

		double[] branch1 = { 0.0, 1.0 };
		TablePotential table1 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch1, state1);

		ArrayList<State> statesNo = new ArrayList<>();
        statesNo.add(variableTreatment.getStates()[variableTreatment.getStateIndex("no")]);
        
        TreeADDBranch branchNo = new TreeADDBranch(statesNo, variableTreatment, table1, variablesTree);
		//table
		double[] branch11 = { 0.5, 0.5, 0.0, 1.0 };
		TablePotential table11 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch11, state1, state0);
		double[] branch21 = { 0.3, 0.7, 0.0, 1.0 };
		TablePotential table2 = createTablePotential(PotentialRole.CONDITIONAL_PROBABILITY, branch21, state1, state0);
		//subtree
		ArrayList<Variable> variables = new ArrayList<>();
		variables.add(state1);
		variables.add(state0);
		variables.add(duration0);
		ArrayList<TreeADDBranch> branches = new ArrayList<>();
		branches.add(new TreeADDBranch(new Threshold(0, false), new Threshold(2, true), duration0, table11, variables));
		branches.add(new TreeADDBranch(new Threshold(2, true), new Threshold(20, true), duration0, table2, variables));
		TreeADDPotential subPotentialState1 = new TreeADDPotential(variables, duration0,
				PotentialRole.CONDITIONAL_PROBABILITY, branches);

		ArrayList<State> statesYes = new ArrayList<>();
        statesYes.add(variableTreatment.getStates()[variableTreatment.getStateIndex("yes")]);
        
        TreeADDBranch branchYes = new TreeADDBranch(statesYes, variableTreatment, subPotentialState1, variablesTree);

		ArrayList<TreeADDBranch> treeBranches = new ArrayList<>();
		treeBranches.add(branchNo);
		treeBranches.add(branchYes);

		TreeADDPotential potentialState1 = new TreeADDPotential(variablesTree, variableTreatment,
				PotentialRole.CONDITIONAL_PROBABILITY, treeBranches);

		//potential duratio0
		ArrayList<Variable> variablesDuration0 = new ArrayList<>();
		variablesDuration0.add(duration0);
		UniformPotential potentialduration0 = new UniformPotential(variablesDuration0,
				PotentialRole.CONDITIONAL_PROBABILITY);
		//potential duration1
		ArrayList<Variable> variablesDuration1 = new ArrayList<>();
		variablesDuration1.add(duration1);
		variablesDuration1.add(duration0);
		CycleLengthShift potetialDuration1 = new CycleLengthShift(variablesDuration1, probNet.getCycleLength());

		//potential cost [0]
		ArrayList<Variable> variablesCost = new ArrayList<>();
		//variablesCost.add(variableCost);
		variablesCost.add(variableTreatment);
		variablesCost.add(state0);

		//cost no treatment
        double[] costNoTreat = {0.0};
		ExactDistrPotential costNoTreatment = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY,
				costNoTreat, variableCost);
		//cost treatment
        double[] costTreat = {3000.0, 0.0};
		ExactDistrPotential costTreatment = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, costTreat,
				variableCost, state0);

		ArrayList<TreeADDBranch> costBranches = new ArrayList<>();
		ArrayList<Variable> variablesBranchCost = new ArrayList<>();
		variablesBranchCost.add(variableCost);
		variablesBranchCost.addAll(variablesCost);
		costBranches.add(new TreeADDBranch(statesNo, variableTreatment, costNoTreatment, variablesBranchCost));
		costBranches.add(new TreeADDBranch(statesYes, variableTreatment, costTreatment, variablesBranchCost));

		List<Variable> treeADDVariablesCost = new ArrayList<>();
		treeADDVariablesCost.add(variableCost);
		treeADDVariablesCost.addAll(variablesCost);
		TreeADDPotential potentialCost = new TreeADDPotential(treeADDVariablesCost, variableTreatment,
				PotentialRole.CONDITIONAL_PROBABILITY, costBranches);

		//potential Qol [0]
		ArrayList<Variable> variablesQoL = new ArrayList<>();
		//variablesCost.add(variableQoL);
		variablesQoL.add(variableTreatment);
		variablesQoL.add(state0);

		//cost no treatment
        double[] qolNoTreat = {0.0};
		ExactDistrPotential qolNoTreatment = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY,
				qolNoTreat, variableQoL);
		//cost treatment
        double[] qolTreat = {1500.0, 0.0};
		ExactDistrPotential qolTreatment = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, qolTreat,
				variableQoL, state0);

		List<TreeADDBranch> qolBranches = new ArrayList<>();
		List<Variable> variablesBranchQoL = new ArrayList<>();
		variablesBranchQoL.add(variableQoL);
		variablesBranchQoL.addAll(variablesQoL);
		qolBranches.add(new TreeADDBranch(statesNo, variableTreatment, qolNoTreatment, variablesBranchQoL));
		qolBranches.add(new TreeADDBranch(statesYes, variableTreatment, qolTreatment, variablesBranchQoL));

		List<Variable> treeADDVariablesQoL = new ArrayList<>();
		treeADDVariablesQoL.add(variableQoL);
		treeADDVariablesQoL.addAll(variablesQoL);
		TreeADDPotential potentialQoL = new TreeADDPotential(treeADDVariablesQoL, variableTreatment,
				PotentialRole.CONDITIONAL_PROBABILITY, qolBranches);

		//links
        probNet.addLink(state0, state1, true);
        probNet.addLink(duration0, duration1, true);
        probNet.addLink(duration0, state1, true);
        probNet.addLink(variableTreatment, state1, true);
        probNet.addLink(variableTreatment, variableCost, true);
        probNet.addLink(variableTreatment, variableQoL, true);
        probNet.addLink(state0, variableQoL, true);
        probNet.addLink(state0, variableCost, true);
        
        //adding potentials to network
		addPotentials(probNet, potentialState0, potentialState1, potentialduration0, potetialDuration1, potentialCost,
				potentialQoL);

		return probNet;
	}

	private static Variable createTemporalVariable(String baseName, int timeSlice, String... statesStateVariable) {
		Variable variable = new Variable(baseName, statesStateVariable);
		variable.setBaseName(variable.getName());
		variable.setTimeSlice(timeSlice);
		return variable;

	}

	public static ProbNet createMIDWithoutStateVariable() {
		return createMIDWithoutStateVariable(0.9, 1.0, 40000, 0);
	}

	public static ProbNet createMIDWithoutStateVariable(double qoLTreat, double qoLNoTreat, double costTreat,
			double costNoTreat) {
		// Define the variables
        double[] tableQoL = {qoLTreat, qoLNoTreat};
		double[] tableCostOfTreatment = { costTreat, costNoTreat };

		//Decision criteria
		ArrayList<Criterion> decisionCriteria = new ArrayList<>();
		Criterion cost = new Criterion("cost");
		Criterion effectiveness = new Criterion("effectiveness");
		decisionCriteria.add(cost);
		decisionCriteria.add(effectiveness);

		Variable variableTreatment = new Variable("Treatment", yesNoStates);
		Variable variableCostOfTreatment = new Variable("Cost of treatment");
		variableCostOfTreatment.setDecisionCriterion(cost);
		Variable variableQoL = createTemporalVariable("QoL", 0);
		variableQoL.setDecisionCriterion(effectiveness);
		ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());

		//set decision criteria to the network
		probNet.setDecisionCriteria(decisionCriteria);

		//Add variables to the network
		addVariables(probNet, NodeType.DECISION, variableTreatment);
		addVariables(probNet, NodeType.UTILITY, variableQoL, variableCostOfTreatment);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableTreatment, variableQoL, variableCostOfTreatment);

		//Potential QoL
        ExactDistrPotential potentialQoL = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableQoL, variableQoL,
                                                                     variableTreatment);

		//Potential Treatment
        ExactDistrPotential potentialCostOfTreatment = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY,
                                                                                 tableCostOfTreatment, variableCostOfTreatment, variableTreatment);

		//Links throws NodeNotFoundException
        probNet.addLink(variableTreatment, variableQoL, true);
        probNet.addLink(variableTreatment, variableCostOfTreatment, true);
        
        addPotentials(probNet, potentialQoL, potentialCostOfTreatment);

		return probNet;
	}

}
