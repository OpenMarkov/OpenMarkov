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
    
    private record Comparison(Object o1, Object o2) {
    }
    
    private ReflectionEquality(EnumSet<ReflectionEqualityOptions> optionsSet) {
        this.optionsSet = optionsSet;
    }
    
    private final EnumSet<ReflectionEqualityOptions> optionsSet;
    
    private boolean checkAreEquals(@Nullable Object o1, @Nullable Object o2, int indent) {
        String indentString = "\t".repeat(indent);
        OpenMarkovLogger.LOGGER.debug(indentString+"Comparing " + o1 + " with " + o2);
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
            OpenMarkovLogger.LOGGER.debug(indentString+"Comparison was already cached (" + preresolved + ") for comparison of " + o1 + " and " + o2);
            return preresolved;
        }
        this.chaches.put(new Comparison(o1, o2), new Unresolved());
        boolean comparisonResult = this.optionsSet.contains(ReflectionEqualityOptions.EQUALS_SHORT_CIRCUIT) && (o1.equals(o2) || o2.equals(o1));
        if (!comparisonResult) {
            comparisonResult = switch (Tuples.record(o1, o2)) {
                case Tuple2Record(HashMap<?, ?> c1, HashMap<?, ?> c2) -> this.checkAreEquals(c1.keySet(), c2.keySet(), indent+1)
                        && c1.entrySet()
                             .stream()
                             .allMatch(entry -> this.checkAreEquals(entry.getValue(), c2.get(entry.getKey()),indent+1));
                case Tuple2Record(Set<?> c1, Set<?> c2) ->
                        c1.size() == c2.size() && ReflectionEquality.allElementsOfCollectionAreInSet(c1, c2) && ReflectionEquality.allElementsOfCollectionAreInSet(c2, c1);
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
                    var fields1 = ReflectionEquality.extractAllAccesibleFields(o1);
                    var fields2 = ReflectionEquality.extractAllAccesibleFields(o1);
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
        OpenMarkovLogger.LOGGER.debug(indentString +"Just resolved (" + comparisonResult + ") comparison of " + o1 + " and " + o2);
        return comparisonResult;
    }
    
    @SuppressWarnings("ProhibitedExceptionCaught")
    private static boolean allElementsOfCollectionAreInSet(Collection<?> collection, Set<?> set) {
        for (Object value : collection) {
            try {
                if (!set.contains(value)) {
                    return false;
                }
            }catch (NullPointerException | ClassCastException e) {
                return false;
            }
        }
        return true;
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
