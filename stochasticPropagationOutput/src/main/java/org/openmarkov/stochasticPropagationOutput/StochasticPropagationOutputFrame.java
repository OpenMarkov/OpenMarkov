/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.stochasticPropagationOutput;


import org.openmarkov.core.exception.*;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.io.OMFileChooser;
import org.openmarkov.gui.exception.NoNetOpenedException;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterPropagation;
import org.openmarkov.inference.algorithm.huginPropagation.HuginPropagation;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LikelihoodWeighting;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LogicSampling;
import org.openmarkov.inference.algorithm.likelihoodWeighting.StochasticPropagation;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Creates the plugin dialog for exporting stochastic propagation data.
 *
 * @author iagoparis - spring 2018
 */
public class StochasticPropagationOutputFrame extends JDialog implements ActionListener {
    
    private final int DEFAULT_SAMPLES = 10000;
    protected ProbNet probNet;
    private EvidenceCase preResolutionEvidence;
    private EvidenceCase postResolutionEvidence;
    String algorithmName;
    
    // Gui components
    private ButtonGroup algorithms;
    private JTextField sampleSizeNumber;
    
    /**
     * Localized stringDatabase
     */
    final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * The constructor creates the dialog's window.
     *
     * @param owner window that owns the dialog.
     */
    public StochasticPropagationOutputFrame(JFrame owner) {
        super(owner);
        this.setTitle(stringDatabase.getString("StochasticPropagationOutput.Window"));
        
        // Get data from the net
        
        NetworkEditorPanel networkPanel = MainGUI.INSTANCE.mainPanel
                                             .getMainPanelListenerAssistant()
                                             .getCurrentNetworkEditorPanel();
        if (networkPanel == null) {
            return;
        }
        this.probNet = networkPanel.getProbNet();
        this.preResolutionEvidence = networkPanel.getEditorPanel().getEvidenceManager().getPreResolutionEvidence();
        this.postResolutionEvidence = networkPanel.getEditorPanel().getEvidenceManager().getCurrentEvidenceCase();
        var boundsException = boundError();
        if (boundsException != null) {
            return;
        }
        
        // Window basics
        JPanel content = new JPanel();
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(padding);
        this.setContentPane(content);
        this.setLocation(300, 300);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        
        
        /*"********
         * Dialog *
         **********/
        GridBagLayout mainLayout = new GridBagLayout();
        content.setLayout(mainLayout);
        GridBagConstraints constraints = new GridBagConstraints();
        
        // Algorithm buttons
        algorithms = new ButtonGroup(); // The round checkboxes that allow only one option enabled.
        JPanel algorithmsPanel = new JPanel();
        algorithmsPanel.setBorder(BorderFactory.createTitledBorder(stringDatabase.getString("Dialog.Algorithms")));
        algorithmsPanel.setLayout(new GridLayout(2, 1, 1, 1));
        
        
        JRadioButton logicSampling = new JRadioButton(stringDatabase.getString("Algorithms.LogicSampling"));
        logicSampling.setActionCommand(stringDatabase.getString("Algorithms.LogicSampling"));
        algorithms.add(logicSampling);
        algorithmsPanel.add(logicSampling);
        
        JRadioButton likelihoodWeighting = new JRadioButton(stringDatabase.
                                                                    getString("Algorithms.LikelihoodWeighting"));
        likelihoodWeighting.setActionCommand(stringDatabase.getString("Algorithms.LikelihoodWeighting"));
        algorithms.add(likelihoodWeighting);
        algorithmsPanel.add(likelihoodWeighting);
        
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 1;
        content.add(algorithmsPanel, constraints);
        
        likelihoodWeighting.setSelected(true); // Likelihood weighting by default
        
        
        // Sample foreground field
        JPanel sampleSize = new JPanel();
        JLabel NumberOfSamples = new JLabel(stringDatabase.getString("Dialog.NSamples"));
        sampleSize.add(NumberOfSamples);
        sampleSizeNumber = new JTextField(Integer.toString(DEFAULT_SAMPLES), 6);
        sampleSize.add(sampleSizeNumber);
        sampleSize.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weighty = 0.5;
        content.add(sampleSize, constraints);
        
        // Save to xlsx button
        JButton xlsxOutput = new JButton(stringDatabase.getString("Dialog.PrintXlsx"));
        xlsxOutput.addActionListener(this); // Only component listened
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weighty = 2;
        content.add(xlsxOutput, constraints);
        
        this.pack();  // Sizes window to preferred size of contents.
    }
    
    /**
     * Checks whether the current network state is valid for stochastic propagation output.
     * Validates that a network is open, is a Bayesian network, and has nodes.
     *
     * @return an exception describing the problem, or {@code null} if no error
     */
    public Exception boundError() {
        // Stop if there is no net opened yet
        if (probNet == null) {
            return new NoNetOpenedException();
        }
        // Stop if other than bayesian net
        if (!(probNet.getNetworkType() instanceof BayesianNetworkType)) {
            return new InvalidNetworkTypeException.NotAllowedType(probNet, Arrays.asList(BayesianNetworkType.getUniqueInstance()));
        }
        // Stop if no nodes in the net
        if (probNet.getNumNodes() == 0) {
            return new NetworkHasNoNodesException(probNet);
        }
        return null;
    }
    
    // Listener for the dialog events. It only listens to the xlsx button
    // and retrieves data from attributes of the dialog.
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            onActionRequest();
        } catch (IncompatibleEvidenceException | ConstraintViolatedException |
                 NotEvaluableNetworkException.NotApplicableNetwork | NonProjectablePotentialException |
                 CannotNormalizePotentialException e) {
            throw new UnrecoverableException(e);
        }
    } // end of actionPerformed
    
    private void onActionRequest() throws IncompatibleEvidenceException, ConstraintViolatedException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, CannotNormalizePotentialException {
        ButtonModel algorithmButton = algorithms.getSelection();
        ClusterPropagation exactAlgorithm;
        algorithmName = algorithmButton.getActionCommand();
        
        // Create the selected algorithms
        StochasticPropagation algorithm = null;
            if (algorithmName.equals(stringDatabase.getString("Algorithms.LikelihoodWeighting"))) {
                algorithm = new LikelihoodWeighting(probNet);
            } else if (algorithmName.equals(stringDatabase.getString("Algorithms.LogicSampling"))) {
                algorithm = new LogicSampling(probNet);
            }
            exactAlgorithm = new HuginPropagation(probNet);
            
        // Prepare the exact algorithm
        exactAlgorithm.setStorageLevel(ClusterPropagation.StorageLevel.MEDIUM);
        exactAlgorithm.compilePriorPotentials();
        
        // Set the evidence in the algorithms
            algorithm.setPreResolutionEvidence(preResolutionEvidence);
        algorithm.setPostResolutionEvidence(postResolutionEvidence);
            exactAlgorithm.setPreResolutionEvidence(preResolutionEvidence);
        exactAlgorithm.setPostResolutionEvidence(postResolutionEvidence);
        
        // Set the sample size from the GUI foreground field into the stochastic algorithm
        int sampleSize;
        try {
            sampleSize = Integer.parseUnsignedInt(sampleSizeNumber.getText());
            algorithm.setSampleSize(sampleSize);
        } catch (NumberFormatException e) {
            throw new UnrecoverableException(e);
        }
        
        // Set the stochastic algorithm to store the samples
        algorithm.setStoringSamples(true);
        
        // Propagate
        HashMap<Variable, TablePotential> exactPosteriorValues;
            algorithm.getPosteriorValues();
            exactPosteriorValues = exactAlgorithm.getPosteriorValues();
        
        // Name for the spreadsheet created.
        String fileName;
        
        /* Save dialog */
        
        OMFileChooser omFileChooser = new OMFileChooser() { // Set starting directory
            // Modify the omFileChooser class on creation to ask for overwrite confirmation
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, stringDatabase.getString("Warnings.Overwrite"),
                                                               stringDatabase.getString("Warnings.OverwriteTitle"), JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION, JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        omFileChooser.setCurrentDirectory(LocalPreferences.LATEST_OPEN_DIRECTORY.get());
        
        // Choose where to save the file
        
        omFileChooser.setSelectedFile(new File(formatCleaner(probNet.getName()) + " - "
                                                     + algorithmName + ".xlsx"));
        omFileChooser.setDialogTitle(stringDatabase.getString("SaveDialog.Title"));
        int optionChosen = omFileChooser.showSaveDialog(this);
        if (optionChosen == JFileChooser.APPROVE_OPTION) {
            fileName = omFileChooser.getSelectedFile().getAbsolutePath();
            if (!fileName.endsWith(".xlsx")) {
                fileName += ".xlsx";
            }
            dispose(); // Closes the dialog
        } else {
            return;
        }
        // Write to xlsx
        Thread writingThread = new Thread(new XlsxWrite(this, algorithm, exactPosteriorValues, fileName));
        writingThread.start();
    }
    
    
    // Given a file name. Deletes the (4-1) characters after and last dot (usually the format)
    private static String formatCleaner(String name) {
        if (name.contains(".")) {
            int lastPointPosition;
            String restOfName = name;
            do {
                lastPointPosition = restOfName.indexOf('.');
                restOfName = restOfName.substring(lastPointPosition + 1);
            } while (restOfName.contains("."));
            
            if (restOfName.length() <= 4 && !restOfName.isEmpty()) {
                name = name.substring(0, name.length() - (restOfName.length() + 1));
            }
        }
        return name;
    }
    
    
}
