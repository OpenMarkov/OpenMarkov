/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author mluque It is the type of 'nodesHashMaps'. It contains a
 * {@code LinkedHashMap} from {@code NodeType} to
 * {@code NodesHashMapType}.
 */
public class NodeTypeDepot {
    private final LinkedHashMap<NodeType, NodesHashMap> nodesHashMaps;
    
    public NodeTypeDepot() {
        nodesHashMaps = new LinkedHashMap<>();
        // create a linkedHashMap for each type of nodes
        for (NodeType type : NodeType.values()) {
            nodesHashMaps.put(type, new NodesHashMap());
        }
    }
    
    /** @return the total number of nodes across all node types */
    public int getNumNodes() {
        int numNodes = 0;
        for (NodesHashMap hashMap : nodesHashMaps.values()) {
            numNodes = numNodes + hashMap.size();
        }
        return numNodes;
    }
    
    /** @return the number of nodes of the specified type */
    public int getNumNodes(NodeType nodeType) {
        return nodesHashMaps.get(nodeType).size();
    }
    
    /** @return all nodes across all node types */
    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<>(getNumNodes());
        for (NodesHashMap hashMap : nodesHashMaps.values()) {
            nodes.addAll(hashMap.values());
        }
        return nodes;
    }
    
    /**
     * @param nodeType the type of nodes whose potentials are returned
     * @return all potentials from nodes of the given type
     */
    public List<Potential> getPotentialsByType(NodeType nodeType) {
        NodesHashMap nodesType = nodesHashMaps.get(nodeType);
        List<Potential> potentials = new ArrayList<>();
        for (Node node : nodesType.values()) {
            potentials.addAll(node.getPotentials());
        }
        return potentials;
    }
    
    /**
     * @param nodeType Node type
     *
     * @return All the nodes of certain kind
     */
    public List<Node> getNodes(NodeType nodeType) {
        return new ArrayList<>(nodesHashMaps.get(nodeType).values());
    }
    


    /**
     * @param nodeType the type of node to look up
     * @param variable the variable associated with the node
     * @return the node, or {@code null} if not found
     */
    public Node getNode(NodeType nodeType, Variable variable) {
        return nodesHashMaps.get(nodeType).get(variable);
    }
    
    /**
     * Finds a node by variable name, searching across all node types.
     *
     * @param nameOfVariable the variable name to search for
     * @return the matching node, or {@code null} if not found
     */
    public Node getNode(String nameOfVariable) {
        for (NodeType nodeType : NodeType.values()) {
            Collection<Node> nodes = nodesHashMaps.get(nodeType).values();
            for (Node node : nodes) {
                if (node.getVariable().getName().contentEquals(nameOfVariable)) {
                    return node;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds a node by variable reference, searching across all node types.
     *
     * @param variable the variable to search for
     * @return the matching node, or {@code null} if not found
     */
    public Node getNode(Variable variable) {
        for (NodesHashMap nodesHashMap : this.nodesHashMaps.values()) {
            Node node = nodesHashMap.get(variable);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * @param nameOfVariable {@code String}
     * @param nodeType       {@code NodeType}
     *
     * @return The node with {@code nameOfVariable} and
     * {@code kindOfNode} if exists otherwhise null
     */
    public Node getNode(String nameOfVariable, NodeType nodeType) {
        for (Node node : nodesHashMaps.get(nodeType).values()) {
            if (node.getVariable().getName().contentEquals(nameOfVariable)) {
                return node;
            }
        }
        return null;
    }
    
    /** Registers a node in the depot under its node type. */
    public void addNode(Node node) {
        nodesHashMaps.get(node.getNodeType()).put(node.getVariable(), node);
    }
    
    /** Removes a node from the depot by its node type and variable. */
    public void removeNode(Node node) {
        NodeType nodeKindValue = node.getNodeType();
        Variable variable = node.getVariable();
        NodesHashMap nodesMap = nodesHashMaps.get(nodeKindValue);
        nodesMap.remove(variable);
    }
    
    /** Removes the node associated with the given variable, regardless of its type. */
    public void removeNode(Variable variable) {
        for (NodesHashMap nodes : nodesHashMaps.values()) {
            if (nodes.get(variable) != null) {
                nodes.remove(variable);
                break;
            }
        }
    }
    
    /** @return the total number of potentials across all nodes */
    public int getNumPotentials() {
        int numPotentials = 0;
        for (NodesHashMap linkedHasMap : nodesHashMaps.values()) {
            for (Node node : linkedHasMap.values()) {
                numPotentials += node.getNumPotentials();
            }
        }
        return numPotentials;
    }
    
    /**
     * @author mluque Contains a {@code LinkedHashMap} from
     * {@code Variable} to {@code Node}.
     */
    private static class NodesHashMap {
        final LinkedHashMap<Variable, Node> nodesHashMap;
        
        NodesHashMap() {
            nodesHashMap = new LinkedHashMap<>();
        }
        
        public Node get(Variable variable) {
            return nodesHashMap.get(variable);
        }
        
        public void put(Variable variable, Node node) {
            nodesHashMap.put(variable, node);
        }
        
        public int size() {
            return nodesHashMap.size();
        }
        
        public Collection<Node> values() {
            return nodesHashMap.values();
        }
        
        public void remove(Variable variable) {
            nodesHashMap.remove(variable);
        }
    }
    
}
