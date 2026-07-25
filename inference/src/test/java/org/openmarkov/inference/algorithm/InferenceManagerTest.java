/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.inference.annotation.InferenceManager;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LikelihoodWeighting;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VariableElimination;

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
        probNet = new ProbNet();     // a Bayesian network
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
        assertTrue(algorithmNames.contains("VariableElimination"),
                "VariableElimination should be registered, and it accepts Bayesian networks");
    }

    @Tag(TestSpeed.FAST)
    @Test
    public void testGetInferenceAlgorithmByName() throws Exception {
        InferenceAlgorithm lw = inferenceManager.getInferenceAlgorithmByName(
                "LikelihoodWeighting", probNet);
        assertNotNull(lw, "Should be able to instantiate LikelihoodWeighting by name");
        assertInstanceOf(LikelihoodWeighting.class, lw);
    }

    /**
     * An unregistered name is a question with an answer — "there is no such algorithm" — not a
     * breakdown. It used to reach {@code null.getConstructor(...)} and raise a bare
     * {@code NullPointerException}.
     */
    @Tag(TestSpeed.FAST)
    @Test
    public void anUnregisteredNameGivesNullAndNotAnException() throws Exception {
        assertNull(inferenceManager.getInferenceAlgorithmByName("NoSuchAlgorithm", probNet));
    }

    /**
     * Every name that {@code getInferenceAlgorithmNames} reports must be one that
     * {@code getInferenceAlgorithms} can actually build: both answer the same question.
     */
    @Tag(TestSpeed.FAST)
    @Test
    public void theAlgorithmsAndTheirNamesAgree() {
        List<String> names = inferenceManager.getInferenceAlgorithmNames(probNet);
        List<InferenceAlgorithm> algorithms = inferenceManager.getInferenceAlgorithms(probNet);

        assertEquals(names.size(), algorithms.size());
        assertFalse(algorithms.isEmpty());
    }

    /**
     * The default for a Bayesian network is variable elimination, which is exact: the same
     * algorithm the graphical interface already uses to propagate evidence. This used to fail
     * with a {@code NullPointerException}, because the manager asked for an algorithm named
     * "VariableElimination" that nothing had registered.
     */
    @Tag(TestSpeed.FAST)
    @Test
    public void testGetDefaultInferenceAlgorithm() {
        InferenceAlgorithm algorithm = inferenceManager.getDefaultInferenceAlgorithm(probNet);

        assertNotNull(algorithm);
        assertInstanceOf(VariableElimination.class, algorithm,
                "Variable elimination is exact, so it is preferred over the sampling algorithms");
    }

    @Tag(TestSpeed.FAST)
    @Test
    public void testGetDefaultApproximateAlgorithm() {
        InferenceAlgorithm algorithm = inferenceManager.getDefaultApproximateAlgorithm(probNet);

        assertNotNull(algorithm);
        assertInstanceOf(LikelihoodWeighting.class, algorithm);
    }
}
