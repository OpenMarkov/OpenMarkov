package org.openmarkov.staticAnalysis;

import org.openmarkov.plugin.PluginClassCategory;
import org.openmarkov.plugin.PluginSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindAllThrownExceptions {
    
    public static void main(String[] args) {
        var thrownExceptions = PluginSearch
                .init().stream().flatMap(openmarkovClass -> {
                    var executables = Stream.concat(Arrays.stream(openmarkovClass.getDeclaredMethods()), Arrays.stream(openmarkovClass.getDeclaredConstructors()));
                    Stream<Class<?>> thrown = executables.flatMap(executable -> Arrays.stream(executable.getExceptionTypes()));
                    return thrown;
                })
                .distinct()
                .sorted(Comparator.comparing(Class::getName))
                .map(exceptionClass -> (Class<Throwable>)exceptionClass)
                .collect(Collectors.toCollection(ArrayList::new));
        
        System.out.println("There are " + thrownExceptions.size() + " different types of exceptions thrown in OpenMarkov's code:");
        thrownExceptions.forEach(exceptionClass -> System.out.println("- " + exceptionClass.getName()));
        
        var exceptionsNames = FindAllThrownExceptions.stringifyThrowables(thrownExceptions
                .stream()
                .filter(exceptionClass -> !exceptionClass.getName().startsWith("org.openmarkov"))
                .map(aClass -> aClass));
        
        System.out.println("Search constructions of external exceptions with:");
        System.out.println("new\\s*(" + exceptionsNames + ")\\s*\\(");
        
        var allExceptionsNames = FindAllThrownExceptions.stringifyThrowables(
                PluginSearch.init(List.of(PluginClassCategory.JAVA, PluginClassCategory.EXTERNAL_DEPENDENCY))
                        .extending(Throwable.class)
                        .stream()
        );
        System.out.println("Search constructions of any external exceptions with:");
        System.out.println("new\\s*(" + allExceptionsNames + ")\\s*\\(");
    }
    
    private static String stringifyThrowables(Stream<Class<? extends Throwable>> throwables) {
        return throwables
                .map(Class::getSimpleName)
                .distinct()
                .sorted()
                .map(name -> "(" + name + ")")
                .collect(Collectors.joining("|"));
    }
    
}
