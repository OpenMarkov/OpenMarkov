/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.*;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DistinctLinksTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void testCheckProbNet() {
        DistinctLinks testedConstraint = new DistinctLinks();
        influenceDiagram.addConstraint(testedConstraint);
        Variable vu = influenceDiagram.getVariable("U");
        Variable va = influenceDiagram.getVariable("A");
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        
        influenceDiagram.addLink(va, vu, false);
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        
        Variable vd = influenceDiagram.getVariable("D");
        influenceDiagram.addLink(vu, vd, true);
        influenceDiagram.addLink(vu, vd, true);
        assertFalse(testedConstraint.isMetBy(influenceDiagram));
        
        influenceDiagram.removeLink(vu, vd, true);
        influenceDiagram.removeLink(vu, vd, true);
        influenceDiagram.addLink(vu, vd, true);
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        
        influenceDiagram.addLink(va, vu, true);
        assertFalse(testedConstraint.isMetBy(influenceDiagram));
        
        influenceDiagram.removeLink(va, vu, true);
        influenceDiagram.removeLink(va, vu, true);
        influenceDiagram.removeLink(va, vu, false);
        influenceDiagram.addLink(va, vu, false);
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
    }
    
    
}
