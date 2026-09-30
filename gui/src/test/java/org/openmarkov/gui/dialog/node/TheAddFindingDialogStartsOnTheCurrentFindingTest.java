/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The add finding dialog starts on the state of the node's current finding, and on the first state
 * only when the node has no finding.
 *
 * @author Manuel Arias
 */
class TheAddFindingDialogStartsOnTheCurrentFindingTest {

    private final Variable x = new Variable("X", "s0", "s1", "s2");

    @Tag(TestSpeed.FAST)
    @Test void withAFinding() {
        assertEquals("s2", AddFindingDialog.initialState(x.getStates(), new Finding(x, x.getState("s2"))));
    }

    @Tag(TestSpeed.FAST)
    @Test void withoutAFinding() {
        assertEquals("s0", AddFindingDialog.initialState(x.getStates(), null));
    }
}
