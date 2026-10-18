/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.menutoolbar.toolbar;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JComboBox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The zoom box and the expansion threshold box take the size their text needs, whatever the size of the
 * letters, and the toolbar does not stretch them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheBoxesOfTheToolbarsAreAsHighAsTheirTextTest {

    @Test
    void theZoomBoxFitsItsText() {
        assertFitsLargeText(new ZoomComboBox(_ -> {
        }));
    }

    @Test
    void theExpansionThresholdBoxFitsItsText() {
        assertFitsLargeText(new ExpansionThresholdComboBox(_ -> {
        }));
    }

    private static void assertFitsLargeText(JComboBox<?> box) {
        box.setFont(box.getFont().deriveFont(40f));
        int textHeight = box.getFontMetrics(box.getFont()).getHeight();
        assertTrue(box.getPreferredSize().height >= textHeight,
                   box.getPreferredSize().height + " px for text " + textHeight + " px high");
        assertEquals(box.getPreferredSize(), box.getMaximumSize());
        assertEquals(box.getPreferredSize(), box.getMinimumSize());
    }
}
