package org.openmarkov.core.developmentStaticAnalysis.mutability;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.java.classUtils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum MutabilityKind {
    EXTERIOR(ExteriorImmutable.class, MutabilityKind::getFieldsPreventingExteriorImmutability),
    INTERIOR(InteriorImmutable.class, MutabilityKind::getFieldsPreventingInteriorImmutability);
    
    private final Class<?> representedByInterface;
    private final Function<Class<?>, Field[]> getNonFinalFields;
    
    MutabilityKind(Class<?> representedByInterface, Function<Class<?>, Field[]> getNonFinalFields) {
        this.representedByInterface = representedByInterface;
        this.getNonFinalFields = getNonFinalFields;
    }
    
    public Class<?> representedByInterface() {
        return representedByInterface;
    }
    
    private static final HashMap<MutabilityKind, HashMap<Class<?>, Mutability>> MUTABILITY_OF_CLASSES = new HashMap<>();
    
    private static final Class<?>[] MANUALLY_SET_AS_IMMUTABLE_CLASSES = new Class<?>[]{String.class};
    
    static {
        for (var mutabilityKind : MutabilityKind.values()) {
            MUTABILITY_OF_CLASSES.put(mutabilityKind, new HashMap<>());
        }
        for (var manuallySetAsImmutableClass : MANUALLY_SET_AS_IMMUTABLE_CLASSES) {
            MUTABILITY_OF_CLASSES.get(EXTERIOR).put(manuallySetAsImmutableClass, Mutability.immutable());
            MUTABILITY_OF_CLASSES.get(INTERIOR).put(manuallySetAsImmutableClass, Mutability.immutable());
        }
    }
    
    public Mutability mutabilityOf(Class<?> clazz) {
        var mutabilityOfClasses = MUTABILITY_OF_CLASSES.get(this);
        if (mutabilityOfClasses.containsKey(clazz)) {
            return mutabilityOfClasses.get(clazz);
        }
        var nonFinalFields = this.getNonFinalFields.apply(clazz);
        var isExteriorMutable = nonFinalFields.length == 0;
        Mutability exteriorMutability = new Mutability(isExteriorMutable ? null : nonFinalFields);
        mutabilityOfClasses.put(clazz, exteriorMutability);
        return exteriorMutability;
    }
    
    private static boolean fieldIsModifiable(Field field) {
        Class<?> type = field.getType();
        if (type.isArray()) {
            return true;
        }
        if (type.isAnnotation() || type.isEnum() || type.isInterface() || type.isPrimitive() || type.isRecord()) {
            return false;
        }
        boolean isFinal = Modifier.isFinal(field.getModifiers());
        if (!isFinal) {
            return true;
        }
        return false;
    }
    
    private static Field @NotNull [] getFieldsPreventingExteriorImmutability(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                     .filter(field -> field.getAnnotation(ConsiderFieldAsInteriorImmutable.class) == null)
                     .filter(MutabilityKind::fieldIsModifiable)
                     .toArray(Field[]::new);
    }
    
    private static Field @NotNull [] getFieldsPreventingInteriorImmutability(Class<?> clazz) {
        HashSet<Class<?>> unvisitedClasses = allTypeFieldsOf(clazz);
        var visitedClassesAndNonFinalFields = new HashMap<Class<?>, Field[]>();
        while (!unvisitedClasses.isEmpty()) {
            var firstClass = unvisitedClasses.stream().findAny().get();
            unvisitedClasses.remove(firstClass);
            
            Mutability alreadyCalculatedExteriorMutability = MUTABILITY_OF_CLASSES.get(EXTERIOR).get(firstClass);
            Mutability alreadyCalculatedInteriorMutability = MUTABILITY_OF_CLASSES.get(INTERIOR).get(firstClass);
            if (alreadyCalculatedExteriorMutability != null && alreadyCalculatedInteriorMutability != null) {
                var nonFinalFieldsFromInterior = alreadyCalculatedInteriorMutability.nonFinalFields() == null ? new Field[0] : alreadyCalculatedInteriorMutability.nonFinalFields();
                var nonFinalFieldsFromExterior = alreadyCalculatedExteriorMutability.nonFinalFields() == null ? new Field[0] : alreadyCalculatedExteriorMutability.nonFinalFields();
                var nonFinalFields = Stream.concat(Arrays.stream(nonFinalFieldsFromInterior), Arrays.stream(nonFinalFieldsFromExterior))
                                           .toArray(Field[]::new);
                visitedClassesAndNonFinalFields.put(firstClass, nonFinalFields);
                continue;
            }
            
            Field[] nonFinalFields = allFieldsOf(firstClass)
                    .filter(field ->
                                    field.getAnnotation(ConsiderFieldAsExteriorImmutable.class) == null
                    )
                    .filter(MutabilityKind::fieldIsModifiable)
                    .toArray(Field[]::new);
            visitedClassesAndNonFinalFields.put(firstClass, nonFinalFields);
            allTypeFieldsOf(firstClass).forEach(t -> {
                if (!visitedClassesAndNonFinalFields.containsKey(t)) {
                    unvisitedClasses.add(t);
                }
            });
        }
        return visitedClassesAndNonFinalFields.values().stream().flatMap(Stream::of).toArray(Field[]::new);
    }
    
    private static @NotNull HashSet<Class<?>> allTypeFieldsOf(Class<?> clazz) {
        return allFieldsOf(clazz)
                .map(Field::getType)
                .collect(Collectors.toCollection(HashSet::new));
    }
    
    private static @NotNull Stream<Field> allFieldsOf(Class<?> clazz) {
        return ClassUtils.streamOfInstanciableClassOf(clazz)
                         .flatMap(clazz1 -> Arrays.stream(clazz1.getDeclaredFields()));
    }
    
    
}
