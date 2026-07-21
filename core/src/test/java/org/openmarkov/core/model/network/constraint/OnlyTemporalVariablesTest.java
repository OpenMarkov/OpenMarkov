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
public class OnlyTemporalVariablesTest {
    
    private ProbNet network;
    
    @BeforeEach public void setUp() {
        network = ConstraintsTests.getTemporalVarNet();
    }
    
    @Test public void testCheckProbNet() {
        OnlyTemporalVariables testedConstraint = new OnlyTemporalVariables();
        network.addConstraint(testedConstraint);
        assertTrue(testedConstraint.isMetBy(network));
        
        network.addNode(new Variable("A"), NodeType.CHANCE);
        assertFalse(testedConstraint.isMetBy(network));
    }

    
}
