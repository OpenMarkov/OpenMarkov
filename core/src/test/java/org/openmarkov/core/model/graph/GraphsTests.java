/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.graph;


/**
 * Auxiliary class that create a graph for test.
 */
public class GraphsTests {
    
    /**
     * Create a <code>Graph</code> for test in package
     * <code>tests.openmarkov.graphs</code>.<p>
     * Graph description:<ul>
     * <li> Nodes are created in this order: nodeA, nodeB, nodeC, nodeD
     * <li> The object in each node is an <code>String</code>:
     * nodeA("A"), nodeB("B"), nodeC("C"), nodeD("D")
     * <li> Directed links: A->B, B->C
     * <li> Undirected links: B--D
     * </ul>
     *
     * @return <code>Graph</code>
     */
    public static Graph<String> createTestGraph() {
        Graph<String> graph = new Graph<>();
        String nodeA = "A";
        String nodeB = "B";
        String nodeC = "C";
        String nodeD = "D";
        
        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addNode(nodeD);
        
        graph.addLink(nodeA, nodeB, true);
        graph.addLink(nodeB, nodeC, true);
        graph.addLink(nodeB, nodeD, false);
        
        return graph;
    }
    
}
