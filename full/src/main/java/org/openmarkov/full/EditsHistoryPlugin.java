package org.openmarkov.full;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Developer tool plugin that displays a modeless dialog showing the undo/redo edit history
 * of the current network. Clicking an edit undoes or redoes up to that point.
 */
public class EditsHistoryPlugin implements ToolPlugin {

    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.UNCATEGORIZED;
    }

    @Override public int priorityInGroup() {
        return 0;
    }

    @Override public boolean isDeveloperTool() {
        return true;
    }

    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Edits history (Developers' tool)")
                .onClick(EditsHistoryPlugin::action)
                .build();
    }

    private static void action() {
        JDialog editsDialog = new JDialog(MainGUI.INSTANCE.mainPanel.getMainFrame());
        editsDialog.setModalityType(Dialog.ModalityType.MODELESS);
        editsDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        editsDialog.setTitle("Visualization of edits");

        JPanel editsPanel = new JPanel();
        editsPanel.setLayout(new BoxLayout(editsPanel, BoxLayout.Y_AXIS));
        var visualPanel = new JPanel();
        visualPanel.setLayout(new BorderLayout());
        visualPanel.add(new JScrollPane(editsPanel), BorderLayout.CENTER);
        editsDialog.setContentPane(visualPanel);

        new EditsView(editsDialog, editsPanel).show();
    }

    /**
     * Undoes {@code edit} and the edits done after it, if the current history of {@code support} still has it done;
     * otherwise nothing.
     *
     * @return the number of edits undone
     */
    static int undoUpTo(PNESupport support, PNEdit edit) {
        List<PNEdit> done = support.getCurrentEditHistory().getDoneEdits();
        int position = positionOf(edit, done);
        if (position < 0) {
            return 0;
        }
        int steps = done.size() - position;
        for (int step = 0; step < steps; step++) {
            support.undo();
        }
        return steps;
    }

    /**
     * Redoes {@code edit} and the edits undone before it, if the current history of {@code support} still has it
     * undone; otherwise nothing.
     *
     * @return the number of edits redone
     */
    static int redoUpTo(PNESupport support, PNEdit edit) {
        // The edit to redo next comes first
        int steps = positionOf(edit, support.getCurrentEditHistory().getUndoneEdits()) + 1;
        for (int step = 0; step < steps; step++) {
            support.redo();
        }
        return steps;
    }

    /** The position of this very edit in {@code edits}, or -1. */
    private static int positionOf(PNEdit edit, List<PNEdit> edits) {
        for (int position = 0; position < edits.size(); position++) {
            if (edits.get(position) == edit) {
                return position;
            }
        }
        return -1;
    }

    /** Associates an edit with its done/undone status. */
    record EditAndDone(PNEdit edit, boolean done) {
    }

    /**
     * The buttons of the dialog, one for each edit of the history of the network in the active tab. They are built
     * again, in the thread of the interface, when that history changes or another tab is selected, and nothing is
     * watched once the dialog is closed. A thread of its own used to read the history in a loop without pause, and
     * kept a processor busy until the program ended (error 172 of the pending list).
     */
    private static final class EditsView implements PNEditListener {

        private final JDialog dialog;
        private final JPanel editsPanel;
        private final List<EditAndDone> edits = new ArrayList<>();
        private final ChangeListener onTabChange = e -> follow(MainPanel.getCurrentProbNet());
        private @Nullable ProbNet watched;

        private EditsView(JDialog dialog, JPanel editsPanel) {
            this.dialog = dialog;
            this.editsPanel = editsPanel;
        }

        private void show() {
            JTabbedPane tabs = MainGUI.INSTANCE.mainPanel.getTabPanels().getjTabbedPane();
            tabs.addChangeListener(this.onTabChange);
            this.dialog.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    tabs.removeChangeListener(EditsView.this.onTabChange);
                    follow(null);
                }
            });
            follow(MainPanel.getCurrentProbNet());
            this.dialog.pack();
            this.dialog.setVisible(true);
        }

        private void follow(@Nullable ProbNet probNet) {
            if (this.watched != null) {
                this.watched.getPNESupport().removeListener(this);
            }
            this.watched = probNet;
            if (this.watched != null) {
                this.watched.getPNESupport().addListener(this);
            }
            refresh();
        }

        // An edit can be done outside the thread of the interface
        @Override public void afterEditExecutes(PNEdit edit) {
            SwingUtilities.invokeLater(this::refresh);
        }

        @Override public void afterUndoingEdit(PNEdit edit) {
            SwingUtilities.invokeLater(this::refresh);
        }

        @Override public void afterRedoingEdit(PNEdit edit) {
            SwingUtilities.invokeLater(this::refresh);
        }

        private void refresh() {
            ProbNet probNet = this.watched;
            List<EditAndDone> newEdits = probNet == null ? Collections.emptyList() : Stream.concat(
                    probNet.getPNESupport().getCurrentEditHistory().getDoneEdits().stream()
                           .map(edit -> new EditAndDone(edit, true)),
                    probNet.getPNESupport().getCurrentEditHistory().getUndoneEdits().stream()
                           .map(edit -> new EditAndDone(edit, false))
            ).toList();
            if (newEdits.equals(this.edits)) {
                return;
            }
            this.edits.clear();
            this.edits.addAll(newEdits);
            this.editsPanel.removeAll();
            var editsButtons = this.edits.stream()
                                         .map(editAndDone -> new JButton(editAndDone.edit.getClass() + ": " + editAndDone.edit.localize()))
                                         .toList();
            for (int buttonIndex = 0; buttonIndex < editsButtons.size(); ++buttonIndex) {
                int finalButtonIndex = buttonIndex;
                var editAndDone = this.edits.get(buttonIndex);
                var editButton = editsButtons.get(buttonIndex);
                var baseBackgroundColor = editButton.getBackground();
                if (editAndDone.done) {
                    editButton.setIcon(IconBind.UNDO_ENABLED.icon());
                    editButton.addActionListener(e -> undoUpTo(probNet.getPNESupport(), editAndDone.edit));
                    editButton.addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) {
                            super.mouseEntered(e);
                            IntStream.range(finalButtonIndex, editsButtons.size())
                                     .filter(index -> EditsView.this.edits.get(index).done)
                                     .mapToObj(editsButtons::get)
                                     .forEach(button -> button.setBackground(GUIColors.DevelopmentTools.EditHistory.EDIT_TO_UNDO_BACKGROUND.getColor()));
                        }

                        @Override public void mouseExited(MouseEvent e) {
                            super.mouseExited(e);
                            editsButtons.forEach(button -> button.setBackground(baseBackgroundColor));
                        }
                    });

                } else {
                    editButton.setIcon(IconBind.REDO_ENABLED.icon());
                    editButton.addActionListener(e -> redoUpTo(probNet.getPNESupport(), editAndDone.edit));
                    editButton.addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) {
                            super.mouseEntered(e);
                            IntStream.range(0, finalButtonIndex + 1)
                                     .filter(index -> !EditsView.this.edits.get(index).done)
                                     .mapToObj(editsButtons::get)
                                     .forEach(button -> button.setBackground(GUIColors.DevelopmentTools.EditHistory.EDIT_TO_REDO_BACKGROUND.getColor()));
                        }

                        @Override public void mouseExited(MouseEvent e) {
                            super.mouseExited(e);
                            editsButtons.forEach(button -> button.setBackground(baseBackgroundColor));
                        }
                    });
                }
            }

            editsButtons.forEach(this.editsPanel::add);
            this.editsPanel.revalidate();
            this.editsPanel.repaint();
            this.dialog.pack();
        }
    }

}
