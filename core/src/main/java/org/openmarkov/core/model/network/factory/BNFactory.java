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
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

public class BNFactory extends NetsFactory {

	/**
	 * @return a Bayesian network with one node node (Disease)
	 */
	public static ProbNet createBN_X(double prevalence) {
        
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

		// Define the variables
		Variable variableX = new Variable(diseaseName, diseaseStates);

		addVariables(probNet, NodeType.CHANCE, variableX);
        
        double[] valuesX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(role, valuesX, variableX);

		addPotentials(probNet, potentialX);

		return probNet;
	}

	/**
	 * @return a Bayesian network with two nodes (X and Y) and a link X -> Y
	 */
	public static ProbNet createBN_XY(double prevalence, double sensitivity, double specificity) {
		return createBN_XY("X", "Y", prevalence, sensitivity, specificity);
	}

	/**
	 * @return a Bayesian network with two nodes (X and Y) and a link X -> Y
	 */
	public static ProbNet createBN_XY(String nameX, String nameY, double prevalence, double sensitivity,
			double specificity) {
        
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

		// Define the variables
		Variable variableX = new Variable(nameX, diseaseStates);
		Variable variableY = new Variable(nameY, testResultStates);

		addVariables(probNet, NodeType.CHANCE, variableX, variableY);

		probNet.addLink(variableX, variableY, true);
        
        double[] valuesX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(role, valuesX, variableX);
        
        double[] valuesYX = valuesCPTResultTest(sensitivity, specificity);
		TablePotential potentialYX = createTablePotential(role, valuesYX, variableY, variableX);

		addPotentials(probNet, potentialX, potentialYX);

		return probNet;
	}

	/**
	 * @return a Bayesian network with three nodes (X, Y and Z) and two links X -> Y, and Y -> Z
	 */
	public static ProbNet createBN_XYZ(double prevalence, double sensitivityY, double specificityY, double sensitivityZ,
			double specificityZ) {
        
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());

		// Define the variables
		Variable variableX = new Variable("X", diseaseStates);
		Variable variableY = new Variable("Y", testResultStates);
		Variable variableZ = new Variable("Z", testResultStates);

		addVariables(probNet, NodeType.CHANCE, variableX, variableY, variableZ);

		probNet.addLink(variableX, variableY, true);
        
        double[] valuesX = valuesAPrioriDisease(prevalence);
		TablePotential potentialX = createTablePotential(role, valuesX, variableX);
        
        double[] valuesYX = valuesCPTResultTest(sensitivityY, specificityY);
		TablePotential potentialYX = createTablePotential(role, valuesYX, variableY, variableX);
        
        double[] valuesZY = valuesCPTResultTest(sensitivityZ, specificityZ);
		TablePotential potentialZY = createTablePotential(role, valuesZY, variableZ, variableY);

		addPotentials(probNet, potentialX, potentialYX, potentialZY);

		return probNet;
	}

	/**
	 * @return A Bayesian network with three nodes (A, B and C) and two links A -> B, and A -> C.
	 * This network was stored in file "peque.elv"
	 */
	public static ProbNet createBN_ABC() {
        
        ProbNet peque = new ProbNet();
        
        String[] nameStates = diseaseStates;
		//Finite States variables}
        Variable variableA = new Variable("A", nameStates);
        Variable variableB = new Variable("B", nameStates);
        Variable variableC = new Variable("C", nameStates);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";

		setAdditionalProperties(relevance, value, variableA);

		variableA.setAdditionalProperty(relevance, value);
		variableB.setAdditionalProperty(relevance, value);
		variableC.setAdditionalProperty(relevance, value);

		addVariables(peque, NodeType.CHANCE, variableA, variableB, variableC);

		//Potentials
		//PotentialType type = PotentialType.TABLE;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;

		//Potential A
        double[] tableA = valuesAPrioriDisease(0.8);
		TablePotential potentialA = createTablePotential(role, tableA, variableA);

		//Potential BA
        double[] tableBA = valuesCPTResultTest(0.1, 0.7);
		TablePotential potentialBA = createTablePotential(role, tableBA, variableB, variableA);

		//potencial CAB
		double[] tableCAB = { 0.02, 0.98, 0.71, 0.29, 0.16, 0.84, 0.85, 0.15 };
		TablePotential potentialCAB = createTablePotential(role, tableCAB, variableC, variableA, variableB);

		NodeType nodeType = NodeType.CHANCE;

		addVariables(peque, nodeType, variableA, variableB, variableC);

		//Links throws NodeNotFoundException
        peque.addLink(variableA, variableB, true);
        peque.addLink(variableA, variableC, true);
        peque.addLink(variableB, variableC, true);
        
        addPotentials(peque, potentialA, potentialBA, potentialCAB);

		return peque;
	}

	/**
	 * @return A Bayesian network with three nodes (A, B and C) and two links A -> B, and A -> C.
	 * This network was stored in file "peque.elv"
	 */
	public static ProbNet createBN_Asia() {
        
        ProbNet network = new ProbNet();

		//Finite States variables
		//"Visit to Asia"
        Variable variableA = new Variable("A", yesNoStates);
		//"Smoker"
        Variable variableS = new Variable("S", yesNoStates);
		//"Tuberculosis"
        Variable variableT = new Variable("T", diseaseStates);
		//"Lung Cancer"
        Variable variableL = new Variable("L", diseaseStates);
		//"Bronchitis"
        Variable variableB = new Variable("B", diseaseStates);
		//"Tuberculosis or Cancer"
        Variable variableTOrC = new Variable("TOrC", yesNoStates);
		//"Positive X-ray"
        Variable variableX = new Variable("X", yesNoStates);
		//"Dyspnea"
        Variable variableD = new Variable("D", yesNoStates);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";

		addVariables(network, NodeType.CHANCE, variableA, variableS, variableT, variableL, variableB, variableTOrC,
				variableX, variableD);

		List<Variable> variables2 = network.getVariables();
        setAdditionalProperties(relevance, value, variables2.toArray(new Variable[variables2.size()]));

		//Potentials
		//PotentialType type = PotentialType.TABLE;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;

		//Potential A
		double[] tableA = { 0.01, 0.99 };
		TablePotential potentialA = createTablePotential(role, tableA, variableA);

		//Potential S
		double[] tableS = { 0.5, 0.5 };
		TablePotential potentialS = createTablePotential(role, tableS, variableS);

		//Potential T
		double[] tableT = { 0.05, 0.95, 0.01, 0.99 };
		TablePotential potentialT = createTablePotential(role, tableT, variableT, variableA);

		//Potential L
		double[] tableL = { 0.1, 0.9, 0.01, 0.99 };
		TablePotential potentialL = createTablePotential(role, tableL, variableL, variableS);

		//Potential B
		double[] tableB = { 0.6, 0.4, 0.3, 0.7 };
		TablePotential potentialB = createTablePotential(role, tableB, variableB, variableS);

		//Potential TOrC
		double[] tableTOrC = { 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0 };
		TablePotential potentialTOrC = createTablePotential(role, tableTOrC, variableTOrC, variableL, variableT);

		//Potential X
		double[] tableX = { 0.98, 0.02, 0.05, 0.95 };
		TablePotential potentialX = createTablePotential(role, tableX, variableX, variableTOrC);

		//Potential D
		double[] tableD = { 0.9, 0.1, 0.7, 0.3, 0.8, 0.2, 0.1, 0.9 };
		TablePotential potentialD = createTablePotential(role, tableD, variableD, variableTOrC, variableB);

		addPotentials(network, potentialA, potentialS, potentialT, potentialL, potentialB, potentialTOrC, potentialX,
				potentialD);

		return network;
	}

}
