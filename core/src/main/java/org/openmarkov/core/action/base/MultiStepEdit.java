/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
@SuppressWarnings("serial") public abstract non-sealed class MultiStepEdit extends MultiEdit {
    
    private final ArrayList<PNEdit> edits;
    
    public MultiStepEdit(ProbNet probNet) {
        super(probNet);
        this.edits = new ArrayList<>();
    }
    
    @Override public Stream<PNEdit> getEdits() {
        return this.edits.stream();
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
    }
    
    @Override protected final void doEdit() throws DoEditException {
        StepExecuter stepExecuter = new StepExecuter();
        try {
            this.edits.clear();
            this.doMultiStepEdit(stepExecuter);
            this.edits.addAll(stepExecuter.executedEdits);
        } catch (DoEditException e) {
            for (PNEdit editToUndo : stepExecuter.executedEdits.reversed()) {
                editToUndo.undo();
            }
            throw e;
        }
    }
    
    protected abstract void doMultiStepEdit(StepExecuter stepExecuter) throws DoEditException;
    
    public static class StepExecuter {
        
        private final ArrayList<PNEdit> executedEdits;
        
        private StepExecuter() {
            this.executedEdits = new ArrayList<>();
        }
        
        public void execute(PNEdit edit) throws DoEditException {
            edit.markItBelongsToACompoundEdit();
            edit.executeEdit();
            this.executedEdits.add(edit);
        }
        
        public Stream<PNEdit> currentlyExecutedEdits() {
            return executedEdits.stream();
        }
        
    }
    
}
