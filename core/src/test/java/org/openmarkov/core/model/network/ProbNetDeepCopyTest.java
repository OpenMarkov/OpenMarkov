/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.constraint.DistinctLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link ProbNet#deepCopy()}, which had no tests of its own:
 * {@link ProbNetCopyTest} only exercises the shallow copy.
 *
 * <p>What is pinned here is what a deep copy must carry beyond the graph and the potentials:
 * every constraint of the original, and the descriptive metadata (comment, whether to show it
 * when opening, default states, agents), with the agents cloned rather than shared.
 *
 * @author Manuel Arias
 */
public class ProbNetDeepCopyTest {

    private ProbNet original;

    @BeforeEach
    public void setUp() {
        original = new ProbNet(BayesianNetworkType.getUniqueInstance());
        original.setName("TestNet");
        original.addNode(new Variable("Rain", new State[]{new State("yes"), new State("no")}),
                NodeType.CHANCE);
    }

    private static Set<Class<?>> classesOf(List<PNConstraint> constraints) {
        return constraints.stream().map(PNConstraint::getClass).collect(Collectors.toSet());
    }

    // -----------------------------------------------------------------------
    // Constraints
    // -----------------------------------------------------------------------

    @Test
    public void deepCopyKeepsEveryConstraint() {
        ProbNet copy = original.deepCopy();

        // A constraint is also a listener that vetoes edits, so one missing means the copy accepts
        // an edit the original would have refused.
        assertEquals(classesOf(original.getConstraints()), classesOf(copy.getConstraints()));
    }

    @Test
    public void deepCopyOfAnInfluenceDiagramKeepsEveryConstraint() {
        ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        id.addNode(new Variable("D", 2), NodeType.DECISION);

        assertEquals(classesOf(id.getConstraints()), classesOf(id.deepCopy().getConstraints()));
    }

    @Test
    public void deepCopyDoesNotBringBackAConstraintTheOriginalHadRemoved() {
        // Removing DistinctLinks to allow duplicate links is what SuperParentNBAlgorithm does, so
        // the copy must be as permissive as the network it comes from — not more, not less.
        PNConstraint distinctLinks = original.getConstraintOfClass(DistinctLinks.class);
        assertNotNull(distinctLinks, "a Bayesian network is expected to have DistinctLinks");
        original.removeConstraint(distinctLinks);

        ProbNet copy = original.deepCopy();

        assertFalse(copy.hasConstraintOfClass(DistinctLinks.class));
        assertEquals(classesOf(original.getConstraints()), classesOf(copy.getConstraints()));
    }

    // -----------------------------------------------------------------------
    // Descriptive metadata
    // -----------------------------------------------------------------------

    @Test
    public void deepCopyCarriesTheComment() {
        original.setComment("Built from the 2019 cohort");
        original.setShowCommentWhenOpening(true);

        ProbNet copy = original.deepCopy();

        assertEquals("Built from the 2019 cohort", copy.getComment());
        assertTrue(copy.getShowCommentWhenOpening());
    }

    @Test
    public void deepCopyCarriesTheDefaultStates() {
        original.setDefaultStates(new State[]{new State("low"), new State("high")});

        State[] copiedStates = original.deepCopy().getDefaultStates();

        assertEquals(2, copiedStates.length);
        assertEquals("low", copiedStates[0].getName());
        assertEquals("high", copiedStates[1].getName());
    }

    @Test
    public void deepCopyCarriesTheAgents() {
        original.setAgents(agents("Doctor", "Patient"));

        List<StringWithProperties> copiedAgents = original.deepCopy().getAgents();

        assertNotNull(copiedAgents, "a copy of a multiagent network without agents is another model");
        assertEquals(List.of("Doctor", "Patient"),
                copiedAgents.stream().map(StringWithProperties::getString).collect(Collectors.toList()));
    }

    @Test
    public void deepCopyClonesTheAgents() {
        original.setAgents(agents("Doctor"));

        StringWithProperties copiedAgent = original.deepCopy().getAgents().get(0);

        assertNotSame(original.getAgents().get(0), copiedAgent);
        copiedAgent.put("speciality", "cardiology");
        assertNull(original.getAgents().get(0).getAdditionalProperties().get("speciality"),
                "the properties of an agent must not be shared with the original");
    }

    @Test
    public void addingAnAgentToTheCopyDoesNotAddItToTheOriginal() {
        original.setAgents(agents("Doctor"));

        ProbNet copy = original.deepCopy();
        copy.getAgents().add(new StringWithProperties("Patient"));

        assertEquals(1, original.getAgents().size());
    }

    @Test
    public void deepCopyOfANetworkWithoutAgentsHasNoAgents() {
        // null, not an empty list: OnlyOneAgent reads any non-null list as "this is multiagent".
        assertNull(original.getAgents());
        assertNull(original.deepCopy().getAgents());
    }

    private static List<StringWithProperties> agents(String... names) {
        List<StringWithProperties> agents = new ArrayList<>();
        for (String name : names) {
            agents.add(new StringWithProperties(name));
        }
        return agents;
    }
}
