/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.core.RemovePolicyEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.Stream;

/**
 * Dialog box to set the additionalProperties of a node. This is the basic of
 * all the node additionalProperties dialog. Subclasses adds the fields that
 * corresponds to each type of node. If the node is a utility node, then no
 * Values panel is displayed
 *
 * @author jmendoza
 * @version 1.4 jrico. Merged with CommonNodePropertiesDialog. Minor clean-up.
 */
public class NodePropertiesDialog extends OkCancelDialog {
    
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 5777866419377968128L;
    
    /**
     * This method initializes this instance.
     *
     * @param owner                  window that owns this dialog.
     * @param nodeNetworkEditorPanel
     * @param newNode                if true, it indicates that a new network is being created; if
     *                               false, an existing network is being modified.
     * @param readOnly               if true, values inside the dialog will not be editable
     */
    public NodePropertiesDialog(Window owner, NetworkEditorPanel nodeNetworkEditorPanel, VisualNode visualNode, boolean newNode, boolean readOnly) {
        super(owner);
        this.nodeNetworkEditorPanel = nodeNetworkEditorPanel;
        this.visualNode = visualNode;
        this.node = visualNode.getNode();
        this.readOnly = readOnly;
        this.newNode = newNode;
        this.setName("NodePropertiesDialog");
        this.setName("NodePropertiesDialog");
        this.node.getProbNet().getPNESupport().openNewSubEditHistory();
        this.reinitialize();
        this.pack();
        this.setLocationRelativeTo(owner);
        this.setMinimumSize(this.getSize());
        this.setResizable(true);
    }
    
    /**
     * Object where all information will be saved.
     */
    protected Node node;
    /**
     * Panel to tab the different options.
     */
    private JTabbedPane tabbedPane = null;
    /**
     * Panel that contains the panel where node fields are. It is used to place
     * the fields at the top of the panel.
     */
    private NodeDefinitionPanel nodeDefinitionPanel = null;
    /**
     * Panel that contains the panel where discretize values fields are. It is
     * used to place the fields at the top of the panel.
     */
    private NodeDomainValuesTablePanel nodeDomainValuesTablePanel = null;
    /**
     * Panel that contains the panel where parents fields are. It is used to
     * place the fields at the top of the panel.
     */
    private NodeParentsPanel nodeParentsPanel = null;
    /**
     * Panel that contains the panel where other property table fields are. It
     * is used to place the fields at the top of the panel.
     */
    private NodeOtherPropsTablePanel nodeOtherPropsTablePanel = null;
    
    private final boolean readOnly;
    private final boolean newNode;
    private JPanel panelForPotentialEdit;
    
    /**
     * This method fills the content of the fields from a Node object. In
     * this method, when Elvira will be discontinued, the code for discriminate
     * discrete and discretized variables must be eliminated
     *
     * @param node object from where load the information.
     */
    private void setFieldsFromProperties(Node node) {
        this.node = node;
        this.setTitle(this.stringDatabase.getString("NodePropertiesDialog.Title") + ": " + node.getName());
        this.nodeDefinitionPanel.setNodeProperties(node);
        if (node.getNodeType() == NodeType.CHANCE || node.getNodeType() == NodeType.DECISION || node.getNodeType() == NodeType.EVENT) {
            this.getNodeDomainValuesTablePanel().setFieldsFromProperties(node);
            if (node.getVariable().getVariableType() == VariableType.FINITE_STATES || node.getVariable()
                                                                                          .getVariableType() == VariableType.DISCRETIZED) {
                int indexOfTab = this.tabbedPane.indexOfTab(this.stringDatabase.getString("NodePropertiesDialog.DiscretizeValuesTab.Title"));
                this.tabbedPane.setEnabledAt(indexOfTab, true);
            }
            /*
             * TODO when Continuous variable will be included, remember to
             * include also in the previous two else if, the appropiate method
             * else if
             * (additionalProperties.getVariableType()==VariableType.CONTINUOUS)
             * { nodeContinuousValuesTablePanel.setFieldsFromProperties(
             * additionalProperties); }
             */
        }
        this.nodeParentsPanel.setNodeProperties(node);
        this.nodeOtherPropsTablePanel.setNodeProperties(node);
        this.nodeDefinitionPanel.setFieldsFromProperties(node);
        this.nodeParentsPanel.setFieldsFromProperties(node);
        this.nodeOtherPropsTablePanel.setFieldsFromProperties(node);
    }
    
    /**
     * This method configures the dialog box.
     */
    protected void reinitialize() {
        this.tabbedPane = null;
        this.nodeDefinitionPanel = null;
        this.nodeDomainValuesTablePanel = null;
        this.nodeParentsPanel = null;
        this.nodeOtherPropsTablePanel = null;
        this.panelForPotentialEdit = null;
        this.removeButtonFromButtonsPanel(this.editOrViewPotentialButton);
        this.removeButtonFromButtonsPanel(this.cancelEditPotentialButton);
        this.setTitle(this.stringDatabase.getString("NodePropertiesDialog.Title") + ": " + (
                this.node == null ? "" : this.node.getName()
        ));
        this.getComponentsPanel().setName("NodePropertiesDialogComponentPane");
        this.configureComponentsPanel();
        if (this.newNode) {
            this.getNodeDefinitionPanel().getJTextFieldNodeName().requestFocus();
        }
        if (this.readOnly) {
            for (var tab : this.getTabbedPane().getComponents()) {
                ComponentUtilities.findComponents(tab, Component.class, ignored -> true)
                                  .forEach(ComponentUtilities::removeInputsFor);
            }
        }
        this.setFieldsFromProperties(this.node);
        this.pack();
    }
    
    /**
     * Sets up the panel where all components, except the buttons of the buttons
     * panel, will be appear.
     */
    private void configureComponentsPanel() {
        this.getComponentsPanel().setLayout(new BorderLayout());
        this.getComponentsPanel().removeAll();
        this.getComponentsPanel().add(this.getTabbedPane(), BorderLayout.CENTER);
    }
    
    /**
     * This method initialises tabbedPane.
     *
     * @return a new tabbed pane.
     */
    private JTabbedPane getTabbedPane() {
        if (this.tabbedPane == null) {
            this.tabbedPane = new JTabbedPane();
            this.tabbedPane.setName("NodePropertiesDialogTabbedPane");
            this.tabbedPane.addTab(this.stringDatabase.getString("NodePropertiesDialog.DefinitionTab.Title"), null,
                                   this.getNodeDefinitionPanel(), null);
            this.tabbedPane.addTab(this.stringDatabase.getString("NodePropertiesDialog.DiscretizeValuesTab.Title"), null,
                                   this.getNodeDomainValuesTablePanel(), null);
            this.tryAddEditPotentialTab();
            this.tabbedPane.addTab(this.stringDatabase.getString("NodePropertiesDialog.ParentsTab.Title"), null,
                                   this.getNodeParentsPanel(), null);
            this.tabbedPane.addTab(this.stringDatabase.getString("NodePropertiesDialog.OtherPropsTab.Title"), null,
                                   this.getNodeOtherPropsTablePanel(), null);
        }
        return this.tabbedPane;
    }
    
    private void tryAddEditPotentialTab() {
        if (!(this.node.getNodeType() == NodeType.CHANCE || this.node.getNodeType() == NodeType.UTILITY)) {
            return;
        }
        this.panelForPotentialEdit = new JPanel();
        this.panelForPotentialEdit.setLayout(new BorderLayout());
        this.editOrViewPotentialButton = new JButton();
        this.editOrViewPotentialButton.setIcon(IconBind.EDIT_PROBABILITIES_ENABLED.icon());
        this.editOrViewPotentialButton.addActionListener(_ -> {
            new PotentialEditDialog(this, this.node, this.readOnly).setVisible(true);
            this.updatePotentialTabTitleAndButton();
        });
        this.cancelEditPotentialButton = new JButton();
        this.cancelEditPotentialButton.setIcon(IconBind.UNDO_ENABLED.icon());
        this.cancelEditPotentialButton.addActionListener(_ -> {
            this.denyPotentialEditChanges();
            this.handleChangeTab();
            this.repaint();
            this.cancelEditPotentialButton.requestFocus();
        });
        this.tabbedPane.addChangeListener(_ -> this.handleChangeTab());
        this.tabbedPane.addTab("", null, this.panelForPotentialEdit, null);
        this.updatePotentialTabTitleAndButton();
    }
    
    private void handleChangeTab() {
        this.editOrViewPotentialButton.setVisible(this.panelForPotentialEdit != this.tabbedPane.getSelectedComponent());
        if (this.panelForPotentialEdit == this.tabbedPane.getSelectedComponent()) {
            this.panelForPotentialEdit.removeAll();
            PotentialEditPanel potentialEditPanel;
            potentialEditPanel = new PotentialEditPanel(this.node, this.readOnly, true);
            this.panelForPotentialEdit.add(potentialEditPanel, BorderLayout.CENTER);
            this.cancelEditPotentialButton.setEnabled(false);
            Stream.concat(Stream.of(potentialEditPanel), ComponentUtilities.findComponents(potentialEditPanel, Component.class, ignored -> true))
                  .forEach(c -> addReadListenersOfPotentialChanges(c));
        } else {
            this.acceptPotentialEditChanges(false);
            this.panelForPotentialEdit.removeAll();
        }
        this.updatePotentialTabTitleAndButton();
    }
    
    private @Nullable PotentialEditPanel getPotentialEditPanel() {
        if (this.panelForPotentialEdit == null || this.panelForPotentialEdit.getComponents().length == 0) {
            return null;
        }
        if (!(this.panelForPotentialEdit.getComponent(0) instanceof PotentialEditPanel potentialEditPanel)) {
            return null;
        }
        return potentialEditPanel;
    }
    
    private void addReadListenersOfPotentialChanges(Component c) {
        if (c instanceof Container container) {
            container.addContainerListener(containerListenerOfPotentialChanges);
        }
        c.addFocusListener(focusListenerOfPotentialChanges);
        c.addMouseListener(mouseAdapterListenerOfPotentialChanges);
        c.addMouseMotionListener(mouseAdapterListenerOfPotentialChanges);
        c.addKeyListener(keyListenerOfPotentialChanges);
    }
    
    private void updatePotentialTabTitleAndButton() {
        if (this.panelForPotentialEdit == null) {
            return;
        }
        if (this.panelForPotentialEdit == this.tabbedPane.getSelectedComponent()) {
            this.removeButtonFromButtonsPanel(this.editOrViewPotentialButton);
            this.addButtonToButtonsPanel(this.cancelEditPotentialButton, 1);
        } else {
            this.removeButtonFromButtonsPanel(this.cancelEditPotentialButton);
            this.addButtonToButtonsPanel(this.editOrViewPotentialButton, 1);
        }
        String goal = stringDatabase.getString("NodePropertiesDialog.EditPotentialTab.Goal." +
                                                       (this.node.getNodeType() == NodeType.CHANCE ? "Probability": "Utility")
        );
        String tabTitle = (this.readOnly ? this.stringDatabase.getString("NodePropertiesDialog.EditPotentialTab.Action.View") :
                this.stringDatabase.getString("NodePropertiesDialog.EditPotentialTab.Action.Edit"))
                + " " + goal.toLowerCase();
        this.editOrViewPotentialButton.setText(tabTitle);
        this.cancelEditPotentialButton.setText(stringDatabase.getString("NodePropertiesDialog.EditPotentialTab.Action.Reset") + " " + goal);
        this.tabbedPane.setTitleAt(this.tabbedPane.indexOfComponent(this.panelForPotentialEdit), goal);
    }
    
    
    private void acceptPotentialEditChanges(boolean canIgnoreException) {
        if (this.panelForPotentialEdit == null || this.panelForPotentialEdit.getComponents().length == 0) {
            return;
        }
        PotentialEditPanel panel = (PotentialEditPanel) this.panelForPotentialEdit.getComponent(0);
        this.panelForPotentialEdit.remove(0);
        try {
            if (!panel.commitChanges()) {
                panel.uncommitChanges();
            }
        } catch (BinomialPotentialWrongValueException.ThetaValueIsWrong |
                 BinomialPotentialWrongValueException.NValuesIsWrong | DoEditException ex) {
            panel.uncommitChanges();
            if (canIgnoreException) {
                throw new UnrecoverableException(ex);
            }
        }
    }
    
    private void denyPotentialEditChanges() {
        if (this.panelForPotentialEdit.getComponents().length == 0) {
            return;
        }
        PotentialEditPanel panel = (PotentialEditPanel) this.panelForPotentialEdit.getComponent(0);
        this.panelForPotentialEdit.remove(0);
        panel.uncommitChanges();
    }
    
    /**
     * This method initialises nodeDefinitionPanel.
     *
     * @return a new node definition panel.
     */
    private NodeDefinitionPanel getNodeDefinitionPanel() {
        if (this.nodeDefinitionPanel == null) {
            this.nodeDefinitionPanel = new NodeDefinitionPanel(this.nodeNetworkEditorPanel, this.node, this);
            this.nodeDefinitionPanel.setName("nodeDefinitionPanel");
            this.nodeDefinitionPanel.setNewNode(this.newNode);
            this.nodeDefinitionPanel.setNodeProperties(this.node);
        }
        return this.nodeDefinitionPanel;
    }
    
    /**
     * This method initializes nodeDomainValuesTablePanel.
     *
     * @return a new node discrete values table panel
     */
    private NodeDomainValuesTablePanel getNodeDomainValuesTablePanel() {
        if (this.nodeDomainValuesTablePanel == null) {
            this.nodeDomainValuesTablePanel = new NodeDomainValuesTablePanel(this.node);
            this.nodeDomainValuesTablePanel.getJLabelPrecision().setHorizontalAlignment(SwingConstants.LEFT);
            this.nodeDomainValuesTablePanel.setName("nodeDiscretizeValuesTablePanel");
            this.nodeDomainValuesTablePanel.setNewNode(this.newNode);
        }
        return this.nodeDomainValuesTablePanel;
    }
    
    /**
     * This method initialises nodeParentsPanel.
     *
     * @return a new node parents panel
     */
    private JPanel getNodeParentsPanel() {
        if (this.nodeParentsPanel == null) {
            this.nodeParentsPanel = new NodeParentsPanel(this.node);
            this.nodeParentsPanel.setName("nodeParentsPanel");
            this.nodeParentsPanel.setNewNode(this.newNode);
            // nodeParentsPanel.setNodeProperties(node);
        }
        return this.nodeParentsPanel;
    }
    
    /**
     * This method initializes nodeOtherPropsTablePanel.
     *
     * @return a new node other additionalProperties table panel
     */
    private JPanel getNodeOtherPropsTablePanel() {
        if (this.nodeOtherPropsTablePanel == null) {
            this.nodeOtherPropsTablePanel = new NodeOtherPropsTablePanel();
            this.nodeOtherPropsTablePanel.setName("nodeOtherPropsTablePanel");
            this.nodeOtherPropsTablePanel.setNewNode(this.newNode);
            this.nodeOtherPropsTablePanel.setNodeProperties(this.node);
        }
        return this.nodeOtherPropsTablePanel;
    }
    
    /**
     * This method carries out the actions when the user press the Cancel button
     * before hide the dialog.
     */
    @Override protected void doCancelClickBeforeHide() {
        this.acceptPotentialEditChanges(true);
        this.node.getProbNet().getPNESupport().cancelLastSubEditHistory();
    }
    
    /**
     * This method shows the dialog and requests the user the node
     * additionalProperties.
     *
     * @return OK_BUTTON if the user has pressed the 'Ok' button or
     * CANCEL_BUTTON if the user has pressed the 'Cancel' button.
     */
    public ChosenOption requestProperties() {
        this.setFieldsFromProperties(this.node);
        this.setVisible(true);
        return this.getSelectedOption();
    }
    
    /**
     * This method carries out the actions when the user press the OK button
     * before hide the dialog.
     *
     * @return true if all the fields are correct.
     */
    @Override protected boolean doOkClickBeforeHide() throws ConstraintViolatedException {
        // If the is user is editing a cell, stop the edition to save the data
        this.nodeDomainValuesTablePanel.getDiscretizedStatesPanel().stopCellEditing();
        this.acceptPotentialEditChanges(false);
        this.nodeDefinitionPanel.checkNameConstraints();
        this.node.getProbNet().getPNESupport().closeSubEditHistory();
        return true;
    }
    
    /**
     * @return the readOnly
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }
    
    
    private final NetworkEditorPanel nodeNetworkEditorPanel;
    private final VisualNode visualNode;
    private JButton editOrViewPotentialButton;
    private JButton cancelEditPotentialButton;
    private FocusListener focusListenerOfPotentialChanges = new FocusListener() {
        @Override public void focusGained(FocusEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void focusLost(FocusEvent e) {
            onActionTriggeredInPotential();
        }
    };
    
    private MouseAdapter mouseAdapterListenerOfPotentialChanges = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void mousePressed(MouseEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void mouseReleased(MouseEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void mouseEntered(MouseEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void mouseExited(MouseEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void mouseDragged(MouseEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void mouseMoved(MouseEvent e) {
            onActionTriggeredInPotential();
        }
    };
    private KeyListener keyListenerOfPotentialChanges = new KeyListener() {
        @Override public void keyTyped(KeyEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void keyPressed(KeyEvent e) {
            onActionTriggeredInPotential();
        }
        
        @Override public void keyReleased(KeyEvent e) {
            onActionTriggeredInPotential();
        }
    };
    private ContainerListener containerListenerOfPotentialChanges = new ContainerListener() {
        @Override public void componentAdded(ContainerEvent e) {
            Stream.concat(Stream.of(e.getChild()), ComponentUtilities.findComponents(e.getChild(), Component.class, ignored -> true))
                  .forEach(c -> NodePropertiesDialog.this.addReadListenersOfPotentialChanges(c));
        }
        
        @Override public void componentRemoved(ContainerEvent e) {
        
        }
    };
    
    private void onActionTriggeredInPotential() {
        NodePropertiesDialog.this.cancelEditPotentialButton.setEnabled(getPotentialEditPanel()!=null && getPotentialEditPanel().potentialHasChanged() && NodePropertiesDialog.this.nodeNetworkEditorPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.EDITION);
    }
}