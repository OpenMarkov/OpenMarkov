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
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;


import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoMixedParentsTest {
    
    private ProbNet influenceDiagram;
    
    @BeforeEach public void setUp() {
        influenceDiagram = ConstraintsTests.getOnlyUtilityChildrenInfluenceDiagram();
    }
    
    @ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG,
            reasonDescription = "This test fails because it tried to add a link from decision node to a node, " +
                    "thing is... that is precisely what the constraint is for, meaning it doesn't contradict the " +
                    "constraint."
    )
    @Test public void testCheckProbNet() {
        NoMixedParents testedConstraint = new NoMixedParents();
        influenceDiagram.addConstraint(testedConstraint);
        assertTrue(testedConstraint.isMetBy(influenceDiagram));
        Variable vc = influenceDiagram.getVariable("C");
        Variable ve = new Variable("E", 2);
        influenceDiagram.addNode(ve, NodeType.DECISION);
        influenceDiagram.addLink(ve, vc, true);
        assertFalse(testedConstraint.isMetBy(influenceDiagram));
    }

    
}
