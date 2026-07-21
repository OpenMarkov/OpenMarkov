
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.constraint.ValidCriterionName;

import java.util.ArrayList;
import java.util.List;

/**
 * Edit that adds, removes, or renames a decision criterion in a multicriteria network.
 */
@SuppressWarnings("serial") public class DecisionCriteriaEdit extends PNEdit {
    private final StateAction stateAction;
    private final List<Criterion> lastCriteria;
    private final Criterion modifiedCriterion;
    private final @Nullable String newName;
    
    public DecisionCriteriaEdit(ProbNet probnet, StateAction stateAction, Criterion modifiedCriterion, String newName) {
        super(probnet);
        this.modifiedCriterion = modifiedCriterion;
        
        if (stateAction == StateAction.ADD) {
            this.newName = modifiedCriterion.getCriterionName();
        } else if (stateAction == StateAction.RENAME) {
            this.newName = newName;
        } else {
            this.newName = null;
        }
        this.stateAction = stateAction;
        this.lastCriteria = new ArrayList<>(probnet.getDecisionCriteria());
    }
    
    @Override public void checkConstraintsWillBeMet(ConstraintChecker constraintChecker) {
        if (probNet.getConstraintOfClass(ValidCriterionName.class) instanceof ValidCriterionName constraint) {
            switch (this.stateAction) {
                case REMOVE, MODIFY_VALUE_INTERVAL, MODIFY_DELIMITER_INTERVAL, DOWN, UP -> {
                }
                case ADD, RENAME -> {
                    String name = this.newName.trim().toLowerCase();
                    if (name.isEmpty()) {
                        constraintChecker.addException(new ConstraintViolatedException.CriterionNameIsEmpty(constraint, modifiedCriterion));
                    }
                    for (Criterion criterion : this.lastCriteria) {
                        if (criterion.getCriterionName().trim().toLowerCase().equals(name)) {
                            constraintChecker.addException(new ConstraintViolatedException.CriterionNameIsAlreadyPresent(constraint, modifiedCriterion, name));
                        }
                    }
                }
            }
        }
    }
    
    @Override protected void doEdit() {
        List<Criterion> criteria = this.probNet.getDecisionCriteria();
        switch (this.stateAction) {
            case ADD -> criteria.add(modifiedCriterion);
            case REMOVE -> criteria.remove(modifiedCriterion);
            case DOWN, UP -> {
                int criterionIndex = criteria.indexOf(modifiedCriterion);
                Criterion swapPos = criteria.get(criterionIndex);
                int movingPos = this.stateAction == StateAction.DOWN ? 1 : -1;
                criteria.set(criterionIndex, criteria.get(criterionIndex + movingPos));
                criteria.set(criterionIndex + movingPos, swapPos);
            }
            case RENAME -> this.modifiedCriterion.setCriterionName(newName);
            default -> {
            }
        }
    }
    
    public String getNewName() {
        return newName;
    }
    
    public StateAction getStateAction() {
        return stateAction;
    }
    
    public List<Criterion> getLastCriteria() {
        return lastCriteria;
    }
    
    @Override public void undo() {
        super.undo();
        probNet.setDecisionCriteria(lastCriteria);
        
    }
}
