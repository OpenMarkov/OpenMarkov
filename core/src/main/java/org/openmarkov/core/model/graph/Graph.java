/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.graph;

import org.openmarkov.core.model.network.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements the minimal set of methods for creating
 * a graph and inserting nodes and links.
 * <p>
 * Every link has two complementary representations, and the graph keeps both of them in step at
 * all times:
 * <ul>
 * <li>the <em>implicit</em> one — the lists of parents, children and siblings of each node — which
 * answers the neighbourhood questions ({@code getParents}, {@code isChild}, {@code existsPath}…)
 * without visiting any link object; and</li>
 * <li>the <em>explicit</em> one — an object of class {@link Link} per link — which is what carries
 * whatever hangs from a link: its restrictions potential, its revealing conditions, its label.</li>
 * </ul>
 * The explicit representation used to be built lazily, the first time a link was asked for, so
 * that a graph that only ever needed its topology paid nothing for it. That made the plain getters
 * mutate the graph and, worse, made the behaviour of the graph depend on whether anyone had
 * happened to read a link before: whether adding a link twice was detectable, whether undoing the
 * removal of a link kept its restrictions, and so on. Links are now explicit from the start, so
 * there is a single, mode-independent behaviour.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @author ibermejo
 * @version 1.1
 * invariant Two different nodes can not represent the same object
 * @see Node
 * @see Link
 * @since OpenMarkov 1.0
 */
public class Graph<T> {

    // Attributes
    private final List<T> nodes;
    
    private final Map<T, List<Link<T>>> nodeLinks;
    
    private final Map<T, List<T>> nodeChildren;
    private final Map<T, List<T>> nodeParents;
    private final Map<T, List<T>> nodeSiblings;
    
    // Constructor
    public Graph() {
        this.nodes = new ArrayList<>();
        this.nodeLinks = new HashMap<>();
        this.nodeChildren = new HashMap<>();
        this.nodeParents = new HashMap<>();
        this.nodeSiblings = new HashMap<>();
    }
    
    // Methods
    public List<T> getChildren(T node) {
        return (nodeChildren.containsKey(node)) ? new ArrayList<>(nodeChildren.get(node)) : new ArrayList<>();
    }
    
    public List<T> getParents(T node) {
        return (nodeParents.containsKey(node)) ? new ArrayList<>(nodeParents.get(node)) : new ArrayList<>();
    }
    
    public List<T> getSiblings(T node) {
        return (nodeSiblings.containsKey(node)) ? new ArrayList<>(nodeSiblings.get(node)) : new ArrayList<>();
    }
    
    public int getNumChildren(T node) {
        return (nodeChildren.containsKey(node)) ? nodeChildren.get(node).size() : 0;
    }
    
    public int getNumParents(T node) {
        return (nodeParents.containsKey(node)) ? nodeParents.get(node).size() : 0;
    }
    
    public int getNumSiblings(T node) {
        return (nodeSiblings.containsKey(node)) ? nodeSiblings.get(node).size() : 0;
    }
    
    public int getNumNeighbors(T node) {
        return getNumParents(node) + getNumChildren(node) + getNumSiblings(node);
    }
    
    /**
     * @param node the node whose neighbours are wanted
     *
     * @return the children, the parents and the siblings of {@code node}, in that order. A node
     * joined to {@code node} by more than one link — for instance a 2-cycle, {@code node --> other}
     * plus {@code other --> node} — appears once per link, so that the size of this list always
     * matches {@link #getNumNeighbors}.
     */
    public List<T> getNeighbors(T node) {
        List<T> neighbors = new ArrayList<>();
        if (nodeChildren.containsKey(node))
            neighbors.addAll(nodeChildren.get(node));
        if (nodeParents.containsKey(node))
            neighbors.addAll(nodeParents.get(node));
        if (nodeSiblings.containsKey(node))
            neighbors.addAll(nodeSiblings.get(node));
        
        return neighbors;
    }
    
    /**
     * Returns if node1 is child of node2
     *
     * @param node1 First node
     * @param node2 Second node
     *
     * @return True if node1 is child of node2
     */
    public boolean isChild(T node1, T node2) {
        return nodeChildren.containsKey(node2) && nodeChildren.get(node2).contains(node1);
    }
    
    /**
     * Returns if node1 is parent of node2
     *
     * @param node1 First node
     * @param node2 Second node
     *
     * @return True if node1 is parent of node2
     */
    public boolean isParent(T node1, T node2) {
        return nodeParents.containsKey(node2) && nodeParents.get(node2).contains(node1);
    }
    
    /**
     * Returns whether node1 and node2 are siblings
     *
     * @param node1 First node
     * @param node2 Second node
     *
     * @return True if node1 and node2 are siblings
     */
    public boolean isSibling(T node1, T node2) {
        return nodeSiblings.containsKey(node2) && nodeSiblings.get(node2).contains(node1);
    }
    
    /**
     * Returns whether node1 and node2 are neighbors
     *
     * @param node1 First node
     * @param node2 Second node
     *
     * @return True if node1 and node2 are neighbors
     */
    public boolean isNeighbor(T node1, T node2) {
        return isParent(node1, node2) || isChild(node1, node2) || isSibling(node1, node2);
    }
    
    /**
     * @return always {@code true}. Links used to become explicit only on demand; they are explicit
     * from the start now, so there is nothing left to ask about.
     *
     * @deprecated there is no longer a graph without explicit links.
     */
    @Deprecated public boolean hasExplicitLinks() {
        return true;
    }
    
    /**
     * @return Number of nodes in the graph
     */
    public int getNumNodes() {
        return getNodes().size();
    }
    
    
    private void addLink(Link<T> link) {
        nodeLinks.get(link.getFrom()).add(link);
        if (!link.getFrom().equals(link.getTo())) {
            nodeLinks.get(link.getTo()).add(link);
        }
    }

    /**
     * Inserts a link between {@code node1} and {@code node2}. {@code node1} and {@code node2} belongs to this
     * <p>
     * A link that already exists is <strong>not</strong> reused: adding it twice leaves two link
     * objects joining the same pair of nodes. That is deliberate — forbidding it is the job of the
     * {@code DistinctLinks} constraint, which each kind of network switches on or off.
     *
     * @param node1    {@code Node}
     * @param node2    {@code Node}
     * @param directed {@code boolean}
     *                 {@code graph}
     *
     * @return the {@link Link} just created
     */
    public Link<T> addLink(T node1, T node2, boolean directed) {
        Link<T> newLink = new Link<>(node1, node2, directed);
        addLink(newLink);
        addImplicitLink(node1, node2, directed);

        return newLink;
    }

    /**
     * Removes a link between two nodes. Removing one that does not exist does nothing.
     *
     * @param node1    {@code Node}
     * @param node2    {@code Node}
     * @param directed {@code boolean}
     */
    public void removeLink(T node1, T node2, boolean directed) {
        Link<T> link = getLink(node1, node2, directed);
        if (link != null) {
            removeLink(link);
        }
    }

    /**
     * Removes a link, in both of its representations.
     *
     * @param link Link&#60;T&#62;
     */
    public void removeLink(Link<T> link) {
        T node1 = link.getFrom();
        T node2 = link.getTo();

        List<Link<T>> linksOfNode1 = nodeLinks.get(node1);
        if (linksOfNode1 != null) {
            linksOfNode1.remove(link);
        }
        List<Link<T>> linksOfNode2 = nodeLinks.get(node2);
        if (linksOfNode2 != null) {
            linksOfNode2.remove(link);
        }

        removeImplicitLink(node1, node2, link.isDirected());
    }
    
    /**
     * Returns the link between {@code node1} and {@code node2}.
     *
     * @param node1    {@code Node}
     * @param node2    {@code Node}
     * @param directed {@code boolean}
     *
     * @return The link between node1 and node2, if it exists, otherwise
     * returns {@code null}
     */
    public Link<T> getLink(T node1, T node2, boolean directed) {
        List<Link<T>> linksNode = nodeLinks.get(node1);
        if (linksNode != null) {
            for (Link<T> link : linksNode) {
                // Two disjuncts, parenthesised for clarity: a matching directed link, or a matching
                // undirected one. For directed links getFrom()==node1 is also checked (added
                // 06/01/2020) so a self-loop does not match any link merely ending in node2.
                if ((directed && link.isDirected()
                        && link.getFrom().equals(node1) && link.getTo().equals(node2))
                        || (!directed && !link.isDirected() && link.contains(node2))) {
                    return link;
                }
            }
            
        }
        return null;
        
    }
    
    /**
     * Does nothing: the links of a graph are explicit from the moment they are added.
     *
     * @param createLabelledLinks {@code boolean}. Ignored. Labelled links, when needed, are built
     *                            by whoever needs them — see {@link LabelledLink}.
     *
     * @deprecated there is nothing left to materialise.
     */
    @Deprecated @SuppressWarnings("unused")
    public void makeLinksExplicit(boolean createLabelledLinks) {
        // Nothing to do: see the class documentation.
    }

    /**
     * Removes every link between {@code node} and its neighbors, in both representations.
     *
     * @param node {@code Node}
     */
    public void removeLinks(T node) {

        List<Link<T>> linksNode = nodeLinks.get(node);
        if (linksNode != null) {
            for (Link<T> link : new ArrayList<>(linksNode)) {
                removeLink(link);
            }
        }

        // Every neighbour list is traversed through a copy. With a self-loop the node is its own
        // neighbour, so the loop body would otherwise remove an element from the very list being
        // iterated, cutting the traversal short and leaving the remaining neighbours pointing at a
        // node that no longer has any link to them.
        List<T> children = nodeChildren.get(node);
        if (children != null) {
            for (T child : new ArrayList<>(children))
                nodeParents.get(child).remove(node);
            children.clear();
        }

        List<T> parents = nodeParents.get(node);
        if (parents != null) {
            for (T parent : new ArrayList<>(parents))
                nodeChildren.get(parent).remove(node);
            parents.clear();
        }

        List<T> siblings = nodeSiblings.get(node);
        if (siblings != null) {
            for (T sibling : new ArrayList<>(siblings))
                nodeSiblings.get(sibling).remove(node);
            siblings.clear();
        }

    }
    
    /**
     * @return A clone of the list of nodes ({@code List} of
     * {@code Node}).
     */
    public List<T> getNodes() {
        return new ArrayList<>(nodes);
    }
    
    public List<Link<T>> getLinks(T node) {
        return nodeLinks.containsKey(node) ? new ArrayList<>(nodeLinks.get(node)) : new ArrayList<>();
    }

    public int getNumLinks(T node) {
        return nodeLinks.containsKey(node) ? nodeLinks.get(node).size() : 0;
    }

    /**
     * @return The links of the {@code Graph}, each one once.
     */
    public List<Link<T>> getLinks() {
        List<Link<T>> links = new ArrayList<>();
        for (T node : nodes) {
            for (Link<T> link : nodeLinks.get(node)) {
                if (link.getFrom().equals(node))
                    links.add(link);
            }
        }
        return links;
    }
    
    /**
     * {@code node1} and {@code node2} belongs to this graph. Otherwise this method always returns {@code false}.
     *
     * @param node1         {@code Node}.
     * @param node2         {@code Node}.
     * @param directed      {@code boolean}. If this parameter is true, this
     *                      method returns {@code true} only if there is a directed path;
     *                      otherwise, this method returns {@code true} if there is any path.
     * @param linksToIgnore the links to ignore
     *
     * @return {@code true} if it exists a path between node1 and node2
     * with a criterion to go from a node to another.
     */
    public boolean existsPath(T node1, T node2, boolean directed, List<Link<T>> linksToIgnore) {
        if ((node1 == null) || (node2 == null)) {
            return false;
        }
        if (node1 == node2) {
            return true;
        }
        // Honour the documented contract: a node that does not belong to this graph yields false
        // (rather than an ArrayIndexOutOfBoundsException from nodes.indexOf(...) == -1 below).
        if (nodes.indexOf(node1) < 0 || nodes.indexOf(node2) < 0) {
            return false;
        }
        int numNodes = nodes.size();
        boolean[] markedNodes = new boolean[numNodes];
        Deque<T> nodesToExpand = new ArrayDeque<>();

        // Mark node1 and put it in the list of nodes to be expanded
        nodesToExpand.push(node1);
        markedNodes[nodes.indexOf(node1)] = true;

        // The search walks links, not neighbours. It used to translate the links to ignore into
        // the neighbours they lead to and then walk the adjacency lists, which are lists of nodes:
        // ignoring one link therefore ignored the pair of nodes, and any second link joining that
        // same pair went with it. Two nodes joined both ways were a loop that NoLoops could not
        // see, because ignoring one of the two links hid the other one as well.
        while (!nodesToExpand.isEmpty()) {
            T expandingNode = nodesToExpand.pop(); // the top of the stack
            for (Link<T> link : getLinks(expandingNode)) {
                if (linksToIgnore.contains(link)) {
                    continue;
                }
                T neighbor = neighborThrough(link, expandingNode, directed);
                if (neighbor == null) {
                    continue;
                }
                if (neighbor.equals(node2)) {
                    return true; // node2 is in a path from node1
                }
                int neighborIndex = nodes.indexOf(neighbor);
                if (!markedNodes[neighborIndex]) {
                    nodesToExpand.push(neighbor);
                    markedNodes[neighborIndex] = true;
                }
            }
        }
        return false;
    }

    /**
     * The node reached from {@code node} by walking {@code link}, or {@code null} when the link
     * cannot be walked that way: a directed search only ever goes along a directed link, from its
     * origin to its destination, whereas an undirected one crosses any link in either sense.
     */
    private T neighborThrough(Link<T> link, T node, boolean directed) {
        if (directed) {
            return link.isDirected() && link.getFrom().equals(node) ? link.getTo() : null;
        }
        return link.getFrom().equals(node) ? link.getTo() : link.getFrom();
    }
    
    /**
     * Adds an undirected link between each pair of nodes in {@code nodeList} that were not siblings
     * yet. All nodes in {@code nodeList} belong to {@code this} graph.
     * <p>
     * Only <em>undirected</em> links are taken into account: a pair already joined by a directed
     * link gets an undirected one as well, so the two end up joined twice. Every caller either
     * works on an undirected (Markov) network or removes all the links beforehand, so this cannot
     * happen today; it is documented because a mixed graph would make it possible.
     *
     * @param nodeList {@code Collection} of nodes of this graph.
     */
    public void marry(Collection<T> nodeList) {
        int size = nodeList.size();
        List<T> nodes = new ArrayList<>(nodeList);
        for (int i = 0; i < size - 1; i++) {
            T node_i = nodes.get(i);
            for (int j = i + 1; j < size; j++) {
                T node_j = nodes.get(j);
                if (!isSibling(node_i, node_j)) {
                    addLink(node_i, node_j, false);
                }
            }
        }
    }
    
    /**
     * @param node {@code Node}
     */
    public void removeNode(T node) {
        removeLinks(node);
        nodes.remove(node);
        // B6: drop the removed node's own keys so the maps keep no stale (empty) entries.
        nodeLinks.remove(node);
        nodeChildren.remove(node);
        nodeParents.remove(node);
        nodeSiblings.remove(node);
    }
    
    /**
     * Adds an implicit link by setting cross references between the two nodes. Both nodes must belong to the same graph.
     *
     * @param node1    {@code Node}
     * @param node2    {@code Node}
     * @param directed {@code boolean}
     *
     */
    private void addImplicitLink(T node1, T node2, boolean directed) {
        if (directed) {
            if (!isChild(node2, node1)) {
                if (!nodeChildren.containsKey(node1))
                    nodeChildren.put(node1, new LinkedList<>());
                nodeChildren.get(node1).add(node2);
            }
            if (!isParent(node1, node2)) {
                if (!nodeParents.containsKey(node2))
                    nodeParents.put(node2, new LinkedList<>());
                nodeParents.get(node2).add(node1);
            }
        } else {
            if (!isSibling(node1, node2)) {
                if (!nodeSiblings.containsKey(node1))
                    nodeSiblings.put(node1, new LinkedList<>());
                if (!nodeSiblings.containsKey(node2))
                    nodeSiblings.put(node2, new LinkedList<>());
                nodeSiblings.get(node1).add(node2);
                if (!node1.equals(node2))   // B5: an undirected self-loop must add the sibling only once
                    nodeSiblings.get(node2).add(node1);
            }
        }
    }
    
    /**
     * Removes an implicit link by deleting cross references between the two
     * nodes. The two nodes must belong to the same graph
     *
     * @param node1    {@code Node}
     * @param node2    {@code Node}
     * @param directed {@code boolean}
     */
    private void removeImplicitLink(T node1, T node2, boolean directed) {
        if (directed) {
            List<T> children = nodeChildren.get(node1);
            if (children != null)
                children.remove(node2);
            List<T> parents = nodeParents.get(node2);
            if (parents != null)
                parents.remove(node1);
        } else {
            List<T> siblings = nodeSiblings.get(node1);
            if (siblings != null)
                siblings.remove(node2);
            siblings = nodeSiblings.get(node2);
            if (siblings != null)
                siblings.remove(node1);
        }
    }
    
    /**
     * @param node {@code T}
     */
    public void addNode(T node) {
        nodes.add(node);
        nodeLinks.put(node, new LinkedList<>());
    }

    /**
     * @return A {@code String} with:
     * <ol>
     * <li>Number of nodes.
     * <li>List of nodes. For each node calls {@code node.toString()}.
     * </ol>
     */
    public String toString() {
        String out = "Nodes (" + nodes.size() + "): \n" + nodes.stream()
                                                               .map(node -> node.toString() + "\n")
                                                               .collect(Collectors.joining());
        
        out += "Links: \n";
        for (Link<T> link : getLinks()) {
            out += link + "\n";
        }
        return out;
    }
    
}
