package org.openmarkov.sensitivityanalysis;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.sensitivityanalysis.dialog.SensitivityAnalysisFrameGenerator;

import javax.swing.*;

/**
 * Tool plugin that adds a "Sensitivity Analysis" entry to the Tools menu,
 * enabling various sensitivity analysis operations on the current network.
 */
public final class SensitivityAnalysisPlugin implements ToolPlugin {
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.ANALYSIS;
    }
    
    @Override public int priorityInGroup() {
        return 1;
    }
    
    public boolean enabled() {
        return MainPanel.getCurrentProbNet() != null;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder(StringDatabase.getUniqueInstance().getString("Menus", "Tools.SensitivityAnalysis"))
                .enabled(MainPanel.getCurrentProbNet() != null)
                .onClick(() -> {
                    var dialog = SensitivityAnalysisFrameGenerator.create(MainGUI.INSTANCE.mainPanel.getMainFrame());
                    if (dialog != null) {
                        dialog.setVisible(true);
                    }
                })
                .build();
    }
}
