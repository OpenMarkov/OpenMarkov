package org.openmarkov.staticAnalysis;

import org.openmarkov.plugin.PluginSearch;
import org.openmarkov.staticAnalysis.utils.ExecutableUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindAllThrowingOverriddenMethods {
    
    public static void main(String... args) {
        var openMarkovClasses = PluginSearch.init().stream().collect(Collectors.toSet());
        
        var methodAppeareances = new HashMap<Method, HashSet<Class<?>>>();
        PluginSearch.init().stream().forEach(openmarkovClass -> Stream
                .concat(
                        Arrays.stream(openmarkovClass.getDeclaredMethods()),
                        Arrays.stream(openmarkovClass.getMethods())
                )
                .distinct()
                .forEach(method -> {
                    Class<?> declaringClass = method.getDeclaringClass();
                    Method originalMethod = ExecutableUtils.getOriginalDeclarationOfExecutable(method);
                    if (!openMarkovClasses.contains(originalMethod.getDeclaringClass())) {
                        return;
                    }
                    if (!methodAppeareances.containsKey(originalMethod)) {
                        methodAppeareances.put(originalMethod, new HashSet<>());
                    }
                    methodAppeareances.get(originalMethod).add(declaringClass);
                }));
        
        //Remove methods that aren't overloaded
        for (var method : methodAppeareances.keySet().stream().toList()) {
            if (methodAppeareances.get(method).size() <= 1) {
                methodAppeareances.remove(method);
            }
        }
        //Remove methods without exceptions
        for (var method : methodAppeareances.keySet().stream().toList()) {
            if (method.getExceptionTypes().length == 0) {
                methodAppeareances.remove(method);
            }
        }
        List<Map.Entry<Method, HashSet<Class<?>>>> sortedEntries =
                methodAppeareances.entrySet()
                                  .stream()
                                  .sorted(Comparator.comparing(
                                          entry -> entry.getKey()
                                                        .getDeclaringClass()
                                                        .getName()))
                                  .toList();
        for (var entry : sortedEntries) {
            var method = entry.getKey();
            var classes = entry.getValue().stream()
                               .sorted(Comparator.comparing(Class::getName))
                               .toList();
            System.out.println("In " + method.getDeclaringClass() + " the method " + method.toString() + " is overloaded in " + classes.size() + " classes: " + classes);
        }
    }
    
}
