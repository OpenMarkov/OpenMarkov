/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openmarkov.core.model.decisiontree.EvaluationType;
import org.openmarkov.core.model.decisiontree.ProbAndCEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.costEffectiveness.CEDecisionResults;
import org.openmarkov.gui.configuration.Theme;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.dialog.costeffectiveness.CEPDialog;
import org.openmarkov.inference.algorithm.DecisionTreeExpansion;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * In the light and the dark theme, every cell of the table of cost-effectiveness intervals and of the table of
 * cost-effectiveness results has a contrast of at least 4.5 with its background, on a scale from 1 (the same colour)
 * to 21 (black on white).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialogs need a screen")
class TheCostEffectivenessTablesCanBeReadInBothThemesTest {

    private static final double TEXT = 4.5;

    private boolean ignoreStorage;
    private Theme theme;
    private LookAndFeel lookAndFeel;
    private ProbNet net;

    @BeforeEach void keepTheThemeAndReadTheNetwork() throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        theme = UserPreferences.PREFERRED_THEME.get();
        lookAndFeel = UIManager.getLookAndFeel();
        net = new PGMXReader().read(IntegrationTest.class.getResource("/networks/id/ID-CEA-test-2therapies.pgmx"))
                              .probNet();
    }

    @AfterEach void restoreTheTheme() throws Exception {
        UserPreferences.PREFERRED_THEME.set(theme);
        UIManager.setLookAndFeel(lookAndFeel);
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @ParameterizedTest @EnumSource(names = {"LIGHT", "DARK"})
    void theCostEffectivenessIntervals(Theme theme) throws Exception {
        use(theme);
        var cep = ((ProbAndCEP) new DecisionTreeExpansion(EvaluationType.CE).expandTree(net.copy(), null, 1)
                                                                               .getEvaluation()).getCep();
        assertEveryCellCanBeRead(new CEPDialog(null, cep, net));
    }

    @ParameterizedTest @EnumSource(names = {"LIGHT", "DARK"})
    void theCostEffectivenessResults(Theme theme) throws Exception {
        use(theme);
        JFrame owner = new JFrame();
        try {
            assertEveryCellCanBeRead(new CEDecisionResults(owner, net, new EvidenceCase(), net.getVariable("Therapy")));
        } finally {
            owner.dispose();
        }
    }

    private static void use(Theme theme) throws Exception {
        UserPreferences.PREFERRED_THEME.set(theme);
        UIManager.setLookAndFeel(theme == Theme.DARK ? new FlatDarculaLaf() : new FlatLightLaf());
    }

    private static void assertEveryCellCanBeRead(JDialog dialog) {
        try {
            List<JTable> tables = tables(dialog.getContentPane(), new ArrayList<>());
            assertFalse(tables.isEmpty(), "The dialog has no table");
            List<String> unreadable = new ArrayList<>();
            for (JTable table : tables) {
                for (int row = 0; row < table.getRowCount(); row++) {
                    for (int column = 0; column < table.getColumnCount(); column++) {
                        Component cell = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                        double contrast = contrast(cell.getForeground(), cell.getBackground());
                        if (contrast < TEXT) {
                            unreadable.add(table.getColumnName(column) + String.format(" (%.1f)", contrast));
                        }
                    }
                }
            }
            assertEquals(List.of(), unreadable.stream().distinct().toList());
        } finally {
            dialog.dispose();
        }
    }

    private static List<JTable> tables(Container container, List<JTable> found) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTable table) {
                found.add(table);
            } else if (component instanceof Container child) {
                tables(child, found);
            }
        }
        return found;
    }

    private static double contrast(Color first, Color second) {
        double a = luminance(first);
        double b = luminance(second);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    private static double luminance(Color colour) {
        return 0.2126 * channel(colour.getRed()) + 0.7152 * channel(colour.getGreen())
               + 0.0722 * channel(colour.getBlue());
    }

    private static double channel(int value) {
        double c = value / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
