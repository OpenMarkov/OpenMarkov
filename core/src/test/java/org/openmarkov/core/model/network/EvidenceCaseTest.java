/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class EvidenceCaseTest {
    
    private Variable variableA;
    private Variable variableB;
    private Variable variableC;
    
    private State absent;
    private State present;
    
    private PotentialRole role;
    
    private ArrayList<Variable> variablesA;
    private ArrayList<Variable> variablesBA;
    private ArrayList<Variable> variablesCBA;
    
    private TablePotential potentialvaluesA;
    private TablePotential potentialvaluesAB;
    private TablePotential potentialvaluesCBA;
    
    private ProbNet probNet;
    
    //private NetworkTypeConstraint networkTypeConstraint = null;
    
    @BeforeEach public void setUp() {
        //Variables
        String a = new String("A");
        String b = new String("B");
        String c = new String("C");
        
        //finite States variables
        variableA = new Variable(a, 2);
        variableB = new Variable(b, 2);
        variableC = new Variable(c, 2);
        
        //additional properties
        String relevance = new String("Relevance");
        String value = new String("7.0");
        
        variableA.setAdditionalProperty(relevance, value);
        variableB.setAdditionalProperty(relevance, value);
        variableC.setAdditionalProperty(relevance, value);
        
        //Setting variable states
        absent = new State("absent");
        present = new State("present");
        State[] states = {absent, present};
        
        variableA.setStates(states);
        variableB.setStates(states);
        variableC.setStates(states);
        
        //Setting Precision
        double precision = 0.01;
        variableA.setPrecision(precision);
        variableB.setPrecision(precision);
        variableC.setPrecision(precision);
        
        //Potentials
        //PotentialType type = PotentialType.TABLE;
        role = PotentialRole.CONDITIONAL_PROBABILITY;
        
        // Potential for A: P(a)
        // It will induce the finding A = 0
        double[] tableA = {1.0, 0.0};
        variablesA = new ArrayList<>();
        variablesA.add(variableA);
        potentialvaluesA = new TablePotential(variablesA, role, tableA);
        
        // Potential for B: P(b|a)
        // It will induce the finding B = 1
        double[] tableBA = {0.0, 1.0, 0.2, 0.8};
        variablesBA = new ArrayList<>();
        variablesBA.add(variableB);
        variablesBA.add(variableA);
        potentialvaluesAB = new TablePotential(variablesBA, role, tableBA);
        
        // Potential for C: P(c|a,b)
        // It will induce the finding C = 1
        double[] tableCBA = {0.2, 0.8, 0.6, 0.4, 0.0, 1.0, 0.8, 0.2};
        variablesCBA = new ArrayList<>();
        variablesCBA.add(variableC);
        variablesCBA.add(variableA);
        variablesCBA.add(variableB);
        potentialvaluesCBA = new TablePotential(variablesCBA, role, tableCBA);
        
        // If NetworkTypeConstraint is null we create a Bayesian network
        // NetworkTypeConstraint networkTypeConstraint = null;
        // ProbNet probNet = new ProbNet(networkTypeConstraint);
        probNet = new ProbNet();
        
        probNet.addNode(variableA, NodeType.CHANCE);
        probNet.addNode(variableB, NodeType.CHANCE);
        probNet.addNode(variableC, NodeType.CHANCE);
        
        probNet.addLink(variableA, variableB, true);
        probNet.addLink(variableA, variableC, true);
        probNet.addLink(variableB, variableC, true);
        
        probNet.addPotential((Potential) potentialvaluesA);
        probNet.addPotential((Potential) potentialvaluesAB);
        probNet.addPotential((Potential) potentialvaluesCBA);
        
    }
    
    @Disabled
    @Test public void extendEvidence() {
        
        assertNotNull(probNet);
        EvidenceCase evidence = new EvidenceCase();
        Variable A = probNet.getVariable("A");
        assertNotNull(A);
        evidence.extendEvidence(probNet);
        assertEquals(3, evidence.getFindings().size());
        Variable B = probNet.getVariable("B");
        Finding bFinding = evidence.getFinding(B);
        assertNotNull(bFinding);
        assertEquals(1, bFinding.getStateIndex());
        
    }
    
}
