/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.configuration;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.component.ValuesTableCellRenderer;
import org.openmarkov.gui.dialog.treeadd.TreeADDCellRenderer;

import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the light and the dark theme, the selection rectangle stands out from the canvas, and the name of a tab with
 * unsaved changes, the text of a cell with uncertainty and the states of a branch of a tree can be read on their
 * background. A line needs a contrast of 3 and a text one of 4.5, on a scale from 1 (the same colour) to 21 (black on
 * white).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheColoursCanBeReadInBothThemesTest {

    private static final double LINE = 3;
    private static final double TEXT = 4.5;

    private boolean ignoreStorage;
    private Theme theme;
    private LookAndFeel lookAndFeel;

    @BeforeEach void keepTheTheme() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        theme = UserPreferences.PREFERRED_THEME.get();
        lookAndFeel = UIManager.getLookAndFeel();
    }

    @AfterEach void restoreTheTheme() throws Exception {
        UserPreferences.PREFERRED_THEME.set(theme);
        UIManager.setLookAndFeel(lookAndFeel);
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @ParameterizedTest @EnumSource(names = {"LIGHT", "DARK"})
    void theSelectionRectangle(Theme theme) throws Exception {
        use(theme);
        assertAtLeast(LINE, GUIColors.Network.SELECTION_RECTANGLE.getColor(), GUIColors.Network.BACKGROUND.getColor());
    }

    @ParameterizedTest @EnumSource(names = {"LIGHT", "DARK"})
    void theNameOfAModifiedTab(Theme theme) throws Exception {
        use(theme);
        assertAtLeast(TEXT, GUIColors.General.ATTENTION_TEXT.getColor(), UIManager.getColor("TabbedPane.background"));
    }

    @ParameterizedTest @EnumSource(names = {"LIGHT", "DARK"})
    void aCellWithUncertainty(Theme theme) throws Exception {
        use(theme);
        var renderer = new ValuesTableCellRenderer(null, 0, new boolean[0]) {
            JLabel cell() {
                return getUncertaintyIcon();
            }
        };
        JLabel cell = renderer.cell();
        assertAtLeast(TEXT, cell.getForeground(), cell.getBackground());
    }

    @ParameterizedTest @EnumSource(names = {"LIGHT", "DARK"})
    void theStatesOfABranch(Theme theme) throws Exception {
        use(theme);
        Variable therapy = new Variable("Therapy", "no", "yes");
        TreeADDBranch branch = new TreeADDBranch(List.of(therapy.getStates()[1]), therapy, new ArrayList<>());
        Color background = UIManager.getColor("Tree.background");
        assertAtLeast(TEXT, strongestPixel(TreeADDCellRenderer.getBranchDescriptiontHTML(branch), background),
                      background);
    }

    private static void use(Theme theme) throws Exception {
        UserPreferences.PREFERRED_THEME.set(theme);
        UIManager.setLookAndFeel(theme == Theme.DARK ? new FlatDarculaLaf() : new FlatLightLaf());
    }

    /** The colour of the text painted by a label, which is the pixel that differs most from the background. */
    private static Color strongestPixel(String text, Color background) {
        JLabel label = new JLabel(text);
        label.setSize(label.getPreferredSize());
        BufferedImage image = new BufferedImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(background);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        label.paint(graphics);
        graphics.dispose();
        Color strongest = background;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixel = new Color(image.getRGB(x, y));
                if (contrast(pixel, background) > contrast(strongest, background)) {
                    strongest = pixel;
                }
            }
        }
        return strongest;
    }

    private static void assertAtLeast(double minimum, Color foreground, Color background) {
        double contrast = contrast(foreground, background);
        assertTrue(contrast >= minimum, foreground + " on " + background + " has a contrast of " + contrast);
    }

    private static double contrast(Color one, Color other) {
        double a = luminance(one), b = luminance(other);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    private static double luminance(Color colour) {
        return 0.2126 * channel(colour.getRed()) + 0.7152 * channel(colour.getGreen()) + 0.0722 * channel(colour.getBlue());
    }

    private static double channel(int value) {
        double v = value / 255.0;
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
}
