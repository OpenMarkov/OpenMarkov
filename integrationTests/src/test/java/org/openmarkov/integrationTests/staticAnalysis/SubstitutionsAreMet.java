package org.openmarkov.integrationTests.staticAnalysis;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.java.staticAnalysis.SubstitutesClass;
import org.openmarkov.plugin.PluginSearch;
import org.openmarkov.staticAnalysis.utils.ObjectCreations;
import org.openmarkov.staticAnalysis.utils.ParseUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class SubstitutionsAreMet {
    
    private static List<TestInfo> TESTS_INFOS = PluginSearch.init()
                                                            .filter(aClass -> aClass.isAnnotationPresent(SubstitutesClass.class))
                                                            .stream()
                                                            .flatMap(annotatedClass -> {
                                                                SubstitutesClass annotation = annotatedClass.getAnnotation(SubstitutesClass.class);
                                                                return Arrays.stream(annotation.value())
                                                                             .map(substitudedClass -> new TestInfo(annotatedClass, substitudedClass, annotation));
                                                            })
                                                            .toList();
    
    static Stream<TestInfo> testInfo() {
        return SubstitutionsAreMet.TESTS_INFOS.stream();
    }
    
    record TestInfo(Class<?> substitutingClass, Class<?> substitudedClass, SubstitutesClass info) {
    }
    
    
    @ParameterizedTest
    @MethodSource("testInfo")
    public void customClassSuccesfullySubstituesClass(TestInfo testInfo) {
        //This noinspection is intentional, as the conditional only happens once instead of in every "filter" operation.
        //noinspection ConditionalCanBePushedInsideExpression
        Predicate<? super ObjectCreations.ObjectCreationWithClass> isInstance = testInfo.info.includesInheritance() ?
                creation -> testInfo.substitudedClass.isAssignableFrom(creation.constructedClass())
                : creation -> testInfo.substitudedClass == creation.constructedClass();
        var wrongCreations = ObjectCreations.OBJECT_CREATIONS.get().stream()
                                                             .filter(isInstance)
                                                             .filter(creation -> !testInfo.substitutingClass.isAssignableFrom(creation.constructedClass()))
                                                             .toList();
        if (wrongCreations.isEmpty()) {
            return;
        }
        fail(testInfo.substitutingClass + " substitutes " + testInfo.substitudedClass + ", but " + testInfo.substitudedClass + " is instantiated in: " + System.lineSeparator() +
                     (wrongCreations.stream()
                                    .map(ObjectCreations.ObjectCreationWithClass::expression)
                                    .map(ParseUtils::getSourceLine)
                                    .map(line -> "\t- " + line)
                                    .collect(Collectors.joining(System.lineSeparator())))
        );
        
    }
    
    
}
