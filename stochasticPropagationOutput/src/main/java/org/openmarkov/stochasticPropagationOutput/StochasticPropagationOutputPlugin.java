package org.openmarkov.stochasticPropagationOutput;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;

/**
 * Tool plugin that registers the stochastic propagation output export option
 * in the Tools menu.
 */
public class StochasticPropagationOutputPlugin implements ToolPlugin {

    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.EXPORT;
    }

    @Override public int priorityInGroup() {
        return 1;
    }

    /**
     * Checks whether this plugin should be enabled based on the presence of an open network.
     *
     * @return {@code true} if a network is currently open, {@code false} otherwise
     */
    public boolean enabled() {
        return MainPanel.getCurrentProbNet() != null;
    }

    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder(
                StringDatabase.getUniqueInstance()
                              .getString("stochasticPropagationOutput", "StochasticPropagationOutput"))
                .enabled(MainPanel.getCurrentProbNet() != null)
                .onClick(() -> {
                    StochasticPropagationOutputFrame frame = new StochasticPropagationOutputFrame(MainGUI.INSTANCE.mainPanel.getMainFrame());
                    Exception error = frame.boundError();
                    if (error != null) {
                        throw error;
                    }
                    frame.setVisible(true);
                })
                .build();
    }
}
