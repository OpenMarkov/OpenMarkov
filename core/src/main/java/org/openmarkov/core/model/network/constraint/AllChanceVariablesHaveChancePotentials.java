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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.List;

@Constraint(name = "AllChanceVariablesHaveChancePotentials", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class AllChanceVariablesHaveChancePotentials extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        List<Node> chanceNodes = probNet.getNodes(NodeType.CHANCE);
        for (Node chanceNode : chanceNodes) {
            Variable variable = chanceNode.getVariable();
            List<Potential> potentialsNode = chanceNode.getPotentials();
            boolean hasPotential = potentialsNode.stream()
                                                 .anyMatch(potential -> potential.getVariables()
                                                                                 .getFirst() == variable);
            if (!hasPotential) {
                constraintChecker.addException(new ConstraintViolatedException.VariableHasNoPotentials(this, variable));
            }
        }
    }
    
}
