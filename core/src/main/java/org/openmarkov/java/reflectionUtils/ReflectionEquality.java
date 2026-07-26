package org.openmarkov.java.reflectionUtils;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.logging.OpenMarkovLogger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ReflectionEquality {
    
    public enum ReflectionEqualityOptions{
        EQUALS_SHORT_CIRCUIT
    }
    
    public static boolean areEquals(@Nullable Object o1, @Nullable Object o2, @NotNull ReflectionEqualityOptions... options) {
        var optionsSet = EnumSet.noneOf(ReflectionEqualityOptions.class);
        optionsSet.addAll(Arrays.asList(options));

        return new ReflectionEquality(optionsSet).checkAreEquals(o1, o2, 0);
    }
    
    private final HashMap<Comparison, Cache> chaches = new HashMap<>();
    
    private sealed interface Cache {
    }
    
    private record Resolved(Boolean comparisonResult) implements Cache {
    }
    
    private record Unresolved() implements Cache {
    }
    
    /**
     * A pair of objects this comparison has already been asked about. It tells pairs apart by
     * IDENTITY, overriding what a record would otherwise generate.
     *
     * <p>The map this keys serves two purposes and both of them mean identity: it answers a repeated
     * question at once, and it survives a cycle by marking a pair as {@link Unresolved} while its
     * members are still being walked. Both are the question "have I already started comparing these
     * two objects?", not "these two values".
     *
     * <p>A record's generated equality would ask the members, and several model classes here answer
     * that question loosely - a potential compares its variables and its role and nothing else. A
     * nested pair whose members are merely equal to an outer pair would then read the outer entry,
     * find the still-being-compared marker and answer yes without looking at anything, and the whole
     * comparison would come back equal. The caller that pays for that is the potential editor, which
     * asks this class whether the user changed anything: a wrong yes throws the edit away.
     */
    private record Comparison(Object o1, Object o2) {
        @Override public boolean equals(Object other) {
            return other instanceof Comparison comparison && o1 == comparison.o1 && o2 == comparison.o2;
        }

        @Override public int hashCode() {
            return 31 * System.identityHashCode(o1) + System.identityHashCode(o2);
        }
    }
    
    private ReflectionEquality(EnumSet<ReflectionEqualityOptions> optionsSet) {
        this.optionsSet = optionsSet;
    }
    
    private final EnumSet<ReflectionEqualityOptions> optionsSet;
    
    private boolean checkAreEquals(@Nullable Object o1, @Nullable Object o2, int indent) {
        String indentString = "\t".repeat(indent);
        OpenMarkovLogger.LOGGER.trace(indentString + "Comparing " + o1 + " with " + o2);
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        Boolean preresolved = switch (this.chaches.get(new Comparison(o1, o2))) {
            case Resolved(Boolean comparisonResult) -> comparisonResult;
            case Unresolved _ -> true;
            case null -> null;
        };
        if (preresolved != null) {
            OpenMarkovLogger.LOGGER.trace(indentString + "Comparison was already cached (" + preresolved + ") for comparison of " + o1 + " and " + o2);
            return preresolved;
        }
        this.chaches.put(new Comparison(o1, o2), new Unresolved());
        boolean comparisonResult = this.optionsSet.contains(ReflectionEqualityOptions.EQUALS_SHORT_CIRCUIT) && (o1.equals(o2) || o2.equals(o1));
        if (!comparisonResult) {
            comparisonResult = switch (Tuples.record(o1, o2)) {
                case Tuple2Record(Map<?, ?> c1, Map<?, ?> c2) ->
                        c1.size() == c2.size() && this.everyEntryHasAMatch(c1, c2, indent + 1);
                case Tuple2Record(Set<?> c1, Set<?> c2) ->
                        c1.size() == c2.size() && this.allElementsOfCollectionAreInSet(c1, c2, indent + 1)
                                && this.allElementsOfCollectionAreInSet(c2, c1, indent + 1);
                case Tuple2Record(Collection<?> c1, Collection<?> c2) -> {
                    if (c1.size() != c2.size()) {
                        yield false;
                    }
                    var c1Iter = c1.iterator();
                    var c2Iter = c2.iterator();
                    while (c1Iter.hasNext() && c2Iter.hasNext()) {
                        Object valueIter1 = c1Iter.next();
                        Object valueIter2 = c2Iter.next();
                        if (!this.checkAreEquals(valueIter1, valueIter2, indent+1)) {
                            yield false;
                        }
                    }
                    yield true;
                }
                case Tuple2Record(Object array1, Object array2)
                        when array1.getClass().isArray() && array2.getClass().isArray() -> {
                    int length1 = Array.getLength(array1);
                    int length2 = Array.getLength(array2);
                    if (length1 != length2) {
                        yield false;
                    }
                    for (int i = 0; i < length1; i++) {
                        Object value1 = Array.get(array1, i);
                        Object value2 = Array.get(array2, i);
                        if (!this.checkAreEquals(value1, value2, indent+1)) {
                            yield false;
                        }
                    }
                    yield true;
                }
                case Tuple2Record(Object _, Object _) -> {
                    if (o1.getClass() != o2.getClass()) {
                        yield false;
                    }
                    if (ReflectionEquality.BASIC_TYPES.contains(o1.getClass()) || ReflectionEquality.BASIC_TYPES.contains(o2.getClass())) {
                        yield o1.equals(o2) || o2.equals(o1);
                    }
                    // The second list is taken from o2, which is what the loop below reads it against.
                    // It used to say o1 here, a slip with no effect: the two objects were just checked
                    // to be of the same class, so the fields are the same either way. Saying o2 makes
                    // "fields2.get(i).get(o2)" true by construction rather than by that coincidence.
                    var fields1 = ReflectionEquality.extractAllAccesibleFields(o1);
                    var fields2 = ReflectionEquality.extractAllAccesibleFields(o2);
                    if (fields1.size() < ReflectionEquality.countComparableFields(o1.getClass())) {
                        // Some of its fields could not be opened - the usual reason is that the class
                        // belongs to another module, which is the case for every type of the JDK. What
                        // this used to do was compare the fields it COULD read, and a class none of
                        // whose fields it can read was compared over no fields at all, which comes out
                        // true: two different objects declared equal, in silence. Ask the object
                        // itself instead, which for those types is exactly who knows the answer.
                        yield o1.equals(o2);
                    }
                    if (!this.checkAreEquals(fields1, fields2, indent+1)) {
                        yield false;
                    }
                    for (int fieldIndex = 0; fieldIndex < fields1.size(); fieldIndex++) {
                        try {
                            Object valueField1 = fields1.get(fieldIndex).get(o1);
                            Object valueField2 = fields2.get(fieldIndex).get(o2);
                            if (!this.checkAreEquals(valueField1, valueField2, indent+1)) {
                                yield false;
                            }
                        } catch (IllegalAccessException e) {
                            throw new UnreachableException(e);
                        }
                    }
                    yield true;
                }
            };
        }
        this.chaches.put(new Comparison(o1, o2), new Resolved(comparisonResult));
        OpenMarkovLogger.LOGGER.trace(indentString + "Just resolved (" + comparisonResult + ") comparison of " + o1 + " and " + o2);
        return comparisonResult;
    }
    
    /**
     * Whether every entry of one map has a matching entry in the other, key and value both decided by
     * THIS comparison.
     *
     * <p>It used to look each key up with {@code get}, which resolves by the key's own hash and
     * equality - so a map keyed by objects of a class that defines neither found nothing, compared a
     * value against null, and reported two identical maps as different. Only maps that were literally
     * a HashMap reached this at all; every other implementation fell through to the walk over fields,
     * which for a map of the JDK can open none, and came out equal whatever it held.
     *
     * <p>Quadratic in the size of the map. That is the price of matching by content when the content
     * cannot be hashed.
     */
    private boolean everyEntryHasAMatch(Map<?, ?> one, Map<?, ?> other, int indent) {
        for (Map.Entry<?, ?> entry : one.entrySet()) {
            boolean matched = false;
            for (Map.Entry<?, ?> candidate : other.entrySet()) {
                if (this.checkAreEquals(entry.getKey(), candidate.getKey(), indent)
                        && this.checkAreEquals(entry.getValue(), candidate.getValue(), indent)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether every element of the collection has a match in the set, decided by THIS comparison and
     * not by the elements' own equality.
     *
     * <p>It used to ask {@code set.contains}, which is the elements' own {@code equals} - the very
     * thing this class exists so as not to need. Two structurally identical objects of a class that
     * defines no equality were reported as absent from each other's sets, so any structure holding
     * them in a set or as the keys of a map came out different from its own copy.
     *
     * <p>Quadratic in the size of the set, which membership by hashing is not. That is the price of
     * matching by content when the content cannot be hashed.
     */
    private boolean allElementsOfCollectionAreInSet(Collection<?> collection, Set<?> set, int indent) {
        for (Object value : collection) {
            boolean found = false;
            for (Object candidate : set) {
                if (this.checkAreEquals(value, candidate, indent)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * How many fields the comparison would want to read from a class: the non-static, non-transient
     * ones, all the way up the hierarchy. Compared against how many it could actually open, it says
     * whether anything was hidden from it.
     */
    private static int countComparableFields(Class<?> type) {
        int count = 0;
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                    count++;
                }
            }
        }
        return count;
    }

    private static ArrayList<Field> extractAllAccesibleFields(Object object) {
        var superclass = object.getClass();
        ArrayList<Field> fields = new ArrayList<>();
        while (superclass != null) {
            Arrays.stream(superclass.getDeclaredFields())
                  .filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                  .peek(field -> {
                      try {
                          field.setAccessible(true);
                      } catch (InaccessibleObjectException e) {
                      }
                  })
                  .filter(field -> field.canAccess(object))
                  .forEach(fields::add);
            superclass = superclass.getSuperclass();
        }
        return fields;
    }
    
    private static final Set<Class<?>> BASIC_TYPES =Set.of(Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class, String.class);
}
