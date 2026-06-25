/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;

public class NullEdit extends PNEdit {
    
    /**
     * Serial ID
     */
    private static final long serialVersionUID = 1L;
    private final int numEdit;
    
    public NullEdit(ProbNet probNet, int numEdit) {
        super(probNet);
        this.numEdit = numEdit;
    }
    
    @Override protected void doEdit() {
    }
    
    @Override public void undo() {
        super.undo();
        
    }
    
    public int getNumEdit() {
        return numEdit;
    }
    
}
