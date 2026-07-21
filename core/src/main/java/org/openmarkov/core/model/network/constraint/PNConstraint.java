/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.developmentStaticAnalysis.mutability.Immutable;


/**
 * A constraint is a condition that a model must fulfill.<p>
 * This class implements {@code PNEditListener} because like
 * that all the classes that implement this interface will be able to receive
 * the same messages than {@code UndoableEditListener} and they will be
 * able to be referenced with same identifier.
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public abstract class PNConstraint implements PNEditListener, ClassLocalizable, Comparable<PNConstraint>, Immutable {
    
    /**
     * @param probNet {@code ProbNet}
     * @param constraintChecker {@code ConstraintChecker}
     */
    public abstract void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker);
    
    public final boolean isMetBy(ProbNet probNet) {
        ConstraintChecker constraintChecker = new ConstraintChecker(probNet);
        checkProbNet(probNet, constraintChecker);
        try {
            constraintChecker.buildAndThrow();
            return true;
        } catch (ConstraintViolatedException e) {
            return false;
        }
    }
    
    @Override public String toString() {
        return this.localize();
    }
    
    @Override public boolean equals(Object paramObject) {
        if (this.getClass() != paramObject.getClass()) {
            return false;
        }
        return this.compareTo(this.getClass().cast(paramObject)) == 0;
    }
    
    @Override public int compareTo(@NotNull PNConstraint o) {
        return this.getClass().getName().compareTo(o.getClass().getName());
    }
    
    @Override public int hashCode() {
        return 17 + this.getClass().hashCode();
    }
    
}
