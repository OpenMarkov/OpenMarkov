/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.factory;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.Arrays;
import java.util.List;

public class IDFactory extends NetsFactory {
	public static final String decTestName = "Do test?";
	public static final String therapyName = "Therapy";
	public static final String healthStateName = "Health state";
	public static final String therapyCostName = "Cost of therapy";
	public static final String testCostName = "Cost of test";

	/**
	 * @return An influence diagram without decisions, with only two nodes: X (chance) and U (utility).
	 */
	public static ProbNet createSimpleIDWithoutDecisions() {
        double[] util = {20, 90};
		return createSimpleIDWithoutDecisions(0.09, util);
	}

	/**
	 * @return An influence diagram without decisions, with only two nodes: X (chance) and U (utility).
	 */
	public static ProbNet createSimpleIDWithoutDecisions(double prevalence, double[] tableUX) {
        
        PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
        
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Define the variables
		Variable variableX = new Variable(diseaseName, diseaseStates);
		Variable variableU = new Variable(healthStateName);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, variableX);
		addVariables(probNet, NodeType.UTILITY, variableU);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableX, variableU);

		//Potential X
        TablePotential potentialX = createPotentialDisease(prevalence, roleProbability, variableX);
        
        ExactDistrPotential potentialU = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableUX, variableU, variableX);

		//Links throws NodeNotFoundException
        probNet.addLink(variableX, variableU, true);
        
        addPotentials(probNet, potentialX, potentialU);

		return probNet;
	}

	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 * The numerical parameters of this method are
	 */
	public static ProbNet createInfluenceDiagramDiagnosisProblem(double prevalence, double sensitivity,
			double specificity, double[] tableUXD) {
        
        PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
        
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Define the variables
		Variable variableX = new Variable(diseaseName, diseaseStates);
		Variable variableY = new Variable(testResultName, testResultStates);
		Variable variableD = new Variable(therapyName, yesNoStates);
		Variable variableU = new Variable(healthStateName);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, variableX, variableY);
		addVariables(probNet, NodeType.DECISION, variableD);
		addVariables(probNet, NodeType.UTILITY, variableU);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableX, variableY, variableD, variableU);

		//Potential X
        TablePotential potentialX = createPotentialDisease(prevalence, roleProbability, variableX);

		//Potential YX
        double[] tableYX = valuesCPTResultTest(sensitivity, specificity);
        TablePotential potentialY = createTablePotential(roleProbability, tableYX, variableY, variableX);
        
        ExactDistrPotential potentialU = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableUXD, variableU, variableX,
                                                                   variableD);

		//Links throws NodeNotFoundException
        probNet.addLink(variableX, variableY, true);
        probNet.addLink(variableY, variableD, true);
        probNet.addLink(variableX, variableU, true);
        probNet.addLink(variableD, variableU, true);
        
        addPotentials(probNet, potentialX, potentialY, potentialU);

		return probNet;
	}

	/**
	 * @return An influence diagram with three nodes: X, D and U. It represents a diagnosis problem without tests.
	 */
	public static ProbNet createIDNoKnowledge(double prevalence, double[] tableUXD) {
        
        PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
        
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Define the variables
		Variable variableX = new Variable(diseaseName, diseaseStates);
		Variable variableD = new Variable(therapyName, yesNoStates);
		Variable variableU = new Variable(healthStateName);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, variableX);
		addVariables(probNet, NodeType.DECISION, variableD);
		addVariables(probNet, NodeType.UTILITY, variableU);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableX, variableD, variableU);

		//Potential X
        TablePotential potentialX = createPotentialDisease(prevalence, roleProbability, variableX);
        
        ExactDistrPotential potentialU = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableUXD, variableU, variableX,
                                                                   variableD);

		//Links throws NodeNotFoundException
        probNet.addLink(variableX, variableU, true);
        probNet.addLink(variableD, variableU, true);
        
        addPotentials(probNet, potentialX, potentialU);

		return probNet;
	}

	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 */
	public static ProbNet createInfluenceDiagramDiagnosisProblem() {
		double prevalence = 0.07;
		double sensitivity = 0.91;
		double specificity = 0.97;
		double[] tableUXD = { 78.0, 88.0, 28.0, 98.0 };
		return createInfluenceDiagramDiagnosisProblem(prevalence, sensitivity, specificity, tableUXD);
	}

	/**
	 * @return An influence diagram with two nodes: D and U
	 */
	public static ProbNet buildIDOneDecision() {
		double[] tableUXD = { 87.4, 63.0 };
		return createIDOneDecision(tableUXD);
	}

	/**
	 * @return An influence diagram with two nodes: D and U
	 */
	public static ProbNet createIDOneDecision(double[] tableUD) {
        
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Define the variables
		Variable variableD = new Variable(therapyName, yesNoStates);
		Variable variableU = new Variable(healthStateName);

		//Add variables to the network
		addVariables(probNet, NodeType.DECISION, variableD);
		addVariables(probNet, NodeType.UTILITY, variableU);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableD, variableU);
        
        ExactDistrPotential potentialU = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableUD, variableU, variableD);

		//Links throws NodeNotFoundException
        probNet.addLink(variableD, variableU, true);
        
        addPotentials(probNet, potentialU);

		return probNet;
	}

	public static ProbNet buildIDNoKnowledge() {
		double[] tableUXD = { 8.0, 9.0, 3.0, 10.0 };
		return createIDNoKnowledge(0.14, tableUXD);

	}

	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 */
	public static ProbNet createUniformInfluenceDiagramDiagnosisProblem() {
		double sameUtility = 10;
		double sameProb = 0.5;
		double prevalence = sameProb;
		double sensitivity = sameProb;
		double specificity = sameProb;
		double[] tableUXD = { sameUtility, sameUtility, sameUtility, sameUtility };
		return createInfluenceDiagramDiagnosisProblem(prevalence, sensitivity, specificity, tableUXD);
	}

	/**
	 * @return An influence diagram with four nodes: X, Y, D and U. It represents a diagnosis problem.
	 * It is the example of influence diagram described in page 11 in the book available online at URL:
	 * http://www.cisiad.uned.es/techreports/decision-medicina.pdf
	 * The numerical parameters of this method are
	 *//*
    public static ProbNet createInfluenceDiagramDecisionTestProblem(
			double prevalence,
			double sensitivity,
			double specificity) {
			
			ProbNet probNet;
			
			SumPotential potentialU;
								
			probNet = createInfluenceDiagramDecisionTestProblemWithoutSV(prevalence,sensitivity,specificity);

			// Define the variables
			Variable variableU1 = null;
			try {
				variableU1 = probNet.getVariable("U1");
			} catch (NodeNotFoundException e1) {
				e1.printStackTrace();
			}
			Variable variableU2 = null;
			try {
				variableU2 = probNet.getVariable("U2");
			} catch (NodeNotFoundException e1) {
				e1.printStackTrace();
			}
			Variable variableU = new Variable("U");
			
			//Add variables to the network			
			addVariables(probNet,NodeType.UTILITY,variableU);
			
			//additional properties
			String relevance = new String("Relevance");
			String value = new String("7.0");				
			setAdditionalProperties(relevance,value,variableU);	
			
			//Potential U2
			potentialU = createSumPotential(variableU,variableU1,variableU2);
				
			//Links throws NodeNotFoundException
			try {
				probNet.addLink(variableU1, variableU, true);
				probNet.addLink(variableU2, variableU, true);
				
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
			
			addPotentials(probNet,potentialU);
			
			return probNet;
		}
*/
	public static ProbNet buildIDDecideTest() {
		return buildIDDecideTest(0.14, 0.91, 0.97);
	}

	public static ProbNet buildIDDecideTest(double prevalence, double sensitivity, double specificity) {
        
        PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
        double[] tableU1XD = {8.0, 9.0, 3.0, 10.0};
		double[] tableU2D = { -0.25, 0.0 };
		double[] tableU3T = { -0.2, 0.2 };
        
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Define the variables
		// Define the variables
		Variable variableX = new Variable(diseaseName, diseaseStates);
		Variable variableY = new Variable(testResultName, testResultStates[0], testResultStates[1], "noresult");
		Variable variableD = new Variable(therapyName, "yes", "no");
		Variable variableT = new Variable(decTestName, "yes", "no");
		Variable variableU1 = new Variable(healthStateName);
		Variable variableU2 = new Variable(therapyCostName);
		Variable variableU3 = new Variable(testCostName);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, variableX, variableY);
		addVariables(probNet, NodeType.DECISION, variableD, variableT);
		addVariables(probNet, NodeType.UTILITY, variableU1, variableU2, variableU3);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableX, variableY, variableD, variableT, variableU1, variableU2);

		//Potential X
        TablePotential potentialX = createPotentialDisease(prevalence, roleProbability, variableX);

		//Potential Y
        double[] tableYXT = valuesCPTResultTestDecisionTestYXT(sensitivity, specificity);
        TablePotential potentialY = createTablePotential(roleProbability, tableYXT, variableY, variableX, variableT);

		//Potential U1
        ExactDistrPotential potentialU1 = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableU1XD, variableU1, variableX,
                                                                    variableD);

		//Potential U2
        ExactDistrPotential potentialU2 = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableU2D, variableU2, variableD);

		//Potential U3
        ExactDistrPotential potentialU3 = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableU3T, variableU3, variableT);

		//Links throws NodeNotFoundException
        probNet.addLink(variableX, variableY, true);
        probNet.addLink(variableT, variableY, true);
        probNet.addLink(variableY, variableD, true);
        probNet.addLink(variableX, variableU1, true);
        probNet.addLink(variableD, variableU1, true);
        probNet.addLink(variableD, variableU2, true);
        probNet.addLink(variableT, variableU3, true);
        
        addPotentials(probNet, potentialX, potentialY, potentialU1, potentialU2, potentialU3);

		return probNet;
	}

	public static ProbNet buildIDSVDecideTestSymptom() {
		ProbNet probNet = buildIDDecideTestSymptom();

		List<Node> utilNodes = probNet.getNodes(NodeType.UTILITY);
        Variable[] utilVariables = new Variable[utilNodes.size()];

		for (int i = 0; i < utilNodes.size(); i++) {
			utilVariables[i] = utilNodes.get(i).getVariable();
		}

		Variable variableU = new Variable("U");

		// Add variables to the network
		addVariables(probNet, NodeType.UTILITY, variableU);

		// additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableU);

		// Potential U2
		SumPotential potentialU = createSumPotential(variableU, utilVariables);

		// Links throws NodeNotFoundException
        for (Variable utilVar : utilVariables) {
            probNet.addLink(utilVar, variableU, true);
        }
        addPotentials(probNet, potentialU);

		return probNet;
	}

	public static ProbNet buildIDDecideTestSymptom() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varDisease = new Variable("Disease", "absent", "present");
		Variable varResult_of_test = new Variable("Result of test", "not-performed", "negative", "positive");
		Variable varSymptom = new Variable("Symptom", "absent", "present");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varDo_test = new Variable("Do test?", "no", "yes");
		Variable varHealth_state = new Variable("Health state");
		Variable varCost_of_test = new Variable("Cost of test");
		Variable varCost_of_therapy = new Variable("Cost of therapy");

		// Nodes
		Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
		Node nodeResult_of_test = probNet.addNode(varResult_of_test, NodeType.CHANCE);
		Node nodeSymptom = probNet.addNode(varSymptom, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeDo_test = probNet.addNode(varDo_test, NodeType.DECISION);
		Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);
		Node nodeCost_of_test = probNet.addNode(varCost_of_test, NodeType.UTILITY);
		Node nodeCost_of_therapy = probNet.addNode(varCost_of_therapy, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDisease, nodeHealth_state, true);
		probNet.addLink(nodeDisease, nodeResult_of_test, true);
		probNet.addLink(nodeDisease, nodeSymptom, true);
		probNet.addLink(nodeResult_of_test, nodeTherapy, true);
		probNet.addLink(nodeSymptom, nodeDo_test, true);
		probNet.addLink(nodeTherapy, nodeHealth_state, true);
		probNet.addLink(nodeTherapy, nodeCost_of_therapy, true);
		probNet.addLink(nodeDo_test, nodeCost_of_test, true);
		probNet.addLink(nodeDo_test, nodeTherapy, true);
		probNet.addLink(nodeDo_test, nodeResult_of_test, true);

		// Potentials
		TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDisease.setValues(new double[] { 0.98, 0.02 });
		nodeDisease.setPotential(potDisease);

		TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varDisease, varDo_test),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potResult_of_test.setValues(new double[] { 1, 0, 0, 1, 0, 0, 0, 0.97, 0.03, 0, 0.09, 0.91 });
		nodeResult_of_test.setPotential(potResult_of_test);

		TablePotential potSymptom = new TablePotential(Arrays.asList(varSymptom, varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potSymptom.setValues(new double[] { 0.95, 0.05, 0.2, 0.8 });
		nodeSymptom.setPotential(potSymptom);

		ExactDistrPotential potHealth_state = new ExactDistrPotential(
				Arrays.asList(varHealth_state, varDisease, varTherapy));
		potHealth_state.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeHealth_state.setPotential(potHealth_state);

		ExactDistrPotential potCost_of_test = new ExactDistrPotential(Arrays.asList(varCost_of_test, varDo_test));
		potCost_of_test.getTablePotential().setValues(new double[] { 0, -0.2 });
		nodeCost_of_test.setPotential(potCost_of_test);

		ExactDistrPotential potCost_of_therapy = new ExactDistrPotential(Arrays.asList(varCost_of_therapy, varTherapy));
		potCost_of_therapy.getTablePotential().setValues(new double[] { 0, -0.25 });
		nodeCost_of_therapy.setPotential(potCost_of_therapy);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDTestAlways() {
		return buildIDTestAlways(0.14, 0.91, 0.97);
	}

	protected static ProbNet buildIDTestAlways(double prevalence, double sensitivity, double specificity) {
        
        PotentialRole roleProbability = PotentialRole.CONDITIONAL_PROBABILITY;
        double[] tableU1XD = {7.8, 8.8, 2.8, 9.8};
		double[] tableU2D = { -0.25, 0.0 };
        
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Define the variables
		// Define the variables
		Variable variableX = new Variable(diseaseName, diseaseStates);
		Variable variableY = new Variable(testResultName, testResultStates[0], testResultStates[1], "noresult");
		Variable variableD = new Variable(therapyName, "yes", "no");
		Variable variableU1 = new Variable(healthStateName);
		Variable variableU2 = new Variable(therapyCostName);

		//Add variables to the network
		addVariables(probNet, NodeType.CHANCE, variableX, variableY);
		addVariables(probNet, NodeType.DECISION, variableD);
		addVariables(probNet, NodeType.UTILITY, variableU1, variableU2);

		//additional properties
        String relevance = "Relevance";
        String value = "7.0";
		setAdditionalProperties(relevance, value, variableX, variableY, variableD, variableU1, variableU2);

		//Potential X
        TablePotential potentialX = createPotentialDisease(prevalence, roleProbability, variableX);

		//Potential Y
        double[] tableYXT = valuesCPTResultTestDecisionTestYXT(sensitivity, specificity);
        TablePotential potentialY = createTablePotential(roleProbability, tableYXT, variableY, variableX);

		//Potential U1
        ExactDistrPotential potentialU1 = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableU1XD, variableU1, variableX,
                                                                    variableD);

		//Potential U2
        ExactDistrPotential potentialU2 = createExactDistrPotential(PotentialRole.CONDITIONAL_PROBABILITY, tableU2D, variableU2, variableD);

		//Links throws NodeNotFoundException
        probNet.addLink(variableX, variableY, true);
        probNet.addLink(variableY, variableD, true);
        probNet.addLink(variableX, variableU1, true);
        probNet.addLink(variableD, variableU1, true);
        probNet.addLink(variableD, variableU2, true);
        
        addPotentials(probNet, potentialX, potentialY, potentialU1, potentialU2);

		return probNet;
	}

	public static ProbNet buildIDPerfectKnowledge() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
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
		probNet.addLink(nodeDisease, nodeTherapy, true);
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

		return probNet;
	}

	public static ProbNet buildIDPerfectKnowledgeCostTherapy() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varDisease = new Variable("Disease", "absent", "present");
		Variable varTherapy = new Variable("Therapy", "no", "yes");
		Variable varHealth_state = new Variable("Health state");
		Variable varCost_of_therapy = new Variable("Cost of therapy");

		// Nodes
		Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
		Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
		Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);
		Node nodeCost_of_therapy = probNet.addNode(varCost_of_therapy, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeDisease, nodeTherapy, true);
		probNet.addLink(nodeDisease, nodeHealth_state, true);
		probNet.addLink(nodeTherapy, nodeHealth_state, true);
		probNet.addLink(nodeTherapy, nodeCost_of_therapy, true);

		// Potentials
		TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDisease.setValues(new double[] { 0.86, 0.14 });
		nodeDisease.setPotential(potDisease);

		ExactDistrPotential potHealth_state = new ExactDistrPotential(
				Arrays.asList(varHealth_state, varDisease, varTherapy));
		potHealth_state.getTablePotential().setValues(new double[] { 10, 3, 9, 8 });
		nodeHealth_state.setPotential(potHealth_state);

		ExactDistrPotential potCost_of_therapy = new ExactDistrPotential(Arrays.asList(varCost_of_therapy, varTherapy));
		potCost_of_therapy.getTablePotential().setValues(new double[] { 0, -0.25 });
		nodeCost_of_therapy.setPotential(potCost_of_therapy);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDArthronet() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varIMC = new Variable("IMC", "no", "si");
		Variable varDiabetes = new Variable("Diabetes", "ausente", "presente");
		Variable varAlergia_ATB = new Variable("Alergia ATB", "no", "si");
		Variable varInfeccion_PTR = new Variable("Infeccion PTR", "ausente", "presente");
		Variable varVSG = new Variable("VSG", "valor =< 30 mm/h tras 6 meses", "valor > 30 mm/h tras 6 meses");
		Variable varPCR = new Variable("PCR", "valor =< 10 mg/l tras 3 semanas", "valor > 10 mg/l tras 3 semanas");
		Variable varGa67_Tc99 = new Variable("Ga67 Tc99", "Prueba no realizada", "Negativo", "Positivo");
		Variable varMovilidad = new Variable("Movilidad", "ROM > 65", "ROM =< 65");
		Variable varCortes_Congelados = new Variable("Cortes Congelados", "Prueba no realizada", "=< 5 PMFN",
				"> 5 PMFN");
		Variable varIsquemia = new Variable("Isquemia", "Implante no realizado", "Menor 1 hora y media",
				"Mayor 1 hora y media");
		Variable varCC_Drenaje = new Variable("CC_Drenaje", "Implante no realizado", "mayor 800 cc y menor 1000 cc",
				"menor 800 cc o mayor 1000 cc");
		Variable varRealizar_Implante = new Variable("Realizar Implante", "no", "si");
		Variable varRealizar_Gammagrafias = new Variable("Realizar Gammagrafias", "no", "si");
		Variable varRealizar_Biopsia_Sinovial = new Variable("Realizar Biopsia Sinovial", "no", "si");
		Variable varTratar_Infeccion_PTR = new Variable("Tratar Infeccion PTR", "no", "si");
		Variable varEVAC_Implante = new Variable("EVAC Implante");
		Variable varMolestias_Gammagrafia = new Variable("Molestias Gammagrafía");
		Variable varMolestias_Biopsia_Sinovial = new Variable("Molestias Biopsia Sinovial");
		Variable varEVAC_Total = new Variable("EVAC Total");
		Variable varCoste_Implante = new Variable("Coste Implante");
		Variable varCoste_Tratamiento = new Variable("Coste Tratamiento");
		Variable varCoste_Gammagrafia = new Variable("Coste Gammagrafia");
		Variable varCoste_Biopsia_Sinovial = new Variable("Coste Biopsia Sinovial");
		Variable varCoste_total = new Variable("Coste total");
		Variable varCoste_ajustado = new Variable("Coste ajustado");
		Variable varBeneficio_neto = new Variable("Beneficio neto");
		Variable varC2E = new Variable("C2E");
		Variable varMejora_Tratamiento = new Variable("Mejora Tratamiento");

		// Nodes
		Node nodeIMC = probNet.addNode(varIMC, NodeType.CHANCE);
		Node nodeDiabetes = probNet.addNode(varDiabetes, NodeType.CHANCE);
		Node nodeAlergia_ATB = probNet.addNode(varAlergia_ATB, NodeType.CHANCE);
		Node nodeInfeccion_PTR = probNet.addNode(varInfeccion_PTR, NodeType.CHANCE);
		Node nodeVSG = probNet.addNode(varVSG, NodeType.CHANCE);
		Node nodePCR = probNet.addNode(varPCR, NodeType.CHANCE);
		Node nodeGa67_Tc99 = probNet.addNode(varGa67_Tc99, NodeType.CHANCE);
		Node nodeMovilidad = probNet.addNode(varMovilidad, NodeType.CHANCE);
		Node nodeCortes_Congelados = probNet.addNode(varCortes_Congelados, NodeType.CHANCE);
		Node nodeIsquemia = probNet.addNode(varIsquemia, NodeType.CHANCE);
		Node nodeCC_Drenaje = probNet.addNode(varCC_Drenaje, NodeType.CHANCE);
		Node nodeRealizar_Implante = probNet.addNode(varRealizar_Implante, NodeType.DECISION);
		Node nodeRealizar_Gammagrafias = probNet.addNode(varRealizar_Gammagrafias, NodeType.DECISION);
		Node nodeRealizar_Biopsia_Sinovial = probNet.addNode(varRealizar_Biopsia_Sinovial, NodeType.DECISION);
		Node nodeTratar_Infeccion_PTR = probNet.addNode(varTratar_Infeccion_PTR, NodeType.DECISION);
		Node nodeEVAC_Implante = probNet.addNode(varEVAC_Implante, NodeType.UTILITY);
		Node nodeMolestias_Gammagrafia = probNet.addNode(varMolestias_Gammagrafia, NodeType.UTILITY);
		Node nodeMolestias_Biopsia_Sinovial = probNet.addNode(varMolestias_Biopsia_Sinovial, NodeType.UTILITY);
		Node nodeEVAC_Total = probNet.addNode(varEVAC_Total, NodeType.UTILITY);
		Node nodeCoste_Implante = probNet.addNode(varCoste_Implante, NodeType.UTILITY);
		Node nodeCoste_Tratamiento = probNet.addNode(varCoste_Tratamiento, NodeType.UTILITY);
		Node nodeCoste_Gammagrafia = probNet.addNode(varCoste_Gammagrafia, NodeType.UTILITY);
		Node nodeCoste_Biopsia_Sinovial = probNet.addNode(varCoste_Biopsia_Sinovial, NodeType.UTILITY);
		Node nodeCoste_total = probNet.addNode(varCoste_total, NodeType.UTILITY);
		Node nodeCoste_ajustado = probNet.addNode(varCoste_ajustado, NodeType.UTILITY);
		Node nodeBeneficio_neto = probNet.addNode(varBeneficio_neto, NodeType.UTILITY);
		Node nodeC2E = probNet.addNode(varC2E, NodeType.UTILITY);
		Node nodeMejora_Tratamiento = probNet.addNode(varMejora_Tratamiento, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeIMC, nodeDiabetes, true);
		probNet.addLink(nodeIMC, nodeInfeccion_PTR, true);
		probNet.addLink(nodeIMC, nodeRealizar_Implante, true);
		probNet.addLink(nodeDiabetes, nodeInfeccion_PTR, true);
		probNet.addLink(nodeDiabetes, nodeRealizar_Implante, true);
		probNet.addLink(nodeAlergia_ATB, nodeInfeccion_PTR, true);
		probNet.addLink(nodeAlergia_ATB, nodeRealizar_Implante, true);
		probNet.addLink(nodeInfeccion_PTR, nodePCR, true);
		probNet.addLink(nodeInfeccion_PTR, nodeVSG, true);
		probNet.addLink(nodeInfeccion_PTR, nodeCortes_Congelados, true);
		probNet.addLink(nodeInfeccion_PTR, nodeMovilidad, true);
		probNet.addLink(nodeInfeccion_PTR, nodeGa67_Tc99, true);
		probNet.addLink(nodeInfeccion_PTR, nodeMejora_Tratamiento, true);
		probNet.addLink(nodeVSG, nodeRealizar_Gammagrafias, true);
		probNet.addLink(nodePCR, nodeRealizar_Gammagrafias, true);
		probNet.addLink(nodeGa67_Tc99, nodeRealizar_Biopsia_Sinovial, true);
		probNet.addLink(nodeMovilidad, nodeRealizar_Gammagrafias, true);
		probNet.addLink(nodeCortes_Congelados, nodeTratar_Infeccion_PTR, true);
		probNet.addLink(nodeIsquemia, nodeInfeccion_PTR, true);
		probNet.addLink(nodeIsquemia, nodeRealizar_Gammagrafias, true);
		probNet.addLink(nodeCC_Drenaje, nodeInfeccion_PTR, true);
		probNet.addLink(nodeCC_Drenaje, nodeRealizar_Gammagrafias, true);
		probNet.addLink(nodeRealizar_Implante, nodeCC_Drenaje, true);
		probNet.addLink(nodeRealizar_Implante, nodeCoste_Implante, true);
		probNet.addLink(nodeRealizar_Implante, nodeEVAC_Implante, true);
		probNet.addLink(nodeRealizar_Implante, nodeInfeccion_PTR, true);
		probNet.addLink(nodeRealizar_Implante, nodeIsquemia, true);
		probNet.addLink(nodeRealizar_Gammagrafias, nodeCoste_Gammagrafia, true);
		probNet.addLink(nodeRealizar_Gammagrafias, nodeGa67_Tc99, true);
		probNet.addLink(nodeRealizar_Gammagrafias, nodeMolestias_Gammagrafia, true);
		probNet.addLink(nodeRealizar_Biopsia_Sinovial, nodeCortes_Congelados, true);
		probNet.addLink(nodeRealizar_Biopsia_Sinovial, nodeCoste_Biopsia_Sinovial, true);
		probNet.addLink(nodeRealizar_Biopsia_Sinovial, nodeMolestias_Biopsia_Sinovial, true);
		probNet.addLink(nodeTratar_Infeccion_PTR, nodeCoste_Tratamiento, true);
		probNet.addLink(nodeTratar_Infeccion_PTR, nodeMejora_Tratamiento, true);
		probNet.addLink(nodeEVAC_Implante, nodeEVAC_Total, true);
		probNet.addLink(nodeMolestias_Gammagrafia, nodeEVAC_Total, true);
		probNet.addLink(nodeMolestias_Biopsia_Sinovial, nodeEVAC_Total, true);
		probNet.addLink(nodeEVAC_Total, nodeBeneficio_neto, true);
		probNet.addLink(nodeCoste_Implante, nodeCoste_total, true);
		probNet.addLink(nodeCoste_Tratamiento, nodeCoste_total, true);
		probNet.addLink(nodeCoste_Gammagrafia, nodeCoste_total, true);
		probNet.addLink(nodeCoste_Biopsia_Sinovial, nodeCoste_total, true);
		probNet.addLink(nodeCoste_total, nodeCoste_ajustado, true);
		probNet.addLink(nodeCoste_ajustado, nodeBeneficio_neto, true);
		probNet.addLink(nodeC2E, nodeCoste_ajustado, true);
		probNet.addLink(nodeMejora_Tratamiento, nodeEVAC_Total, true);

		// Potentials
		TablePotential potIMC = new TablePotential(Arrays.asList(varIMC), PotentialRole.CONDITIONAL_PROBABILITY);
		potIMC.setValues(new double[] { 0.5, 0.5 });
		nodeIMC.setPotential(potIMC);

		TablePotential potDiabetes = new TablePotential(Arrays.asList(varDiabetes, varIMC),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potDiabetes.setValues(new double[] { 0.5, 0.5, 0.5, 0.5 });
		nodeDiabetes.setPotential(potDiabetes);

		TablePotential potAlergia_ATB = new TablePotential(Arrays.asList(varAlergia_ATB),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potAlergia_ATB.setValues(new double[] { 0.5, 0.5 });
		nodeAlergia_ATB.setPotential(potAlergia_ATB);

		TablePotential potInfeccion_PTR = new TablePotential(
				Arrays.asList(varInfeccion_PTR, varAlergia_ATB, varCC_Drenaje, varDiabetes, varIsquemia, varIMC,
						varRealizar_Implante), PotentialRole.CONDITIONAL_PROBABILITY);
		potInfeccion_PTR.setValues(new double[] { 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0,
				1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
				0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0,
				1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
				0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0,
				1, 0, 1, 0, 1, 0, 1, 0, 0.98958, 0.01042, 0.95069, 0.04931, 0.95031, 0.04969, 0.79511, 0.20489, 1, 0, 1,
				0, 0.97364, 0.02636, 0.88226, 0.11774, 0.88143, 0.11857, 0.60133, 0.39867, 1, 0, 1, 0, 0.73788, 0.26212,
				0.36355, 0.63645, 0.3617, 0.6383, 0.10312, 0.89688, 1, 0, 1, 0, 0.52248, 0.47752, 0.18168, 0.81832,
				0.18049, 0.81951, 0.04278, 0.95722, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
				0, 1, 0, 1, 0, 0.95734, 0.04266, 0.81995, 0.18005, 0.81876, 0.18124, 0.47826, 0.52174, 1, 0, 1, 0,
				0.89715, 0.10285, 0.63899, 0.36101, 0.63715, 0.36285, 0.2627, 0.7373, 1, 0, 1, 0, 0.39939, 0.60061,
				0.11889, 0.88111, 0.11805, 0.88195, 0.02644, 0.97356, 1, 0, 1, 0, 0.20538, 0.79462, 0.04983, 0.95017,
				0.04945, 0.95055, 0.01045, 0.98955 });
		nodeInfeccion_PTR.setPotential(potInfeccion_PTR);

		TablePotential potVSG = new TablePotential(Arrays.asList(varVSG, varInfeccion_PTR),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potVSG.setValues(new double[] { 0.85, 0.15, 0.18, 0.82 });
		nodeVSG.setPotential(potVSG);

		TablePotential potPCR = new TablePotential(Arrays.asList(varPCR, varInfeccion_PTR),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potPCR.setValues(new double[] { 0.74, 0.26, 0.11, 0.89 });
		nodePCR.setPotential(potPCR);

		TablePotential potGa67_Tc99 = new TablePotential(
				Arrays.asList(varGa67_Tc99, varInfeccion_PTR, varRealizar_Gammagrafias),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potGa67_Tc99.setValues(new double[] { 1, 0, 0, 1, 0, 0, 0, 0.86, 0.14, 0, 0.67, 0.33 });
		nodeGa67_Tc99.setPotential(potGa67_Tc99);

		TablePotential potMovilidad = new TablePotential(Arrays.asList(varMovilidad, varInfeccion_PTR),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potMovilidad.setValues(new double[] { 0.15, 0.85, 0.3, 0.7 });
		nodeMovilidad.setPotential(potMovilidad);

		TablePotential potCortes_Congelados = new TablePotential(
				Arrays.asList(varCortes_Congelados, varInfeccion_PTR, varRealizar_Biopsia_Sinovial),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potCortes_Congelados.setValues(new double[] { 1, 0, 0, 1, 0, 0, 0, 0.95, 0.05, 0, 0.16, 0.84 });
		nodeCortes_Congelados.setPotential(potCortes_Congelados);

		TablePotential potIsquemia = new TablePotential(Arrays.asList(varIsquemia, varRealizar_Implante),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potIsquemia.setValues(new double[] { 1, 0, 0, 0, 0.37276, 0.62724 });
		nodeIsquemia.setPotential(potIsquemia);

		TablePotential potCC_Drenaje = new TablePotential(Arrays.asList(varCC_Drenaje, varRealizar_Implante),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potCC_Drenaje.setValues(new double[] { 1, 0, 0, 0, 0.15054, 0.84946 });
		nodeCC_Drenaje.setPotential(potCC_Drenaje);

		ExactDistrPotential potEVAC_Implante = new ExactDistrPotential(
				Arrays.asList(varEVAC_Implante, varRealizar_Implante));
		potEVAC_Implante.getTablePotential().setValues(new double[] { 0, 4.64 });
		nodeEVAC_Implante.setPotential(potEVAC_Implante);

		ExactDistrPotential potMolestias_Gammagrafia = new ExactDistrPotential(
				Arrays.asList(varMolestias_Gammagrafia, varRealizar_Gammagrafias));
		potMolestias_Gammagrafia.getTablePotential().setValues(new double[] { 0, 0 });
		nodeMolestias_Gammagrafia.setPotential(potMolestias_Gammagrafia);

		ExactDistrPotential potMolestias_Biopsia_Sinovial = new ExactDistrPotential(
				Arrays.asList(varMolestias_Biopsia_Sinovial, varRealizar_Biopsia_Sinovial));
		potMolestias_Biopsia_Sinovial.getTablePotential().setValues(new double[] { 0, -0.00001 });
		nodeMolestias_Biopsia_Sinovial.setPotential(potMolestias_Biopsia_Sinovial);

		SumPotential potEVAC_Total = new SumPotential(
				Arrays.asList(varEVAC_Total, varEVAC_Implante, varMolestias_Gammagrafia, varMolestias_Biopsia_Sinovial,
						varMejora_Tratamiento), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeEVAC_Total.setPotential(potEVAC_Total);

		ExactDistrPotential potCoste_Implante = new ExactDistrPotential(
				Arrays.asList(varCoste_Implante, varRealizar_Implante));
		potCoste_Implante.getTablePotential().setValues(new double[] { 0, 6865.52 });
		nodeCoste_Implante.setPotential(potCoste_Implante);

		ExactDistrPotential potCoste_Tratamiento = new ExactDistrPotential(
				Arrays.asList(varCoste_Tratamiento, varTratar_Infeccion_PTR));
		potCoste_Tratamiento.getTablePotential().setValues(new double[] { 0, 39196.89 });
		nodeCoste_Tratamiento.setPotential(potCoste_Tratamiento);

		ExactDistrPotential potCoste_Gammagrafia = new ExactDistrPotential(
				Arrays.asList(varCoste_Gammagrafia, varRealizar_Gammagrafias));
		potCoste_Gammagrafia.getTablePotential().setValues(new double[] { 0, 335.08 });
		nodeCoste_Gammagrafia.setPotential(potCoste_Gammagrafia);

		ExactDistrPotential potCoste_Biopsia_Sinovial = new ExactDistrPotential(
				Arrays.asList(varCoste_Biopsia_Sinovial, varRealizar_Biopsia_Sinovial));
		potCoste_Biopsia_Sinovial.getTablePotential().setValues(new double[] { 0, 405.28 });
		nodeCoste_Biopsia_Sinovial.setPotential(potCoste_Biopsia_Sinovial);

		SumPotential potCoste_total = new SumPotential(
				Arrays.asList(varCoste_total, varCoste_Biopsia_Sinovial, varCoste_Gammagrafia, varCoste_Implante,
						varCoste_Tratamiento), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeCoste_total.setPotential(potCoste_total);

		ProductPotential potCoste_ajustado = new ProductPotential(
				Arrays.asList(varCoste_ajustado, varCoste_total, varC2E), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeCoste_ajustado.setPotential(potCoste_ajustado);

		SumPotential potBeneficio_neto = new SumPotential(
				Arrays.asList(varBeneficio_neto, varCoste_ajustado, varEVAC_Total),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeBeneficio_neto.setPotential(potBeneficio_neto);

		ExactDistrPotential potC2E = new ExactDistrPotential(Arrays.asList(varC2E));
		potC2E.getTablePotential().setValues(new double[] { -0.0000333 });
		nodeC2E.setPotential(potC2E);

		ExactDistrPotential potMejora_Tratamiento = new ExactDistrPotential(
				Arrays.asList(varMejora_Tratamiento, varInfeccion_PTR, varTratar_Infeccion_PTR));
		potMejora_Tratamiento.getTablePotential().setValues(new double[] { 0, -14.4, -4.64, -4.64192 });
		nodeMejora_Tratamiento.setPotential(potMejora_Tratamiento);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDMediastinet() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varN2_N3 = new Variable("N2_N3", "negative", "positive");
		Variable varCT_scan = new Variable("CT_scan", "negative", "positive");
		Variable varTBNA = new Variable("TBNA", "no_result", "negative", "positive");
		Variable varPET = new Variable("PET", "no_result", "negative", "positive");
		Variable varEBUS = new Variable("EBUS", "no_result", "negative", "positive");
		Variable varEUS = new Variable("EUS", "no_result", "negative", "positive");
		Variable varMED = new Variable("MED", "no_result", "negative", "positive");
		Variable varMED_Sv = new Variable("MED_Sv", "no", "yes");
		Variable varDecTBNA = new Variable("Dec:TBNA", "no", "yes");
		Variable varDecPET = new Variable("Dec:PET", "no", "yes");
		Variable varDecMED = new Variable("Dec:MED", "no", "yes");
		Variable varDecEBUSEUS = new Variable("Dec:EBUSEUS", "no_test", "eus", "ebus", "ebus_eus");
		Variable varTreatment = new Variable("Treatment", "palliative", "chemotherapy", "thoracotomy");
		Variable varSurvivors_QALE = new Variable("Survivors_QALE");
		Variable varInmediate_Survival = new Variable("Inmediate_Survival");
		Variable varMED_Survival = new Variable("MED_Survival");
		Variable varNet_QALE = new Variable("Net_QALE");
		Variable varTBNA_Morbidity = new Variable("TBNA_Morbidity");
		Variable varMED_Morbidity = new Variable("MED_Morbidity");
		Variable varEUS_Morbidity = new Variable("EUS_Morbidity");
		Variable varEBUS_Morbidity = new Variable("EBUS_Morbidity");
		Variable varTotal_QALE = new Variable("Total_QALE");
		Variable varEconomic_Cost_CT_scan = new Variable("Economic_Cost_CT_scan");
		Variable varEconomic_Cost_TBNA = new Variable("Economic_Cost_TBNA");
		Variable varEconomic_Cost_EBUS = new Variable("Economic_Cost_EBUS");
		Variable varEconomic_Cost_EUS = new Variable("Economic_Cost_EUS");
		Variable varEconomic_Cost_MED = new Variable("Economic_Cost_MED");
		Variable varEconomic_Cost_PET = new Variable("Economic_Cost_PET");
		Variable varEconomic_Cost_Treatment = new Variable("Economic_Cost_Treatment");
		Variable varTotal_Economic_Cost = new Variable("Total_Economic_Cost");
		Variable varC2E = new Variable("C2E");
		Variable varWeighted_Economic_Cost = new Variable("Weighted_Economic_Cost");
		Variable varNet_Effectiveness = new Variable("Net_Effectiveness");

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
		Node nodeDecEBUSEUS = probNet.addNode(varDecEBUSEUS, NodeType.DECISION);
		Node nodeTreatment = probNet.addNode(varTreatment, NodeType.DECISION);
		Node nodeSurvivors_QALE = probNet.addNode(varSurvivors_QALE, NodeType.UTILITY);
		Node nodeInmediate_Survival = probNet.addNode(varInmediate_Survival, NodeType.UTILITY);
		Node nodeMED_Survival = probNet.addNode(varMED_Survival, NodeType.UTILITY);
		Node nodeNet_QALE = probNet.addNode(varNet_QALE, NodeType.UTILITY);
		Node nodeTBNA_Morbidity = probNet.addNode(varTBNA_Morbidity, NodeType.UTILITY);
		Node nodeMED_Morbidity = probNet.addNode(varMED_Morbidity, NodeType.UTILITY);
		Node nodeEUS_Morbidity = probNet.addNode(varEUS_Morbidity, NodeType.UTILITY);
		Node nodeEBUS_Morbidity = probNet.addNode(varEBUS_Morbidity, NodeType.UTILITY);
		Node nodeTotal_QALE = probNet.addNode(varTotal_QALE, NodeType.UTILITY);
		Node nodeEconomic_Cost_CT_scan = probNet.addNode(varEconomic_Cost_CT_scan, NodeType.UTILITY);
		Node nodeEconomic_Cost_TBNA = probNet.addNode(varEconomic_Cost_TBNA, NodeType.UTILITY);
		Node nodeEconomic_Cost_EBUS = probNet.addNode(varEconomic_Cost_EBUS, NodeType.UTILITY);
		Node nodeEconomic_Cost_EUS = probNet.addNode(varEconomic_Cost_EUS, NodeType.UTILITY);
		Node nodeEconomic_Cost_MED = probNet.addNode(varEconomic_Cost_MED, NodeType.UTILITY);
		Node nodeEconomic_Cost_PET = probNet.addNode(varEconomic_Cost_PET, NodeType.UTILITY);
		Node nodeEconomic_Cost_Treatment = probNet.addNode(varEconomic_Cost_Treatment, NodeType.UTILITY);
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
		probNet.addLink(nodeCT_scan, nodeDecTBNA, true);
		probNet.addLink(nodeCT_scan, nodeEBUS, true);
		probNet.addLink(nodeCT_scan, nodeEUS, true);
		probNet.addLink(nodeCT_scan, nodeMED, true);
		probNet.addLink(nodeCT_scan, nodePET, true);
		probNet.addLink(nodeCT_scan, nodeTBNA, true);
		probNet.addLink(nodeTBNA, nodeDecPET, true);
		probNet.addLink(nodePET, nodeDecEBUSEUS, true);
		probNet.addLink(nodePET, nodeEBUS, true);
		probNet.addLink(nodePET, nodeEUS, true);
		probNet.addLink(nodePET, nodeMED, true);
		probNet.addLink(nodeEBUS, nodeDecMED, true);
		probNet.addLink(nodeEUS, nodeDecMED, true);
		probNet.addLink(nodeMED, nodeTreatment, true);
		probNet.addLink(nodeMED_Sv, nodeMED_Morbidity, true);
		probNet.addLink(nodeMED_Sv, nodeMED_Survival, true);
		probNet.addLink(nodeDecTBNA, nodeEconomic_Cost_TBNA, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA_Morbidity, true);
		probNet.addLink(nodeDecPET, nodeEconomic_Cost_PET, true);
		probNet.addLink(nodeDecPET, nodePET, true);
		probNet.addLink(nodeDecMED, nodeEconomic_Cost_MED, true);
		probNet.addLink(nodeDecMED, nodeMED, true);
		probNet.addLink(nodeDecMED, nodeMED_Sv, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEBUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEBUS_Morbidity, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEUS_Morbidity, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEconomic_Cost_EBUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEconomic_Cost_EUS, true);
		probNet.addLink(nodeTreatment, nodeEconomic_Cost_Treatment, true);
		probNet.addLink(nodeTreatment, nodeInmediate_Survival, true);
		probNet.addLink(nodeTreatment, nodeSurvivors_QALE, true);
		probNet.addLink(nodeSurvivors_QALE, nodeNet_QALE, true);
		probNet.addLink(nodeInmediate_Survival, nodeNet_QALE, true);
		probNet.addLink(nodeMED_Survival, nodeNet_QALE, true);
		probNet.addLink(nodeNet_QALE, nodeTotal_QALE, true);
		probNet.addLink(nodeTBNA_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeMED_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEUS_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEBUS_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeTotal_QALE, nodeNet_Effectiveness, true);
		probNet.addLink(nodeEconomic_Cost_CT_scan, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_TBNA, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_EBUS, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_EUS, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_MED, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_PET, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_Treatment, nodeTotal_Economic_Cost, true);
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
		potTBNA.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92143, 0.07857, 0, 0.90435, 0.09565, 0,
				0.98, 0.02, 0, 0.54032, 0.45968 });
		nodeTBNA.setPotential(potTBNA);

		TablePotential potPET = new TablePotential(Arrays.asList(varPET, varCT_scan, varN2_N3, varDecPET),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potPET.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92473, 0.07527, 0, 0.775, 0.225, 0,
				0.25974, 0.74026, 0, 0.09524, 0.90476 });
		nodePET.setPotential(potPET);

		TablePotential potEBUS = new TablePotential(Arrays.asList(varEBUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEBUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.97778, 0.02222, 0, 0.975, 0.025, 0, 0.96667, 0.03333, 0, 0.97368,
				0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811, 0.89189, 0, 0.11905, 0.88095, 0, 0.10811,
				0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121, 0.87879, 0, 0.97778, 0.02222, 0, 0.975,
				0.025, 0, 0.96667, 0.03333, 0, 0.97368, 0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811,
				0.89189, 0, 0.11905, 0.88095, 0, 0.10811, 0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121,
				0.87879 });
		nodeEBUS.setPotential(potEBUS);

		TablePotential potEUS = new TablePotential(Arrays.asList(varEUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0, 0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857,
				0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0, 0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935,
				0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0, 0.13889, 0.86111, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0,
				0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857, 0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0,
				0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935, 0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0,
				0.13889, 0.86111 });
		nodeEUS.setPotential(potEUS);

		TablePotential potMED = new TablePotential(Arrays.asList(varMED, varPET, varCT_scan, varN2_N3, varDecMED),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potMED.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.94444, 0.05556, 0, 0.9375, 0.0625, 0, 0.94737, 0.05263, 0, 0.92857,
				0.07143, 0, 0.94118, 0.05882, 0, 0.95, 0.05, 0, 0.27273, 0.72727, 0, 0.2, 0.8, 0, 0.21429, 0.78571, 0,
				0.1875, 0.8125, 0, 0.1875, 0.8125, 0, 0.2, 0.8 });
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

		ExactDistrPotential potEUS_Morbidity = new ExactDistrPotential(Arrays.asList(varEUS_Morbidity, varDecEBUSEUS));
		potEUS_Morbidity.getTablePotential().setValues(new double[] { 0, -0.03, 0, -0.03 });
		nodeEUS_Morbidity.setPotential(potEUS_Morbidity);

		ExactDistrPotential potEBUS_Morbidity = new ExactDistrPotential(
				Arrays.asList(varEBUS_Morbidity, varDecEBUSEUS));
		potEBUS_Morbidity.getTablePotential().setValues(new double[] { 0, 0, -0.03, -0.03 });
		nodeEBUS_Morbidity.setPotential(potEBUS_Morbidity);

		SumPotential potTotal_QALE = new SumPotential(
				Arrays.asList(varTotal_QALE, varEBUS_Morbidity, varEUS_Morbidity, varMED_Morbidity, varNet_QALE,
						varTBNA_Morbidity), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal_QALE.setPotential(potTotal_QALE);

		ExactDistrPotential potEconomic_Cost_CT_scan = new ExactDistrPotential(Arrays.asList(varEconomic_Cost_CT_scan));
		potEconomic_Cost_CT_scan.getTablePotential().setValues(new double[] { 670 });
		nodeEconomic_Cost_CT_scan.setPotential(potEconomic_Cost_CT_scan);

		ExactDistrPotential potEconomic_Cost_TBNA = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_TBNA, varDecTBNA));
		potEconomic_Cost_TBNA.getTablePotential().setValues(new double[] { 0, 80 });
		nodeEconomic_Cost_TBNA.setPotential(potEconomic_Cost_TBNA);

		ExactDistrPotential potEconomic_Cost_EBUS = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_EBUS, varDecEBUSEUS));
		potEconomic_Cost_EBUS.getTablePotential().setValues(new double[] { 0, 0, 620, 620 });
		nodeEconomic_Cost_EBUS.setPotential(potEconomic_Cost_EBUS);

		ExactDistrPotential potEconomic_Cost_EUS = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_EUS, varDecEBUSEUS));
		potEconomic_Cost_EUS.getTablePotential().setValues(new double[] { 0, 620, 0, 620 });
		nodeEconomic_Cost_EUS.setPotential(potEconomic_Cost_EUS);

		ExactDistrPotential potEconomic_Cost_MED = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_MED, varDecMED));
		potEconomic_Cost_MED.getTablePotential().setValues(new double[] { 0, 1620 });
		nodeEconomic_Cost_MED.setPotential(potEconomic_Cost_MED);

		ExactDistrPotential potEconomic_Cost_PET = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_PET, varDecPET));
		potEconomic_Cost_PET.getTablePotential().setValues(new double[] { 0, 2250 });
		nodeEconomic_Cost_PET.setPotential(potEconomic_Cost_PET);

		ExactDistrPotential potEconomic_Cost_Treatment = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_Treatment, varTreatment));
		potEconomic_Cost_Treatment.getTablePotential().setValues(new double[] { 3000, 11242, 19646 });
		nodeEconomic_Cost_Treatment.setPotential(potEconomic_Cost_Treatment);

		SumPotential potTotal_Economic_Cost = new SumPotential(
				Arrays.asList(varTotal_Economic_Cost, varEconomic_Cost_CT_scan, varEconomic_Cost_EBUS,
						varEconomic_Cost_EUS, varEconomic_Cost_MED, varEconomic_Cost_PET, varEconomic_Cost_TBNA,
						varEconomic_Cost_Treatment), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal_Economic_Cost.setPotential(potTotal_Economic_Cost);

		ExactDistrPotential potC2E = new ExactDistrPotential(Arrays.asList(varC2E));
		potC2E.getTablePotential().setValues(new double[] { -0.000033333333333 });
		nodeC2E.setPotential(potC2E);

		ProductPotential potWeighted_Economic_Cost = new ProductPotential(
				Arrays.asList(varWeighted_Economic_Cost, varC2E, varTotal_Economic_Cost),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeWeighted_Economic_Cost.setPotential(potWeighted_Economic_Cost);

		SumPotential potNet_Effectiveness = new SumPotential(
				Arrays.asList(varNet_Effectiveness, varTotal_QALE, varWeighted_Economic_Cost),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeNet_Effectiveness.setPotential(potNet_Effectiveness);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	/**
	 * @return A version of Mediastinet whose utility function only contains effectiveness nodes
	 */
	public static ProbNet buildIDQaleMediastinet() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varN2_N3 = new Variable("N2_N3", "negative", "positive");
		Variable varCT_scan = new Variable("CT_scan", "negative", "positive");
		Variable varTBNA = new Variable("TBNA", "no_result", "negative", "positive");
		Variable varPET = new Variable("PET", "no_result", "negative", "positive");
		Variable varEBUS = new Variable("EBUS", "no_result", "negative", "positive");
		Variable varEUS = new Variable("EUS", "no_result", "negative", "positive");
		Variable varMED = new Variable("MED", "no_result", "negative", "positive");
		Variable varMED_Sv = new Variable("MED_Sv", "no", "yes");
		Variable varDecTBNA = new Variable("Dec:TBNA", "no", "yes");
		Variable varDecPET = new Variable("Dec:PET", "no", "yes");
		Variable varDecMED = new Variable("Dec:MED", "no", "yes");
		Variable varDecEBUSEUS = new Variable("Dec:EBUSEUS", "no_test", "eus", "ebus", "ebus_eus");
		Variable varTreatment = new Variable("Treatment", "palliative", "chemotherapy", "thoracotomy");
		Variable varSurvivors_QALE = new Variable("Survivors_QALE");
		Variable varInmediate_Survival = new Variable("Inmediate_Survival");
		Variable varMED_Survival = new Variable("MED_Survival");
		Variable varNet_QALE = new Variable("Net_QALE");
		Variable varTBNA_Morbidity = new Variable("TBNA_Morbidity");
		Variable varMED_Morbidity = new Variable("MED_Morbidity");
		Variable varEUS_Morbidity = new Variable("EUS_Morbidity");
		Variable varEBUS_Morbidity = new Variable("EBUS_Morbidity");
		Variable varTotal_QALE = new Variable("Total_QALE");

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
		Node nodeDecEBUSEUS = probNet.addNode(varDecEBUSEUS, NodeType.DECISION);
		Node nodeTreatment = probNet.addNode(varTreatment, NodeType.DECISION);
		Node nodeSurvivors_QALE = probNet.addNode(varSurvivors_QALE, NodeType.UTILITY);
		Node nodeInmediate_Survival = probNet.addNode(varInmediate_Survival, NodeType.UTILITY);
		Node nodeMED_Survival = probNet.addNode(varMED_Survival, NodeType.UTILITY);
		Node nodeNet_QALE = probNet.addNode(varNet_QALE, NodeType.UTILITY);
		Node nodeTBNA_Morbidity = probNet.addNode(varTBNA_Morbidity, NodeType.UTILITY);
		Node nodeMED_Morbidity = probNet.addNode(varMED_Morbidity, NodeType.UTILITY);
		Node nodeEUS_Morbidity = probNet.addNode(varEUS_Morbidity, NodeType.UTILITY);
		Node nodeEBUS_Morbidity = probNet.addNode(varEBUS_Morbidity, NodeType.UTILITY);
		Node nodeTotal_QALE = probNet.addNode(varTotal_QALE, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeN2_N3, nodeCT_scan, true);
		probNet.addLink(nodeN2_N3, nodeEBUS, true);
		probNet.addLink(nodeN2_N3, nodeEUS, true);
		probNet.addLink(nodeN2_N3, nodeMED, true);
		probNet.addLink(nodeN2_N3, nodePET, true);
		probNet.addLink(nodeN2_N3, nodeSurvivors_QALE, true);
		probNet.addLink(nodeN2_N3, nodeTBNA, true);
		probNet.addLink(nodeCT_scan, nodeDecTBNA, true);
		probNet.addLink(nodeCT_scan, nodeEBUS, true);
		probNet.addLink(nodeCT_scan, nodeEUS, true);
		probNet.addLink(nodeCT_scan, nodeMED, true);
		probNet.addLink(nodeCT_scan, nodePET, true);
		probNet.addLink(nodeCT_scan, nodeTBNA, true);
		probNet.addLink(nodeTBNA, nodeDecPET, true);
		probNet.addLink(nodePET, nodeDecEBUSEUS, true);
		probNet.addLink(nodePET, nodeEBUS, true);
		probNet.addLink(nodePET, nodeEUS, true);
		probNet.addLink(nodePET, nodeMED, true);
		probNet.addLink(nodeEBUS, nodeDecMED, true);
		probNet.addLink(nodeEUS, nodeDecMED, true);
		probNet.addLink(nodeMED, nodeTreatment, true);
		probNet.addLink(nodeMED_Sv, nodeMED_Morbidity, true);
		probNet.addLink(nodeMED_Sv, nodeMED_Survival, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA_Morbidity, true);
		probNet.addLink(nodeDecPET, nodePET, true);
		probNet.addLink(nodeDecMED, nodeMED, true);
		probNet.addLink(nodeDecMED, nodeMED_Sv, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEBUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEBUS_Morbidity, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEUS_Morbidity, true);
		probNet.addLink(nodeTreatment, nodeInmediate_Survival, true);
		probNet.addLink(nodeTreatment, nodeSurvivors_QALE, true);
		probNet.addLink(nodeSurvivors_QALE, nodeNet_QALE, true);
		probNet.addLink(nodeInmediate_Survival, nodeNet_QALE, true);
		probNet.addLink(nodeMED_Survival, nodeNet_QALE, true);
		probNet.addLink(nodeNet_QALE, nodeTotal_QALE, true);
		probNet.addLink(nodeTBNA_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeMED_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEUS_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEBUS_Morbidity, nodeTotal_QALE, true);

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
		potTBNA.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92143, 0.07857, 0, 0.90435, 0.09565, 0,
				0.98, 0.02, 0, 0.54032, 0.45968 });
		nodeTBNA.setPotential(potTBNA);

		TablePotential potPET = new TablePotential(Arrays.asList(varPET, varCT_scan, varN2_N3, varDecPET),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potPET.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92473, 0.07527, 0, 0.775, 0.225, 0,
				0.25974, 0.74026, 0, 0.09524, 0.90476 });
		nodePET.setPotential(potPET);

		TablePotential potEBUS = new TablePotential(Arrays.asList(varEBUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEBUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.97778, 0.02222, 0, 0.975, 0.025, 0, 0.96667, 0.03333, 0, 0.97368,
				0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811, 0.89189, 0, 0.11905, 0.88095, 0, 0.10811,
				0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121, 0.87879, 0, 0.97778, 0.02222, 0, 0.975,
				0.025, 0, 0.96667, 0.03333, 0, 0.97368, 0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811,
				0.89189, 0, 0.11905, 0.88095, 0, 0.10811, 0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121,
				0.87879 });
		nodeEBUS.setPotential(potEBUS);

		TablePotential potEUS = new TablePotential(Arrays.asList(varEUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0, 0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857,
				0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0, 0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935,
				0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0, 0.13889, 0.86111, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0,
				0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857, 0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0,
				0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935, 0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0,
				0.13889, 0.86111 });
		nodeEUS.setPotential(potEUS);

		TablePotential potMED = new TablePotential(Arrays.asList(varMED, varPET, varCT_scan, varN2_N3, varDecMED),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potMED.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.94444, 0.05556, 0, 0.9375, 0.0625, 0, 0.94737, 0.05263, 0, 0.92857,
				0.07143, 0, 0.94118, 0.05882, 0, 0.95, 0.05, 0, 0.27273, 0.72727, 0, 0.2, 0.8, 0, 0.21429, 0.78571, 0,
				0.1875, 0.8125, 0, 0.1875, 0.8125, 0, 0.2, 0.8 });
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

		ExactDistrPotential potEUS_Morbidity = new ExactDistrPotential(Arrays.asList(varEUS_Morbidity, varDecEBUSEUS));
		potEUS_Morbidity.getTablePotential().setValues(new double[] { 0, -0.03, 0, -0.03 });
		nodeEUS_Morbidity.setPotential(potEUS_Morbidity);

		ExactDistrPotential potEBUS_Morbidity = new ExactDistrPotential(
				Arrays.asList(varEBUS_Morbidity, varDecEBUSEUS));
		potEBUS_Morbidity.getTablePotential().setValues(new double[] { 0, 0, -0.03, -0.03 });
		nodeEBUS_Morbidity.setPotential(potEBUS_Morbidity);

		SumPotential potTotal_QALE = new SumPotential(
				Arrays.asList(varTotal_QALE, varEBUS_Morbidity, varEUS_Morbidity, varMED_Morbidity, varNet_QALE,
						varTBNA_Morbidity), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal_QALE.setPotential(potTotal_QALE);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDMediastinetWithoutSV() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varN2_N3 = new Variable("N2_N3", "negative", "positive");
		Variable varCT_scan = new Variable("CT_scan", "negative", "positive");
		Variable varTBNA = new Variable("TBNA", "no_result", "negative", "positive");
		Variable varPET = new Variable("PET", "no_result", "negative", "positive");
		Variable varEBUS = new Variable("EBUS", "no_result", "negative", "positive");
		Variable varEUS = new Variable("EUS", "no_result", "negative", "positive");
		Variable varMED = new Variable("MED", "no_result", "negative", "positive");
		Variable varMED_Sv = new Variable("MED_Sv", "no", "yes");
		Variable varDecTBNA = new Variable("Dec:TBNA", "no", "yes");
		Variable varDecPET = new Variable("Dec:PET", "no", "yes");
		Variable varDecMED = new Variable("Dec:MED", "no", "yes");
		Variable varDecEBUSEUS = new Variable("Dec:EBUSEUS", "no_test", "eus", "ebus", "ebus_eus");
		Variable varTreatment = new Variable("Treatment", "palliative", "chemotherapy", "thoracotomy");
		Variable varNet_Effectiveness = new Variable("Net_Effectiveness");

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
		Node nodeDecEBUSEUS = probNet.addNode(varDecEBUSEUS, NodeType.DECISION);
		Node nodeTreatment = probNet.addNode(varTreatment, NodeType.DECISION);
		Node nodeNet_Effectiveness = probNet.addNode(varNet_Effectiveness, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeN2_N3, nodeCT_scan, true);
		probNet.addLink(nodeN2_N3, nodeEBUS, true);
		probNet.addLink(nodeN2_N3, nodeEUS, true);
		probNet.addLink(nodeN2_N3, nodeMED, true);
		probNet.addLink(nodeN2_N3, nodePET, true);
		probNet.addLink(nodeN2_N3, nodeTBNA, true);
		probNet.addLink(nodeN2_N3, nodeNet_Effectiveness, true);
		probNet.addLink(nodeCT_scan, nodeDecTBNA, true);
		probNet.addLink(nodeCT_scan, nodeEBUS, true);
		probNet.addLink(nodeCT_scan, nodeEUS, true);
		probNet.addLink(nodeCT_scan, nodeMED, true);
		probNet.addLink(nodeCT_scan, nodePET, true);
		probNet.addLink(nodeCT_scan, nodeTBNA, true);
		probNet.addLink(nodeTBNA, nodeDecPET, true);
		probNet.addLink(nodePET, nodeDecEBUSEUS, true);
		probNet.addLink(nodePET, nodeEBUS, true);
		probNet.addLink(nodePET, nodeEUS, true);
		probNet.addLink(nodePET, nodeMED, true);
		probNet.addLink(nodeEBUS, nodeDecMED, true);
		probNet.addLink(nodeEUS, nodeDecMED, true);
		probNet.addLink(nodeMED, nodeTreatment, true);
		probNet.addLink(nodeMED_Sv, nodeNet_Effectiveness, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA, true);
		probNet.addLink(nodeDecTBNA, nodeNet_Effectiveness, true);
		probNet.addLink(nodeDecPET, nodePET, true);
		probNet.addLink(nodeDecPET, nodeNet_Effectiveness, true);
		probNet.addLink(nodeDecMED, nodeMED, true);
		probNet.addLink(nodeDecMED, nodeMED_Sv, true);
		probNet.addLink(nodeDecMED, nodeNet_Effectiveness, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEBUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeNet_Effectiveness, true);
		probNet.addLink(nodeTreatment, nodeNet_Effectiveness, true);

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
		potTBNA.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92143, 0.07857, 0, 0.90435, 0.09565, 0,
				0.98, 0.02, 0, 0.54032, 0.45968 });
		nodeTBNA.setPotential(potTBNA);

		TablePotential potPET = new TablePotential(Arrays.asList(varPET, varCT_scan, varN2_N3, varDecPET),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potPET.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92473, 0.07527, 0, 0.775, 0.225, 0,
				0.25974, 0.74026, 0, 0.09524, 0.90476 });
		nodePET.setPotential(potPET);

		TablePotential potEBUS = new TablePotential(Arrays.asList(varEBUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEBUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.97778, 0.02222, 0, 0.975, 0.025, 0, 0.96667, 0.03333, 0, 0.97368,
				0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811, 0.89189, 0, 0.11905, 0.88095, 0, 0.10811,
				0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121, 0.87879, 0, 0.97778, 0.02222, 0, 0.975,
				0.025, 0, 0.96667, 0.03333, 0, 0.97368, 0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811,
				0.89189, 0, 0.11905, 0.88095, 0, 0.10811, 0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121,
				0.87879 });
		nodeEBUS.setPotential(potEBUS);

		TablePotential potEUS = new TablePotential(Arrays.asList(varEUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0, 0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857,
				0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0, 0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935,
				0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0, 0.13889, 0.86111, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0,
				0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857, 0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0,
				0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935, 0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0,
				0.13889, 0.86111 });
		nodeEUS.setPotential(potEUS);

		TablePotential potMED = new TablePotential(Arrays.asList(varMED, varPET, varCT_scan, varN2_N3, varDecMED),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potMED.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.94444, 0.05556, 0, 0.9375, 0.0625, 0, 0.94737, 0.05263, 0, 0.92857,
				0.07143, 0, 0.94118, 0.05882, 0, 0.95, 0.05, 0, 0.27273, 0.72727, 0, 0.2, 0.8, 0, 0.21429, 0.78571, 0,
				0.1875, 0.8125, 0, 0.1875, 0.8125, 0, 0.2, 0.8 });
		nodeMED.setPotential(potMED);

		TablePotential potMED_Sv = new TablePotential(Arrays.asList(varMED_Sv, varDecMED),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potMED_Sv.setValues(new double[] { 0, 1, 0.03704, 0.96296 });
		nodeMED_Sv.setPotential(potMED_Sv);

		ExactDistrPotential potNet_Effectiveness = new ExactDistrPotential(
				Arrays.asList(varNet_Effectiveness, varTreatment, varMED_Sv, varDecTBNA, varDecMED, varDecEBUSEUS,
						varDecPET, varN2_N3));
		potNet_Effectiveness.getTablePotential().setValues(new double[] { -0.12233, -0.39707, -0.6772, 1.05408, 1.51372,
				2.00007, -0.1251, -0.39983, -0.67997, 1.05132, 1.51095, 1.99731, -0.17633, -0.45107, -0.7312, 1.00008,
				1.45972, 1.94607, -0.1791, -0.45383, -0.73397, 0.99732, 1.45695, 1.94331, -0.173, -0.44773, -0.72787,
				1.00342, 1.46305, 1.94941, -0.17577, -0.4505, -0.73063, 1.00065, 1.46028, 1.94664, -0.227, -0.50173,
				-0.78187, 0.94942, 1.40905, 1.89541, -0.22977, -0.5045, -0.78463, 0.94665, 1.40628, 1.89264, -0.173,
				-0.44773, -0.72787, 1.00342, 1.46305, 1.94941, -0.17577, -0.4505, -0.73063, 1.00065, 1.46028, 1.94664,
				-0.227, -0.50173, -0.78187, 0.94942, 1.40905, 1.89541, -0.22977, -0.5045, -0.78463, 0.94665, 1.40628,
				1.89264, -0.22367, -0.4984, -0.77853, 0.95275, 1.41238, 1.89874, -0.22643, -0.50117, -0.7813, 0.94998,
				1.40962, 1.89597, -0.27767, -0.5524, -0.83253, 0.89875, 1.35838, 1.84474, -0.28043, -0.55517, -0.8353,
				0.89598, 1.35562, 1.84197, -0.19733, -0.47207, -0.7522, 0.97908, 1.43872, 1.92507, -0.2001, -0.47483,
				-0.75497, 0.97632, 1.43595, 1.92231, -0.25133, -0.52607, -0.8062, 0.92508, 1.38472, 1.87107, -0.2541,
				-0.52883, -0.80897, 0.92232, 1.38195, 1.86831, -0.248, -0.52273, -0.80287, 0.92842, 1.38805, 1.87441,
				-0.25077, -0.5255, -0.80563, 0.92565, 1.38528, 1.87164, -0.302, -0.57673, -0.85687, 0.87442, 1.33405,
				1.82041, -0.30477, -0.5795, -0.85963, 0.87165, 1.33128, 1.81764, -0.248, -0.52273, -0.80287, 0.92842,
				1.38805, 1.87441, -0.25077, -0.5255, -0.80563, 0.92565, 1.38528, 1.87164, -0.302, -0.57673, -0.85687,
				0.87442, 1.33405, 1.82041, -0.30477, -0.5795, -0.85963, 0.87165, 1.33128, 1.81764, -0.29867, -0.5734,
				-0.85353, 0.87775, 1.33738, 1.82374, -0.30143, -0.57617, -0.8563, 0.87498, 1.33462, 1.82097, -0.35267,
				-0.6274, -0.90753, 0.82375, 1.28338, 1.76974, -0.35543, -0.63017, -0.9103, 0.82098, 1.28062, 1.76697,
				-0.12233, -0.39707, -0.6772, 0.31823, 0.36666, -0.1272, -0.1251, -0.39983, -0.67997, 0.31547, 0.36389,
				-0.12997, -0.17633, -0.45107, -0.7312, 0.26423, 0.31266, -0.1812, -0.1791, -0.45383, -0.73397, 0.26147,
				0.30989, -0.18397, -0.173, -0.44773, -0.72787, 0.26757, 0.31599, -0.17787, -0.17577, -0.4505, -0.73063,
				0.2648, 0.31323, -0.18063, -0.227, -0.50173, -0.78187, 0.21357, 0.26199, -0.23187, -0.22977, -0.5045,
				-0.78463, 0.2108, 0.25923, -0.23463, -0.173, -0.44773, -0.72787, 0.26757, 0.31599, -0.17787, -0.17577,
				-0.4505, -0.73063, 0.2648, 0.31323, -0.18063, -0.227, -0.50173, -0.78187, 0.21357, 0.26199, -0.23187,
				-0.22977, -0.5045, -0.78463, 0.2108, 0.25923, -0.23463, -0.22367, -0.4984, -0.77853, 0.2169, 0.26533,
				-0.22853, -0.22643, -0.50117, -0.7813, 0.21413, 0.26256, -0.2313, -0.27767, -0.5524, -0.83253, 0.1629,
				0.21133, -0.28253, -0.28043, -0.55517, -0.8353, 0.16013, 0.20856, -0.2853, -0.19733, -0.47207, -0.7522,
				0.24323, 0.29166, -0.2022, -0.2001, -0.47483, -0.75497, 0.24047, 0.28889, -0.20497, -0.25133, -0.52607,
				-0.8062, 0.18923, 0.23766, -0.2562, -0.2541, -0.52883, -0.80897, 0.18647, 0.23489, -0.25897, -0.248,
				-0.52273, -0.80287, 0.19257, 0.24099, -0.25287, -0.25077, -0.5255, -0.80563, 0.1898, 0.23823, -0.25563,
				-0.302, -0.57673, -0.85687, 0.13857, 0.18699, -0.30687, -0.30477, -0.5795, -0.85963, 0.1358, 0.18423,
				-0.30963, -0.248, -0.52273, -0.80287, 0.19257, 0.24099, -0.25287, -0.25077, -0.5255, -0.80563, 0.1898,
				0.23823, -0.25563, -0.302, -0.57673, -0.85687, 0.13857, 0.18699, -0.30687, -0.30477, -0.5795, -0.85963,
				0.1358, 0.18423, -0.30963, -0.29867, -0.5734, -0.85353, 0.1419, 0.19033, -0.30353, -0.30143, -0.57617,
				-0.8563, 0.13913, 0.18756, -0.3063, -0.35267, -0.6274, -0.90753, 0.0879, 0.13633, -0.35753, -0.35543,
				-0.63017, -0.9103, 0.08513, 0.13356, -0.3603 });
		nodeNet_Effectiveness.setPotential(potNet_Effectiveness);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDMediastinetWithoutMediastinoscopy() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varN2_N3 = new Variable("N2_N3", "negative", "positive");
		Variable varCT_scan = new Variable("CT_scan", "negative", "positive");
		Variable varTBNA = new Variable("TBNA", "no_result", "negative", "positive");
		Variable varPET = new Variable("PET", "no_result", "negative", "positive");
		Variable varEBUS = new Variable("EBUS", "no_result", "negative", "positive");
		Variable varEUS = new Variable("EUS", "no_result", "negative", "positive");
		Variable varDecTBNA = new Variable("Dec:TBNA", "no", "yes");
		Variable varDecPET = new Variable("Dec:PET", "no", "yes");
		Variable varDecEBUSEUS = new Variable("Dec:EBUSEUS", "no_test", "eus", "ebus", "ebus_eus");
		Variable varTreatment = new Variable("Treatment", "palliative", "chemotherapy", "thoracotomy");
		Variable varSurvivors_QALE = new Variable("Survivors_QALE");
		Variable varInmediate_Survival = new Variable("Inmediate_Survival");
		Variable varNet_QALE = new Variable("Net_QALE");
		Variable varTBNA_Morbidity = new Variable("TBNA_Morbidity");
		Variable varEUS_Morbidity = new Variable("EUS_Morbidity");
		Variable varEBUS_Morbidity = new Variable("EBUS_Morbidity");
		Variable varTotal_QALE = new Variable("Total_QALE");
		Variable varEconomic_Cost_CT_scan = new Variable("Economic_Cost_CT_scan");
		Variable varEconomic_Cost_TBNA = new Variable("Economic_Cost_TBNA");
		Variable varEconomic_Cost_EBUS = new Variable("Economic_Cost_EBUS");
		Variable varEconomic_Cost_EUS = new Variable("Economic_Cost_EUS");
		Variable varEconomic_Cost_PET = new Variable("Economic_Cost_PET");
		Variable varEconomic_Cost_Treatment = new Variable("Economic_Cost_Treatment");
		Variable varTotal_Economic_Cost = new Variable("Total_Economic_Cost");
		Variable varC2E = new Variable("C2E");
		Variable varWeighted_Economic_Cost = new Variable("Weighted_Economic_Cost");
		Variable varNet_Effectiveness = new Variable("Net_Effectiveness");

		// Nodes
		Node nodeN2_N3 = probNet.addNode(varN2_N3, NodeType.CHANCE);
		Node nodeCT_scan = probNet.addNode(varCT_scan, NodeType.CHANCE);
		Node nodeTBNA = probNet.addNode(varTBNA, NodeType.CHANCE);
		Node nodePET = probNet.addNode(varPET, NodeType.CHANCE);
		Node nodeEBUS = probNet.addNode(varEBUS, NodeType.CHANCE);
		Node nodeEUS = probNet.addNode(varEUS, NodeType.CHANCE);
		Node nodeDecTBNA = probNet.addNode(varDecTBNA, NodeType.DECISION);
		Node nodeDecPET = probNet.addNode(varDecPET, NodeType.DECISION);
		Node nodeDecEBUSEUS = probNet.addNode(varDecEBUSEUS, NodeType.DECISION);
		Node nodeTreatment = probNet.addNode(varTreatment, NodeType.DECISION);
		Node nodeSurvivors_QALE = probNet.addNode(varSurvivors_QALE, NodeType.UTILITY);
		Node nodeInmediate_Survival = probNet.addNode(varInmediate_Survival, NodeType.UTILITY);
		Node nodeNet_QALE = probNet.addNode(varNet_QALE, NodeType.UTILITY);
		Node nodeTBNA_Morbidity = probNet.addNode(varTBNA_Morbidity, NodeType.UTILITY);
		Node nodeEUS_Morbidity = probNet.addNode(varEUS_Morbidity, NodeType.UTILITY);
		Node nodeEBUS_Morbidity = probNet.addNode(varEBUS_Morbidity, NodeType.UTILITY);
		Node nodeTotal_QALE = probNet.addNode(varTotal_QALE, NodeType.UTILITY);
		Node nodeEconomic_Cost_CT_scan = probNet.addNode(varEconomic_Cost_CT_scan, NodeType.UTILITY);
		Node nodeEconomic_Cost_TBNA = probNet.addNode(varEconomic_Cost_TBNA, NodeType.UTILITY);
		Node nodeEconomic_Cost_EBUS = probNet.addNode(varEconomic_Cost_EBUS, NodeType.UTILITY);
		Node nodeEconomic_Cost_EUS = probNet.addNode(varEconomic_Cost_EUS, NodeType.UTILITY);
		Node nodeEconomic_Cost_PET = probNet.addNode(varEconomic_Cost_PET, NodeType.UTILITY);
		Node nodeEconomic_Cost_Treatment = probNet.addNode(varEconomic_Cost_Treatment, NodeType.UTILITY);
		Node nodeTotal_Economic_Cost = probNet.addNode(varTotal_Economic_Cost, NodeType.UTILITY);
		Node nodeC2E = probNet.addNode(varC2E, NodeType.UTILITY);
		Node nodeWeighted_Economic_Cost = probNet.addNode(varWeighted_Economic_Cost, NodeType.UTILITY);
		Node nodeNet_Effectiveness = probNet.addNode(varNet_Effectiveness, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeN2_N3, nodeCT_scan, true);
		probNet.addLink(nodeN2_N3, nodeEBUS, true);
		probNet.addLink(nodeN2_N3, nodeEUS, true);
		probNet.addLink(nodeN2_N3, nodePET, true);
		probNet.addLink(nodeN2_N3, nodeSurvivors_QALE, true);
		probNet.addLink(nodeN2_N3, nodeTBNA, true);
		probNet.addLink(nodeCT_scan, nodeDecTBNA, true);
		probNet.addLink(nodeCT_scan, nodeEBUS, true);
		probNet.addLink(nodeCT_scan, nodeEUS, true);
		probNet.addLink(nodeCT_scan, nodePET, true);
		probNet.addLink(nodeCT_scan, nodeTBNA, true);
		probNet.addLink(nodeTBNA, nodeDecPET, true);
		probNet.addLink(nodePET, nodeDecEBUSEUS, true);
		probNet.addLink(nodePET, nodeEBUS, true);
		probNet.addLink(nodePET, nodeEUS, true);
		probNet.addLink(nodeEBUS, nodeTreatment, true);
		probNet.addLink(nodeEUS, nodeTreatment, true);
		probNet.addLink(nodeDecTBNA, nodeEconomic_Cost_TBNA, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA, true);
		probNet.addLink(nodeDecTBNA, nodeTBNA_Morbidity, true);
		probNet.addLink(nodeDecPET, nodeEconomic_Cost_PET, true);
		probNet.addLink(nodeDecPET, nodePET, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEBUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEBUS_Morbidity, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEUS_Morbidity, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEconomic_Cost_EBUS, true);
		probNet.addLink(nodeDecEBUSEUS, nodeEconomic_Cost_EUS, true);
		probNet.addLink(nodeTreatment, nodeEconomic_Cost_Treatment, true);
		probNet.addLink(nodeTreatment, nodeInmediate_Survival, true);
		probNet.addLink(nodeTreatment, nodeSurvivors_QALE, true);
		probNet.addLink(nodeSurvivors_QALE, nodeNet_QALE, true);
		probNet.addLink(nodeInmediate_Survival, nodeNet_QALE, true);
		probNet.addLink(nodeNet_QALE, nodeTotal_QALE, true);
		probNet.addLink(nodeTBNA_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEUS_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeEBUS_Morbidity, nodeTotal_QALE, true);
		probNet.addLink(nodeTotal_QALE, nodeNet_Effectiveness, true);
		probNet.addLink(nodeEconomic_Cost_CT_scan, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_TBNA, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_EBUS, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_EUS, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_PET, nodeTotal_Economic_Cost, true);
		probNet.addLink(nodeEconomic_Cost_Treatment, nodeTotal_Economic_Cost, true);
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
		potTBNA.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92143, 0.07857, 0, 0.90435, 0.09565, 0,
				0.98, 0.02, 0, 0.54032, 0.45968 });
		nodeTBNA.setPotential(potTBNA);

		TablePotential potPET = new TablePotential(Arrays.asList(varPET, varCT_scan, varN2_N3, varDecPET),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potPET.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92473, 0.07527, 0, 0.775, 0.225, 0,
				0.25974, 0.74026, 0, 0.09524, 0.90476 });
		nodePET.setPotential(potPET);

		TablePotential potEBUS = new TablePotential(Arrays.asList(varEBUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEBUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.97778, 0.02222, 0, 0.975, 0.025, 0, 0.96667, 0.03333, 0, 0.97368,
				0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811, 0.89189, 0, 0.11905, 0.88095, 0, 0.10811,
				0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121, 0.87879, 0, 0.97778, 0.02222, 0, 0.975,
				0.025, 0, 0.96667, 0.03333, 0, 0.97368, 0.02632, 0, 0.97561, 0.02439, 0, 0.96552, 0.03448, 0, 0.10811,
				0.89189, 0, 0.11905, 0.88095, 0, 0.10811, 0.89189, 0, 0.08108, 0.91892, 0, 0.11111, 0.88889, 0, 0.12121,
				0.87879 });
		nodeEBUS.setPotential(potEBUS);

		TablePotential potEUS = new TablePotential(Arrays.asList(varEUS, varPET, varCT_scan, varN2_N3, varDecEBUSEUS),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potEUS.setValues(new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0, 0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857,
				0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0, 0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935,
				0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0, 0.13889, 0.86111, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,
				0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0.92308, 0.07692, 0,
				0.9375, 0.0625, 0, 0.92593, 0.07407, 0, 0.92857, 0.07143, 0, 0.93333, 0.06667, 0, 0.93548, 0.06452, 0,
				0.2381, 0.7619, 0, 0.43333, 0.56667, 0, 0.41935, 0.58065, 0, 0.14286, 0.85714, 0, 0.13158, 0.86842, 0,
				0.13889, 0.86111 });
		nodeEUS.setPotential(potEUS);

		ExactDistrPotential potSurvivors_QALE = new ExactDistrPotential(
				Arrays.asList(varSurvivors_QALE, varN2_N3, varTreatment));
		potSurvivors_QALE.getTablePotential().setValues(new double[] { 1.25, 0.5, 2, 0.83, 3, 0.66 });
		nodeSurvivors_QALE.setPotential(potSurvivors_QALE);

		ExactDistrPotential potInmediate_Survival = new ExactDistrPotential(
				Arrays.asList(varInmediate_Survival, varTreatment));
		potInmediate_Survival.getTablePotential().setValues(new double[] { 0.98113, 0.98039, 0.90909 });
		nodeInmediate_Survival.setPotential(potInmediate_Survival);

		ProductPotential potNet_QALE = new ProductPotential(
				Arrays.asList(varNet_QALE, varInmediate_Survival, varSurvivors_QALE),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeNet_QALE.setPotential(potNet_QALE);

		ExactDistrPotential potTBNA_Morbidity = new ExactDistrPotential(Arrays.asList(varTBNA_Morbidity, varDecTBNA));
		potTBNA_Morbidity.getTablePotential().setValues(new double[] { 0, -0.0001 });
		nodeTBNA_Morbidity.setPotential(potTBNA_Morbidity);

		ExactDistrPotential potEUS_Morbidity = new ExactDistrPotential(Arrays.asList(varEUS_Morbidity, varDecEBUSEUS));
		potEUS_Morbidity.getTablePotential().setValues(new double[] { 0, -0.03, 0, -0.03 });
		nodeEUS_Morbidity.setPotential(potEUS_Morbidity);

		ExactDistrPotential potEBUS_Morbidity = new ExactDistrPotential(
				Arrays.asList(varEBUS_Morbidity, varDecEBUSEUS));
		potEBUS_Morbidity.getTablePotential().setValues(new double[] { 0, 0, -0.03, -0.03 });
		nodeEBUS_Morbidity.setPotential(potEBUS_Morbidity);

		SumPotential potTotal_QALE = new SumPotential(
				Arrays.asList(varTotal_QALE, varEBUS_Morbidity, varEUS_Morbidity, varNet_QALE, varTBNA_Morbidity),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal_QALE.setPotential(potTotal_QALE);

		ExactDistrPotential potEconomic_Cost_CT_scan = new ExactDistrPotential(Arrays.asList(varEconomic_Cost_CT_scan));
		potEconomic_Cost_CT_scan.getTablePotential().setValues(new double[] { 670 });
		nodeEconomic_Cost_CT_scan.setPotential(potEconomic_Cost_CT_scan);

		ExactDistrPotential potEconomic_Cost_TBNA = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_TBNA, varDecTBNA));
		potEconomic_Cost_TBNA.getTablePotential().setValues(new double[] { 0, 80 });
		nodeEconomic_Cost_TBNA.setPotential(potEconomic_Cost_TBNA);

		ExactDistrPotential potEconomic_Cost_EBUS = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_EBUS, varDecEBUSEUS));
		potEconomic_Cost_EBUS.getTablePotential().setValues(new double[] { 0, 0, 620, 620 });
		nodeEconomic_Cost_EBUS.setPotential(potEconomic_Cost_EBUS);

		ExactDistrPotential potEconomic_Cost_EUS = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_EUS, varDecEBUSEUS));
		potEconomic_Cost_EUS.getTablePotential().setValues(new double[] { 0, 620, 0, 620 });
		nodeEconomic_Cost_EUS.setPotential(potEconomic_Cost_EUS);

		ExactDistrPotential potEconomic_Cost_PET = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_PET, varDecPET));
		potEconomic_Cost_PET.getTablePotential().setValues(new double[] { 0, 2250 });
		nodeEconomic_Cost_PET.setPotential(potEconomic_Cost_PET);

		ExactDistrPotential potEconomic_Cost_Treatment = new ExactDistrPotential(
				Arrays.asList(varEconomic_Cost_Treatment, varTreatment));
		potEconomic_Cost_Treatment.getTablePotential().setValues(new double[] { 3000, 11242, 19646 });
		nodeEconomic_Cost_Treatment.setPotential(potEconomic_Cost_Treatment);

		SumPotential potTotal_Economic_Cost = new SumPotential(
				Arrays.asList(varTotal_Economic_Cost, varEconomic_Cost_CT_scan, varEconomic_Cost_EBUS,
						varEconomic_Cost_EUS, varEconomic_Cost_PET, varEconomic_Cost_TBNA, varEconomic_Cost_Treatment),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeTotal_Economic_Cost.setPotential(potTotal_Economic_Cost);

		ExactDistrPotential potC2E = new ExactDistrPotential(Arrays.asList(varC2E));
		potC2E.getTablePotential().setValues(new double[] { -0.00003333333333 });
		nodeC2E.setPotential(potC2E);

		ProductPotential potWeighted_Economic_Cost = new ProductPotential(
				Arrays.asList(varWeighted_Economic_Cost, varC2E, varTotal_Economic_Cost),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeWeighted_Economic_Cost.setPotential(potWeighted_Economic_Cost);

		SumPotential potNet_Effectiveness = new SumPotential(
				Arrays.asList(varNet_Effectiveness, varTotal_QALE, varWeighted_Economic_Cost),
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeNet_Effectiveness.setPotential(potNet_Effectiveness);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	/**
	 * @return A network identical to id/ID-redundant-chance.pgmx
	 */
	public static ProbNet buildIDRedundantChance() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varU = new Variable("U");
		Variable varD = new Variable("D", "no", "yes");
		Variable varU1 = new Variable("U1");
		Variable varE = new Variable("E", "absent", "present");

		// Nodes
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeU1 = probNet.addNode(varU1, NodeType.UTILITY);
		Node nodeE = probNet.addNode(varE, NodeType.CHANCE);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeD, nodeU, true);
		probNet.addLink(nodeE, nodeU1, true);
		probNet.addLink(nodeE, nodeD, true);

		// Potentials
		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varD));
		potU.getTablePotential().setValues(new double[] { 1, 2 });
		nodeU.setPotential(potU);

		ExactDistrPotential potU1 = new ExactDistrPotential(Arrays.asList(varU1, varE));
		potU1.getTablePotential().setValues(new double[] { 112, 234 });
		nodeU1.setPotential(potU1);

		TablePotential potE = new TablePotential(Arrays.asList(varE), PotentialRole.CONDITIONAL_PROBABILITY);
		potE.setValues(new double[] { 0.5, 0.5 });
		nodeE.setPotential(potE);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	/**
	 * @return A network identical to id/ID-two-independent-decisions.pgmx
	 */
	public static ProbNet buildIDTwoIndependentDecisions() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varD = new Variable("D", "no", "yes");
		Variable varD1 = new Variable("D1", "no", "yes");
		Variable varU = new Variable("U");
		Variable varU1 = new Variable("U1");

		// Nodes
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeD1 = probNet.addNode(varD1, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);
		Node nodeU1 = probNet.addNode(varU1, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeD, nodeU1, true);
		probNet.addLink(nodeD, nodeD1, true);
		probNet.addLink(nodeD1, nodeU, true);

		// Potentials
		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varD1));
		potU.getTablePotential().setValues(new double[] { 2, 3 });
		nodeU.setPotential(potU);

		ExactDistrPotential potU1 = new ExactDistrPotential(Arrays.asList(varU1, varD));
		potU1.getTablePotential().setValues(new double[] { 1, 0 });
		nodeU1.setPotential(potU1);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDThreeIndependentDecisions() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varD2 = new Variable("D2", "no", "yes");
		Variable varD1 = new Variable("D1", "no", "yes");
		Variable varU1 = new Variable("U1");
		Variable varU2 = new Variable("U2");
		Variable varB = new Variable("B", "absent", "present");
		Variable varA = new Variable("A", "absent", "present");
		Variable varU3 = new Variable("U3");
		Variable varD3 = new Variable("D3", "no", "yes");

		// Nodes
		Node nodeD2 = probNet.addNode(varD2, NodeType.DECISION);
		Node nodeD1 = probNet.addNode(varD1, NodeType.DECISION);
		Node nodeU1 = probNet.addNode(varU1, NodeType.UTILITY);
		Node nodeU2 = probNet.addNode(varU2, NodeType.UTILITY);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeU3 = probNet.addNode(varU3, NodeType.UTILITY);
		Node nodeD3 = probNet.addNode(varD3, NodeType.DECISION);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeD2, nodeU2, true);
		probNet.addLink(nodeD2, nodeD3, true);
		probNet.addLink(nodeD1, nodeU1, true);
		probNet.addLink(nodeD1, nodeD2, true);
		probNet.addLink(nodeB, nodeD2, true);
		probNet.addLink(nodeB, nodeU2, true);
		probNet.addLink(nodeA, nodeD1, true);
		probNet.addLink(nodeA, nodeU1, true);
		probNet.addLink(nodeD3, nodeU3, true);

		// Potentials
		ExactDistrPotential potU1 = new ExactDistrPotential(Arrays.asList(varU1, varD1, varA));
		potU1.getTablePotential().setValues(new double[] { 2, 0, 0, 2 });
		nodeU1.setPotential(potU1);

		ExactDistrPotential potU2 = new ExactDistrPotential(Arrays.asList(varU2, varD2, varB));
		potU2.getTablePotential().setValues(new double[] { 1, 8, 3, 4 });
		nodeU2.setPotential(potU2);

		TablePotential potB = new TablePotential(Arrays.asList(varB), PotentialRole.CONDITIONAL_PROBABILITY);
		potB.setValues(new double[] { 0.4, 0.6 });
		nodeB.setPotential(potB);

		TablePotential potA = new TablePotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		potA.setValues(new double[] { 0.7, 0.3 });
		nodeA.setPotential(potA);

		ExactDistrPotential potU3 = new ExactDistrPotential(Arrays.asList(varU3, varD3));
		potU3.getTablePotential().setValues(new double[] { 12, 30.03 });
		nodeU3.setPotential(potU3);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDConcatenateOrderTwoDecisions() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varD = new Variable("D", "no", "yes");
		Variable varD1 = new Variable("D1", "no", "yes");
		Variable varU = new Variable("U");
		Variable varU1 = new Variable("U1");
		Variable varB = new Variable("B", "absent", "present");

		// Nodes
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeD1 = probNet.addNode(varD1, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);
		Node nodeU1 = probNet.addNode(varU1, NodeType.UTILITY);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeD, nodeD1, true);
		probNet.addLink(nodeD, nodeU1, true);
		probNet.addLink(nodeD1, nodeU, true);
		probNet.addLink(nodeB, nodeD, true);
		probNet.addLink(nodeB, nodeU1, true);

		// Potentials
		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varD1));
		potU.getTablePotential().setValues(new double[] { 0.65, 1.35 });
		nodeU.setPotential(potU);

		ExactDistrPotential potU1 = new ExactDistrPotential(Arrays.asList(varU1, varD, varB));
		potU1.getTablePotential().setValues(new double[] { 1, 8, 6, 4 });
		nodeU1.setPotential(potU1);

		TablePotential potB = new TablePotential(Arrays.asList(varB), PotentialRole.CONDITIONAL_PROBABILITY);
		potB.setValues(new double[] { 0.4, 0.6 });
		nodeB.setPotential(potB);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;

	}

	public static ProbNet buildIDStatesTies() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varU = new Variable("U");
		Variable varD = new Variable("D", "s1", "s2", "s3", "s4", "s5");

		// Nodes
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeD, nodeU, true);

		// Potentials
		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varD));
		potU.getTablePotential().setValues(new double[] { 11.4, 11.4, 13.7, 11.4, 13.7 });
		nodeU.setPotential(potU);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDStatesTiesPerfectKnowledge() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varA = new Variable("A", "absent", "present");
		Variable varD = new Variable("D", "low", "medium", "high");
		Variable varU = new Variable("U");

		// Nodes
		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeA, nodeU, true);
		probNet.addLink(nodeA, nodeD, true);
		probNet.addLink(nodeD, nodeU, true);

		// Potentials
		TablePotential potA = new TablePotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		potA.setValues(new double[] { 0.5, 0.5 });
		nodeA.setPotential(potA);

		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varD, varA));
		potU.getTablePotential().setValues(new double[] { -1, 0, 0, 1, 2, 3 });
		nodeU.setPotential(potU);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	public static ProbNet buildIDConsecutiveDecisions() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varX0 = new Variable("X0", "yes", "no");
		Variable varD2 = new Variable("D2", "present", "absent");
		Variable varD3 = new Variable("D3", "no", "yes");
		Variable varU = new Variable("U");

		// Nodes
		Node nodeX0 = probNet.addNode(varX0, NodeType.CHANCE);
		Node nodeD2 = probNet.addNode(varD2, NodeType.DECISION);
		Node nodeD3 = probNet.addNode(varD3, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeX0, nodeD2, true);
		probNet.addLink(nodeX0, nodeU, true);
		probNet.addLink(nodeD2, nodeD3, true);
		probNet.addLink(nodeD3, nodeU, true);

		// Potentials
		TablePotential potX0 = new TablePotential(Arrays.asList(varX0), PotentialRole.CONDITIONAL_PROBABILITY);
		potX0.setValues(new double[] { 0.21249053, 0.78750947 });
		nodeX0.setPotential(potX0);

		ExactDistrPotential potU = new ExactDistrPotential(Arrays.asList(varU, varD3, varX0));
		potU.getTablePotential().setValues(new double[] { 0.4, 3, 5, 4.8 });
		nodeU.setPotential(potU);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

}
