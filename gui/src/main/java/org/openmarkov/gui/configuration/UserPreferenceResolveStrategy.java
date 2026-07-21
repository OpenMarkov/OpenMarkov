package org.openmarkov.gui.configuration;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.logging.OpenMarkovLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Strategy for resolving preference values from different backing stores.
 * Supports the Java Preferences API, user home directory files, local installation files,
 * and in-memory session storage.
 */
enum UserPreferenceResolveStrategy {
    USER_FOLDER,
    INSTALLED_LOCATION,
    BACKING_STORE,
    SESSION;
    
    /**
     * Retrieves the stored value for the given preference path.
     *
     * @param path the preference path segments
     * @return the stored value, or {@code null} if not found or on error
     */
    public @Nullable String get(List<String> path) {
        try {
            return switch (this) {
                case BACKING_STORE -> UserPreferenceResolveStrategy.preferenceNodeFor(path).get(path.getLast(), null);
                case USER_FOLDER -> Files.readString(UserPreferenceResolveStrategy.userFileFor(path).toPath());
                case INSTALLED_LOCATION -> Files.readString(UserPreferenceResolveStrategy.localFileFor(path).toPath());
                case SESSION -> null;
            };
        } catch (RuntimeException | IOException e) {
            return null;
        }
    }
    
    /**
     * Checks whether a value is set for the given preference path.
     *
     * @param path the preference path segments
     * @return {@code true} if a value is stored at the given path
     */
    public boolean isSet(List<String> path) {
        try {
            return switch (this) {
                case BACKING_STORE ->
                        UserPreferenceResolveStrategy.preferenceNodeFor(path).get(path.getLast(), null) != null;
                case USER_FOLDER ->
                        UserPreferenceResolveStrategy.existsFile(UserPreferenceResolveStrategy.userFileFor(path));
                case INSTALLED_LOCATION ->
                        UserPreferenceResolveStrategy.existsFile(UserPreferenceResolveStrategy.localFileFor(path));
                case SESSION -> false;
            };
        } catch (RuntimeException e) {
            return false;
        }
    }
    
    /**
     * Stores a value at the given preference path.
     *
     * @param path  the preference path segments
     * @param value the value to store
     * @return {@code true} if the value was stored successfully
     */
    public boolean put(List<String> path, String value) {
        try {
            switch (this) {
                case BACKING_STORE -> UserPreferenceResolveStrategy.preferenceNodeFor(path).put(path.getLast(), value);
                case USER_FOLDER ->
                        UserPreferenceResolveStrategy.writeInPath(UserPreferenceResolveStrategy.userFileFor(path), value);
                case INSTALLED_LOCATION ->
                        UserPreferenceResolveStrategy.writeInPath(UserPreferenceResolveStrategy.localFileFor(path), value);
                case SESSION -> {
                }
            }
            OpenMarkovLogger.LOGGER.debug("Save value for " + String.join(".", path) + " with strategy " + this + " and value " + value);
            return true;
        } catch (RuntimeException | IOException e) {
            OpenMarkovLogger.LOGGER.debug("Could not save value for " + String.join(".", path) + " with strategy " + this + " and value " + value, e);
            return false;
        }
    }
    
    /**
     * Removes the value at the given preference path.
     *
     * @param path the preference path segments
     * @return {@code true} if the value was successfully removed
     */
    public boolean clear(List<String> path) {
        try {
            switch (this) {
                case BACKING_STORE ->
                        UserPreferenceResolveStrategy.preferenceNodeFor(path).parent().remove(path.getLast());
                case USER_FOLDER -> Files.delete(UserPreferenceResolveStrategy.userFileFor(path).toPath());
                case INSTALLED_LOCATION -> Files.delete(UserPreferenceResolveStrategy.localFileFor(path).toPath());
                case SESSION -> {
                }
            }
            OpenMarkovLogger.LOGGER.debug("Removed value for " + String.join(".", path) + " with strategy " + this);
            return true;
        } catch (RuntimeException | IOException e) {
            OpenMarkovLogger.LOGGER.debug("Could not remove value for " + String.join(".", path) + " with strategy " + this, e);
            return false;
        }
    }
    
    private static Preferences preferenceNodeFor(List<String> path) {
        var node = Preferences.userRoot().node("OPENMARKOV");
        for (var pathElement : path) {
            node = node.node(pathElement);
        }
        return node;
    }
    
    private static File localFileFor(List<String> path) {
        return UserPreferenceResolveStrategy.resolveFile(new File("openmarkov_preferences"), path);
    }
    
    private static File userFileFor(List<String> path) {
        return UserPreferenceResolveStrategy.resolveFile(new File(new File(SystemUtils.getUserHome(), ".openmarkov"), "preferences"), path);
    }
    
    private static File resolveFile(File parentFile, List<String> path) {
        var preferenceFile = parentFile;
        for (String pathElement : path) {
            preferenceFile = new File(preferenceFile, pathElement);
        }
        return preferenceFile;
    }
    
    private static boolean existsFile(File path) {
        return path.exists() && path.isFile();
    }
    
    private static void writeInPath(File path, String value) throws IOException {
        path.getParentFile().mkdirs();
        Files.writeString(path.toPath(), value);
    }
    
}
