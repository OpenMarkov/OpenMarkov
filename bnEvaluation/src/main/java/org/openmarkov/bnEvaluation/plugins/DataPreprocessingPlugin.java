package org.openmarkov.bnEvaluation.plugins;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.bnEvaluation.dialog.CrossValidationDialog;
import org.openmarkov.bnEvaluation.dialog.DataPreprocessingDialog;
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
 * Tool plugin that adds a "Data preprocessing" entry to the Tools menu for
 * filtering, discretizing, and handling missing values in case databases.
 */
public class DataPreprocessingPlugin implements ToolPlugin {
    
    private static final Lazy<DataPreprocessingDialog> DATA_PREPROCESSING_DIALOG = Lazy.of(
            () -> new DataPreprocessingDialog(MainGUI.INSTANCE.mainPanel.getMainFrame()));
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.PROCESSING;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Data preprocessing")
                .onClick(() -> GUIUtils.showDialog(DATA_PREPROCESSING_DIALOG.get()))
                .build();
    }
}
