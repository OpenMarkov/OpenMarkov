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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.constraint.NoLinkRestriction;

/******
 * This class validates if a link satisfies the conditions to have a link
 * restriction
 *
 * @author ckonig
 *
 */
public class LinkRestrictionValidator {

	/******
	 * Checks if the link satisfies the conditions to have a link restriction
	 * associated.
	 *
	 * @param link the link
     * @return {@code true} if a link restriction can be applied to the
	 *         link.
	 */
	public static boolean validate(Link<Node> link) {
        
        Node node1 = link.getFrom();
        Node node2 = link.getTo();
		ProbNet net = node1.getProbNet();
        if (!net.hasConstraintOfClass(NoLinkRestriction.class)) {
			if ((node1.getNodeType() == NodeType.CHANCE || node1.getNodeType() == NodeType.DECISION) && (
					node2.getNodeType() == NodeType.CHANCE || node2.getNodeType() == NodeType.DECISION
			) || node2.getNodeType() == NodeType.UTILITY) {

				Variable var1 = node1.getVariable();
				Variable var2 = node2.getVariable();

				if (var1.getVariableType() == VariableType.FINITE_STATES)
					if (node2.getNodeType() != NodeType.UTILITY) {
                        return var2.getVariableType() == VariableType.FINITE_STATES;
					} else {
						return true;
					}
			}

		}
		return false;
	}
}
