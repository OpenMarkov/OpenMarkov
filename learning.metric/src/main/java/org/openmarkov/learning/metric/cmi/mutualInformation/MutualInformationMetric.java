/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.cmi.mutualInformation;

import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.metric.cache.Cache;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.metric.annotation.MetricType;
import org.openmarkov.learning.core.util.Util;
import org.openmarkov.learning.metric.cmi.util.MetricUtils;

import java.util.HashMap;
import java.util.List;

/**
 * This class implements the Mutual Information metric
 *
 * @author jmsevillano
 * @author fjdiez
 * @author joliva
 * @author manuel
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@MetricType(name = "MutualInformation", classConditionedMetric = true)
public class MutualInformationMetric extends Metric {


	public MutualInformationMetric() {
		super();
	}


	protected String classVariable;
	protected MetricUtils utils;
	protected double alpha;



	@Override public double score(TablePotential tablePotential) {

		int xNumStates = tablePotential.getVariable(0).getNumStates();
		int yNumStates = tablePotential.getVariable(1).getNumStates();
		double caseCount = caseDatabase.getNumCases();

		double[] freq = tablePotential.getValues();
		double mutualInformation = 0.0;
		int k =0;

		for(int j=0; j < yNumStates; j++){
			int yOffset = j*xNumStates;
			double pY = MetricUtils.sumArray(freq, yOffset, yOffset+xNumStates, 1) / caseCount;

			for (int i =0; i < xNumStates; i++){
				double pX = MetricUtils.sumArray(freq, i, xNumStates*yNumStates, xNumStates) / caseCount;
				double pXY = freq[i+j*xNumStates] / caseCount;

				mutualInformation+= (pXY>0 && pX*pY>0)? pXY * Math.log(pXY/(pX*pY)):0;
			}
		}
		return mutualInformation;
	}


	@Override protected void initCache() {
		this.cache = new Cache();
		utils = new MetricUtils(probNet, classVariable);
		cachedNodeScores = new HashMap<String, Double>();
		cache.flush(probNet);
		cachedScore = 0;
        
        
        getNonRootNodes().forEach(node -> {
			BaseLinkEdit edit = new AddLinkEdit(probNet, getRootNode().getVariable(), node.getVariable(), true);
			cache.cacheScore(edit, this.scoreEdit(edit, false));
			cachedNodeScores.put(node.getName(), 0.0);
		});
        
        
        getNonRootVariables().forEach(tail -> {
            getNonRootVariables().stream().filter(v -> v != tail).toList().forEach(head -> {
				BaseLinkEdit edit = (!probNet.getNode(head).isParent(probNet.getNode(tail)))?
						new AddLinkEdit(probNet, tail, head, true): new RemoveLinkEdit(probNet, tail, head, true);
				cache.cacheScore(edit, (scoreEdit(edit, false)));
			});
		});

	}



	/**
	 * Scores the associated network with the link given in the received edition added. We only have to recalculate the score
	 * of the destination node.
	 *
     * @param edition {@code BaseLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is definitive (UndoableEditHappened called this method) or not.
     * @return {@code double} score of the net with the given edition
	 */
	protected double scoreEdit(BaseLinkEdit edition, boolean change) {
        return score(Util.getAbsoluteFreqExtraParent(probNet, caseDatabase, probNet.getNode(edition.getVariableFrom()), probNet.getNode(edition.getVariableTo())));
	}

	@Override protected double score(AddLinkEdit edition, boolean change) {
		return scoreEdit(edition, change);
	}

	@Override protected double score(RemoveLinkEdit edition, boolean change) {
		return scoreEdit(edition, change);
	}

	/**
	 * Returns the variables that are not the class variable.
	 *
	 * @return list of non-class variables
	 */
	protected List<Variable> getNonRootVariables(){
		return utils.getNonRootVariables();
	}

	/**
	 * Returns the nodes that are not the class variable node.
	 *
	 * @return list of non-class nodes
	 */
	protected List<Node> getNonRootNodes(){
		return utils.getNonRootNodes();
	}

	/**
	 * Returns the class variable node.
	 *
	 * @return the root (class) node
	 */
	protected Node getRootNode(){
		return utils.getRootNode();
	}

	public void setClassVariable(String classVariable) {
		this.classVariable = classVariable;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

}
