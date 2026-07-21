/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.*;

import java.util.List;

public class NetworkTypeTest {
    
    @Test public void testDefaultNetworkTypeIsBayesian() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        List<PNConstraint> constraints = probNet.getConstraints();
        Assertions.assertTrue(constraints.contains(new NoEmptyName()));
        Assertions.assertTrue(constraints.contains(new DistinctVariableNames()));
        Assertions.assertTrue(constraints.contains(new OnlyChanceNodes()));
        Assertions.assertTrue(constraints.contains(new OnlyAtemporalVariables()));
        Assertions.assertFalse(constraints.contains(new OnlyTemporalVariables()));
        Assertions.assertTrue(constraints.contains(new OnlyOneAgent()));
        Assertions.assertTrue(constraints.contains(new DistinctLinks()));
        Assertions.assertTrue(constraints.contains(new NoMultipleLinks()));
        Assertions.assertTrue(constraints.contains(new OnlyDirectedLinks()));
        Assertions.assertFalse(constraints.contains(new OnlyUndirectedLinks()));
        Assertions.assertTrue(constraints.contains(new NoRevelationArc()));
        Assertions.assertTrue(constraints.contains(new NoSelfLoop()));
        Assertions.assertTrue(constraints.contains(new NoCycle()));
    }
    
    @Test public void testConvertingBayesianIntoMarkov() throws ConstraintViolatedException {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.setNetworkType(MarkovNetworkType.getUniqueInstance());
        List<PNConstraint> constraints = probNet.getConstraints();
        Assertions.assertFalse(constraints.contains(new OnlyDirectedLinks()));
        Assertions.assertTrue(constraints.contains(new OnlyUndirectedLinks()));
    }
    
    @Test public void testRemovingConstraintsNoLongerApplicable() throws ConstraintViolatedException {
        ProbNet probNet = new ProbNet();
        probNet.setNetworkType(InfluenceDiagramType.getUniqueInstance());
        List<PNConstraint> constraints = probNet.getConstraints();
        Assertions.assertFalse(constraints.contains(new OnlyChanceNodes()));
    }
    
    //(expected = ConstraintViolatedException.class)
    @Disabled
    @Test public void testImpossibleNetworkTypeConversion() throws ConstraintViolatedException {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.setNetworkType(InfluenceDiagramType.getUniqueInstance());
        
        probNet.addNode(new Variable("a"), NodeType.DECISION);
        probNet.setNetworkType(BayesianNetworkType.getUniqueInstance());
    }
    
    //(expected = ConstraintViolatedException.class)
    @Test public void testAddingNotApplicableConstraints() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.addConstraint(new OnlyUndirectedLinks());
        
    }
}
