/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The operations on a link that need to look inside its ends — that is, the ones that read the
 * variable of a node.
 * <p>
 * {@link Link} is generic in the type of its ends: a graph of anything can have links, and the
 * tests build graphs of plain strings. Restrictions and revealing conditions, on the other hand,
 * are written in terms of the states of the variables of two nodes, so they only make sense for a
 * {@code Link<Node>}. They used to live in {@code Link} itself, casting each end to {@code Node}
 * and throwing {@code ClassCastException} for any other kind of link; here the compiler checks it
 * instead.
 *
 * @author Manuel Arias
 * @see Link
 * @since OpenMarkov 1.0
 */
public final class LinkOperations {

    private LinkOperations() {
        // A holder of static operations; it is never instantiated.
    }

    /**
     * Gives the link a restrictions potential over the variables of its two nodes, with every
     * combination of states declared compatible (all its values are 1).
     *
     * @param link the link to give a restrictions potential to
     */
    public static void initializesRestrictionsPotential(Link<Node> link) {
        List<Variable> variables = new ArrayList<>();
        variables.add(link.getFrom().getVariable());
        variables.add(link.getTo().getVariable());
        TablePotential restrictionsPotential = new TablePotential(variables, PotentialRole.LINK_RESTRICTION);
        Arrays.fill(restrictionsPotential.getValues(), 1);
        link.setRestrictionsPotential(restrictionsPotential);
    }

    /**
     * Declares the combination of {@code state1} and {@code state2} compatible or incompatible. A
     * link with no restrictions potential yet is given one — with everything compatible — before
     * setting the value.
     *
     * @param link          the link whose restrictions are being set
     * @param state1        state of the variable of the first node
     * @param state2        state of the variable of the second node
     * @param compatibility 1 for compatible, 0 for incompatible
     */
    public static void setCompatibilityValue(Link<Node> link, State state1, State state2, int compatibility) {
        if (!link.hasRestrictions()) {
            initializesRestrictionsPotential(link);
        }
        TablePotential restrictionsPotential = link.getRestrictionsPotential();
        int[] indexes = new int[2];
        indexes[0] = restrictionsPotential.getVariable(0).getStateIndex(state1);
        indexes[1] = restrictionsPotential.getVariable(1).getStateIndex(state2);
        List<Variable> variables = restrictionsPotential.getVariables();
        restrictionsPotential.setValue(variables, indexes, compatibility);
    }

    /**
     * @param link the link to examine
     *
     * @return {@code true} if the link has revealing conditions. Which of the two lists of
     * conditions counts depends on the variable of the first node: intervals for a numeric
     * variable, states for any other kind.
     */
    public static boolean hasRevealingConditions(Link<Node> link) {
        VariableType variableType = link.getFrom().getVariable().getVariableType();

        if (variableType == VariableType.NUMERIC) {
            return !link.getRevealingIntervals().isEmpty();
        }
        return !link.getRevealingStates().isEmpty();
    }
}
