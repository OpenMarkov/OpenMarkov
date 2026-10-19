/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.edition.networkEditorPanel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.inputTracing.GlobalInputTracker;
import org.openmarkov.gui.dialog.node.ImposePolicyDialog;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.ZoomManager;

import javax.swing.SwingUtilities;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * In the network editor, in edition mode, Alt+click on a decision without an imposed policy opens the dialog of its
 * policy, as the menus do; it used to open the dialog of its relation, which failed (error 210).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class AltClickOnADecisionOpensItsPolicyTest {

    private static boolean ignoreStorage;

    private NetworkEditorPanel tab;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        // The program follows the keys from the first event it handles; alone, this test's first key would be lost
        GlobalInputTracker.getHeldKeyCodes();
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    /** An influence diagram with the decision Therapy, without a policy, at (100, 100), at a zoom of 100 %. */
    @BeforeEach void openADecision() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.setName("one decision");
        Variable therapy = new Variable("Therapy", "no", "yes");
        net.addNode(therapy, NodeType.DECISION).setCoordinateX(100);
        net.getNode(therapy).setCoordinateY(100);
        SwingUtilities.invokeAndWait(() -> {
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
            tab.setZoom(1);
        });
    }

    @AfterEach void closeTheTab() throws Exception {
        SwingUtilities.invokeAndWait(() -> MainGUI.INSTANCE.mainPanel.forceClose(tab));
    }

    private MouseEvent event(int id, long when, int modifiers, double x, double y, int button) {
        ZoomManager zoom = tab.getZoomManager();
        return new MouseEvent(tab, id, when, modifiers, (int) Math.round(zoom.panelToScreen(x)),
                              (int) Math.round(zoom.panelToScreen(y)), id == MouseEvent.MOUSE_PRESSED ? 1 : 0, false,
                              button);
    }

    /** The key goes through the focus manager, where the program follows the keys, and then to the canvas. */
    private void key(int id, long when, int keyCode, int modifiers) {
        KeyEvent key = new KeyEvent(tab, id, when, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
        tab.dispatchEvent(key);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(tab, key);
    }

    @Test void theDialogOfThePolicyOpensWithoutAnError() throws Exception {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> thrown.set(exception));
        try {
            // The dialog is modal, so the click is not waited for
            SwingUtilities.invokeLater(() -> {
                long now = System.currentTimeMillis();
                key(KeyEvent.KEY_PRESSED, now, KeyEvent.VK_ALT, InputEvent.ALT_DOWN_MASK);
                tab.dispatchEvent(event(MouseEvent.MOUSE_MOVED, ++now, InputEvent.ALT_DOWN_MASK, 110, 110,
                                        MouseEvent.NOBUTTON));
                tab.dispatchEvent(event(MouseEvent.MOUSE_PRESSED, ++now,
                                        InputEvent.ALT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK, 110, 110,
                                        MouseEvent.BUTTON1));
            });
            ImposePolicyDialog dialog = null;
            for (long end = System.currentTimeMillis() + 10_000;
                 dialog == null && thrown.get() == null && System.currentTimeMillis() < end; ) {
                Thread.sleep(20);
                for (Window window : Window.getWindows()) {
                    if (window.isShowing() && window instanceof ImposePolicyDialog shown) {
                        dialog = shown;
                    }
                }
            }
            ImposePolicyDialog shown = dialog;
            if (shown != null) {
                SwingUtilities.invokeLater(() -> shown.getCancelButton().doClick());
                for (long end = System.currentTimeMillis() + 10_000; shown.isShowing() && System.currentTimeMillis() < end; ) {
                    Thread.sleep(20);
                }
            }
            SwingUtilities.invokeAndWait(() -> {
                long now = System.currentTimeMillis();
                tab.dispatchEvent(event(MouseEvent.MOUSE_RELEASED, now, InputEvent.ALT_DOWN_MASK, 110, 110,
                                        MouseEvent.BUTTON1));
                key(KeyEvent.KEY_RELEASED, ++now, KeyEvent.VK_ALT, 0);
            });

            assertNull(thrown.get());
            assertNotNull(shown);
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous);
        }
    }
}
