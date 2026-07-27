/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Every decision criterion must have a name, and no two criteria may share
 * one: criteria are matched by name wherever a utility potential declares
 * which criterion it contributes to.
 * <p>
 * The two exceptions this constraint reports existed for years while its body
 * was a comment, so the application announced the guarantee without imposing
 * it. The annotation also declared the name "NoValidCriterionName" — a double
 * negation contradicting the purpose of the class.
 */
@Constraint(name = "ValidCriterionName", defaultBehavior = ConstraintBehavior.YES) public class ValidCriterionName
		extends PNConstraint {

    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        if (!(probNet instanceof ProbNet net)) {
            // Decision criteria live on ProbNet; a bare graph has none to check.
            return;
        }
        List<Criterion> criteria = net.getDecisionCriteria();
        if (criteria == null) {
            return;
        }
        Set<String> seenNames = new HashSet<>();
        for (Criterion criterion : criteria) {
            String name = criterion.getCriterionName();
            if (name == null || name.isEmpty()) {
                constraintChecker.addException(
                        new ConstraintViolatedException.CriterionNameIsEmpty(this, criterion));
            } else if (!seenNames.add(name)) {
                constraintChecker.addException(
                        new ConstraintViolatedException.CriterionNameIsAlreadyPresent(this, criterion, name));
            }
        }
    }

}
