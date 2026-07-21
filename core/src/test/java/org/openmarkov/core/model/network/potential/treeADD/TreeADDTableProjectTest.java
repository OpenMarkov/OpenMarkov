/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeADD;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TreeADDTableProjectTest {
    
    private Variable variableA;
    private Variable variableB;
    private Variable variableC;
    private State absent;
    private State present;
    private State mild;
    private State moderate;
    private State severe;
    private TreeADDPotential treeADD;
    
    @BeforeEach public void setUp() {
        // create variables
        variableA = new Variable("A", 4);
        variableB = new Variable("B", 2);
        variableC = new Variable("C", 2);
        
        // set variable states
        absent = new State("absent");
        present = new State("present");
        State[] states = {absent, present};
        
        mild = new State("mild");
        moderate = new State("moderate");
        severe = new State("severe");
        
        State[] statesA = {absent, mild, moderate, severe};
        
        variableC.setStates(states);
        variableB.setStates(states);
        variableA.setStates(statesA);
        
        List<Variable> variablesC = Arrays.asList(variableC);
        double[] tableBAbsent = {0.7, 0.3};
        TablePotential bAbsentPotential = new TablePotential(variablesC, PotentialRole.CONDITIONAL_PROBABILITY,
                                                             tableBAbsent);
        
        double[] tableBPresent = {0.8, 0.2};
        TablePotential bPresentPotential = new TablePotential(variablesC, PotentialRole.CONDITIONAL_PROBABILITY,
                                                              tableBPresent);
        
        double[] tableASevere = {0.6, 0.4};
        TablePotential aSeverePotential = new TablePotential(variablesC, PotentialRole.CONDITIONAL_PROBABILITY,
                                                             tableASevere);
        
        List<Variable> variablesCB = Arrays.asList(variableC, variableB);
        double[] tableAmoderate = {0.7, 0.3, 0.1, 0.9};
        TablePotential aModeratePotential = new TablePotential(variablesCB, PotentialRole.CONDITIONAL_PROBABILITY,
                                                               tableAmoderate);
        
        //Branches
        List<State> branchModerateStates = new ArrayList<>();
        branchModerateStates.add(moderate);
        List<Variable> parentVariables = Arrays.asList(variableC, variableA, variableB);
        TreeADDBranch branchModerate = new TreeADDBranch(branchModerateStates, variableA, aModeratePotential,
                                                         parentVariables);
        
        List<State> branchSevereStates = new ArrayList<>();
        branchSevereStates.add(severe);
        TreeADDBranch branchSevere = new TreeADDBranch(branchSevereStates, variableA, aSeverePotential,
                                                       parentVariables);
        
        //Subtree
        List<Variable> subVariables = Arrays.asList(variableC, variableB);
        TreeADDBranch branchAbsent = new TreeADDBranch(Arrays.asList(absent), variableB, bAbsentPotential,
                                                       subVariables);
        TreeADDBranch branchPresent = new TreeADDBranch(Arrays.asList(present), variableB, bPresentPotential,
                                                        subVariables);
        List<TreeADDBranch> subBranches = Arrays.asList(branchAbsent, branchPresent);
        TreeADDPotential aAbsentMildPotential = new TreeADDPotential(subVariables, variableB,
                                                                     PotentialRole.CONDITIONAL_PROBABILITY, subBranches);
        
        TreeADDBranch branchAbsentMild = new TreeADDBranch(Arrays.asList(absent, mild), variableA, aAbsentMildPotential,
                                                           parentVariables);
        
        List<TreeADDBranch> branches = new ArrayList<>();
        branches.add(branchAbsentMild);
        branches.add(branchModerate);
        branches.add(branchSevere);
        
        treeADD = new TreeADDPotential(parentVariables, variableA, PotentialRole.CONDITIONAL_PROBABILITY, branches);
    }
    
    @Test public void testTableProject() throws NumberFormatException, NonProjectablePotentialException {
        TablePotential tablePotential = treeADD.tableProject(null, null);
        List<Variable> variables = tablePotential.getVariables();
        Assertions.assertEquals(3, variables.size());
        Assertions.assertEquals(16, tablePotential.getValues().length);
        List<Variable> expectedVariables = Arrays.asList(variableC, variableB, variableA);
        TablePotential expectedTablePotential = new TablePotential(expectedVariables,
                                                                   PotentialRole.CONDITIONAL_PROBABILITY);
        expectedTablePotential.setValues(new double[]{0.7, 0.3, 0.8, 0.2, 0.7, 0.3, 0.8, 0.2, 0.7, 0.3, 0.1, 0.9, 0.6,
                0.4, 0.6, 0.4});
        expectedTablePotential = (TablePotential) expectedTablePotential.reorder(tablePotential.getVariables());
        
        Assertions.assertArrayEquals(expectedTablePotential.getValues(), tablePotential.getValues(), 0.001);
    }
}
