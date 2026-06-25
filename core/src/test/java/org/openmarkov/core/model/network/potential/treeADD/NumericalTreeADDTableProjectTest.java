/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeADD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.factory.MIDFactory;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NumericalTreeADDTableProjectTest {
    private TreeADDPotential tree;
    private Variable age;
    
    @BeforeEach public void setUp() {
        State dead = new State("dead");
        State alive = new State("alive");
        State[] states = {dead, alive};
        Variable previousState = new Variable("state0", 2);
        previousState.setStates(states);
        Variable currentState = new Variable("state1", 2);
        currentState.setStates(states);
        age = new Variable("Age0", true, 0.0, 10.0, true, 0.01);
        
        List<Variable> treeVariables = new ArrayList<>();
        treeVariables.add(currentState);
        treeVariables.add(previousState);
        treeVariables.add(age);
        
        //subtree
        //branch 1
        Threshold min1 = new Threshold(0, false);
        Threshold max1 = new Threshold(5, true);
        
        List<Variable> table1Variables = new ArrayList<>();
        table1Variables.add(currentState);
        double[] tableBranch1 = {1.0, 0.0};
        TablePotential subTablePotential1 = new TablePotential(table1Variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                               tableBranch1);
        List<Variable> subParentVariables = new ArrayList<>();
        subParentVariables.add(currentState);
        subParentVariables.add(age);
        
        TreeADDBranch subBranch1 = new TreeADDBranch(min1, max1, age, subTablePotential1, subParentVariables);
        
        //branch 2
        Threshold min2 = new Threshold(5, true);
        Threshold max2 = new Threshold(10, false);
        
        List<Variable> table2Variables = new ArrayList<>();
        table2Variables.add(currentState);
        double[] tableBranch2 = {0.5, 0.5};
        TablePotential subTablePotential2 = new TablePotential(table2Variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                               tableBranch2);
        TreeADDBranch subBranch2 = new TreeADDBranch(min2, max2, age, subTablePotential2, subParentVariables);
        
        List<TreeADDBranch> subBranches = new ArrayList<>();
        subBranches.add(subBranch1);
        subBranches.add(subBranch2);
        
        TreeADDPotential subTree = new TreeADDPotential(subParentVariables, age, PotentialRole.CONDITIONAL_PROBABILITY,
                                                        subBranches);
        
        //tree
        List<Variable> parentVariables = new ArrayList<>();
        parentVariables.add(previousState);
        parentVariables.add(currentState);
        parentVariables.add(age);
        
        List<State> states1 = new ArrayList<>();
        states1.add(dead);
        double[] table1 = {0.0, 1.0};
        TablePotential tablePotential1 = new TablePotential(table2Variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                            table1);
        
        TreeADDBranch branch1 = new TreeADDBranch(states1, previousState, tablePotential1, parentVariables);
        
        List<State> states2 = new ArrayList<>();
        states2.add(alive);
        
        TreeADDBranch branch2 = new TreeADDBranch(states2, previousState, subTree, parentVariables);
        
        List<Variable> variables = new ArrayList<>();
        variables.add(currentState);
        variables.add(previousState);
        variables.add(age);
        List<TreeADDBranch> branches = new ArrayList<>();
        branches.add(branch1);
        branches.add(branch2);
        tree = new TreeADDPotential(variables, previousState, PotentialRole.CONDITIONAL_PROBABILITY, branches);
        
    }
    
    @Test public void testTableProject() throws NonProjectablePotentialException {
        List<Finding> findings = new ArrayList<>();
        Finding value = new Finding(age, 0.5);
        findings.add(value);
        EvidenceCase evidenceCase = new EvidenceCase(findings);
        
        TablePotential tablePotential = tree.tableProject(evidenceCase, null);
        List<Variable> variables = tablePotential.getVariables();
        assertEquals(2, variables.size());
        assertEquals(4, tablePotential.getValues().length);
        double[] projectedValues = {0.0, 1.0, 1.0, 0.0};
        
        assertEquals(projectedValues[0], tablePotential.getValues()[0], 0.1);
        assertEquals(projectedValues[1], tablePotential.getValues()[1], 0.1);
        assertEquals(projectedValues[2], tablePotential.getValues()[2], 0.1);
        assertEquals(projectedValues[3], tablePotential.getValues()[3], 0.1);
        
    }
    
    @Tag(TestSpeed.SLOW)
    @Test
    public void testTablePorjectNumericalTop() throws NonProjectablePotentialException {
        //Evidence
        ProbNet probNet = MIDFactory.createSemiMarkovOnlyChanceNet();
        List<Finding> findings = new ArrayList<>();
        findings.add(new Finding(probNet.getVariable("Duration [0]"), 1.0));
        
        EvidenceCase evidence = new EvidenceCase(findings);
        
        TablePotential tablePotential1 = probNet.getNode("State [1]")
                                                .getPotentials()
                                                .get(0)
                                                .tableProject(evidence, null);
        List<Variable> variables = tablePotential1.getVariables();
        assertEquals(2, variables.size());
        assertEquals(4, tablePotential1.getValues().length);
        assertEquals(0.5, tablePotential1.getValues()[0], 0.1);
        assertEquals(0.5, tablePotential1.getValues()[1], 0.1);
        assertEquals(0.0, tablePotential1.getValues()[2], 0.1);
        assertEquals(1.0, tablePotential1.getValues()[3], 0.1);
        
        
        List<Finding> findings2 = new ArrayList<>();
        findings2.add(new Finding(probNet.getVariable("Duration [0]"), 2.0));
        
        EvidenceCase evidence2 = new EvidenceCase(findings2);
        
        TablePotential tablePotential2 = probNet.getNode("State [1]")
                                                .getPotentials()
                                                .get(0)
                                                .tableProject(evidence2, null);
        List<Variable> variables2 = tablePotential2.getVariables();
        assertEquals(2, variables2.size());
        assertEquals(4, tablePotential2.getValues().length);
        assertEquals(0.5, tablePotential2.getValues()[0], 0.1);
        assertEquals(0.5, tablePotential2.getValues()[1], 0.1);
        assertEquals(0.0, tablePotential2.getValues()[2], 0.1);
        assertEquals(1.0, tablePotential2.getValues()[3], 0.1);
        
        
        List<Finding> findings3 = new ArrayList<>();
        findings3.add(new Finding(probNet.getVariable("Duration [0]"), 2.0));
        
        EvidenceCase evidence3 = new EvidenceCase(findings3);
        
        TablePotential tablePotential3 = probNet.getNode("State [1]")
                                                .getPotentials()
                                                .get(0)
                                                .tableProject(evidence3, null);
        List<Variable> variables3 = tablePotential3.getVariables();
        assertEquals(2, variables3.size());
        assertEquals(4, tablePotential3.getValues().length);
        assertEquals(0.5, tablePotential3.getValues()[0], 0.1);
        assertEquals(0.5, tablePotential3.getValues()[1], 0.1);
        assertEquals(0.0, tablePotential3.getValues()[2], 0.1);
        assertEquals(1.0, tablePotential3.getValues()[3], 0.1);
        
    }
}
