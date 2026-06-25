package org.openmarkov.integrationTests.staticAnalysis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.plugin.PluginClassCategory;
import org.openmarkov.plugin.PluginSearch;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public class NoConfusingExceptions {
    
    /**
     * Tests classes created in OpenMarkov that extend {@link Throwable} don't have names belonging to other
     * {@link Throwable} classes from external dependencies or Java itself.
     */
    @Tag(TestSpeed.SLOW)
    @Test
    public void noConfusingExceptions() {
        HashMap<String, Class<? extends Throwable>> externalExceptionsNames = new HashMap<>();
        PluginSearch.init(List.of(PluginClassCategory.JAVA, PluginClassCategory.EXTERNAL_DEPENDENCY))
                    .extending(Throwable.class).stream()
                    .forEach(throwableClass -> {
                        try {
                            externalExceptionsNames.put(throwableClass.getSimpleName(), throwableClass);
                        } catch (NoClassDefFoundError e) {
                        }
                    });
        var confusingExceptions = PluginSearch.init().extending(Throwable.class).stream()
                                              .map(openmarkovThrowableClass -> new OpenMarkovExceptionAndExternalException(
                                                      openmarkovThrowableClass,
                                                      externalExceptionsNames.get(openmarkovThrowableClass.getSimpleName())
                                              ))
                                              .filter(openMarkovExceptionAndExternalException -> openMarkovExceptionAndExternalException.externalThrowableClass != null)
                                              .sorted(Comparator.comparing(openMarkovExceptionAndExternalException -> openMarkovExceptionAndExternalException.openmarkovThrowableClass.getName()))
                                              .toList();
        if (confusingExceptions.isEmpty()) {
            return;
        }
        fail("There are some exceptions in OpenMarkov that clashes with names of Exceptions defined in either Java or the external dependencies:"
                     + System.lineSeparator()
                     + confusingExceptions.stream()
                                          .map(ex -> "\t- " + ex.openmarkovThrowableClass.getName() + " clashes with " + ex.externalThrowableClass.getName())
                                          .collect(Collectors.joining(System.lineSeparator()))
        );
    }
    
    public record OpenMarkovExceptionAndExternalException(
            Class<? extends Throwable> openmarkovThrowableClass,
            Class<? extends Throwable> externalThrowableClass) {
    }
    
}
