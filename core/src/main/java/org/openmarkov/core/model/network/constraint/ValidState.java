/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;

import java.util.HashMap;

/**
 * Checks that the state field is filled and there isn't any node with the same
 * name.
 */
public class ValidState extends PNConstraint {
    
    /**
     * This method checks that the state field is filled and there isn't any
     * node with the same name.
     *
     * @param constraintChecker the constraint checker to add exceptions to
     * @param newState the new state name to check
     * @param node the node to check
     * @param stateAction the state action being performed
     */
    public void checkState(ConstraintChecker constraintChecker, String newState, Node node, StateAction stateAction) {
        switch (stateAction) {
            case ADD, RENAME -> {
                boolean isEmpty = (newState == null) || newState.isEmpty();
                if (isEmpty) {
                    constraintChecker.addException(new ConstraintViolatedException.NameOfStateCannotBeEmpty(this, node));
                } else if (ValidState.existState(newState, node)) {
                    constraintChecker.addException(new ConstraintViolatedException.StateAlreadyExists(this, node, newState));
                }
            }
            case REMOVE, MODIFY_VALUE_INTERVAL, MODIFY_DELIMITER_INTERVAL, DOWN, UP -> {
            }
        };
    }
    
    /**
     * This method checks if exists the state specified.
     *
     * @param node  node to search.
     * @param state state to be checked
     *
     * @return true if the state exists; otherwise, false.
     */
    private static boolean existState(String state, Node node) {
        for (State states : node.getVariable().getStates()) {
            if (states.getName().equalsIgnoreCase(state)) {
                return true;
            }
        }
        return false;
    }
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node node : probNet.getNodes()) {
            for (State state : node.getVariable().getStates()) {
                if ((state.getName() == null) || (state.getName().contentEquals(""))) {
                    constraintChecker.addException(new ConstraintViolatedException.NameOfStateCannotBeEmpty(this, node));
                }
            }
            HashMap<String, Integer> statesAndNumber = new HashMap<>();
            for (State state : node.getVariable().getStates()) {
                statesAndNumber.put(state.getName(), statesAndNumber.getOrDefault(state.getName(), 0) + 1);
            }
            for (var entry : statesAndNumber.entrySet()) {
                if (entry.getValue() > 1) {
                    constraintChecker.addException(new ConstraintViolatedException.StateDuplicated(this, node, entry.getKey()));
                }
            }
        }
    }
    
}
