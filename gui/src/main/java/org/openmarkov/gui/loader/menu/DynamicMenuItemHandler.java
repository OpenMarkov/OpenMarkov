/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;

/*
 * Handles the Dynamic activation inside a menu that is loaded by name at
 * runtime @author jlgozalo
 *
 * @version 1.0
 */
public class DynamicMenuItemHandler extends MenuItemAdapter {

	/**
	 * DynamicMenuItemHandler constructor comment.
	 */
	public DynamicMenuItemHandler() {

	}

	/**
	 * This method is called when a MenuItem is activated.
	 */
	@Override public void itemActivated(JMenuItem item, ActionEvent event, String sCommand) {

		System.out.println("Menu item " + item.getName() + " activated dynamically!");
		System.out.println("Command = '" + sCommand + "'");
	}
}