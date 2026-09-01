/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Dialog box to edit all type of potentials ( TablePotential and TreeADDs ). If
 * the potential is a utility role or uniform type, then no Values panel is
 * displayed. If potential is TreeADDpotential, then graphic edition panel is
 * showed.
 *
 * @author mpalacios
 * @author jmendoza
 * @author ibermejo
 * @version 1.3 cmyago 19/06/2016 - adapted the class to the new utility treatment; minor changes
 */
public class PotentialEditDialog extends OkCancelDialog {
    
    protected PotentialEditPanel potentialEditPanel;
    protected final Node node;
    protected final boolean readOnly;
    
    public PotentialEditDialog(Window owner, Node node, boolean readOnly) throws ThereIsNoPotentialsInNodeException {
        super(owner);
        this.node = node;
        this.readOnly = readOnly;
        initialize();
        this.getComponentsPanel().setLayout(new BorderLayout());
        this.addComponentListener(new ComponentListener() {
            @Override public void componentResized(ComponentEvent e) {
                PotentialEditDialog.this.potentialEditPanel.setSize(new Dimension(
                        PotentialEditDialog.this.getSize().width - 18,
                        PotentialEditDialog.this.getSize().height - 100)
                );
            }
            
            @Override public void componentMoved(ComponentEvent e) {
            
            }
            
            @Override public void componentShown(ComponentEvent e) {
            
            }
            
            @Override public void componentHidden(ComponentEvent e) {
            
            }
        });
    }
    
    protected void initialize() throws ThereIsNoPotentialsInNodeException {
        this.potentialEditPanel = this.generatePotentialEditPanel(node, readOnly);
        this.getComponentsPanel().setLayout(new BorderLayout());
        this.getComponentsPanel().add(this.potentialEditPanel, BorderLayout.CENTER);
        
        this.setResizable(true);
        // Set default title
        this.setTitle(getBaseTitle() +": "+this.node.getName());
        this.setMinimumSize(new Dimension(600, 100));
        this.setSize(1000, 500);
        this.setLocationRelativeTo(this.getOwner());
    }
    
    protected String getBaseTitle() {
        return StringDatabase.getUniqueInstance().getString("NodePropertiesDialog.EditPotentialTab.EditPotentialTitle");
    }
    
    public PotentialEditPanel getPotentialEditPanel() {
        return this.potentialEditPanel;
    }
    
    /**
     * @return An integer indicating the button clicked by the user when closing
     * this dialog
     */
    public ChosenOption requestValues() throws ThereIsNoPotentialsInNodeException {
        // Shows the potentials' options table
        if (this.node.getNodeType() == NodeType.DECISION && this.node.getPolicyType() == PolicyType.OPTIMAL && this.readOnly) {
            this.potentialEditPanel.setEnabledDecisionOptions();
        }
        this.setVisible(true);
        return this.getSelectedOption();
    }
    
    PotentialEditPanel generatePotentialEditPanel(Node node, boolean readOnly) throws ThereIsNoPotentialsInNodeException {
        return new PotentialEditPanel(node, readOnly, true);
    }
    
    
    @Override
    protected boolean doOkClickBeforeHide() throws BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong, DoEditException, ThereIsNoPotentialsInNodeException {
        this.potentialEditPanel.commitChanges();
        return true;
    }
    
    @Override protected void doCancelClickBeforeHide() {
        this.potentialEditPanel.uncommitChanges();
    }
    
}
