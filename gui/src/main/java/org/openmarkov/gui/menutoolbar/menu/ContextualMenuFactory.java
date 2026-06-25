/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.gui.graphic.VisualElement;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.menutoolbar.common.MenuAssistant;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.awt.event.ActionListener;

/**
 * This class has a reference to all the contextual menus of the application.
 *
 * @author jmendoza
 */
public class ContextualMenuFactory implements MenuToolBarBasic {

	/**
	 * Constant that indentifies the network contextual menu.
	 */
	public static final int NETWORK = 0;

	/**
	 * Constant that indentifies the node contextual menu.
	 */
	public static final int NODE = 1;

	/**
	 * Constant that indentifies the link contextual menu.
	 */
	public static final int LINK = 2;

	/**
	 * Constant that indentifies the instance contextual menu.
	 */
	public static final int INSTANCE = 3;

	/**
	* Constant that identifies the tree contextual menu.
	*/
	public static final int TREE = 4;

	/**
	 * Contextual menu that has the options of a whole network.
	 */
	private ContextualMenu networkContextualMenu = null;

	/**
	 * Contextual menu that has the options of a node.
	 */
	private ContextualMenu nodeContextualMenu = null;

	/**
	 * Contextual menu that has the options of a link.
	 */
	private ContextualMenu linkContextualMenu = null;
    
    /**
	 * Contextual menu that has the options of a node in a decision tree.
	 */
	private ContextualMenu treeContextualMenu = null;


	/**
	 * Assistant that manages all the contextual menus.
	 */
	private MenuAssistant menuAssistant = null;

	/**
	 * Listener for all the contextual menus.
	 */
    private final ActionListener listener;

	/**
	 * Creates a new instance.
	 *
	 * @param newListener listener of the user's actions.
	 */
	public ContextualMenuFactory(ActionListener newListener) {

		listener = newListener;
		initialize();
	}

	/**
	 * This method initialises the instance.
	 */
	private void initialize() {

		menuAssistant = new MenuAssistant();
	}

	/**
	 * This method initialises networkContextualMenu.
	 *
	 * @return the network panel contextual menu.
	 */
	public ContextualMenu getNetworkContextualMenu(boolean canBeExpanded) {

		networkContextualMenu = new NetworkContextualMenu(listener, canBeExpanded);
		networkContextualMenu.setName("networkContextualMenu");
		menuAssistant.addMenu(networkContextualMenu);

		return networkContextualMenu;
	}

	/**
	 * Creates and registers a node contextual menu for the specified visual node.
	 *
	 * @param selectedNode the visual node that was right-clicked
	 * @param panel        the editor panel containing the node
	 * @return the node contextual menu
	 */
	private ContextualMenu getNodeContextualMenu(VisualNode selectedNode, NetworkEditorPanel panel) {

		menuAssistant.removeMenu(nodeContextualMenu);
		nodeContextualMenu = new NodeContextualMenu(listener, selectedNode, panel);
		nodeContextualMenu.setName("nodeContextualMenu");
		menuAssistant.addMenu(nodeContextualMenu);
		return nodeContextualMenu;
	}

	/**
	 * Creates and registers a link contextual menu for the specified visual link.
	 *
	 * @param selectedLink the visual link that was right-clicked
	 * @param panel        the editor panel containing the link
	 * @return the link contextual menu
	 */
	private ContextualMenu getLinkContextualMenu(VisualLink selectedLink, NetworkEditorPanel panel) {

		menuAssistant.removeMenu(linkContextualMenu);
		linkContextualMenu = new LinkContextualMenu(listener, selectedLink, panel);
		linkContextualMenu.setName("linkContextualMenu");
		menuAssistant.addMenu(linkContextualMenu);
		return linkContextualMenu;
	}
    
    /**
	 * Creates and registers a contextual menu for decision tree nodes.
	 *
	 * @param enableShowCEP whether the "Show CEP" option should be enabled
	 * @return the tree contextual menu
	 */
	private ContextualMenu getStandardTreeContextualMenu(boolean enableShowCEP) {

		menuAssistant.removeMenu(treeContextualMenu);
		treeContextualMenu = new TreeContextualMenu(listener,enableShowCEP);
		treeContextualMenu.setName("treeContextualMenu");
		menuAssistant.addMenu(treeContextualMenu);
		return treeContextualMenu;
	}

	// TODO OOPN end

	/**
	 * Enables or disabled an option identified by an action command.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to enable the option, false to disable.
	 */
	@Override public void setOptionEnabled(String actionCommand, boolean b) {

		menuAssistant.setOptionEnabled(actionCommand, b);
	}

	/**
	 * Selects or unselects an option identified by an action command. Only
	 * selects or unselects the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to select the option, false to unselect.
	 */
	@Override public void setOptionSelected(String actionCommand, boolean b) {

		menuAssistant.setOptionSelected(actionCommand, b);
	}

	/**
     * Adds a foreground to the label of an option identified by an action command.
     * Only adds a foreground to the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
     * @param text          foreground to add to the label of the options. If null, nothing is
	 *                      added.
	 */
	@Override public void addOptionText(String actionCommand, String text) {

		menuAssistant.addOptionText(actionCommand, text);
	}

	/**
     * Changes the foreground of menu item
	 *
	 * @param actionCommand action command that identifies the option.
     * @param text          foreground to set to the Item.
	 */
	@Override public void setText(String actionCommand, String text) {

		menuAssistant.setText(actionCommand, text);
	}

	/**
	 * Returns an instance of a pop up menu given the class and some additional
	 * info
	 * @return The contextual menu
	 */
	public ContextualMenu getContextualMenu(VisualElement selectedElement, NetworkEditorPanel panel) {
		ContextualMenu contextualMenu = null;
		if (VisualNode.class.isAssignableFrom(selectedElement.getClass())) {
			contextualMenu = getNodeContextualMenu((VisualNode) selectedElement, panel);
		} else if (VisualLink.class.isAssignableFrom(selectedElement.getClass())) {
			contextualMenu = getLinkContextualMenu((VisualLink) selectedElement, panel);
		}
		return contextualMenu;
	}

	/**
	 * Returns the tree contextual menu. Currently only one tree menu type is supported.
	 *
	 * @param enableShowCEP whether the "Show CEP" option should be enabled
	 * @return the tree contextual menu
	 */
	public ContextualMenu getTreeContextualMenu(boolean enableShowCEP) {
		return getStandardTreeContextualMenu(enableShowCEP);
	}


}
