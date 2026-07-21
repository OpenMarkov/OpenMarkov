/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.ConditionalGaussianPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.dialog.node.PotentialEditDialog;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.util.GUIUtils;

import javax.swing.*;
import java.awt.*;


/**
 * Panel for editing a {@link ConditionalGaussianPotential}, providing buttons to
 * edit the mean and variance sub-potentials in separate dialogs.
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = ConditionalGaussianPotential.class)
public class ConditionalGaussianPotentialPanel
        extends PotentialPanel implements PNEditListener {

    private final ProbNet probNet;
    private Node meanDummyNode = null;
    private Node varianceDummyNode = null;
    private final Node node;
    private final Potential oldPotential;
    private final ConditionalGaussianPotential newPotential;
    
    public ConditionalGaussianPotentialPanel(Node node) {
        super();
        initComponents();
        this.probNet = node.getProbNet();
        this.node = node;
        this.oldPotential = node.getPotentials().get(0);
        this.newPotential = (ConditionalGaussianPotential) oldPotential.copy();
        setData(node);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JButton editMeanButton = new JButton("Edit mean potential");
        editMeanButton.addActionListener(e -> {
            editMeanPotential();
        });
        JButton editVarianceButton = new JButton("Edit variance potential");
        editVarianceButton.addActionListener(e -> {
            editVariancePotential();
        });
        buttonPanel.add(editMeanButton);
        buttonPanel.add(editVarianceButton);
        add(buttonPanel, BorderLayout.PAGE_START);
    }
    
    private void editMeanPotential() {
        PotentialEditDialog potentialEditDialog = new PotentialEditDialog(GUIUtils.getOwner(this), meanDummyNode,
                                                                          isReadOnly());
        if (potentialEditDialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            // TODO: Do nothing?
            // Answer: Yes, and apparently, it still does the operation. Just try the following scenarios in a
            // Conditional Gaussian and be amazed:
            // 1 - Press the "Edit mean" and then in the table change a value and press Ok. Close the remaining
            // PotentialEditDialogs until going back to the probnet and then open the potential to see the changes
            // taking effect as you specified.
            // 2 - Press the "Edit mean" and then in the table change a value and press Cancel. Close the remaining
            // PotentialEditDialogs until going back to the probnet and then open the potential to see the changes
            // taking effect, which you specified it should NOT. This will be confusing as if from the initial
            // PotentialEditDialog you were to open the mean again, you would see that the table would be as you
            // expected it to be, but by reopening the potential, you will see it is not.
        } else {
        
        }
    }
    
    private void editVariancePotential() {
        PotentialEditDialog potentialEditDialog = new PotentialEditDialog(GUIUtils.getOwner(this), varianceDummyNode,
                                                                          isReadOnly());
        if (potentialEditDialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            // TODO: Do nothing?
        } else {
        
        }
    }
    
    @Override public void setData(Node node) {
        ProbNet meanDummyNet = new ProbNet(probNet.getNetworkType());
        meanDummyNode = meanDummyNet.addPotential(newPotential.getMean());
        meanDummyNet.getPNESupport().addListener(this);
        
        ProbNet varianceDummyNet = new ProbNet(probNet.getNetworkType());
        varianceDummyNode = varianceDummyNet.addPotential(newPotential.getVariance());
        varianceDummyNet.getPNESupport().addListener(this);
    }
    
    @Override public void close() {
        meanDummyNode.getProbNet().getPNESupport().removeListener(this);
        varianceDummyNode.getProbNet().getPNESupport().removeListener(this);
    }
    
    @Override
    public boolean saveChanges() throws BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong, DoEditException {
        boolean result = super.saveChanges();
        newPotential.setComment(oldPotential.getComment());
        PotentialChangeEdit edit = new PotentialChangeEdit(node, oldPotential, newPotential);
        edit.executeEdit();
        return result;
    }
    
    private void update() throws NonProjectablePotentialException {
        TablePotential projectedPotential = newPotential.tableProject(new EvidenceCase(), null);
        // TODO update table with projected potential
    }
    
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        // Update new potential and potential panel
        if (edit instanceof PotentialChangeEdit pcEdit) {
            newPotential.setMean(meanDummyNode.getPotentials().get(0));
            newPotential.setVariance(varianceDummyNode.getPotentials().get(0));
            //update();
        }
    }
}
