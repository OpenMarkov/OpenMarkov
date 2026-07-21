/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.inference.common.ScopeSelectorPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Dialog that configures a temporal evolution analysis, letting the user choose
 * the number of time slices, scope, and pre-resolution evidence before running
 * the computation.
 */
public class TemporalEvolutionDialog extends OkCancelDialog {
    
    private JLabel numSlicesLabel;
    private JTextField numSlicesTextField;
    private Integer numSlices;
    private final ProbNet probNet;
    private ScopeSelectorPanel scopeSelectorPanel;
    private Node selectedNode;
    private EvidenceCase preResolutionEvidence;
//  Constructor split because now we have two cases: temporal evolution of a selected node and temporal evolution by criterion

//Former constructor.

//	/**
//	 * Constructor. initialises the instance.
//	 *
//	 * @param owner window that owns the dialog.
//	 */
//	public TemporalEvolutionDialog(Window owner, Node selectedNode, EvidenceCase preResolutionEvidence) {
//		super(owner);
//		setMinimumSize(new Dimension(300, 300));
//		this.setResizable(true);
//		this.probNet = selectedNode.getProbNet();
//		this.selectedNode = selectedNode;
//		this.preResolutionEvidence = new EvidenceCase(preResolutionEvidence.getFindings());;
//
//		this.setTitle(stringDatabase.getString("TemporalEvolutionResultDialog.Title) + selectedNode.getProbNet()
//				.getName());
//		getComponentsPanel().setLayout(new BoxLayout(getComponentsPanel(), BoxLayout.PAGE_AXIS));
//		getComponentsPanel().add(getSlicesPanel());
//		getComponentsPanel().add(getScopeSelectorPanel());
//		setLocationRelativeTo(owner);
//		this.pack();
//		this.setVisible(true);
//
//	}
    
    /**
     * Constructor. Initialises the instance for displaying probNet associated temporal evolution
     *
     * @param owner                 window that owns the dialog.
     * @param probNet               network from which an associated temporal evolution is displayed
     * @param preResolutionEvidence network pre-resolution evidence
     */
    public TemporalEvolutionDialog(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence) {
        super(owner);
        this.probNet = probNet;
        showWindow(owner, probNet, preResolutionEvidence);
    }
    
    
    /**
     * Constructor. Initialises the instance for displaying selectedNode temporal evolution
     *
     * @param owner                 window that owns the dialog.
     * @param selectedNode          node whose temporal evolution is displayed
     * @param preResolutionEvidence network pre-resolution evidence
     */
    public TemporalEvolutionDialog(Window owner, Node selectedNode, EvidenceCase preResolutionEvidence) {
        super(owner);
        this.selectedNode = selectedNode;
        this.probNet = selectedNode.getProbNet();
        showWindow(owner, probNet, preResolutionEvidence);
        
    }
    
    /**
     * Operations for showing temporal evolution dialog
     *
     * @param owner                 window that owns the dialog.
     * @param probNet               network from which an associated temporal evolution is displayed
     * @param preResolutionEvidence network pre-resolution evidence
     */
    private void showWindow(Window owner, ProbNet probNet, EvidenceCase preResolutionEvidence) {
        setMinimumSize(new Dimension(300, 300));
        this.setResizable(true);
        this.preResolutionEvidence = new EvidenceCase(preResolutionEvidence.getFindings());
        this.setTitle(stringDatabase.getString("TemporalEvolutionResultDialog.Title") + probNet
                .getName());
        getComponentsPanel().setLayout(new BoxLayout(getComponentsPanel(), BoxLayout.PAGE_AXIS));
        getComponentsPanel().add(getSlicesPanel());
        getComponentsPanel().add(getScopeSelectorPanel());
        setLocationRelativeTo(owner);
        this.pack();
        this.setVisible(true);
    }
    
    public JPanel getSlicesPanel() {
        JPanel slicesPanel = new JPanel();
        slicesPanel.add(getJLabelNumSlices());
        slicesPanel.add(getNumSlicesTextField());
        slicesPanel.setBorder(new TitledBorder(stringDatabase.getString("Inference.TemporalOptions")));
        slicesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        return slicesPanel;
    }
    
    private JLabel getJLabelNumSlices() {
        if (numSlicesLabel == null) {
            numSlicesLabel = new JLabel(stringDatabase.getString("CostEffectiveness.NumberOfCycles"));
        }
        return numSlicesLabel;
    }
    
    private JTextField getNumSlicesTextField() {
        if (numSlicesTextField == null) {
            numSlices = probNet.getInferenceOptions().getTemporalOptions().getHorizon();
            numSlicesTextField = new JTextField();
            numSlicesTextField.setText("" + numSlices);
            numSlicesTextField.setColumns(10);
            numSlicesTextField.setName("numSlicesTextField");
        }
        return numSlicesTextField;
    }
    
    public ScopeSelectorPanel getScopeSelectorPanel() {
        if (scopeSelectorPanel == null) {
            //20/10/2022 calling scopeSelectorPanel with temporalEvolution flag for not using "type panel" with (global/one decision)
            //scopeSelectorPanel = new ScopeSelectorPanel(probNet, preResolutionEvidence);
            scopeSelectorPanel = new ScopeSelectorPanel(probNet, preResolutionEvidence, true);
        }
        return scopeSelectorPanel;
    }
    
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    @Override protected boolean doOkClickBeforeHide() throws Exception {
        preResolutionEvidence.addFindings(scopeSelectorPanel.getSelectedFindings());
        try {
            numSlices = Integer.parseInt(numSlicesTextField.getText());
            probNet.getInferenceOptions().getTemporalOptions().setHorizon(numSlices);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException("Slices number", numSlicesTextField.getText(), "is not a valid number, as it must be a number between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE);
        }

//        /*
//        Window owner, Node node, EvidenceCase evidence,
//												Variable decisionSelected, List<Finding> scenario
//         */
//		TraceTemporalEvolutionDialog dialog = new TraceTemporalEvolutionDialog(getOwner(), selectedNode,
//				preResolutionEvidence, scopeSelectorPanel.getDecisionSelected());
        //Temporal evolution by criterion
        
        if (selectedNode == null) {
            new TraceTemporalEvolutionDialog(getOwner(), probNet,
                                             preResolutionEvidence, scopeSelectorPanel.getDecisionSelected());
        } else { //Temporal evolution of selected node
            TraceTemporalEvolutionDialog dialog = new TraceTemporalEvolutionDialog(getOwner(), selectedNode,
                                                                                   preResolutionEvidence, scopeSelectorPanel.getDecisionSelected());
        }
        
        return super.doOkClickBeforeHide();
    }
}
