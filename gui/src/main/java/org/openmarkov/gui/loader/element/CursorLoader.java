/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.element;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.java.initialization.Lazy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    public static final Lazy<Cursor> CURSOR_NODE_CHANCE = Lazy.of(() -> load("chance.png"));

    public static final Lazy<Cursor> CURSOR_NODE_DECISION = Lazy.of(() -> load("decision.png"));

    public static final Lazy<Cursor> CURSOR_NODE_UTILITY = Lazy.of(() -> load("utility.png"));

    public static final Lazy<Cursor> CURSOR_NODE_EVENT = Lazy.of(() -> load("event.png"));
    
    /**
     * Folder where cursors are saved.
     */
    private static final String CURSORS_PATH = "cursors/";

    /**
     * How many pixels a cursor spans when the interface is at its normal scale. It is the usual
     * size of a pointer on Windows, Linux and macOS alike.
     */
    private static final int DEFAULT_CURSOR_SIZE = 32;

    /**
     * A cursor smaller than this is hard to aim with, whatever the interface scale says.
     */
    private static final int MINIMUM_CURSOR_SIZE = 16;
    
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
        BufferedImage image;
        try {
            image = ImageIO.read(resource);
        } catch (IOException e) {
            image = null;
        }
        if (image == null) {
            System.err.println(StringDatabase.getUniqueInstance()
                                             .getFormattedString("CursorResourceNotExists.Text", CURSORS_PATH + cursorName));
            return null;
        }
        Dimension size = cursorSizeFor(tk, image);
        return tk.createCustomCursor(resize(image, size), centreOf(size), cursorName);
    }

    /**
     * The size to draw the cursor at. A cursor is shown at exactly the size of the image it is
     * built from, so this is what decides how big the pointer looks: it follows the interface
     * scale the user chose, the same preference the toolbar icons obey.
     * <p>
     * The files are stored at the largest size worth using, so this only ever scales down and
     * the drawing never turns into a block of pixels. The final say belongs to
     * {@link Toolkit#getBestCursorSize}, which snaps the request to a size the system supports.
     */
    private static Dimension cursorSizeFor(Toolkit toolkit, BufferedImage image) {
        double uiScale = UserPreferences.UI_SCALE.get();
        int wanted = (int) Math.round(CursorLoader.DEFAULT_CURSOR_SIZE * uiScale);
        wanted = Math.max(CursorLoader.MINIMUM_CURSOR_SIZE,
                          Math.min(Math.min(image.getWidth(), image.getHeight()), wanted));
        Dimension best = toolkit.getBestCursorSize(wanted, wanted);
        return (best.width > 0 && best.height > 0) ? best : new Dimension(wanted, wanted);
    }

    /**
     * {@link Toolkit#createCustomCursor} resizes the image itself, but with
     * {@link Image#SCALE_DEFAULT}, which leaves the edges muddy. Doing it here buys the same
     * interpolation the rest of the interface uses for its icons.
     */
    private static Image resize(BufferedImage image, Dimension size) {
        if (size.width == image.getWidth() && size.height == image.getHeight()) {
            return image;
        }
        BufferedImage resized = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(image, 0, 0, size.width, size.height, null);
        g.dispose();
        return resized;
    }

    /**
     * The point of the cursor that the click lands on. These cursors draw the node the user is
     * about to create, so the click has to land where that node will appear: the centre of the
     * drawing, not the top-left corner the images used to be anchored by. A hot spot outside the
     * cursor's bounds is rejected, hence the clamp.
     */
    private static Point centreOf(Dimension size) {
        return new Point(Math.max(0, Math.min(size.width - 1, size.width / 2)),
                         Math.max(0, Math.min(size.height - 1, size.height / 2)));
    }

}
