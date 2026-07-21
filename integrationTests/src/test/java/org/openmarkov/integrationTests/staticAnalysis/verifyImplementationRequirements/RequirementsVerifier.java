package org.openmarkov.integrationTests.staticAnalysis.verifyImplementationRequirements;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.developmentStaticAnalysis.requirements.SelfClass;
import org.openmarkov.plugin.PluginSearch;
import org.openmarkov.java.classUtils.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record RequirementsVerifier(Class<? extends Object> constrainedClass, ImplementationRequirements requirements) {
    
    public @NotNull Stream<String> findAllErrors() {
        return Stream.of(this.findInheritanceErrors(), this.findConstructorsErrors(), this.findMethodErrors())
                     .flatMap(self -> self);
    }
    
    private @NotNull Stream<String> findMethodErrors() {
        if (this.requirements.requiresMethods().length == 0) {
            return Stream.empty();
        }
        return Arrays
                .stream(this.requirements.requiresMethods())
                .flatMap(requiredMethod -> this
                        .getSourceClasses()
                        .filter(ClassUtils::isConcrete)
                        .filter(classRequiringAMethod -> {
                            try {
                                var method = classRequiringAMethod.getDeclaredMethod(requiredMethod.methodName(), requiredMethod.parameters());
                                if (!requiredMethod.methodKind().is(method)) {
                                    return true;
                                }
                                var returnOfMethod = method.getReturnType();
                                boolean bothReturnsAreTheSame = requiredMethod.returnType().equals(returnOfMethod);
                                boolean returnTypeCanBeAssignedToMethodReturnType = requiredMethod.returnType()
                                                                                                  .isAssignableFrom(returnOfMethod);
                                boolean returnTypeCanBeAssignedToRequieredSelfClass = requiredMethod.returnType() == SelfClass.class && classRequiringAMethod.isAssignableFrom(returnOfMethod);
                                boolean isProperlyImplemented = bothReturnsAreTheSame || returnTypeCanBeAssignedToMethodReturnType || returnTypeCanBeAssignedToRequieredSelfClass;
                                return !isProperlyImplemented;
                            } catch (NoSuchMethodException e) {
                                return true;
                            }
                        })
                        .map(wrongClass -> {
                            
                            String stringifiedReturnType;
                            if (requiredMethod.returnType() == void.class) {
                                stringifiedReturnType = "";
                            } else if (requiredMethod.returnType() == SelfClass.class) {
                                stringifiedReturnType = wrongClass.getName() + " ";
                            } else {
                                stringifiedReturnType = requiredMethod.returnType().getName() + " ";
                            }
                            return wrongClass +
                                    " " + this.stringifyOrigin() +
                                    " and so it should have a method such as: " +
                                    requiredMethod.methodKind().stringifyForMethodTitle() +
                                    stringifiedReturnType +
                                    requiredMethod.methodName() + "(" + Arrays.stream(requiredMethod.parameters())
                                                                              .map(Class::getName)
                                                                              .collect(Collectors.joining(", ")) + ")";
                        }));
    }
    
    private @NotNull Stream<String> findConstructorsErrors() {
        if (this.requirements.requiresOneOfTheseConstructors().length == 0) {
            return Stream.empty();
        }
        var requiredConstructorsArgs = Arrays
                .stream(this.requirements.requiresOneOfTheseConstructors())
                .map(RequiredConstructor::value)
                .toList();
        var constructorsStrings = requiredConstructorsArgs
                .stream()
                .map(requiredConstructors ->
                             Arrays.stream(requiredConstructors)
                                   .map(Class::getName)
                                   .collect(Collectors.joining(", ")))
                .toList();
        
        return this
                .getSourceClasses()
                .filter(ClassUtils::isConcrete)
                .filter(annotatedClass -> requiredConstructorsArgs
                        .stream()
                        .noneMatch(constructorArgs -> {
                            try {
                                annotatedClass.getDeclaredConstructor(constructorArgs);
                                return true;
                            } catch (NoSuchMethodException e) {
                                return false;
                            }
                        })
                )
                .map(wrongClass -> wrongClass +
                        " " + this.stringifyOrigin() +
                        " and so it should have a constructor such as any of the following: " +
                        constructorsStrings.stream()
                                           .map(constructorString -> wrongClass.getSimpleName() + "(" + constructorString + ")")
                                           .collect(Collectors.joining(", ")));
    }
    
    private @NotNull Stream<String> findInheritanceErrors() {
        if (this.requirements.hasToExtendOneOfTheseClasses().length == 0) {
            return Stream.empty();
        }
        var requiredExtension = List.<Class<?>>of(this.requirements.hasToExtendOneOfTheseClasses());
        return this
                .getSourceClasses()
                .filter(ClassUtils::isConcrete)
                .filter(annotatedClass -> requiredExtension
                        .stream()
                        .noneMatch(aClassThatShouldExtend -> aClassThatShouldExtend.isAssignableFrom(annotatedClass)))
                .map(wrongClass -> wrongClass +
                        " " + this.stringifyOrigin() +
                        " and so it should extend one of the following: " + requiredExtension);
    }
    
    private @NotNull Stream<Class<? extends Object>> getSourceClasses() {
        return Stream.concat(
                PluginSearch.init().annotatedWith(this.constrainedClass).stream(),
                PluginSearch.init().extending(this.constrainedClass).stream()
        ).distinct();
    }
    
    private String stringifyOrigin() {
        if (this.constrainedClass.isAnnotation()) {
            return "is annotated with @" + this.constrainedClass.getName();
        }
        if (this.constrainedClass.isInterface()) {
            return "implements interface " + this.constrainedClass.getName();
        }
        if (Modifier.isAbstract(this.constrainedClass.getModifiers())) {
            return "extends abstract class " + this.constrainedClass.getName();
        }
        return "extends class " + this.constrainedClass.getName();
    }
}
