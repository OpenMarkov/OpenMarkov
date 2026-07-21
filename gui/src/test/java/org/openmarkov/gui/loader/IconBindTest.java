/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader;

import org.junit.jupiter.api.*;

import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.loader.element.IconBind;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.MissingResourceException;


/**
 * This class tests the class {@link IconBind}.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.1 jlgozalo add test for Infinite Positive and negative icons
 */

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class IconBindTest {
    
    /**
     * Creates a new icon loader for all tests.
     */
    @BeforeEach public void setUp() {
    }
    
    /**
     * This method tests the method 'load' when tries to load an icon.
     *
     * @throws MissingResourceException if any icon doesn't exist.
     */
    @Tag(TestSpeed.MEDIUM)
    @Test public final void testLoad() throws MissingResourceException {
        ArrayList<IconBind> missingIcons = new ArrayList<>();
        for (var iconBind : IconBind.values()) {
            try {
                iconBind.icon();
            } catch (RuntimeException e) {
                missingIcons.add(iconBind);
            }
        }
        if (missingIcons.isEmpty()) {
            return;
        }
        fail("Could not resolve some icons: " + missingIcons.stream()
                                                            .map(iconBind -> iconBind.name() + "(" + iconBind.fileName + ")")
                                                            .collect(java.util.stream.Collectors.joining(", ")));
        
    }
    
}
