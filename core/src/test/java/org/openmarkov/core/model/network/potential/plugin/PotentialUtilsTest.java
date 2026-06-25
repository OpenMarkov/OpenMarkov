/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.plugin;

import org.junit.jupiter.api.*;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PotentialUtilsTest {
    
    Variable variableA;
    Variable variableB;
    Variable variableC;
    Variable variableU;
    ProbNet probNet;
    Node nodeU;
    
    @BeforeEach public void setUp() {
        probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        variableA = new Variable("A", "no", "yes");
        variableB = new Variable("B", "no", "yes");
        variableC = new Variable("C", "no", "yes");
        variableU = new Variable("U");
        
        probNet.addNode(variableA, NodeType.CHANCE);
        probNet.addNode(variableB, NodeType.CHANCE);
        probNet.addNode(variableC, NodeType.CHANCE);
        nodeU = probNet.addNode(variableU, NodeType.UTILITY);
        
        probNet.addLink(variableB, variableA, true);
        probNet.addLink(variableC, variableA, true);
        probNet.addLink(variableB, variableU, true);
        probNet.addLink(variableC, variableU, true);
        probNet.addLink(variableA, variableU, true);
        
        List<Variable> potentialUVariables = new ArrayList<Variable>();
        potentialUVariables.addAll(Arrays.asList(variableU, variableA, variableB, variableC));
        Potential potentialU = new UniformPotential(potentialUVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        nodeU.setPotential(potentialU);
    }
    
}
