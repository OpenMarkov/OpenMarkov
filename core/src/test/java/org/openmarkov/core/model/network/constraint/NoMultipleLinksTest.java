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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoMultipleLinksTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
    }
    
    @Test public void testCheckProbNet() {
        NoMultipleLinks testedConstraint = new NoMultipleLinks();
        influenceDiagram.addConstraint(testedConstraint);
        Variable vu = influenceDiagram.getVariable("U");
        Variable va = influenceDiagram.getVariable("A");
        Variable vd = influenceDiagram.getVariable("D");
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        
        influenceDiagram.removeConstraint(testedConstraint);
        influenceDiagram.addLink(vu, va, true);
        influenceDiagram.addConstraint(testedConstraint);
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        
        influenceDiagram.removeConstraint(testedConstraint);
        influenceDiagram.addLink(vd, va, false);
        influenceDiagram.addConstraint(testedConstraint);
        assertFalse(testedConstraint.isMetBy(influenceDiagram));
    }
    
}
