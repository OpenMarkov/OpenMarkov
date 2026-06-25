/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.OnlyContinuousVariables;
import org.openmarkov.core.model.network.constraint.OnlyDiscreteVariables;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.action.base.PNEdit;

import java.util.List;

/**
 * Changes the constraints that determinate the types of variables (discrete, continuous, etc.) that the ProbNet
 * accepts.
 *
 * @author mpalacios based
 * @version 1.0
 */

@SuppressWarnings("serial") public class VariableTypeConstraintEdit extends PNEdit {
    // Attributes
    
    private final PNConstraint newVariableTypeConstraint;
    private PNConstraint lastConstraint;
    
    // Constructor
    
    /**
     * This method creates a new VariableTypeConstraintEdit
     *
     * @param probNet                   the network that will be edited
     *                                  {@code ProbNet}
     * @param newVariableTypeConstraint the new constraint. If null, the network
     *                                  will do not have constraint about variables, i.e, works with continuous
     *                                  and discrete variables.
     */
    public VariableTypeConstraintEdit(ProbNet probNet, PNConstraint newVariableTypeConstraint) {
        super(probNet);
        this.newVariableTypeConstraint = newVariableTypeConstraint;
        List<PNConstraint> constraints = probNet.getConstraints();
        for (PNConstraint constraint : constraints) {
            if (constraint instanceof OnlyDiscreteVariables || constraint instanceof OnlyContinuousVariables) {
                lastConstraint = constraint;
                break;
            }
        }
        
    }
    
    @Override
    public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (newVariableTypeConstraint instanceof OnlyDiscreteVariables constraint) {
            for (Node node : probNet.getNodes()) {
                if (node.getVariable().getVariableType() != VariableType.FINITE_STATES) {
                    constraintChecker.addException(new ConstraintViolatedException.OnlyDiscreteVariablesAllowed(constraint, node.getVariable()));
                }
            }
        }
        if (newVariableTypeConstraint instanceof OnlyContinuousVariables constraint) {
            for (Node node : probNet.getNodes()) {
                if (node.getVariable().getVariableType() != VariableType.NUMERIC) {
                    constraintChecker.addException(new ConstraintViolatedException.OnlyContinuousVariablesAllowed(constraint, node.getVariable()));
                }
            }
        }
    }
    
    // Methods
    @Override protected void doEdit() {
        
        if (lastConstraint != null) {
            probNet.removeConstraint(lastConstraint);
        }
        
        if (newVariableTypeConstraint != null) {
            probNet.addConstraint(newVariableTypeConstraint);
        }
        
    }
    
    @Override public void undo() {
        super.undo();
        
        if (newVariableTypeConstraint != null) {
            probNet.removeConstraint(newVariableTypeConstraint);
        }
        
        if (lastConstraint != null) {
            probNet.addConstraint(lastConstraint);
        }
        
    }
    
}
