package org.openmarkov.java.reflectionUtils;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ReflectionEqualityTest {
    
    
    record SplitTest(String goal, Runnable action) {
        @Override public @NotNull String toString() {
            return this.goal;
        }
    }
    
    static Stream<SplitTest> source() {
        return Stream.of(
                new SplitTest("Object is equals to self", () ->
                        assertTrue(ReflectionEquality.areEquals(
                                ReflectionEqualityTest.createBaseHuman(),
                                ReflectionEqualityTest.createBaseHuman()))),
                new SplitTest("Object is different when an int field is different", ()
                        -> assertFalse(ReflectionEquality.areEquals(
                        ReflectionEqualityTest.createBaseHuman(),
                        ReflectionEqualityTest.createBaseHumanWith(human -> human.age = 10000)))),
                new SplitTest("Object is different when a String field is different", ()
                        -> assertFalse(ReflectionEquality.areEquals(
                        ReflectionEqualityTest.createBaseHuman(),
                        ReflectionEqualityTest.createBaseHumanWith(human -> human.name = "Juan")))),
                new SplitTest("Object is different when a Set field is different", ()
                        -> assertFalse(ReflectionEquality.areEquals(
                        ReflectionEqualityTest.createBaseHuman(),
                        ReflectionEqualityTest.createBaseHumanWith(human -> human.favouriteFruits = Set.of("Mango"))))),
                new SplitTest("Object is different when a List field is different", ()
                        -> assertFalse(ReflectionEquality.areEquals(
                        ReflectionEqualityTest.createBaseHuman(),
                        ReflectionEqualityTest.createBaseHumanWith(human -> {
                            human.friends.removeLast();
                            human.friends.add(ReflectionEqualityTest.createBaseHuman());
                        }))))
        );
    }
    
    @ParameterizedTest
    @MethodSource("source")
    void main(SplitTest splitTest) {
        splitTest.action.run();
    }
    
    private static @NotNull Human createBaseHuman() {
        Human friend1 = new Human("Friend1", 1, Collections.emptyList(), Set.of("Strawberry"), new HashMap<>());
        Human friend2 = new Human("Friend2", 2, Collections.emptyList(), Set.of("Banana"), new HashMap<>());
        Human friend3 = new Human("Friend2", 3, Collections.emptyList(), Set.of("Tangerine"), new HashMap<>());
        
        return new Human("Jorge", 23, new ArrayList<>(List.of(friend1, friend2, friend3)), Set.of("Cherry"), Map.of(friend1, 100, friend2, 200, friend3, 300));
    }
    
    private static @NotNull Human createBaseHumanWith(Consumer<Human> consumer) {
        Human baseHuman = ReflectionEqualityTest.createBaseHuman();
        consumer.accept(baseHuman);
        return baseHuman;
    }
    
    
    static class Human {
        String name;
        int age;
        List<Human> friends;
        Set<String> favouriteFruits;
        Map<Human, Integer> friendShip;

        Human(String name, int age, List<Human> friends, Set<String> favouriteFruits, Map<Human, Integer> friendShip) {
            this.name = name;
            this.age = age;
            this.friends = friends;
            this.favouriteFruits = favouriteFruits;
            this.friendShip = friendShip;
        }
    }

    // ------------------------------------------- objects whose fields cannot be opened

    /**
     * Two objects that differ must not be declared equal just because their fields cannot be read.
     * <p>
     * The comparison walks fields by reflection, and the fields of a class from another module cannot
     * be opened: setAccessible throws, the field is skipped, and a class all of whose fields are
     * skipped ends up compared field by field over no fields at all - which comes out true. Any type
     * of the JDK is in that position.
     * <p>
     * The maps have their own reason on top of that: the branch that compares maps is written for
     * HashMap and not for Map, so every other implementation - TreeMap, what Map.of gives back,
     * ConcurrentHashMap - never reaches it and falls through to the field walk.
     * <p>
     * It is not theoretical. This class is the change detector of the potential editor, and
     * PiecewiseExponentialPotential keeps its data in five TreeMaps: there is a path along which a
     * user's edit is declared "no change" and dropped.
     */
    @org.junit.jupiter.api.Test
    void twoTreeMapsWithDifferentContentsAreNotEqual() {
        java.util.TreeMap<String, Integer> one = new java.util.TreeMap<>(Map.of("a", 1));
        java.util.TreeMap<String, Integer> other = new java.util.TreeMap<>(Map.of("a", 2));

        assertFalse(ReflectionEquality.areEquals(one, other));
    }

    @org.junit.jupiter.api.Test
    void twoTreeMapsWithTheSameContentsAreEqual() {
        java.util.TreeMap<String, Integer> one = new java.util.TreeMap<>(Map.of("a", 1, "b", 2));
        java.util.TreeMap<String, Integer> other = new java.util.TreeMap<>(Map.of("b", 2, "a", 1));

        assertTrue(ReflectionEquality.areEquals(one, other));
    }

    @org.junit.jupiter.api.Test
    void twoImmutableMapsWithDifferentContentsAreNotEqual() {
        assertFalse(ReflectionEquality.areEquals(Map.of("a", 1), Map.of("a", 2)));
    }

    /** The HashMap case already worked; this is here so that fixing the others does not break it. */
    @org.junit.jupiter.api.Test
    void twoHashMapsWithDifferentContentsAreStillNotEqual() {
        assertFalse(ReflectionEquality.areEquals(new HashMap<>(Map.of("a", 1)),
                                                 new HashMap<>(Map.of("a", 2))));
    }

    /** And a type with no collection about it at all: two different dates are two different dates. */
    @org.junit.jupiter.api.Test
    void twoDifferentDatesAreNotEqual() {
        assertFalse(ReflectionEquality.areEquals(java.time.LocalDate.of(2026, 7, 27),
                                                 java.time.LocalDate.of(1999, 1, 1)));
    }

    // ------------------------------------------- the comparison cache and object identity

    /**
     * A class whose own equality looks at one field and ignores another, which is the shape of
     * several OpenMarkov model classes: a potential compares its variables and its role, a state
     * compares its name.
     */
    static final class WeaklyEqual {
        final String tag;
        final int hidden;
        WeaklyEqual child;

        WeaklyEqual(String tag, int hidden) {
            this.tag = tag;
            this.hidden = hidden;
        }

        @Override public boolean equals(Object other) {
            return other instanceof WeaklyEqual weaklyEqual && tag.equals(weaklyEqual.tag);
        }

        @Override public int hashCode() {
            return tag.hashCode();
        }
    }

    /**
     * The comparison remembers the pairs it has already seen, both to answer a repeated question at
     * once and to survive a cycle. Both of those mean "have I already started comparing THESE TWO
     * OBJECTS", so the memory has to tell objects apart by identity.
     * <p>
     * If it goes by value instead, a nested pair whose two members are merely <em>equal</em> to the
     * outer pair reads the outer entry, finds the "still being compared" marker, and answers yes
     * without ever looking. Here the two children differ in a field the comparison must see, and the
     * whole comparison used to come back true. The caller that pays for that is the potential editor,
     * which asks this class whether the user changed anything: a wrong yes throws the edit away.
     */
    @org.junit.jupiter.api.Test
    void theMemoryOfComparisonsTellsObjectsApartByIdentityAndNotByValue() {
        WeaklyEqual one = new WeaklyEqual("t", 1);
        WeaklyEqual other = new WeaklyEqual("t", 1);
        one.child = new WeaklyEqual("t", 2);
        other.child = new WeaklyEqual("t", 3);

        assertFalse(ReflectionEquality.areEquals(one, other),
                    "the two children differ in a field that is compared, so the objects are different");
    }
}