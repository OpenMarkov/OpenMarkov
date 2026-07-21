/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoCyclesRelatedEditsTest {
    
    private ProbNet probNetDirected;
    
    @BeforeEach public void setUp() {
        probNetDirected = ConstraintsTests.getTestProbNetDirected();
    }
    
    /**
     * Checks or not all the <code>probNet</code> in different situations in
     * <code>OnlyDirectedLinks</code> constructor.
     *
     */
    @Tag(TestSpeed.SLOW)
    @Test public void testCheckProbNet() {
        NoCycle testedConstraint = new NoCycle();
        probNetDirected.addConstraint(testedConstraint);
        Node nodeA = probNetDirected.getNode("A", NodeType.CHANCE);
        assertNotNull(nodeA, "node A should exist in the test network");
        Variable va = nodeA.getVariable();
        Node nodeC = probNetDirected.getNode("C", NodeType.CHANCE);
        assertNotNull(nodeC, "node C should exist in the test network");
        Variable vc = nodeC.getVariable();
        
        assertTrue(testedConstraint.isMetBy(probNetDirected));
        try {
            new AddLinkEdit(probNetDirected, vc, va, true).executeEdit();
            fail();
        } catch (DoEditException e) {
            //Exception should have happened.
        }
    }
    
}
