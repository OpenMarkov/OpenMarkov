/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Sampler that walks the {@link ProbNet}, collects all uncertain parameters
 * (including those nested in {@link TreeADDPotential} branches) and produces
 * deterministic, reproducible samples by sweeping each parameter through its
 * domain interval. Used by sensitivity-analysis tasks rather than by random
 * propagation.
 *
 * @author Manuel Arias
 */
public class SystematicSampling extends Sampler {

	ProbNet network;

	/**
	 * @param net Network
	 * @return The UncertainParameters built from "net"
	 */
	public static List<UncertainParameter> getUncertainParameters(ProbNet net) {
		List<Potential> potentials = net.getPotentials();
		List<UncertainParameter> uncertainParams = new ArrayList<>();
		for (Potential potential : potentials) {
			Set<UncertainParameter> auxUncertainParameters = getUncertainParameters(potential);
			if (auxUncertainParameters != null) {
				uncertainParams.addAll(auxUncertainParameters);
			}
		}
		return uncertainParams;
	}

	/**
	 * @param potential Potential
	 * @return A set of UncertainParameters built from the uncertain values appearing in "potential"
	 */
	private static Set<UncertainParameter> getUncertainParameters(Potential potential) {
		Set<UncertainParameter> uncertainParams = new HashSet<>();

		Map<UncertainValue, SubPotentialAndPositionInTablePotential> uncertainValues = getUncertainValues(potential);

		for (UncertainValue auxUncertainValue : uncertainValues.keySet()) {
			SubPotentialAndPositionInTablePotential subPotentialAndPosition = uncertainValues.get(auxUncertainValue);
			uncertainParams
					.add(new UncertainParameter(potential, auxUncertainValue, subPotentialAndPosition.getSubPotential(),
							subPotentialAndPosition.getPosition()));
		}
		return uncertainParams;
	}

	/**
	 * @param potential Potential
	 * @return A hash table with the uncertain values appearing in potential, and for each one the hash value is the subpotential where appearing
	 */
	private static Map<UncertainValue, SubPotentialAndPositionInTablePotential> getUncertainValues(Potential potential) {
		Map<UncertainValue, SubPotentialAndPositionInTablePotential> uncertainValuesHash = new HashMap<>();

		boolean isExactDistrPotential = potential instanceof ExactDistrPotential;
		if (potential instanceof TablePotential || isExactDistrPotential) {
			TablePotential tablePotential = (TablePotential)(!isExactDistrPotential? potential:((ExactDistrPotential)potential).getTablePotential());
			UncertainValue[] uncertainValuesPotential = tablePotential.getUncertainValues();
			if (uncertainValuesPotential != null) {
				int i = 0;
				for (UncertainValue auxUncertain : uncertainValuesPotential) {
					if (auxUncertain != null) {
						addIfNonExisting(uncertainValuesHash, auxUncertain,
								new SubPotentialAndPositionInTablePotential(potential, i));
					}
					i = i + 1;
				}
			}
		} else {
			if (potential instanceof TreeADDPotential) {
				for (TreeADDBranch branch : ((TreeADDPotential) potential).getBranches()) {
					if (branch != null) {
						Potential branchPotential = branch.getPotential();
						if (branchPotential != null) {
							Map<UncertainValue, SubPotentialAndPositionInTablePotential> auxUncertainValues = getUncertainValues(
									branchPotential);
							for (UncertainValue auxUncertain : auxUncertainValues.keySet()) {
								addIfNonExisting(uncertainValuesHash, auxUncertain,
										auxUncertainValues.get(auxUncertain));
							}
						}

					}
				}
			}
		}
		return uncertainValuesHash;
	}

	/**
	 * @param uncertainValuesHash     Hastable of uncertainvalues and subpotential and position elements
	 * @param auxUncertain            Auxiliary uncertain value
	 * @param subPotentialAndPosition Subpotential and position of the auxiliary uncertain value
	 *                                Adds the key, value pair (auxUncertain, tablePotential) to "uncertainValuesHash" if "auxUncertain" does not belong to the key set
	 */
	private static void addIfNonExisting(Map<UncertainValue, SubPotentialAndPositionInTablePotential> uncertainValuesHash,
			UncertainValue auxUncertain, SubPotentialAndPositionInTablePotential subPotentialAndPosition) {
		if (!uncertainValuesHash.containsKey(auxUncertain)) {
			uncertainValuesHash.put(auxUncertain, subPotentialAndPosition);
		}
	}

	/**
	 * @param potential   Potential
	 * @param newVariable New variable
	 * @return Adds a variable to the end of the list variables of "potential", and replicates the original values and uncertainValues
	 */
	private static TablePotential addVariableReplicatingValuesAndUncertainValues(TablePotential potential,
			Variable newVariable) {
		// creates the new potential
		List<Variable> newVariables = new ArrayList<>(potential.getVariables());
		newVariables.add(newVariable);
		// assigns the values of the new potential
		int newVariableNumStates = newVariable.getNumStates();
		double[] values = potential.getValues();
		UncertainValue[] uncertainValues = potential.getUncertainValues();
		boolean hasUncertainty = (uncertainValues != null) && (uncertainValues.length > 0);
		UncertainTablePotential uncertainNew = null;
		TablePotential newPotential;
		if (hasUncertainty) {
			uncertainNew = new UncertainTablePotential(newVariables, potential.getPotentialRole());
			uncertainNew.uncertainValues = new UncertainValue[uncertainNew.getTableSize()];
			newPotential = uncertainNew;
		} else {
			newPotential = new TablePotential(newVariables, potential.getPotentialRole());
		}

		for (int i = 0; i < newVariableNumStates; i++) {
			int offset = i * values.length;
			for (int j = 0; j < values.length; j++) {
				int newPos = j + offset;
				newPotential.getValues()[newPos] = values[j];
				if (hasUncertainty) {
					uncertainNew.uncertainValues[newPos] = uncertainValues[j];
				}
			}
		}
		return newPotential;
	}
	
	private static TablePotential getPotentialTable(Potential pot) {
		boolean isExactDistrPotential = pot instanceof ExactDistrPotential;
		return (!isExactDistrPotential)?(TablePotential)pot:((ExactDistrPotential)pot).getTablePotential();
	}

	private static ProbNet sampleNetwork(ProbNet originalNet, List<ParameterAnalysisInformation> parameters,
			int numIntervals) {
		ProbNet net = originalNet.copy();

		UncertainParameter uncertainParameter;
		int numPoints = numIntervals + 1;
		List<Class<? extends ProbDensFunction>> functionTypes = initializeTypeFunctions();

		for (ParameterAnalysisInformation parameter : parameters) {
			uncertainParameter = parameter.uncertainParameter;
			if (uncertainParameter != null) {
				String iterationVariableName = parameter.iterationVariableName;
				Variable iterVariable = new Variable(iterationVariableName, numPoints);
				Potential originalPotential = uncertainParameter.potential;
				Potential newPotential = originalPotential.copy();
				Potential originalSubPotential = uncertainParameter.subPotential;
                boolean isSubPotentialExactDistrPotential = originalSubPotential instanceof ExactDistrPotential;
                TablePotential originalSubPotentialTable = getPotentialTable(originalSubPotential);
                boolean isOriginalPotentialATreeADD = originalPotential instanceof TreeADDPotential;
                Potential newSubPotential = isOriginalPotentialATreeADD ? originalSubPotential.copy() : newPotential;
				int position = getPosition(originalSubPotentialTable, uncertainParameter.uncertainValue);
				int posUncertainInColumn = calculatePositionUncertainInColumn(originalSubPotentialTable, position, isSubPotentialExactDistrPotential);
				int originalValuesLength = originalSubPotentialTable.getTableSize();
				TablePotential newTablePotential = addVariableReplicatingValuesAndUncertainValues(originalSubPotentialTable,
						iterVariable);
				List<Variable> newTablePotentialVariables = newTablePotential.getVariables();
				List<Variable> newSubPotentialVariables = new ArrayList<>();
				if (isSubPotentialExactDistrPotential) {
					newSubPotentialVariables.add(originalSubPotential.getVariable(0));
				}
				newSubPotentialVariables.addAll(newTablePotentialVariables);
				newSubPotential.setVariables(newSubPotentialVariables);
				TablePotential newSubPotentialTable = newTablePotential;
				if (newSubPotentialTable != newSubPotential) {
					newSubPotentialTable.setVariables(newTablePotentialVariables);
				}
				double min = parameter.min;
				double pointsDistance = (parameter.max - min) / numIntervals;
				int numStates = numElementsInColumn(originalSubPotential);
				int configurationBasePositionInitColumn = position - posUncertainInColumn;
				List<UncertainValue> columnUncertainValues = getUncertainValuesChance(
						originalSubPotentialTable.getUncertainValues(), configurationBasePositionInitColumn, numStates);
				Sampler sampler = new SystematicSampling();
				double[] sampledConfigurationValues = sampler.generateSample(columnUncertainValues, numStates, functionTypes);
				double[] auxSampledConfigurationValues = new double[numStates];
				double auxValueToAssign = min;
				double[] newSubpotentialTableValues = newSubPotentialTable.getValues();
				for (int i = 0; i < numPoints; i++) {
					System.arraycopy(sampledConfigurationValues, 0, auxSampledConfigurationValues, 0, numStates);
					replaceValueAndRedistributeComplements(auxSampledConfigurationValues, sampler, posUncertainInColumn,
							auxValueToAssign);
					copyInArray(newSubpotentialTableValues, configurationBasePositionInitColumn + i * originalValuesLength,
							auxSampledConfigurationValues);
					//TODO Distribute the probability mass when changing one value
					/*newSubPotential.getValues()[position + i * originalValuesLength] = min
							+ i * pointsDistance;*/
					auxValueToAssign += pointsDistance;
				}
                TablePotential auxTablePotential = getPotentialTable(newSubPotential);
				auxTablePotential.setValues(newSubpotentialTableValues);
				/*TODO When we sample a potential value, the uncertain values of the resulting potential is set to empty. In the future we could just set to the empty
				the sampled parameter, and keep the rest of uncertain values parameters.
				*/
				auxTablePotential.setUncertainValues(new UncertainValue[newSubpotentialTableValues.length]);
				if (isOriginalPotentialATreeADD) {
					newPotential.addVariable(iterVariable);
					replace((TreeADDPotential)originalPotential, (TreeADDPotential) newPotential, originalSubPotential, newSubPotential);
				}
				net.removePotential(originalPotential);
				net.addPotential(newPotential);
			}
		}
		return net;
	}
	
	/**
	 *  Method used in by the CESpiderDialog class in "sensitivityanalysis" repo
	 */
	private static ProbNet networkSample(ProbNet originalNet, List<ParameterAnalysisInformation> parameters, int numIntervals, int interval) {
		
		ProbNet net = originalNet.copy();

		UncertainParameter uncertainParameter;
		int numPoints = numIntervals + 1;
		List<Class<? extends ProbDensFunction>> functionTypes = initializeTypeFunctions();

		for (ParameterAnalysisInformation parameter : parameters) {
			uncertainParameter = parameter.uncertainParameter;
			if (uncertainParameter != null) {
				String iterationVariableName = parameter.iterationVariableName;
				Variable iterVariable = new Variable(iterationVariableName, numPoints);
				Potential originalPotential = uncertainParameter.potential;
				Potential newPotential = originalPotential.copy();
				Potential originalSubPotential = uncertainParameter.subPotential;
                boolean isSubPotentialExactDistrPotential = originalSubPotential instanceof ExactDistrPotential;
                TablePotential originalSubPotentialTable = getPotentialTable(originalSubPotential);
                boolean isOriginalPotentialATreeADD = originalPotential instanceof TreeADDPotential;
                Potential newSubPotential = isOriginalPotentialATreeADD ? originalSubPotential.copy() : newPotential;
				int position = getPosition(originalSubPotentialTable, uncertainParameter.uncertainValue);
				int posUncertainInColumn = calculatePositionUncertainInColumn(originalSubPotentialTable, position, isSubPotentialExactDistrPotential);
				
				List<Variable> newTablePotentialVariables = originalSubPotentialTable.getVariables();
				List<Variable> newSubPotentialVariables = new ArrayList<>();
				if (isSubPotentialExactDistrPotential) {
					newSubPotentialVariables.add(originalSubPotential.getVariable(0));
				}
				newSubPotentialVariables.addAll(newTablePotentialVariables);
				newSubPotential.setVariables(newSubPotentialVariables);
				TablePotential newSubPotentialTable = originalSubPotentialTable;
				if (newSubPotentialTable != newSubPotential) {
					newSubPotentialTable.setVariables(newTablePotentialVariables);
				}
				double min = parameter.min;
				double pointsDistance = (parameter.max - min) / numIntervals;
				int numStates = numElementsInColumn(originalSubPotential);
				int configurationBasePositionInitColumn = position - posUncertainInColumn;
				List<UncertainValue> columnUncertainValues = getUncertainValuesChance(
						originalSubPotentialTable.getUncertainValues(), configurationBasePositionInitColumn, numStates);
				Sampler sampler = new SystematicSampling();
				double[] sampledConfigurationValues = sampler.generateSample(columnUncertainValues, numStates, functionTypes);
				double[] auxSampledConfigurationValues = new double[numStates];
				double auxValueToAssign = min+(pointsDistance*interval);
				//Returns the net to its original potentials (after giving the last net with the last intervals potentials)
				//for the next uncertainParameter. This is necessary because even if you operate on a copy of the original network
				//with "net.copy()", the changes are transferred to the original network. 
				if(interval > numIntervals) {
					auxValueToAssign = uncertainParameter.getBaseLineValue();
				}
				double[] newSubpotentialTableValues = newSubPotentialTable.getValues();
								
				System.arraycopy(sampledConfigurationValues, 0, auxSampledConfigurationValues, 0, numStates);
				replaceValueAndRedistributeComplements(auxSampledConfigurationValues, sampler, posUncertainInColumn,
							auxValueToAssign);					
				copyInArray(newSubpotentialTableValues, configurationBasePositionInitColumn,
                            auxSampledConfigurationValues);
                
                TablePotential auxTablePotential = getPotentialTable(newSubPotential);
				auxTablePotential.setValues(newSubpotentialTableValues);
				auxTablePotential.setUncertainValues(new UncertainValue[newSubpotentialTableValues.length]);
				
				if (isOriginalPotentialATreeADD) {
					newPotential.addVariable(iterVariable);
					replace((TreeADDPotential)originalPotential, (TreeADDPotential) newPotential, originalSubPotential, newSubPotential);
				}				
			}
		}
		return net;
	}
	

	private static void replaceValueAndRedistributeComplements(double[] samples, Sampler sampler, int posToReplace,
			double newValue) {
		double oldValue = samples[posToReplace];
		samples[posToReplace] = newValue;
		ComplementFamily complemFamily = sampler.samplerUncertainValues.complementFamily;
		ComplementFamily auxComplementFamily = new ComplementFamily(complemFamily.family);
		auxComplementFamily.setProbMass(complemFamily.getProbMass() + oldValue - newValue);
		double[] newComplementSamples = auxComplementFamily.getSample();
		placeInArray(samples, sampler.samplerUncertainValues.indexesComplement, newComplementSamples);
	}

	private static int calculatePositionUncertainInColumn(TablePotential potential, int position, boolean isOriginalPotentialAnExactDistrPotential) {
		int posInCol;
		//if (potential.getVariable(0).getVariableType().equals(VariableType.NUMERIC)) {
		if (isOriginalPotentialAnExactDistrPotential) {
			posInCol = 0;
		} else {//Probability potential
            Variable variable = potential.getVariable(0);
            posInCol = position % variable.getNumStates();
		}
		return posInCol;
	}

	/**
	 * @param originalNet           Original network
	 * @param uncertainParameter    Uncertain parameter
	 * @param min                   Min
	 * @param max                   Max
	 * @param numIntervals          Number of intervals
	 * @param iterationVariableName Conditioned variable
	 * @return The original network "net" where the sub potential table where "parameterName" appears has been conditioned in the variable
	 * "interationVariable" name, and for state of this variable the cell of the parameter has been assigned one of the equally distant
	 * points between min and max.
	 */
	public static ProbNet sampleNetwork(ProbNet originalNet, UncertainParameter uncertainParameter, double min,
			double max, int numIntervals, String iterationVariableName) {
		List<ParameterAnalysisInformation> parameters = Collections
				.singletonList(new ParameterAnalysisInformation(uncertainParameter, min, max, iterationVariableName));
		return SystematicSampling.sampleNetwork(originalNet, parameters, numIntervals);
	}

	public static ProbNet sampleNetwork(ProbNet originalNet, UncertainParameter uncertainParameter1, double min1,
			double max1, UncertainParameter uncertainParameter2, double min2, double max2, int numIntervals,
			String iterationVariableName1, String iterationVariableName2) {
		List<ParameterAnalysisInformation> parameters = Arrays
				.asList(new ParameterAnalysisInformation(uncertainParameter1, min1, max1, iterationVariableName1),
						new ParameterAnalysisInformation(uncertainParameter2, min2, max2, iterationVariableName2));
		return SystematicSampling.sampleNetwork(originalNet, parameters, numIntervals);
	}	
	
	/**
	 * Returns the original network with the sub-potential table where "parameterName" appears has been 
	 * conditioned in the variable "iterationVariable" name, replaced by the potential in the interval
	 * of uncertainty corresponding to the point indicated by pointOfInterval.
	 * The uncertainty interval is divided into as many equally distant intervals between 'min' and 'max'
	 * as the number of iterations 
	 */
	
	public static ProbNet networkSample(ProbNet originalNet, UncertainParameter uncertainParameter, double min,
			double max, int numIntervals, String iterationVariableName, int interval) {
		List<ParameterAnalysisInformation> parameters = Collections
				.singletonList(new ParameterAnalysisInformation(uncertainParameter, min, max, iterationVariableName));
		return SystematicSampling.networkSample(originalNet, parameters, numIntervals, interval);
	}

	private static void replace(TreeADDPotential originalPotential, TreeADDPotential newPotential, Potential subPotToReplace, Potential newSubPot) {

		List<TreeADDBranch> branches = originalPotential.getBranches();
		if (branches != null)
			for (int i=0;i<branches.size();i++) {
				TreeADDBranch treeADDBranch = branches.get(i);
				if (treeADDBranch != null) {
					replace(treeADDBranch, newPotential.getBranches().get(i), subPotToReplace, newSubPot);
				}
			}
	}

	private static void replace(TreeADDBranch treeADDBranchOrig, TreeADDBranch treeADDBranchNew, Potential subPotToReplace, Potential newSubPot) {
		Potential potential = treeADDBranchOrig.getPotential();
		if (potential == subPotToReplace) {
			treeADDBranchNew.setPotential(newSubPot);
		} else {
            if (potential instanceof TreeADDPotential) {
				replace((TreeADDPotential) potential, (TreeADDPotential) treeADDBranchNew.getPotential(), subPotToReplace, newSubPot);
			}
		}
	}

	public static UncertainParameter getUncertainParameter(ProbNet net, String parameterName) {
		List<UncertainParameter> uncertainParameters = SystematicSampling.getUncertainParameters(net);
		return getUncertainParameter(uncertainParameters, parameterName);
	}

	/**
	 * @param potential      Potential
	 * @param uncertainValue Uncertain value
	 * @return The position in the uncertain values table of "potential" where "uncertainValue" is placed
	 */
	private static int getPosition(TablePotential potential, UncertainValue uncertainValue) {
		int pos = -1;
		boolean found = false;
		UncertainValue[] uncertainValues = potential.getUncertainValues();
		for (int i = 0; i < uncertainValues.length && !found; i++) {
			found = uncertainValues[i] == uncertainValue;
			if (found) {
				pos = i;
			}
		}
		return pos;
	}

	/*public static TablePotential getExpectedUtilitySystematicSampling(ProbNet net, String parameterName, double min, double max,
			 int numIntervals, String iterationVariableName){
		ProbNet newNet = sampleNetwork(net,parameterName,min,max,numIntervals,iterationVariableName);
		Variable iterVariable = null;
		try {
			iterVariable = newNet.getVariable(iterationVariableName);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		VariableElimination algorithm = null;
		try {
			algorithm = new VariableElimination(newNet);
		} catch (NotEvaluableNetworkException e) {
			e.printStackTrace();
		}
		algorithm.setConditioningVariables(Arrays.asList(iterVariable));
		TablePotential globalUtility = null;
		try {
			globalUtility = algorithm.getGlobalUtility();
		} catch (IncompatibleEvidenceException e) {
			e.printStackTrace();
		} catch (UnexpectedInferenceException e) {
			e.printStackTrace();
		}
		return globalUtility;
		
	}*/

	/**
	 * @param uncertainParameters List of uncertain parameters
	 * @param parameterName       Parameter name
	 * @return The UncertainParameter that corresponds to "parameterName"
	 */
	private static UncertainParameter getUncertainParameter(List<UncertainParameter> uncertainParameters,
			String parameterName) {
		UncertainParameter paramFound = null;
		boolean found = false;
		for (int i = 0; i < uncertainParameters.size() && !found; i++) {
			UncertainParameter auxUncertain = uncertainParameters.get(i);
			String auxName = auxUncertain.uncertainValue.getName();
			if (auxName != null && auxName.equals(parameterName)) {
				found = true;
				paramFound = auxUncertain;
			}
		}
		return paramFound;
	}

	@Override protected double[] getSample(FamilyDistribution family, Random randomGenerator) {
		return family.getMean();
	}

	@Override protected Random createRandomGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	private static class SubPotentialAndPositionInTablePotential {

		private final Potential subPotential;
		private final int position;

		public SubPotentialAndPositionInTablePotential(Potential subPotential, int position) {
			this.subPotential = subPotential;
			this.position = position;
		}

		public Potential getSubPotential() {
			return subPotential;
		}

		public int getPosition() {
			return position;
		}

	}

	private static class ParameterAnalysisInformation {
		final UncertainParameter uncertainParameter;
		final double min;
		final double max;
		final String iterationVariableName;

		public ParameterAnalysisInformation(UncertainParameter uncertainParameter, double min, double max,
				String iterationVariableName) {
			this.uncertainParameter = uncertainParameter;
			this.min = min;
			this.max = max;
			this.iterationVariableName = iterationVariableName;
		}

	}
}