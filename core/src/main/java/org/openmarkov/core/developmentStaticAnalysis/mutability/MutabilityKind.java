package org.openmarkov.core.developmentStaticAnalysis.mutability;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.java.classUtils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    
    /**
     * What has already been worked out, so that a class is examined once. A concurrent map because the
     * verification is a parameterized test over the two kinds, and the day the suite runs its tests in
     * parallel two threads would be reading and writing this at the same time — with a plain HashMap
     * that can corrupt the table, not merely duplicate work.
     */
    private static final Map<MutabilityKind, Map<Class<?>, Mutability>> MUTABILITY_OF_CLASSES = new ConcurrentHashMap<>();
    
    private static final Class<?>[] MANUALLY_SET_AS_IMMUTABLE_CLASSES = new Class<?>[]{String.class};
    
    static {
        for (var mutabilityKind : MutabilityKind.values()) {
            MUTABILITY_OF_CLASSES.put(mutabilityKind, new ConcurrentHashMap<>());
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
        var fieldsPreventingImmutability = this.getNonFinalFields.apply(clazz);
        Mutability mutability = fieldsPreventingImmutability.length == 0 ?
                Mutability.immutable() :
                new Mutability(fieldsPreventingImmutability);
        mutabilityOfClasses.put(clazz, mutability);
        return mutability;
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
    
    /**
     * The fields that stop a class from being exterior immutable: the ones that can be reassigned
     * after the object is built, which in Java means the ones that are not {@code final}.
     * <p>
     * That, and nothing else. This used to ask {@code fieldIsModifiable}, a question about the
     * <em>type</em> of the field that belongs to the interior check, and got both answers wrong: a
     * field of a primitive, enum, record or interface type was let through even when it was not
     * {@code final} — {@code int age} is the very example the package documentation gives of exterior
     * <em>mutable</em> — and a {@code final} array was reported as a problem although its reference
     * cannot be reassigned.
     * <p>
     * Static fields are left out: they are state of the class, not of the object, and exterior
     * immutability is about what can change in an object once it exists. Inherited fields are
     * included: a field reassignable in a superclass is reassignable in the subclass too.
     */
    private static Field @NotNull [] getFieldsPreventingExteriorImmutability(Class<?> clazz) {
        return allFieldsOf(clazz)
                     .filter(field -> !Modifier.isStatic(field.getModifiers()))
                     .filter(field -> field.getAnnotation(ConsiderFieldAsExteriorImmutable.class) == null)
                     .filter(field -> !Modifier.isFinal(field.getModifiers()))
                     .toArray(Field[]::new);
    }
    
    /**
     * The fields that stop a class from being interior immutable: the ones whose contents can change,
     * looked for both in the class itself and, recursively, inside the types of its fields.
     * <p>
     * The walk starts at {@code clazz} on purpose. It used to start at the <em>types</em> of its
     * fields, which meant the fields of the class itself were never examined, and a whole kind of
     * mutable content escaped: an array. {@code final int[] data} was called interior immutable
     * because an array type has no fields of its own to walk into — while {@code final ArrayList} was
     * caught, but only indirectly, through the array that {@code ArrayList} keeps inside.
     * <p>
     * <strong>Two holes remain, by nature of what reflection can see.</strong> A field declared with an
     * interface type ({@code final List<String>}) is taken as safe, because an interface has no fields
     * to walk into, even though it may hold any mutable implementation. And generic arguments are
     * erased, so {@code final ImmutableList<Human>} is judged by {@code ImmutableList} alone, without
     * looking at {@code Human}. Closing the first one would mean flagging every interface-typed field
     * unless exempted, which is a decision about how demanding this tool should be, not a repair.
     */
    private static Field @NotNull [] getFieldsPreventingInteriorImmutability(Class<?> clazz) {
        HashSet<Class<?>> unvisitedClasses = new HashSet<>();
        unvisitedClasses.add(clazz);
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
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .filter(field -> field.getAnnotation(ConsiderFieldAsInteriorImmutable.class) == null)
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
