/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.costEffectiveness;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.dialog.treeadd.TreeADDCellRenderer;
import org.openmarkov.gui.dialog.treeadd.TreeADDEditorPanel;

import java.awt.event.ActionListener;

/**
 * Panel for editing intervention strategy trees in cost-effectiveness analysis.
 * Extends {@link TreeADDEditorPanel} with default read-only behavior.
 */
@SuppressWarnings("serial") public class InterventionEditorPanel extends TreeADDEditorPanel implements ActionListener {

	/**
	 * Creates an intervention editor panel.
	 *
	 * @param cellRenderer the renderer for tree ADD cells
	 * @param node         the node whose intervention to edit
	 * @param readOnly     whether the panel is read-only
	 */
	public InterventionEditorPanel(TreeADDCellRenderer cellRenderer, Node node, boolean readOnly) {
		super(cellRenderer, node, readOnly);
	}

	/**
	 * Creates a read-only intervention editor panel.
	 *
	 * @param cellRenderer the renderer for tree ADD cells
	 * @param node         the node whose intervention to display
	 */
	public InterventionEditorPanel(TreeADDCellRenderer cellRenderer, Node node) {
		super(cellRenderer, node, true);
	}

}
