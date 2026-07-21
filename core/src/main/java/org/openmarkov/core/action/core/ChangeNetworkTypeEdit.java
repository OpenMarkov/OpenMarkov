/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;

/**
 * {@code ChangeNetworkTypeEdit} is a edit that allow to change the
 * {@code NetworkTypeConstraint} object to one network.
 *
 * @author mpalacios
 * @version 1.0 21/12/10
 */
//TODO verify the performance when undo is executed.  
@SuppressWarnings("serial") public class ChangeNetworkTypeEdit extends PNEdit {
    
    /**
     * The current NetworkTypeConstraint associated with the network
     */
    private final NetworkType currentNetworkType;
    
    /**
     * The new NetworkTypeConstraint associated with the network
     */
    private final NetworkType newNetworkType;
    
    /**
     * Creates a new {@code ChangeNetworkTypeEdit} that allow to change the
     * {@code NetworkTypeConstraint} object in the network.
     *
     * @param probNet        the network that will be edited.
     * @param newNetworkType the new {@code NetworkType} object.
     */
    public ChangeNetworkTypeEdit(ProbNet probNet, @NotNull NetworkType newNetworkType) {
        super(probNet);
        this.currentNetworkType = probNet.getNetworkType();
        this.newNetworkType = newNetworkType;
    }
    
    // Methods
    @Override protected void doEdit() throws DoEditException.CannotDoEditException {
        try {
            probNet.setNetworkType(newNetworkType);
        } catch (ConstraintViolatedException e) {
            throw new DoEditException.CannotDoEditException(e, this);
        }
        if (probNet.isMultiagent()) {
            ArrayList<StringWithProperties> agents = new ArrayList<>();
            agents.add(new StringWithProperties(" Agent 1"));
            agents.add(new StringWithProperties(" Agent 2"));
            probNet.setAgents(agents);
        }
    }
    
    @Override public void undo() {
        super.undo();
        try {
            probNet.setNetworkType(currentNetworkType);
            if (!probNet.isMultiagent()) {
                probNet.setAgents(null);
            }
        } catch (ConstraintViolatedException e) {
            throw new UnreachableException(e);
        }
    }
}
