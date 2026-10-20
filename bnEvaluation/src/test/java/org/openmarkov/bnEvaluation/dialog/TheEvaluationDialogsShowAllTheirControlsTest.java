/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.dialog;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.Theme;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The windows «Data preprocessing» and «Cross validation» show every control whole: none falls outside the window or
 * is cut by its border (error 215).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialogs need a screen")
class TheEvaluationDialogsShowAllTheirControlsTest {

    private static boolean ignoreStorage;
    private static LookAndFeel lookAndFeel;

    /**
     * Building the dialogs must not read or write the preferences of whoever runs the tests, and they are laid out
     * with the look of the application, whose letters are larger than Java's default ones.
     */
    @BeforeAll static void useTheLookOfTheApplication() throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        lookAndFeel = UIManager.getLookAndFeel();
        Theme.LIGHT.setlookAndFeel();
    }

    @AfterAll static void restoreTheLookAndThePreferences() throws Exception {
        UIManager.setLookAndFeel(lookAndFeel);
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void dataPreprocessingShowsAllItsControls() throws Exception {
        assertEquals(List.of(), controlsCutIn(DataPreprocessingDialog::new));
    }

    @Test void crossValidationShowsAllItsControls() throws Exception {
        assertEquals(List.of(), controlsCutIn(CrossValidationDialog::new));
    }

    /** The controls of the dialog, laid out as it opens, that are not seen whole. */
    private static List<String> controlsCutIn(Function<JFrame, JDialog> dialogOf) throws Exception {
        List<String> cut = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            JFrame owner = new JFrame();
            JDialog dialog = dialogOf.apply(owner);
            dialog.validate();
            collectCut(dialog.getContentPane(), dialog.getContentPane(), cut);
            dialog.dispose();
            owner.dispose();
        });
        return cut;
    }

    private static void collectCut(Component component, Container contentPane, List<String> cut) {
        if (!component.isVisible()) return;
        if (!(component instanceof JPanel) && visibleSize(component, contentPane) < area(component)) {
            cut.add(component.getClass().getSimpleName() + " " + textOf(component));
        }
        if (component instanceof JScrollPane || component instanceof JComboBox || component instanceof JSpinner) return;
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) collectCut(child, contentPane, cut);
        }
    }

    /** The area of the component that its containers, up to the content pane, leave in sight. */
    private static long visibleSize(Component component, Container contentPane) {
        Rectangle visible = new Rectangle(0, 0, component.getWidth(), component.getHeight());
        for (Component inner = component; inner != contentPane; inner = inner.getParent()) {
            Container parent = inner.getParent();
            visible.translate(inner.getX(), inner.getY());
            visible = visible.intersection(new Rectangle(0, 0, parent.getWidth(), parent.getHeight()));
        }
        return visible.isEmpty() ? 0 : (long) visible.width * visible.height;
    }

    private static long area(Component component) {
        return (long) component.getWidth() * component.getHeight();
    }

    private static String textOf(Component component) {
        if (component instanceof AbstractButton button) return "«" + button.getText() + "»";
        if (component instanceof JLabel label) return "«" + label.getText() + "»";
        if (component instanceof JComboBox<?> comboBox && comboBox.getItemCount() > 0) {
            return "showing «" + comboBox.getItemAt(0) + "»";
        }
        return "";
    }
}
