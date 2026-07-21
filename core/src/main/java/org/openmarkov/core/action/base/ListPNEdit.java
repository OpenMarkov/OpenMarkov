/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A compound edit is a complex edition composed of several editions. This is an
 * abstract class.
 */
@SuppressWarnings("serial") public non-sealed class ListPNEdit extends MultiEdit {
    
    private final List<PNEdit> edits;
    
    public ListPNEdit(ProbNet probNet, List<PNEdit> doneEdits) {
        super(probNet);
        this.edits = Collections.unmodifiableList(doneEdits);
        this.edits.forEach(PNEdit::markItBelongsToACompoundEdit);
    }
    
    @Override public Stream<PNEdit> getEdits() {
        return this.edits.stream();
    }
}
