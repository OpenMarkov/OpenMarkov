/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author manuel
 * @author fjdiez
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TablePotentialTest {
    
    /*  Public scope for use in all tests. */
    public static final double maxError = 0.0001;
    
    private TablePotential tablePotential1;
    
    private TablePotential tablePotential2;
    
    private TablePotential tablePotential3;
    
    private TablePotential tablePotential4;
    
    /**
     * Two binary variables: fsVariable1 and fsVariable2.
     */
    private TablePotential tablePotential5;
    
    private List<Variable> fsVariables1;
    
    private List<Variable> fsVariables2;
    
    private List<Variable> fsVariables3;
    
    private List<Variable> fsVariables4;
    
    private List<Variable> fsVariables5;
    
    private Variable fsVariable1;
    
    private Variable fsVariable2;
    
    private Variable fsVariable3;
    
    private Variable fsVariable4;
    
    private Variable fsVariableB;
    
    private Variable fsVariableC;
    
    private Variable fsVariableD;
    
    private Variable fsVariableA;
    
    private State[] states1;
    
    private State[] states2;
    
    private State[] states3;
    
    private State[] states4;
    
    private State[] twoStates;
    
    private State[] threeStates;
    
    private State[] fourStates;
    
    private State[] fiveStates;
    
    private Finding finding1;
    
    /**
     * Two binary variables: fsVariable2 = 1, fsVariable4 = 0.
     */
    private EvidenceCase evidenceCase;
    
    /**
     * @param actual
     * @param expected Checks if two potentials are equal
     */
    public static void checkEqualPotentials(TablePotential actual, TablePotential expected, double maxError) {
        
        int numConfigurationsActual = actual.getTableSize();
        assertEquals(numConfigurationsActual, expected.getTableSize());
        
        List<Finding> findings;
        
        for (int i = 0; i < numConfigurationsActual; i++) {
            int[] auxConfiguration = actual.getConfiguration(i);
            double actualValue = actual.getValues()[i];
            findings = new ArrayList<>();
            List<Variable> variables = actual.getVariables();
            for (int j = 0; j < variables.size(); j++) {
                Variable variableJActual = variables.get(j);
                Variable variableInExpected = getVariableName(expected.getVariables(), variableJActual.getName());
                findings.add(new Finding(variableInExpected,
                                         variableInExpected.getState(variableJActual.getStates()[auxConfiguration[j]].getName())));
            }
            double expectedValue = expected.getValue(new EvidenceCase(findings));
            assertEquals(expectedValue, actualValue, maxError);
            
        }
        
    }
    
    private static Variable getVariableName(List<Variable> variables, String name) {
        boolean found = false;
        Variable var = null;
        for (int i = 0; i < variables.size() && !found; i++) {
            Variable auxVar = variables.get(i);
            if (auxVar.getName().equalsIgnoreCase(name)) {
                found = true;
                var = auxVar;
            }
        }
        
        return var;
    }
    
    @BeforeEach public void setUp() throws org.openmarkov.core.exception.IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        states1 = new State[]{new State("S1V1"), new State("S2V1")};
        states2 = new State[]{new State("S1V2"), new State("S2V2")};
        states3 = new State[]{new State("S1V3"), new State("S2V3")};
        states4 = new State[]{new State("S1V4"), new State("S2V4")};
        twoStates = new State[]{new State("S1Vx"), new State("S2Vx")};
        threeStates = new State[]{new State("S1Vx"), new State("S2Vx"), new State("S3Vx")};
        fourStates = new State[]{new State("S1Vx"), new State("S2Vx"), new State("S3Vx"), new State("S4Vx")};
        fiveStates = new State[]{new State("S1Vx"), new State("S2Vx"), new State("S3Vx"), new State("S4Vx"),
                new State("S5Vx")};
        fsVariable1 = new Variable("V1", states1);
        fsVariable2 = new Variable("V2", states2);
        fsVariable3 = new Variable("V3", states3);
        fsVariable4 = new Variable("V4", states4);
        fsVariableB = new Variable("VB", twoStates);
        fsVariableD = new Variable("VD", threeStates);
        fsVariableA = new Variable("VA", fourStates);
        fsVariableC = new Variable("VC", fiveStates);
        fsVariables1 = Arrays.asList(fsVariable2, fsVariable4, fsVariable1, fsVariable3);
        fsVariables2 = Arrays.asList(fsVariable1, fsVariable2, fsVariable3);
        fsVariables3 = Arrays.asList(fsVariableB, fsVariableD, fsVariableA, fsVariableC);
        fsVariables4 = Arrays.asList(fsVariableA, fsVariableB, fsVariableC);
        fsVariables5 = Arrays.asList(fsVariable1, fsVariable2);
        
        tablePotential1 = new TablePotential(fsVariables1, PotentialRole.CONDITIONAL_PROBABILITY);
        tablePotential2 = new TablePotential(fsVariables2, PotentialRole.CONDITIONAL_PROBABILITY);
        tablePotential3 = new TablePotential(fsVariables3, PotentialRole.CONDITIONAL_PROBABILITY);
        tablePotential4 = new TablePotential(fsVariables4, PotentialRole.CONDITIONAL_PROBABILITY);
        tablePotential5 = new TablePotential(fsVariables5, PotentialRole.CONDITIONAL_PROBABILITY);
        // initialize tables
        double[] table = tablePotential4.getValues();
        for (int i = 0; i < table.length; i++) {
            table[i] = Double.valueOf(i);
        }
        table = tablePotential5.getValues();
        for (int i = 0; i < table.length; i++) {
            table[i] = Double.valueOf(i);
        }
        finding1 = new Finding(fsVariable2, 1);
        evidenceCase = new EvidenceCase();
        Finding finding1 = new Finding(fsVariable2, 1);
        Finding finding2 = new Finding(fsVariable4, 0);
        evidenceCase.addFinding(finding1);
        evidenceCase.addFinding(finding2);
    }
    
    @Test public void testTablePotential() {
        assertEquals(16, tablePotential1.getValues().length);
    }
    
    /**
     * test the offsets array length and the value of each offset
     */
    @Test public void testGetOffsets() {
        int[] offsets = tablePotential1.getOffsets();
        assertEquals(4, offsets.length);
        assertEquals(1, offsets[0]);
        assertEquals(2, offsets[1]);
        assertEquals(4, offsets[2]);
        assertEquals(8, offsets[3]);
    }
    
    @Test public void testGetTable() {
        double[] table = tablePotential1.getValues();
        assertEquals(16, table.length);
    }
    
    @Test public void testGetAccumulateOffsets()
            throws org.openmarkov.core.exception.IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        // tablePotential1 contains B,D,A,C. Dimensions (2,2,2,2)
        // tablePotential2 contains A,B,C. Dimensions (2,2,2)
        int[] accOffsets = tablePotential1.getAccumulatedOffsets(tablePotential2.getVariables());
        
        assertEquals(4, accOffsets.length);
        
        assertEquals(2, accOffsets[0]);
        assertEquals(-2, accOffsets[1]);
        assertEquals(-1, accOffsets[2]);
        assertEquals(1, accOffsets[3]);
        
        // tablePotential3 contains B,D,A,C. Dimensions (2,3,4,5)
        // tablePotential4 contains A,B,C. Dimensions (4,2,5)
        
        accOffsets = tablePotential3.getAccumulatedOffsets(tablePotential4.getVariables());
        
        assertEquals(4, accOffsets.length);
        
        assertEquals(4, accOffsets[0]);
        assertEquals(-4, accOffsets[1]);
        assertEquals(-3, accOffsets[2]);
        assertEquals(1, accOffsets[3]);
        
        // test getAccumulatedOffsets with previous projected tablePotentials
        evidenceCase = new EvidenceCase();
        finding1 = new Finding(fsVariableD, 1);
        evidenceCase.addFinding(finding1);
        
        TablePotential projected = tablePotential3.tableProject(evidenceCase, null);
        TablePotential tp3Projected = projected;
        projected = tablePotential4.tableProject(evidenceCase, null);
        TablePotential tp4Projected = projected;
        
        accOffsets = tp3Projected.getAccumulatedOffsets(tp4Projected.getVariables());
        
        assertEquals(3, accOffsets.length);
        
        assertEquals(4, accOffsets[0]);
        assertEquals(-3, accOffsets[1]);
        assertEquals(1, accOffsets[2]);
        
        evidenceCase = new EvidenceCase();
        Finding finding2 = new Finding(fsVariableB, 1);
        evidenceCase.addFinding(finding2);
        
        projected = tp3Projected.tableProject(evidenceCase, null);
        tp3Projected = (TablePotential) projected;
        projected = tp4Projected.tableProject(evidenceCase, null);
        tp4Projected = (TablePotential) projected;
        
        accOffsets = tp3Projected.getAccumulatedOffsets(tp4Projected.getVariables());
        
        assertEquals(2, accOffsets.length);
        
        assertEquals(1, accOffsets[0]);
        assertEquals(1, accOffsets[1]);
    }
    
    /*
     * @Test /** tablePotential5 has two variables: fsVariable1 and fsVariable2,
     * each one with 2 states.<p> evidenceCase: fsVariable2 = 1, fsVariable4 =
     * 0.
     */
    @Disabled
    @Test
    public void testProject1() throws NonProjectablePotentialException {
        TablePotential projected = tablePotential5.tableProject(evidenceCase, null);
        
        // Test projected variables
        assertEquals(1, projected.getVariables().size());
        assertEquals(fsVariable1, projected.getVariables().get(0));
        
        // Test same table as original potential
        double[] tableProjected = projected.getValues();
        assertArrayEquals(tablePotential5.getValues(), tableProjected);
        
        // Initial position
        int initialPosition = projected.getInitialPosition();
        assertEquals(2, initialPosition);
        
        // Offsets
        int[] offsets = projected.getOffsets();
        
        // Test table content
        assertEquals(1, offsets.length);
        assertEquals(1, offsets[0]);
        assertEquals(2.0, tableProjected[initialPosition], 0.001);
        assertEquals(3.0, tableProjected[initialPosition + offsets[0]], 0.001);
    }
    
    @Test
    /** tablePotential5 has two variables: fsVariable1 and fsVariable2, each one
     *  with 2 states.<p>
     *  evidenceCase: fsVariable2 = 1, fsVariable4 = 0. */ public void testProject2() throws NonProjectablePotentialException {
        
        TablePotential projected = tablePotential5.tableProject(evidenceCase, null);
        
        // Test projected variables
        assertEquals(1, projected.getVariables().size());
        assertEquals(fsVariable1, projected.getVariables().get(0));
        
        // Test projected potential size
        double[] tableProjected = projected.getValues();
        assertEquals(2, tableProjected.length);
        
        // Initial position
        int initialPosition = projected.getInitialPosition();
        assertEquals(0, initialPosition);
        
        // Offsets
        int[] offsets = projected.getOffsets();
        
        // Test table content
        assertEquals(1, offsets.length, maxError);
        assertEquals(1, offsets[0], maxError);
        assertEquals(2.0, tableProjected[initialPosition], maxError);
        assertEquals(3.0, tableProjected[initialPosition + offsets[0]], maxError);
    }
    
    /*
     * @Test public void testReorder() { TablePotential tablePotential; Variable
     * A = new Variable("A", 2); Variable B = new Variable("B", 2); Variable C =
     * new Variable("C", 2); ArrayList<Variable> baPotentialVariables = new
     * ArrayList<Variable>(); baPotentialVariables.add(B);
     * baPotentialVariables.add(A); ArrayList<Variable> cabPotentialVariables =
     * new ArrayList<Variable>(); cabPotentialVariables.add(C);
     * cabPotentialVariables.add(A); cabPotentialVariables.add(B); double[]
     * baTable = {0.7, 0.3, 0.9, 0.1};
     *
     * double[] cabTable = {0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02};
     * TablePotential cabPotential = new
     * TablePotential(cabPotentialVariables,PotentialRole
     * .CONDITIONAL_PROBABILITY, cabTable); TablePotential bcPotential = new
     * TablePotential
     * (baPotentialVariables,PotentialRole.CONDITIONAL_PROBABILITY, baTable);
     * ArrayList<Variable> newOrderVariables = new ArrayList<Variable>();
     *
     * end=-1;
     *
     * if ( cabPotentialVariables.size() > 0 ){
     *
     * for (int i = cabPotentialVariables.size()-1; i>end; i--){
     * newOrderVariables.add(cabPotentialVariables.get(i)); //newOrderVariables
     * B A C
     *
     * }
     *
     * } tablePotential = DiscretePotentialOperations.reorder( cabPotential,
     * newOrderVariables ); }
     *
     *
     * /* @Test /** Test accumulated offsets in projected potentials.
     */
    /*
     * public void testGetAccumulateOffsetsProjected() throws NoFindingException
     * { // Create data // Variables Variable A = new Variable("A", 2); Variable
     * B = new Variable("B", 2); Variable C = new Variable("C", 2); // Arrays of
     * variables ArrayList<Variable> baPotentialVariables = new
     * ArrayList<Variable>(); baPotentialVariables.add(B);
     * baPotentialVariables.add(A); ArrayList<Variable> cabPotentialVariables =
     * new ArrayList<Variable>(); cabPotentialVariables.add(C);
     * cabPotentialVariables.add(A); cabPotentialVariables.add(B);
     * ArrayList<Variable> bcPotentialVariables = new ArrayList<Variable>();
     * bcPotentialVariables.add(B); bcPotentialVariables.add(C); // table of
     * potentials double[] baTable = {0.7, 0.3, 0.9, 0.1}; double[] cabTable =
     * {0.15, 0.85, 0.84, 0.16, 0.29, 0.71, 0.98, 0.02}; // tablePotentials
     * TablePotential cabPotential = new TablePotential(cabPotentialVariables,
     * cabTable); TablePotential bcPotential = new
     * TablePotential(bcPotentialVariables, baTable); // evidence case Finding
     * a0Finding = new Finding(A, 0); Finding b1Finding = new Finding(B, 1);
     * EvidenceCase evidenceCaseA0 = new EvidenceCase();
     * evidenceCaseA0.addFinding(a0Finding); EvidenceCase evidenceCaseB1 = new
     * EvidenceCase(); evidenceCaseB1.addFinding(b1Finding); // project
     * potentials TablePotential cbPotential =
     * (TablePotential)cabPotential.project(evidenceCaseA0).get(0);
     *
     * ArrayList<Variable> otherVariables = new ArrayList<Variable>();
     * otherVariables.add(B); otherVariables.add(C); int[]
     * cabAccOffsetsNotProjected = bcPotential
     * .getAccumulatedOffsets(cabPotential.getVariables()); int[]
     * cbAccOffsetsProjected = bcPotential
     * .getAccumulatedOffsets(cbPotential.getOriginalVariables()); int
     * cabAccOffsetsLength = cabAccOffsetsNotProjected.length;
     * assertEquals(cabAccOffsetsLength, cbAccOffsetsProjected.length); for(int
     * i = 0; i < cabAccOffsetsLength; i++) {
     * assertEquals(cabAccOffsetsNotProjected[i], cbAccOffsetsProjected[i]); } }
     */
    
    @Test
    /** Test multiplication of projected potentials. */ public void testMultiplicationProjected() throws NonProjectablePotentialException {
        int dimA = 3;
        int dimB = 2;
        int dimC = 3;
        Variable A = new Variable("A", dimA);
        Variable B = new Variable("B", dimB);
        Variable C = new Variable("C", dimC);
        List<Variable> variablesTPAB = new ArrayList<>();
        variablesTPAB.add(A);
        variablesTPAB.add(B);
        List<Variable> variablesTPCBA = new ArrayList<>();
        variablesTPCBA.add(C);
        variablesTPCBA.add(B);
        variablesTPCBA.add(A);
        double[] tableAB = new double[]{0.2, 0.1, 0.7, 0.6, 0.4, 0.0};
        TablePotential tpAB = new TablePotential(variablesTPAB, PotentialRole.CONDITIONAL_PROBABILITY, tableAB);
        double[] tableCBA = new double[]{0.0, 0.1, 0.9, 0.7, 0.1, 0.2, 0.6, 0.2, 0.2, 0.15, 0.35, 0.5, 0.55, 0.25,
                0.2, 0.85, 0.05, 0.1};
        TablePotential tpCBA = new TablePotential(variablesTPCBA, PotentialRole.CONDITIONAL_PROBABILITY, tableCBA);
        
        Finding findingA1 = new Finding(A, 1);
        HashMap<Variable, Finding> findingsA1 = new HashMap<>();
        findingsA1.put(A, findingA1);
        EvidenceCase evidenceCaseA1 = new EvidenceCase(findingsA1);
        
        TablePotential projectedPotential = tpAB.tableProject(evidenceCaseA1, null);
        TablePotential projectedPotentialB = (TablePotential) projectedPotential;
        
        List<TablePotential> projectedPotentials = new ArrayList<>(Arrays.asList(tpCBA.tableProject(evidenceCaseA1, null)));
        
        // Test multiply projected potentials
        projectedPotentials.add(projectedPotentialB);
        TablePotential multiplication = DiscretePotentialOperations.multiply(projectedPotentials);
        multiplication = (TablePotential) multiplication.reorder(Arrays.asList(B, C));
        // Test variables
        List<Variable> variables = multiplication.getVariables();
        assertEquals(2, variables.size());
        
        // Test table
        assertEquals(6, multiplication.getValues().length);
        assertEquals(0.06, multiplication.getValues()[0], maxError);
        assertEquals(0.06, multiplication.getValues()[1], maxError);
        assertEquals(0.02, multiplication.getValues()[2], maxError);
        assertEquals(0.14, multiplication.getValues()[3], maxError);
        assertEquals(0.02, multiplication.getValues()[4], maxError);
        assertEquals(0.2, multiplication.getValues()[5], maxError);
    }
    
    @Test public void testGetInitialPosition() {
        assertEquals(0, tablePotential1.getInitialPosition());
    }
    
}
