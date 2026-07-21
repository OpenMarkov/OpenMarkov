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


import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OnlyChanceNodesTest {
    
    // Attributes
    private ProbNet influenceDiagram;
    

    // Methods
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getInfuenceDiagram();
    }
    
    /**
     * Checks or not all the <code>probNet</code> in different situations in
     * <code>OnlyDirectedLinks</code> constructor.
     */
    @Test public void testCheckProbNet() {
        // test only directed links insertions without checking.
        assertFalse(new OnlyChanceNodes().isMetBy(influenceDiagram));
    }

    
}
