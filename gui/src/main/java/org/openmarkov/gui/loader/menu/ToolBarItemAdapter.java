/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

/*
 * This class will do the work when the items were activated/deselected/checked
 * @author jlgozalo
 *
 * @version 1.0
 */
public class ToolBarItemAdapter implements ToolBarItemHandler {

	/**
	 * constructor
	 */
	public ToolBarItemAdapter() {

	}

	/**
	 * Called when a ToolBatItem(component) is activated.
	 */
	@Override public void itemActivated(JComponent item, ActionEvent event, String sCommand) {

		System.out.println("que pasaaaaa");
	}

	/**
	 * Called when a ToolBatItem(component) is deselected.
	 */
	@Override public void itemDeselected(JComponent item, ItemEvent event, String sCommand) {

	}

	/**
	 * Called when a ToolBatItem(component) is selected.
	 */
	@Override public void itemSelected(JComponent item, ItemEvent event, String sCommand) {

	}
}