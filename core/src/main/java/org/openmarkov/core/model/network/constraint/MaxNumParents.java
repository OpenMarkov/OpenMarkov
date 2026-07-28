/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;

/**
 * Limits how many parents a node may have. Used while learning a network: the number of
 * parameters of a conditional probability table grows exponentially with the number of
 * parents, so bounding it trades a little bias for much less variance in the estimates.
 *
 * <p>This constraint is deliberately <em>not</em> annotated with
 * {@link org.openmarkov.core.model.network.constraint.annotation.Constraint}. That annotation
 * makes {@link ConstraintManager} build the constraint by reflection through its no-argument
 * constructor, and this one cannot have such a constructor: the bound has to come from the
 * caller. Whoever needs it creates it explicitly, as
 * {@code probNet.addConstraint(new MaxNumParents(k))}.
 */
public class MaxNumParents extends PNConstraint {
    
    private final int maxNumParents;
    
    public MaxNumParents(int maxNumParents) {
        this.maxNumParents = maxNumParents;
    }
    
    public int getMaxNumParents() {
        return this.maxNumParents;
    }
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node child : probNet.getNodes()) {
            int numParents = probNet.getNumParents(child);
            if (numParents > maxNumParents) {
                constraintChecker.addException(new ConstraintViolatedException.NodeHasMoreParentsThanAllowed(this, child, numParents, maxNumParents));
            }
        }
    }
    
    @Override public int compareTo(@NotNull PNConstraint o) {
        var difference = super.compareTo(o);
        if (difference != 0) {
            return difference;
        }
        MaxNumParents other = (MaxNumParents) o;
        return Integer.valueOf(this.maxNumParents).compareTo(other.maxNumParents);
    }
}
