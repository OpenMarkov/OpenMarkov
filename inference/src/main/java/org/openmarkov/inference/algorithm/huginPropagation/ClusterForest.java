/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.huginPropagation;

import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represent a tree of {@code ClustersOfVariable}.
 *
 * @author Manuel Arias
 * @author Francisco Javier Diez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public abstract class ClusterForest extends Graph<ClusterOfVariables> {
	// Attributes
	protected int numClusters;
	protected final List<ClusterOfVariables> rootClusters;
	/**
     * this {@code Map} stores the smallest cluster containing each
	 * variable.
	 */
	protected final Map<Variable, ClusterOfVariables> variables2Clusters;

	// Constructors

	/**
	 * Empty constructor
	 */
	public ClusterForest() {
		rootClusters = new ArrayList<ClusterOfVariables>();
		variables2Clusters = new HashMap<Variable, ClusterOfVariables>();
	}

	/**
     * @param rootCluster {@code ClusterOfVariables}
	 */
	public ClusterForest(ClusterOfVariables rootCluster) {
		this();
		rootClusters.add(rootCluster);
	}

	// Methods

	/**
     * @return {@code ArrayList} of {@code ClusterOfVariables}
	 */
	@Override public List<ClusterOfVariables> getNodes() {
		List<ClusterOfVariables> rootCliques = new ArrayList<>(getRootClusters());
		List<ClusterOfVariables> cliques = new ArrayList<ClusterOfVariables>();
        while (!rootCliques.isEmpty()) {
			int lastCliqueIndex = rootCliques.size() - 1;
			ClusterOfVariables cliqueToExpand = rootCliques.get(lastCliqueIndex);
			rootCliques.remove(lastCliqueIndex);
			List<ClusterOfVariables> children = cliqueToExpand.getChildren();
            rootCliques.addAll(children);
            cliques.add(cliqueToExpand);
		}
		return cliques;
	}

	/**
     * @param cluster {@code ClusterOfVariables}.
	 */
	public void addCluster(ClusterOfVariables cluster) {
		rootClusters.add(cluster);
	}

	/**
     * @param variable {@code Variable}.
	 * @return One of the clusters containing the variable; more precisely, the
     * cluster associated to that variable in the {@code hashMap}
	 * variables2Clusters.
	 */
	public ClusterOfVariables getCluster(Variable variable) {
		return variables2Clusters.get(variable);
	}

	/**
     * @return The collection stored in this class: {@code ArrayList} of
     * {@code ClusterOfVariables}
	 */
	public List<ClusterOfVariables> getRootClusters() {
		return rootClusters;
	}

	/**
     * @param variable  {@code Variable}.
     * @param potential {@code Potential}.
	 */
	public void introduceFindingPotential(Variable variable, Potential potential) {
	}

	public void increaseNumNodes() {
		numClusters++;
	}

	/**
     * @param cluster {@code ClusterOfVariables}.
	 */
	public void setClusterAsRoot(ClusterOfVariables cluster) {
		rootClusters.add(cluster);
	}

	/**
     * @return numClusters {@code int}
	 */
	@Override public int getNumNodes() {
		return numClusters;
	}

	/**
	 * Generates a string with root clusters (more than one in construction
	 * time) and non root clusters.
	 *
     * @return {@code String}
	 */
	public String toString() {
		String out = "Root cluster";
		if (rootClusters.size() > 1) {
			out += "s";
		}
		out += ":" + rootClusters.stream()
								 .map(ClusterOfVariables::toString)
								 .map(stringified -> "\n  " + stringified)
								 .collect(Collectors.joining())
				+ "\nNodes (" + getNodes().size() + "):" + getNodes().stream()
																	 .map(ClusterOfVariables::toString)
																	 .map(stringified -> "\n  " + stringified)
																	 .collect(Collectors.joining())
				+ "\nLinks (" + getLinks().size() + "):" + getLinks().stream()
																	 .map(link -> "\n  " + link.getFrom()
																							   .getName() + " -> " + link.getTo()
																														 .getName())
																	 .collect(Collectors.joining());
		return out;
	}
}