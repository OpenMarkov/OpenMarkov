/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.dialog.common.PrefixedOtherPropertiesTablePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Panel to set other additionalProperties in the node not managed by OpenMarkov
 * directly. It will have a scroll table with two visible columns and a third
 * column to store the object type. Also, a "+" and "-" buttons to manage the
 * insert and delete additionalProperties in the right side and the "Accept" and
 * "Cancel" buttons on the bottom, and a HTML comment foreground field
 *
 * @author jlgozalo
 * @version 1.0 jlgozalo initial
 */
public class NodeOtherPropsTablePanel extends JPanel implements ItemListener {
	/**
	 * serial uid
	 */
	private static final long serialVersionUID = 1047978130482205148L;
	/**
	 * String database
	 */
	protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	/**
	 * label for the table to show the other additionalProperties
	 */
	private JLabel jLabelOtherPropertiesTable = null;
	/**
	 * table to show the other additionalProperties
	 */
	private PrefixedOtherPropertiesTablePanel otherPropertiesTablePanel = null;
	//private NodeOtherPropertiesKeyTablePanel otherPropertiesTablePanel = null;
	/**
	 * Object where all information will be saved.
	 */
	private Node nodeProperties = null;
	/**
	 * Specifies if the node whose additionalProperties are edited is new.
	 */
    private boolean newNode;

	/**
	 * constructor without construction parameters
	 */
	public NodeOtherPropsTablePanel() {
		this(true);
	}

	/**
	 * This method initialises this instance.
	 *
	 * @param newNode true if the node is a new node; otherwise false
	 */
	public NodeOtherPropsTablePanel(final boolean newNode) {
		this.newNode = newNode;
		setName("NodeOtherPropsTablePanel");
			initialize();
	}

	/**
	 * Get the node Properties in this panel
	 *
	 * @return the nodeProperties
	 */
	public Node getNodeProperties() {
		return nodeProperties;
	}
	
	/**
	 * Set the node additionalProperties in this panel with the provided ones
	 *
	 * @param nodeProperties the nodeProperties to set
	 */
	public void setNodeProperties(final Node nodeProperties) {
		this.nodeProperties = nodeProperties;
		otherPropertiesTablePanel.setProperties(nodeProperties);
	}

	/**
	 * @return the newNode
	 */
	public boolean isNewNode() {
		return newNode;
	}

	/**
	 * @param newNode the newNode to set
	 */
	public void setNewNode(boolean newNode) {
		this.newNode = newNode;
	}

	/**
	 * init the layout for this panel. Firstly, the horizontal layout will be
	 * set with one row (parallel group) in the first row the table with
	 * additionalProperties and the Add/Delete buttons and to force the buttons
	 * to be aligned, we will do: - sequential group (gap, table, gap,
	 * buttonsParalellGroup) Then, the vertical layout will be set with the two
	 * columns (parallel group) in the first column, the table in the second
	 * column, the Add/Delete buttons
	 */
	private void initialize() {
		setPreferredSize(new Dimension(700, 375));
        final GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addContainerGap()
						.addComponent(getJLabelOtherPropertiesTable(), GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(getOtherPropertiesTablePanel(), GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(61, 61, 61)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addContainerGap().addGroup(
						groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(getOtherPropertiesTablePanel(), GroupLayout.Alignment.LEADING,
										GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
								.addComponent(getJLabelOtherPropertiesTable(), GroupLayout.Alignment.LEADING))
						.addContainerGap(89, Short.MAX_VALUE)));
		setLayout(groupLayout);
	}

	/**
	 * This method initialises jLabelOtherPropertiesTable
	 *
	 * @return the JLabel for the Other Properties Table
	 */
	private JLabel getJLabelOtherPropertiesTable() {
		if (jLabelOtherPropertiesTable == null) {
			jLabelOtherPropertiesTable = new JLabel();
			jLabelOtherPropertiesTable.setName("jLabelOtherPropertiesTable");
			jLabelOtherPropertiesTable.setVerticalTextPosition(SwingConstants.TOP);
			jLabelOtherPropertiesTable.setVerticalAlignment(SwingConstants.TOP);
			jLabelOtherPropertiesTable.setText("a Label :");
			jLabelOtherPropertiesTable
					.setText(stringDatabase.getString("NodeOtherPropertiesPanel.jLabelOtherPropertiesTable.Text"));
		}
		return jLabelOtherPropertiesTable;
	}

	/**
	 * This method initialises otherPropertiesTable.
	 *
	 * @return the PrefixedKeyTablePanel for the Other additionalProperties of
	 * the network
	 */
	private PrefixedOtherPropertiesTablePanel getOtherPropertiesTablePanel() {
		if (otherPropertiesTablePanel == null) {
            String[] columnNames = new String[]{stringDatabase.getString(
                    "NodeOtherPropertiesPanel.OtherPropertiesTablePanel.PropertyIdColumn"),
					stringDatabase.getString(
                            "NodeOtherPropertiesPanel.OtherPropertiesTablePanel.PropertyNameColumn"),
					stringDatabase.getString(
                            "NodeOtherPropertiesPanel.OtherPropertiesTablePanel.PropertyValueColumn"),
					// dialogStringResource
                    // .getValuesInAString("NodeOtherPropertiesPanel.OtherPropertiesTablePanel.PropertyTypeColumn)
			};
			// ,
			otherPropertiesTablePanel = new PrefixedOtherPropertiesTablePanel(columnNames, new Object[][] {},
					stringDatabase.getString("NodeOtherPropertiesPanel.OtherPropertiesTablePanel.PropertyIdColumn.Prefix"),
					true, nodeProperties);
			/*otherPropertiesTablePanel = new NodeOtherPropertiesKeyTablePanel(columnNames, new Object[][] {},
					stringDatabase.getString("NodeOtherPropertiesPanel.OtherPropertiesTablePanel.PropertyIdColumn.Prefix"),
					true, nodeProperties);*/
			/*otherPropertiesTablePanel = new PrefixedOtherPropertiesTablePanel(columnNames, new Object[][] {},
					stringDatabase
							.getString("NodeOtherPropertiesPanel.OtherPropertiesTablePanel.PropertyIdColumn.Prefix"),
					true);*/
		}
		return otherPropertiesTablePanel;
	}

	/**
	 * Invoked when an item has been selected.
	 *
	 * @param e event information.
	 */
	@Override public void itemStateChanged(ItemEvent e) {
	}

	/**
	 * This method fills the content of the fields from a NodeProperties object.
	 *
	 * @param nodeProperties object from where load the information.
	 */
	public void setFieldsFromProperties(Node nodeProperties) {
		getOtherPropertiesTablePanel().setProperties(nodeProperties);
		///.setData( additionalProperties.getOtherProperties() );
	}
}
