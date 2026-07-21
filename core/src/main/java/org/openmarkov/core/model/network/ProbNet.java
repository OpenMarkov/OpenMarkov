/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.PNESupport;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@code ProbNet} stores {@code Node}s in an efficient manner.
 * It has the operations to manage {@code Variables, nodes} and {@code
 * Potentials}.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @author mpalacios
 * @author mluque
 * @version 1.0
 * @see org.openmarkov.core.model.graph
 * @see Node
 * @since OpenMarkov 1.0
 */
public class ProbNet implements PotentialNetwork, Cloneable, ClassLocalizable {
    
    /** Internal graph that holds the topology (nodes, directed/undirected links). */
    private final Graph<Node> graph = new Graph<>();
    
    /**
     * Descriptive and configuration metadata (name, comment, criteria,
     * agents, inference options, etc.).
     */
    private final NetworkMetadata metadata = new NetworkMetadata();
    /**
     * Nodes are stored in several HashMaps to accelerate the access. The type
     * of node determines the {@code HashMap} in which the node is stored.
     */
    protected final NodeTypeDepot nodeDepot;
    /**
     * Network type of this {@code ProbNet}.
     */
    private NetworkType networkType;
    /**
     * {@code ArrayList} of {@code Constraints} that defines this
     * {@code ProbNet}. This attribute is not frozen to allow conversions
     */
    private final TreeSet<PNConstraint> constraints;
    private final PNESupport pNESupport;
    
    /**
     * Potentials that have no associated variables (i.e. constant potentials).
     * Uses a concurrent set so that {@code addPotential}, {@code removeNode},
     * and the various {@code getPotentials*} getters can be called safely from
     * different threads without {@link ConcurrentModificationException}.
     */
    private final Set<Potential> constantPotentials;
    
    // Constructors
    
    /**
     * Creates a probabilistic network of the given type.
     *
     * <p>The {@link ConstraintViolatedException} thrown by {@link #setNetworkType} is
     * caught here because a freshly created, empty network always satisfies every
     * constraint imposed by any built-in {@link NetworkType}. If it were ever
     * thrown it would indicate a programming error, hence it is re-wrapped as
     * {@link UnreachableException} — which preserves the original cause and
     * stack trace — rather than propagating a checked exception that callers
     * cannot meaningfully handle at construction time.
     *
     * @param networkType the type that defines which constraints apply to this network
     */
    public ProbNet(NetworkType networkType) {
        this.pNESupport = new PNESupport(this);
        this.constraints = new TreeSet<>();
        this.nodeDepot = new NodeTypeDepot();
        this.constantPotentials = ConcurrentHashMap.newKeySet();
        try {
            this.setNetworkType(networkType);
        } catch (ConstraintViolatedException e) {
            throw new UnreachableException(e);
        }
    }
    
    /**
     * Creates a probabilistic network. NetworkTypeConstraint defines the
     * network type. If NetworkTypeConstraint is null the network type will be
     * Bayesian Network
     */
    public ProbNet() {
        this(BayesianNetworkType.getUniqueInstance());
    }
    
    /**
     * @param nodes list of {@code Node}s
     *
     * @return variables corresponding to the received nodes.
     * {@code List} of {@code Variable}
     */
    public static List<Variable> getVariables(Collection<Node> nodes) {
        List<Variable> variables = null;
        if (nodes != null) {
            variables = new ArrayList<>(nodes.size());
            for (Node node : nodes) {
                variables.add(node.getVariable());
            }
        }
        return variables != null ? variables : new ArrayList<>();
    }
    
    /**
     * Adds the variables in the received {@code Potential} to this
     * {@code MarkovNet}, creates links between those variables creating
     * cliques and assigns the {@code potential} to the conditioned
     * variable (the first one).
     *
     * @param projectedTablePotentials {@code ArrayList} of {@code Potential}s
     *
     * @return A Markov Network in witch potentials are used to create cliques.
     * ({@code ProbNet}).
     * Condition: At least one potential depends on at least one variable
     * (otherwise the network would have no node, and it would be
     * impossible to assign constant potentials)
     *
     * <p>Delegates to {@link ProbNetPotentialQueries#buildMarkovDecisionNetwork(ProbNet, Collection)}.
     */
    public ProbNet buildMarkovDecisionNetwork(Collection<? extends Potential> projectedTablePotentials) {
        return ProbNetPotentialQueries.buildMarkovDecisionNetwork(this, projectedTablePotentials);
    }
    
    /**
     * @param constraint {@code PNConstraint}
     */
    public void addConstraint(PNConstraint constraint) {
        constraints.add(constraint);
        pNESupport.addListener(constraint);
    }
    
    /**
     * @param constraint {@code PNConstraint}
     */
    public void removeConstraint(PNConstraint constraint) {
        if (constraints.remove(constraint)) {
            pNESupport.removeListener(constraint);
        }
    }
    
    /**
     * Checks all constraints applied to this network.
     *
     * @throws ConstraintViolatedException if any constraint is violated
     */
    public void checkConstraints() throws ConstraintViolatedException {
        this.checkConstraints(this.constraints);
    }
    
    /**
     * Checks the given constraints against this network.
     *
     * @param constraints the constraints to check
     *
     * @throws ConstraintViolatedException if any constraint is violated
     */
    public void checkConstraints(Iterable<? extends PNConstraint> constraints) throws ConstraintViolatedException {
        ConstraintChecker checker = new ConstraintChecker(this);
        for (PNConstraint constraint : constraints) {
            constraint.checkProbNet(this, checker);
        }
        checker.buildAndThrow();
    }
    
    public void checkConstraintsIn(ConstraintChecker constraintChecker) {
        for (PNConstraint constraint : this.constraints) {
            constraint.checkProbNet(this, constraintChecker);
        }
    }
    
    /**
     * Removes all constraints in the given collection.
     *
     * @param constraints the collection of {@code PNConstraint} instances to be removed
     */
    public void removeConstraints(Collection<PNConstraint> constraints) {
        for (PNConstraint constraint : constraints) {
            removeConstraint(constraint);
        }
    }
    
    /** Wipes all constraints and their listeners. Used by {@link ProbNetCopier}. */
    void clearConstraints() {
        new ArrayList<>(constraints).forEach(this::removeConstraint);
    }
    
    /**
     * Remove all the constraints in the network
     *
     * @param constraintClass {@code Class}
     */
    public void removeAllConstraints(Class<PNConstraint> constraintClass) {
        List<PNConstraint> constraintsToRemove = new ArrayList<>();
        for (PNConstraint constraint : constraints) {
            if (constraint.getClass() == constraintClass) {
                constraintsToRemove.add(constraint);
            }
        }
        constraintsToRemove.forEach(constraints::remove);
    }
    
    /**
     * @return {@code ArrayList} of {@code PNConstraint}s
     */
    public List<PNConstraint> getConstraints() {
        return new ArrayList<>(constraints);
    }
    
    /** Returns all constraints that matches a certain class */
    public <TargetConstraint extends PNConstraint> Stream<TargetConstraint>
    getConstraintsOfClass(Class<TargetConstraint> constraintClass) {
        return this.constraints.stream().filter(constraintClass::isInstance).map(constraintClass::cast);
    }
    
    /** Returns a constraint whose class matches a certain class */
    public <TargetConstraint extends PNConstraint> @Nullable TargetConstraint
    getConstraintOfClass(Class<TargetConstraint> constraintClass) {
        return this.getConstraintsOfClass(constraintClass).findFirst().orElse(null);
    }
    
    /** Returns whether this ProbNet contains or not a constraint of a certain constraint */
    public boolean hasConstraintOfClass(Class<? extends PNConstraint> constraintClass) {
        return this.getConstraintsOfClass(constraintClass).findAny().isPresent();
    }
    
    
    /**
     * @return {@code ArrayList} of {@code PNConstraint}s
     */
    public List<PNConstraint> getAdditionalConstraints() {
        List<PNConstraint> additionalConstraints = new ArrayList<>(constraints);
        List<PNConstraint> networkTypeConstraints = ConstraintManager.getUniqueInstance()
                                                                     .buildConstraintList(networkType);
        additionalConstraints.removeAll(networkTypeConstraints);
        return additionalConstraints;
    }
    
    /**
     * Gets Network type constraint. There is only one and it is stored in first
     * position.
     *
     * @return constraint. NetworkType
     */
    public NetworkType getNetworkType() {
        return networkType;
    }
    
    /**
     * Changes the network type and updates the constraint set accordingly.
     *
     * <p>The method first verifies that the current network satisfies all
     * constraints required by {@code newNetworkType}. Only if every check
     * passes are constraints added/removed and the type committed. If any
     * constraint is violated the network type is rolled back to its previous
     * value and a {@link ConstraintViolatedException} is thrown, leaving the
     * network in its original state.
     *
     * @param newNetworkType the network type to switch to; must not be {@code null}
     *
     * @throws ConstraintViolatedException if the current network structure violates
     *                                     a constraint required by {@code newNetworkType}
     */
    public void setNetworkType(NetworkType newNetworkType) throws ConstraintViolatedException {
        NetworkType oldNetworkType = this.networkType;
        this.networkType = newNetworkType;
        List<PNConstraint> newConstraints = ConstraintManager.getUniqueInstance().buildConstraintList(newNetworkType);
        // Add new constraints implied by the network type
        newConstraints.removeIf(this.constraints::contains);
        try {
            for (PNConstraint newConstraint : newConstraints) {
                var checker = new ConstraintChecker(this);
                newConstraint.checkProbNet(this, checker);
                checker.buildAndThrow();
            }
        } catch (ConstraintViolatedException e) {
            this.networkType = oldNetworkType;
            throw e;
        }
        for (PNConstraint newConstraint : newConstraints) {
            addConstraint(newConstraint);
        }
        this.constraints.removeIf(constraint -> !newNetworkType.isApplicableConstraint(constraint));
    }
    
    /**
     * Checks all the constraints applied to this {@code probNet}.
     *
     * @return {@code true} when all the constraints are fullfilled,
     * otherwise {@code false}.
     */
    public boolean checkProbNet() {
        return getUnsatisfiedConstraints().isEmpty();
    }
    
    /**
     * Returns the list of constraints that are currently not satisfied by this network.
     *
     * @return list of unsatisfied constraints; empty if all are satisfied
     */
    public List<PNConstraint> getUnsatisfiedConstraints() {
        List<PNConstraint> unsatisfied = new ArrayList<>();
        for (PNConstraint constraint : this.constraints) {
            if (!constraint.isMetBy(this)) {
                unsatisfied.add(constraint);
            }
        }
        return unsatisfied;
    }
    
    /** Delegates to {@link ProbNetClassifier#variablesCouldBeTemporal(ProbNet)}. */
    public boolean variablesCouldBeTemporal() {
        return ProbNetClassifier.variablesCouldBeTemporal(this);
    }
    
    /** Delegates to {@link ProbNetClassifier#isMultiagent(ProbNet)}. */
    public boolean isMultiagent() {
        return ProbNetClassifier.isMultiagent(this);
    }
    
    /** Delegates to {@link ProbNetClassifier#getNumCriteria(PotentialNetwork)}. */
    public int getNumCriteria() {
        return ProbNetClassifier.getNumCriteria(this);
    }
    
    /** Delegates to {@link ProbNetClassifier#thereAreTemporalNodes(GraphNetwork)}. */
    public boolean thereAreTemporalNodes() {
        return ProbNetClassifier.thereAreTemporalNodes(this);
    }
    
    /** Delegates to {@link ProbNetClassifier#onlyTemporal(ProbNet)}. */
    public boolean onlyTemporal() {
        return ProbNetClassifier.onlyTemporal(this);
    }
    
    /** Delegates to {@link ProbNetClassifier#onlyChanceNodes(ProbNet)}. */
    public boolean onlyChanceNodes() {
        return ProbNetClassifier.onlyChanceNodes(this);
    }
    
    /**
     * Creates a shallow structural copy of {@code this ProbNet}: copies the
     * graph and the nodes but does not copy variables nor potentials.
     *
     * @return {@code this probNet} copied.
     *
     * @see ProbNetCopier#shallowCopy(ProbNet)
     */
    public ProbNet copy() {
        return ProbNetCopier.shallowCopy(this);
    }
    
    /**
     * Returns a shallow structural copy of this network, satisfying the
     * {@link Cloneable} contract. Delegates to {@link #copy()}.
     */
    @Override
    public ProbNet clone() {
        return copy();
    }
    
    /**
     * Inserts a link ({@code directed = true} or {@code false})
     * between the nodes associated to {@code variable1} and
     * {@code variable2} in {@code this} graph.
     *
     * @param variable1 {@code Variable}
     * @param variable2 {@code Variable}
     * @param directed  {@code boolean}
     */
    public void addLink(Variable variable1, Variable variable2, boolean directed) {
        // Get nodes
        Node node1 = getNode(variable1);
        Node node2 = getNode(variable2);
        if (node1 != null && node2 != null) {
            addLink(node1, node2, directed);
        }
    }
    
    /**
     * Inverts the link ({@code directed = true} or {@code false})
     * that goes from the nodes associated to {@code variable1} and
     * {@code variable2} in {@code this} graph.
     *
     * @param variable1 {@code Variable}
     * @param variable2 {@code Variable}
     */
    public void invertLink(Variable variable1, Variable variable2) {
        removeLink(variable1, variable2, true);
        addLink(variable2, variable1, true);
    }
    
    public String getName() {
        return metadata.getName();
    }
    
    public void setName(String name) {
        metadata.setName(name);
    }
    
    /**
     * @return Number of nodes in {@code probNet}. {@code int}
     */
    public int getNumNodes() {
        return nodeDepot.getNumNodes();
    }
    
    /**
     * @param nodeType - {@code NodeType}
     *
     * @return Number of nodes with {@code NodeType = nodeType}.
     * {@code int}
     */
    public int getNumNodes(NodeType nodeType) {
        return nodeDepot.getNumNodes(nodeType);
    }
    
    /**
     * Delegates to {@link ProbNetPotentialQueries#tableProjectPotentials(ProbNet, EvidenceCase)}.
     *
     * @param evidenceCase Evidence in that the potentials will be projected
     *
     * @return The potentials of the network projected on the evidence
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     *
     */
    public List<TablePotential> tableProjectPotentials(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
        return ProbNetPotentialQueries.tableProjectPotentials(this, evidenceCase);
    }
    
    /**
     * Returns all non-null potentials in this network (from every node and from
     * the constant-potential set) that satisfy {@code predicate}.
     *
     * @param predicate filter applied to each non-null potential
     *
     * @return mutable list of matching potentials
     */
    public List<Potential> getPotentials(Predicate<Potential> predicate) {
        List<Potential> potentials = new ArrayList<>();
        for (Node node : getNodes()) {
            for (Potential potential : node.getPotentials()) {
                if (potential != null && predicate.test(potential)) {
                    potentials.add(potential);
                }
            }
        }
        for (Potential potential : constantPotentials) {
            if (potential != null && predicate.test(potential)) {
                potentials.add(potential);
            }
        }
        return potentials;
    }
    
    /** Returns all non-null potentials in this network. */
    public List<Potential> getPotentials() {
        return getPotentials(p -> true);
    }
    
    /**
     * @return All the potentials of this network sorted topologically.
     * {@code List} of {@code Potential}s.
     */
    public List<Potential> getSortedPotentials() {
        List<Node> nodes = ProbNetOperations.sortTopologically(this);
        List<Potential> potentials = new ArrayList<>();
        for (Node node : nodes) {
            potentials.addAll(node.getPotentials());
        }
        return potentials;
    }
    
    /**
     * @param variables {@code ArrayList} of {@code Variable}
     *
     * @return All the nodes corresponding to variables in same order.
     * {@code ArrayList} of {@code Node}
     */
    public List<Node> getNodes(List<Variable> variables) {
        List<Node> nodes = new ArrayList<>(variables.size());
        for (Variable variable : variables) {
            nodes.add(getNode(variable));
        }
        return nodes;
    }
    
    /**
     * @param nodeType nodetype filter
     *
     * @return All the nodes of certain kind
     */
    public List<Node> getNodes(NodeType nodeType) {
        return nodeDepot.getNodes(nodeType);
    }
    
    /**
     * Gets all utility nodes with the cECriterion
     *
     * @param cECriterion cost-effectiveness criterion filter
     *
     * @return All utility nodes with the cost effectiveness criterion
     */
    public List<Node> getNodes(CECriterion cECriterion) {
        List<Node> utilityNodes = getNodes(NodeType.UTILITY);
        List<Node> nodes = new ArrayList<>();
        
        for (Node utilityNode : utilityNodes) {
            if (utilityNode.getVariable().getDecisionCriterion().getCECriterion() == cECriterion) {
                nodes.add(utilityNode);
            }
        }
        return nodes;
    }
    
    /**
     * The potentials that contain {@code variable} are stored in the node
     * associated to the {@code variable} or in the neighbors of that node.
     * This method returns as well the constant potentials (i.e., the potentials
     * that do not depend on any variable) stored in the node associated to
     * {@code variable}.
     *
     * @param variable {@code Variable}.
     *
     * @return {@code ArrayList} of all the {@code Potential}s in this
     * network that contains {@code variable}
     */
    public List<Potential> getPotentials(Variable variable) {
        Node node = getNode(variable);
        if (node == null) {
            return new ArrayList<>();
        }
        
        // potentials associated to this node
        List<Potential> potentials = new ArrayList<>();
        
        // potentials in neighbors that contains variable
        Set<Node> semiNeighbors = new LinkedHashSet<>(getNeighbors(node));
        semiNeighbors.add(node);
        List<Node> children = getChildren(node);
        for (Node child : children) {
            semiNeighbors.addAll(child.getParents());
        }
        for (Node neighbor : semiNeighbors) {
            List<Potential> nodePotentials = neighbor.getPotentials();
            for (Potential potential : nodePotentials) {
                if (potential.contains(variable)) {
                    potentials.add(potential);
                }
            }
        }
        return potentials;
    }
    
    /**
     * Get all the potentials of a specific node type
     *
     * @param nodeType node type filter
     *
     * @return All the utility potentials of a type.
     */
    public List<Potential> getPotentialsByType(NodeType nodeType) {
        return nodeDepot.getPotentialsByType(nodeType);
    }
    
    /**
     * Get all the potentials of a specific role
     *
     * @param role potential role filter
     *
     * @return All the potentials of a role.
     */
    public List<Potential> getPotentialsByRole(PotentialRole role) {
        return getPotentials(p -> p.getPotentialRole() == role);
    }
    
    /**
     * Get all the additive potentials
     *
     * @return All additive potentials. {@code List} of {@code Potential}
     */
    public List<Potential> getAdditivePotentials() {
        return getPotentials(Potential::isAdditive);
    }
    
    
    /**
     * Gets all the probability potentials that contain the
     * {@code Variable} received. The potentials that can contain that
     * variable are in the node associated to the variable and its neighbors.
     *
     * <p>Delegates to {@link ProbNetPotentialQueries#getProbPotentials(GraphNetwork, Variable)}.
     *
     * @param variable variable that belongs to this {@code ProbNet}
     *
     * @return {@code ArrayList} of potentials containing
     * {@code variable}.
     */
    public List<Potential> getProbPotentials(Variable variable) {
        return ProbNetPotentialQueries.getProbPotentials(this, variable);
    }
    
    /**
     * Gets all the utility potentials that contain the {@code variable}
     * received. Constant utility potentials are also returned by this method.
     * <p>
     * The potentials that can contain that variable are in the node associated
     * to the variable and its neighbors.
     *
     * <p>Delegates to {@link ProbNetPotentialQueries#getUtilityPotentials(GraphNetwork, Variable)}.
     *
     * @param variable that belongs to this {@code ProbNet}
     *                 {@code Variable}.
     *
     * @return {@code ArrayList} of potentials containing
     * {@code variable}.
     */
    public List<Potential> getUtilityPotentials(Variable variable) {
        return ProbNetPotentialQueries.getUtilityPotentials(this, variable);
    }
    
    /**
     * Removes {@code potential} from this {@code ProbNet}
     *
     * @param potential Potential
     *
     * @return The node where the potential was located or {@code null} if
     * it did not exists
     */
    public @Nullable Node removePotential(Potential potential) {
        List<Variable> variables = potential.getVariables();
        List<Node> candidateNodes = new ArrayList<>();
        // gets nodes that could contain the potential
        if (variables.isEmpty()) {// Constant potentials can be in any
            // node
            candidateNodes = this.getNodes();
        } else {
            for (Variable variable : variables) {
                Node node = getNode(variable);
                if (node != null) {
                    candidateNodes.add(getNode(variable));
                }
            }
        }
        
        // find in such nodes the potential to remove
        for (Node node : candidateNodes) {
            if (node != null) {
                List<Potential> potentialsNode = node.getPotentials();
                for (Potential potentialNode : potentialsNode) {
                    if (potentialNode == potential) {
                        if (node.removePotential(potentialNode)) {
                            return node;
                        }
                    }
                }
            }
        }
        //Couldn't find the potential in any node.
        constantPotentials.remove(potential);
        return null;
    }
    
    /**
     * Removes all the potentials that contains the {@code variable}
     * associated to {@code node}
     *
     * @param node {@code Node}
     */
    public void removePotentials(Node node) {
        // get the nodes that contains potentials associated to the variable
        List<Node> nodes = new ArrayList<>();
        Variable variable = node.getVariable();
        nodes.add(node);
        // and its siblings
        nodes.addAll(getSiblings(node));
        // for each node extract its potentials ...
        for (Node otherNode : nodes) {
            for (Potential potential : new ArrayList<>(otherNode.getPotentials())) {
                // ... and removes the potentials that contains the variable
                if (potential.getVariables().contains(variable)) {
                    otherNode.removePotential(potential);
                }
            }
        }
    }
    
    /**
     * Removes all the potentials in the array of potentials received.
     *
     * @param toRemovePotentials {@code ArrayList} of {@code Potential}
     */
    public void removePotentials(List<Potential> toRemovePotentials) {
        for (Potential potential : toRemovePotentials) {
            this.removePotential(potential);
        }
    }
    
    /**
     * @param variable that not belongs
     *                 . {@code Variable}
     * @param nodeType . {@code NodeType}
     *
     * @return The {@code node} that points to {@code variable} in
     * {@code this} network.
     * Condition: the variable must not be in the ProbNet.
     */
    public Node addNode(Variable variable, NodeType nodeType) {
        Node node = nodeDepot.getNode(nodeType, variable);
        if (node == null) {
            node = new Node(this, variable, nodeType);
            addNode(node);
        }
        return node;
    }
    
    /**
     * @param node . {@code Node}
     *             Condition: the variable must not be in the ProbNet. This method is
     *             used to redo the {@code AddVariableEdit}, i.e., to
     *             reinsert a Node that has been removed.
     */
    public void addNode(Node node) {
        graph.addNode(node);
        nodeDepot.addNode(node);
    }
    
    /**
     * Adds a node with a default potential appropriate for its type, and
     * places it at the given cursor position.
     *
     * @param variable       the variable for the new node
     * @param nodeType       the type of node to create
     * @param cursorPosition the screen position for the new node
     */
    public void addNodeConsistently(Variable variable, NodeType nodeType, Point2D.Double cursorPosition) {
        cursorPosition = cursorPosition.clone();
        Node newNode = addNode(variable, nodeType);
        if (nodeType == NodeType.DECISION) {
            newNode.setPolicyType(PolicyType.OPTIMAL);
        }
        var defaultPotential = PotentialUtils.generateDefaultPotential(this, variable, nodeType);
        if (defaultPotential != null) {
            addPotential(defaultPotential);
        }
        newNode.setCoordinateX((int) cursorPosition.getX());
        newNode.setCoordinateY((int) cursorPosition.getY());
    }
    
    
    /**
     * @param nameOfVariable {@code String}
     *
     * @return The {@code Node} that matches the
     * {@code nameOfVariable}
     */
    public @Nullable Node getNode(String nameOfVariable) {
        return nodeDepot.getNode(nameOfVariable);
    }
    
    /**
     * @param nameOfVariable {@code String}
     * @param nodeType       {@code NodeType}
     *
     * @return The node with {@code nameOfVariable} and
     * {@code kindOfNode} if exists otherwise null
     */
    public @Nullable Node getNode(String nameOfVariable, NodeType nodeType) {
        return nodeDepot.getNode(nameOfVariable, nodeType);
    }
    
    /**
     * @param variable {@code Variable}
     *
     * @return The {@code Node} that matches the {@code Variable}
     */
    public @Nullable Node getNode(Variable variable) {
        return nodeDepot.getNode(variable);
    }
    
    /**
     * @param variableName name of the variable
     *                     . {@code String}
     *
     * @return variable that matches {@code variableName} if exists,
     * otherwise {@code null}. {@code Variable}
     */
    public @Nullable Variable getVariable(String variableName) {
        Node node = getNode(variableName);
        return node == null ? null : node.getVariable();
    }
    
    /**
     * Returns variable on a certain timeSlice
     *
     * @param baseName  base name of the variable
     * @param timeSlice time slice of the variable
     *
     * @return return variable with that basename and time slice
     */
    public @Nullable Variable getVariable(String baseName, int timeSlice) {
        return getVariable(baseName + " [" + timeSlice + "]");
    }
    
    /**
     * @param variable       . a {@code Variable}
     * @param timeDifference time slice diference
     *
     * @return a new variable having the same base name as the first argument
     * but in the time slice indicated by the second argument
     */
    public @Nullable Variable getShiftedVariable(Variable variable, int timeDifference) {
        return getVariable(variable.getBaseName(), variable.getTimeSlice() + timeDifference);
    }
    
    // TODO Con este nuevo metodo podemos evitar la chapuza hecha en
    // varios lugares de invocar getVariable para ver si lanzaba una excepcion.
    // Revisar el uso de esa excepcion y evitarla en lo posible.
    
    /**
     * @param variableName the name to check
     *
     * @return {@code true} if this network contains a variable with the given name
     */
    public boolean containsVariable(String variableName) {
        return nodeDepot.getNode(variableName) != null;
    }
    
    /**
     * @param variable the variable to check
     *
     * @return {@code true} if this network contains the given variable
     */
    public boolean containsVariable(Variable variable) {
        return getNode(variable) != null;
    }
    
    /**
     * Returns true if this probNet contains the shifted variable
     *
     * @param variable       variable
     * @param timeDifference time difference
     *
     * @return if the probNet contains that shifted variable returns true, else in other case
     */
    public boolean containsShiftedVariable(Variable variable, int timeDifference) {
        int timeSlice = variable.getTimeSlice() + timeDifference;
        String baseName = variable.getBaseName();
        return containsVariable(baseName + " [" + timeSlice + "]");
    }
    
    /**
     * Adds the received potential to the list of potentials of the conditioned
     * variable (the first one).
     *
     * @param potential . {@code Potential}
     *
     * @return The {@code Node} in which the {@code potential}
     * received has been added.
     * Condition: network contains at least one chance variable
     * Condition: potential type must correspond with the roles (discrete or
     * continuous) of the variables in the network
     * Condition: If A is the first variable in the potential and
     * B<sub>0</sub> ... B<sub>n</sub> the remainders, there must
     * be a directed link B<sub>i</sub> -&#82; A for every variable
     * B<sub>i</sub> in the potential (other than A)
     */
    public Node addPotential(Potential potential) {
        return addPotential(potential, null);
    }
    
    /**
     * @param potential       {@code Potential}
     * @param originalProbNet To get information from nodes when using this method to build a {@code ProbNet}
     *                        from another one.
     *
     * @return The {@code Node} in which the {@code potential}
     * received has been added.
     *
     * @see ProbNet#addPotential(Potential)
     */
    public @Nullable Node addPotential(Potential potential, ProbNet originalProbNet) {
        List<Variable> variables = potential.getVariables();
        List<Node> nodes = new ArrayList<>();
        for (Variable variable : variables) {
            // add the variables that are not yet in the network
            Node node = getNode(variable);
            if (node == null) {
                NodeType nodeType = (originalProbNet == null || originalProbNet.getNode(variable) == null) ?
                        NodeType.CHANCE : originalProbNet.getNode(variable).getNodeType();
                node = addNode(variable, nodeType);
            }
            nodes.add(node);
        }
        
        // add the potential
        if (variables.isEmpty()) {
            this.constantPotentials.add(potential);
        } else {
            nodes.getFirst().addPotential(potential);
        }
        
        // draw links between the variables
        boolean isDirected = !this.hasConstraintOfClass(OnlyUndirectedLinks.class);
        if (isDirected) {
            // TODO - CHECK
            if (variables.isEmpty()) {
                return null;
            }
            Node conditionedNode = nodes.getFirst();
            for (int i = 1; i < nodes.size(); i++) {
                Node conditioningNode = nodes.get(i);
                if (!isParent(conditioningNode, conditionedNode)) {
                    addLink(conditioningNode, conditionedNode, true);
                }
            }
        } else {
            int potentialSize = nodes.size();
            for (int i = 0; i < potentialSize - 1; i++) {
                Node node1 = nodes.get(i);
                for (int j = i + 1; j < potentialSize; j++) {
                    Node node2 = nodes.get(j);
                    if (!isSibling(node1, node2)) {
                        addLink(node1, node2, false);
                    }
                }
            }
        }
        return nodes.isEmpty() ? null : nodes.getFirst();
    }
    
    /**
     * Returns all variables except those attached to UTILITY nodes.
     * This includes CHANCE, DECISION, SV_PRODUCT and SV_SUM nodes.
     *
     * @return list of non-utility variables
     */
    public List<Variable> getNonUtilityVariables() {
        return new ArrayList<>(getNodes()
                                       .stream()
                                       .filter(node -> node.getNodeType() != NodeType.UTILITY)
                                       .map(Node::getVariable)
                                       .toList());
    }
    
    /**
     * @deprecated Use {@link #getNonUtilityVariables()} instead.
     * This method was misnamed: it also returns SV_PRODUCT
     * and SV_SUM variables, not just CHANCE and DECISION.
     */
    @Deprecated
    public List<Variable> getChanceAndDecisionVariables() {
        return getNonUtilityVariables();
    }
    
    /**
     * @param nodeType {@code NodeType}
     *
     * @return Variables corresponding to the node type received.
     * {@code ArrayList} of {@code Variable}
     */
    public List<Variable> getVariables(NodeType nodeType) {
        return new ArrayList<>(getNodes(nodeType).stream().map(Node::getVariable).toList());
    }
    
    /** @return all variables in this network */
    public ArrayList<Variable> getVariables() {
        return new ArrayList<>(getNodes().stream().map(Node::getVariable).toList());
    }
    
    /** @return the names of all variables in this network */
    public ArrayList<String> getVariablesNames() {
        return new ArrayList<>(getNodes().stream().map(Node::getVariable).map(Variable::getName).toList());
    }
    
    
    /**
     * Removes {@code node} from {@code this ProbNet} and removes
     * also the associated {@code node} from the associated
     * {@code Graph}.
     *
     * @param node {@code Node}
     */
    public void removeNode(Node node) {
        graph.removeNode(node);
        nodeDepot.removeNode(node);
    }
    
    /**
     * @param variable1 {@code Variable}
     * @param variable2 {@code Variable}
     * @param directed  {@code boolean}
     */
    public void removeLink(Variable variable1, Variable variable2, boolean directed) {
        removeLink(getNode(variable1), getNode(variable2), directed);
    }
    
    /**
     * @return Number of potentials. {@code int}
     */
    public int getNumPotentials() {
        return nodeDepot.getNumPotentials();
    }
    
    /** @return the edit support that manages undo/redo and listeners */
    public PNESupport getPNESupport() {
        return pNESupport;
    }
    
    /** Package-private accessor to the metadata object (for copiers and serializers). */
    NetworkMetadata getMetadata() {
        return metadata;
    }
    
    public String getComment() {
        return metadata.getComment();
    }
    
    public void setComment(String comment) {
        metadata.setComment(comment);
    }
    
    public State[] getDefaultStates() {
        return metadata.getDefaultStates();
    }
    
    public void setDefaultStates(State[] defaultStates) {
        metadata.setDefaultStates(defaultStates);
    }
    
    /**
     * Condition: oldNode belongs to this probNet
     *
     * @param coordinateXOffset Coordinate X offset
     * @param coordinateYOffset Coordinate Y offset
     * @param oldNode           Old node
     * @param timeDifference    Time difference
     *
     * @return Node
     */
    public Node addShiftedNode(Node oldNode, int timeDifference, double coordinateXOffset, double coordinateYOffset) {
        Variable oldVariable = oldNode.getVariable();
        Variable newVariable = oldVariable.clone();
        newVariable.setTimeSlice(oldVariable.getTimeSlice() + timeDifference);
        Node newNode = addNode(newVariable, oldNode.getNodeType());
        newNode.setCoordinateX(oldNode.getCoordinateX() + coordinateXOffset);
        newNode.setCoordinateY(oldNode.getCoordinateY() + coordinateYOffset);
        // TODO Hacer clon para node y quitar estas lineas
        newNode.setPurpose(oldNode.getPurpose());
        newNode.setRelevance(oldNode.getRelevance());
        newNode.setComment(oldNode.getComment());
        newNode.setAdditionalProperties(oldNode.getAdditionalProperties());
        return newNode;
    }
    
    public List<StringWithProperties> getAgents() {
        return metadata.getAgents();
    }
    
    public void setAgents(List<StringWithProperties> agents) {
        metadata.setAgents(agents);
    }
    
    public List<Criterion> getDecisionCriteria() {
        return metadata.getDecisionCriteria();
    }
    
    public void setDecisionCriteria(List<Criterion> decisionCriteria) {
        metadata.setDecisionCriteria(decisionCriteria);
    }
    
    /** @return whether the network comment should be displayed when the file is opened */
    public boolean getShowCommentWhenOpening() {
        return metadata.getShowCommentWhenOpening();
    }
    
    public void setShowCommentWhenOpening(boolean show) {
        metadata.setShowCommentWhenOpening(show);
    }
    
    /**
     * Node calls this method when its variable instance has been changed, so
     * that probNet updates its reference too.
     *
     * @param oldVariable old variable to be updated
     */
    public void updateVariable(Variable oldVariable) {
        Node node = nodeDepot.getNode(oldVariable);
        nodeDepot.removeNode(oldVariable);
        nodeDepot.addNode(node);
    }
    
    public InferenceOptions getInferenceOptions() {
        return metadata.getInferenceOptions();
    }
    
    public void setInferenceOptions(InferenceOptions inferenceOptions) {
        metadata.setInferenceOptions(inferenceOptions);
    }
    
    public CycleLength getCycleLength() {
        return metadata.getCycleLength();
    }
    
    public void setCycleLength(CycleLength temporalUnit) {
        metadata.setCycleLength(temporalUnit);
    }
    
    /**
     * Creates a full deep copy of {@code this ProbNet}: all mutable objects
     * (criteria, cycle length, inference options, nodes, potentials, link
     * intervals) are cloned into independent instances.
     *
     * @see ProbNetCopier#deepCopy(ProbNet)
     */
    public ProbNet deepCopy() {
        return ProbNetCopier.deepCopy(this);
    }
    
    /**
     * Returns an unmodifiable view of the constant potentials (those with no
     * associated variables). Use {@link #addPotential(Potential)} to add new
     * constant potentials and {@link #removePotential(Potential)} to remove them.
     *
     * @return unmodifiable view of the constant {@link TablePotential}s
     */
    public Set<Potential> getConstantPotentials() {
        return Collections.unmodifiableSet(constantPotentials);
    }
    
    
    /** @return unmodifiable view of format-specific additional properties */
    public Map<String, String> getAdditionalProperties() {
        return metadata.getAdditionalProperties();
    }
    
    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        metadata.setAdditionalProperties(additionalProperties);
    }
    
    public void putAdditionalProperty(String key, String value) {
        metadata.putAdditionalProperty(key, value);
    }
    
    /**
     * Moves the specified nodes to the given positions.
     *
     * @param namesNode    names of the nodes to move
     * @param newPositions corresponding new positions for each node
     *
     * @throws IllegalArgumentException if the lists have different sizes
     */
    public void moveNode(List<String> namesNode, List<Point2D.Double> newPositions) {
        if (namesNode.size() != newPositions.size()) {
            throw new IllegalArgumentException(
                    "namesNode and newPositions must have the same size: "
                            + namesNode.size() + " vs " + newPositions.size());
        }
        for (int i = 0; i < namesNode.size(); i++) {
            Node node = getNode(namesNode.get(i));
            if (node != null) {
                node.setCoordinateX(newPositions.get(i).getX());
                node.setCoordinateY(newPositions.get(i).getY());
            }
        }
    }
    
    /** Delegates to {@link ProbNetAgentManager#modifyAgent(ProbNet, StateAction, String, Object[][])}. */
    public void modifyAgent(StateAction stateAction, String agentName, Object[][] dataTable) {
        ProbNetAgentManager.modifyAgent(this, stateAction, agentName, dataTable);
    }
    
    // =========================================================
    // Graph structure — delegation to internal Graph<Node>
    // =========================================================
    
    public List<Node> getNodes() {
        return graph.getNodes();
    }
    
    public List<Node> getChildren(Node node) {
        return graph.getChildren(node);
    }
    
    public List<Node> getParents(Node node) {
        return graph.getParents(node);
    }
    
    public List<Node> getSiblings(Node node) {
        return graph.getSiblings(node);
    }
    
    public List<Node> getNeighbors(Node node) {
        return graph.getNeighbors(node);
    }
    
    public int getNumChildren(Node node) {
        return graph.getNumChildren(node);
    }
    
    public int getNumParents(Node node) {
        return graph.getNumParents(node);
    }
    
    public int getNumSiblings(Node node) {
        return graph.getNumSiblings(node);
    }
    
    public int getNumNeighbors(Node node) {
        return graph.getNumNeighbors(node);
    }
    
    public boolean isChild(Node node1, Node node2) {
        return graph.isChild(node1, node2);
    }
    
    public boolean isParent(Node node1, Node node2) {
        return graph.isParent(node1, node2);
    }
    
    public boolean isSibling(Node node1, Node node2) {
        return graph.isSibling(node1, node2);
    }
    
    public boolean isNeighbor(Node node1, Node node2) {
        return graph.isNeighbor(node1, node2);
    }
    
    public Link<Node> addLink(Node node1, Node node2, boolean directed) {
        return graph.addLink(node1, node2, directed);
    }
    
    public void removeLink(Node node1, Node node2, boolean directed) {
        graph.removeLink(node1, node2, directed);
    }
    
    public void removeLink(Link<Node> link) {
        graph.removeLink(link);
    }
    
    public void removeLinks(Node node) {
        graph.removeLinks(node);
    }
    
    public Link<Node> getLink(Node node1, Node node2, boolean directed) {
        return graph.getLink(node1, node2, directed);
    }
    
    public List<Link<Node>> getLinks(Node node) {
        return graph.getLinks(node);
    }
    
    public List<Link<Node>> getLinks() {
        return graph.getLinks();
    }
    
    public int getNumLinks(Node node) {
        return graph.getNumLinks(node);
    }
    
    public void makeLinksExplicit(boolean createLabelledLinks) {
        graph.makeLinksExplicit(createLabelledLinks);
    }
    
    public boolean hasExplicitLinks() {
        return graph.hasExplicitLinks();
    }
    
    public boolean existsPath(Node node1, Node node2, boolean directed, List<Link<Node>> linksToIgnore) {
        return graph.existsPath(node1, node2, directed, linksToIgnore);
    }
    
    public void marry(Collection<Node> nodeList) {
        graph.marry(nodeList);
    }
    
    @Override
    public String toString() {
        String out = "Type: " + networkType.toString() + "\n";
        List<Node> nodes = getNodes();
        int numPotentials = getNumPotentials();
        int numNodes = nodes.size();
        if (numNodes == 0) {
            out += "No nodes.\n";
        } else {
            out += "Nodes (" + numNodes + "): ";
            for (Node node : nodes) {
                out += ("\n  ") + node.toString();
            }
            out += "\n";
        }
        if (numPotentials == 0) {
            out += "No potentials.\n";
        } else {
            out += "Number of potentials: " + numPotentials + "\n";
        }
        if (constraints.isEmpty()) {
            out += "No constraints\n";
        } else {
            String constraintsAsStr = constraints.stream().map(constraint -> {
                String strConstraint = constraint.toString();
                return strConstraint.substring(strConstraint.lastIndexOf('.') + 1);
            }).collect(Collectors.joining(", "));
            out += "Constraints: " + constraintsAsStr + "\n";
        }
        if (getAgents() != null) {
            out += "\nAgents:\n" + getAgents();
        }
        return out;
    }
    
}
