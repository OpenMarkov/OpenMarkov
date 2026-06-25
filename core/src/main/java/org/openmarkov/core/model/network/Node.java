/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.java.cloneUtils.CloneUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.openmarkov.core.model.network.VariableType.*;

/**
 * A probabilistic node has a set of conditional probabilities, one variable,
 * etc. The structural aspect of the underlying  graph is in the node
 * associated.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @version 1.0
 * @see Node
 * @see ProbNet
 * @since OpenMarkov 1.0
 */
public class Node implements Cloneable, ClassLocalizable {
    
    // Constants
    public static final double DEFAULT_RELEVANCE = 5.0;
    /**
     * Additional properties read from disk that have no direct mapping to
     * fields of this object (e.g. format-specific metadata).
     * Exposed as an unmodifiable view via {@link #getAdditionalProperties()};
     * mutated through {@link #setAdditionalProperties(Map)} and
     * {@link #putAdditionalProperty(String, String)}.
     */
    private final Map<String, String> additionalProperties;
    
    // Attributes/
    /**
     * Node type
     */
    private NodeType nodeType;
    
    /**
     * Network
     */
    protected final transient ProbNet probNet;
    
    /**
     * Potentials associated to this node (CPTs, utility functions, link restrictions).
     * Wrapped in a synchronized list so that individual operations ({@link #addPotential},
     * {@link #removePotential}, {@link #clearPotentials}) are thread-safe.
     * Compound operations ({@link #setPotential}, {@link #setPotentials}) use an explicit
     * {@code synchronized(potentials)} block to guarantee atomicity.
     * {@link #getPotentials()} returns a defensive copy, so external iteration is safe.
     */
    @NotNull
    protected final List<Potential> potentials;
    
    /**
     * The variable associated
     */
    protected Variable variable;
    private final int hashCode;
    /**
     * Purpose of node
     */
    private String purpose = "";
    /**
     * Relevance of node
     */
    private double relevance = DEFAULT_RELEVANCE;
    /**
     * Comment about node definition
     */
    private String comment = "";
    
    //TODO OOPN start
    private PolicyType policyType = PolicyType.OPTIMAL;
    //TODO OOPN end
    /**
     * Indicates whether this node is an input parameter
     */
    private boolean isInput = false;
    private double coordinateX = 100;
    private double coordinateY = 100;
    private boolean alwaysObserved = false;
    
    // 24/10/2020
    /**
     * Event nodes property which indicates its behaviour when triggered by another event:
     * When true a new instance of the event with its timestamp is queued (the event may be several times in the queue)
     * When false there is only an instance of the event in the queue at maximum:
     * if the event is in the queue, it is added to it. Otherwise the timestamp of the event is modified.
     */
    private boolean alwaysAppend = false;
    
    // Constructor
    
    /**
     * @param probNet  {@code ProbNet}
     * @param variable {@code Variable}
     * @param nodeType {@code NodeType}
     */
    public Node(ProbNet probNet, Variable variable, NodeType nodeType) {
        this.probNet = probNet;
        this.variable = variable;
        if (nodeType == NodeType.UTILITY) {
            this.variable.setVariableType(NUMERIC);
        }
        if (nodeType == NodeType.EVENT) {
            this.variable.setVariableType(EVENT);
        }
        this.nodeType = nodeType;
        potentials = Collections.synchronizedList(new ArrayList<>());
        additionalProperties = new LinkedHashMap<>();  // mutable backing map
        hashCode = 31 * variable.hashCode() + 17 * nodeType.hashCode();
    }
    
    /**
     * Copy Constructor for the GUI
     *
     * @param node Node
     */
    public Node(Node node) {
        this.probNet = node.getProbNet();
        this.variable = node.getVariable();
        this.nodeType = node.getNodeType();
        potentials = Collections.synchronizedList(new ArrayList<>(node.getPotentials()));
        additionalProperties = new LinkedHashMap<>(node.getAdditionalProperties());
        alwaysObserved = node.isAlwaysObserved();
        hashCode = 31 * variable.hashCode() + 17 * nodeType.hashCode();
    }
    
    //Methods
    
    /**
     * @return The {@code Variable} associated to this
     * {@code node}.
     */
    public Variable getVariable() {
        return variable;
    }
    
    /**
     * Sets a new variable and updates the reference in ProbNet
     *
     * @param newVariable New variable
     */
    public void setVariable(Variable newVariable) {
        Variable oldVariable = this.variable;
        this.variable = newVariable;
        this.probNet.updateVariable(oldVariable);
    }
    
    /**
     * Replaces all additional properties with the entries from the given map.
     *
     * @param additionalProperties new properties; {@code null} is treated as empty
     */
    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties.clear();
        if (additionalProperties != null) {
            this.additionalProperties.putAll(additionalProperties);
        }
    }
    
    /**
     * Adds or replaces a single additional property.
     *
     * @param key   property name; must not be {@code null}
     * @param value property value
     */
    public void putAdditionalProperty(String key, String value) {
        this.additionalProperties.put(key, value);
    }
    
    /**
     * @return Variable name. {@code String}
     */
    public String getName() {
        return getVariable().getName();
    }
    
    /**
     * @return The base name of the variable (without temporal index).
     */
    public String getBaseName() {
        return getVariable().getBaseName();
    }
    
    /**
     * Replaces all potentials of this node with a single one.
     * The clear and add are performed atomically to prevent other threads
     * from observing an intermediate empty state.
     *
     * @param potential the new potential; must not be {@code null}
     */
    public void setPotential(Potential potential) {
        synchronized (potentials) {
            this.potentials.clear();
            addPotential(potential);
        }
    }
    
    /** Removes all potentials from this node. */
    public void clearPotentials() {
        this.potentials.clear();
    }
    
    /**
     * @param potential {@code Potential}
     */
    public void addPotential(Potential potential) {
        this.potentials.add(potential);
    }
    
    /**
     * Replaces all potentials of this node with the given list.
     * The clear and addAll are performed atomically to prevent other threads
     * from observing an intermediate empty state.
     *
     * @param potentials new list of potentials; {@code null} is treated as empty
     */
    public void setPotentials(List<Potential> potentials) {
        synchronized (this.potentials) {
            this.potentials.clear();
            if (potentials != null) {
                this.potentials.addAll(potentials);
            }
        }
    }
    
    /**
     * @param potential {@code Potential}
     *
     * @return {@code true} if {@code potentialList} contained the
     * specified element; otherwise {@code false}.
     */
    public boolean removePotential(Potential potential) {
        return potentials.remove(potential);
    }
    
    /**
     * @return {@code NodeType}
     */
    public NodeType getNodeType() {
        return nodeType;
    }
    
    /**
     * Changes the type of this node and updates the internal depot index.
     *
     * @param newNodeType the new node type
     */
    public void setNodeType(NodeType newNodeType) {
        NodeType oldNodeType = this.nodeType;
        if(oldNodeType == newNodeType){
            return;
        }
        // Remove node from NodeTypeDepot HashMap
        this.probNet.nodeDepot.removeNode(this);
        // Change of newNodeType
        this.nodeType = newNodeType;
        // Add node to NodeTypeDepot HashMap
        this.probNet.nodeDepot.addNode(this);
    }
    
    /**
     * @return An {@code ArrayList} cloned with all the potentials
     * associated to this {@code Node}
     */
    public @NotNull List<Potential> getPotentials() {
        return new ArrayList<>(potentials);
    }
    
    /**
     * @return the first potential in this node's list
     *
     * @throws ThereIsNoPotentialsInNodeException if the node has no potentials
     */
    public Potential getFirstPotential() throws ThereIsNoPotentialsInNodeException {
        if (this.potentials.isEmpty()) {
            throw new ThereIsNoPotentialsInNodeException(this);
        }
        return potentials.getFirst();
    }
    
    /**
     * @return Number of potentials. {@code int}
     */
    public int getNumPotentials() {
        return potentials.size();
    }
    
    /**
     * Returns an unmodifiable view of the additional properties.
     * Use {@link #setAdditionalProperties(Map)} to bulk-replace or
     * {@link #putAdditionalProperty(String, String)} to add a single entry.
     *
     * @return unmodifiable map; never {@code null}
     */
    public Map<String, String> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }
    
    /**
     * @return probNet. {@code ProbNet}
     */
    public ProbNet getProbNet() {
        return probNet;
    }
    
    /** @return all links (directed and undirected) incident to this node */
    public List<Link<Node>> getLinks() {
        return probNet.getLinks(this);
    }
    
    /** @return the child nodes of this node (targets of outgoing directed links) */
    public @NotNull List<Node> getChildren() {
        return probNet.getChildren(this);
    }
    
    /** @return the parent nodes of this node (sources of incoming directed links) */
    public @NotNull List<Node> getParents() {
        return probNet==null?Collections.emptyList():probNet.getParents(this);
    }
    
    /** @return the sibling nodes of this node (connected by undirected links) */
    public List<Node> getSiblings() {
        return probNet.getSiblings(this);
    }
    
    /** @return all neighbor nodes (parents, children, and siblings) */
    public List<Node> getNeighbors() {
        return probNet.getNeighbors(this);
    }
    
    /** @return the number of child nodes */
    public int getNumChildren() {
        return probNet.getNumChildren(this);
    }
    
    /** @return the number of parent nodes */
    public int getNumParents() {
        return probNet.getNumParents(this);
    }
    
    /** @return the number of sibling nodes */
    public int getNumSiblings() {
        return probNet.getNumSiblings(this);
    }
    
    /** @return the number of neighbor nodes */
    public int getNumNeighbors() {
        return probNet.getNumNeighbors(this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} is parent of {@code this} node
     */
    public boolean isParent(Node node) {
        return probNet.isParent(node, this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} is child of {@code this} node
     */
    public boolean isChild(Node node) {
        return probNet.isChild(node, this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} and {@code this} are siblings
     */
    public boolean isSibling(Node node) {
        return probNet.isSibling(node, this);
    }
    
    /**
     * @param node {@code Node}
     *
     * @return True if {@code node} and {@code this} are neighbors
     */
    public boolean isNeighbor(Node node) {
        return probNet.isNeighbor(node, this);
    }
    
    //	@Override
    //	public int hashCode() {
    //		return super.hashCode();
    //	}
    @Override public boolean equals(Object obj) {
        boolean equals = obj instanceof Node;
        if (equals) {
            Node otherNode = (Node) obj;
            equals = variable.equals(otherNode.variable) && probNet.equals(otherNode.probNet) && nodeType == otherNode.nodeType;
        }
        return equals;
    }
    
    @Override public int hashCode() {
        return hashCode;
    }
    
    public String toString() {
        String out = variable.getName() + " (" + switch (nodeType) {
            case CHANCE -> "Chance";
            case DECISION -> "Decision";
            case UTILITY -> "Utility";
            case EVENT -> "Event";
            case SV_SUM -> "SV_SUM";
            case SV_PRODUCT -> "SV_PRODUCT";
        };
        out += "): ";
        List<Node> parents = getParents();
        List<Node> children = getChildren();
        List<Node> siblings = getSiblings();
        List<Node> neighbors = getNeighbors();
        if (neighbors.isEmpty()) {
            out += "No neighbors - ";
        } else {
            if (!parents.isEmpty()) {
                out += ((parents.size() == 1) ? "Parent" : "Parents") + ": {";
                for (int i = 0; i < parents.size(); i++) {
                    Node parent = parents.get(i);
                    out += (parent.getVariable());
                    if (i < parents.size() - 1) {
                        out += (", ");
                    }
                }
                out += ("} - ");
            }
            if (!children.isEmpty()) {
                out += ((children.size() == 1) ? "Child" : "Children") + ": {";
                for (int i = 0; i < children.size(); i++) {
                    Node child = children.get(i);
                    out += (child.getVariable());
                    if (i < children.size() - 1) {
                        out += (", ");
                    }
                }
                out += ("} - ");
            }
            if (!siblings.isEmpty()) {
                out += ((siblings.size() == 1) ? "Sibling" : "Siblings") + ": {";
                for (int i = 0; i < siblings.size(); i++) {
                    Node sibling = siblings.get(i);
                    out += (sibling.getVariable());
                    if (i < siblings.size() - 1) {
                        out += (", ");
                    }
                }
                out += ("} - ");
            }
        }
        int numPotentials = potentials.size();
        if (numPotentials > 0) {
            out += ((numPotentials == 1) ? "Potential: " : "Potentials (" + numPotentials + "): {");
            for (int i = 0; i < potentials.size(); ++i) {
                out += (potentials.get(i).toShortString());
                if (i < potentials.size() - 1) {
                    out += (", ");
                }
            }
            if (numPotentials > 1) {
                out += ("}");
            }
        } else {
            out += ("No potentials");
        }
        return out;
    }
    
    
    /**
     * @return {@code String}
     */
    public String getPurpose() {
        return purpose;
    }
    
    /**
     * @param purpose {@code String}
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    /**
     * @return {@code double}
     */
    public double getRelevance() {
        return relevance;
    }
    
    /**
     * @param relevance {@code double}
     */
    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }
    
    /**
     * @return the comment. {@code String}
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * @param comment the comment to set. {@code String}
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * @return the modelType. {@code PolicyType}
     */
    public PolicyType getPolicyType() {
        return policyType;
    }
    
    /**
     * @param policyType the modelType to set. {@code PolicyType}
     */
    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }
    
    /**
     * @return {@code true} if it is a decision node with a non uniform potential.
     * {@code boolean}
     */
    public boolean hasPolicy() {
        return nodeType == NodeType.DECISION && !potentials.isEmpty();
    }
    
    
    /**
     * @return true if the variable is a supervalue node. False if does not
     */
    public boolean isSuperValueNode() {
        Node utilityNode = probNet.getNode(variable);
        int numOfUtilityParents = 0;
        for (Node parent : probNet.getParents(utilityNode)) {
            if (parent.getNodeType() == NodeType.UTILITY) {
                //if the node has two or more utility parents then is a super value node
                if ((numOfUtilityParents++) >= 1) {
                    return true;
                }
            }
            
        }
        return false;
    }
    
    /**
     * Returns the subset of this node's parents that are utility nodes.
     *
     * @return a list with utility parents
     */
    public List<Node> getUtilityParents() {
        List<Node> utilityParents = new ArrayList<>();
        for (Node parent : getParents()) {
            if (parent.getNodeType() == NodeType.UTILITY) {
                utilityParents.add(parent);
            }
        }
        return utilityParents;
    }
    
    /**
     * @return true if a node has only utility parents
     */
    public boolean checkOnlyUtilityparents() {
        return getUtilityParents().size() == getParents().size();
    }
    
    /**
     * @return True if the node has only numerical parents
     */
    public boolean onlyNumericalParents() {
        List<Node> numericalParents = new ArrayList<>();
        List<Node> finiteStatesOrDiscretizedParents = new ArrayList<>();
        
        for (Node parent : getParents()) {
            if (parent.getVariable().getVariableType() == NUMERIC) {
                numericalParents.add(parent);
            } else if (parent.getVariable().getVariableType() == FINITE_STATES
                    || parent.getVariable().getVariableType() == DISCRETIZED) {
                finiteStatesOrDiscretizedParents.add(parent);
            }
        }
        return !numericalParents.isEmpty() && finiteStatesOrDiscretizedParents.isEmpty();
    }
    
    /**
     * Returns the isInput.
     *
     * @return the isInput.
     */
    public boolean isInput() {
        return isInput;
    }
    
    /**
     * Sets the isInput.
     *
     * @param isInput the isInput to set.
     */
    public void setInput(boolean isInput) {
        this.isInput = isInput;
    }
    
    /**
     * @return the alwaysObserved
     */
    public boolean isAlwaysObserved() {
        return alwaysObserved;
    }
    
    /**
     * @param alwaysObserved the alwaysObserved to set
     */
    public void setAlwaysObserved(boolean alwaysObserved) {
        this.alwaysObserved = alwaysObserved;
    }
    
    // 24/10/2020 - event nodes queue behaviour
    /**
     * Returns true if alwaysAppend is set.
     * alwaysAppend is an event nodes property which indicates its behaviour when triggered by another event:
     * When true a new instance of the event with its timestamp is queued (the event may be several times in the queue)
     * When false there is only an instance of the event in the queue at maximum:
     * if the event is in the queue, it is added to it. Otherwise the timestamp of the event is modified.
     */
    public boolean isAlwaysAppend() {
        return alwaysAppend;
    }
    
    /**
     * Sets the property alwaysAppend of event nodes.
     * alwaysAppend is an event nodes property which indicates its behaviour when triggered by another event:
     * When true a new instance of the event with its timestamp is queued (the event may be several times in the queue)
     * When false there is only an instance of the event in the queue at maximum:*
     * @param alwaysAppend - true if the event is always queued when triggered by another event.
     */
    public void setAlwaysAppend(boolean alwaysAppend) {
        this.alwaysAppend = alwaysAppend;
    }
    
    public double getCoordinateX() {
        return coordinateX;
    }
    
    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }
    
    public double getCoordinateY() {
        return coordinateY;
    }
    
    public void setCoordinateY(double coordinateY) {
        this.coordinateY = coordinateY;
    }
    
    public void setCoordinates(double coordinateX, double coordinateY) {
        this.setCoordinateX(coordinateX);
        this.setCoordinateY(coordinateY);
    }
    
    /**
     * Creates a clone of this node bound to the given {@code ProbNet}, copying
     * all metadata (coordinates, purpose, relevance, etc.) but not potentials.
     *
     * @param probNet the target network for the cloned node
     *
     * @return a new {@code Node} with cloned variable and copied properties
     */
    public Node clone(ProbNet probNet) {
        Variable newVariable = CloneUtils.safeClone(this.variable);
        if (this.getNodeType() == NodeType.UTILITY) {
            for (Criterion criterion : probNet.getDecisionCriteria()) {
                if (criterion.getCriterionName().equals(this.variable.getDecisionCriterion().getCriterionName())) {
                    newVariable.setDecisionCriterion(criterion);
                    break;
                }
            }
        }
        Node newNode = new Node(probNet, newVariable, this.getNodeType());
        newNode.coordinateX = this.coordinateX;
        newNode.setCoordinateX(this.getCoordinateX());
        newNode.setCoordinateY(this.getCoordinateY());
        newNode.setPurpose(this.getPurpose());
        newNode.setRelevance(this.getRelevance());
        newNode.setComment(this.getComment());
        newNode.setAdditionalProperties(this.additionalProperties);
        newNode.setAlwaysObserved(this.isAlwaysObserved());
        return newNode;
    }
    
    /**
     * @return the first potential, or {@code null} if no potentials are assigned
     */
    public Potential getPotential() {
        if (potentials.isEmpty()) {
            return null;
        } else {
            return getPotentials().getFirst();
        }
    }
    
    /**
     * @return the last potential in this node's list
     */
    //TODO: very possibly removal
    public Potential getPreviousPotential() {
        int x = getPotentials().size() - 1;
        return getPotentials().get(x);
    }
    
    public boolean temporalEvolutionCanBeExecuted() {
        return this.getVariable().isTemporal() &&
                !(
                        this.getNodeType() == NodeType.CHANCE
                                && this.getVariable().getVariableType() != FINITE_STATES
                );
    }
    
    public @Nullable Variable nextSlice() {
        if (!this.getVariable().isTemporal()) {
            return null;
        }
        return this.getProbNet().getShiftedVariable(this.getVariable(), 1);
    }
    
    public String removeAdditionalProperty(String string) {
        return this.additionalProperties.remove(string);
    }
}
