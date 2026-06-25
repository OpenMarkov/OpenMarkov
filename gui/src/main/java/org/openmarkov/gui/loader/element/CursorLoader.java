/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.element;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.java.initialization.Lazy;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;

/**
 * This class is used to load cursors from a folder.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - Handle Exceptions and Change inefficient Integer
 * conversion to String in load(number)
 */
public class CursorLoader {
    /**
     * Name of the cursor that represents the default one.
     */
    public static final Cursor CURSOR_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
    /**
     * Name of the cursor that represents the movement of nodes.
     */
    public static final Cursor CURSOR_NODES_MOVEMENT = new Cursor(Cursor.MOVE_CURSOR);
    /**
     * Name of the cursor that represents the selection of varios nodes.
     */
    public static final Cursor CURSOR_MULTIPLE_SELECTION = new Cursor(Cursor.CROSSHAIR_CURSOR);
    
    public static final Lazy<Cursor> CURSOR_LINK = Lazy.of(() -> load("link.png"));
    
    /**
     * Folder where cursors are saved.
     */
    private static final String CURSORS_PATH = "cursors/";
    
    /**
     * This method loads a cursor resource and handles the exception if not
     * exist.
     *
     * @param cursorName name of the cursor to load.
     * @return a reference to the cursor resource.
     */
    public static Cursor load(String cursorName) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        String path = CURSORS_PATH + cursorName;
        URL resource = CursorLoader.class.getClassLoader().getResource(path);
        if (resource == null) {
            return null;
        }
        Image image = tk.getImage(resource);
        if (image == null) {
            System.err.println(StringDatabase.getUniqueInstance()
                                             .getFormattedString("CursorResourceNotExists.Text", CURSORS_PATH + cursorName));
            return null;
        }
        return tk.createCustomCursor(image, new Point(0, 0), "");
    }
    
}
