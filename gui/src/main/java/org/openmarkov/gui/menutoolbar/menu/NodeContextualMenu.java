/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.validator.AbsorbParentsValidator;
import org.openmarkov.gui.validator.AbsorbNodeValidator;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;
import org.openmarkov.gui.window.edition.mode.SelectionEditionMode;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NodesAlignment;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * This class implements a contextual menu that is displayed when the user
 * clicks on a node.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.2 asaez - Add options for expanding and contracting nodes, setting
 * and deleting findings and policies.
 */
public class NodeContextualMenu extends ContextualMenu {
    /**
     * Static field for serializable class.
     */
    @Serial
    private static final long serialVersionUID = 8556550568033250304L;
    /**
     * Object that represents the item 'Cut'.
     */
    private JMenuItem cutMenuItem = null;
    /**
     * Object that represents the item 'Copy'.
     */
    private JMenuItem copyMenuItem = null;
    /**
     * Object that represents the item 'Remove'.
     */
    private JMenuItem removeMenuItem = null;
    /**
     * Object that represents the item 'AbsorbNode'.
     */
    private JMenuItem absorbNodeMenuItem = null;
    /**
     * Object that represents the item 'AbsorbParents'.
     */
    private JMenuItem absorbParentsMenuItem = null;
    /**
     * Object that represents the item 'Properties'.
     */
    private JMenuItem propertiesMenuItem = null;
    private JMenuItem relationMenuItem;
    /**
     * Object that represents the item 'ImposePolicy'.
     */
    private JMenuItem imposePolicyMenuItem = null;
    
    /**
     * Object that represents the item 'AddTimeToEvent'.
     */
    private JMenuItem editTimeToEventMenuItem = null;
    
    /**
     * Object that represents the item 'RemovePolicy'.
     */
    private JMenuItem removePolicyMenuItem = null;
    /**
     * Object that represents the item 'ShowExpectedUtility'.
     */
    private JMenuItem showExpectedUtilityMenuItem = null;
    /**
     * Object that represents the item 'ShowOptimalPolicy'.
     */
    private JMenuItem showOptimalPolicyMenuItem = null;
    /**
     * Object that represents the item 'Expand'.
     */
    private JMenuItem expandMenuItem = null;
    /**
     * Object that represents the item 'Contract'.
     */
    private JMenuItem contractMenuItem = null;
    /**
     * Object that represents the item 'addFinding'.
     */
    private JMenuItem addFindingMenuItem = null;
    /**
     * Object that represents the item 'removeFinding'.
     */
    private JMenuItem removeFindingMenuItem = null;
    
    private JMenuItem logMenuItem;
    
    /**
     * Object that represents the item 'Temporal evolution'.
     */
    private JMenuItem temporalEvolutionMenuItem;
    
    /**
     * Object that represents the item 'Create node in next slice'.
     */
    private JMenuItem nextSliceNodeMenuItem;
    
    
    private final NetworkEditorPanel networkEditorPanel;
    
    /**
     * This constructor creates a new instance.
     *
     * @param newListener        object that listens to the menu events.
     * @param networkEditorPanel the panel
     * @param selectedNode       the selected node
     */
    public NodeContextualMenu(ActionListener newListener, VisualNode selectedNode, NetworkEditorPanel networkEditorPanel) {
        super(newListener);
        this.networkEditorPanel = networkEditorPanel;
        NetworkEditorPanel.WorkingMode workingMode = networkEditorPanel.getNetworkEditorPanel().getWorkingMode();
        this.selectedNode = selectedNode;
        NodeType nodeType = selectedNode.getNode().getNodeType();
        // Test if the node can be absorbed. Validate method returns true in that case
        Node node = selectedNode.getNode();
        setOptionEnabled(ActionCommands.ABSORB_NODE.getCommandName(), AbsorbNodeValidator.validate(node));
        
        // Test if parents of the node can be absorbed. Validate method returns true in that case
        setOptionEnabled(ActionCommands.ABSORB_PARENTS.getCommandName(), AbsorbParentsValidator.validate(node));
        
        boolean isEventNode = selectedNode.getNode().getNodeType().equals(NodeType.EVENT);
        
        add(getCutMenuItem());
        add(getCopyMenuItem());
        add(getRemoveMenuItem());
        if (networkEditorPanel.getVisualNetwork()
                              .getSelectedNodes()
                              .size() > 1 && networkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION) {
            add(getAlignmentMenuItem());
        }
        
        addSeparator();
        if (networkEditorPanel.getEditionMode() instanceof SelectionEditionMode selectionEditionMode) {
            add(getCreateLinkParentMenuItem(selectionEditionMode));
            add(getCreateLinkChildMenuItem(selectionEditionMode));
            addSeparator();
        }
        if (workingMode == NetworkEditorPanel.WorkingMode.EDITION) {
            add(getAbsorbNodeMenuItem());
            add(getAbsorbParentsMenuItem());
            addSeparator();
        }
        
        //visualNode.getNode().getVariable().isTemporal()
        
        if (node.getVariable().isTemporal()) {
            add(getTemporalEvolutionMenuItem());
            if (workingMode == NetworkEditorPanel.WorkingMode.EDITION) {
                add(getNextSliceNodeMenuItem());
            }
            addSeparator();
        }
        add(getPropertiesMenuItem());
        if (isEventNode) {
            add(getEditTimeToEventMenuItem());
        }
        
        
        if (nodeType != NodeType.DECISION) {
            add(getEditPotentialMenuItem());
        }
        addSeparator();
        if (networkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) {
            add(getExpandMenuItem());
            add(getContractMenuItem());
            addSeparator();
        }
        
        if (nodeType==NodeType.DECISION) {
            if (workingMode == NetworkEditorPanel.WorkingMode.EDITION) {
                add(getImposePolicyMenuItem(this.selectedNode.getNode()));
                add(getRemovePolicyMenuItem());
                addSeparator();
            } else if (workingMode == NetworkEditorPanel.WorkingMode.INFERENCE
                    && networkEditorPanel.getEvidenceManager()
                                         .getEvidenceCasesCompilationState(networkEditorPanel.getEvidenceManager()
                                                                                             .getCurrentCase())) {
                add(getShowExpectedUtilityMenuItem());
                add(getShowOptimalPolicyMenuItem());
                addSeparator();
            }
        }
        
        add(getAddFindingMenuItem());
        add(getRemoveFindingMenuItem());
        
    }
    
    private JMenuItem getAlignmentMenuItem() {
        if (this.alignmentMenuItem == null) {
            this.alignmentMenuItem = new JMenuItemBuilder("Alignment")
                    .enabled(this.networkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION)
                    .withItems(
                            new JMenuItemBuilder("Center vertically")
                                    .onClick(() -> NodesAlignment.verticalAlign(this.networkEditorPanel))
                                    .enabled(NodesAlignment.canVerticalAlign(this.networkEditorPanel))
                                    .build(),
                            new JMenuItemBuilder("Center horizontally")
                                    .onClick(() -> NodesAlignment.horizontalAlign(this.networkEditorPanel))
                                    .enabled(NodesAlignment.canHorizontalAlign(this.networkEditorPanel))
                                    .build(),
                            new JMenuItemBuilder("Sparse vertically")
                                    .onClick(() -> NodesAlignment.verticalSparse(this.networkEditorPanel))
                                    .enabled(NodesAlignment.canVerticalSparse(this.networkEditorPanel))
                                    .build(),
                            new JMenuItemBuilder("Sparse horizontally")
                                    .onClick(() -> NodesAlignment.horizontalSparse(this.networkEditorPanel))
                                    .enabled(NodesAlignment.canHorizontalSparse(this.networkEditorPanel))
                                    .build()
                    )
                    .build();
        }
        return this.alignmentMenuItem;
    }
    
    private JMenuItem getCreateLinkParentMenuItem(SelectionEditionMode selectionEditionMode) {
        var isWorkingMode = networkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION;
        return new JMenuItemBuilder("Create link (parent)")
                .withIcon(IconBind.LINK_PARENT_ENABLED.icon())
                .withName("NodeContextualMenuCreateLink")
                .withActionCommand(ActionCommands.LINK_CREATION)
                .enabled(isWorkingMode)
                .onClick(e -> {
                    selectionEditionMode.startLinkCreation(
                            new Point2D.Double(this.getRelativeShownLocationX(), this.getRelativeShownLocationY()), VisualNetwork.LinkCreationSourceDirection.PARENT);
                })
                .build();
    }
    
    private JMenuItem getCreateLinkChildMenuItem(SelectionEditionMode selectionEditionMode) {
        var isWorkingMode = networkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION;
        return new JMenuItemBuilder("Create link (child)")
                .withIcon(IconBind.LINK_CHILD_ENABLED.icon())
                .withName("NodeContextualMenuCreateLink")
                .withActionCommand(ActionCommands.LINK_CREATION)
                .enabled(isWorkingMode)
                .onClick(e -> {
                    selectionEditionMode.startLinkCreation(
                            new Point2D.Double(this.getRelativeShownLocationX(), this.getRelativeShownLocationY()), VisualNetwork.LinkCreationSourceDirection.CHILD);
                })
                .build();
    }
    
    /*
     * private JMenuItem getLogMenuItem() { if (logMenuItem == null) {
     * logMenuItem = new LocalizedMenuItem (MenuItemNames.EDIT_LOG_MENUITEM,
     * ActionCommands.LOG); logMenuItem.addActionListener(listener); } return
     * logMenuItem; }
     */
    
    /**
     * This method initializes temporalEvolutionMenuItem.
     *
     * @return a new 'Temporal Evolution' menu item.
     */
    private JMenuItem getTemporalEvolutionMenuItem() {
        if (temporalEvolutionMenuItem == null) {
            temporalEvolutionMenuItem = new LocalizedMenuItem(MenuItemNames.TEMPORAL_EVOLUTION_MENUITEM,
                                                              ActionCommands.TEMPORAL_EVOLUTION_ACTION.getCommandName());
            temporalEvolutionMenuItem.addActionListener(listener);
        }
        return temporalEvolutionMenuItem;
    }
    
    /**
     * This method initializes temporalEvolutionMenuItem.
     *
     * @return a new 'Temporal Evolution' menu item.
     */
    private JMenuItem getNextSliceNodeMenuItem() {
        if (nextSliceNodeMenuItem == null) {
            nextSliceNodeMenuItem = new LocalizedMenuItem(MenuItemNames.NEXT_SLICE_NODE,
                                                          ActionCommands.NEXT_SLICE_NODE.getCommandName());
            nextSliceNodeMenuItem.addActionListener(listener);
        }
        return nextSliceNodeMenuItem;
    }
    
    /**
     * This method initializes cutMenuItem.
     *
     * @return a new 'Cut' menu item.
     */
    private JMenuItem getCutMenuItem() {
        if (cutMenuItem == null) {
            cutMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_CUT_MENUITEM, ActionCommands.CLIPBOARD_CUT.getCommandName());
            cutMenuItem.setIcon(IconBind.CUT_ENABLED.icon());
            cutMenuItem.setEnabled(networkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION);
            cutMenuItem.addActionListener(listener);
        }
        return cutMenuItem;
    }
    
    /**
     * This method initialises copyMenuItem.
     *
     * @return a new 'Copy' menu item.
     */
    private JMenuItem getCopyMenuItem() {
        if (copyMenuItem == null) {
            copyMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_COPY_MENUITEM, ActionCommands.CLIPBOARD_COPY.getCommandName());
            copyMenuItem.setIcon(IconBind.COPY_ENABLED.icon());
            copyMenuItem.addActionListener(listener);
        }
        return copyMenuItem;
    }
    
    /**
     * This method initialises removeMenuItem.
     *
     * @return a new 'Remove' menu item.
     */
    private JMenuItem getRemoveMenuItem() {
        if (removeMenuItem == null) {
            removeMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_REMOVE_MENUITEM, ActionCommands.OBJECT_REMOVAL.getCommandName());
            removeMenuItem.setIcon(IconBind.REMOVE_ENABLED.icon());
            removeMenuItem.setEnabled(networkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION);
            removeMenuItem.addActionListener(listener);
        }
        return removeMenuItem;
    }
    
    /**
     * This method initialises absorbNodeMenuItem.
     *
     * @return a new 'absorbNode' menu item.
     */
    private JMenuItem getAbsorbNodeMenuItem() {
        if (absorbNodeMenuItem == null) {
            absorbNodeMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_ABSORBNODE_MENUITEM, ActionCommands.ABSORB_NODE.getCommandName());
            absorbNodeMenuItem.addActionListener(listener);
        }
        return absorbNodeMenuItem;
    }
    
    /**
     * This method initialises propertiesMenuItem.
     *
     * @return a new 'Properties' menu item.
     */
    private JMenuItem getPropertiesMenuItem() {
        if (propertiesMenuItem == null) {
            propertiesMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_NODEPROPERTIES_MENUITEM,
                                                       ActionCommands.NODE_PROPERTIES.getCommandName());
            propertiesMenuItem.addActionListener(listener);
            propertiesMenuItem.setIcon(IconBind.SETTINGS_ENABLED.icon());
        }
        return propertiesMenuItem;
    }
    
    /**
     * This method initialises tableMenuItem.
     *
     * @return a new 'Table' menu item.
     */
    private JMenuItem getEditPotentialMenuItem() {
        if (relationMenuItem == null) {
            relationMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_NODERELATION_MENUITEM,
                                                     ActionCommands.EDIT_POTENTIAL.getCommandName());
            relationMenuItem.setIcon(IconBind.EDIT_PROBABILITIES_ENABLED.icon());
            relationMenuItem.addActionListener(listener);
        }
        return relationMenuItem;
    }
    
    /**
     * This methods return the Time To Event menu item
     *
     * @return a new addTimeToEvent menu item.
     */
    private JMenuItem getEditTimeToEventMenuItem() {
        if (editTimeToEventMenuItem == null) {
            editTimeToEventMenuItem = new LocalizedMenuItem(MenuItemNames.EVENT_EDIT_TIME_TO_EVENT_MENUITEM,
                                                            ActionCommands.EVENT_EDIT_TIME_TO_EVENT.getCommandName());
            editTimeToEventMenuItem.addActionListener(listener);
        }
        return editTimeToEventMenuItem;
    }
    
    /**
     * This method initialises imposePolicyMenuItem.
     *
     * @return a new 'ImposePolicy' menu item.
     */
    private JMenuItem getImposePolicyMenuItem(Node node) {
        if (imposePolicyMenuItem == null) {
            if (!node.getPotentials().isEmpty()) {
                imposePolicyMenuItem = new LocalizedMenuItem(MenuItemNames.DECISION_EDIT_POLICY_MENUITEM,
                                                             ActionCommands.DECISION_EDIT_POLICY.getCommandName());
            } else {
                imposePolicyMenuItem = new LocalizedMenuItem(MenuItemNames.DECISION_IMPOSE_POLICY_MENUITEM,
                                                             ActionCommands.DECISION_IMPOSE_POLICY.getCommandName());
            }
            imposePolicyMenuItem.setIcon(IconBind.EDIT_PROBABILITIES_ENABLED.icon());
            imposePolicyMenuItem.addActionListener(listener);
        }
        return imposePolicyMenuItem;
    }
    
    /**
     * This method initialises removePolicyMenuItem.
     *
     * @return a new 'RemovePolicy' menu item.
     */
    private JMenuItem getRemovePolicyMenuItem() {
        if (removePolicyMenuItem == null) {
            removePolicyMenuItem = new LocalizedMenuItem(MenuItemNames.DECISION_REMOVE_POLICY_MENUITEM,
                                                         ActionCommands.DECISION_REMOVE_POLICY.getCommandName());
            removePolicyMenuItem.addActionListener(listener);
        }
        return removePolicyMenuItem;
    }
    
    /**
     * This method initialises showExpectedUtilityMenuItem.
     *
     * @return a new 'ShowExpectedUtility' menu item.
     */
    private JMenuItem getShowExpectedUtilityMenuItem() {
        if (showExpectedUtilityMenuItem == null) {
            showExpectedUtilityMenuItem = new LocalizedMenuItem(MenuItemNames.SHOW_EXPECTED_UTILITY_MENUITEM,
                                                                ActionCommands.DECISION_SHOW_EXPECTED_UTILITY.getCommandName());
            showExpectedUtilityMenuItem.addActionListener(listener);
        }
        return showExpectedUtilityMenuItem;
    }
    
    /**
     * This method initialises showOptimalPolicyMenuItem.
     *
     * @return a new 'ShowOptimalPolicy' menu item.
     */
    private JMenuItem getShowOptimalPolicyMenuItem() {
        if (showOptimalPolicyMenuItem == null) {
            showOptimalPolicyMenuItem = new LocalizedMenuItem(MenuItemNames.SHOW_OPTIMAL_POLICY_MENUITEM,
                                                              ActionCommands.DECISION_SHOW_OPTIMAL_POLICY.getCommandName());
            showOptimalPolicyMenuItem.addActionListener(listener);
        }
        return showOptimalPolicyMenuItem;
    }
    
    /**
     * This method initialises expandMenuItem.
     *
     * @return a new 'Expand' menu item.
     */
    private JMenuItem getExpandMenuItem() {
        if (expandMenuItem == null) {
            expandMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_EXPAND_NODE_MENUITEM,
                                                   ActionCommands.NODE_EXPANSION.getCommandName());
            expandMenuItem.addActionListener(listener);
        }
        return expandMenuItem;
    }
    
    /**
     * This method initialises contractMenuItem.
     *
     * @return a new 'Contract' menu item.
     */
    private JMenuItem getContractMenuItem() {
        if (contractMenuItem == null) {
            contractMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_CONTRACT_NODE_MENUITEM,
                                                     ActionCommands.NODE_CONTRACTION.getCommandName());
            contractMenuItem.addActionListener(listener);
        }
        return contractMenuItem;
    }
    
    /**
     * This method initialises addFindingMenuItem.
     *
     * @return a new 'addFinding' menu item.
     */
    private JMenuItem getAddFindingMenuItem() {
        if (addFindingMenuItem == null) {
            addFindingMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_ADD_FINDING_MENUITEM,
                                                       ActionCommands.NODE_ADD_FINDING.getCommandName());
            addFindingMenuItem.addActionListener(listener);
            addFindingMenuItem.setIcon(IconBind.CREATE_NEW_EVIDENCE_CASE_ENABLED.icon());
        }
        return addFindingMenuItem;
    }
    
    /**
     * This method initialises removeFindingMenuItem.
     *
     * @return a new 'removeFinding' menu item.
     */
    private JMenuItem getRemoveFindingMenuItem() {
        if (removeFindingMenuItem == null) {
            removeFindingMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_REMOVE_FINDING_MENUITEM,
                                                          ActionCommands.NODE_REMOVE_FINDING.getCommandName());
            removeFindingMenuItem.addActionListener(listener);
            removeFindingMenuItem.setIcon(IconBind.CLEAR_OUT_ALL_EVIDENCE_CASES_ENABLED.icon());
        }
        return removeFindingMenuItem;
    }
    
    // TODO OOPN start
    
    /**
     * This method initialises AbsorbParentsMenuItem.
     *
     * @return a new 'AbsorbParents' menu item.
     */
    private JMenuItem getAbsorbParentsMenuItem() {
        if (absorbParentsMenuItem == null) {
            absorbParentsMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_ABSORBPARENTS_MENUITEM, ActionCommands.ABSORB_PARENTS.getCommandName());
            absorbParentsMenuItem.addActionListener(listener);
        }
        
        return absorbParentsMenuItem;
    }
    
    // TODO OOPN end
    
    /**
     * Returns the component that corresponds to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    @Override protected JComponent getJComponentActionCommand(String actionCommand) {
        JComponent component = switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.CLIPBOARD_CUT -> cutMenuItem;
            case ActionCommands.CLIPBOARD_COPY -> copyMenuItem;
            case ActionCommands.OBJECT_REMOVAL -> removeMenuItem;
            case ActionCommands.ABSORB_NODE -> absorbNodeMenuItem;
            case ActionCommands.ABSORB_PARENTS -> absorbParentsMenuItem;
            case ActionCommands.NODE_PROPERTIES -> propertiesMenuItem;
            case ActionCommands.EDIT_POTENTIAL -> relationMenuItem;
            case ActionCommands.DECISION_EDIT_POLICY ->
                    selectedNode.getNode().getNodeType()==NodeType.DECISION ? !selectedNode.getNode().getPotentials().isEmpty() ? imposePolicyMenuItem : null : null;
            case ActionCommands.DECISION_IMPOSE_POLICY ->
                    selectedNode.getNode().getNodeType()==NodeType.DECISION ? selectedNode.getNode().getPotentials().isEmpty() ? imposePolicyMenuItem : null : null;
            case ActionCommands.DECISION_REMOVE_POLICY -> removePolicyMenuItem;
            case ActionCommands.EVENT_EDIT_TIME_TO_EVENT -> editTimeToEventMenuItem;
            case ActionCommands.DECISION_SHOW_EXPECTED_UTILITY -> showExpectedUtilityMenuItem;
            case ActionCommands.DECISION_SHOW_OPTIMAL_POLICY -> showOptimalPolicyMenuItem;
            case ActionCommands.NODE_EXPANSION -> expandMenuItem;
            case ActionCommands.NODE_CONTRACTION -> contractMenuItem;
            case ActionCommands.NODE_ADD_FINDING -> addFindingMenuItem;
            case ActionCommands.NODE_REMOVE_FINDING -> removeFindingMenuItem;
            case ActionCommands.LOG -> logMenuItem;
            case ActionCommands.TEMPORAL_EVOLUTION_ACTION -> temporalEvolutionMenuItem;
            case ActionCommands.NEXT_SLICE_NODE -> nextSliceNodeMenuItem;
            case null, default -> null;
        };
        return component;
    }
    
    private final VisualNode selectedNode;
    private JMenuItem alignmentMenuItem;
}
