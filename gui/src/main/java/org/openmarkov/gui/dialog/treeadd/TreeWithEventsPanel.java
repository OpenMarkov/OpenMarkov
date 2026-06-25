/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.gui.dialog.common.PotentialPanel;
import org.openmarkov.gui.dialog.common.PotentialPanelPlugin;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel with shows a TreeWithEvents formed by a TreeADDEditorsPanel for each Event Variable of the Potential
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = TreeWithEventsPotential.class) public class TreeWithEventsPanel
		extends PotentialPanel {

	/**
	 * Initial height of each TreeADDEditorPanel
	 */
	public static final int EVENT_TREE_HEIGHT = 200;

	/**
	 * The builder object of Tree - ADDs
	 */
	private ArrayList<TreeADDEditorPanel>  treeADDControllerList;

	/**
	 * The node edited
	 */
	private Node node;

	/**
	 * Potential to be edited
	 */
	private TreeWithEventsPotential treeWithEvents;

	/**
	 * Variables with VariableType.EVENT
	 */
	private List<Variable> events;

	/**
	 * Constructor
	 * @param node node edited
	 */
	public TreeWithEventsPanel(Node node) {
		super();
		try {
			setData(node);
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.setSize(this.getWidth(),EVENT_TREE_HEIGHT*events.size() );
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

		this.node = node;
		treeWithEvents = (TreeWithEventsPotential) node.getPotentials().get(0);
		events = treeWithEvents.getEvents();
		treeADDControllerList = new ArrayList<>();
		removeAll();
		for (Variable event: events){
			TreeADDEditorPanel treeADDEditorPanel = new TreeADDEditorPanel(new TreeADDCellRenderer(node.getProbNet()),node , treeWithEvents.getTrees().get(event));
			add(treeADDEditorPanel);
			treeADDEditorPanel.setBorder(BorderFactory.createEmptyBorder());
			treeADDEditorPanel.setPreferredSize(new Dimension(this.getWidth(), EVENT_TREE_HEIGHT));
			setName("nodeTreeADDPotentialPanel");setBackground(Color.blue);
			treeADDControllerList.add (treeADDEditorPanel);
		}

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
