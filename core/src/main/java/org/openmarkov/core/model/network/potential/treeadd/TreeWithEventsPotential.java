/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeadd;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.DESSimulablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Potential which consists on a set of TreeADDPotentials. There is one TreeADDPotential for each parent Variable of VariableType EVENT
 * The variables of each TreADDPotential are the Event Variable (which is the root Variable) and the other variables whose type is not Event
 */
@PotentialType(names = "Tree with Events")
public class TreeWithEventsPotential extends Potential implements DESSimulablePotential {

	// Attributes
	/**
	 *List of parent Variable with VariableType.EVENT
	 */
	private List<Variable> events;

	/**
	 *List of parent Variable with isn't VariableType.EVENT
	 */
	private List<Variable> noEvents;

	/**
	 * Map mapping each Event Variable with its TreeADDPotential
	 */
	protected HashMap<Variable, TreeADDPotential> trees;


	/**
	 * Creates a new TreeWithEventsPotential with variables as its list of Variable and role as PotentialRole
	 * @param variables potential variables
	 * @param role potential PotentialRole
	 */
	public TreeWithEventsPotential(List<Variable> variables, PotentialRole role) {
		super(variables,role);
		events = variables.subList(1,variables.size()).stream().filter(variable -> variable.getVariableType() == VariableType.EVENT).collect(Collectors.toList());
		noEvents = variables.subList(1,variables.size()).stream().filter(variable -> variable.getVariableType() !=VariableType.EVENT ).collect(Collectors.toList());
		trees = new HashMap<>();
		for (Variable event: events){
			List<Variable> eventTreeVariables = new ArrayList<>();
			eventTreeVariables.add(getConditionedVariable());
			eventTreeVariables.add(event);
			eventTreeVariables.addAll(noEvents);
			trees.put(event, new TreeADDPotential(eventTreeVariables, event, role));
		}
	}

	/**
	 * Creates a new TreeWithEventsPotential copying treeWithEvents
	 *
	 * @param treeWithEvents TreeWithEventsPotential to be created
	 */
	public TreeWithEventsPotential(TreeWithEventsPotential treeWithEvents) {
		this(treeWithEvents.getVariables(),treeWithEvents.role);
		HashMap<Variable,TreeADDPotential> treesToCopy = treeWithEvents.getTrees();
		trees.clear();
		for (Variable event: treeWithEvents.getEvents()){
			trees.put(event, (TreeADDPotential) treesToCopy.get(event).copy());
		}

	}

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role
	 *
	 * @param variables
	 * @param role
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		// 10/01/2023 FIXME Provisional
		if (!(node.getProbNet().getNetworkType() instanceof DESNetworkType)) return false;
		boolean validate = variables.subList(1,variables.size()).stream().anyMatch(variable -> variable.getVariableType() ==VariableType.EVENT);
		return validate;

	}


	/**
	 * Returns the HashMap which maps the Event variables with its TreeADDPotential
	 * @return the HashMap which maps the Event variables with its TreeADDPotential
	 */
	public HashMap<Variable, TreeADDPotential> getTrees() {
		return trees;
	}

	/**
	 * Sets trees as the HashMap of this TreeWithEventsPotential
	 * @param trees HashMap to be set to this TreeWithEventsPotential
	 */
	public void setTrees(HashMap<Variable, TreeADDPotential> trees) {
		this.trees = trees;
	}


	/**
	 * Returns the TreeADDPotential associated to eventVariable
	 * @param eventVariable Variable with VariableType.EVENT
	 * @return the TreeADDPotential associated to eventVariable
	 * @throws IncompatibleEvidenceException exception thrown when eventVariable has not VariableType.EVENT. TODO Exception type may not be correct
	 */
	public TreeADDPotential getTree(Variable eventVariable) throws IncompatibleEvidenceException.VariableMustBeEvent {
		if (eventVariable.getVariableType() != VariableType.EVENT) {
			throw new IncompatibleEvidenceException.VariableMustBeEvent(eventVariable);
		};
		return trees.get(eventVariable);
	}


	/**
	 * Sets tree associated to eventVariable in this TreeWithEventsPotential
	 * @param eventVariable Variable with VariableType.EVENT
	 * @throws IncompatibleEvidenceException exception thrown when eventVariable has not VariableType.EVENT. TODO Exception type may not be correct
	 */
	public void setTree(Variable eventVariable, TreeADDPotential tree) throws IncompatibleEvidenceException.VariableMustBeEvent {
		if (eventVariable.getVariableType() != VariableType.EVENT) {
			throw new IncompatibleEvidenceException.VariableMustBeEvent(eventVariable);
		};
		trees.put(eventVariable, tree);
	}


	/**
	 * Returns a List<Variable> with the parent variables with VariableType.EVENT
	 * @return  List<Variable> with the parent variables with VariableType.EVENT
	 */
	public List<Variable> getEvents() {
		return events;
	}

	//14/08/2022 refactored for avoiding nuisance variance
	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {


		List<Variable> eventVariables = parents.getVariables().stream().filter(variable ->variable.getVariableType() ==VariableType.EVENT).collect(Collectors.toList());
		//19/03/2023 - only one event in parents - FIXME throw exception without changing method signature; consider to add exception
		if ( eventVariables.size()!=1) throw new RuntimeException("TreeWithEventsPotential#sampleConditionedVariable: There must be exactly one event in parents configuration");
		//
		//It is supposed the tree has a different potential for each event, so this method return a sampled value when an event has happened

		TreeADDPotential eventTree= null;
		try {
			eventTree = getTree(eventVariables.get(0));
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
		}
		double result =0;
		try
		{
			//19/03/2023 the event is not part of the branch potential
			result = eventTree.sampleConditionedVariable(randomNumbers, parents);

		} catch(Exception e) {
			//FIXME when completed this method coding this catch will be removed
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,"TreeWithEventsPotential: Getting sample exception: " +eventVariables.get(0).getName());
			throw new RuntimeException(e);
		}
		return result;
	}
	@Override
	public void resetSimulation()  {

		trees.values().forEach(DESSimulablePotential::resetSimulation);
	}


	@Override public Potential copy() {
		return new TreeWithEventsPotential(this);
	}


	@Override public boolean isUncertain() {
	//TODO
		return false;
	}

	/**
	 * Generates a sampled potential
	 */
	@Override public Potential sample() {
		//TODO
		return null;
	}



	@Override public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) {
		//TODO
		return null;
	}


	@Override public void replaceVariable(int position, Variable variable) {
		//TODO
	}

	/**
	 * @param evidenceCase               <code>EvidenceCase</code>
	 * @param inferenceOptions
	 * @param alreadyProjectedPotentials <code>List</code> of already projected potentials
	 */
	@Override
	public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public void replaceNumericVariable(Variable convertedParentVariable) {
		//TODO
	}

	@Override public void scalePotential(double scale) {
		//TODO

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		//TODO
			return null;
	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		return null;
	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		return null;
	}


	// Methods for toString()
	public String toString() {
		//TODO
		return "";
	}

}
