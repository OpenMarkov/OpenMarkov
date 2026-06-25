/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Uses <code>GraphsTests</code> to create a simple <code>Graph</code>.
 * Test elementary methods in <code>Graph</code> class.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class GraphTest {
    
    private Graph<String> graph;
    
    private String nodeA, nodeB, nodeC, nodeD;
    
    private List<String> nodes;
    
    @BeforeEach public void setUp() {
        graph = GraphsTests.createTestGraph();
        nodes = graph.getNodes();
        nodeA = nodes.get(0);
        nodeB = nodes.get(1);
        nodeC = nodes.get(2);
        nodeD = nodes.get(3);
    }
    
    @Test public void testMakeLinksExplicit() {
        
        // Test that the number of links does not change after making them explicit
        int numLinksBefore = countLinks(graph);
        graph.makeLinksExplicit(false);
        int numLinksAfter = countLinks(graph);
        assertEquals(numLinksBefore, numLinksAfter);
        
        // Examine the explicit links and compare them with the implicit ones
        // Check that the explicit link A->B
        List<Link<String>> linksOfA = graph.getLinks();
        assertEquals(3, linksOfA.size());
        linksOfA = graph.getLinks(nodeA);
        assertEquals(1, linksOfA.size());
        Link<String> linkABinA = linksOfA.get(0);
        assertTrue(linkABinA.isDirected());
        assertEquals(linkABinA.getFrom(), nodeA);
        assertEquals(linkABinA.getTo(), nodeB);
        
        // test all the links involving node B
        List<Link<String>> linksOfB = graph.getLinks(nodeB);
        assertEquals(3, linksOfB.size());
        boolean existsLinkABinB = false;
        boolean existsLinkBCinB = false;
        boolean existsLinkBDinB = false;
        for (Link<String> link : linksOfB) {
            if (link.contains(nodeA)) {
                existsLinkABinB = true;
                // test link A->B is also in nodeB
                assertEquals(linkABinA, link);
            }
            if (link.contains(nodeC)) {
                existsLinkBCinB = true;
                assertEquals(link.getFrom(), nodeB);
                assertEquals(link.getTo(), nodeC);
                assertTrue(link.isDirected());
            }
            if (link.contains(nodeD)) {
                existsLinkBDinB = true;
                assertTrue(link.contains(nodeB));
                assertTrue(link.contains(nodeD));
                assertFalse(link.isDirected());
            }
        }
        assertTrue(existsLinkABinB);
        assertTrue(existsLinkBCinB);
        assertTrue(existsLinkBDinB);
        
        // Part 3: test that links created after Graph.makeLinksExplicit()
        // are created properly.
        // Creates an undirected link between A and C
        graph.addLink(nodeA, nodeC, false);
        // checks that the explict link is in both A and C
        linksOfA = graph.getLinks(nodeA);
        assertEquals(2, linksOfA.size());
        List<Link<String>> linksOfC = graph.getLinks(nodeC);
        assertEquals(2, linksOfC.size());
        for (Link<String> link : linksOfA) {
            if (link.contains(nodeC)) {
                assertTrue(link.contains(nodeA));
                for (Link<String> link2 : linksOfC) {
                    if (link2.contains(nodeA)) {
                        assertEquals(link, link2);
                    }
                }
            }
        }
        
        // test the implicit links
        assertTrue(graph.isSibling(nodeA, nodeC));
        assertTrue(graph.isSibling(nodeC, nodeA));
    }
    
    @Test public void testRemoveExplicitLink() {
        graph.makeLinksExplicit(false);
        
        Link<String> linkABinA = graph.getLinks(nodeA).get(0);
        graph.removeLink(linkABinA);
        
        // check that there is no implicit link between A and B
        assertEquals(0, graph.getChildren(nodeA).size());
        assertEquals(0, graph.getParents(nodeB).size());
        
        // test that there is no explicit link between A and B
        assertEquals(0, graph.getLinks(nodeA).size());
        assertEquals(2, graph.getLinks(nodeB).size());
    }
    
    @Test public void testRemoveImplicitLink() {
        graph.removeLink(nodeB, nodeC, true);
        // check that there is no implicit link between B and C
        List<String> childrenOfB = graph.getChildren(nodeB);
        assertEquals(0, childrenOfB.size());
        List<String> parentsOfC = graph.getParents(nodeC);
        assertEquals(0, parentsOfC.size());
    }
    
    @Test public void testRemoveNode() {
        // check the number of nodes and links after removing node A
        graph.removeNode(nodeA);
        assertEquals(2, countLinks(graph));
        assertEquals(3, graph.getNumNodes());
        // check the number of nodes and links after removing node B
        graph.removeNode(nodeB);
        assertEquals(0, countLinks(graph));
        assertEquals(2, graph.getNumNodes());
    }
    
    @Test public void testGetLink() {
        graph.makeLinksExplicit(false);
        // check  that link A->B is directed
        assertNull(graph.getLink(nodeA, nodeB, false));
        Link<String> link = graph.getLink(nodeA, nodeB, true);
        assertEquals(nodeA, link.getFrom());
        assertEquals(nodeB, link.getTo());
        assertTrue(link.isDirected());
        // check  that link D-B is undirected
        assertNull(graph.getLink(nodeB, nodeD, true));
        link = graph.getLink(nodeD, nodeB, false);
        assertTrue(link.contains(nodeD));
        assertTrue(link.contains(nodeB));
    }
    
    @Test public void testGetLinks() {
        // check all the links in the graph directed and undirected ones invoking getLinks()
        int numLinks = countLinks(graph);
        List<Link<String>> links = graph.getLinks();
        assertEquals(numLinks, links.size());
        for (Link<String> link : links) {
            assertTrue(link.contains(nodeB)); // all links contains nodeB
            if (!link.isDirected()) {
                assertTrue(link.contains(nodeD));
            } else {
                if (!link.contains(nodeA)) {
                    assertEquals(link.getTo(), nodeC);
                } else {
                    assertEquals(link.getTo(), nodeB);
                }
            }
        }
    }
    
    @Test public void testExistsPath() {
        assertTrue(graph.existsPath(nodeC, nodeA, false, Collections.emptyList()));
        assertFalse(graph.existsPath(nodeC, nodeA, true, Collections.emptyList()));
        assertFalse(graph.existsPath(nodeA, nodeD, true, Collections.emptyList()));
        assertTrue(graph.existsPath(nodeD, nodeA, false, Collections.emptyList()));
    }
    
    @Test public void testMarry() {
        // Marry 2 nodes
        List<String> nodesToMarry = new ArrayList<>();
        nodesToMarry.add(nodeA);
        nodesToMarry.add(nodeB);
        int numLinks = countLinks(graph);
        graph.marry(nodesToMarry);
        assertEquals(numLinks + 1, countLinks(graph));
        assertFalse(graph.hasExplicitLinks()); // it does not add explicit links
        assertEquals(1, graph.getNumSiblings(nodeA));
        assertTrue(graph.getSiblings(nodeA).contains(nodeB));
        assertTrue(graph.getSiblings(nodeB).contains(nodeA));
        assertEquals(2, graph.getNumSiblings(nodeB));
        
        // Marry 3 nodes. It exists 2 undirected links and it must create only 1
        nodesToMarry.add(nodeD);
        numLinks = countLinks(graph);
        graph.marry(nodesToMarry);
        assertEquals(numLinks + 1, countLinks(graph));
        assertEquals(2, graph.getNumSiblings(nodeA));
        assertEquals(2, graph.getNumSiblings(nodeB));
        assertEquals(2, graph.getNumSiblings(nodeD));
        assertTrue(graph.getSiblings(nodeA).contains(nodeD));
    }
    
    /**
     * @return Number of links of the graph
     */
    private int countLinks(Graph<String> graph) {
        List<String> nodes = graph.getNodes();
        int numLinks = 0;
        if (graph.hasExplicitLinks()) {
            for (String node : nodes) {
                numLinks += graph.getNumLinks(node);
            }
        } else {
            for (String node : nodes) {
                numLinks += graph.getNumChildren(node) + graph.getNumParents(node) + graph.getNumSiblings(node);
            }
        }
        numLinks /= 2;
        return numLinks;
    }
    
}
