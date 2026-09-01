/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.type;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.UtilityNodes;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The contracts the whole subpackage leans on: every type is a singleton whose
 * one instance is obtained through its own {@code getUniqueInstance} — network
 * types are compared by reference identity, so uniqueness is correctness, not
 * style — and looking a type up by name answers {@code null} for a name that
 * belongs to no type.
 *
 * @author Manuel Arias
 */
class NetworkTypeContractsTest {

    /**
     * Each type declares its own {@code getUniqueInstance}, and it returns the
     * exact type, the same instance every time. This is what the reflective
     * instantiation of {@code NetworkTypeUtils} assumes without the compiler
     * enforcing it; a subtype that forgot its own method would silently inherit
     * its parent's and answer an instance of the wrong type. It also pins the
     * repaired singleton of DESNetworkType, the one type that built its
     * instance lazily and unsynchronized.
     */
    @Test
    void everyTypeIsItsOwnSingleton() {
        for (Class<? extends NetworkType> typeClass : NetworkTypeUtils.NETWORK_TYPE_CLASSES) {
            NetworkType first = NetworkTypeUtils.safeInstanciate(typeClass);
            NetworkType second = NetworkTypeUtils.safeInstanciate(typeClass);

            assertEquals(typeClass, first.getClass(),
                    typeClass.getSimpleName() + " must answer an instance of its exact type");
            assertSame(first, second,
                    typeClass.getSimpleName() + " must always answer the same instance");
        }
    }

    /**
     * The empty string names no type. The annotation's default for alternative
     * names used to be the literal {@code ""} — as an array default, one
     * alternative name, the empty string — so every type without alternatives
     * answered to {@code ""} and this lookup returned whichever type came
     * first instead of null.
     */
    @Test
    void lookingUpTheEmptyNameAnswersNull() {
        assertNull(NetworkTypeUtils.getNetworkClassByName(""));
    }

    /** The one real alternative name still works: MIDs used to be called MPADs. */
    @Test
    void theHistoricalAlternativeNameStillFindsItsType() {
        assertEquals(MIDType.class, NetworkTypeUtils.getNetworkClassByName("MPAD"));
    }

    /**
     * The label the user sees for an MDP says MDP. The bundle used to say
     * "MPAD" — the historical name of a different type, the Markov influence
     * diagram — so the user created a network called MDP and the application
     * then talked about it under another type's name.
     */
    @Test
    void anMDPIsCalledMDPWhereTheUserReadsIt() {
        assertEquals("MDP", MDPType.getUniqueInstance().toString());
    }

    /** Same trap, second case: the "Markov DAN" type is not a Bayesian network. */
    @Test
    void theMarkovDANTypeIsNotLabelledBayesian() {
        assertEquals("Markov DAN", MarkovDANType.getUniqueInstance().toString());
    }

    /**
     * A network of any type can be created empty. The constructor used to check
     * the constraints of the type on the network it was still building, and an
     * MDP demands a utility node, so an empty MDP was rejected by its own type.
     * What the caller saw was not even that: the rollback left the type unset,
     * and composing the message of the rejection printed the network, so the
     * failure arrived as a NullPointerException from {@code toString}.
     */
    @Test
    void everyTypeCanBeCreatedEmpty() {
        for (Class<? extends NetworkType> typeClass : NetworkTypeUtils.NETWORK_TYPE_CLASSES) {
            NetworkType type = NetworkTypeUtils.safeInstanciate(typeClass);

            ProbNet network = assertDoesNotThrow(() -> new ProbNet(type),
                    "an empty " + typeClass.getSimpleName() + " must be possible to create");
            assertSame(type, network.getNetworkType(),
                    typeClass.getSimpleName() + " must keep the type it was created with");
        }
    }

    /**
     * Not checking at construction time does not mean not checking: the
     * constraints of the type are installed, so the network knows what it is
     * still missing and every later edit is checked against them.
     */
    @Test
    void anEmptyMDPSaysThatItStillNeedsAUtilityNode() {
        ProbNet mdp = new ProbNet(MDPType.getUniqueInstance());

        assertFalse(mdp.checkProbNet(), "an empty MDP does not satisfy its constraints yet");
        assertTrue(mdp.getUnsatisfiedConstraints().stream().anyMatch(constraint -> constraint instanceof UtilityNodes),
                "an empty MDP must report that it has no utility node");
    }
}
