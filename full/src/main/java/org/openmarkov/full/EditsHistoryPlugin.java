package org.openmarkov.full;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;
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
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Edits history")
                .onClick(EditsHistoryPlugin::action)
                .build();
    }
    
    private static void action() {
        JDialog editsDialog = new JDialog(MainGUI.INSTANCE.mainPanel.getMainFrame());
        editsDialog.setVisible(true);
        editsDialog.setModalityType(Dialog.ModalityType.MODELESS);
        editsDialog.setTitle("Visualization of edits");
        
        JPanel editsPanel = new JPanel();
        editsPanel.setLayout(new BoxLayout(editsPanel, BoxLayout.Y_AXIS));
        var visualPanel = new JPanel();
        visualPanel.setLayout(new BorderLayout());
        visualPanel.add(new JScrollPane(editsPanel), BorderLayout.CENTER);
        editsDialog.setContentPane(visualPanel);
        
        final ArrayList<EditAndDone> edits = new ArrayList<>();
        new Thread(() -> {
            while (true) {
                
                ProbNet currentProbNet = MainPanel.getCurrentProbNet();
                try {
                    List<EditAndDone> newEdits;
                    try {
                        List<PNEdit> undoneEdits = currentProbNet.getPNESupport()
                                                                 .getCurrentEditHistory()
                                                                 .getUndoneEdits();
                        List<PNEdit> doneEdits = currentProbNet.getPNESupport().getCurrentEditHistory().getDoneEdits();
                        newEdits = Stream.concat(
                                doneEdits.stream().map(edit -> new EditAndDone(edit, true)),
                                undoneEdits.stream().map(edit -> new EditAndDone(edit, false))
                        ).toList();
                    } catch (RuntimeException e) {
                        newEdits = Collections.emptyList();
                    }
                    if (!newEdits.equals(edits)) {
                        edits.clear();
                        edits.addAll(newEdits);
                        editsPanel.removeAll();
                        var editsButtons = edits.stream()
                                                .map(editAndDone -> new JButton(editAndDone.edit.getClass() + ": " + editAndDone.edit.localize()))
                                                .toList();
                        for (int buttonIndex = 0; buttonIndex < editsButtons.size(); ++buttonIndex) {
                            int finalButtonIndex = buttonIndex;
                            var editAndDone = edits.get(buttonIndex);
                            var editButton = editsButtons.get(buttonIndex);
                            var baseBackgroundColor = editButton.getBackground();
                            if (editAndDone.done) {
                                editButton.setIcon(IconBind.UNDO_ENABLED.icon());
                                editButton.addActionListener(e -> {
                                    while (!currentProbNet.getPNESupport().undo().contains(editAndDone.edit)) {
                                    
                                    }
                                });
                                editButton.addMouseListener(new MouseAdapter() {
                                    @Override public void mouseEntered(MouseEvent e) {
                                        super.mouseEntered(e);
                                        IntStream.range(finalButtonIndex, editsButtons.size())
                                                 .filter(index -> edits.get(index).done)
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
                                editButton.addActionListener(e -> {
                                    while (!currentProbNet.getPNESupport().redo().contains(editAndDone.edit)) {
                                    
                                    }
                                });
                                editButton.addMouseListener(new MouseAdapter() {
                                    @Override public void mouseEntered(MouseEvent e) {
                                        super.mouseEntered(e);
                                        IntStream.range(0, finalButtonIndex + 1)
                                                 .filter(index -> !edits.get(index).done)
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
                        
                        
                        editsButtons.forEach(editsPanel::add);
                        editsPanel.revalidate();
                        editsPanel.repaint();
                        editsDialog.pack();
                    }
                } catch (RuntimeException e) {
                    System.err.println(e);
                }
            }
        }).start();
    }
    
    /** Associates an edit with its done/undone status. */
    record EditAndDone(PNEdit edit, boolean done) {
    }
    
}
