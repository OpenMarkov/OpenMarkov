/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.HashSet;
import java.util.Set;

/**
 * The states of a variable of finite states or divided into intervals have names, and no two of them share one,
 * as {@link State#sameName} compares names. The only state of a numeric variable has no name.
 */
@Constraint(name = "ValidState", defaultBehavior = ConstraintBehavior.YES)
public class ValidState extends PNConstraint {
    
    /**
     * Checks the name a state of {@code node} will have after adding it or renaming it.
     *
     * @param renamed the state being renamed, which may keep its own name; {@code null} when adding
     */
    public void checkState(ConstraintChecker constraintChecker, String newName, Node node, StateAction stateAction,
                           @Nullable State renamed) {
        if (stateAction != StateAction.ADD && stateAction != StateAction.RENAME) {
            return;
        }
        Variable variable = node.getVariable();
        if (variable.hasNamedStates() && (newName == null || newName.isBlank())) {
            constraintChecker.addException(new ConstraintViolatedException.NameOfStateCannotBeEmpty(this, node));
        } else if (variable.isStateNameTaken(newName, renamed)) {
            constraintChecker.addException(new ConstraintViolatedException.StateAlreadyExists(this, node, newName));
        }
    }
    
    /** Checks the states {@code node} will have. */
    public void checkStates(ConstraintChecker constraintChecker, Node node, State[] states) {
        if (!node.getVariable().hasNamedStates()) {
            return;
        }
        Set<String> seen = new HashSet<>();
        for (State state : states) {
            if (state.getName() == null || state.getName().isBlank()) {
                constraintChecker.addException(new ConstraintViolatedException.NameOfStateCannotBeEmpty(this, node));
            } else if (!seen.add(State.nameKey(state.getName()))) {
                constraintChecker.addException(new ConstraintViolatedException.StateDuplicated(this, node, state.getName()));
            }
        }
    }
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node node : probNet.getNodes()) {
            checkStates(constraintChecker, node, node.getVariable().getStates());
        }
    }
    
}
