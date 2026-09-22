package org.openmarkov.gui.toolplugin;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.dialog.io.FileFilterByExtension;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SaveLogToolPlugin implements ToolPlugin {
    
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Save log file")
                .onClick(() -> SaveLogToolPlugin.requestSaveLog(MainGUI.INSTANCE, SaveLogToolPlugin.generateDefaultLogFileName()))
                .build();
    }
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.UNCATEGORIZED;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    public static final OMFileChooser LOG_SAVE_FILE_CHOOSER = new OMFileChooser();
    
    static {
        SaveLogToolPlugin.LOG_SAVE_FILE_CHOOSER.setCurrentDirectory(UserPreferences.LATEST_LOGS_DIRECTORY.get());
    }
    
    public static boolean requestSaveLog(Component parent, String defaultLogFileName) throws IOException {
        File defaultFile = switch (SaveLogToolPlugin.LOG_SAVE_FILE_CHOOSER.getSelectedFile()) {
            case File file -> file.toPath().resolve(defaultLogFileName).toFile();
            case null -> new File(defaultLogFileName);
        };
        SaveLogToolPlugin.LOG_SAVE_FILE_CHOOSER.setSelectedFile(defaultFile);
        SaveLogToolPlugin.LOG_SAVE_FILE_CHOOSER.setFileFilter(new FileFilterByExtension<>(null, List.of("log"), "Log"));
        int saveResult = SaveLogToolPlugin.LOG_SAVE_FILE_CHOOSER.showSaveDialog(parent);
        UserPreferences.LATEST_LOGS_DIRECTORY.set(SaveLogToolPlugin.LOG_SAVE_FILE_CHOOSER.getCurrentDirectory());
        if (saveResult != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        Files.write(SaveLogToolPlugin.LOG_SAVE_FILE_CHOOSER.getSelectedFile().toPath(),
                    OpenMarkovLogger.getCapturedLogs().getBytes());
        return true;
    }
    
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    public static String generateDefaultLogFileName() {
        return LocalDateTime.now().format(SaveLogToolPlugin.TIME_FORMATTER) + ".log";
    }
}
