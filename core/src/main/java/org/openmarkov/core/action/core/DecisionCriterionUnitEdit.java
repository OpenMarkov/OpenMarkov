/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;

/**
 * {@code DecisionCriterionUnitEdit} is a simple edit that allow modify the unit
 * of a criterion
 *
 * @author Jorge
 */
public class DecisionCriterionUnitEdit extends PNEdit {


	private static final long serialVersionUID = 1L;
    
    private final String oldUnit;
    private final String newUnit;
    private Criterion criterion;

	public DecisionCriterionUnitEdit(ProbNet probnet, String criterionName, String newUnit) {
		super(probnet);
        this.newUnit = newUnit;

		// Search the criterion in where we want set the new unit of measure
		for (Criterion criterion : probnet.getDecisionCriteria()) {
			if (criterion.getCriterionName() != null && criterion.getCriterionName().equals(criterionName)) {
				this.criterion = criterion;
				break;
			}
		}

		this.oldUnit = this.criterion.getCriterionUnit();
	}
	
	@Override protected void doEdit() {
		this.criterion.setCriterionUnit(newUnit);
	}
    
    @Override public void undo() {
		super.undo();
		this.criterion.setCriterionUnit(oldUnit);
	}
}


