package org.openmarkov.integrationTests.staticAnalysis;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.gson.GsonCommon;
import org.openmarkov.gui.license.LicenseHolder;
import org.openmarkov.integrationTests.Resources;
import org.openmarkov.plugin.PluginClassCategory;
import org.openmarkov.plugin.PluginSearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public class NoConfusingExceptionsTest {
    
    
    private static final File CACHE_DIRECTORY = new File(Resources.RAW_DIR, "caches");
    
    private static final File CACHE_FILE = new File(CACHE_DIRECTORY, "NoConfusingExceptionsTest.json");
    
    private static class LastRun {
        final List<String> omThrowables;
        final List<String> packages;
        @Nullable String failMessage = null;
        
        public LastRun(List<String> omThrowables, List<String> packages) {
            this.omThrowables = omThrowables;
            this.packages = packages;
        }
    }
    
    /**
     * Tests classes created in OpenMarkov that extend {@link Throwable} don't have names belonging to other
     * {@link Throwable} classes from external dependencies or Java itself.
     */
    @Tag(TestSpeed.SLOW)
    @Test
    public void noConfusingExceptions() throws IOException {
        List<Class<? extends Throwable>> omThrowables = PluginSearch.init()
                                                                    .extending(Throwable.class).stream()
                                                                    .sorted(Comparator.comparing(Class::getSimpleName))
                                                                    .filter(clazz -> !clazz.getName()
                                                                                           .startsWith("org.openmarkov.integrationTests"))
                                                                    .toList();
        var dependencies = LicenseHolder.LICENSE_HOLDERS.stream().map(LicenseHolder::descriptor).toList();
        var currentRun = new LastRun(
                omThrowables.stream().map(Class::getSimpleName).toList(),
                dependencies
        );
        
        try {
            LastRun lastRun = GsonCommon.GSON.fromJson(Files.readString(CACHE_FILE.toPath()), new TypeToken<LastRun>() {
            }.getType());
            if (lastRun.omThrowables.equals(currentRun.omThrowables) && lastRun.packages.equals(currentRun.packages)) {
                if (lastRun.failMessage != null) {
                    fail(lastRun.failMessage);
                }
                OpenMarkovLogger.LOGGER.debug("Pre-resolved");
                return;
            } else {
                OpenMarkovLogger.LOGGER.debug("Last run and current run differ.");
                OpenMarkovLogger.LOGGER.debug("Last run had " + lastRun.packages.size() + " dependencies and " + lastRun.omThrowables.size() + " throwables");
                OpenMarkovLogger.LOGGER.debug("Current run has " + currentRun.packages.size() + " dependencies and " + currentRun.omThrowables.size() + " throwables");
            }
        } catch (Exception e) {
            OpenMarkovLogger.LOGGER.debug(e);
            //Cache could not be read or loaded
        }
        
        HashMap<String, Class<? extends Throwable>> externalExceptionsNames = new HashMap<>();
        PluginSearch.init(List.of(PluginClassCategory.JAVA, PluginClassCategory.EXTERNAL_DEPENDENCY))
                    .extending(Throwable.class).stream()
                    .forEach(throwableClass -> {
                        try {
                            externalExceptionsNames.put(throwableClass.getSimpleName(), throwableClass);
                        } catch (NoClassDefFoundError e) {
                        }
                    });
        var confusingExceptions = omThrowables.stream()
                                              .map(openmarkovThrowableClass -> new OpenMarkovExceptionAndExternalException(
                                                      openmarkovThrowableClass,
                                                      externalExceptionsNames.get(openmarkovThrowableClass.getSimpleName())
                                              ))
                                              .filter(openMarkovExceptionAndExternalException -> openMarkovExceptionAndExternalException.externalThrowableClass != null)
                                              .sorted(Comparator.comparing(openMarkovExceptionAndExternalException -> openMarkovExceptionAndExternalException.openmarkovThrowableClass.getName()))
                                              .toList();
        if (confusingExceptions.isEmpty()) {
            Files.writeString(CACHE_FILE.toPath(), GsonCommon.GSON.toJson(currentRun));
            return;
        }
        String failMessage = "There are some exceptions in OpenMarkov that clashes with names of Exceptions defined in either Java or the external dependencies:"
                + System.lineSeparator()
                + confusingExceptions.stream()
                                     .map(ex -> "\t- " + ex.openmarkovThrowableClass.getName() + " clashes with " + ex.externalThrowableClass.getName())
                                     .collect(Collectors.joining(System.lineSeparator()));
        currentRun.failMessage = failMessage;
        Files.writeString(CACHE_FILE.toPath(), GsonCommon.GSON.toJson(currentRun));
        fail(failMessage);
    }
    
    public record OpenMarkovExceptionAndExternalException(
            Class<? extends Throwable> openmarkovThrowableClass,
            Class<? extends Throwable> externalThrowableClass) {
    }
    
}
