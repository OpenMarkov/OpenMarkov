package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;


public class RemoveConstraintEdit extends PNEdit {

    private PNConstraint constraint;

    public RemoveConstraintEdit(ProbNet probNet, PNConstraint constraint) {
        super(probNet);
        this.constraint = constraint;

    }

    // Methods
    @Override protected void doEdit() {

        if (constraint != null) {
            probNet.removeConstraint(constraint);
        }

    }

    @Override public void undo() {
        super.undo();

        if (constraint != null) {
            probNet.addConstraint(constraint);
        }

    }
}
