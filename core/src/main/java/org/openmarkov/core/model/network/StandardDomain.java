/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The catalogue of ready-made sets of states ---domains--- that the program offers when a variable
 * is created or its states are replaced.
 *
 * <p>This enumeration is the only place where those domains are declared. Every menu, dialog and
 * default that used to keep its own copy reads them from here, so no two of them can disagree.
 *
 * <p>The strings are the internal names of the states, which are the ones written to the network
 * file; the interface shows them through its own texts. Each domain is written from the least to
 * the most ---{@code absent} before {@code present}, {@code low} before {@code high}---, an order
 * that whoever lays the states along a numeric scale relies on.
 *
 * @author Manuel Arias
 */
public enum StandardDomain {
    ABSENT_PRESENT("absent", "present"),
    NO_YES("no", "yes"),
    NEGATIVE_POSITIVE("negative", "positive"),
    ABSENT_MILD_MODERATE_SEVERE("absent", "mild", "moderate", "severe"),
    LOW_MEDIUM_HIGH("low", "medium", "high"),
    NOT_HAPPENED_HAPPENED("not happened", "happened");

    private final List<String> stateNames;

    StandardDomain(String... stateNames) {
        this.stateNames = List.of(stateNames);
    }

    /**
     * The names of its states, in the order the user sees them. The list cannot be modified: the
     * catalogue is shared by everyone who asks for it.
     *
     * @return the names of the states of this domain.
     */
    public List<String> getStateNames() {
        return stateNames;
    }

    /**
     * A new set of states of this domain, which the caller may modify without touching the
     * catalogue.
     *
     * @return the states of this domain.
     */
    public State[] getStates() {
        return stateNames.stream().map(State::new).toArray(State[]::new);
    }

    /**
     * The domain made of exactly these names, in this order, if the catalogue has one.
     *
     * @param stateNames names of the states looked for.
     *
     * @return the domain with those states, or nothing if no domain has them.
     */
    public static Optional<StandardDomain> withStateNames(List<String> stateNames) {
        return Arrays.stream(values()).filter(domain -> domain.stateNames.equals(stateNames)).findFirst();
    }

    /**
     * The domain made of exactly these states, in this order, if the catalogue has one. Only the
     * names of the states are compared.
     *
     * @param states states looked for.
     *
     * @return the domain with those states, or nothing if no domain has them.
     */
    public static Optional<StandardDomain> withStates(State[] states) {
        return withStateNames(Arrays.stream(states).map(State::getName).toList());
    }
}
