/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.loader.element.CursorLoader;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * This class tests the CursorLoader class.
 *
 * @author jlgozalo
 * @version 1.0
 */
public class CursorLoaderTest {
    
    /**
     * test to verify that exception is captured when the cursor GIF file is not
     * present.
     */
    //TODO Fix compilation error when migrating to JUnit 5
    //@Test
    //public void testNonExistantCursorForCursorNameProvidedClassicStyle() {
    
    //Cursor cursor = CursorLoader.TEST_CURSOR_FOR_NON_EXISTANT;
    //assertEquals("The cursor will be null", cursor, null);
    //}
    
    /**
     * test to verify OK
     */
    @Tag(TestSpeed.MEDIUM)
    @Test public void testLoadOK() {
        Cursor cursor = CursorLoader.CURSOR_DEFAULT;
        Cursor newCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        assertEquals(cursor.getName(), newCursor.getName(), "The cursor Names must be the same ");
    }
    
    /**
     * This method tests if all the cursor hanged by this class are not null.
     */
    @Test public void testAllCursors() {
        
        assertNotNull(CursorLoader.CURSOR_DEFAULT);
        assertNotNull(CursorLoader.CURSOR_NODES_MOVEMENT);
        assertNotNull(CursorLoader.CURSOR_MULTIPLE_SELECTION);
    }
}
