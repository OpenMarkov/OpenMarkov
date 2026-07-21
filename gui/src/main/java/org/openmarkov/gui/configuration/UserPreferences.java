package org.openmarkov.gui.configuration;

import com.google.gson.reflect.TypeToken;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.gui.dialog.common.WindowDimensions;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry of all persistent user preferences for OpenMarkov.
 * Each preference is a typed {@link UserPreference} backed by the Java Preferences API
 * and serialized via GSON. Preferences cover directories, UI settings, colors, formats,
 * and language.
 */
public final class UserPreferences {
    
    public static final UserPreference<Boolean> HOVER_LOGGER_ENABLED = UserPreference
            .of("dev_tools/hover_logger_enabled", () -> false, null, new TypeToken<>() {
            });
    
    public static final UserPreference<File> LATEST_OPEN_DIRECTORY = UserPreference
            .of("directories/latest_open_directory", () -> new File("."), null, new TypeToken<>() {
            });
    
    public static final UserPreference<File> LATEST_SAVED_DIRECTORY = UserPreference
            .of("directories/latest_saved_directory", () -> new File("."), null, new TypeToken<>() {
            });
    
    public static final UserPreference<File> LATEST_OPEN_DATASET_DIRECTORY = UserPreference
            .of("directories/latest_open_dataset_directory", () -> new File("."), null, new TypeToken<>() {
            });
    
    public static final UserPreference<File> LATEST_SAVED_DATASET_DIRECTORY = UserPreference
            .of("directories/latest_saved_dataset_directory", () -> new File("."), null, new TypeToken<>() {
            });
    
    public static final UserPreference<ArrayList<String>> LAST_OPEN_NETWORKS_FILES = UserPreference
            .of("directories/last_open_networks_files", () -> new ArrayList<>(), null, new TypeToken<>() {
            });
    
    
    public static final UserPreference<Boolean> RESTORE_LATEST_MAIN_GUI_DIMENSIONS = UserPreference
            .of("user_interface/restore_latest_main_gui_dimensions",
                () -> true, new UserPreference.BackupInfo(true,
                                                          "Restore window on startup",
                                                          "When opening OpenMarkov, the window will have the same dimensions as when last closed",
                                                          () -> {
                                                          }),
                new TypeToken<>() {
                });
    
    public static final UserPreference<WindowDimensions> LATEST_MAIN_GUI_DIMENSIONS = UserPreference
            .of("user_interface/latest_main_gui_dimensions",
                () -> new WindowDimensions(new Point(0, 0), new Dimension(600, 400), 0),
                null, new TypeToken<>() {
                    });
    
    public static final UserPreference<Double> UI_SCALE = UserPreference
            .of("user_interface/ui_scale", () -> 1.0, null,
                
                new TypeToken<>() {
                });
    
    public static final UserPreference<Theme> PREFERRED_THEME = UserPreference
            .of("user_interface/prefered_theme", () -> Theme.SYNC_OS, new UserPreference.BackupInfo(true, "Prefered theme", "The appearance of OpenMarkov", () -> {
                try {
                    Theme.updateInterfaceToLook();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException e) {
                }
            }), new TypeToken<>() {
            });
    
    public static final UserPreference<String> PREFERENCE_LANGUAGE = UserPreference
            .of("languages/user_preferred_language", () -> System.getProperty("user.language"), null, new TypeToken<>() {
            });
    
    public static final UserPreference<String> LATEST_NETWORK_FORMAT = UserPreference
            .of("formats/latest_network_format", () -> OMFileChooser.DEFAULT_FILE_FORMAT, null, new TypeToken<>() {
            });
    
    public static final UserPreference<Class<? extends ProbNetWriter>> LATEST_SAVED_NETWORK_WRITER_CLASS = UserPreference
            .of("formats/latest_saved_network_writer", () -> PGMXWriter_1_0.class, null, new TypeToken<>() {
            });
    
    public static final UserPreference<Class<? extends ProbNetReader>> LATEST_SAVED_NETWORK_READER_CLASS = UserPreference
            .of("formats/latest_saved_network_reader", () -> PGMXReader.class, null, new TypeToken<>() {
            });
    
    public static final UserPreference<String> LATEST_LOADED_EVIDENCE_FORMAT = UserPreference
            .of("formats/latest_loaded_evidence_format", () -> "xlsx", null, new TypeToken<>() {
            });
    
    public static final UserPreference<String> LATEST_SAVED_DATASET_EXTENSION = UserPreference
            .of("formats/latest_saved_dataset_format", () -> "xlsx", null, new TypeToken<>() {
            });
    
    public static final UserPreference<ArrayList<ArrayList<String>>> CUSTOM_DOMAINS = UserPreference
            .of("network/custom_domains", () -> new ArrayList<>(), new UserPreference.BackupInfo(true, "Custom domains", "Custom domain used for new nodes", () -> {
            
            }), new TypeToken<>() {
            });
    
    private static final List<UserPreference<?>> ALL_PREFERENCES;
    
    static {
        List<UserPreference<?>> allPreferences = new ArrayList<>();
        for (var field : UserPreferences.class.getDeclaredFields()) {
            if (!UserPreference.class.isAssignableFrom(field.getType())) continue;
            try {
                allPreferences.add((UserPreference<?>) field.get(null));
            } catch (IllegalAccessException ignored) {
            }
        }
        ALL_PREFERENCES = Collections.unmodifiableList(allPreferences);
    }
    
    /**
     * Returns an unmodifiable list of all declared preferences.
     *
     * @return all local preferences
     */
    public static List<UserPreference<?>> getAllPreferences() {
        return ALL_PREFERENCES;
    }
    
    /**
     * Eagerly initializes all preferences, loading their values from persistent storage.
     */
    public static void initializeAllPreferences() {
        ALL_PREFERENCES.forEach(UserPreference::initialize);
    }
}
