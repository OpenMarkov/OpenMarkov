package org.openmarkov.full;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Tool plugin that exports the current probabilistic network as a PNG image file.
 */
public class SaveProbnetImagePlugin implements ToolPlugin {
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.UNCATEGORIZED;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    /** {@return whether a network is currently open and can be exported} */
    public boolean enabled() {
        return MainPanel.getCurrentProbNet() != null;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Save network as image")
                .enabled(MainPanel.getCurrentProbNet() != null)
                .onClick(SaveProbnetImagePlugin::action)
                .build();
    }
    
    private static void action() throws IOException {
        String title = StringDatabase.getUniqueInstance().getString("SaveNetworkImage");
        SaveProbnetImagePlugin.SAVE_IMAGE_FILE_CHOOSER.setDialogTitle(title);
        if (SaveProbnetImagePlugin.SAVE_IMAGE_FILE_CHOOSER.showSaveDialog(GUIUtils.getOwner(MainGUI.INSTANCE.mainPanel)) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        var file = SaveProbnetImagePlugin.SAVE_IMAGE_FILE_CHOOSER.getSelectedFile();
        if (file == null) {
            return;
        }
        if (!file.toPath().toString().endsWith(".png")) {
            file = Paths.get(file.toPath().toString() + ".png").toFile();
        }
        SaveProbnetImagePlugin.saveProbnetAsImage(MainPanel.getCurrentProbNet(), file);
    }
    
    private static @NotNull BufferedImage imageOfProbnet(ProbNet currentProbNet) {
        NetworkEditorPanel networkPanel = new NetworkEditorPanel(currentProbNet, MainGUI.INSTANCE.mainPanel);
        networkPanel.setZoom(1.0);
        NetworkEditorPanel networkEditorPanel = networkPanel.getEditorPanel();
        networkEditorPanel.adjustPanelDimension();
        
        BufferedImage image = new BufferedImage((int) networkEditorPanel.getCurrentWidth(), (int) networkEditorPanel.getCurrentHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        networkEditorPanel.paint(g);
        return image;
    }
    
    private static void saveProbnetAsImage(ProbNet currentProbNet, File file) throws IOException {
        ImageIO.write(SaveProbnetImagePlugin.imageOfProbnet(currentProbNet), "png", file);
    }
    
    
    private static final OMFileChooser SAVE_IMAGE_FILE_CHOOSER = new OMFileChooser();
    
    static {
        SaveProbnetImagePlugin.SAVE_IMAGE_FILE_CHOOSER.setFileFilter(new FileFilter() {
            
            @Override public boolean accept(File f) {
                return f.isDirectory() || "png".equals(org.apache.commons.io.FilenameUtils.getExtension(f.getName()));
            }
            
            @Override public String getDescription() {
                return "Portable Network Graphics (.png)";
            }
        });
    }
}
