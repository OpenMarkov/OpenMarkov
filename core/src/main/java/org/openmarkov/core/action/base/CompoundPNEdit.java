/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.java.initialization.Lazy;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
public abstract non-sealed class CompoundPNEdit extends MultiEdit {
    
    // Constructor
    private Lazy<ArrayList<PNEdit>> edits;
    

    public CompoundPNEdit(ProbNet probNet) {
        super(probNet);
        this.edits = new Lazy<>(() -> {
            var res = this.generateEdits();
            res.forEach(PNEdit::markItBelongsToACompoundEdit);
            return res;
        });
    }
    
    /**
     * Returns the sub-edits, generating them on first call. All sub-edits are
     * marked as belonging to this compound edit.
     *
     * @return the list of sub-edits
     */
    public Stream<PNEdit> getEdits() {
        return this.edits.get().stream();
    }
    
    /**
     * Generates the list of sub-edits that compose this compound edit.
     * Called lazily on first access via {@link #getEdits()}.
     *
     * @return the list of sub-edits
     */
    protected abstract ArrayList<PNEdit> generateEdits();
    

    
    
}
