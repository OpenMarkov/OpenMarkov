/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.List;

public abstract class DoEditException extends OpenMarkovException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public final PNEdit failedEdit;
    
    protected DoEditException(PNEdit failedEdit) {
        this.failedEdit = failedEdit;
    }
    
    //TODO: This is a wrapper, meaning either design is poor, or the exceptions it encloses
    // could be turned into RuntimeExceptions.
    public static final class CannotDoEditException extends DoEditException {
        public CannotDoEditException(IOpenMarkovException originException, PNEdit failedEdit) {
            super(failedEdit);
            initCause((Exception) originException);
            this.originException = originException;
        }

        public final IOpenMarkovException originException;
        
        @Override public @Nullable String getExceptionMessage() {
            return this.originException.getExceptionMessage();
        }
        
        @Override public @Nullable String getExceptionTitle() {
            return this.originException.getExceptionTitle();
        }
    }
    
    //TODO: Used by RemoveNodeEdit in case a node isn't selected, but... Can that really happen? It is likely this
    // can be removed
	public static final class NodeIsNull extends DoEditException {
		public NodeIsNull(ProbNet probNet, PNEdit failedEdit) {
            super(failedEdit);
            this.probNet = probNet;
        }
        
        public final ProbNet probNet;
    }
    
    //TODO: It is caught and ignored in almost every catch block, probably leading to unexpected
    // bugs.
    // Perhaps it could be turned into a RuntimeException.
	public static final class CannotRemovePotential extends DoEditException {
		public CannotRemovePotential(ProbNet probNet,PNEdit failedEdit, Potential oldPotential) {
            super(failedEdit);
            this.probNet = probNet;
            this.oldPotential = oldPotential;
        }
        
        public final ProbNet probNet;
        public final Potential oldPotential;
    }
	
	public static final class CannotInvertLink extends DoEditException {
		public CannotInvertLink(Node from, Node to, ProbNet probNet, PNEdit failedEdit, List<PNConstraint> unsatisfiedConstraint) {
            super(failedEdit);
            this.from = from;
            this.to = to;
            this.probNet = probNet;
            this.unsatisfiedConstraint = unsatisfiedConstraint;
        }
        
        public final Node from;
        public final Node to;
        public final ProbNet probNet;
        public final List<PNConstraint> unsatisfiedConstraint;
    }
    
}
