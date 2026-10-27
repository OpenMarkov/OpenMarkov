/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.full;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A button of «Edits history» undoes or redoes up to its edit. It used to repeat «undo» until its edit came back, so
 * a button older than the history, whose edit was no longer there, undid everything and then kept undoing nothing,
 * and the program hung (error 172 of the pending list). Now it takes the steps that are needed, and none if its edit
 * is not in the history.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheEditsHistoryTakesTheStepsThatAreNeededTest {

    private ProbNet probNet;
    private PNESupport support;
    private PNEdit addA;
    private PNEdit addB;
    private PNEdit addC;

    private PNEdit added(String name) throws Exception {
        AddNodeEdit edit = new AddNodeEdit(probNet, new Variable(name, "yes", "no"), NodeType.CHANCE, null);
        edit.executeEdit();
        return edit;
    }

    @BeforeEach void threeNodesAdded() throws Exception {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        support = probNet.getPNESupport();
        support.setWithUndo(true);
        addA = added("A");
        addB = added("B");
        addC = added("C");
    }

    @Test void undoingUpToTheSecondEditTakesTwoSteps() {
        assertEquals(2, EditsHistoryPlugin.undoUpTo(support, addB));
        assertEquals(List.of(addA), support.getCurrentEditHistory().getDoneEdits());
        assertEquals(List.of(addB, addC), support.getCurrentEditHistory().getUndoneEdits());
    }

    @Test void redoingUpToTheLastEditTakesTwoSteps() {
        EditsHistoryPlugin.undoUpTo(support, addB);

        assertEquals(2, EditsHistoryPlugin.redoUpTo(support, addC));
        assertEquals(List.of(addA, addB, addC), support.getCurrentEditHistory().getDoneEdits());
    }

    @Test void anEditThatIsNoLongerThereChangesNothing() throws Exception {
        EditsHistoryPlugin.undoUpTo(support, addC);
        PNEdit addD = added("D");

        // The new edit has replaced the one undone, which a button on screen may still show
        assertEquals(0, EditsHistoryPlugin.redoUpTo(support, addC));
        assertEquals(0, EditsHistoryPlugin.undoUpTo(support, addC));
        assertEquals(List.of(addA, addB, addD), support.getCurrentEditHistory().getDoneEdits());
    }
}
