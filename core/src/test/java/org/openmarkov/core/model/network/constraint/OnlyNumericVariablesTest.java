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
import org.openmarkov.core.model.network.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyNumericVariablesTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getNumericInfluenceDiagram();
    }
    
    @Test public void testCheckProbNet() {
        OnlyNumericVariables testedConstraint = new OnlyNumericVariables();
        influenceDiagram.addConstraint(testedConstraint);
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        
        influenceDiagram.addNode(new Variable("E", 2), NodeType.CHANCE);
        assertFalse(testedConstraint.isMetBy(influenceDiagram));
    }

    
}
