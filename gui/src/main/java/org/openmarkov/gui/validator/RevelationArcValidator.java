/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.validator;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;

/*****
 * This class validates if a link satisfies the conditions to be a revelation
 * arc.
 *
 * @author ckonig
 *
 */
public class RevelationArcValidator {
	/**********
	 * Checks if the link satisfies the conditions to have the condition of
	 * revelation arc
	 *
	 * @param link the link
     * @return {@code true} if a link restriction can be applied to the
	 *         link.
	 */
	public static boolean validate(Link<Node> link) {
        
        Node node1 = link.getFrom();
        Node node2 = link.getTo();
		ProbNet net = node1.getProbNet();
        if (!net.hasConstraintOfClass(NoRevelationArc.class)) {
            
            return (node1.getNodeType() == NodeType.CHANCE || node1.getNodeType() == NodeType.DECISION) && (
                    node2.getNodeType() == NodeType.CHANCE
            );
		}
		return false;
	}

}
