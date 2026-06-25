/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.PNEdit;

/**
 * Edit that changes the temporal options (horizon, discount rate, cycle length, etc.)
 * of a dynamic network's inference configuration. Supports undo/redo.
 */
public class TemporalOptionsEdit extends PNEdit {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	private final TemporalOptions oldTemporalOptions;
	private final TemporalOptions newTemporalOptions;

	public TemporalOptionsEdit(ProbNet probNet, TemporalOptions options) {
		super(probNet);
		this.oldTemporalOptions = probNet.getInferenceOptions().getTemporalOptions().clone();
		this.newTemporalOptions = options;
	}
	
	@Override protected void doEdit() {
		probNet.getInferenceOptions().setTemporalOptions(this.newTemporalOptions);

	}
    
    @Override public void undo() {
		super.undo();
		probNet.getInferenceOptions().setTemporalOptions(this.oldTemporalOptions);
	}
	
	@ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG,
			reasonDescription = "This calls super.redo, which will call doEdit if this edit is 'typicalRedo', and then " +
					"it calls doEdit. This looks like it is redoing the edit 2 times, which might lead to inconsistencies")
	@Override public void redo() {
		super.redo();
		doEdit();
	}

}
