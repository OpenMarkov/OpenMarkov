/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.NoEventNodes;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.mode.NodeEditionMode;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class implements a contextual menu that shows when a user click on the
 * background of a network panel.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.2.1 - cmyago 26/02/2023 - Added "Expand network" menu item added (fixing regression).
 */
public class NetworkContextualMenu extends ContextualMenu {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 3673127766586232832L;
    /**
     * Object that represents the item 'Paste'.
     */
    private JMenuItem pasteMenuItem = null;
    /**
     * Object that represents the item 'Network additionalProperties'.
     */
    private JMenuItem networkPropertiesMenuItem = null;
    
    
    private JMenuItem expandNetworkMenuItem;
    private JMenuItem teCriterionMenuItem;
    
    private JMenuItem expandNetworCEkMenuItem;
    private final boolean canBeExpanded;
    
    /**
     * This constructor creates a new instance.
     *
     * @param newListener object that listens to the menu events.
     */
    public NetworkContextualMenu(ActionListener newListener, boolean canBeExpanded) {
        super(newListener);
        this.canBeExpanded = canBeExpanded;
        initialize();
    }
    
    /**
     * This method initializes this instance.
     */
    private void initialize() {
        add(getCreateElementsMenuItem());
        add(getPasteMenuItem());
        addSeparator();
        add(getNetworkPropertiesMenuItem());
        if (canBeExpanded) {
            addSeparator();
            add(getTECriterionMenuItem());
            addSeparator();
            add(getExpandedNetworkMenuItem());
        }
    }
    
    record NodeMenuGenerator(NodeType nodeType, String title, String name, String tooltip, Icon icon, ActionCommands actionCommand,
                             boolean enabled) {
    }
    
    ;
    
    private JMenuItem getCreateElementsMenuItem() {
        NetworkEditorPanel networkEditorPanel = Objects.requireNonNull(MainPanel.getCurrentNetworkEditorPanel());
        var currentNetwork = networkEditorPanel.getProbNet();
        boolean isEditionMode = networkEditorPanel
                .getEditorPanel()
                .getVisualNetwork()
                .getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION;
        
        
        return new JMenuItemBuilder("Add")
                .withItems(
                        Stream.of(
                                new NodeMenuGenerator(NodeType.CHANCE, "Chance node", "jmenuItemCreateChanceNode", "Create a chance node", IconBind.CHANCE_ENABLED.icon(),
                                                      ActionCommands.CHANCE_CREATION, true),
                                new NodeMenuGenerator(NodeType.DECISION, "Decision node", "jmenuItemCreateDecisionNode","Create a decision node",
                                                      IconBind.DECISION_ENABLED.icon(),
                                                      ActionCommands.DECISION_CREATION, !currentNetwork.hasConstraintOfClass(OnlyChanceNodes.class)),
                                new NodeMenuGenerator(NodeType.UTILITY, "Utility node", "jmenuItemCreateUtilityNode","Create an utility node",
                                                      IconBind.UTILITY_ENABLED.icon(),
                                                      ActionCommands.UTILITY_CREATION, !currentNetwork.hasConstraintOfClass(OnlyChanceNodes.class)),
                                
                                new NodeMenuGenerator(NodeType.EVENT, "Event node", "jmenuItemCreateEventNode","Create an event node",
                                                      IconBind.EVENT_ENABLED.icon(),
                                                      ActionCommands.EVENT_CREATION, !currentNetwork.hasConstraintOfClass(OnlyChanceNodes.class) && !currentNetwork.hasConstraintOfClass(NoEventNodes.class))
                        
                        
                        ).map(nodeMenuGenerator -> new JMenuItemBuilder(nodeMenuGenerator.title)
                                .withName(nodeMenuGenerator.name)
                                .withIcon(nodeMenuGenerator.icon)
                                .withActionCommand(nodeMenuGenerator.actionCommand)
                                .withTooltip(nodeMenuGenerator.tooltip)
                                .enabled(isEditionMode && nodeMenuGenerator.enabled)
                                .onClick(e -> NodeEditionMode.createNode(currentNetwork, nodeMenuGenerator.nodeType,
                                                                         new Point2D.Double(this.getRelativeShownLocationX(), this.getRelativeShownLocationY()), networkEditorPanel))
                                .build())
                
                )
                .build();
    }
    
    
    /**
     * This method initializes pasteMenuItem.
     *
     * @return a new 'ExpandedNetworkMenuItem' menu item.
     */
    private JMenuItem getExpandedNetworkMenuItem() {
        if (expandNetworkMenuItem == null) {
            expandNetworkMenuItem = new LocalizedMenuItem(MenuItemNames.EXPAND_NETWORK_MENUITEM,
                                                          ActionCommands.EXPAND_NETWORK.getCommandName());
            expandNetworkMenuItem.addActionListener(listener);
        }
        return expandNetworkMenuItem;
    }
    
    /**
     * This method initializes pasteMenuItem.
     *
     * @return a new 'Temporal Evolution by Criterion' menu item.
     */
    private JMenuItem getTECriterionMenuItem() {
        if (teCriterionMenuItem == null) {
            teCriterionMenuItem = new LocalizedMenuItem(MenuItemNames.TEMPORAL_EVOLUTION_BY_CRITERION_MENUITEM,
                                                        ActionCommands.TEMPORAL_EVOLUTION_BY_CRITERION.getCommandName());
            teCriterionMenuItem.addActionListener(listener);
        }
        return teCriterionMenuItem;
    }


//	/**
//	 * This method initializes pasteMenuItem.
//	 *
//	 * @return a new 'Paste' menu item.
//	 */
//	private JMenuItem getExpandedNetworkCEMenuItem() {
//		if (expandNetworCEkMenuItem == null) {
//			expandNetworCEkMenuItem = new LocalizedMenuItem(MenuItemNames.EXPAND_NETWORK_CE_MENUITEM,
//					ActionCommands.EXPAND_NETWORK_CE);
//			expandNetworCEkMenuItem.addActionListener(listener);
//		}
//		return expandNetworCEkMenuItem;
//	}
    
    /**
     * This method initializes pasteMenuItem.
     *
     * @return a new 'Paste' menu item.
     */
    private JMenuItem getPasteMenuItem() {
        if (pasteMenuItem == null) {
            pasteMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_PASTE_MENUITEM, ActionCommands.CLIPBOARD_PASTE.getCommandName(),
                                                  IconBind.PASTE_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
            NetworkEditorPanel networkEditorPanel = MainPanel.getCurrentNetworkEditorPanel();
            
            pasteMenuItem.addActionListener((ignored) -> {
                try {
                    networkEditorPanel.pasteFromClipboard(new Point2D.Double(this.getRelativeShownLocationX(), this.getRelativeShownLocationY()));
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
            });
            ;
            pasteMenuItem.setEnabled(networkEditorPanel.hasPasteContents() && networkEditorPanel
                                              .getEditorPanel()
                                              .getVisualNetwork()
                                              .getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION);
        }
        return pasteMenuItem;
    }
    
    /**
     * This method initializes networkPropertiesMenuItem.
     *
     * @return a new 'Network additionalProperties' menu item.
     */
    private JMenuItem getNetworkPropertiesMenuItem() {
        if (networkPropertiesMenuItem == null) {
            networkPropertiesMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_NETWORKPROPERTIES_MENUITEM,
                                                              ActionCommands.NETWORK_PROPERTIES.getCommandName());
            networkPropertiesMenuItem.addActionListener(listener);
        }
        return networkPropertiesMenuItem;
    }
    
    /**
     * Returns the component that corresponds to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    @Override protected JComponent getJComponentActionCommand(String actionCommand) {
        JComponent component = null;
        if (actionCommand.equals(ActionCommands.CLIPBOARD_PASTE.getCommandName())) {
            component = pasteMenuItem;
        } else if (actionCommand.equals(ActionCommands.NETWORK_PROPERTIES.getCommandName())) {
            component = networkPropertiesMenuItem;
        }
        return component;
    }
}
