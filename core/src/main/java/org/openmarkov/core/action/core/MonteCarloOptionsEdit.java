package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.ProbNet;

import javax.swing.undo.CannotUndoException;

/**
 * This class contains the information for doEdit() of MonteCarloOptions.
 * 04/10/2023 FIXME check if it complies with OM wiki
 * @author cmyago
 * @version 1.0 25/08/2019
 */
public class MonteCarloOptionsEdit extends PNEdit {

    private MonteCarloOptions oldMonteCarloOptions;
	private MonteCarloOptions newMonteCarloOptions;

	public MonteCarloOptionsEdit(ProbNet probNet, MonteCarloOptions options) {
		super(probNet);
		this.oldMonteCarloOptions = probNet.getInferenceOptions().getMonteCarloOptions().clone();
		this.newMonteCarloOptions = options;
	}

	@Override public void doEdit() throws DoEditException {
		probNet.getInferenceOptions().setMonteCarloOptions(this.newMonteCarloOptions);

	}

	@Override public void undo() throws CannotUndoException {
		super.undo();
		probNet.getInferenceOptions().setMonteCarloOptions(this.oldMonteCarloOptions);
	}

	@Override public void redo() {
		super.redo();
		try {
			doEdit();
		} catch (DoEditException e) {
			e.printStackTrace();
		}
	}

}
