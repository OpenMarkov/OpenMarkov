package org.openmarkov.gui.dialog.common;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.*;
import org.openmarkov.gui.dialog.node.PotentialEditDialog;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.DiscretizedCauchyPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for editing a {@link DiscretizedCauchyPotential}, providing buttons to
 * edit the median and scale sub-potentials in separate dialogs.
 */
@SuppressWarnings("serial")
@PotentialPanelPlugin(potentialClasses = DiscretizedCauchyPotential.class)
public class DiscretizedCauchyPotentialPanel extends PotentialPanel implements PNEditListener {
    
    private JButton editMedianButton;
    private JButton editScaleButton;
    private final ProbNet probNet;
    private Node medianDummyNode = null;
    private Node scaleDummyNode = null;
    private final Potential oldPotential;
    private final DiscretizedCauchyPotential newPotential;
    
    public DiscretizedCauchyPotentialPanel(Node node) {
        super();
        initComponents();
        this.probNet = node.getProbNet();
        this.oldPotential = node.getPotentials().get(0);
        this.newPotential = (DiscretizedCauchyPotential) oldPotential.copy();
        setData(node);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        editMedianButton = new JButton("Edit median potential");
        editMedianButton.addActionListener(e -> {
            editMedianPotential();
        });
        editScaleButton = new JButton("Edit scale potential");
        editScaleButton.addActionListener(e -> {
            editScalePotential();
        });
        buttonPanel.add(editMedianButton);
        buttonPanel.add(editScaleButton);
        add(buttonPanel, BorderLayout.PAGE_START);
    }
    
    private void editMedianPotential() {
        PotentialEditDialog potentialEditDialog = new PotentialEditDialog(GUIUtils.getOwner(this), medianDummyNode, isReadOnly());
        if (potentialEditDialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            // TODO: Do nothing?
        } else {
        
        }
    }
    
    private void editScalePotential() {
        PotentialEditDialog potentialEditDialog = new PotentialEditDialog(GUIUtils.getOwner(this), scaleDummyNode, isReadOnly());
        if (potentialEditDialog.requestValues() == OkCancelDialog.ChosenOption.Ok) {
            // TODO: Do nothing?
        } else {
        
        }
    }
    
    @Override
    public void setData(Node node) {
        ProbNet medianDummyNet = new ProbNet(probNet.getNetworkType());
        medianDummyNode = medianDummyNet.addPotential(newPotential.getMedian());
        medianDummyNet.getPNESupport().addListener(this);
        
        ProbNet scaleDummyNet = new ProbNet(probNet.getNetworkType());
        scaleDummyNode = scaleDummyNet.addPotential(newPotential.getScale());
        scaleDummyNet.getPNESupport().addListener(this);
    }
    
    @Override
    public void close() {
        medianDummyNode.getProbNet().getPNESupport().removeListener(this);
        scaleDummyNode.getProbNet().getPNESupport().removeListener(this);
    }
    
    @Override
    public boolean saveChanges() throws BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong, DoEditException {
        boolean result = super.saveChanges();
        newPotential.setComment(oldPotential.getComment());
        Node node = probNet.getNode(newPotential.getVariable(0));
        PotentialChangeEdit edit = new PotentialChangeEdit(node, oldPotential, newPotential);
        edit.executeEdit();
        return result;
    }
    
    private void update() throws NonProjectablePotentialException {
        TablePotential projectedPotential = newPotential.tableProject(new EvidenceCase(), null);
        // TODO update table with projected potential
    }
    
    @Override
    public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        // Update new potential and potential panel
        if (edit instanceof PotentialChangeEdit pcEdit) {
            newPotential.setMedian(medianDummyNode.getPotentials().get(0));
            newPotential.setScale(scaleDummyNode.getPotentials().get(0));
            
            //update();
        }
    }
}