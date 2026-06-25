package org.openmarkov.dbgenerator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.dbgenerator.gui.DBGeneratorGUI;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.*;

/**
 * Tool plugin that adds a "DB Generator" entry to the Tools menu, allowing users
 * to generate synthetic case databases from a Bayesian network.
 */
public final class DBGeneratorPlugin implements ToolPlugin {
    
    public @Nullable Character mnemonic() {
        var mnemonic = StringDatabase.getUniqueInstance().getString("Menus", "Tools.DBGenerator.Mnemonic");
        if (mnemonic == null || mnemonic.isEmpty()) return null;
        return mnemonic.charAt(0);
    }
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.EXPORT;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder(StringDatabase.getUniqueInstance().getString("Menus", "Tools.DBGenerator"))
                .onClick(() -> new DBGeneratorGUI(MainGUI.INSTANCE.mainPanel.getMainFrame()).setVisible(true))
                .build();
    }
}