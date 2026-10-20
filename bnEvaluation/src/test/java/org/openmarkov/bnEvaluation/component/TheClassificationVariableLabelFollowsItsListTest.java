/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.component;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Component;
import java.util.Arrays;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the box «Measures», the label «Classification variable» is enabled exactly when the list next to it is: after
 * the box is enabled, and when «Predictive capacity» is ticked or unticked (error 216).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheClassificationVariableLabelFollowsItsListTest {

    @Test void theLabelIsEnabledWithItsList() {
        MeasuresPanel panel = new MeasuresPanel();
        JLabel label = (JLabel) find(panel, c -> c instanceof JLabel l && "Classification variable".equals(l.getText()));
        JComboBox<?> list = (JComboBox<?>) find(panel, c -> c instanceof JComboBox<?>);
        AbstractButton predictiveCapacity =
                (AbstractButton) find(panel, c -> c instanceof AbstractButton b && "Predictive capacity".equals(b.getText()));

        panel.setEnabled(false);
        panel.setEnabled(true);
        assertFalse(list.isEnabled());
        assertEquals(list.isEnabled(), label.isEnabled());

        predictiveCapacity.doClick();
        assertTrue(list.isEnabled());
        assertEquals(list.isEnabled(), label.isEnabled());

        predictiveCapacity.doClick();
        assertFalse(list.isEnabled());
        assertEquals(list.isEnabled(), label.isEnabled());
    }

    private static Component find(MeasuresPanel panel, Predicate<Component> wanted) {
        return Arrays.stream(panel.getComponents()).filter(wanted).findFirst().orElseThrow();
    }
}
