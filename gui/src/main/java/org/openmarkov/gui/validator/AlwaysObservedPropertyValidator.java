/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.validator;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;

public class AlwaysObservedPropertyValidator {
	/*****
	 * Checks if a node can have the alwaysObserved property.
	 * @param node the node
     * @return {@code true} if the node can have the alwaysObserved property.
	 */
	public static boolean validate(Node node) {
        if (!node.getProbNet().hasConstraintOfClass(NoRevelationArc.class)) {
            return node.getNodeType() == NodeType.CHANCE;
		}
		return false;
	}

}
