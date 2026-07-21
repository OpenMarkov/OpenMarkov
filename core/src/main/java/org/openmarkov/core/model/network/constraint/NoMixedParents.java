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
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * This class implements the NoMixedParents constraint, which establishes that all the parents
 * of a utility node belong to only one of these two sets of parents:
 * - chance and decision nodes
 * - utility nodes
 *
 * @author ckonig
 */
@Constraint(name = "NoMixedParents", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class NoMixedParents extends PNConstraint {
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
        for (Node utilityNode : probNet.getNodes(NodeType.UTILITY)) {
            List<Node> parents = probNet.getParents(utilityNode);
            this.checkParents(utilityNode, parents, constraintChecker);
        }
    }
    
    enum MixedParentsGroup {
        UTILITY,
        CHANCE_OR_DECISION,
        WRONG_GROUP;
        
        static MixedParentsGroup of(NodeType nodeType) {
            return switch (nodeType) {
                case CHANCE, DECISION, EVENT -> MixedParentsGroup.CHANCE_OR_DECISION;
                case UTILITY -> MixedParentsGroup.UTILITY;
                case SV_SUM, SV_PRODUCT -> MixedParentsGroup.WRONG_GROUP;
            };
        }
    }
    
    public void checkParents(Node child, List<Node> parents, ConstraintChecker constraintChecker) {
        if (child.getNodeType() != NodeType.UTILITY) {
            return;
        }
        var nodesByType = parents.stream().collect(groupingBy(node -> MixedParentsGroup.of(node.getNodeType())));
        var wrongNodes = nodesByType.remove(MixedParentsGroup.WRONG_GROUP);
        if (wrongNodes != null) {
            for (Node wrongNode : wrongNodes) {
                constraintChecker.addException(new ConstraintViolatedException.MixedParentDoesntAllowThisNodeType(this, child, wrongNode));
            }
        }
        if (nodesByType.size() <= 1) {
            return;
        }
        //At this point, the nodesByType map contains UTILITY and CHANCE_OR_DECISION groups
        constraintChecker.addException(new ConstraintViolatedException.MixedParentContainsMoreThanOneSet(
                this,
                child,
                nodesByType.remove(MixedParentsGroup.CHANCE_OR_DECISION),
                nodesByType.remove(MixedParentsGroup.UTILITY)
        ));
    }
    
    public static boolean parentNodeIsNotMixed(Node parent) {
        NodeType parentNodeType = parent.getNodeType();
        boolean metCondition = parentNodeType == NodeType.UTILITY || parentNodeType == NodeType.CHANCE || parentNodeType == NodeType.DECISION;
        return metCondition;
    }
    
}
