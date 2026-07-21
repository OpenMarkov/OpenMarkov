package org.openmarkov.full;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;

/**
 * Developer tool plugin that provides a menu item whose action is an empty breakpoint,
 * useful for attaching a debugger and inspecting the current network state.
 */
public class BreakPointPlugin implements ToolPlugin {
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.UNCATEGORIZED;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Break-point (Developers' tool)")
                .onClick(() -> {
                    ProbNet currentProbNet = MainPanel.getCurrentProbNet();
                    boolean a = true;
                })
                .build();
    }
}
