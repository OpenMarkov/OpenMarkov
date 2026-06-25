/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.common;

import javax.swing.*;
import java.util.Enumeration;

/**
 * This class implements the methods to modify the state and texts of the
 * components of the menus and toolbars.
 *
 * @author jmendoza
 * @version 1.0 jmendoza
 */
public class MenuToolBarBasicImpl {

	/**
	 * private constructor for a class with only static members
	 */
	private MenuToolBarBasicImpl() {

	}

	/**
	 * Enables or disabled a component.
	 *
	 * @param component component to be enabled or disabled.
	 * @param b         true to enable the component, false to disable.
	 */
	public static void setOptionEnabled(JComponent component, boolean b) {

		if (component != null) {
			component.setEnabled(b);
			if (!b) {
				clearSelection(component);
			}
		}

	}

	/**
	 * This method clears the selection of a button group if all the elements in
	 * it are disabled. This operation is carried out only if the component is
	 * an instance of AbstractButton, its model is an instance of
	 * JToggleButton.ToggleButtonModel and it belongs to a button group.
	 *
	 * @param component component whose group will be processed.
	 */
	private static void clearSelection(JComponent component) {
        
        ButtonModel model;
        ButtonGroup group;
		boolean enabled = false;
        Enumeration<AbstractButton> elements;

		if (component instanceof AbstractButton) {
			model = ((AbstractButton) component).getModel();
			if (model instanceof JToggleButton.ToggleButtonModel) {
                group = model.getGroup();
				if (group != null) {
					elements = group.getElements();
					while (!enabled && elements.hasMoreElements()) {
						enabled = elements.nextElement().isEnabled();
					}
					if (!enabled) {
						group.clearSelection();
					}
				}
			}
		}

	}

	/**
	 * Selects or unselects a component. Only selects or unselects the component
	 * if it is an instance of AbstractButton.
	 *
	 * @param component component to be selected or unselected.
	 * @param b         true to select the component, false to unselect.
	 */
	public static void setOptionSelected(JComponent component, boolean b) {

		if (component != null) {
			if (component instanceof AbstractButton) {
				((AbstractButton) component).setSelected(b);
			}
		}

	}

	/**
     * Adds a foreground to the default label of a component. Only adds a foreground to the
	 * component if it is an instance of AbstractButton.
	 *
     * @param component    component whose foreground is going to be modified.
	 * @param defaultLabel default label of the component.
     * @param text         foreground to add to the label of the options. If null, nothing is
	 *                     added.
	 */
	public static void addOptionText(JComponent component, String defaultLabel, String text) {
        
        AbstractButton abstractButton;
        String newText;

		if (component != null) {
			if (component instanceof AbstractButton) {
				abstractButton = ((AbstractButton) component);
				newText = defaultLabel;
				newText = ((newText == null) ? "" : newText) + " " + ((text == null) ? "" : text);
				abstractButton.setText(newText);
			}
		}

	}

	/**
	 * Changes the caption to menu item.
	 *
     * @param component  component whose foreground is going to be modified.
     * @param newCaption The new foreground to set.
	 */
	public static void setText(JComponent component, String newCaption) {
		if (component != null) {
            if (component instanceof JMenuItem jMenuItem) {
                if (newCaption != null) {
					jMenuItem.setText(newCaption);
				}
			}
		}

	}
}
