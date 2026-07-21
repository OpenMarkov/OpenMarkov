/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.factory;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author manolo
 * This class is used for building ProbNets corresponding to some networks used in the tests.
 * Networks are built via Java sentences not requiring any parser.
 */
public class NetsFactory {

	public static final String diseaseName = "Disease";
	public static final String testResultName = "Result of test";
    public static final String[] diseaseStates = {"present", "absent"};
    
    public static final String[] testResultStates = {"positive", "negative"};
    
    public static final String[] yesNoStates = {"yes", "no"};

	/**
	 * @param variables the variables
	 * @return An ArrayList containing the variables
	 */
	private static List<Variable> createVariableList(Variable... variables) {
		List<Variable> list = new ArrayList<>();
        Collections.addAll(list, variables);
		return list;

	}

	protected static double[] valuesAPrioriDisease(double prevalence) {
        
        double[] values = new double[2];
		values[0] = prevalence;
		values[1] = 1.0 - prevalence;

		return values;
	}

	protected static double[] valuesCPTResultTest(double sensitivity, double specificity) {

		double[] values = { sensitivity, 1.0 - sensitivity, 1.0 - specificity, specificity };
		//double [] values = {specificity, 1.0-specificity, 1.0-sensitivity, sensitivity};

		return values;
	}

	protected static double[] valuesCPTResultTestDecisionTestYXT(double sensitivity, double specificity) {

		double[] values = { sensitivity, 1.0 - sensitivity, 0.0, 1.0 - specificity, specificity, 0.0, 0.0, 0.0, 1.0,
				0.0, 0.0, 1.0 };

		return values;
	}

	/**
	 * @param net the net
	 * @param potentials It adds a list of potentials to the network.
	 */
	protected static void addPotentials(ProbNet net, Potential... potentials) {
		for (int i = 0; i < potentials.length; i++) {
			net.addPotential(potentials[i]);
		}
	}

	/**
	 * @param net       Network
	 * @param nodeType  The type of node
	 * @param variables List of variables to add
	 *                  It adds a list of variables to the network.
	 */
	protected static void addVariables(ProbNet net, NodeType nodeType, Variable... variables) {
		for (int i = 0; i < variables.length; i++) {
			net.addNode(variables[i], nodeType);
		}
	}

	/**
	 * @param role      Role of the potential
	 * @param values    Values of the potential
	 * @param variables Variables
	 * @return A TablePotential
	 */
	protected static TablePotential createTablePotential(PotentialRole role, double[] values, Variable... variables) {

		return new TablePotential(createVariableList(variables), role, values);
	}

	/**
	 * @param role      Role of the potential
	 * @param values    Values of the potential
	 * @param variables Variables
	 * @return A TablePotential
	 */
	protected static ExactDistrPotential createExactDistrPotential(PotentialRole role, double[] values,
			Variable... variables) {

		return new ExactDistrPotential(createVariableList(variables), role, values);
	}

	/**
	 * Create utility potential
	 *
	 * @param varSV the var sv
	 * @param parents Variables
	 * @return A TablePotential
	 */
	protected static SumPotential createSumPotential(Variable varSV, Variable... parents) {
		List<Variable> variables = Arrays.asList(varSV);
		variables.addAll(createVariableList(parents));
		return new SumPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
	}

	/**
	 * @param relevance the relevance
	 * @param value the value
	 * @param variables It sets the relevance to a set of variables
	 */
	protected static void setAdditionalProperties(String relevance, String value, Variable... variables) {
		for (Variable variable : variables) {
			variable.setAdditionalProperty(relevance, value);
		}

	}

	protected static TablePotential createPotentialDisease(double prevalence, PotentialRole roleProbability,
			Variable variableX) {
		double[] tableX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(roleProbability, tableX, variableX);
		return potentialX;
	}

	public enum NamesNetworks {
		//IDs
		ONE_CHANCE_ID, ONE_DECISION_ID, NO_KNOWLEDGE_ID, PERFECT_KNOWLEDGE_ID, DIAGNOSIS_ID, DECIDE_TEST_ID, DECIDE_TEST_SYMPTOM_ID,

		//DANs
		ONE_CHANCE_DAN, BLIND_TREATMENT_DAN, PERFECT_INFORMATION_TREATMENT_DAN, DECIDE_TEST_DAN, TWO_TEST_DAN, DIABETES_DAN, DATING_DAN, REACTOR_DAN, WOOER_DAN, PERFECT_INFORMATION_TREATMENT_RESTRICTED_DAN, MEDIASTINET_DAN, BRANCH_ACCEPT_DATING_SIMPLIFIED_DAN, DATING_TV_BAD_DAN, DATING_ACCEPT_NO_DAN, USED_CAR_BUYER_DAN, N_TESTS

	}

}
