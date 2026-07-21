/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.plugins;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.bnEvaluation.dialog.BNEvaluationDialog;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.java.initialization.Lazy;

import javax.swing.*;

/**
 * @author evillar
 */

public class BNEvaluationPlugin implements ToolPlugin {
    
    private static final Lazy<BNEvaluationDialog> BN_EVALUATION_DIALOG = Lazy.of(
            () -> new BNEvaluationDialog(MainGUI.INSTANCE.mainPanel.getMainFrame()));
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.PROCESSING;
    }
    
    @Override public int priorityInGroup() {
        return 4;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Bayesian network evaluation")
                .onClick(() -> {
                    BNEvaluationPlugin.BN_EVALUATION_DIALOG.get().reload();
                    GUIUtils.showDialog(BNEvaluationPlugin.BN_EVALUATION_DIALOG.get());
                })
                .build();
    }
    
}
