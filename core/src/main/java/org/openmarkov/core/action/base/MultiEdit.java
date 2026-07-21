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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
public abstract sealed class MultiEdit extends PNEdit permits CompoundPNEdit, ListPNEdit, MultiStepEdit {
    
    public MultiEdit(ProbNet probNet) {
        super(probNet);
    }
    
    public abstract Stream<PNEdit> getEdits();
    
    /**
     * Generate edits and does them
     *
     * @throws DoEditException DoEditException
     */
    @Override protected void doEdit() throws DoEditException {
        ArrayList<PNEdit> doneEdits = new ArrayList<>((int) this.getEdits().count());
        try {
            for (PNEdit edit : this.getEdits().toList()) {
                edit.executeEdit();
                doneEdits.add(edit);
            }
        } catch (DoEditException e) {
            for (PNEdit editToUndo : doneEdits.reversed()) {
                editToUndo.undo();
            }
            throw e;
        }
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        for (PNEdit pnEdit : this.getEdits().toList()) {
            pnEdit.checkConstraintsWillBeMet(constraintChecker);
        }
    }
    
    
    @Override public final void redo() {
        this.getEdits().forEach(PNEdit::redo);
        this.setTypicalRedo(false);
        super.redo();
    }
    
    @Override public final void undo() {
        List<PNEdit> edits = this.getEdits().toList();
        edits.reversed().forEach(PNEdit::undo);
        super.undo();
    }
    
}
