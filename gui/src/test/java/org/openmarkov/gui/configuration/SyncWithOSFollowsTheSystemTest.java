/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.configuration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With «Sync with OS», the interface follows a change of the system also when the system changed before, while
 * another theme was chosen.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class SyncWithOSFollowsTheSystemTest {

    @Test void afterTheSystemChangedUnderAnotherTheme() {
        // The system is light at startup and the user has chosen «Light»
        AtomicBoolean lastWasDark = new AtomicBoolean(false);

        // The system turns dark: nothing to repaint under «Light»
        assertFalse(Theme.mustFollowTheSystem(lastWasDark, Theme.LIGHT, true));

        // The user chooses «Sync with OS», which asks the system directly; then the system turns light again
        assertTrue(Theme.mustFollowTheSystem(lastWasDark, Theme.SYNC_OS, false));
    }

    @Test void notWhenTheSystemSaysWhatItSaidBefore() {
        AtomicBoolean lastWasDark = new AtomicBoolean(true);

        assertFalse(Theme.mustFollowTheSystem(lastWasDark, Theme.SYNC_OS, true));
    }
}
