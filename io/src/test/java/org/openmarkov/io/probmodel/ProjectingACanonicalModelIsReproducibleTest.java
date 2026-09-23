/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Each reading creates new variables, with other hash codes. The projection of a canonical model
 * must not depend on them: same variable order, the declared one, and the same bits.
 *
 * @author Manuel Arias
 */
public class ProjectingACanonicalModelIsReproducibleTest {

    private static final int READINGS = 6;

    private ProbNet read() throws Exception {
        URL url = getClass().getClassLoader().getResource("test-ici-reading.pgmx");
        return new PGMXReader_0_2().read(url).probNet();
    }

    private static List<String> names(TablePotential potential) {
        return potential.getVariables().stream().map(Variable::getName).toList();
    }

    private void assertReproducible(String node, List<String> expectedOrder, String observed) throws Exception {
        double[] reference = null;
        for (int i = 0; i < READINGS; i++) {
            ProbNet net = read();
            EvidenceCase evidence = new EvidenceCase();
            if (observed != null) {
                evidence.addFinding(new Finding(net.getVariable(observed), 0));
            }
            TablePotential projected = net.getNode(node).getPotential().tableProject(evidence, null);
            assertEquals(expectedOrder, names(projected), "reading " + i);
            if (reference == null) {
                reference = projected.getValues();
            } else {
                assertArrayEquals(reference, projected.getValues(), 0.0, "reading " + i);
            }
        }
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void theTuningModelKeepsItsDeclaredOrderAndItsBits() throws Exception {
        assertReproducible("I", List.of("I", "E", "F", "G"), null);
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void theTuningModelWithAnObservedParentKeepsTheRestInOrder() throws Exception {
        assertReproducible("I", List.of("I", "E", "G"), "F");
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void theNoisyMaxKeepsItsDeclaredOrderAndItsBits() throws Exception {
        assertReproducible("H", List.of("H", "A", "B", "C"), null);
    }
}
