/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.exception.CannotInvertUndirectedLinks;
import org.openmarkov.gui.exception.LinkInversionRequiresChanceVariablesWithPotential;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.validator.LinkInversionWithPotentialsUpdateValidator;
import org.openmarkov.gui.validator.LinkRestrictionValidator;
import org.openmarkov.gui.validator.RevelationArcValidator;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * This class implements a contextual menu that is displayes when the user clicks on
 * a link.
 *
 * @author jmendoza
 * @author jlgozalo
 * @author iagoparis - summer 2018
 * @version 1.1 jlgozalo - Add change locale management setting the item names.
 */
class LinkContextualMenu extends ContextualMenu {
    
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 5509407441152307200L;
    
    /**
     * Object that represents the item 'Remove'.
     */
    private JMenuItem removeMenuItem = null;
    
    /**
     * Object that represents the item 'Edit restriction'.
     */
    private JMenuItem linkRestrictionEditMenuItem = null;
    
    /**
     * Object that represents the item 'Remove restriction'.
     */
    private JMenuItem linkRestrictionDisableMenuItem = null;
    
    /**
     * Object that represents the item 'Add revelation arc'.
     */
    private JMenuItem revelationArcMenuItem = null;
    
    /**
     * Object that represents the item 'Invert arc'.
     */
    private JMenuItem invertLinkAndUpdatePotentialsMenuItem = null;
    
    /**
     * Object that represents the item 'Properties'.
     */
    private JMenuItem propertiesMenuItem = null;
    
    private final Link<Node> link;
    
    /**
     * This constructor creates a new instance.
     *
     * @param newListener  object that listens to the menu events.
     * @param panel        the panel
     * @param selectedLink the selected link
     */
    public LinkContextualMenu(ActionListener newListener, VisualLink selectedLink, NetworkEditorPanel panel) {
        super(newListener);
        link = selectedLink.getLink();
        boolean linkRestrictionEnabled = LinkRestrictionValidator.validate(link);
        initialize();
        switch (panel.getNetworkEditorPanel().getWorkingMode()) {
            case EDITION -> {
                setOptionEnabled(ActionCommands.LINK_RESTRICTION_EDIT_PROPERTIES,
                                 linkRestrictionEnabled);
                setOptionEnabled(ActionCommands.LINK_RESTRICTION_DISABLE_PROPERTIES,
                                 (linkRestrictionEnabled && link.hasRestrictions()));
                
                // Test if revelation arc should be enabled. Validate method returns true if that's the case
                setOptionEnabled(ActionCommands.LINK_REVELATIONARC_PROPERTIES,
                                 RevelationArcValidator.validate(link));
                
                try {
                    LinkInversionWithPotentialsUpdateValidator.validate(link);
                    setOptionEnabled(ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS, true);
                    getInvertLinkAndUpdatePotentialsMenuItem().setToolTipText(null);
                } catch (LinkInversionRequiresChanceVariablesWithPotential | ConstraintViolatedException |
                         CannotInvertUndirectedLinks e) {
                    setOptionEnabled(ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS, false);
                    getInvertLinkAndUpdatePotentialsMenuItem().setToolTipText(e.toString());
                }
            }
            case INFERENCE -> {
                setOptionEnabled(ActionCommands.LINK_RESTRICTION_EDIT_PROPERTIES, false);
                setOptionEnabled(ActionCommands.LINK_RESTRICTION_DISABLE_PROPERTIES, false);
                setOptionEnabled(ActionCommands.LINK_REVELATIONARC_PROPERTIES, false);
                setOptionEnabled(ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS, false);
            }
        }
        
        
        // Test if arc reversal should be enabled. Validate method returns true if that's the case
        
    }
    
    /**
     * This method initialises this instance.
     */
    private void initialize() {
        
        add(getRemoveMenuItem());
        addSeparator();
        add(getLinkRestrictionEditMenuItem());
        add(getLinkRestrictionDisableMenuItem());
        addSeparator();
        add(getRevelationArcMenuItem());
        addSeparator();
        add(getInvertLinkAndUpdatePotentialsMenuItem());
        /*
         * This item must be added to the menu when is active the possibility of
         * editing the additionalProperties of a link in future versions.
         */
        // addSeparator()
        // getPropertiesMenuItem();
    }
    
    /**
     * This method initialises removeMenuItem.
     *
     * @return a new 'Remove' menu item.
     */
    private JMenuItem getRemoveMenuItem() {
        if (removeMenuItem == null) {
            removeMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_REMOVE_MENUITEM, ActionCommands.OBJECT_REMOVAL.getCommandName());
            removeMenuItem.addActionListener(listener);
            removeMenuItem.setIcon(IconBind.REMOVE_ENABLED.icon());
        }
        return removeMenuItem;
        
    }
    
    /**
     * This method initialises the disableLinkRestriction menu item.
     *
     * @return a new 'LinkRestrictionDisable' menu item.
     */
    private JMenuItem getLinkRestrictionDisableMenuItem() {
        
        if (linkRestrictionDisableMenuItem == null) {
            linkRestrictionDisableMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_LINKRESTRICTION_DISABLE_MENUITEM,
                                                                   ActionCommands.LINK_RESTRICTION_DISABLE_PROPERTIES.getCommandName());
            linkRestrictionDisableMenuItem.addActionListener(listener);
        }
        
        return linkRestrictionDisableMenuItem;
        
    }
    
    /**
     * This method initialises the editLinkRestriction menu item.
     *
     * @return a new 'LinkRestrictionEdit' menu item.
     */
    private JMenuItem getLinkRestrictionEditMenuItem() {
        if (linkRestrictionEditMenuItem == null) {
            linkRestrictionEditMenuItem = new LocalizedMenuItem(link.hasRestrictions()?MenuItemNames.EDIT_LINKRESTRICTION_EDIT_MENUITEM:  MenuItemNames.EDIT_LINKRESTRICTION_ENABLE_MENUITEM,
                                                                  ActionCommands.LINK_RESTRICTION_EDIT_PROPERTIES.getCommandName());
            linkRestrictionEditMenuItem.addActionListener(listener);
        }
        return linkRestrictionEditMenuItem;
        
    }
    
    /**
     * This method initialises the revelationArc menu item.
     *
     * @return a new 'revelationArc' menu item.
     */
    
    private JMenuItem getRevelationArcMenuItem() {
        
        if (revelationArcMenuItem == null) {
            revelationArcMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_LINKREVELATIONARC_MENUITEM,
                                                          ActionCommands.LINK_REVELATIONARC_PROPERTIES.getCommandName());
            revelationArcMenuItem.addActionListener(listener);
        }
        
        return revelationArcMenuItem;
        
    }
    
    /**
     * This method initialises invertLinkAndUpdatePotentialsMenuItem.
     *
     * @return a new 'Invert link and update potentials' menu item.
     */
    public JMenuItem getInvertLinkAndUpdatePotentialsMenuItem() {
        
        if (invertLinkAndUpdatePotentialsMenuItem == null) {
            invertLinkAndUpdatePotentialsMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_INVERT_LINK_AND_UPDATE_POTENTIALS_MENUITEM,
                                                                          ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS.getCommandName());
            invertLinkAndUpdatePotentialsMenuItem.addActionListener(listener);
        }
        return invertLinkAndUpdatePotentialsMenuItem;
    }
    
    /**
     * This method initialises propertiesMenuItem.
     *
     * @return a new 'Properties' menu item.
     */
    private JMenuItem getPropertiesMenuItem() {
        
        if (propertiesMenuItem == null) {
            propertiesMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_LINKPROPERTIES_MENUITEM,
                                                       ActionCommands.LINK_PROPERTIES.getCommandName());
            propertiesMenuItem.addActionListener(listener);
        }
        
        return propertiesMenuItem;
        
    }
    
    /**
     * Returns the component that corresponds to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    @Override protected JComponent getJComponentActionCommand(String actionCommand) {
        return switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.OBJECT_REMOVAL -> removeMenuItem;
            case ActionCommands.LINK_RESTRICTION_DISABLE_PROPERTIES -> linkRestrictionDisableMenuItem;
            case ActionCommands.LINK_RESTRICTION_EDIT_PROPERTIES -> linkRestrictionEditMenuItem;
            case ActionCommands.LINK_REVELATIONARC_PROPERTIES -> revelationArcMenuItem;
            case ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS -> invertLinkAndUpdatePotentialsMenuItem;
            case null, default -> null;
        };
        
    }
    
}
