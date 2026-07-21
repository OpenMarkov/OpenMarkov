/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.ProbNet;

/**
 * Abstract class that defines the basic attribute (a {@code ProbNet})
 * and operations of editions.
 */
public abstract class PNEdit implements ClassLocalizable {
    //Start interface
    
    /**
     * This method acts as a contract saying no constraint will be violated after the edit is done.
     * <p>
     * If this method returns a {@link ConstraintViolatedException}, then it means
     * this edit should not be applied, as it will violate that constraint.
     */
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
    }
    
    /**
     * Checks all constraints and throws if any are violated.
     *
     * @throws ConstraintViolatedException if at least one constraint would be violated
     */
    public final void tryConstraintsWillBeMet() throws ConstraintViolatedException {
        ConstraintChecker constraintChecker = new ConstraintChecker(probNet);
        this.checkConstraintsWillBeMet(constraintChecker);
        constraintChecker.buildAndThrow();
    }
    
    /**
     * Returns whether all constraints will be met after this edit.
     *
     * @return {@code true} if no constraints would be violated
     */
    public final boolean constraintsWillBeMet() {
        try {
            this.tryConstraintsWillBeMet();
            return true;
        } catch (ConstraintViolatedException ex) {
            return false;
        }
    }
    
    /**
     * Abstract method to be defined in derived classes
     *
     * @throws DoEditException DoEditException
     */
    protected abstract void doEdit() throws DoEditException;
    
    /**
     * Validates constraints, executes the edit, records it in the undo history
     * if applicable, and notifies listeners.
     *
     * @throws DoEditException if the edit fails during execution
     * @throws ConstraintViolatedException if constraints would be violated
     */
    public final void executeEdit() throws DoEditException {
        PNESupport pneSupport = getProbNet().getPNESupport();
        try {
            ConstraintChecker constraintChecker = new ConstraintChecker(probNet);
            this.checkConstraintsWillBeMet(constraintChecker);
            constraintChecker.buildAndThrow();
        } catch (ConstraintViolatedException ex) {
            for (PNEditListener listener : pneSupport.getListeners()) {
                listener.onEditViolatesConstraints(this, ex);
            }
            throw new DoEditException.CannotDoEditException(ex, this);
        }
        for (PNEditListener listener : pneSupport.getListeners()) {
            listener.beforeEditExecutes(this);
        }
        try {
            this.doEdit();
        } catch (DoEditException e) {
            for (PNEditListener listener : pneSupport.getListeners()) {
                listener.onEditFailed(this, e);
            }
            throw e;
        }
        if (pneSupport.isWithUndo() && !belongsToACompoundEdit) {
            pneSupport.getCurrentEditHistory().addEdit(this);
        }
        if (!belongsToACompoundEdit) {
            for (PNEdit flatEdit : PNESupport.flattenEdit(this)) {
                for (PNEditListener listener : pneSupport.getListeners()) {
                    listener.afterEditExecutes(flatEdit);
                }
            }
        }
    }
    
    //End interface
    
    
    // Attributes
    /**
     * {@code ProbNet} over witch the operations are defined.
     */
    protected ProbNet probNet;
    
    private boolean typicalRedo = true;

    // Constructor
    
    /**
     * @param probNet the probabilistic network this edit operates on
     */
    public PNEdit(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    // Methods
    
    /**
     * @return probNet. {@code ProbNet}
     */
    public final ProbNet getProbNet() {
        return probNet;
    }
    
    public final void setProbNet(ProbNet probNet) {
        this.probNet = probNet;
    }
    
    protected final void setTypicalRedo(boolean redo) {
        typicalRedo = redo;
    }
    
    public void redo() {
        if (typicalRedo) {
            try {
                doEdit();
            } catch (DoEditException e) {
                throw new UnreachableException(e);
            }
        } else {
            typicalRedo = true;
        }
    }
    
    public void undo() {
    }
    
    private boolean belongsToACompoundEdit = false;
    
    public boolean belongsToACompoundEdit() {
        return this.belongsToACompoundEdit;
    }
    
    /**
     * Marks this edit as belonging to a compound edit, so it will not be
     * independently recorded in the undo history.
     */
    public final void markItBelongsToACompoundEdit() {
        this.belongsToACompoundEdit = true;
    }
    
    @Override public String toString() {
        return this.localize();
    }
}
