/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.PNEdit;
import java.util.ArrayList;
import java.util.List;

/**
 * Edit that changes the multicriteria options and decision criteria of a network.
 */
public class MulticriteriaEdit extends PNEdit {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1166925687725877620L;
	private List<Criterion> oldDecisionCriteria;
	private final List<Criterion> newDecisionCriteria;
	private final MulticriteriaOptions oldMulticriteriaOptions;
	private final MulticriteriaOptions newMulticriteriaOptions;

	public MulticriteriaEdit(ProbNet probNet, List<Criterion> decisionCriteria, MulticriteriaOptions options) {
		super(probNet);
		if (probNet.getDecisionCriteria() != null && !probNet.getDecisionCriteria().isEmpty()) {
			this.oldDecisionCriteria = new ArrayList<>();
			for (Criterion criterion : probNet.getDecisionCriteria()) {
				this.oldDecisionCriteria.add(criterion.clone());
			}
		}
		this.oldMulticriteriaOptions = probNet.getInferenceOptions().getMultiCriteriaOptions().clone();
		this.newDecisionCriteria = decisionCriteria;
		this.newMulticriteriaOptions = options;
	}
	
	@Override protected void doEdit() {
		//probNet.setDecisionCriteria(this.newDecisionCriteria);
		// Set the new data at the probNet criteria 
		for (Criterion oldCriterion : probNet.getDecisionCriteria()) {
			for (Criterion newCriterion : this.newDecisionCriteria) {
				if (oldCriterion.getCriterionName().equals(newCriterion.getCriterionName())) {
					oldCriterion.copy(newCriterion);
				}
			}
		}

		probNet.getInferenceOptions().setMultiCriteriaOptions(this.newMulticriteriaOptions);

	}
    
    @Override public void undo() {
		super.undo();
		//probNet.setDecisionCriteria(oldDecisionCriteria);
		for (Criterion oldCriterion : probNet.getDecisionCriteria()) {
			for (Criterion newCriterion : this.oldDecisionCriteria) {
				if (oldCriterion.getCriterionName().equals(newCriterion.getCriterionName())) {
					oldCriterion.copy(newCriterion);
				}
			}
		}
		probNet.getInferenceOptions().setMultiCriteriaOptions(this.oldMulticriteriaOptions);
	}
	
	@ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG,
			reasonDescription = "This calls super.redo, which will call doEdit if this edit is 'typicalRedo', and then " +
					"it calls doEdit. This looks like it is redoing the edit 2 times, which might lead to inconsistencies")
	@Override public void redo() {
		super.redo();
		doEdit();
	}

}
