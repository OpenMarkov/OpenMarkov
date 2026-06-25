/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

/**
 *
 */
package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Contextual menu displayed when the user right-clicks on a node that has
 * parameter uncertainty. Provides options for editing the uncertainty distribution.
 *
 * @author mpalacios
 */
public class UncertaintyContextualMenu extends ContextualMenu {

	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 8556550568033250304L;
	/**
	 * Object that represents the item 'Cut'.
	 */
	private JMenuItem assignMenuItem = null;
	/**
	 * Object that represents the item 'Copy'.
	 */
	private JMenuItem editMenuItem = null;
	/**
	 * Object that represents the item 'Remove'.
	 */
	private JMenuItem removeMenuItem = null;

	public UncertaintyContextualMenu(ActionListener newListener) {
		super(newListener);
		// TODO Auto-generated constructor stub
		initialize();
	}

	/**
	 * This method initializes this instance.
	 */
	private void initialize() {

		add(getAssignMenuItem());
		add(getEditMenuItem());
		//addSeparator();
		add(getRemoveMenuItem());

	}

	/**
	 * This method initializes assignMenuItem.
	 *
	 * @return a new 'Assign' menu item.
	 */
	private JMenuItem getAssignMenuItem() {

		if (assignMenuItem == null) {
			assignMenuItem = new LocalizedMenuItem(MenuItemNames.UNCERTAINTY_ASSIGN_MENUITEM,
                                                   ActionCommands.UNCERTAINTY_ASSIGN.getCommandName());
			assignMenuItem.addActionListener(listener);
		}

		return assignMenuItem;

	}

	/**
	 * This method initializes editMenuItem.
	 *
	 * @return a new 'Edit' menu item.
	 */
	private JMenuItem getEditMenuItem() {

		if (editMenuItem == null) {
			editMenuItem = new LocalizedMenuItem(MenuItemNames.UNCERTAINTY_EDIT_MENUITEM,
                                                 ActionCommands.UNCERTAINTY_EDIT.getCommandName());
			editMenuItem.addActionListener(listener);
		}

		return editMenuItem;
	}

	/**
	 * This method initializes removeMenuItem.
	 *
	 * @return a new 'Remove' menu item.
	 */
	private JMenuItem getRemoveMenuItem() {

		if (removeMenuItem == null) {
			removeMenuItem = new LocalizedMenuItem(MenuItemNames.UNCERTAINTY_REMOVE_MENUITEM,
                                                   ActionCommands.UNCERTAINTY_REMOVE.getCommandName());
			removeMenuItem.addActionListener(listener);
		}

		return removeMenuItem;

	}

	/**
	 * Returns the component that corresponds to an action command.
	 *
	 * @param actionCommand action command that identifies the component.
	 * @return a components identified by the action command.
	 */
	@Override public JComponent getJComponentActionCommand(String actionCommand) {
        JComponent component = switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.UNCERTAINTY_ASSIGN -> assignMenuItem;
            case ActionCommands.UNCERTAINTY_EDIT -> editMenuItem;
            case ActionCommands.UNCERTAINTY_REMOVE -> removeMenuItem;
            case null, default -> null;
        };
        return component;

	}

}

