/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A file whose list of decision criteria is empty is read as one that has no list: the network gets the
 * default criterion, and it can be evaluated.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
public class ADecisionNetworkReadWithNoCriteriaGetsTheDefaultOneTest {

    private ProbNet readWithAnEmptyListOfCriteria(String path) throws Exception {
        String text;
        try (InputStream in = getClass().getResourceAsStream(path)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String withoutCriteria = text.replaceAll("(?s)<DecisionCriteria>.*?</DecisionCriteria>",
                                                 "<DecisionCriteria></DecisionCriteria>");
        assertNotEquals(text, withoutCriteria);
        Path file = Files.createTempFile("no-criteria", ".pgmx");
        try {
            Files.writeString(file, withoutCriteria);
            return new PGMXReader().read(file.toUri().toURL()).probNet();
        } finally {
            Files.delete(file);
        }
    }

    @Test
    void theNetworkHasACriterionAndIsEvaluated() throws Exception {
        ProbNet net = readWithAnEmptyListOfCriteria("/networks/dan/DAN-dating.pgmx");

        assertEquals(1, net.getDecisionCriteria().size());
        ProbNet original = new PGMXReader().read(getClass().getResource("/networks/dan/DAN-dating.pgmx")).probNet();
        assertEquals(new DANDecompositionIntoSymmetricDANsEvaluation(original).getUtility().getValues()[0],
                     new DANDecompositionIntoSymmetricDANsEvaluation(net).getUtility().getValues()[0], 1E-6);
    }
}
