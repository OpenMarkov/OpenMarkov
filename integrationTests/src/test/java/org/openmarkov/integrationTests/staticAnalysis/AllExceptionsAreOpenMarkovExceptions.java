package org.openmarkov.integrationTests.staticAnalysis;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IOpenMarkovException;
import org.openmarkov.plugin.PluginSearch;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

public class AllExceptionsAreOpenMarkovExceptions {
    
    private static final Set<Class<? extends Throwable>> ALLOWED_EXCEPTIONS = Set.of(
            org.openmarkov.core.exception.UnreachableException.class,
            org.openmarkov.core.exception.UnreachableCodeException.class,
            org.openmarkov.core.exception.UnrecoverableException.class
    );
    
    @Test
    public void allExceptionsAreOpenMarkovExceptions() {
        var missingOpenMarkovExceptions = PluginSearch.init()
                                                      .childrenOf(Throwable.class)
                                                      .filter(exceptionClass -> !IOpenMarkovException.class.isAssignableFrom(exceptionClass))
                                                      .stream()
                                                      .collect(Collectors.toSet());
        missingOpenMarkovExceptions.removeAll(ALLOWED_EXCEPTIONS);
        if (missingOpenMarkovExceptions.isEmpty()) {
            return;
        }
        fail("Not all exceptions declared in OpenMarkov implement " + IOpenMarkovException.class + ":" + System.lineSeparator()
                     + missingOpenMarkovExceptions.stream()
                                                  .map(exceptionClass -> "\t- " + exceptionClass)
                                                  .collect(Collectors.joining(System.lineSeparator()))
        );
    }
}
