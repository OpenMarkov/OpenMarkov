/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PotentialNetwork;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.List;

@Constraint(name = "UtilityNodes", defaultBehavior = ConstraintBehavior.OPTIONAL) public class UtilityNodes
		extends PNConstraint {

    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Node> utilityNodes = probNet.getNodes(NodeType.UTILITY);
		int numUtilityNodes = utilityNodes.size();
		if (numUtilityNodes == 0) {
            constraintChecker.addException(new ConstraintViolatedException.NetworkHasNoUtilityNodes(this, (ProbNet) probNet));
        } // check same number of utility nodes and utility potentials
        List<Potential> potentials = ((PotentialNetwork) probNet).getPotentials();
        int numUtilityPontentials = 0;
        for (Potential potential : potentials) {
            if (potential.getVariable(0).getDecisionCriterion() != null) {
                numUtilityPontentials++;
            }
        }
        if (numUtilityPontentials != numUtilityNodes) {
            constraintChecker.addException(new ConstraintViolatedException.NumOfPotentialsMismatchesNumOfUtilities(this, numUtilityNodes, numUtilityPontentials));
        }
    }

}
