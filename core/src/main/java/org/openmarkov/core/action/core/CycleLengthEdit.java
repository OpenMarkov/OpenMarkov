/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.ProbNet;

/**
 * Edit that changes the cycle length (temporal unit) of a dynamic network.
 */
public class CycleLengthEdit extends PNEdit {

	/**
	 * Default serial UID
	 */
	private static final long serialVersionUID = 1L;

	private final CycleLength oldTemporalUnit;
	private final CycleLength newTemporalUnit;

	public CycleLengthEdit(ProbNet probNet, CycleLength newTemporalUnit) {
		super(probNet);
		this.oldTemporalUnit = probNet.getCycleLength().clone();
		this.newTemporalUnit = newTemporalUnit;
	}
	
	@Override protected void doEdit() {
		probNet.setCycleLength(this.newTemporalUnit);
	}
    
    @Override public void undo() {
		super.undo();
		probNet.setCycleLength(this.oldTemporalUnit);
	}
	
	@ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG,
			reasonDescription = "This calls super.redo, which will call doEdit if this edit is 'typicalRedo', and then " +
					"it calls doEdit. This looks like it is redoing the edit 2 times, which might lead to inconsistencies")
	@Override public void redo() {
		super.redo();
		doEdit();
	}

}
