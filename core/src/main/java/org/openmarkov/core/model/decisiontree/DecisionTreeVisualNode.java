/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.decisiontree;

import java.util.List;

/**
 * Interface that represents an element in a decision tree.
 * Both nodes and branches implement this interface: it is what the interface
 * walks to paint the tree.
 */
public sealed interface DecisionTreeVisualNode permits DecisionTreeBranch, DecisionTreeNode {
	/**
	 * Returns the list of children of this element. An element with no children
	 * returns an empty list, never {@code null} and never a list containing
	 * {@code null}: the tree is walked by recursing over these children, so a
	 * placeholder among them would fail one frame below instead of here.
	 *
	 * @return List of children, possibly empty.
	 */
	List<DecisionTreeVisualNode> getChildren();

}
