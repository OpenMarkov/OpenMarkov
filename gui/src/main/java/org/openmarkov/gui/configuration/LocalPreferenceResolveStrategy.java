package org.openmarkov.gui.configuration;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;

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
enum LocalPreferenceResolveStrategy {
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
                case BACKING_STORE -> LocalPreferenceResolveStrategy.preferenceNodeFor(path).get(path.getLast(), null);
                case USER_FOLDER -> Files.readString(LocalPreferenceResolveStrategy.userFileFor(path).toPath());
                case INSTALLED_LOCATION -> Files.readString(LocalPreferenceResolveStrategy.localFileFor(path).toPath());
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
                        LocalPreferenceResolveStrategy.preferenceNodeFor(path).get(path.getLast(), null) != null;
                case USER_FOLDER ->
                        LocalPreferenceResolveStrategy.existsFile(LocalPreferenceResolveStrategy.userFileFor(path));
                case INSTALLED_LOCATION ->
                        LocalPreferenceResolveStrategy.existsFile(LocalPreferenceResolveStrategy.localFileFor(path));
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
                case BACKING_STORE -> LocalPreferenceResolveStrategy.preferenceNodeFor(path).put(path.getLast(), value);
                case USER_FOLDER ->
                        LocalPreferenceResolveStrategy.writeInPath(LocalPreferenceResolveStrategy.userFileFor(path), value);
                case INSTALLED_LOCATION ->
                        LocalPreferenceResolveStrategy.writeInPath(LocalPreferenceResolveStrategy.localFileFor(path), value);
                case SESSION -> {
                }
            }
            return true;
        } catch (RuntimeException | IOException e) {
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
                        LocalPreferenceResolveStrategy.preferenceNodeFor(path).parent().remove(path.getLast());
                case USER_FOLDER -> Files.delete(LocalPreferenceResolveStrategy.userFileFor(path).toPath());
                case INSTALLED_LOCATION -> Files.delete(LocalPreferenceResolveStrategy.localFileFor(path).toPath());
                case SESSION -> {
                }
            }
            return true;
        } catch (RuntimeException | IOException e) {
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
        return LocalPreferenceResolveStrategy.resolveFile(new File("openmarkov_preferences"), path);
    }
    
    private static File userFileFor(List<String> path) {
        return LocalPreferenceResolveStrategy.resolveFile(new File(new File(SystemUtils.getUserHome(), ".openmarkov"), "preferences"), path);
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
