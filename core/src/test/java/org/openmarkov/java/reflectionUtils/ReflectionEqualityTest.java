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
    
}