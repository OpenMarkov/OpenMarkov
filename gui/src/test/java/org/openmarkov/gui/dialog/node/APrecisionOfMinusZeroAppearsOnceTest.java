/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JComboBox;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The dialog of a node fills the precision selector twice; a precision that is not in the list is added once, also
 * −0, which is the precision 0, and NaN.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APrecisionOfMinusZeroAppearsOnceTest {

    @ParameterizedTest @CsvSource({"-0.0, 0", "0.0, 0", "NaN, NaN", "0.003, 0.003"})
    void fillingTheSelectorTwiceAddsThePrecisionOnce(double precision, String written) {
        JComboBox<String> comboBox = new JComboBox<>(new String[]{"1", "0.1", "0.01"});
        NodeDomainValuesTablePanel.selectPrecision(comboBox, precision);
        NodeDomainValuesTablePanel.selectPrecision(comboBox, precision);

        assertEquals(4, comboBox.getItemCount());
        assertEquals(written, comboBox.getSelectedItem());
    }
}
