/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class StrategyTreeTest {
    
    private Variable chanceVar0, chanceVar1, chanceVar2;
    private Variable decisionVar;
    private StrategyTree strategyTree0;
    private StrategyTree strategyTree1;
    private StrategyTree strategyTree2;
    private List<StrategyTree> strategyTrees;
    private List<State> chanceStates;
    
    @BeforeEach public void setUp() {
        chanceVar0 = new Variable("chance", "chance0", "chance1", "chance2");
        chanceVar1 = new Variable("chance", "chance0", "chance1", "chance2");
        chanceVar2 = new Variable("chance", "chance0", "chance1", "chance2");
        decisionVar = new Variable("decision", "decision0", "decision1", "decision2");
        strategyTree0 = new StrategyTree(chanceVar0);
        strategyTree1 = new StrategyTree(chanceVar1);
        strategyTree2 = new StrategyTree(chanceVar2);
        strategyTrees = new ArrayList<>();
        chanceStates = new ArrayList<>();
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    /** Test constructor with several interventions and states */ public void testIntervention1() {
        
        strategyTrees = Arrays.asList(strategyTree0, strategyTree1, strategyTree2);
        chanceStates = Arrays.asList(chanceVar0.getStates());
        
        // Test 1: All interventions are distinct.
        StrategyTree testStrategyTree0 = new StrategyTree(decisionVar, chanceStates, strategyTrees);
        
        List<TreeADDBranch> branches = testStrategyTree0.getBranches();
        assertEquals(3, branches.size());
        TreeADDBranch branch0 = testStrategyTree0.getBranch(chanceStates.get(0));
        assertEquals(branch0.getPotential(), strategyTree0);
        branch0 = testStrategyTree0.getBranch(chanceStates.get(1));
        assertEquals(branch0.getPotential(), strategyTree1);
        branch0 = testStrategyTree0.getBranch(chanceStates.get(2));
        assertEquals(branch0.getPotential(), strategyTree2);
        assertEquals(decisionVar, testStrategyTree0.getRootVariable());
        
        // Test 2: Two interventions are equal.
        strategyTrees = Arrays.asList(strategyTree0, strategyTree1, strategyTree0);
        
        StrategyTree testStrategyTree1 = new StrategyTree(decisionVar, chanceStates, strategyTrees);
        
        branches = testStrategyTree1.getBranches();
        assertEquals(2, branches.size());
        branch0 = testStrategyTree1.getBranch(chanceStates.get(0));
        assertEquals(branch0.getPotential(), strategyTree0);
        List<State> branchStates = branch0.getBranchStates();
        assertTrue(branchStates.contains(chanceVar0.getState("chance2")));
        assertTrue(branchStates.contains(chanceVar0.getState("chance0")));
        TreeADDBranch branch1 = testStrategyTree1.getBranch(chanceStates.get(1));
        assertEquals(branch1.getPotential(), strategyTree1);
        assertEquals(decisionVar, testStrategyTree1.getRootVariable());
        
        // Test 3: All interventions are null. Intervention without branches
        strategyTrees = Arrays.asList(null, null, null);
        StrategyTree testStrategyTree2 = new StrategyTree(decisionVar, chanceStates, strategyTrees);
        
        branches = testStrategyTree2.getBranches();
        assertEquals(0, branches.size());
        assertEquals(decisionVar, testStrategyTree2.getRootVariable());
    }
    
    /**
     * Simple test for equals
     */
    @Test public void testEquals1() {
        // Create several interventions with small differences. All of them are different
        StrategyTree[] testInterventions0 = new StrategyTree[5];
        testInterventions0[0] = new StrategyTree(chanceVar0);
        
        testInterventions0[1] = new StrategyTree(chanceVar0);
        List<State> statesTestIntervention1 = Arrays.asList(chanceVar0.getState("chance0"));
        testInterventions0[1].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, strategyTree0, null));
        
        testInterventions0[2] = new StrategyTree(chanceVar0);
        List<State> statesTestIntervention2 = Arrays.asList(chanceVar0.getState("chance1"));
        testInterventions0[2].addBranch(new TreeADDBranch(statesTestIntervention2, chanceVar0, strategyTree0, null));
        
        testInterventions0[3] = new StrategyTree(chanceVar1);
        testInterventions0[3].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar1, strategyTree0, null));
        
        testInterventions0[4] = new StrategyTree(chanceVar0);
        testInterventions0[4].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, strategyTree1, null));
        
        // Test begins here
        for (int i = 0; i < testInterventions0.length - 1; i++) {
            for (int j = i + 1; j < testInterventions0.length; j++) {
                if (testInterventions0[i].equals(testInterventions0[j])) {
                    System.out.println("Aqui");
                }
                assertFalse(testInterventions0[i].equals(testInterventions0[j]));
            }
        }
        
        // Create the same interventions with different objects
        StrategyTree[] testInterventions1 = new StrategyTree[5];
        testInterventions1[0] = new StrategyTree(chanceVar0);
        
        testInterventions1[1] = new StrategyTree(chanceVar0);
        testInterventions1[1].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, strategyTree0, null));
        
        testInterventions1[2] = new StrategyTree(chanceVar0);
        testInterventions1[2].addBranch(new TreeADDBranch(statesTestIntervention2, chanceVar0, strategyTree0, null));
        
        testInterventions1[3] = new StrategyTree(chanceVar1);
        testInterventions1[3].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar1, strategyTree0, null));
        
        testInterventions1[4] = new StrategyTree(chanceVar0);
        testInterventions1[4].addBranch(new TreeADDBranch(statesTestIntervention1, chanceVar0, strategyTree1, null));
        
        // Test begins here
        for (int i = 0; i < testInterventions1.length; i++) {
            if (!testInterventions0[i].equals(testInterventions1[i])) {
                System.out.println("Aqui");
            }
            assertTrue(testInterventions0[i].equals(testInterventions1[i]));
        }
    }
    
}
