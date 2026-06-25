/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.core.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.core.MulticriteriaEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MulticriteriaEditTest {
    
    private ProbNet probNet;
    
    @BeforeEach public void setUp() throws ParserException, URISyntaxException, IOException {
        this.probNet = getProbNet4Test();
        probNet.getPNESupport().setWithUndo(true);
    }
    
    @Test
    public void multiCriteriaOptionsTest() throws DoEditException {
        
        MulticriteriaOptions multicriteriaOptions = new MulticriteriaOptions();
        multicriteriaOptions.setMainUnit("Unit A");
        multicriteriaOptions.setMulticriteriaType(MulticriteriaOptions.Type.UNICRITERION);
        
        List<Criterion> decisionCriteria = new ArrayList<>();
        Criterion criterion1 = new Criterion("Criterion A");
        decisionCriteria.add(criterion1);
        probNet.setDecisionCriteria(decisionCriteria);
        MulticriteriaEdit edit = new MulticriteriaEdit(probNet, decisionCriteria, multicriteriaOptions);
        
        edit.executeEdit();
        assertTrue(probNet.getInferenceOptions().getMultiCriteriaOptions().getMainUnit().equals("Unit A"));
        assertTrue(probNet.getDecisionCriteria().equals(decisionCriteria));
        
        MulticriteriaOptions multicriteriaOptions2 = new MulticriteriaOptions();
        multicriteriaOptions2.setMainUnit("Unit B");
        multicriteriaOptions2.setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);
        List<Criterion> decisionCriteria2 = new ArrayList<>();
        Criterion criterion2 = new Criterion("Criterion B");
        decisionCriteria2.add(criterion2);
        probNet.setDecisionCriteria(decisionCriteria2);
        
        MulticriteriaEdit edit2 = new MulticriteriaEdit(probNet, decisionCriteria2, multicriteriaOptions2);
        edit2.executeEdit();
        assertTrue(probNet.getInferenceOptions().getMultiCriteriaOptions().getMainUnit().equals("Unit B"));
        assertTrue(probNet.getDecisionCriteria().equals(decisionCriteria2));
        
        
        probNet.getPNESupport().undo();
        
        assertTrue(probNet.getInferenceOptions().getMultiCriteriaOptions().getMainUnit().equals("Unit A"));
        assertTrue(!probNet.getDecisionCriteria().equals(decisionCriteria));
        
    }
    
    private ProbNet getProbNet4Test() throws ParserException, IOException {
        String bayesNetworkName = "networks/bn/BN-MulticriteriaEditTest.pgmx";
        
        // Load the Bayesian network
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        ProbNet probNet = pgmxReader.read(getClass().getClassLoader().getResource(bayesNetworkName)).probNet();
        
        return probNet;
    }
}
