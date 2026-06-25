/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyFiniteStateVariablesTest {
    
    // Attributes
    private ProbNet influenceDiagram;
    private ProbNet mixedVariableInfluenceDiagram;
    
    // Methods
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
        mixedVariableInfluenceDiagram = getMixedVariableInfluenceDiagram();
    }
    
    private ProbNet getMixedVariableInfluenceDiagram() {
        ProbNet net = ConstraintsTests.getInfuenceDiagram();
        Variable vC = new Variable("C");
        Variable vU = net.getVariable("U");
        net.addNode(vC, NodeType.CHANCE);
        net.addLink(vU, vC, true);
        return net;
    }
    
    @Test public void testCheckProbNet() {
        OnlyFiniteStatesVariables testedConstraint = new OnlyFiniteStatesVariables();
        influenceDiagram.addConstraint(testedConstraint);
        mixedVariableInfluenceDiagram.addConstraint(testedConstraint);
        
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        assertFalse(testedConstraint.isMetBy(mixedVariableInfluenceDiagram));
    }

    
}
