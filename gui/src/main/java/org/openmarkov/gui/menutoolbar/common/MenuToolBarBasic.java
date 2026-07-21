/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.common;

/**
 * This interface defines the methods that all menus and toolbars must
 * implement.
 *
 * @author jmendoza
 * @version 1.1 jlgozalo Fixing semantic errors as this class is an interface
 */
public interface MenuToolBarBasic {
    
    /**
	 * Enables or disabled an option identified by an action command.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to enable the option, false to disable.
	 */
	void setOptionEnabled(String actionCommand, boolean b);

	/**
	 * Selects or unselects an option identified by an action command. Only
	 * selects or unselects the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to select the option, false to unselect.
	 */
	void setOptionSelected(String actionCommand, boolean b);

	/**
     * Adds a foreground to the label of an option identified by an action command.
     * Only adds a foreground to the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label of the options. If null, nothing is
	 *                      added.
	 */
	void addOptionText(String actionCommand, String text);

	/**
     * Sets a foreground of an option identified by an action command.
     * Only adds a foreground to the components that are JMenuItem.
	 *
	 * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label of the options. If null, nothing is
	 *                      added.
	 */
	void setText(String actionCommand, String text);
}
