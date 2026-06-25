/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.PNEdit;

/**
 * Changes an old potential for a new potential
 */
public class PotentialChangeEdit extends PNEdit {
    
    // Attribute
    protected Potential newPotential;
    
    protected Potential oldPotential;
    
    private final Node node;
    
    // Constructor
    
    /**
     * @param node         {@code Node}
     * @param oldPotential {@code Potential}
     * @param newPotential {@code Potential}
     */
    public PotentialChangeEdit(Node node, Potential oldPotential, Potential newPotential) {
        super(node.getProbNet());
        this.newPotential = newPotential;
        this.oldPotential = oldPotential;
        this.node = node;
    }
    
    @Override protected void doEdit() throws DoEditException.CannotRemovePotential {
        if (oldPotential != null && !node.removePotential(oldPotential)) {
            throw new DoEditException.CannotRemovePotential(probNet, this, oldPotential);
        }
        node.addPotential(newPotential);
    }
    
    
    /**
     * @return A {@code String} with the variables of both potentials.
     */
    public String toString() {
        return "ChangePotentialEdit: " + oldPotential.getVariables() + " --> " + newPotential.getVariables();
    }
    
    public Potential getNewPotential() {
        return newPotential;
    }
    
    public Potential getOldPotential() {
        return oldPotential;
    }
    
}
