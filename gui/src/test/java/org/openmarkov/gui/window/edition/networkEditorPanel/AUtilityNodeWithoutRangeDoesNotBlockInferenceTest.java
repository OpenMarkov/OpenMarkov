/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A utility node whose range cannot be computed is left without one, and the other utility nodes
 * keep theirs, so that entering inference mode does not fail.
 *
 * @author Manuel Arias
 */
class AUtilityNodeWithoutRangeDoesNotBlockInferenceTest {

    /** "Drug cost [0]" is a tree whose value depends on a numeric variable without a finding. */
    private static final String NETWORK = "MID-dmhee-4.7.pgmx";

    @Tag(TestSpeed.MEDIUM)
    @Test void theOtherUtilityNodesKeepTheirRange() throws Exception {
        ProbNet net = new PGMXReader().read(getClass().getClassLoader().getResource(NETWORK)).probNet();
        Map<Variable, Double> min = new HashMap<>();
        Map<Variable, Double> max = new HashMap<>();

        EvidenceManager.computeUtilityRanges(net, min, max);

        Variable drugCost = net.getVariable("Drug cost [0]");
        assertNotNull(drugCost);
        assertFalse(min.containsKey(drugCost));
        assertFalse(max.containsKey(drugCost));
        for (Variable utility : net.getVariables(NodeType.UTILITY)) {
            if (utility != drugCost) {
                assertTrue(min.containsKey(utility), utility.getName());
                assertTrue(min.get(utility) <= max.get(utility), utility.getName());
            }
        }
        assertEquals(min.keySet(), max.keySet());
    }
}
