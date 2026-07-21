/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.gui.dialog.common.PotentialPanel;
import org.openmarkov.gui.dialog.common.PotentialPanelPlugin;

import javax.swing.BoxLayout;
import java.awt.Color;
import java.util.List;

/**
 * Panel with shows a TreeWithEvents formed by a TreeADDEditorsPanel for each Event Variable of the Potential
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = TreeWithExcludedEventsPotential.class) public class TreeWithExcludedEventsPanel
		extends PotentialPanel {

	/**
	 * Initial height of each TreeADDEditorPanel
	 */
	public static final int EVENT_TREE_HEIGHT = 200;

	/**
	 * The builder object of Tree - ADDs
	 */
	private TreeADDEditorPanel treeADDEditorPanel;



	/**
	 * Potential to be edited
	 */
	private TreeWithExcludedEventsPotential treeWithExcludedEvents;

	/**
	 * Variables with VariableType.EVENT
	 */
	private List<Variable> events;

	/**
	 * Constructor
	 * @param node node edited
	 */
	public TreeWithExcludedEventsPanel(Node node) {
		super();
		try {
			setData(node);
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//			this.setSize(this.getWidth(),EVENT_TREE_HEIGHT*events.size() );
			this.setSize(this.getWidth(),EVENT_TREE_HEIGHT );
		} catch (Exception e ){
			e.printStackTrace();
		}
	}

//	public boolean saveChanges() {
////		SetPotentialEdit setPotentialEdit = new SetPotentialEdit(node, treeADDController.getTreePotential());
////		try {
////			node.getProbNet().doEdit(setPotentialEdit);
////		} catch (ConstraintViolationException e1) {
////			JOptionPane.showMessageDialog(this, e1.getMessage(),
////					StringDatabase.getUniqueInstance().getString("ConstraintViolationException"),
////					JOptionPane.ERROR_MESSAGE);
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
//		return true;
//	}

	/**
	 *
	 * @param node Node to be edited
	 */
	@Override public void setData(Node node) {

		treeWithExcludedEvents = (TreeWithExcludedEventsPotential) node.getPotentials().get(0);
		removeAll();
		TreeADDEditorPanel treeADDEditorPanel = new TreeADDEditorPanel(new TreeADDCellRenderer(node.getProbNet()),node , treeWithExcludedEvents.getNoEventTree());
		add(treeADDEditorPanel);
		setName("nodeTreeADDPotentialPanel");setBackground(Color.blue);
	}

	/**
	 * Sets the panel as readOnly according to parameter readOnly
	 * @param readOnly boolean variable which indicates if the panel is readOnly
	 */
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
//		treeADDController.setReadOnly(readOnly);
	}

	@Override public void close() {
		// TODO Auto-generated method stub
	}
}
