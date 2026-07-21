package org.openmarkov.bnEvaluation.plugins;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.bnEvaluation.dialog.CrossValidationDialog;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.java.initialization.Lazy;

import javax.swing.*;

/**
 * @author mvillar
 */
/**
 * Tool plugin that adds a "Cross-validation" entry to the Tools menu for
 * evaluating learning algorithms via cross-validation or multiple sampling.
 */
public class CrossValidationPlugin implements ToolPlugin {
    
    private static final Lazy<CrossValidationDialog> CROSS_VALIDATION_DIALOG = Lazy.of(
            () -> new CrossValidationDialog(MainGUI.INSTANCE.mainPanel.getMainFrame()));
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.PROCESSING;
    }
    
    @Override public int priorityInGroup() {
        return 4;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Cross-validation")
                .onClick(() -> GUIUtils.showDialog(CROSS_VALIDATION_DIALOG.get()))
                .build();
    }
    
}
