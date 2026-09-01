/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

import org.apache.logging.log4j.LogManager;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

/**
 * The font OpenMarkov draws its networks with. It travels with the program, so that the same network
 * is drawn the same everywhere.
 * <p>
 * It used to ask for Helvetica, which is on hardly any machine that is not a Mac. Java answers such a
 * request without complaining: it hands back whatever it decides to use instead, and what it decides
 * depends on the system. That matters more here than it would elsewhere, because the width of a node
 * is measured from the width of its name, so the font does not only decide how a network looks, it
 * decides how big its nodes are. The same file opened on two machines came out with nodes of
 * different sizes, over positions that are saved and not recomputed.
 * <p>
 * DejaVu Sans was chosen for being free to redistribute and easy to read at the sizes used here: a
 * tall x-height and letters set well apart. Its licence travels beside it, in the same directory.
 */
public final class GUIFonts {

    /** The family the bundled files register themselves under. */
    private static final String FAMILY = "DejaVu Sans";

    private static final boolean AVAILABLE = registerAll("/fonts/DejaVuSans.ttf", "/fonts/DejaVuSans-Bold.ttf");

    private GUIFonts() {
    }

    /**
     * The bundled font, in the given style and size. Falls back to the sans-serif of the system if the
     * bundled files could not be read, which keeps the program drawing rather than stopping.
     */
    public static Font of(int style, int size) {
        return new Font(AVAILABLE ? FAMILY : Font.SANS_SERIF, style, size);
    }

    private static boolean registerAll(String... resources) {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String resource : resources) {
            try (InputStream stream = GUIFonts.class.getResourceAsStream(resource)) {
                if (stream == null) {
                    LogManager.getLogger(GUIFonts.class).warn("The font {} is not in the program", resource);
                    return false;
                }
                environment.registerFont(Font.createFont(Font.TRUETYPE_FONT, stream));
            } catch (IOException | FontFormatException cannotBeRead) {
                LogManager.getLogger(GUIFonts.class).warn("The font {} cannot be read", resource, cannotBeRead);
                return false;
            }
        }
        return true;
    }
}
