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

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoUtilityParentTest {
    
    private ProbNet probNetImproperUtilityChildren;
    private ProbNet probNetProperUtilityChildren;

    @BeforeEach public void setUp() {
        probNetImproperUtilityChildren = ConstraintsTests.getNotOnlyUtilityChildrenInfluenceDiagram();
        probNetProperUtilityChildren = ConstraintsTests.getOnlyUtilityChildrenInfluenceDiagram();
    }
    
    @Test public void testOnlyUtilityChilren() {
        assertFalse(probNetImproperUtilityChildren.checkProbNet());
        
        //test only utility children
        PNConstraint constraint = new NoUtilityParent();
        assertTrue(constraint.isMetBy(probNetProperUtilityChildren));
    }

    
}
