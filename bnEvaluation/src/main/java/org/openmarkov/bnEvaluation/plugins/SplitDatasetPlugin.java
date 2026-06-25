package org.openmarkov.bnEvaluation.plugins;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.bnEvaluation.dialog.DataPreprocessingDialog;
import org.openmarkov.bnEvaluation.dialog.SplitDatasetDialog;
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
 * Tool plugin that adds a "Split Dataset" entry to the Tools menu for dividing
 * a case database into training and test subsets.
 */
public class SplitDatasetPlugin implements ToolPlugin {
    
    private static final Lazy<SplitDatasetDialog> SPLIT_DATASET_DIALOG = Lazy.of(
            () -> new SplitDatasetDialog(MainGUI.INSTANCE.mainPanel.getMainFrame()));
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.PROCESSING;
    }
    
    @Override public int priorityInGroup() {
        return 1;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Split Dataset")
                .onClick(() -> GUIUtils.showDialog(SPLIT_DATASET_DIALOG.get()))
                .build();
    }
}
