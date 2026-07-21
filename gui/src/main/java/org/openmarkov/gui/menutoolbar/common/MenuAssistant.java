/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class implements the basic features of a class that assists to another
 * to manage various menus and toolbars. It manages only menus and toolbars that
 * implements the interface 'MenuToolBarBasic'. The management of another types
 * of menus and toolbars (that implements, for example, the interface
 * 'ZoomMenuToolBar'), must be implemented in subclasses.
 *
 * @author jmendoza
 */
public class MenuAssistant {

	/**
	 * Basic menus and toolbars. Only have the options to enabled, select a set
     * foreground.
	 */
    protected final List<MenuToolBarBasic> basicMenus;

	/**
	 * Constructor that registers the array of menus.
	 *
	 * @param newBasicMenus array of basic menus and toolbars.
	 */
	public MenuAssistant(List<MenuToolBarBasic> newBasicMenus) {

		if (newBasicMenus == null) {
			basicMenus = new ArrayList<MenuToolBarBasic>();
		} else {
			basicMenus = newBasicMenus;
		}
	}

	/**
	 * Varargs constructor that registers the given menus.
	 *
	 * @param newBasicMenus menus and toolbars to manage
	 */
	public MenuAssistant(MenuToolBarBasic... newBasicMenus) {

		basicMenus = new ArrayList<>();
        Collections.addAll(basicMenus, newBasicMenus);
	}

	/**
	 * Adds a menu or toolbar to the managed list.
	 *
	 * @param newBasicMenu the menu or toolbar to add
	 */
	public void addMenu(MenuToolBarBasic newBasicMenu) {
		basicMenus.add(newBasicMenu);
	}

	/**
	 * Removes a menu or toolbar from the managed list.
	 *
	 * @param newBasicMenu the menu or toolbar to remove
	 */
	public void removeMenu(MenuToolBarBasic newBasicMenu) {
		basicMenus.remove(newBasicMenu);
	}

	/**
	 * Selects or unselects an option identified by an action command on the
	 * menus and toolbars.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to select the option, false to unselect.
	 */
	public void setOptionSelected(String actionCommand, boolean b) {
		for (MenuToolBarBasic menu : basicMenus) {
			menu.setOptionSelected(actionCommand, b);
		}
    }
    
    /**
     * Selects or unselects an option identified by an action command on the
     * menus and toolbars.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to select the option, false to unselect.
     */
    public void setOptionSelected(ActionCommands actionCommand, boolean b) {
        for (MenuToolBarBasic menu : basicMenus) {
            menu.setOptionSelected(actionCommand.getCommandName(), b);
        }
    }

	/**
	 * Enables or disabled an option identified by an action command on the
	 * menus and toolbars.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to enable the option, false to disable.
	 */
	public void setOptionEnabled(String actionCommand, boolean b) {
		for (MenuToolBarBasic menu : basicMenus) {
			menu.setOptionEnabled(actionCommand, b);
		}
    }
    
    /**
     * Enables or disabled an option identified by an action command on the
     * menus and toolbars.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to enable the option, false to disable.
     */
    public void setOptionEnabled(ActionCommands actionCommand, boolean b) {
        for (MenuToolBarBasic menu : basicMenus) {
            menu.setOptionEnabled(actionCommand.getCommandName(), b);
        }
    }

	/**
	 * Enables or disabled a group of options on the menus and toolbars.
	 *
	 * @param actionCommandGroup array of action command.
	 * @param b                  true to enable the options, false to disable.
	 */
	public void setOptionEnabled(String[] actionCommandGroup, boolean b) {
		for (String actionCommand : actionCommandGroup) {
			for (MenuToolBarBasic menu : basicMenus) {
				menu.setOptionEnabled(actionCommand, b);
			}
		}
    }
    
    /**
     * Enables or disabled a group of options on the menus and toolbars.
     *
     * @param actionCommandGroup array of action command.
     * @param b                  true to enable the options, false to disable.
     */
    public void setOptionEnabled(ActionCommands[] actionCommandGroup, boolean b) {
        for (ActionCommands actionCommand : actionCommandGroup) {
            for (MenuToolBarBasic menu : basicMenus) {
                menu.setOptionEnabled(actionCommand.getCommandName(), b);
            }
        }
    }

	/**
     * Adds a foreground to the label of an option identified by an action command on
	 * the menus and toolbars.
	 *
	 * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label of the options. If null, nothing is
	 *                      added.
	 */
	public void addOptionText(String actionCommand, String text) {

		for (MenuToolBarBasic menu : basicMenus) {
			menu.addOptionText(actionCommand, text);
		}

	}

	/**
	 * Changes the caption of menu item identified by an action command on
	 * the menus and toolbars.
	 *
	 * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label of the options. If null, nothing is
	 *                      added.
	 */
	public void setText(String actionCommand, String text) {

		for (MenuToolBarBasic menu : basicMenus) {
			menu.setText(actionCommand, text);
		}

	}
}
