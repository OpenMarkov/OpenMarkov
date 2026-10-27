/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The edit of the comment of a network also sets whether it is shown when the network is opened, but undoing it, or
 * cancelling the window of the properties of the network, gave back only the comment: the option stayed as it was
 * left, and was saved (error 33 of the pending list).
 *
 * @author Manuel Arias
 */
class UndoingACommentEditGivesBackItsOptionTest {

    private ProbNet probNet;

    @BeforeEach void aNetworkWhoseCommentIsNotShown() {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.setComment("A comment");
        probNet.setShowCommentWhenOpening(false);
        probNet.getPNESupport().setWithUndo(true);
    }

    @Test void undoGivesBackTheOption() throws Exception {
        new NetworkCommentEdit(probNet, "A comment", true).executeEdit();
        assertTrue(probNet.getShowCommentWhenOpening());

        probNet.getPNESupport().undo();

        assertFalse(probNet.getShowCommentWhenOpening());
        assertEquals("A comment", probNet.getComment());
    }

    @Test void cancellingTheWindowGivesBackTheOption() throws Exception {
        probNet.getPNESupport().openNewSubEditHistory();
        new NetworkCommentEdit(probNet, "Another comment", true).executeEdit();

        probNet.getPNESupport().cancelLastSubEditHistory();

        assertFalse(probNet.getShowCommentWhenOpening());
        assertEquals("A comment", probNet.getComment());
    }

    @Test void redoSetsItAgain() throws Exception {
        new NetworkCommentEdit(probNet, "A comment", true).executeEdit();
        probNet.getPNESupport().undo();

        probNet.getPNESupport().redo();

        assertTrue(probNet.getShowCommentWhenOpening());
    }
}
