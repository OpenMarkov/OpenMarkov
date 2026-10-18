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

import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The networks are drawn with the font that travels with the program, not with one of the system.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheNetworksAreDrawnWithTheBundledFontTest {

    @Test
    void theFontIsIbmPlexSans() {
        assertEquals("IBM Plex Sans", GUIFonts.of(Font.PLAIN, 16).getFamily());
    }
}
