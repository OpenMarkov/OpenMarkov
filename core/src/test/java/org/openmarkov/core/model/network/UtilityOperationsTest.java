/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IntervalsAreNotEvenException;
import org.openmarkov.core.exception.IntervalsAreNotMultipleOf3Exception;
import org.openmarkov.core.inference.MulticriteriaOptions.Type;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


public class UtilityOperationsTest {
    
    public static ProbNet getProbNet4Test() {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        // Variables
        Variable varDisease = new Variable("Disease", "absent", "present");
        Variable varResult_of_test = new Variable("Result of test", "not-performed", "negative", "positive");
        Variable varTherapy = new Variable("Therapy", "no", "yes");
        Variable varDo_test_ = new Variable("Do test?", "no", "yes");
        Variable varHealth_state = new Variable("Health state");
        Variable varCost_of_test = new Variable("Cost of test");
        
        // ProbNet Criteria
        List<Criterion> decisionCriteria = new ArrayList<>();
        
        Criterion criHealth_state = new Criterion("Effectiveness", "QALY");
        criHealth_state.setUnicriterizationScale(0.8);
        criHealth_state.setCECriterion(CECriterion.Effectiveness);
        decisionCriteria.add(criHealth_state);
        
        Criterion criCost_of_test = new Criterion("Cost", "€");
        criCost_of_test.setUnicriterizationScale(1);
        criCost_of_test.setCECriterion(CECriterion.Cost);
        decisionCriteria.add(criCost_of_test);
        
        probNet.setDecisionCriteria(decisionCriteria);
        
        // Assign criteria to variables
        varHealth_state.setDecisionCriterion(criHealth_state);
        varCost_of_test.setDecisionCriterion(criCost_of_test);
        
        probNet.getInferenceOptions().getMultiCriteriaOptions().setMainUnit("€");
        probNet.getInferenceOptions().getMultiCriteriaOptions().setMulticriteriaType(Type.UNICRITERION);
        
        // Nodes
        Node nodeDisease = probNet.addNode(varDisease, NodeType.CHANCE);
        Node nodeResult_of_test = probNet.addNode(varResult_of_test, NodeType.CHANCE);
        Node nodeTherapy = probNet.addNode(varTherapy, NodeType.DECISION);
        Node nodeDo_test_ = probNet.addNode(varDo_test_, NodeType.DECISION);
        Node nodeHealth_state = probNet.addNode(varHealth_state, NodeType.UTILITY);
        Node nodeCost_of_test = probNet.addNode(varCost_of_test, NodeType.UTILITY);
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeDisease, nodeHealth_state, true);
        probNet.addLink(nodeDisease, nodeResult_of_test, true);
        probNet.addLink(nodeResult_of_test, nodeTherapy, true);
        probNet.addLink(nodeTherapy, nodeHealth_state, true);
        probNet.addLink(nodeDo_test_, nodeCost_of_test, true);
        probNet.addLink(nodeDo_test_, nodeTherapy, true);
        probNet.addLink(nodeDo_test_, nodeResult_of_test, true);
        
        // Potentials
        TablePotential potDisease = new TablePotential(Arrays.asList(varDisease),
                                                       PotentialRole.CONDITIONAL_PROBABILITY);
        potDisease.setValues(new double[]{0.86, 0.14});
        nodeDisease.setPotential(potDisease);
        
        TablePotential potResult_of_test = new TablePotential(Arrays.asList(varResult_of_test, varDo_test_, varDisease),
                                                              PotentialRole.CONDITIONAL_PROBABILITY);
        potResult_of_test.setValues(new double[]{1, 0, 0, 0, 0.97, 0.03, 1, 0, 0, 0, 0.09, 0.91});
        nodeResult_of_test.setPotential(potResult_of_test);
        
        ExactDistrPotential potHealth_state = new ExactDistrPotential(
                Arrays.asList(varHealth_state, varDisease, varTherapy));
        potHealth_state.setValues(new double[]{10, 3, 9, 8});
        nodeHealth_state.setPotential(potHealth_state);
        
        ExactDistrPotential potCost_of_test = new ExactDistrPotential(Arrays.asList(varCost_of_test, varDo_test_));
        potCost_of_test.setValues(new double[]{0, -0.2});
        nodeCost_of_test.setPotential(potCost_of_test);
        
        // Link restrictions and revealing states
        // Always observed nodes
        
        return probNet;
    }
	/* TODO - Change the method. Use a 0 scale?
	@Test
	public void removeTerminalNullCostEffectivenessNodesTest(){
		ProbNet probNet = getProbNet4Test();
		UtilityOperations.removeTerminalNullCostEffectivenessNodes(probNet);
		
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		assertTrue(utilityNodes.size() == 1);
		
		for(Node node : utilityNodes){
			assertTrue(node.getVariable().getDecisionCriterion().getCECriterion() != CECriterion.Null);
		}
		
	}*/
    
    @Test
    public void transformToUnicriterionTest() {
        ProbNet probNet = getProbNet4Test();
        ProbNet unicriterizationNet = probNet.copy();
        
        UtilityOperations.transformToUnicriterion(unicriterizationNet);
        
        List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
        for (Node node : utilityNodes) {
            
            Node unicriterizatedNode = unicriterizationNet.getNode(node.getVariable());
            assertEquals(node.getPotentials().size(), unicriterizatedNode.getPotentials().size());
            assertNotSame(node.getPotentials(), unicriterizatedNode.getPotentials());
            double scale = node.getVariable().getDecisionCriterion().getUnicriterizationScale();
            for (int i = 0; i < node.getPotentials().size(); i++) {
                ExactDistrPotential potential = (ExactDistrPotential) node.getPotentials().get(i);
                ExactDistrPotential unicriterizatedPotential = (ExactDistrPotential) unicriterizatedNode.getPotentials()
                                                                                                        .get(i);
                Assertions.assertNotSame(potential, unicriterizatedPotential);
                Assertions.assertEquals(potential.getVariables(), unicriterizatedPotential.getVariables());
                double[] values = new double[potential.getTablePotential().getValues().length];
                
                for (int j = 0; j < values.length; j++) {
                    values[j] = scale * potential.getTablePotential().getValues()[j];
                }
                assertArrayEquals(values, unicriterizatedPotential.getTablePotential().getValues(), Math.pow(10, -7));
                
            }
//			assertTrue(node.getVariable().getDecisionCriterion().getCriterionName().equals(Criterion.C_GLOBALCRITERION));
        
        
        }
        
    }
    
    @Test public void applyRiemannSumAndTrapezoidalRuleTest() throws IntervalsAreNotEvenException {
        Random r = new Random();
        double[] values = new double[]{r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble(),
                r.nextDouble(), r.nextDouble()};
        double leftRiemannSum = UtilityOperations.applyLeftRiemannSum(values, 1);
        double rightRiemannSum = UtilityOperations.applyRightRiemannSum(values, 1);
        double trapezoidalRule = UtilityOperations.applyTrapezoidalRule(values, 1);
        Assertions.assertEquals((leftRiemannSum + rightRiemannSum) / 2.0, trapezoidalRule, Math.pow(10, -3));
        UtilityOperations.applyCompositeSimpsonsOneThirdRule(values, 1);
    }
    
    @Test
    /**
     * Test passed with other calculators: f(x) = (1/18)(x+5)(x+1)(x-4); a = 0; b = 54; n = 54.
     * Web for Riemann's Summs, Trapezoidal Rule and Simpson's One Third Rule: https://www.desmos.com/calculator/gdn6ldu1mw
     * Web for Simpson's Rules (both): http://www.atozmath.com/CONM/NumeInte.aspx
     */ public void applyRiemannsTrapezoidalAndSimpsonsRulesTest() throws IntervalsAreNotEvenException, IntervalsAreNotMultipleOf3Exception {
        double delta = Math.pow(10, -6);
        int numberOfCycles = 54;
        int numberOfSubintervalsPerCycle = 1;
        int numberOfTotalSubintervals = numberOfCycles * numberOfSubintervalsPerCycle;
        // We add one more subinterval for the 0 slice
        double[] values = new double[numberOfTotalSubintervals + 1];
        for (int i = 0; i <= numberOfTotalSubintervals; i++) {
            int cycleNumber = i / numberOfSubintervalsPerCycle;
            int subInterval = i % numberOfSubintervalsPerCycle;
            double x = ((double) i) / numberOfSubintervalsPerCycle;
            values[i] = (1.0 / 18.0) * (x + 5) * (x + 1) * (x - 4);
        }
        double leftRiemannSum = UtilityOperations.applyLeftRiemannSum(values, numberOfSubintervalsPerCycle);
        Assertions.assertEquals(117865, leftRiemannSum, delta);
        double rightRiemannSum = UtilityOperations.applyRightRiemannSum(values, numberOfSubintervalsPerCycle);
        Assertions.assertEquals(126880, rightRiemannSum, delta);
        double trapezoidalRule = UtilityOperations.applyTrapezoidalRule(values, numberOfSubintervalsPerCycle);
        Assertions.assertEquals(122372.5, trapezoidalRule, delta);
        
        double compositeSimpsonsOneThirdRule = UtilityOperations
                .applyCompositeSimpsonsOneThirdRule(values, numberOfSubintervalsPerCycle);
        Assertions.assertEquals(122331, compositeSimpsonsOneThirdRule, delta);
        
        double compositeSimpsonsThreeEighthsRule = UtilityOperations
                .applyCompositeSimpsonsThreeEighthsRule(values, numberOfSubintervalsPerCycle);
        Assertions.assertEquals(122331, compositeSimpsonsThreeEighthsRule, delta);
        double a = compositeSimpsonsOneThirdRule + compositeSimpsonsThreeEighthsRule;
    }
}
