package org.openmarkov.integrationTests.staticAnalysis;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.openmarkov.core.developmentStaticAnalysis.mutability.MutabilityKind;
import org.openmarkov.plugin.PluginSearch;
import org.openmarkov.java.classUtils.ClassUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class VerifyImmutableClasses {
    
    private static final int IDENTATION_OF_SUBMESSAGES = 4;
    private static final MutabilityKind[] IMMUTABILITY_KINDS = MutabilityKind.values();
    
    @ParameterizedTest
    @FieldSource("IMMUTABILITY_KINDS")
    public void verifyMutability(MutabilityKind mutabilityKind) {
        var classWithWrongMutability = PluginSearch
                .init()
                .childrenOf(mutabilityKind.representedByInterface())
                .stream()
                .filter(ClassUtils::isConcrete)
                .filter(immutableClass -> !mutabilityKind.mutabilityOf(immutableClass).isImmutable())
                .toList();
        if (classWithWrongMutability.isEmpty()) {
            return;
        }
        String errorTitle = "There are " + classWithWrongMutability.size() + " "
                + mutabilityKind.name().toLowerCase()
                + " immutable classes containing non-final fields, meaning they aren't actually immutable:";
        String errorDescription = classWithWrongMutability.stream().map(notImmutableClass -> {
            var wrongFields = mutabilityKind.mutabilityOf(notImmutableClass).nonFinalFields();
            String mainMessage = "- " + notImmutableClass + " has wrong mutability because of fields:";
            String fieldsDescription = Arrays
                    .stream(wrongFields)
                    .sorted(
                            Comparator.comparing((Field field) -> field.getDeclaringClass().getName())
                                      .thenComparing(Field::getName))
                    .map(field -> "- " + field.toString() + ", which comes from " + field.getDeclaringClass())
                    .collect(Collectors.joining(System.lineSeparator()));
            return mainMessage + System.lineSeparator() + fieldsDescription.indent(IDENTATION_OF_SUBMESSAGES);
        }).collect(Collectors.joining(System.lineSeparator()));
        fail(errorTitle + System.lineSeparator() + errorDescription.indent(IDENTATION_OF_SUBMESSAGES));
    }
    
}
