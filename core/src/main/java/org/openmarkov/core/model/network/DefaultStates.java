/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import java.util.List;

/**
 * Access by position to the catalogue of default states declared in {@link StandardDomain}, for the
 * menus that offer the domains as a list and answer with the position chosen.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo - fix javadoc and initial values for fields
 * @version 1.2 The domains are no longer declared here: {@link StandardDomain} holds them.
 */
public class DefaultStates {

    /**
     * This method returns an array containing the default states of an element
     * of the list.
     *
     * @param index element of the list of default states.
     *
     * @return an array that contains the default states of an element of the
     * list of default states.
     *
     * @throws IndexOutOfBoundsException if the index is not that of a domain of the catalogue.
     */
    public static String[] getByIndex(int index) {
        if ((index < 0) || (index >= StandardDomain.values().length)) {
            throw new IndexOutOfBoundsException(
                    "There is no set of default states number " + index + "; the catalogue has "
                            + StandardDomain.values().length + " of them.");
        }
        List<String> stateNames = StandardDomain.values()[index].getStateNames();
        return stateNames.toArray(new String[0]);
    }

    /**
     * This method returns the index in the list of the states passed as
     * parameter. A states set matches an element of the list if has the same
     * size and the same elements in the same order. The elements of the list
     * are the names of the states, not the language-dependent strings. If the
     * parameter doesn't match any element of the list, then the last index is
     * returned.
     *
     * @param states array that contains the names of the states.
     *
     * @return the index in the list of the states set
     */
    public static int getIndex(State[] states) {
        return StandardDomain.withStates(states).map(Enum::ordinal).orElse(StandardDomain.values().length - 1);
    }

    /**
     * Returns the default states that correspond to a type of node. A default
     * set of states is given for the chance nodes. A prefixed set of states
     * (yes, no) corresponds to the decision nodes. Utility nodes hasn't states.
     *
     * @param type                 type of the node.
     * @param networkDefaultStates default set of states.
     *
     * @return a set of states corresponding to the type of the node.
     */
    public static State[] getStatesNodeType(NodeType type, State[] networkDefaultStates) {
        return switch (type) {
            case CHANCE -> networkDefaultStates;
            case DECISION -> StandardDomain.NO_YES.getStates();
            case UTILITY, EVENT -> new State[]{new State("Default")};
            default -> null;
        };
    }
}
