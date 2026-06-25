/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TemporalNetOperationsTest {
    
    private ProbNet probNet;
    
    @BeforeEach
    public void setUp() throws java.net.URISyntaxException, org.openmarkov.core.exception.ParserException, IOException {
        String networkName = "networks/mid/SimpleTemporalUtilityNode.pgmx";
        // Open the file containing the network
        
        // Load the Bayesian network
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        probNet = pgmxReader.read(getClass().getClassLoader().getResource(networkName)).probNet();
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(15);
        
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void applyDiscountToUtilityNodesTest1() {
        // Set discount to the node
        for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
            utilityNode.getVariable().getDecisionCriterion().setDiscount(0.2);
        }
        // Expand the network
        this.probNet = TemporalNetOperations.expandNetwork(probNet);
        
        // Apply discount
        TemporalNetOperations.applyDiscountToUtilityNodes(probNet);
        
        for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
            double potential1 = ((ExactDistrPotential) utilityNode.getPotentials().get(0))
                    .getTablePotential().getValues()[0];
            double potential2 = ((ExactDistrPotential) utilityNode.getPotentials().get(0))
                    .getTablePotential().getValues()[1];
            int numSlice = utilityNode.getVariable().getTimeSlice();
            double discount = CycleLength.getTemporalAdjustedDiscount(probNet.getCycleLength().getUnit(),
                                                                      probNet.getCycleLength().getValue(),
                                                                      utilityNode.getVariable()
                                                                                 .getDecisionCriterion()
                                                                                 .getDiscountUnit(),
                                                                      utilityNode.getVariable()
                                                                                 .getDecisionCriterion()
                                                                                 .getDiscount());
            
            // Utility Potential U[0] = [20, 50]
            double expectedPotential1 = 20.0 / (Math.pow(1 + discount, numSlice));
            double expectedPotential2 = 50.0 / (Math.pow(1 + discount, numSlice));
            //			assertTrue(potential1 == expectedPotential1);
            //			assertTrue(potential2 == expectedPotential2);
            assertTrue(Math.abs((potential1 - expectedPotential1)) < (expectedPotential1 / Math.pow(10, 9)));
            assertTrue(Math.abs((potential2 - expectedPotential2)) < (expectedPotential2 / Math.pow(10, 9)));
        }
    }
    
    @Test public void applyDiscountToUtilityNodesTest2() {
        // Set discount to the node
        for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
            utilityNode.getVariable().getDecisionCriterion().setDiscount(0.1);
        }
        
        // Set a different cycle length unit to the probNet
        probNet.getCycleLength().setUnit(Unit.DAY);
        
        // Expand the network
        this.probNet = TemporalNetOperations.expandNetwork(probNet);
        
        // Apply discount
        TemporalNetOperations.applyDiscountToUtilityNodes(probNet);
        
        for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
            double potential1 = ((ExactDistrPotential) utilityNode.getPotentials().get(0))
                    .getTablePotential().getValues()[0];
            double potential2 = ((ExactDistrPotential) utilityNode.getPotentials().get(0))
                    .getTablePotential().getValues()[1];
            int numSlice = utilityNode.getVariable().getTimeSlice();
            double discount = CycleLength.getTemporalAdjustedDiscount(probNet.getCycleLength().getUnit(),
                                                                      probNet.getCycleLength().getValue(),
                                                                      utilityNode.getVariable()
                                                                                 .getDecisionCriterion()
                                                                                 .getDiscountUnit(),
                                                                      utilityNode.getVariable()
                                                                                 .getDecisionCriterion()
                                                                                 .getDiscount());
            
            // Utility Potential U[0] = [20, 50]
            double expectedPotential1 = 20.0 / (Math.pow(1 + discount, numSlice));
            double expectedPotential2 = 50.0 / (Math.pow(1 + discount, numSlice));
            //			assertTrue(potential1 == expectedPotential1);
            //			assertTrue(potential2 == expectedPotential2);
            assertTrue(Math.abs((potential1 - expectedPotential1)) < (expectedPotential1 / Math.pow(10, 9)));
            assertTrue(Math.abs((potential2 - expectedPotential2)) < (expectedPotential2 / Math.pow(10, 9)));
        }
    }
}