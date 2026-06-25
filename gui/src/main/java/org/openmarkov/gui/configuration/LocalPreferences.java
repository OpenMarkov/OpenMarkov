package org.openmarkov.gui.configuration;

import com.google.gson.reflect.TypeToken;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.gui.dialog.common.WindowDimensions;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_1_0;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Central registry of all persistent user preferences for OpenMarkov.
 * Each preference is a typed {@link LocalPreference} backed by the Java Preferences API
 * and serialized via GSON. Preferences cover directories, UI settings, colors, formats,
 * and language.
 */
public final class LocalPreferences {
    
    public static final LocalPreference<Boolean> HOVER_LOGGER_ENABLED = LocalPreference
            .of("dev_tools/hover_logger_enabled", () -> false, new TypeToken<>() {
            });
    
    public static final LocalPreference<File> LATEST_OPEN_DIRECTORY = LocalPreference
            .of("directories/latest_open_directory", () -> new File("."), new TypeToken<>() {
            });
    
    public static final LocalPreference<File> LATEST_SAVED_DIRECTORY = LocalPreference
            .of("directories/latest_saved_directory", () -> new File("."), new TypeToken<>() {
            });
    
    public static final LocalPreference<File> LATEST_OPEN_DATASET_DIRECTORY = LocalPreference
            .of("directories/latest_open_dataset_directory", () -> new File("."), new TypeToken<>() {
            });
    
    public static final LocalPreference<File> LATEST_SAVED_DATASET_DIRECTORY = LocalPreference
            .of("directories/latest_saved_dataset_directory", () -> new File("."), new TypeToken<>() {
            });
    
    public static final LocalPreference<ArrayList<String>> LAST_OPEN_NETWORKS_FILES = LocalPreference
            .of("directories/last_open_networks_files", () -> new ArrayList<>(), new TypeToken<>() {
            });
    
    
    public static final LocalPreference<WindowDimensions> LATEST_MAIN_GUI_DIMENSIONS = LocalPreference
            .of("user_interface/latest_main_gui_dimensions",
                () -> new WindowDimensions(new Point(0, 0), new Dimension(600, 400), 0),
                new TypeToken<>() {
                });
    
    public static final LocalPreference<Double> UI_SCALE = LocalPreference
            .of("user_interface/ui_scale", () -> 1.0, new TypeToken<>() {
            });
    
    public static final LocalPreference<Theme> PREFERRED_THEME = LocalPreference
            .of("user_interface/prefered_theme", () -> Theme.LIGHT, new TypeToken<>() {
            });
    
    public static final LocalPreference<String> PREFERENCE_LANGUAGE = LocalPreference
            .of("languages/user_preferred_language", () -> System.getProperty("user.language"), new TypeToken<>() {
            });
    
    public static final LocalPreference<String> LATEST_NETWORK_FORMAT = LocalPreference
            .of("formats/latest_network_format", () -> OMFileChooser.DEFAULT_FILE_FORMAT, new TypeToken<>() {
            });
    
    public static final LocalPreference<Class<? extends ProbNetWriter>> LATEST_SAVED_NETWORK_WRITER_CLASS = LocalPreference
            .of("formats/latest_saved_network_writer", () -> PGMXWriter_1_0.class, new TypeToken<>() {
            });
    
    public static final LocalPreference<Class<? extends ProbNetReader>> LATEST_SAVED_NETWORK_READER_CLASS = LocalPreference
            .of("formats/latest_saved_network_reader", () -> PGMXReader.class, new TypeToken<>() {
            });
    
    public static final LocalPreference<String> LATEST_LOADED_EVIDENCE_FORMAT = LocalPreference
            .of("formats/latest_loaded_evidence_format", () -> "xlsx", new TypeToken<>() {
            });
    
    public static final LocalPreference<String> LATEST_SAVED_DATASET_FORMAT = LocalPreference
            .of("formats/latest_saved_dataset_format", () -> OMFileChooser.DEFAULT_FILE_FORMAT, new TypeToken<>() {
            });
    
    public static final LocalPreference<String> LATEST_SAVED_DATASET_EXTENSION = LocalPreference
            .of("formats/latest_saved_dataset_format", () -> "xlsx", new TypeToken<>() {
            });
    
    private static final List<LocalPreference<?>> ALL_PREFERENCES;
    
    static {
        List<LocalPreference<?>> allPreferences = new ArrayList<>();
        for (var field : LocalPreferences.class.getDeclaredFields()) {
            if (!LocalPreference.class.isAssignableFrom(field.getType())) continue;
            try {
                allPreferences.add((LocalPreference<?>) field.get(null));
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
    public static List<LocalPreference<?>> getAllPreferences() {
        return ALL_PREFERENCES;
    }

    /**
     * Eagerly initializes all preferences, loading their values from persistent storage.
     */
    public static void initializeAllPreferences() {
        ALL_PREFERENCES.forEach(LocalPreference::initialize);
    }
}
