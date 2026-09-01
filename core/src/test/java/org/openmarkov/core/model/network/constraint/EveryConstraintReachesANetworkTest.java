/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeUtils;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A constraint that no network carries defends nobody. For each class annotated with
 * {@code @Constraint}, either some type of network puts it in its list, or it is named below as
 * one of those that somebody adds by hand, or as one that reaches no network at all.
 *
 * @author Manuel Arias
 */
public class EveryConstraintReachesANetworkTest {

    /**
     * Constraints that no type of network carries because a caller creates them: they either need
     * data in the constructor or answer to a choice of the user.
     */
    private static final Map<String, String> ADDED_BY_HAND = new LinkedHashMap<>();

    /**
     * Constraints that reach no network at all. They are marked OPTIONAL, and the list of optional
     * constraints is only built by the tests: in the program, ProbNet always asks for the list
     * without them. Emptying this list is the decision the plan calls F3-c.
     */
    private static final Map<String, String> WITHOUT_A_NETWORK = new LinkedHashMap<>();

    static {
        ADDED_BY_HAND.put("MaxNumParents", "five learning algorithms, each with its own number");
        ADDED_BY_HAND.put("ModelNetworkConstraint", "the learning manager, with the network the user chose");
        ADDED_BY_HAND.put("OnlyDiscreteVariables", "the dialog of properties of the network");
        ADDED_BY_HAND.put("OnlyContinuousVariables", "the dialog of properties of the network");

        WITHOUT_A_NETWORK.put("NoLoops", "R3");
        WITHOUT_A_NETWORK.put("NoMixedParents", "R3");
        WITHOUT_A_NETWORK.put("NoSuperValueNode", "R3");
        WITHOUT_A_NETWORK.put("OnlyFiniteStatesVariables", "R3");
        WITHOUT_A_NETWORK.put("OnlyNumericVariables", "R3");
        WITHOUT_A_NETWORK.put("OnlyOneUtilityNode", "R3, the clearest case: the edits ask for it and get nothing");
        WITHOUT_A_NETWORK.put("ProperUtilityPotentials", "R3");
        WITHOUT_A_NETWORK.put("OnlyUnlabeledLinks", "R3");
    }

    @Tag(TestSpeed.FAST)
    @Test public void everyConstraintIsCarriedByANetworkOrIsNamedHere() {
        Set<String> carried = constraintsOfEveryNetworkType();

        List<String> unaccounted = annotatedConstraints().stream()
                                                         .filter(name -> !carried.contains(name))
                                                         .filter(name -> !ADDED_BY_HAND.containsKey(name))
                                                         .filter(name -> !WITHOUT_A_NETWORK.containsKey(name))
                                                         .sorted()
                                                         .toList();

        assertTrue(unaccounted.isEmpty(),
                "These constraints reach no network and are not named in this test: " + unaccounted);
    }

    /**
     * The constraints named in the two lists must still be out of every type of network. When one
     * of them ends up in a type, this test asks for its entry to be removed.
     */
    @Tag(TestSpeed.FAST)
    @Test public void theConstraintsNamedHereAreStillOutOfEveryNetworkType() {
        Set<String> carried = constraintsOfEveryNetworkType();

        List<String> nowCarried = namesUsedHere().filter(carried::contains).sorted().toList();

        assertTrue(nowCarried.isEmpty(),
                "These constraints are now in some type of network. Remove their entry from this test "
                + "so that they are checked from now on: " + nowCarried);
    }

    /**
     * Nothing is named here that no longer exists, and the search finds something.
     */
    @Tag(TestSpeed.FAST)
    @Test public void theNamesUsedHereAreThoseOfConstraintsThatExist() {
        Set<String> annotated = annotatedConstraints();
        assertFalse(annotated.isEmpty(), "No annotated constraint was found");

        List<String> gone = namesUsedHere().filter(name -> !annotated.contains(name)).sorted().toList();

        assertTrue(gone.isEmpty(), "These names are not those of an annotated constraint: " + gone);
    }

    private static Stream<String> namesUsedHere() {
        return Stream.concat(ADDED_BY_HAND.keySet().stream(), WITHOUT_A_NETWORK.keySet().stream());
    }

    private static Set<String> annotatedConstraints() {
        return ConstraintManager.findAllConstraints().map(Class::getSimpleName).collect(Collectors.toSet());
    }

    private static Set<String> constraintsOfEveryNetworkType() {
        ConstraintManager manager = ConstraintManager.getUniqueInstance();
        return NetworkTypeUtils.NETWORK_TYPE_CLASSES.stream()
                               .map(NetworkTypeUtils::safeInstanciate)
                               .map(manager::buildConstraintList)
                               .flatMap(List::stream)
                               .map(constraint -> constraint.getClass().getSimpleName())
                               .collect(Collectors.toSet());
    }
}
