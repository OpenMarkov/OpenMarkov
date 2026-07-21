/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Contextual menu displayed when the user right-clicks on a node in the
 * decision tree view. Provides options for expanding, collapsing, and
 * exporting the tree.
 */
public class TreeContextualMenu extends ContextualMenu {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 8556550111033250304L;
    /**
     * Object that represents the item 'Expand next'.
     */
    private JMenuItem expandNextMenuItem = null;
    /**
     * Object that represents the item 'Expand all'.
     */
    private JMenuItem expandAllMenuItem = null;
    /**
     * Object that represents the item 'Open network'.
     */
    private JMenuItem openNetworkMenuItem = null;
    /**
     * Object that represents the item 'Show CEP'.
     */
    private JMenuItem showCEPMenuItem = null;
    /**
     * Object that represents the item 'Save GraphViz'.
     */
    private JMenuItem saveGraphvizMenuItem = null;

    public TreeContextualMenu(ActionListener newListener, boolean enableShowCEP) {
        super(newListener);
        initialize(enableShowCEP);

    }

    /**
     * Construct the menu from the items
     * @param enableShowCEP the enable show cep
     */
    private void initialize(boolean enableShowCEP) {
        add(getExpandNextMenuItem());
        add(getExpandAllMenuItem());
        add(getOpenNetworkMenuItem());
        if (enableShowCEP) {
        	add(getShowCEPMenuItem());
        }
        add(getSaveGraphvizMenuItem());
    }

    /**
     * This method initializes expandNextMenuItem.
     *
     * @return a new 'Expand next' menu item.
     */
    private JMenuItem getExpandNextMenuItem() {
        if (expandNextMenuItem == null) {
            expandNextMenuItem = new LocalizedMenuItem(MenuItemNames.TREE_EXPAND_NEXT_MENUITEM, ActionCommands.TREE_EXPAND_NEXT.getCommandName());
            expandNextMenuItem.addActionListener(listener);
        }
        return expandNextMenuItem;
    }

    /**
     * This method initialises expandAllMenuItem.
     *
     * @return a new 'Expand all' menu item.
     */
    private JMenuItem getExpandAllMenuItem() {
        if (expandAllMenuItem == null) {
            expandAllMenuItem = new LocalizedMenuItem(MenuItemNames.TREE_EXPAND_ALL_MENUITEM, ActionCommands.TREE_EXPAND_ALL.getCommandName());
            expandAllMenuItem.addActionListener(listener);
        }
        return expandAllMenuItem;
    }

    /**
     * This method initialises openNetworkMenuItem.
     *
     * @return a new 'Open network' menu item.
     */
    private JMenuItem getOpenNetworkMenuItem() {
        if (openNetworkMenuItem == null) {
            openNetworkMenuItem = new LocalizedMenuItem(MenuItemNames.TREE_OPEN_NETWORK_MENUITEM, ActionCommands.TREE_OPEN_NETWORK.getCommandName());
            openNetworkMenuItem.addActionListener(listener);
        }
        return openNetworkMenuItem;
    }

    /**
     * This method initialises showCEPMenuItem.
     *
     * @return a new 'Show CEP' menu item.
     */
    private JMenuItem getShowCEPMenuItem() {
        if (showCEPMenuItem == null) {
            showCEPMenuItem = new LocalizedMenuItem(MenuItemNames.TREE_SHOW_CEP_MENUITEM, ActionCommands.TREE_SHOW_CEP.getCommandName());
            showCEPMenuItem.addActionListener(listener);
        }
        return showCEPMenuItem;
    }

    /**
     * This method initialises saveGraphvizMenuItem.
     *
     * @return a new 'Save Graphviz' menu item.
     */
    private JMenuItem getSaveGraphvizMenuItem() {
        if (saveGraphvizMenuItem == null) {
            saveGraphvizMenuItem = new LocalizedMenuItem(MenuItemNames.TREE_SAVE_GRAPHVIZ, ActionCommands.TREE_SAVE_GRAPHVIZ.getCommandName());
            saveGraphvizMenuItem.addActionListener(listener);
        }
        return saveGraphvizMenuItem;
    }

    /**
     * Returns the component that corresponds to an action command.
     *
     * @param actionCommand action command that identifies the component.
     * @return a components identified by the action command.
     */
    @Override
    protected JComponent getJComponentActionCommand(String actionCommand) {
        return switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.TREE_EXPAND_NEXT -> expandNextMenuItem;
            case ActionCommands.TREE_EXPAND_ALL -> expandAllMenuItem;
            case ActionCommands.TREE_OPEN_NETWORK -> openNetworkMenuItem;
            case ActionCommands.TREE_SHOW_CEP -> showCEPMenuItem;
            case null, default -> null;
        };
    }
}
