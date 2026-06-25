/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.localize;

import org.openmarkov.gui.loader.element.IconBind;

import javax.swing.*;

/**
 * A {@link JMenuItem} that automatically sets its label and mnemonic from
 * the localized string database using the given name as the resource key.
 */
@SuppressWarnings("serial") public class LocalizedMenuItem extends JMenuItem {

    /**
     * Creates a localized menu item with foreground and mnemonic resolved from the string database.
     *
     * @param name          the resource key used for label and mnemonic lookup
     * @param actionCommand the action command string for this menu item
     */
    public LocalizedMenuItem(String name, String actionCommand) {
        this.setName(name);
        this.setText(MenuLocalizer.getLabel(name));
        this.setMnemonic(MenuLocalizer.getMnemonic(name).charAt(0));
        this.setActionCommand(actionCommand);
	}
    
    /**
     * Creates a localized menu item with optional icon and keyboard accelerator.
     *
     * @param name          the resource key used for label and mnemonic lookup
     * @param actionCommand the action command string
     * @param iconBind      the icon to display, or {@code null} for no icon
     * @param keyStroke     the keyboard accelerator, or {@code null} for none
     */
    public LocalizedMenuItem(String name, String actionCommand, IconBind iconBind, KeyStroke keyStroke) {
        this(name, actionCommand);
        if (iconBind != null) {
            this.setIcon(iconBind.icon());
        }
        if (keyStroke != null) {
            this.setAccelerator(keyStroke);
        }
	}

}
