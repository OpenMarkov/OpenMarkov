/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The first time OpenMarkov starts, the user has to accept its disclaimer, in a window whose only button is
 * «Accept»; closing it is refusing it, and it is asked again next time. The About window shows it too.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The disclaimer is shown in a window")
class TheDisclaimerIsAskedUntilItIsAcceptedTest {

    private static boolean ignoreStorage;
    private static boolean accepted;

    /** The acceptance is kept only in memory, not in the preferences of whoever runs the tests. */
    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        accepted = UserPreferences.DISCLAIMER_ACCEPTED.get();
    }

    @AfterAll static void restoreThePreferences() {
        UserPreferences.DISCLAIMER_ACCEPTED.set(accepted);
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @BeforeEach void notAcceptedYet() {
        UserPreferences.DISCLAIMER_ACCEPTED.set(false);
    }

    @AfterEach void closeTheWindows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog dialog && dialog.isShowing()) {
                    dialog.dispose();
                }
            }
        });
    }

    @Test void acceptingItIsRemembered() throws Exception {
        CompletableFuture<Boolean> answer = ask();
        JDialog asked = shownDialog();
        JOptionPane pane = find(asked, JOptionPane.class);
        assertNotNull(pane, "the window that showed is not a message");
        String shownText = assertInstanceOf(JLabel.class, pane.getMessage()).getText();
        assertTrue(shownText.contains(Disclaimer.message()), shownText);
        assertArrayEquals(new Object[] { "Accept" }, pane.getOptions());

        SwingUtilities.invokeAndWait(() -> button(asked, "Accept").doClick());

        assertTrue(answer.get(10, SECONDS));
        assertTrue(UserPreferences.DISCLAIMER_ACCEPTED.get());
        assertTrue(ask().get(10, SECONDS), "once accepted, it is not asked again");
    }

    @Test void closingTheWindowIsRefusingIt() throws Exception {
        CompletableFuture<Boolean> answer = ask();
        JDialog asked = shownDialog();

        SwingUtilities.invokeAndWait(() -> asked.dispatchEvent(new WindowEvent(asked, WindowEvent.WINDOW_CLOSING)));

        assertFalse(answer.get(10, SECONDS));
        assertFalse(UserPreferences.DISCLAIMER_ACCEPTED.get());
    }

    @Test void theAboutWindowShowsIt() throws Exception {
        SwingUtilities.invokeLater(() -> new AboutBox());
        JDialog about = shownDialog();
        assertInstanceOf(AboutBox.class, about);

        List<String> texts = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> collectLabels(about, texts));
        assertTrue(texts.stream().anyMatch(text -> text.contains(Disclaimer.message())), texts.toString());
    }

    /** Asks for the disclaimer as OpenMarkov does when it starts, out of the queue of events because it is modal. */
    private static CompletableFuture<Boolean> ask() {
        CompletableFuture<Boolean> answer = new CompletableFuture<>();
        SwingUtilities.invokeLater(() -> answer.complete(Disclaimer.isAcceptedBy(null)));
        return answer;
    }

    private static JDialog shownDialog() throws InterruptedException {
        for (long end = System.currentTimeMillis() + 20_000; System.currentTimeMillis() < end; ) {
            Thread.sleep(50);
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog dialog && dialog.isShowing()) {
                    return dialog;
                }
            }
        }
        throw new AssertionError("no window showed");
    }

    private static <T extends Component> T find(Container container, Class<T> type) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container inner && find(inner, type) != null) {
                return find(inner, type);
            }
        }
        return null;
    }

    private static JButton button(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container inner && button(inner, text) != null) {
                return button(inner, text);
            }
        }
        return null;
    }

    private static void collectLabels(Container container, List<String> texts) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label && label.getText() != null) {
                texts.add(label.getText());
            }
            if (component instanceof Container inner) {
                collectLabels(inner, texts);
            }
        }
    }
}
