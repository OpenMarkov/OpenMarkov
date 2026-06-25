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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Uses <code>GraphsTests</code> to create a simple <code>Graph</code>.
 * Test elementary methods in <code>Node</code> class.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NodeTest {
    
    private Graph<String> graph;
    
    private List<String> nodes;
    
    private String nodeA, nodeB, nodeC, nodeD;
    
    @BeforeEach public void setUp() {
        graph = GraphsTests.createTestGraph();
        nodes = graph.getNodes();
        nodeA = nodes.get(0);
        nodeB = nodes.get(1);
        nodeC = nodes.get(2);
        nodeD = nodes.get(3);
    }
    
    @Test public void testNode() {
        // Test created nodes in GraphsTest.createTestGraph()
        assertEquals(4, nodes.size());
        assertEquals("A", nodeA);
        assertEquals("B", nodeB);
        assertEquals("C", nodeC);
        assertEquals("D", nodeD);
    }
    
    @Test public void testIsChild() {
        assertTrue(graph.isChild(nodeB, nodeA));
        assertTrue(graph.isChild(nodeC, nodeB));
    }
    
    @Test public void testIsParent() {
        assertTrue(graph.isParent(nodeA, nodeB));
        assertTrue(graph.isParent(nodeB, nodeC));
    }
    
    @Test public void testIsSibling() {
        assertTrue(graph.isSibling(nodeB, nodeD));
        assertTrue(graph.isSibling(nodeD, nodeB));
    }
    
    @Test public void testGetNeighbors() {
        List<String> neighborsB = graph.getNeighbors(nodeB);
        assertEquals(3, neighborsB.size());
        assertTrue(neighborsB.contains(nodeA));
        assertTrue(neighborsB.contains(nodeC));
        assertTrue(neighborsB.contains(nodeD));
    }
    
    @Test public void testGetLinks() {
        graph.makeLinksExplicit(false);
        List<Link<String>> links = graph.getLinks(nodeB);
        assertEquals(3, links.size());
        int directed = 0;
        int undirected = 0;
        for (Link<String> link : links) {
            assertTrue(link.contains(nodeB));
            if (link.isDirected()) {
                directed++;
            } else {
                undirected++;
                assertTrue(link.contains(nodeD));
            }
        }
        assertEquals(2, directed);
        assertEquals(1, undirected);
    }
    
}
