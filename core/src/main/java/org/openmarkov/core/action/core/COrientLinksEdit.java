/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.linkEdits.OrientLinkEdit;

import java.util.ArrayList;

/**
 * Compound edit that orients (directs) multiple undirected links at once.
 */
@SuppressWarnings("serial") public class COrientLinksEdit extends CompoundPNEdit {
    
    public COrientLinksEdit(ProbNet probNet, ArrayList<OrientLinkEdit> edits) {
        super(probNet);
        this.orientLinkEdits = edits;
    }
    
    private final ArrayList<OrientLinkEdit> orientLinkEdits;
    
    // Methods
    @Override public ArrayList<PNEdit> generateEdits() {
        return (ArrayList<PNEdit>) (ArrayList) orientLinkEdits;
    }
    
    @Override public boolean equals(Object arg0) {
        boolean sameInformation = true;
        if (arg0 instanceof COrientLinksEdit editToCompare) {
            for (PNEdit edit : editToCompare.getEdits().toList()) {
                sameInformation = sameInformation && getEdits().toList().contains(edit);
            }
            for (PNEdit edit : getEdits().toList()) {
                sameInformation = sameInformation && editToCompare.getEdits().toList().contains(edit);
            }
        } else {
            sameInformation = false;
        }
        return sameInformation;
    }
    
}
