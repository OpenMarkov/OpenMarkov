/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.graph;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests over random graphs, aimed at the invariants that tie together the two
 * representations of a link in {@link Graph}: the <em>implicit</em> one (the lists of parents,
 * children and siblings of every node) and the <em>explicit</em> one (the {@link Link} objects
 * that {@code makeLinksExplicit} materialises).
 * <p>
 * The example-based tests in {@code GraphTest} pin down concrete cases; these ones generate many
 * graphs instead, so that a discrepancy between the two representations shows up whatever the
 * shape of the graph.
 *
 * @author Manuel Arias
 */
class GraphPropertyTest {

    /** Nodes of every generated graph. Small on purpose: it keeps the shrunk counterexamples readable. */
    private static final int NUM_NODES = 5;

    /**
     * One link of a generated graph, as a recipe: the two nodes are given by their index, so that a
     * recipe can be built without knowing the graph it will be applied to.
     */
    record Edge(int from, int to, boolean directed) {
    }

    // -----------------------------------------------------------------------
    // Properties
    // -----------------------------------------------------------------------

    /**
     * The number of links must be the same counted through either representation.
     */
    @Property
    void theTwoRepresentationsAgreeOnTheNumberOfLinks(@ForAll("linkRecipes") List<Edge> recipe) {
        List<Edge> links = distinctEdges(recipe);

        Graph<String> implicitGraph = buildGraph(links, false);
        assertThat(countImplicitLinks(implicitGraph)).isEqualTo(links.size());

        Graph<String> explicitGraph = buildGraph(links, true);
        assertThat(explicitGraph.getLinks()).hasSize(links.size());
        assertThat(countImplicitLinks(explicitGraph)).isEqualTo(links.size());
    }

    /**
     * Materialising the explicit links is a change of representation, not of contents: neither the
     * number of links nor the neighbourhood of any node may change.
     */
    @Property
    void makingTheLinksExplicitChangesNeitherTheCountNorTheTopology(@ForAll("linkRecipes") List<Edge> recipe) {
        Graph<String> graph = buildGraph(distinctEdges(recipe), false);
        int numLinksBefore = countImplicitLinks(graph);
        Map<String, List<String>> neighborhoodBefore = neighborhood(graph);

        graph.makeLinksExplicit(false);

        assertThat(countImplicitLinks(graph)).isEqualTo(numLinksBefore);
        assertThat(graph.getLinks()).hasSize(numLinksBefore);
        assertThat(neighborhood(graph)).isEqualTo(neighborhoodBefore);
    }

    /**
     * The explicit links carry the whole topology: a graph rebuilt out of them alone must have the
     * same implicit representation as the original.
     */
    @Property
    void theExplicitLinksReproduceTheImplicitRepresentation(@ForAll("linkRecipes") List<Edge> recipe) {
        Graph<String> graph = buildGraph(distinctEdges(recipe), false);
        graph.makeLinksExplicit(false);

        Graph<String> rebuilt = new Graph<>();
        for (String node : graph.getNodes()) {
            rebuilt.addNode(node);
        }
        for (Link<String> link : graph.getLinks()) {
            rebuilt.addLink(link.getFrom(), link.getTo(), link.isDirected());
        }

        assertThat(neighborhood(rebuilt)).isEqualTo(neighborhood(graph));
    }

    /**
     * Adding a link that did not exist and removing it again must leave the graph exactly as it was,
     * in either mode. (Adding one that <em>did</em> exist is left out: an explicit graph is allowed
     * to hold duplicate links on purpose — that is what the {@code DistinctLinks} constraint is
     * there to forbid, when a model wants it forbidden.)
     */
    @Property
    void addingAndThenRemovingALinkLeavesTheGraphAsItWas(@ForAll("linkRecipes") List<Edge> recipe,
            @ForAll @IntRange(min = 0, max = NUM_NODES - 1) int from,
            @ForAll @IntRange(min = 0, max = NUM_NODES - 1) int to, @ForAll boolean directed,
            @ForAll boolean explicit) {
        Assume.that(from != to);
        Graph<String> graph = buildGraph(distinctEdges(recipe), explicit);
        Assume.that(!graph.isNeighbor(node(from), node(to)));

        Map<String, List<String>> neighborhoodBefore = neighborhood(graph);
        int numLinksBefore = countImplicitLinks(graph);

        graph.addLink(node(from), node(to), directed);
        graph.removeLink(node(from), node(to), directed);

        assertThat(neighborhood(graph)).isEqualTo(neighborhoodBefore);
        assertThat(countImplicitLinks(graph)).isEqualTo(numLinksBefore);
        if (explicit) {
            assertThat(graph.getLinks()).hasSize(numLinksBefore);
        }
    }

    /**
     * A removed node must leave no trace: no neighbour list and no explicit link may still mention
     * it. Self-loops are allowed here, since they are the case in which the node is its own
     * neighbour and the cleanup is easiest to get wrong.
     */
    @Property
    void removingANodeLeavesNoTraceOfIt(@ForAll("linkRecipesWithSelfLoops") List<Edge> recipe,
            @ForAll @IntRange(min = 0, max = NUM_NODES - 1) int indexToRemove, @ForAll boolean explicit) {
        Graph<String> graph = buildGraph(distinctEdges(recipe), explicit);
        String removedNode = node(indexToRemove);

        graph.removeNode(removedNode);

        assertThat(graph.getNodes()).doesNotContain(removedNode);
        for (String node : graph.getNodes()) {
            assertThat(graph.getParents(node)).doesNotContain(removedNode);
            assertThat(graph.getChildren(node)).doesNotContain(removedNode);
            assertThat(graph.getSiblings(node)).doesNotContain(removedNode);
        }
        if (explicit) {
            for (Link<String> link : graph.getLinks()) {
                assertThat(link.contains(removedNode)).isFalse();
            }
        }
    }

    /**
     * An undirected search walks every link in both senses, so reachability must be symmetric.
     */
    @Property
    void anUndirectedPathExistsInBothSenses(@ForAll("linkRecipes") List<Edge> recipe,
            @ForAll @IntRange(min = 0, max = NUM_NODES - 1) int one,
            @ForAll @IntRange(min = 0, max = NUM_NODES - 1) int other) {
        Graph<String> graph = buildGraph(distinctEdges(recipe), false);

        assertThat(graph.existsPath(node(one), node(other), false, List.of())).isEqualTo(
                graph.existsPath(node(other), node(one), false, List.of()));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String node(int index) {
        return "N" + index;
    }

    /**
     * Builds a graph with {@link #NUM_NODES} nodes and the given links. When {@code explicit}, the
     * links are made explicit <em>before</em> adding them, so that they are created as {@link Link}
     * objects rather than materialised afterwards.
     */
    private static Graph<String> buildGraph(List<Edge> links, boolean explicit) {
        Graph<String> graph = new Graph<>();
        for (int i = 0; i < NUM_NODES; i++) {
            graph.addNode(node(i));
        }
        if (explicit) {
            graph.makeLinksExplicit(false);
        }
        for (Edge link : links) {
            graph.addLink(node(link.from()), node(link.to()), link.directed());
        }
        return graph;
    }

    /**
     * The links a recipe really creates: repetitions collapse into one, and an undirected link is
     * the same link whichever of its two ends it is written from.
     */
    private static List<Edge> distinctEdges(List<Edge> recipe) {
        Set<Edge> distinct = new LinkedHashSet<>();
        for (Edge edge : recipe) {
            distinct.add(edge.directed() ?
                    edge :
                    new Edge(Math.min(edge.from(), edge.to()), Math.max(edge.from(), edge.to()), false));
        }
        return new ArrayList<>(distinct);
    }

    /**
     * Counts the links through the implicit representation. Every link joins two nodes, and so is
     * counted twice — once from each end.
     */
    private static int countImplicitLinks(Graph<String> graph) {
        int numNeighbors = 0;
        for (String node : graph.getNodes()) {
            numNeighbors += graph.getNumNeighbors(node);
        }
        return numNeighbors / 2;
    }

    /** A snapshot of the implicit representation, comparable with {@code equals}. */
    private static Map<String, List<String>> neighborhood(Graph<String> graph) {
        Map<String, List<String>> snapshot = new TreeMap<>();
        for (String node : graph.getNodes()) {
            snapshot.put("parents of " + node, sorted(graph.getParents(node)));
            snapshot.put("children of " + node, sorted(graph.getChildren(node)));
            snapshot.put("siblings of " + node, sorted(graph.getSiblings(node)));
        }
        return snapshot;
    }

    private static List<String> sorted(List<String> nodes) {
        List<String> sortedNodes = new ArrayList<>(nodes);
        Collections.sort(sortedNodes);
        return sortedNodes;
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    /** Recipes without self-loops: the usual case, and the one in which link counting is simple. */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<List<Edge>> linkRecipes() {
        return edges().filter(edge -> edge.from() != edge.to()).list().ofMaxSize(2 * NUM_NODES);
    }

    /** Recipes that may contain self-loops, to exercise the case of a node being its own neighbour. */
    @Provide
    @SuppressWarnings("unused")
    Arbitrary<List<Edge>> linkRecipesWithSelfLoops() {
        return edges().list().ofMaxSize(2 * NUM_NODES);
    }

    private static Arbitrary<Edge> edges() {
        return Combinators.combine(Arbitraries.integers().between(0, NUM_NODES - 1),
                Arbitraries.integers().between(0, NUM_NODES - 1), Arbitraries.of(true, false)).as(Edge::new);
    }
}
