/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author jlgozalo
 * @version 1.0 07/12/2008
 */
public class DefaultMenuItemListener implements ActionListener, ItemListener {

	/**
	 * Default constructor
	 */
	public DefaultMenuItemListener() {

	}

	/**
	 * Dispatch check/uncheck events from CheckboxMenuItems.
	 */
	@Override public void itemStateChanged(ItemEvent e) {

		Object oSource = e.getSource();
        if (oSource instanceof JMenuItem mi) {
            MenuItemHandler mih = MenuHandlersTable.menuitemhandlerFind(mi);
			if (mih != null) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					mih.itemSelected(mi, e, mi.getActionCommand());
				} else {
					mih.itemDeselected(mi, e, mi.getActionCommand());
				}
			}
		}
	}

	/**
	 * When a MenuItem is activated, this method finds and calls the MenuItem's
	 * MenuItemHandler.
	 */
	@Override public void actionPerformed(ActionEvent e) {

		Object oSource = e.getSource();
        if (oSource instanceof JMenuItem mi) {
            MenuItemHandler mih = MenuHandlersTable.menuitemhandlerFind(mi);
			if (mih != null) {
				mih.itemActivated(mi, e, mi.getActionCommand());
			}
		}
	}

}
