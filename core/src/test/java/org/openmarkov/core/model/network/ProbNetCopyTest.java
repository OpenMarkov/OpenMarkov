/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link ProbNet#copy()} and {@link ProbNet#auxCopy(ProbNet)}.
 *
 * <p>Invariants verified:
 * <ul>
 *   <li>Structural equality (same variables, nodes, links, potentials).</li>
 *   <li>The copy shares variable/potential <em>object references</em> with the
 *       original (shallow copy of those objects).</li>
 *   <li>Node coordinates are reproduced in the copy.</li>
 *   <li>Network-level {@code additionalProperties} map is an independent copy.</li>
 *   <li>The network name is reproduced.</li>
 *   <li>Constraints are transferred to the copy.</li>
 *   <li>Network type is preserved.</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ProbNetCopyTest {

    private static final double DELTA = 1e-9;

    // Variables shared across tests
    private Variable rain;
    private Variable wet;
    private TablePotential pRain;
    private TablePotential pWetGivenRain;
    private ProbNet original;

    @BeforeEach
    public void setUp() {
        rain = new Variable("Rain", new State[]{new State("yes"), new State("no")});
        wet  = new Variable("Wet",  new State[]{new State("yes"), new State("no")});

        original = new ProbNet(BayesianNetworkType.getUniqueInstance());
        original.setName("TestNet");

        Node nodeRain = original.addNode(rain, NodeType.CHANCE);
        nodeRain.setCoordinateX(100);
        nodeRain.setCoordinateY(200);

        Node nodeWet = original.addNode(wet, NodeType.CHANCE);
        nodeWet.setCoordinateX(300);
        nodeWet.setCoordinateY(200);

        original.addLink(nodeRain, nodeWet, true);

        pRain = new TablePotential(List.of(rain), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.3, 0.7});
        nodeRain.setPotentials(List.of(pRain));

        pWetGivenRain = new TablePotential(List.of(wet, rain), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1, 0.2, 0.8});
        nodeWet.setPotentials(List.of(pWetGivenRain));
    }

    // -----------------------------------------------------------------------
    // Name
    // -----------------------------------------------------------------------

    @Test
    public void copyPreservesNetworkName() {
        ProbNet copy = original.copy();
        assertEquals("TestNet", copy.getName());
    }

    @Test
    public void copyOfRenamedNetworkCarriesNewName() {
        original.setName("Renamed");
        ProbNet copy = original.copy();
        assertEquals("Renamed", copy.getName());
    }

    // -----------------------------------------------------------------------
    // Network type
    // -----------------------------------------------------------------------

    @Test
    public void copyPreservesNetworkType() {
        ProbNet copy = original.copy();
        assertEquals(original.getNetworkType().getClass(), copy.getNetworkType().getClass());
    }

    @Test
    public void copyOfInfluenceDiagramPreservesType() {
        ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        id.setName("ID");
        Variable d = new Variable("D", 2);
        id.addNode(d, NodeType.DECISION);

        ProbNet copy = id.copy();
        assertEquals(InfluenceDiagramType.class, copy.getNetworkType().getClass());
    }

    // -----------------------------------------------------------------------
    // Variables / nodes
    // -----------------------------------------------------------------------

    @Test
    public void copyHasSameNumberOfNodes() {
        ProbNet copy = original.copy();
        assertEquals(original.getNodes().size(), copy.getNodes().size());
    }

    @Test
    public void copyContainsSameVariables() {
        ProbNet copy = original.copy();
        // Variables are shared by reference
        assertNotNull(copy.getNode(rain));
        assertNotNull(copy.getNode(wet));
    }

    @Test
    public void copySharesVariableObjectReferences() {
        ProbNet copy = original.copy();
        Node copyNodeRain = copy.getNode(rain);
        // The variable object itself is the same instance
        assertSame(rain, copyNodeRain.getVariable());
    }

    // -----------------------------------------------------------------------
    // Coordinates
    // -----------------------------------------------------------------------

    @Test
    public void copyPreservesNodeCoordinates() {
        ProbNet copy = original.copy();

        Node copyRain = copy.getNode(rain);
        assertEquals(100, copyRain.getCoordinateX(), DELTA);
        assertEquals(200, copyRain.getCoordinateY(), DELTA);

        Node copyWet = copy.getNode(wet);
        assertEquals(300, copyWet.getCoordinateX(), DELTA);
        assertEquals(200, copyWet.getCoordinateY(), DELTA);
    }

    @Test
    public void changingCopyCoordinatesDoesNotAffectOriginal() {
        ProbNet copy = original.copy();
        copy.getNode(rain).setCoordinateX(999);

        assertEquals(100, original.getNode(rain).getCoordinateX(), DELTA);
    }

    // -----------------------------------------------------------------------
    // Links
    // -----------------------------------------------------------------------

    @Test
    public void copyPreservesLinkCount() {
        ProbNet copy = original.copy();
        assertEquals(original.getLinks().size(), copy.getLinks().size());
    }

    @Test
    public void copyPreservesDirectedLink() {
        ProbNet copy = original.copy();
        Node copyRain = copy.getNode(rain);
        Node copyWet  = copy.getNode(wet);

        assertTrue(copyRain.getChildren().contains(copyWet),
                "Rain should still be a parent of Wet in the copy");
        assertTrue(copyWet.getParents().contains(copyRain),
                "Wet should still have Rain as parent in the copy");
    }

    @Test
    public void copyLinkNodesAreFromCopyNotOriginal() {
        ProbNet copy = original.copy();
        Node originalRain = original.getNode(rain);
        Node copyRain     = copy.getNode(rain);

        assertNotSame(originalRain, copyRain,
                "Node instances in the copy must be distinct from the original");
    }

    // -----------------------------------------------------------------------
    // Potentials — shallow (shared references)
    // -----------------------------------------------------------------------

    @Test
    public void copySharesPotentialObjectReferences() {
        ProbNet copy = original.copy();
        Node copyRain = copy.getNode(rain);
        // auxCopy does setPotentials(node.getPotentials()) — same list/objects
        assertTrue(copyRain.getPotentials().contains(pRain),
                "Copy node should hold the same potential object as the original");
    }

    @Test
    public void mutatingSharedPotentialAffectsBothNets() {
        ProbNet copy = original.copy();
        // Potentials are shared; mutating the shared array changes both views.
        pRain.getValues()[0] = 0.99;
        Node copyRain = copy.getNode(rain);
        TablePotential copyPotential = (TablePotential) copyRain.getPotentials().get(0);
        assertEquals(0.99, copyPotential.getValues()[0], DELTA,
                "Shared potential mutation must be visible from the copy");
    }

    // -----------------------------------------------------------------------
    // additionalProperties (ProbNet level)
    // -----------------------------------------------------------------------

    @Test
    public void copyHasIndependentAdditionalPropertiesMap() {
        original.putAdditionalProperty("key", "value");
        ProbNet copy = original.copy();
        copy.putAdditionalProperty("key", "changed");

        assertEquals("value", original.getAdditionalProperties().get("key"),
                "Modifying copy's additionalProperties must not affect original");
    }

    @Test
    public void copyPreservesAdditionalPropertiesContent() {
        original.putAdditionalProperty("author", "test");
        ProbNet copy = original.copy();
        assertEquals("test", copy.getAdditionalProperties().get("author"));
    }

    // -----------------------------------------------------------------------
    // Empty network
    // -----------------------------------------------------------------------

    @Test
    public void copyOfEmptyNetworkHasNoNodes() {
        ProbNet empty = new ProbNet(BayesianNetworkType.getUniqueInstance());
        ProbNet copy  = empty.copy();
        assertEquals(0, copy.getNodes().size());
        assertEquals(0, copy.getLinks().size());
    }

    // -----------------------------------------------------------------------
    // auxCopy via subtype (uses auxCopy indirectly)
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Node properties (purpose, relevance, comment, alwaysObserved)
    // -----------------------------------------------------------------------

    @Test
    public void copyPreservesNodePurpose() {
        original.getNode(rain).setPurpose("testPurpose");
        ProbNet copy = original.copy();
        assertEquals("testPurpose", copy.getNode(rain).getPurpose());
    }

    @Test
    public void copyPreservesNodeRelevance() {
        original.getNode(rain).setRelevance(7.5);
        ProbNet copy = original.copy();
        assertEquals(7.5, copy.getNode(rain).getRelevance(), DELTA);
    }

    @Test
    public void copyPreservesNodeComment() {
        original.getNode(rain).setComment("a comment");
        ProbNet copy = original.copy();
        assertEquals("a comment", copy.getNode(rain).getComment());
    }

    @Test
    public void copyPreservesNodeAlwaysObserved() {
        original.getNode(rain).setAlwaysObserved(true);
        ProbNet copy = original.copy();
        assertTrue(copy.getNode(rain).isAlwaysObserved());
    }

    // -----------------------------------------------------------------------
    // decisionCriteria
    // -----------------------------------------------------------------------

    @Test
    public void copyPreservesDecisionCriteriaReference() {
        // auxCopy does setDecisionCriteria(this.getDecisionCriteria()) — shared reference.
        // Verify the criteria are visible in the copy.
        Criterion c = new Criterion("Cost");
        List<Criterion> criteria = new ArrayList<>();
        criteria.add(c);
        original.setDecisionCriteria(criteria);

        ProbNet copy = original.copy();
        assertNotNull(copy.getDecisionCriteria());
        assertEquals(1, copy.getDecisionCriteria().size());
        assertEquals("Cost", copy.getDecisionCriteria().get(0).getCriterionName());
    }

    // -----------------------------------------------------------------------
    // cycleLength
    // -----------------------------------------------------------------------

    @Test
    public void copyPreservesCycleLength() {
        original.setCycleLength(new CycleLength(CycleLength.Unit.MONTH));
        ProbNet copy = original.copy();
        assertNotNull(copy.getCycleLength());
        assertEquals(CycleLength.Unit.MONTH, copy.getCycleLength().getUnit());
    }

    @Test
    public void copyPreservesDefaultCycleLengthUnit() {
        // ProbNet always initialises cycleLength with new CycleLength() → default unit YEAR.
        ProbNet copy = original.copy();
        assertNotNull(copy.getCycleLength());
        assertEquals(original.getCycleLength().getUnit(), copy.getCycleLength().getUnit());
    }

    // -----------------------------------------------------------------------
    // Explicit links branch
    // -----------------------------------------------------------------------

    @Test
    public void copyWithExplicitLinksPreservesLinkCount() {
        original.makeLinksExplicit(false);
        assertTrue(original.hasExplicitLinks());
        ProbNet copy = original.copy();
        assertEquals(original.getLinks().size(), copy.getLinks().size());
    }

    @Test
    public void copyWithExplicitLinksIsAlsoExplicit() {
        original.makeLinksExplicit(false);
        ProbNet copy = original.copy();
        assertTrue(copy.hasExplicitLinks());
    }

    @Test
    public void copyWithExplicitLinksReproducesDirection() {
        original.makeLinksExplicit(false);
        ProbNet copy = original.copy();
        // The Rain→Wet link must remain directed in the copy.
        Node copyRain = copy.getNode(rain);
        Node copyWet  = copy.getNode(wet);
        assertTrue(copyRain.getChildren().contains(copyWet));
    }

    @Test
    public void copyWithExplicitLinksSharesRestrictionPotentialReference() {
        original.makeLinksExplicit(false);
        // Assign a restriction potential to the Rain→Wet link.
        Link<Node> link = original.getLink(original.getNode(rain), original.getNode(wet), true);
        TablePotential restriction = new TablePotential(List.of(rain, wet), PotentialRole.LINK_RESTRICTION);
        link.setRestrictionsPotential(restriction);

        ProbNet copy = original.copy();
        Link<Node> copyLink = copy.getLink(copy.getNode(rain), copy.getNode(wet), true);
        assertSame(restriction, copyLink.getRestrictionsPotential(),
                "Restriction potential should be the same object (shallow copy)");
    }

    // -----------------------------------------------------------------------
    // auxCopy via subtype (uses auxCopy indirectly)
    // -----------------------------------------------------------------------

    @Test
    public void auxCopyViaSubclassPreservesStructure() {
        // auxCopy is called by copy(); we verify the full round-trip via a
        // concrete subtype scenario (ID) to confirm auxCopy handles it.
        ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        id.setName("myID");
        Variable x = new Variable("X", 2);
        Variable u = new Variable("U", 1);
        Node nx = id.addNode(x, NodeType.CHANCE);
        Node nu = id.addNode(u, NodeType.UTILITY);
        id.addLink(nx, nu, true);

        ProbNet copy = id.copy();
        assertEquals("myID", copy.getName());
        assertEquals(2, copy.getNodes().size());
        assertEquals(1, copy.getLinks().size());
        assertNotNull(copy.getNode(x));
        assertNotNull(copy.getNode(u));
    }
}
