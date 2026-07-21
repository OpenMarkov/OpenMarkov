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
public class MenuItemAdapter implements MenuItemHandler {

	/**
	 * the parent Frame where this menu item is located
	 */
	public JFrame aParentFrame = null;

	/**
	 * constructor
	 */
	public MenuItemAdapter() {

	}

	/**
	 * Called when a JMenuItem is activated.
	 */
	@Override public void itemActivated(JMenuItem item, ActionEvent event, String sCommand) {

		System.out.println("Item activado " + item.getName() + " y evento " + event.toString());
	}

	/**
	 * Called when a CheckboxMenuItem is deselected.
	 */
	@Override public void itemDeselected(JMenuItem item, ItemEvent event, String sCommand) {

	}

	/**
	 * Called when a CheckboxMenuItem is selected.
	 */
	@Override public void itemSelected(JMenuItem item, ItemEvent event, String sCommand) {

	}
}