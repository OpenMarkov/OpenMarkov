/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.SwingUtilities;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The window «Standard domains» opened with no domain chosen and «OK» enabled: accepting it so ended in an unexpected
 * error (error 173 of the pending list), and it had no title (error 180). Now «OK» waits for a choice, and the window
 * is named after the button that opens it.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The window needs a screen")
class StandardDomainsWaitForAChoiceTest {

    @Test void okWaitsForADomain() throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            SwingUtilities.invokeAndWait(() -> {
                StandardDomainsDialog dialog = new StandardDomainsDialog(null);
                try {
                    assertEquals("Standard domains", dialog.getTitle());
                    assertFalse(dialog.getOKButton().isEnabled());
                    assertFalse(dialog.doOkClickBeforeHide());

                    dialog.getJPanelStandardDomains().getRadioButtons().stream()
                          .filter(button -> button.getText().equals("no - yes")).findFirst().orElseThrow()
                          .setSelected(true);
                    assertTrue(dialog.getOKButton().isEnabled());
                    assertTrue(dialog.doOkClickBeforeHide());
                    assertEquals(List.of("no", "yes"), dialog.getChosenStates());
                } finally {
                    dialog.dispose();
                }
            });
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
