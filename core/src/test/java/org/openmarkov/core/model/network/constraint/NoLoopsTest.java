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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;


import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoLoopsTest {
    
    private ProbNet directedNet;
    private ProbNet undirectedNet;
    
    @BeforeEach public void setUp() {
        directedNet = ConstraintsTests.getTestProbNetDirected();
        undirectedNet = ConstraintsTests.getTestProbNetUndirected();
    }
    
    @Tag(TestSpeed.SLOW)
    @Test public void testCheckProbNet() {
        NoLoops testedConstraints = new NoLoops();
        directedNet.addConstraint(testedConstraints);
        assertTrue(testedConstraints.isMetBy(directedNet));
        
        Variable varA = directedNet.getVariable("A");
        Variable varC = directedNet.getVariable("C");
        directedNet.addLink(varA, varC, true);
        
        assertFalse(testedConstraints.isMetBy(directedNet));
        
        varA = undirectedNet.getVariable("A");
        Variable varB = undirectedNet.getVariable("B");
        varC = undirectedNet.getVariable("C");
        undirectedNet.addLink(varA, varC, false);
        undirectedNet.removeLink(varA, varC, false);
        undirectedNet.removeLink(varB, varC, false);
        undirectedNet.addLink(varB, varC, true);
        undirectedNet.addLink(varA, varC, true);
        assertFalse(testedConstraints.isMetBy(directedNet));
        
        undirectedNet.removeLink(varA, varB, false);
        undirectedNet.addLink(varA, varB, true);
        assertFalse(testedConstraints.isMetBy(directedNet));
    }

}

