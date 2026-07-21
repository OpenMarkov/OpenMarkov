/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.tasks.OptimalIntervention;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.treeadd.TreeADDCellRenderer;
import org.openmarkov.gui.dialog.treeadd.TreeADDEditorPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog that displays the optimal strategy (decision policies) computed by
 * inference, rendered as a tree using the TreeADD editor.
 */
@SuppressWarnings("serial") public class OptimalStrategyDialog extends OkCancelDialog {

	public OptimalStrategyDialog(Window owner, ProbNet probNet, OptimalIntervention optimalIntervention)
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		super(owner);
		TreeADDCellRenderer cellRenderer = new TreeADDCellRenderer(probNet);
		ProbNet dummyProbNet = new ProbNet();
		Node dummyNode = new Node(dummyProbNet, new Variable("Global utility"), NodeType.UTILITY);
        dummyNode.setPotential(optimalIntervention.getOptimalIntervention());
        
        //VEPosteriorValues vePosteriorValues = new VEPosteriorValues(probNet,probNet.getVariables(),preResolutionEvidence,evidence);
		//individualProbabilities = vePosteriorValues.getPosteriorValues();

		TreeADDEditorPanel treeADDEditorPanel = new TreeADDEditorPanel(cellRenderer, dummyNode);

		setMinimumSize(new Dimension(500, 500));
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		treeADDEditorPanel.setMinimumSize(new Dimension(450, 400));
		panel.add(treeADDEditorPanel, BorderLayout.CENTER);

		add(panel);
		pack();

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = (int) (screenSize.getWidth() - getSize().getWidth()) / 2;
		int y = (int) (screenSize.getHeight() - getSize().getHeight()) / 2;
		setLocation(new Point(x, y));
		setTitle(stringDatabase.getString("Decision.ShowOptimalStrategy.Title"));
		setResizable(true);
	}

	public OptimalStrategyDialog(Window owner, ProbNet probNet, StrategyTree optimalStrategyTree) {
		super(owner);
		TreeADDCellRenderer cellRenderer = new TreeADDCellRenderer(probNet);
		ProbNet dummyProbNet = new ProbNet();
		Node dummyNode = new Node(dummyProbNet, new Variable("Global utility"), NodeType.UTILITY);
		dummyNode.setPotential(optimalStrategyTree);
        
        //VEPosteriorValues vePosteriorValues = new VEPosteriorValues(probNet,probNet.getVariables(),preResolutionEvidence,evidence);
		//individualProbabilities = vePosteriorValues.getPosteriorValues();

		TreeADDEditorPanel treeADDEditorPanel = new TreeADDEditorPanel(cellRenderer, dummyNode);

		setMinimumSize(new Dimension(500, 500));
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		treeADDEditorPanel.setMinimumSize(new Dimension(450, 400));
		panel.add(treeADDEditorPanel, BorderLayout.CENTER);

		add(panel);
		pack();

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = (int) (screenSize.getWidth() - getSize().getWidth()) / 2;
		int y = (int) (screenSize.getHeight() - getSize().getHeight()) / 2;
		setLocation(new Point(x, y));
		setTitle(stringDatabase.getString("Decision.ShowOptimalStrategy.Title"));
		setResizable(true);
	}

}
