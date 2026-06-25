/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.huginPropagation;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterPropagation.StorageLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a set of variables. The main difference with
 * openmarkov.inference.ClusterOfVariables is in method
 * openmarkov.inference.ClusterOfVariables#distributeEvidence
 *
 * @author Manuel Arias
 * @author fjdiez
 */
public class HuginClique extends ClusterOfVariables {
	// Attributes
	protected static final String clusterNamePrefix = "Clique.";
	// Constructor

	/**
     * @param huginForest        {@code HuginForest}
     * @param cliqueVariables    {@code ArrayList} of {@code Variable}
     * @param separatorVariables {@code ArrayList} of {@code Variable}
	 */
	public HuginClique(HuginForest huginForest, List<Variable> cliqueVariables, List<Variable> separatorVariables) {
		super(huginForest, cliqueVariables);
		name = clusterNamePrefix + (huginForest.getNumNodes() - 1);
		this.separatorVariables = separatorVariables;
	}

	// Methods

	/**
     * @param storageLevel {@code int}
	 */
	@Override public void distributeEvidence(StorageLevel storageLevel) {
		for (ClusterOfVariables childClique : getChildren()) {
			Potential upgoingChildMessage = childClique.getUpgoingMessage(storageLevel);
			Potential posteriorMarginalized = DiscretePotentialOperations
					.marginalize(posteriorPotential, childClique.getSeparatorVariables());
			TablePotential division = DiscretePotentialOperations.divide(posteriorMarginalized, upgoingChildMessage);
			List<TablePotential> potentials = new ArrayList<TablePotential>();
			potentials.add(division);
			potentials.add(childClique.getPosteriorPotential(storageLevel));
			childClique.setPosteriorPotential(DiscretePotentialOperations.multiply(potentials));
			childClique.distributeEvidence(storageLevel);
		}
	}

	/**
     * @param variablesList {@code ArrayList} of {@code Variable}
     * @return {@code true} if all the variables in
     * {@code variablesList} are included in the clique variables
	 */
	public boolean containsAll(List<Variable> variablesList) {
        return variables.containsAll(variablesList);
    }
}
