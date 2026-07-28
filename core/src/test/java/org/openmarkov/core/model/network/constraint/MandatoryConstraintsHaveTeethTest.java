/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ValidCriterionName, NoRevelationArc and NoLinkRestriction are declared
 * mandatory ({@code defaultBehavior = YES}) and their check bodies were empty
 * for years: the application announced the guarantees without imposing them.
 * These tests pin that each one now tells a violating network from a clean
 * one.
 *
 * @author Manuel Arias
 */
class MandatoryConstraintsHaveTeethTest {

    private ProbNet probNet;
    private Node parent;
    private Node child;

    @BeforeEach
    void setUp() {
        probNet = new ProbNet();
        parent = probNet.addNode(new Variable("Parent", "no", "yes"), NodeType.CHANCE);
        child = probNet.addNode(new Variable("Child", "no", "yes"), NodeType.CHANCE);
    }

    @Test
    void validCriterionNameRejectsAnEmptyName() {
        probNet.setDecisionCriteria(List.of(new Criterion(""), new Criterion("cost")));

        assertFalse(new ValidCriterionName().isMetBy(probNet));
    }

    @Test
    void validCriterionNameRejectsARepeatedName() {
        probNet.setDecisionCriteria(List.of(new Criterion("cost"), new Criterion("cost")));

        assertFalse(new ValidCriterionName().isMetBy(probNet));
    }

    @Test
    void validCriterionNameAcceptsDistinctNonEmptyNames() {
        probNet.setDecisionCriteria(List.of(new Criterion("cost"), new Criterion("effectiveness")));

        assertTrue(new ValidCriterionName().isMetBy(probNet));
    }

    @Test
    void noRevelationArcTellsARevealingLinkFromAPlainOne() {
        Link<Node> link = probNet.addLink(parent, child, true);
        assertTrue(new NoRevelationArc().isMetBy(probNet), "a plain link reveals nothing");

        link.addRevealingState(parent.getVariable().getState("yes"));

        assertFalse(new NoRevelationArc().isMetBy(probNet));
    }

    @Test
    void noLinkRestrictionTellsARestrictedLinkFromAPlainOne() {
        Link<Node> link = probNet.addLink(parent, child, true);
        assertTrue(new NoLinkRestriction().isMetBy(probNet), "a plain link restricts nothing");

        TablePotential restrictions = new TablePotential(
                Arrays.asList(parent.getVariable(), child.getVariable()), PotentialRole.UNSPECIFIED);
        restrictions.setValues(new double[] { 1.0, 0.0, 1.0, 1.0 });
        link.setRestrictionsPotential(restrictions);

        assertFalse(new NoLinkRestriction().isMetBy(probNet));
    }

    /** The empty bodies made every network pass; a clean network must still pass now. */
    @Test
    void aCleanNetworkMeetsAllThree() {
        probNet.addLink(parent, child, true);

        assertTrue(new ValidCriterionName().isMetBy(probNet));
        assertTrue(new NoRevelationArc().isMetBy(probNet));
        assertTrue(new NoLinkRestriction().isMetBy(probNet));
    }
}
