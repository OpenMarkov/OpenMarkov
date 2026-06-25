package org.openmarkov.learning.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.*;

/**
 * Tool plugin that adds a "Learning" entry to the Tools menu, launching
 * the multi-tab learning dialog for structure and parameter learning.
 */
public final class LearningPlugin implements ToolPlugin {
    
    public @Nullable Character mnemonic() {
        var mnemonic = StringDatabase.getUniqueInstance().getString("Menus", "Tools.Learning.Mnemonic");
        if (mnemonic == null || mnemonic.isEmpty()) return null;
        return mnemonic.charAt(0);
    }
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.PROCESSING;
    }
    
    @Override public int priorityInGroup() {
        return 2;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder(StringDatabase.getUniqueInstance().getString("Menus", "Tools.Learning"))
                .onClick(() -> new LearningDialog(MainGUI.INSTANCE).setVisible(true))
                .build();
    }
}
