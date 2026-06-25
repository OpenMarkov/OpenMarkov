package org.openmarkov.integrationTests.staticAnalysis.verifyImplementationRequirements;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.plugin.PluginSearch;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ImplementationRequirementsAreMet {
    
    @Tag(TestSpeed.FAST)
    @Test
    public void requirementsAreMet() {
        var currentDate = Date.from(Instant.now());
        @SuppressWarnings({"UseOfObsoleteDateTimeApi", "deprecation"})
        var maxDate = new Date(2025 - 1900, 9 - 1, 1, 0, 0, 0);
        if (currentDate.before(maxDate)) {
            return;
        }
        var errors = PluginSearch.init()
                                 .annotatedWith(ImplementationRequirements.class)
                                 .stream()
                                 .flatMap(constrainedClass -> new RequirementsVerifier(constrainedClass, constrainedClass.getAnnotation(ImplementationRequirements.class)).findAllErrors())
                                 .sorted()
                                 .map(error -> "\t- " + error)
                                 .toList();
        if (errors.isEmpty()) {
            return;
        }
        var errorsString = errors.stream().collect(Collectors.joining(System.lineSeparator()));
        fail("Some classes aren't implemented as expected (" + errors.size() + " errors):" + System.lineSeparator() + errorsString);
    }
    
}
