/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.List;

/**
 * Read-only view of a probabilistic network's graph structure and variables.
 *
 * <p>Algorithms that only need to traverse the graph topology (topological
 * ordering, independence tests, constraint checking) should accept a
 * {@code GraphNetwork} rather than the full {@link ProbNet}, making their
 * actual dependency explicit and enabling lighter-weight implementations.
 *
 * @see PotentialNetwork
 * @see ProbNet
 */
public interface GraphNetwork {

    // ---------------------------------------------------------------
    // Node access
    // ---------------------------------------------------------------

    List<Node> getNodes();
    List<Node> getNodes(NodeType nodeType);
    List<Node> getNodes(List<Variable> variables);
    int getNumNodes();
    int getNumNodes(NodeType nodeType);

    @Nullable Node getNode(String nameOfVariable);
    @Nullable Node getNode(Variable variable);
    Node getNode(String nameOfVariable, NodeType nodeType);

    // ---------------------------------------------------------------
    // Variable access
    // ---------------------------------------------------------------

    List<Variable> getVariables();
    List<Variable> getVariables(NodeType nodeType);
    List<String>   getVariablesNames();
    List<Variable> getNonUtilityVariables();
    @Nullable Variable getVariable(String variableName);

    boolean containsVariable(Variable variable);
    boolean containsVariable(String variableName);

    // ---------------------------------------------------------------
    // Adjacency queries
    // ---------------------------------------------------------------

    List<Node> getChildren(Node node);
    List<Node> getParents(Node node);
    List<Node> getSiblings(Node node);
    List<Node> getNeighbors(Node node);

    int getNumChildren(Node node);
    int getNumParents(Node node);
    int getNumSiblings(Node node);
    int getNumNeighbors(Node node);

    boolean isChild(Node node1, Node node2);
    boolean isParent(Node node1, Node node2);
    boolean isSibling(Node node1, Node node2);
    boolean isNeighbor(Node node1, Node node2);

    // ---------------------------------------------------------------
    // Link access
    // ---------------------------------------------------------------

    List<Link<Node>> getLinks();
    List<Link<Node>> getLinks(Node node);
    @Nullable Link<Node> getLink(Node node1, Node node2, boolean directed);
    int getNumLinks(Node node);
    boolean hasExplicitLinks();

    boolean existsPath(Node node1, Node node2, boolean directed, List<Link<Node>> linksToIgnore);

    // ---------------------------------------------------------------
    // Network type
    // ---------------------------------------------------------------

    NetworkType getNetworkType();
}
