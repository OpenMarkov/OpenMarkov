/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.Arrays;

public class SensitivityAnalysisFactory extends IDFactory {
    public static ProbNet buildIDDecideTestSA() {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        // Variables
        Variable varDisease = new Variable("Disease", "absent", "present");
        Variable varResult_of_test = new Variable("Result of test", "not-performed", "negative", "positive");
        Variable varTherapy = new Variable("Therapy", "no", "yes");
        Variable varDo_test_ = new Variable("Do test?", "no", "yes");
        Variable varHealth_state = new Variable("Health state");
        Variable varCost_of_test = new Variable("Cost of test");
        Variable varCost_of_therapy = new Variable("Cost of therapy");
        
        // Nodes
        Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
        Node nodeResult_of_test = probNet.addNode(varResult_of_test, NodeType.CHANCE);
        Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
        Node nodeDo_test_ = probNet.addNode(varDo_test_, NodeType.DECISION);
        Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);
        Node nodeCost_of_test = probNet.addNode(varCost_of_test, NodeType.UTILITY);
        Node nodeCost_of_therapy = probNet.addNode(varCost_of_therapy, NodeType.UTILITY);
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeDisease, nodeHealth_state, true);
        probNet.addLink(nodeDisease, nodeResult_of_test, true);
        probNet.addLink(nodeResult_of_test, nodeTherapy, true);
        probNet.addLink(nodeTherapy, nodeHealth_state, true);
        probNet.addLink(nodeTherapy, nodeCost_of_therapy, true);
        probNet.addLink(nodeDo_test_, nodeCost_of_test, true);
        probNet.addLink(nodeDo_test_, nodeTherapy, true);
        probNet.addLink(nodeDo_test_, nodeResult_of_test, true);
        
        // Potentials
        UncertainTablePotential potDisease = new UncertainTablePotential(Arrays.asList(varDisease),
                                                       PotentialRole.CONDITIONAL_PROBABILITY);
        potDisease.setValues(new double[]{0.86, 0.14});
        potDisease.uncertainValues = new UncertainValue[]{new UncertainValue(new ComplementFunction(1), ""),
                new UncertainValue(new BetaFunction(14, 86), "prevalence")};
        nodeDisease.setPotential(potDisease);

        UncertainTablePotential potResult_of_test = new UncertainTablePotential(Arrays.asList(varResult_of_test, varDo_test_, varDisease),
                                                              PotentialRole.CONDITIONAL_PROBABILITY);
        potResult_of_test.setValues(new double[]{1, 0, 0, 0, 0.97, 0.03, 1, 0, 0, 0, 0.09, 0.91});
        potResult_of_test.uncertainValues = new UncertainValue[]{null, null, null,
                new UncertainValue(new ExactFunction(0), ""),
                new UncertainValue(new BetaFunction(97, 3), "specificity"),
                new UncertainValue(new ComplementFunction(1), ""), null, null, null,
                new UncertainValue(new ExactFunction(0), ""), new UncertainValue(new ComplementFunction(1), ""),
                new UncertainValue(new BetaFunction(91, 9), "sensitivity")};
        nodeResult_of_test.setPotential(potResult_of_test);
        
        ExactDistrPotential potHealth_state = new ExactDistrPotential(
                Arrays.asList(varHealth_state, varDisease, varTherapy));
        potHealth_state.setValues(new double[]{10, 4.94616381, 9, 8});
        potHealth_state.setUncertainValues(new UncertainValue[]{null,
                new UncertainValue(new LogNormalFunction(1.09861229, 1), "utility non-treated disease"),
                new UncertainValue(new TriangularFunction(8.5, 9.5, 9), "utility not treated"),
                new UncertainValue(new TriangularFunction(7.5, 8.5, 8), "utility treated disease")});
        nodeHealth_state.setPotential(potHealth_state);
        
        ExactDistrPotential potCost_of_test = new ExactDistrPotential(Arrays.asList(varCost_of_test, varDo_test_));
        potCost_of_test.setValues(new double[]{0, -0.2});
        potCost_of_test.setUncertainValues(
                new UncertainValue[]{null, new UncertainValue(new TriangularFunction(-0.25, -0.15, -0.2), "")});
        nodeCost_of_test.setPotential(potCost_of_test);
        
        ExactDistrPotential potCost_of_therapy = new ExactDistrPotential(Arrays.asList(varCost_of_therapy, varTherapy));
        potCost_of_therapy.setValues(new double[]{0, -0.2});
        potCost_of_therapy.setUncertainValues(
                new UncertainValue[]{null, new UncertainValue(new TriangularFunction(-0.3, -0.1, -0.2), "")});
        nodeCost_of_therapy.setPotential(potCost_of_therapy);
        
        // Link restrictions and revealing states
        // Always observed nodes
        
        return probNet;
    }
    
    public static ProbNet createSimpleIDWithoutDecisionsBeta() {
        return createSimpleIDWithoutDecisions(new BetaFunction(14, 86));
    }
    
    public static ProbNet createSimpleIDWithoutDecisionsTriangular() {
        return createSimpleIDWithoutDecisions(new TriangularFunction(0.1, 0.18, 0.14));
    }
    
    /**
     * @param prevalenceProbFunction TODO
     * @return An influence diagram without decisions, with only two nodes: X (chance) and U (utility).
     */
    public static ProbNet createSimpleIDWithoutDecisions(ProbDensFunction prevalenceProbFunction) {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        // Variables
        Variable variableX = new Variable(diseaseName, diseaseStates);
        Variable variableU = new Variable(healthStateName);
        
        // Nodes
        Node nodeAlive = probNet.addNode(variableX, NodeType.CHANCE);
        Node nodeHealth_state = probNet.addNode(variableU, NodeType.UTILITY);
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeAlive, nodeHealth_state, true);
        
        // Potentials
        UncertainTablePotential potAlive = new UncertainTablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
        potAlive.setValues(new double[]{0.14, 0.86});
        potAlive.uncertainValues = new UncertainValue[]{new UncertainValue(prevalenceProbFunction, "prevalence"),
                new UncertainValue(new ComplementFunction(1), "")};
        nodeAlive.setPotential(potAlive);

        ExactDistrPotential potHealth_state = new ExactDistrPotential(Arrays.asList(variableU, variableX));
        potHealth_state.setValues(new double[]{0, 1});
        nodeHealth_state.setPotential(potHealth_state);

        // Link restrictions and revealing states
        // Always observed nodes

        return probNet;

    }

    /**
     * @return An influence diagram without decisions, with only two nodes: X (chance) and U (utility).
     */
    public static ProbNet createSimpleIDWithoutDecisionsDiseaseFourStates() {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        // Variables
        
        Variable variableX = new Variable(diseaseName, "s1", "s2", "s3", "s4");
        Variable variableU = new Variable(healthStateName);
        
        // Nodes
        Node nodeAlive = probNet.addNode(variableX, NodeType.CHANCE);
        Node nodeHealth_state = probNet.addNode(variableU, NodeType.UTILITY);
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeAlive, nodeHealth_state, true);
        
        // Potentials
        UncertainTablePotential potAlive = new UncertainTablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
        potAlive.setValues(new double[]{0.2, 0.35, 0.15, 0.3});
        potAlive.uncertainValues = new UncertainValue[]{
                new UncertainValue(new RangeFunction(0.1, 0.3), "prevalence1"),
                new UncertainValue(new RangeFunction(0.2, 0.5), "prevalence2"),
                new UncertainValue(new ComplementFunction(1), ""), new UncertainValue(new ComplementFunction(2), "")};
        nodeAlive.setPotential(potAlive);
        
        ExactDistrPotential potHealth_state = new ExactDistrPotential(Arrays.asList(variableU, variableX));
        potHealth_state.setValues(new double[]{0, 0.1, 0.4, 1});
        nodeHealth_state.setPotential(potHealth_state);
        
        // Link restrictions and revealing states
        // Always observed nodes
        
        return probNet;
        
    }
    
    /**
     * @return An influence diagram without decisions, with only two nodes: X (chance) and U (utility).
     */
    public static ProbNet createSimpleIDWithoutDecisionsBeta(BetaFunction betaFunction) {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        // Variables
        Variable variableX = new Variable(diseaseName, diseaseStates);
        Variable variableU = new Variable(healthStateName);
        
        // Nodes
        Node nodeAlive = probNet.addNode(variableX, NodeType.CHANCE);
        Node nodeHealth_state = probNet.addNode(variableU, NodeType.UTILITY);
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeAlive, nodeHealth_state, true);
        
        // Potentials
        UncertainTablePotential potAlive = new UncertainTablePotential(Arrays.asList(variableX), PotentialRole.CONDITIONAL_PROBABILITY);
        potAlive.setValues(new double[]{0.14, 0.86});
        potAlive.uncertainValues = new UncertainValue[]{new UncertainValue(new BetaFunction(14, 86), "prevalence"),
                new UncertainValue(new ComplementFunction(1), "")};
        nodeAlive.setPotential(potAlive);
        
        ExactDistrPotential potHealth_state = new ExactDistrPotential(Arrays.asList(variableU, variableX));
        potHealth_state.setValues(new double[]{0, 1});
        nodeHealth_state.setPotential(potHealth_state);
        
        // Link restrictions and revealing states
        // Always observed nodes
        
        return probNet;
        
    }
    
}
