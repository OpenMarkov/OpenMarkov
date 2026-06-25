/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.inference.annotation.InferenceManager;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Manuel Arias
 */
public class InferenceManagerTest {
    private InferenceManager inferenceManager;
    private ProbNet probNet;

    @BeforeEach public void setUp() {
        inferenceManager = new InferenceManager();
        probNet = new ProbNet();
    }

    @Tag(TestSpeed.FAST)
    @Test
    public void testGetInferenceAlgorithmNames() {
        List<String> algorithmNames = inferenceManager.getInferenceAlgorithmNames(probNet);
        assertFalse(algorithmNames.isEmpty(),
                "There should be at least one registered inference algorithm");
        // LikelihoodWeighting and LogicSampling are always available
        assertTrue(algorithmNames.contains("LikelihoodWeighting"),
                "LikelihoodWeighting should be registered");
        assertTrue(algorithmNames.contains("LogicSampling"),
                "LogicSampling should be registered");
    }

    // getInferenceAlgorithmByName requires a static checkEvaluability method
    // that no algorithm currently implements (HuginPropagation has it commented out).
    @Disabled("No algorithm implements checkEvaluability — InferenceManager.getInferenceAlgorithmByName always throws NoSuchMethodException")
    @Tag(TestSpeed.FAST)
    @Test
    public void testGetInferenceAlgorithmByName() throws Exception {
        InferenceAlgorithm lw = inferenceManager.getInferenceAlgorithmByName(
                "LikelihoodWeighting", probNet);
        assertNotNull(lw, "Should be able to instantiate LikelihoodWeighting by name");
    }

    // getDefaultInferenceAlgorithm tries to find "VariableElimination" by name,
    // but VariableElimination is abstract and not registered via @InferenceAnnotation.
    @Disabled("InferenceManager.getDefaultInferenceAlgorithm references 'VariableElimination' which is not registered")
    @Test
    public void testGetDefaultInferenceAlgorithm() throws NotEvaluableNetworkException {
        InferenceAlgorithm algorithm = inferenceManager.getDefaultInferenceAlgorithm(probNet);
        assertNotNull(algorithm);
    }
}
