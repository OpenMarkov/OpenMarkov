/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.dialog.costeffectiveness.CEPDialog;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeElementPanel;
import org.openmarkov.gui.window.decisiontree.elements.DecisionTreeNodePanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * In a cost-effectiveness tree, the node of an impossible branch, such as the result of a test that is not done, has
 * no cost-effectiveness partition worth showing: its probability is zero. «Show CEP» on that node built the window of
 * the partition anyway and failed with a NullPointerException (error 322). Now it says why there is nothing to show;
 * on any other node it still shows the partition. {@code ID-decide-test}, analysed for cost-effectiveness, has such
 * nodes in the tree the window builds.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.SLOW)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ShowCEPOfAnImpossibleScenarioSaysWhyThereIsNoneTest {

    private static final StringDatabase strings = StringDatabase.getUniqueInstance();

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    private static DecisionTreeEditor editor;

    @BeforeAll static void openTheTree(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("ID-decide-test.pgmx");
        try (InputStream network = ShowCEPOfAnImpossibleScenarioSaysWhyThereIsNoneTest.class.getResourceAsStream(
                "/ID-decide-test.pgmx")) {
            Files.copy(network, file);
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainGUI.INSTANCE.mainPanel.openNetwork(file.toString());
                tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel();
                tab.getProbNet().getInferenceOptions().getMultiCriteriaOptions()
                   .setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);
                editor = new DecisionTreeEditor(tab);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @AfterAll static void closeTheNetwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> tab.close());
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @AfterEach void closeTheDialogs() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog dialog && dialog.isShowing()) {
                    dialog.dispose();
                }
            }
        });
    }

    @Test void anImpossibleScenarioSaysWhyItHasNoPartition() throws Exception {
        JDialog shown = showCEPOf(DecisionTreeNode::isImpossible);

        JOptionPane message = find(shown, JOptionPane.class);
        assertNotNull(message, "the window that showed is not a message");
        assertEquals(strings.getString("ShowCEP.ImpossibleScenario"), message.getMessage());
    }

    @Test void aPossibleScenarioShowsItsPartition() throws Exception {
        JDialog shown = showCEPOf(node -> !node.isImpossible());

        assertInstanceOf(CEPDialog.class, shown);
    }

    /**
     * Selects the first node of the tree that meets the condition and chooses «Show CEP», as the contextual menu
     * does; returns the window that shows. The menu option is pressed outside the queue of events, because what it
     * opens is modal.
     */
    private static JDialog showCEPOf(Predicate<DecisionTreeNode> condition) throws Exception {
        List<ActionListener> listeners = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            JTree tree = find(editor, JTree.class);
            DecisionTreeElementPanel root = (DecisionTreeElementPanel) tree.getModel().getRoot();
            TreePath path = pathTo(new TreePath(root), condition);
            assertNotNull(path, "no node of the tree meets the condition");
            tree.setSelectionPath(path);
            for (MouseListener listener : tree.getMouseListeners()) {
                if (listener instanceof ActionListener actionListener) {
                    listeners.add(actionListener);
                }
            }
        });
        assertEquals(1, listeners.size(), "the tree should have one listener of its menu");
        JTree tree = find(editor, JTree.class);
        SwingUtilities.invokeLater(() -> listeners.get(0).actionPerformed(
                new ActionEvent(tree, ActionEvent.ACTION_PERFORMED, ActionCommands.TREE_SHOW_CEP.getCommandName())));
        for (long end = System.currentTimeMillis() + 20_000; System.currentTimeMillis() < end; ) {
            Thread.sleep(50);
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog dialog && dialog.isShowing()) {
                    return dialog;
                }
            }
        }
        throw new AssertionError("«Show CEP» did not open any window");
    }

    private static TreePath pathTo(TreePath path, Predicate<DecisionTreeNode> condition) {
        DecisionTreeElementPanel last = (DecisionTreeElementPanel) path.getLastPathComponent();
        if (last instanceof DecisionTreeNodePanel nodePanel && condition.test(nodePanel.getTreeNode())) {
            return path;
        }
        for (DecisionTreeElementPanel child : last.getChildren()) {
            TreePath found = pathTo(path.pathByAddingChild(child), condition);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static <T> T find(Component component, Class<T> type) {
        if (type.isInstance(component)) {
            return type.cast(component);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                T found = find(child, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
