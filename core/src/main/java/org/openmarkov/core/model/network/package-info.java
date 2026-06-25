/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

/**
 * Core domain model for probabilistic graphical models (Bayesian networks,
 * influence diagrams, dynamic networks, etc.).
 *
 * <h2>Domain model overview</h2>
 *
 * <p><img src="doc-files/domain-model.png"
 *         alt="UML class diagram of the core domain model"
 *         style="max-width:100%">
 *
 * <p>The diagram above shows the main classes and their relationships,
 * colour-coded by responsibility area.
 *
 * <h3>Network container (green)</h3>
 *
 * <p>{@link org.openmarkov.core.model.network.ProbNet} is the central class.
 * It holds a {@link org.openmarkov.core.model.graph.Graph Graph&lt;Node&gt;} by
 * composition, delegating the directed/undirected graph structure (nodes, links,
 * parent/child maps).  On top of the graph it adds network-level metadata: the
 * {@link org.openmarkov.core.model.network.type.NetworkType} (Bayesian network,
 * influence diagram, LIMID, etc.), a set of
 * {@link org.openmarkov.core.model.network.constraint.PNConstraint constraints}
 * that enforce structural rules (no cycles, only chance nodes, etc.), decision
 * criteria, agents, and default states for new variables.
 * {@link org.openmarkov.core.model.network.NodeTypeDepot} is an internal index
 * that organises nodes by type (CHANCE, DECISION, UTILITY) for fast lookup.
 *
 * <h3>Domain model (blue)</h3>
 *
 * <p>Each {@link org.openmarkov.core.model.network.Node} in the graph owns
 * exactly one {@link org.openmarkov.core.model.network.Variable} and a list of
 * {@link org.openmarkov.core.model.network.potential.Potential Potentials}.
 * The node also stores visual properties (coordinates, relevance, purpose) and
 * holds a back-reference to its parent {@code ProbNet}.
 *
 * <p>A {@link org.openmarkov.core.model.network.Variable} represents a random
 * variable or decision with a name, a type
 * ({@link org.openmarkov.core.model.network.VariableType#FINITE_STATES FINITE_STATES},
 * {@link org.openmarkov.core.model.network.VariableType#NUMERIC NUMERIC}, or
 * {@link org.openmarkov.core.model.network.VariableType#DISCRETIZED DISCRETIZED}),
 * and an array of {@link org.openmarkov.core.model.network.State States}.
 * Discretized and numeric variables additionally own a
 * {@link org.openmarkov.core.model.network.PartitionedInterval} that defines
 * the continuous interval partition.  Temporal variables encode their time slice
 * in the name (e.g. {@code "X [3]"}).
 *
 * <p>{@link org.openmarkov.core.model.network.Criterion} defines a decision
 * criterion (name, unit, cost-effectiveness role) used by utility nodes in
 * multicriteria analysis.  A variable may reference a criterion when it
 * represents a utility node.
 *
 * <p>{@link org.openmarkov.core.model.graph.Link Link&lt;Node&gt;} represents
 * a directed or undirected edge between two nodes.  Links may carry
 * restriction potentials (constraining which parent-child state combinations
 * are valid) and revealing conditions (which states trigger observation of the
 * child node).
 *
 * <h3>Potentials (orange)</h3>
 *
 * <p>{@link org.openmarkov.core.model.network.potential.Potential} is the
 * abstract base class for conditional probability tables, utility functions,
 * and other parametric distributions (30+ concrete subclasses).  Each potential
 * references the list of variables it depends on, a
 * {@link org.openmarkov.core.model.network.potential.PotentialRole role}
 * (conditional probability, policy, link restriction, etc.), and optionally a
 * {@link org.openmarkov.core.model.network.Criterion}.
 *
 * <h3>Evidence (purple)</h3>
 *
 * <p>{@link org.openmarkov.core.model.network.EvidenceCase} groups a set of
 * {@link org.openmarkov.core.model.network.Finding Findings} for inference.
 * Each finding associates a variable with an observed state index or numerical
 * value.
 *
 * <h3>Composition vs. association</h3>
 *
 * <p>Filled-diamond arrows in the diagram denote <em>composition</em>: the
 * container owns and controls the lifecycle of the contained object (e.g. a
 * Node owns its Variable; deleting the node deletes the variable).  Plain
 * arrows denote <em>association</em>: the referencing object does not own the
 * referenced one (e.g. a Finding references a Variable but does not own it).
 *
 * @see org.openmarkov.core.model.network.ProbNet
 * @see org.openmarkov.core.model.network.Node
 * @see org.openmarkov.core.model.network.Variable
 * @see org.openmarkov.core.model.network.potential.Potential
 * @see org.openmarkov.core.model.network.EvidenceCase
 */
package org.openmarkov.core.model.network;
