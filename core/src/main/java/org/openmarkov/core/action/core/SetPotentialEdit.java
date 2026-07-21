/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.core.action.base.PNEdit;


/**
 * Edit that replaces a node's potential, respecting link restrictions. Supports undo/redo
 * by storing both the old and new potentials.
 *
 * @author Manuel Arias
 */
public class SetPotentialEdit extends PNEdit {
    
    private final Potential lastPotential;
    
    private final @Nullable Potential newPotential;
    
    private final Node node;
    
    
    /**
     * Creates an edit that will clear the node's potential (set to {@code null}).
     *
     * @param node the node whose potential will be cleared
     */
    public SetPotentialEdit(Node node) {
        super(node.getProbNet());
        this.node = node;
        lastPotential = node.getPotentials().getFirst();
        newPotential = null;
    }
    
    
    /**
     * Creates an edit that will replace the node's current potential with the given one.
     *
     * @param node      the node whose potential will be replaced
     * @param potential the new potential to assign
     */
    public SetPotentialEdit(Node node, Potential potential) {
        super(node.getProbNet());
        this.node = node;
        // If node is a decision node it may have no potential assigned yet.
        if (!node.getPotentials().isEmpty()) {
            lastPotential = node.getPotentials().getFirst();
        } else {
            lastPotential = null;
        }
        newPotential = potential;
    }
    
    
    /**
     * Creates an edit with explicit old and new potentials, useful for redo scenarios.
     *
     * @param node          the node whose potential will be replaced
     * @param lastPotential the previous potential (for undo)
     * @param newPotential  the new potential to assign
     */
    public SetPotentialEdit(Node node,
                            Potential lastPotential,
                            Potential newPotential) {
        super(node.getProbNet());
        this.node = node;
        this.lastPotential = lastPotential;
        this.newPotential = newPotential;
    }
    
    
    // TODO al asignar un potencial tener en cuenta a los padres y a los
    // predecesores informativos que me los va a dar Manolo invocando a una
    // funcion
    
    @Override protected void doEdit() {
        LinkRestrictionPotentialOperations.setPotentialWithRestrictions(node,newPotential);
    }
    
    @Override public void undo() {
        super.undo();
        LinkRestrictionPotentialOperations.setPotentialWithRestrictions(node,lastPotential);
    }
    
    
    @Override public void redo() {
        super.redo();
        LinkRestrictionPotentialOperations.setPotentialWithRestrictions(node,newPotential);
    }
    
}
