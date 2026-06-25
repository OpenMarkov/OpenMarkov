/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author manuel
 * This class creates some influence diagrams for test purposes.
 */
public class IDFactory {
    
    private static final Variable costOfTherapy = new Variable("Cost of therapy");
    private static final Variable costOfTest = new Variable("Cost of test");
    private static Variable effectiveness = new Variable("Effectiveness");
    private static final Variable disease = new Variable("Disease", "absent", "present");
    private static final Variable resultOfTest = new Variable("Result of test", "negative", "positive", "not performed");
    private static final Variable doTest = new Variable("Do test", "no", "yes");
    private static final Variable therapy = new Variable("Therapy", "no", "yes");
    private static final Variable healthState = new Variable("Health state");
    
    public static ProbNet createNoKnowledge() {
        ProbNet noKnowledge = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        
        // Create disease variable and potential
        noKnowledge.addNode(disease, NodeType.CHANCE);
        double[] diseaseValues = {0.14, 0.86};
        TablePotential diseasePotential = new TablePotential(getVariablesList(disease),
                                                             PotentialRole.CONDITIONAL_PROBABILITY, diseaseValues);
        noKnowledge.addPotential(diseasePotential);
        
        // Decision variable
        noKnowledge.addNode(therapy, NodeType.DECISION);
        therapy.setDecisionCriterion(new Criterion());
        
        // Create health state variable and potential
        noKnowledge.addNode(healthState, NodeType.UTILITY);
        healthState.setDecisionCriterion(new Criterion());
        ExactDistrPotential healthStatePotential = new ExactDistrPotential(Arrays.asList(healthState, therapy, disease),
                                                                           PotentialRole.UNSPECIFIED);
        healthStatePotential.setValues(new double[]{10.0, 9.0, 3.0, 8.0});
        noKnowledge.addPotential(healthStatePotential);
        
        // Create cost of therapy variable and potential
        noKnowledge.addNode(costOfTherapy, NodeType.UTILITY);
        costOfTherapy.setDecisionCriterion(new Criterion());
        ExactDistrPotential costOfTherapyPotential = new ExactDistrPotential(Arrays.asList(costOfTherapy, therapy),
                                                                             PotentialRole.UNSPECIFIED);
        costOfTherapyPotential.setValues(new double[]{0.0, -0.25});
        noKnowledge.addPotential(costOfTherapyPotential);
        
        return noKnowledge;
    }
    
    public static ProbNet createPerfectKnowledge() {
        ProbNet perfectKnowledge = createNoKnowledge();
        perfectKnowledge.addLink(disease, therapy, true);
        return perfectKnowledge;
    }
    
    public static ProbNet createTestDecisionID() {
        ProbNet testDecision = createNoKnowledge();
        testDecision.addNode(resultOfTest, NodeType.CHANCE);
        testDecision.addNode(doTest, NodeType.DECISION);
        testDecision.addNode(costOfTest, NodeType.UTILITY);
        testDecision.addLink(disease, resultOfTest, true);
        testDecision.addLink(resultOfTest, therapy, true);
        testDecision.addLink(doTest, resultOfTest, true);
        testDecision.addLink(doTest, costOfTest, true);
        testDecision.addLink(doTest, therapy, true);
        ExactDistrPotential costOfTherapyPotential = new ExactDistrPotential(
                Arrays.asList(costOfTherapy, resultOfTest));
        costOfTherapyPotential.setValues(new double[]{0.0, 20000.0, 70000.0});
        testDecision.addPotential(costOfTherapyPotential);
        ExactDistrPotential costOfTestPotential = new ExactDistrPotential(Arrays.asList(costOfTest, doTest));
        costOfTestPotential.setValues(new double[]{0.0, -0.2});
        testDecision.addPotential(costOfTherapyPotential);
        ExactDistrPotential resultOfTestPotential = new ExactDistrPotential(
                Arrays.asList(resultOfTest, resultOfTest, doTest, disease));
        resultOfTestPotential.setValues(new double[]{0.0, 0.0, 1.0, 0.03, 0.97, 0.0, 0.91, 0.09, 0.0});
        testDecision.addPotential(resultOfTestPotential);
        return testDecision;
    }
    
    /**
     * @param variablesArray
     * @return A <code>List</code> of <code>Variable</code>s
     */
    private static List<Variable> getVariablesList(Variable... variablesArray) {
        List<Variable> variablesList = new ArrayList<>(variablesArray.length);
        for (Variable variable : variablesArray) {
            variablesList.add(variable);
        }
        return variablesList;
    }
    
}