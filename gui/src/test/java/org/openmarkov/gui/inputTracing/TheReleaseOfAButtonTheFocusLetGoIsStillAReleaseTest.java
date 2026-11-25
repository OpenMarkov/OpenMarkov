/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.inputTracing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.bindings.Binding;
import org.openmarkov.gui.bindings.Input;
import org.openmarkov.gui.bindings.InputCombination;

import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The click on the empty canvas that closes the menu of a node moves the focus through nobody for an instant, while
 * the button is down, and the tracker lets go of every button then, so that none stays held. Its release must still
 * count as one: «Select element» acts when the button is let go, and the click left the selection as it was, on the
 * empty canvas and on another node (error 369 of the pending list). The focus is lost here by calling the tracker as
 * its listener of the focus does, so no screen is needed.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheReleaseOfAButtonTheFocusLetGoIsStillAReleaseTest {

    private static final InputCombination LEFT_CLICK = new InputCombination(new Input.Click(MouseEvent.BUTTON1, 1));

    @BeforeEach
    void nothingIsHeld() {
        GlobalInputTracker.HELD_KEY_CODES.clear();
        GlobalInputTracker.HELD_MOUSE_BUTTONS.clear();
        GlobalInputTracker.MOUSE_CLICK_COUNTS.clear();
        GlobalInputTracker.LET_GO_BY_THE_FOCUS.clear();
        GlobalInputTracker.focusLost();
        GlobalInputTracker.LET_GO_BY_THE_FOCUS.clear();
    }

    private static boolean letGo() {
        return LEFT_CLICK.isMet(Binding.MeetCondition.ON_LOSE, Binding.Exclusivity.EXCLUSIVE, true);
    }

    private static boolean held() {
        return LEFT_CLICK.isMet(Binding.MeetCondition.HELD, Binding.Exclusivity.EXCLUSIVE, true);
    }

    @Test
    void aClickIsLetGoWhenItsButtonIsReleased() {
        GlobalInputTracker.pressed(MouseEvent.BUTTON1, 1, null);
        assertTrue(held());
        GlobalInputTracker.released(MouseEvent.BUTTON1, null);
        assertTrue(letGo());
    }

    @Test
    void aClickWhoseButtonTheFocusLetGoIsStillLetGoWhenItIsReleased() {
        GlobalInputTracker.pressed(MouseEvent.BUTTON1, 1, null);
        GlobalInputTracker.focusLost();
        assertFalse(held(), "The loss of the focus still lets go of the button, so that none stays held");
        GlobalInputTracker.released(MouseEvent.BUTTON1, null);
        assertTrue(letGo());
    }

    @Test
    void theReleaseIsCountedOnce() {
        GlobalInputTracker.pressed(MouseEvent.BUTTON1, 1, null);
        GlobalInputTracker.focusLost();
        GlobalInputTracker.released(MouseEvent.BUTTON1, null);
        GlobalInputTracker.released(MouseEvent.BUTTON1, null);
        assertFalse(letGo(), "A second release, without a press, lets nothing go");
    }

    @Test
    void aNewPressForgetsTheButtonTheFocusLetGo() {
        GlobalInputTracker.pressed(MouseEvent.BUTTON1, 1, null);
        GlobalInputTracker.focusLost();
        GlobalInputTracker.pressed(MouseEvent.BUTTON1, 1, null);
        GlobalInputTracker.released(MouseEvent.BUTTON1, null);
        assertTrue(letGo());
        GlobalInputTracker.released(MouseEvent.BUTTON1, null);
        assertFalse(letGo());
    }

    @Test
    void aButtonThatWasNotDownIsNotLetGoByTheFocus() {
        GlobalInputTracker.focusLost();
        GlobalInputTracker.released(MouseEvent.BUTTON1, null);
        assertFalse(letGo());
    }
}
