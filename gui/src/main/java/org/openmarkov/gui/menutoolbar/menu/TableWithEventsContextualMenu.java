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

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;

/**
 * Contextual menu when calculating TTE in Event nodes
 * Adapted from UncertaintyContextualMenu
 *
 * @author cyago
 * @version 2 - 29/08/2023 impossible configuration commented
 *
 */
public class TableWithEventsContextualMenu extends ContextualMenu {
    
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 8556550568033250304L;
//	/**
//	 * Object that represents the item 'Set'.
//	 */
//	private JMenuItem setImpossibleMenuItem = null;
//	/**
//	 * Object that represents the item 'Unset'.
//	 */
//	private JMenuItem unsetImpossibleMenuItem = null;
    
    /**
     * Selected to introduce a formula instead of a number
     */
    private JMenuItem addFunctionMenuItem = null;
    
    
    public TableWithEventsContextualMenu(ActionListener newListener) {
        this(newListener, true);
    }
    
    
    public TableWithEventsContextualMenu(ActionListener newListener, boolean setAddFunction) {
        super(newListener);
//		initialize(setAddFunction);
        initialize(true);
    }
    
    /**
     * This method initializes this instance.
     */
    private void initialize(boolean setAddFunction) {

//		add(getSetImpossibleMenuItem());
//		add(getUnsetImpossibleMenuItem());
        if (setAddFunction) {
            add(getAddFunctionMenuItem());
        }
    }


//	/**
//	 * This method initializes assignMenuItem.
//	 *
//	 * @return a new 'Assign' menu item.
//	 */
//	private JMenuItem getSetImpossibleMenuItem() {
//
//		if (setImpossibleMenuItem == null) {
//			//TODO Use new unsetImpossibleMenuItem = new LocalizedMenuItem(MenuItemNames.UNCERTAINTY_REMOVE_MENUITEM,
//			//					ActionCommands.UNCERTAINTY_REMOVE);
//			setImpossibleMenuItem = new LocalizedMenuItem(MenuItemNames.SET_IMPOSSIBLE_CONFIGURATION_MENUITEM,
//					ActionCommands.SET_IMPOSSIBLE_CONFIGURATION);
//			setImpossibleMenuItem.addActionListener(listener);
//		}
//
//		return setImpossibleMenuItem;
//
//	}


//	/**
//	 * This method initializes UnsetImpossibleMenuItem.
//	 *
//	 * @return a new 'Unset Impossible Configuration' menu item.
//	 */
//	private JMenuItem getUnsetImpossibleMenuItem() {
//		//TODO Use new unsetImpossibleMenuItem = new LocalizedMenuItem(MenuItemNames.UNCERTAINTY_REMOVE_MENUITEM,
//		//					ActionCommands.UNCERTAINTY_REMOVE)
//		if (unsetImpossibleMenuItem == null) {
//			unsetImpossibleMenuItem = new LocalizedMenuItem(MenuItemNames.UNSET_IMPOSSIBLE_CONFIGURATION_MENUITEM	,
//					ActionCommands.UNSET_IMPOSSIBLE_CONFIGURATION);
//			unsetImpossibleMenuItem.addActionListener(listener);
//		}
//
//		return unsetImpossibleMenuItem;
//
//	}
    
    /**
     * This method initializes AddFunctionMenuItem.
     *
     * @return a new 'Add Function' menu item.
     */
    private JMenuItem getAddFunctionMenuItem() {
        if (addFunctionMenuItem == null) {
            addFunctionMenuItem = new LocalizedMenuItem(MenuItemNames.ADD_FUNCTION,
                                                        ActionCommands.ADD_FUNCTION.getCommandName());
            addFunctionMenuItem.addActionListener(listener);
        }
        
        return addFunctionMenuItem;
        
        
    }


//	/**
//	 * Returns the component that corresponds to an action command.
//	 *
//	 * @param actionCommand action command that identifies the component.
//	 * @return a components identified by the action command.
//	 */
//	@Override public JComponent getJComponentActionCommand(String actionCommand) {
//
//		JComponent component = null;
//
//        switch (actionCommand) {
//            case ActionCommands.SET_IMPOSSIBLE_CONFIGURATION:
//                component = setImpossibleMenuItem;
//                break;
//            case ActionCommands.UNSET_IMPOSSIBLE_CONFIGURATION:
//                component = unsetImpossibleMenuItem;
//                break;
//			case ActionCommands.ADD_FUNCTION:
//				component = unsetImpossibleMenuItem;
//				break;
//        }
//
//		return component;
//
//	}
    
    /**
     * Returns the component that corresponds to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    @Override public JComponent getJComponentActionCommand(String actionCommand) {
        return switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.ADD_FUNCTION -> addFunctionMenuItem;
            default -> null;
        };
    }
    
}

