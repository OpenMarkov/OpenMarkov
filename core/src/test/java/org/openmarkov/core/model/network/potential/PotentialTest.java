/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.util.UtilTestMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of <code>Potential</code> class. As this class is abstract we use the
 * class <code>TablePotential</code>.
 */
public class PotentialTest {
    
    /**
     * Compares potential1 and potential2.
     *
     * @param potential1 <code>Potential</code>
     * @param potential2 <code>Potential</code>
     * @return <code>true</code> if both potentials are equal.
     */
    public static boolean equalPotentials(Potential potential1, Potential potential2) {
        boolean equals = true;
        if (potential1.getPotentialRole() == potential2.getPotentialRole() && potential1.getClass() == potential2
                .getClass() && potential1.getComment().contentEquals(potential2.getComment())
                && potential1.getNumVariables() == potential2.getNumVariables()
            //		&& potential1.isUtility() == potential2.isUtility()
        ) {
            List<Variable> variables1 = potential1.getVariables();
            List<Variable> variables2 = potential2.getVariables();
            int numVariables = variables1.size();
            int i = 0;
            while (i < numVariables && equals) {
                if (!VariableTest.equalVariables(variables1.get(i), variables2.get(i))) {
                    equals = false;
                }
                i++;
            }
            // TODO Mover a TablePotential
            if (potential1 instanceof TablePotential && potential2 instanceof TablePotential) {
                TablePotentialTest
                        .checkEqualPotentials((TablePotential) potential1, (TablePotential) potential2, 0.0001);
                equals = true;
            }
        }
        return equals;
    }
    
    // TODO Sobrecargar equals y poner en el comentario que
    // equals ya NO consiste en comparar la dirección de memoria de dos objetos
    // TODO Cada tipo de potential tiene que tener un método equals y llamar al del padre
    
    public static ProbNet getProbNet4ScaleTest() {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        // Variables
        Variable varA = new Variable("A", "absent", "present");
        Variable varB = new Variable("B");
        Variable varC = new Variable("C", "absent", "present");
        Variable varTreeAddUtility = new Variable("TreeAddUtility");
        Variable varTableUtility = new Variable("TableUtility");
        Variable varLCUtility = new Variable("LCUtility");
        Variable varExponentialUtility = new Variable("ExponentialUtility");
        
        // Nodes
        Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
        Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
        Node nodeC = probNet.addNode(varC, NodeType.CHANCE);
        Node nodeTreeAddUtility = probNet.addNode(varTreeAddUtility, NodeType.UTILITY);
        Node nodeTableUtility = probNet.addNode(varTableUtility, NodeType.UTILITY);
        Node nodeLCUtility = probNet.addNode(varLCUtility, NodeType.UTILITY);
        Node nodeExponentialUtility = probNet.addNode(varExponentialUtility, NodeType.UTILITY);
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeA, nodeTreeAddUtility, true);
        probNet.addLink(nodeA, nodeTableUtility, true);
        probNet.addLink(nodeB, nodeLCUtility, true);
        probNet.addLink(nodeC, nodeExponentialUtility, true);
        probNet.addLink(nodeC, nodeLCUtility, true);
        
        // Potentials
        UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeA.setPotential(potA);
        
        UniformPotential potB = new UniformPotential(Arrays.asList(varB), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeB.setPotential(potB);
        
        UniformPotential potC = new UniformPotential(Arrays.asList(varC), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeC.setPotential(potC);
        
        TreeADDPotential potTreeAddUtility = new TreeADDPotential(Arrays.asList(varTreeAddUtility, varA),
                                                                  PotentialRole.CONDITIONAL_PROBABILITY);
        TablePotential tablePotentialBranch1 = new TablePotential(UtilTestMethods
                                                                          .getListOfVariables(potTreeAddUtility.getBranches()
                                                                                                               .get(0)
                                                                                                               .getRootVariable(),
                                                                                              potTreeAddUtility.getBranches()
                                                                                                               .get(0)
                                                                                                               .getParentVariables()),
                                                                  PotentialRole.CONDITIONAL_PROBABILITY);
        tablePotentialBranch1.setValues(new double[]{5});
        potTreeAddUtility.getBranches().get(0).setPotential(tablePotentialBranch1);
        
        LinearCombinationPotential lcPotentialBranch2 = new LinearCombinationPotential(UtilTestMethods
                                                                                               .getListOfVariables(potTreeAddUtility.getBranches()
                                                                                                                                    .get(1)
                                                                                                                                    .getRootVariable(),
                                                                                                                   potTreeAddUtility.getBranches()
                                                                                                                                    .get(1)
                                                                                                                                    .getParentVariables()),
                                                                                       PotentialRole.CONDITIONAL_PROBABILITY);
        double[] coefficientsLCBranch2 = {5};
        lcPotentialBranch2.setCoefficients(coefficientsLCBranch2);
        potTreeAddUtility.getBranches().get(1).setPotential(lcPotentialBranch2);
        
        nodeTreeAddUtility.setPotential(potTreeAddUtility);
        
        ExactDistrPotential potTableUtility = new ExactDistrPotential(Arrays.asList(varTableUtility, varA));
        potTableUtility.setValues(new double[]{10, 9});
        nodeTableUtility.setPotential(potTableUtility);
        
        LinearCombinationPotential potLCUtility = new LinearCombinationPotential(
                Arrays.asList(varLCUtility, varB, varC), PotentialRole.CONDITIONAL_PROBABILITY);
        double[] coefficientsLC = {3.0, 5.0, 9.0};
        potLCUtility.setCoefficients(coefficientsLC);
        nodeLCUtility.setPotential(potLCUtility);
        
        ExponentialPotential potExponentialUtility = new ExponentialPotential(
                Arrays.asList(varExponentialUtility, varC), PotentialRole.CONDITIONAL_PROBABILITY);
        double[] coefficientsExp = {3.0, 7.0};
        potExponentialUtility.setCoefficients(coefficientsExp);
        nodeExponentialUtility.setPotential(potExponentialUtility);
        
        return probNet;
    }
    
    /**
     * Creates a potential
     */
    @Test public void testCreation() {
        TablePotential potential = new TablePotential(null, PotentialRole.CONDITIONAL_PROBABILITY);
        assertNotNull(potential);
        // test number of variables of created potential
        List<Variable> variables = potential.getVariables();
        assertEquals(0, variables.size());
        
        // check that potential creation with an array of zero variables
        // produces the same result as above.
        potential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        assertNotNull(potential);
        // test number of variables of created potential
        variables = potential.getVariables();
        assertEquals(0, variables.size());
    }
    
    @Test public void scalePotentialTest() {
        ProbNet probNet = getProbNet4ScaleTest();
        List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
        for (Node node : utilityNodes) {
            
            // Scale any potential by 0.5
            node.getPotentials().get(0).scalePotential(0.5);
            
            switch (node.getVariable().getName()) {
                case "TreeAddUtility":
                    TreeADDBranch b1 = ((TreeADDPotential) node.getPotentials().get(0)).getBranches().get(0);
                    double[] expectedBranch1Values = {2.5};
                    Assertions.assertArrayEquals(expectedBranch1Values, ((TablePotential) b1.getPotential()).getValues(),
                                                 0.001);
                    
                    TreeADDBranch b2 = ((TreeADDPotential) node.getPotentials().get(0)).getBranches().get(1);
                    double[] expectedBranch2Values = {2.5};
                    Assertions.assertArrayEquals(expectedBranch2Values,
                                                 ((LinearCombinationPotential) b2.getPotential()).getCoefficients(), 0.001);
                    
                    break;
                case "TableUtility":
                    double[] expectedTableValues = {5.0, 4.5};
                    Assertions.assertArrayEquals(expectedTableValues,
                                                 ((ExactDistrPotential) node.getPotentials()
                                                                            .get(0)).getValues(), 0.001);
                    break;
                case "LCUtility":
                    double[] expectedLCValues = {1.5, 2.5, 4.5};
                    Assertions.assertArrayEquals(expectedLCValues,
                                                 ((LinearCombinationPotential) node.getPotentials()
                                                                                   .get(0)).getCoefficients(), 0.001);
                    break;
                case "ExponentialUtility":
                    double[] expectedExponentialValues = {2.30685281944005, 7};
                    Assertions.assertArrayEquals(expectedExponentialValues,
                                                 ((ExponentialPotential) node.getPotentials()
                                                                             .get(0)).getCoefficients(), 0.001);
                    break;
                
            }
        }
        
    }
    
    @Test public void deltaPotentialDeepCopyTest() {
        Variable variable = new Variable("DeltaVariable");
        State deltaState = new State("Delta");
        State[] states = {deltaState, new State("state2"), new State("state3")};
        variable.setStates(states);
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        DeltaPotential deltaPotential = new DeltaPotential(variableList, PotentialRole.CONDITIONAL_PROBABILITY,
                                                           deltaState);
        DeltaPotential deltaPotentialCopy = (DeltaPotential) deltaPotential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(deltaPotential, deltaPotentialCopy);
        assertNotSame(deltaPotential.getState(), deltaPotentialCopy.getState());
    }
    
    @Test public void tablePotentialDeepCopyTest() {
        Variable variable = new Variable("TablePotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        TablePotential potential = new TablePotential(variableList, PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setComment("Comment");
        TablePotential potentialCopy = (TablePotential) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
        assertNotSame(potential.getValues(), potentialCopy.getValues());
        // Plain TablePotential never has strategyTrees; StrategicTablePotential carries them.
        assertFalse(potential.hasInterventions());
    }
    
    @Test
    public void conditionalGaussianPotentialDeepCopyTest() {
        Variable variable = new Variable("CycleLengthShiftVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        CycleLengthShift cycleLengthShift = new CycleLengthShift(variableList, new CycleLength());
        probNet.getNode("CycleLengthShiftVariable").setPotential(cycleLengthShift);
        
        Variable variable2 = new Variable("TablePotentialVariable");
        List<Variable> variableList2 = new ArrayList<>();
        variableList2.add(variable2);
        
        probNet.addNode(variable2, NodeType.CHANCE);
        
        TablePotential tablePotential = new TablePotential(variableList2, PotentialRole.CONDITIONAL_PROBABILITY);
        probNet.getNode("TablePotentialVariable").setPotential(tablePotential);
        
        List<Variable> allVariables = new ArrayList<>();
        allVariables.addAll(variableList);
        allVariables.addAll(variableList2);
        ConditionalGaussianPotential conditionalGaussianPotential =
                new ConditionalGaussianPotential(allVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        conditionalGaussianPotential.setComment("Comment");
        conditionalGaussianPotential.setMean(cycleLengthShift);
        conditionalGaussianPotential.setVariance(tablePotential);
        
        ConditionalGaussianPotential conditionalGaussianPotentialCopy =
                (ConditionalGaussianPotential) conditionalGaussianPotential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(conditionalGaussianPotential, conditionalGaussianPotentialCopy);
        assertTrue(conditionalGaussianPotential.getMean() != conditionalGaussianPotentialCopy.getMean());
        assertTrue(conditionalGaussianPotential.getVariance() != conditionalGaussianPotentialCopy.getVariance());
        
    }
    
    @Test public void cycleLengthShiftDeepCopyTest() {
        Variable variable = new Variable("CycleLengthShiftVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        CycleLengthShift potential = new CycleLengthShift(variableList, new CycleLength());
        potential.setComment("Comment");
        CycleLengthShift potentialCopy = (CycleLengthShift) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
    }
    
    @Test public void exponentialPotentialDeepCopyTest() {
        Variable variable = new Variable("ExponentialPotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        ExponentialPotential potential = new ExponentialPotential(variableList, PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setComment("Comment");
        potential.setCholeskyDecomposition(new double[]{1, 2, 3});
        potential.setCoefficients(new double[]{4, 5, 6});
        potential.setConstant(3);
        potential.setCovarianceMatrix(new double[]{8, 9, 10});
        potential.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "11"),
                new VariableExpression(Collections.emptyList(), "12"),
                new VariableExpression(Collections.emptyList(), "13"),
        });
        potential.covariates = new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "14"),
                new VariableExpression(Collections.emptyList(), "15"),
                new VariableExpression(Collections.emptyList(), "16"),
        };
        potential.sampledCoefficients = new double[]{17, 18, 19};
        
        ExponentialPotential potentialCopy = (ExponentialPotential) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
        
        assertNotSame(potential.getCholeskyDecomposition(), potentialCopy.getCholeskyDecomposition());
        assertNotSame(potential.getCoefficients(), potentialCopy.getCoefficients());
        assertNotSame(potential.getCovarianceMatrix(), potentialCopy.getCovarianceMatrix());
        assertNotSame(potential.getCovariates(), potentialCopy.getCovariates());
        assertNotSame(potential.covariates, potentialCopy.covariates);
        assertNotSame(potential.sampledCoefficients, potentialCopy.sampledCoefficients);
        
    }
    
    @Test public void linearCombinationPotentialDeepCopyTest() {
        Variable variable = new Variable("ExponentialPotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        LinearCombinationPotential potential = new LinearCombinationPotential(variableList,
                                                                              PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setComment("Comment");
        potential.setCholeskyDecomposition(new double[]{1, 2, 3});
        potential.setCoefficients(new double[]{4, 5, 6});
        potential.setConstant(3);
        potential.setCovarianceMatrix(new double[]{8, 9, 10});
        potential.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "11"),
                new VariableExpression(Collections.emptyList(), "12"),
                new VariableExpression(Collections.emptyList(), "13"),
        });
        potential.covariates = new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "14"),
                new VariableExpression(Collections.emptyList(), "15"),
                new VariableExpression(Collections.emptyList(), "16"),
        };
        
        potential.sampledCoefficients = new double[]{17, 18, 19};
        
        LinearCombinationPotential potentialCopy = (LinearCombinationPotential) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
        
        assertNotSame(potential.getCholeskyDecomposition(), potentialCopy.getCholeskyDecomposition());
        assertNotSame(potential.getCoefficients(), potentialCopy.getCoefficients());
        assertNotSame(potential.getCovarianceMatrix(), potentialCopy.getCovarianceMatrix());
        assertNotSame(potential.getCovariates(), potentialCopy.getCovariates());
        assertNotSame(potential.covariates, potentialCopy.covariates);
        assertNotSame(potential.sampledCoefficients, potentialCopy.sampledCoefficients);
        
    }
    
    @Test public void productPotentialDeepCopyTest() {
        Variable variable = new Variable("ExponentialPotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        ProductPotential potential = new ProductPotential(variableList, PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setComment("Comment");
        
        ProductPotential potentialCopy = (ProductPotential) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
    }
    
    @Test public void sameAsPreviousPotentialDeepCopyTest() {
        Variable variable = new Variable("ExponentialPotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        SameAsPrevious potential = new SameAsPrevious(variableList);
        potential.setComment("Comment");
        
        SameAsPrevious potentialCopy = (SameAsPrevious) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
    }
    
    @Test public void sumPotentialDeepCopyTest() {
        Variable variable = new Variable("ExponentialPotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        SumPotential potential = new SumPotential(variableList, PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setComment("Comment");
        
        SumPotential potentialCopy = (SumPotential) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
    }
    
    @Test public void uniformPotentialDeepCopyTest() {
        Variable variable = new Variable("ExponentialPotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        UniformPotential potential = new UniformPotential(variableList, PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setComment("Comment");
        
        UniformPotential potentialCopy = (UniformPotential) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
        
        assertEquals(potential.getDiscreteValue(), potentialCopy.getDiscreteValue());
    }
    
    @Test public void weibullHazardPotentialDeepCopyTest() {
        Variable variable = new Variable("ExponentialPotentialVariable");
        List<Variable> variableList = new ArrayList<>();
        variableList.add(variable);
        
        ProbNet probNet = getProbNet4ScaleTest();
        probNet.addNode(variable, NodeType.CHANCE);
        
        WeibullHazardPotential potential = new WeibullHazardPotential(variableList,
                                                                      PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setComment("Comment");
        potential.setCholeskyDecomposition(new double[]{1, 2, 3});
        potential.setCoefficients(new double[]{4, 5, 6});
        potential.setCovarianceMatrix(new double[]{8, 9, 10});
        potential.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "11"),
                new VariableExpression(Collections.emptyList(), "12"),
                new VariableExpression(Collections.emptyList(), "13"),
        });
        potential.covariates = new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "14"),
                new VariableExpression(Collections.emptyList(), "15"),
                new VariableExpression(Collections.emptyList(), "16"),
        };
        potential.sampledCoefficients = new double[]{17, 18, 19};
        potential.setLog(true);
        
        WeibullHazardPotential potentialCopy = (WeibullHazardPotential) potential.deepCopy(probNet);
        
        compareBasicCopiedAttributesPotential(potential, potentialCopy);
        
        assertNotSame(potential.getCholeskyDecomposition(), potentialCopy.getCholeskyDecomposition());
        assertNotSame(potential.getCoefficients(), potentialCopy.getCoefficients());
        assertNotSame(potential.getCovarianceMatrix(), potentialCopy.getCovarianceMatrix());
        assertNotSame(potential.getCovariates(), potentialCopy.getCovariates());
        assertNotSame(potential.covariates, potentialCopy.covariates);
        assertNotSame(potential.sampledCoefficients, potentialCopy.sampledCoefficients);
        assertEquals(potential.isLog(), potentialCopy.isLog());
        
    }
    
    /**
     * This method assert true if basic attributes of a potential are copies of the other.
     *
     * @param potential1
     * @param potential2
     */
    private void compareBasicCopiedAttributesPotential(Potential potential1, Potential potential2) {
        assertNotSame(potential1, potential2);
        if (potential1.getComment() != null) {
            assertEquals(potential1.getComment(), potential2.getComment());
        }
        //		if(potential1.getUtilityVariable() != null) {
        //			assertTrue(potential1.getUtilityVariable() != potential2.getUtilityVariable());
        //		}
        if (potential1.getVariables() != null) {
            assertNotSame(potential1.getVariables(), potential2.getVariables());
        }
        assertEquals(potential1.getNumVariables(), potential2.getNumVariables());
        assertSame(potential1.getPotentialRole(), potential2.getPotentialRole());
        assertSame(potential1.getCriterion(), potential2.getCriterion());
    }

    @Test
    public void sampleConditionedVariableThrowsWhenNotOverridden() {
        Variable variable = new Variable("v");
        State[] states = {new State("a"), new State("b"), new State("c")};
        variable.setStates(states);
        DeltaPotential potential = new DeltaPotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY,
                                                      states[0]);
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> potential.sampleConditionedVariable(new java.util.Random(), new java.util.HashMap<>()));
        assertTrue(ex.getMessage().contains("DeltaPotential"));
    }
}
