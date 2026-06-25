/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Base Panel for node panels
 *
 * @author jlgozalo
 * @version 1.0 jlgozalo
 */
public class NodeBasePanel extends JPanel implements ItemListener {
	/**
	 * serial uid
	 */
	private static final long serialVersionUID = 1047978130482205148L;

	/**
	 * Specifies if the node whose additionalProperties are edited is new.
	 */
	private boolean newNode = false;

	/**
	 * constructor without construction parameters
	 */
	public NodeBasePanel() {
		init();
	}

	/**
	 * This method initialises this instance.
	 *
	 * @param newNode true if the node is a new node; otherwise false
	 */
	public NodeBasePanel(final boolean newNode) {
		this.newNode = newNode;
		setName("NodeBasePanel");
		init();
	}

	/**
	 * set the visual aspect of the panel
	 */
	private void init() {
		initialize();
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
     * {@code Initialize}
	 * <p>
	 * initialize the layout for this panel
	 */
	private void initialize() {
        final GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 500, Short.MAX_VALUE));
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 375, Short.MAX_VALUE));
		setLayout(groupLayout);
	}

	/**
	 * Invoked when an item has been selected.
	 *
	 * @param e event information.
	 */
	@Override public void itemStateChanged(ItemEvent e) {
	}
}
