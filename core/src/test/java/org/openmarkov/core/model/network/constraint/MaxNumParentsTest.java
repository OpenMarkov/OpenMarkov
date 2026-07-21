/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;


import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MaxNumParentsTest {
    
    private ProbNet net;
    
    @BeforeEach public void setUp() {
        net = ConstraintsTests.getTestProbNetDirected();
    }
    
    @Tag(TestSpeed.SLOW)
    @Test public void testCheckProbNet() {
        MaxNumParents constraint = new MaxNumParents(1);
        net.removeConstraint(constraint);
        net.addConstraint(constraint);
        
        
        net.removeConstraint(constraint);
        Variable vD = new Variable("D");
        Variable vB = net.getVariable("B");
        net.addNode(vD, NodeType.CHANCE);
        net.addLink(vD, vB, true);
        net.addConstraint(constraint);
        assertFalse(constraint.isMetBy(net)); //La prueba solo se cumple si no se cumplen las constraints.
    }

    
}
