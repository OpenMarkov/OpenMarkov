/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.loader.menu;


/*
 * Interface
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

/**
 * Menu XML uses this interface to notify client objects of ToolBar Component
 * events.
 *
 * @author jlgozalo
 * @version 1.0
 */
public interface ToolBarItemHandler {

	/**
	 * Called when a ToolBarItem is activated.
	 */
	void itemActivated(JComponent item, ActionEvent event, String sCommand);

	/**
	 * Called when a ToolBarItem is deselected.
	 */
	void itemDeselected(JComponent item, ItemEvent event, String sCommand);

	/**
	 * Called when a ToolBarItem is selected.
	 */
    void itemSelected(JComponent item, ItemEvent event, String sCommand);
}