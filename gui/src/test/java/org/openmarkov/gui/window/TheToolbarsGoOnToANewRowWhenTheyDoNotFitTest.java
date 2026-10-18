/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Two toolbars 300 pixels wide and 40 high, with gaps of 5 pixels: the strip that holds them is as high as
 * the rows they need at its width, so the second one is not left out of sight.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheToolbarsGoOnToANewRowWhenTheyDoNotFitTest {

    private JPanel strip;

    @BeforeEach
    void setUp() {
        strip = new JPanel(new WrappingFlowLayout(FlowLayout.LEFT));
        for (int i = 0; i < 2; i++) {
            JPanel toolbar = new JPanel();
            toolbar.setPreferredSize(new Dimension(300, 40));
            strip.add(toolbar);
        }
    }

    @Test
    void inANarrowStripTheyTakeTwoRows() {
        strip.setSize(500, 50);
        assertEquals(5 + 40 + 5 + 40 + 5, strip.getPreferredSize().height);
    }

    @Test
    void inAWideStripTheyShareOneRow() {
        strip.setSize(800, 50);
        assertEquals(5 + 40 + 5, strip.getPreferredSize().height);
    }
}
