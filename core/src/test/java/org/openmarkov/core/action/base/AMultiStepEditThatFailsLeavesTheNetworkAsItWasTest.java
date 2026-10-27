/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.base;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.NetworkCommentEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An edit of several steps that failed with an unexpected error left the steps already done in the network, out of
 * the history and so impossible to undo; only some expected errors undid them (error 28 of the pending list).
 *
 * @author Manuel Arias
 */
class AMultiStepEditThatFailsLeavesTheNetworkAsItWasTest {

    @Test void theStepsDoneAreUndone() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.setComment("before");
        MultiStepEdit edit = new MultiStepEdit(probNet) {
            @Override protected void doMultiStepEdit(StepExecuter stepExecuter) throws org.openmarkov.core.exception.DoEditException {
                stepExecuter.execute(new NetworkCommentEdit(probNet, "after", false));
                throw new IllegalStateException("A step that fails");
            }
        };

        assertThrows(IllegalStateException.class, edit::executeEdit);

        assertEquals("before", probNet.getComment());
    }
}
