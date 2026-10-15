/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.dialog.inference.common.PossibleScenario;
import org.openmarkov.sensitivityanalysis.model.ScopeType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisConfiguration;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for scope selection
 *
 * @author jperez-martin
 */
public class ScopePanel extends JPanel {
    
    /**
     * Panel with scope type (decision/global)
     */
    private JPanel scopeTypePanel;
    
    /**
     * Panel with the decision selector
     */
    private JPanel decisionSelectorPanel;
    
    /**
     * Panel with the scenario controls
     */
    private JPanel decisionScenarioPanel;
    
    /**
     * main panel with all controls
     */
    private JPanel mainPanel;
    
    /**
     * Decision selector control
     */
    private JComboBox<String> decisionSelector;
    
    /**
     * Configuration of the uncertainty model
     */
    private SensitivityAnalysisConfiguration configuration;
    
    /**
     * ProbNet
     */
    private ProbNet probNet;
    
    /**
     * Selected decision
     */
    private Variable decisionSelected;
    
    /**
     * Selected scenario
     */
    /** The selectors of the scenario; null with a global scope. */
    private PossibleScenario selectedScenario;
    
    /**
     * Controller
     */
    private SensitivityAnalysisController controller;
    
    /**
     * Scope type
     */
    private ScopeType scopeType;
    
    private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * Scope panel constructor
     *
     * @param controller
     */
    public ScopePanel(SensitivityAnalysisController controller) {
        super();
        this.controller = controller;
        this.configuration = controller.getConfiguration();
        this.probNet = controller.getProbNet();
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.add(getMainPanel());
    }
    
    /**
     * Builds the main panel
     *
     * @return
     */
    public JPanel getMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(getScopeTypePanel());
        
        if (configuration.isCanBeDecision()) {
            mainPanel.add(getDecisionSelectorPanel());
            
            List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
            if (decisionNodes == null || decisionNodes.isEmpty()) {
                for (Component component : scopeTypePanel.getComponents()) {
                    component.setEnabled(false);
                }
            }
            
            mainPanel.add(getDecisionScenarioPanel());
        }
        return mainPanel;
    }
    
    /**
     * Builds the scope type panel
     *
     * @return
     */
    public JPanel getScopeTypePanel() {
        scopeTypePanel = new JPanel();
        scopeTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel scopeLabel = new JLabel(stringDatabase.getString("ScopeSelector.Title"));
        scopeTypePanel.add(scopeLabel);
        
        JComboBox<String> scopeTypeSelector = new JComboBox<>();
        for (ScopeType scopeTypeEnum : ScopeType.values()) {
            scopeTypeSelector.addItem(stringDatabase.getString(scopeTypeEnum.toString()));
        }
        scopeTypeSelector.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JComboBox<String> scopeSelector = (JComboBox<String>) e.getSource();
                if (scopeSelector.getSelectedItem().equals(stringDatabase.getString(ScopeType.GLOBAL.toString()))) {
                    setScopeType(ScopeType.GLOBAL);
                    decisionSelected = null;
                    controller.getSensitivityAnalysisModel().setDecisionVariable(decisionSelected);
                    
                    if (decisionSelectorPanel != null) {
                        for (Component component : decisionSelectorPanel.getComponents()) {
                            component.setEnabled(false);
                        }
                    }
                } else {
                    setScopeType(ScopeType.DECISION);
                    if (decisionSelector != null) {
                        decisionSelected = probNet.getVariable(decisionSelector.getSelectedItem().toString());
                        controller.getSensitivityAnalysisModel().setDecisionVariable(decisionSelected);
                    }
                    if (decisionSelectorPanel != null) {
                        for (Component component : decisionSelectorPanel.getComponents()) {
                            component.setEnabled(true);
                        }
                    }
                }
                refreshScenario();
            }
        });
        scopeTypePanel.add(scopeTypeSelector);
        
        //        // Get all the avaible decision nodes (without policy)
        //        List<Node> avaibleDecisionNodes = new ArrayList<>();
        //        for (Node node : probNet.getNodes(NodeType.DECISION)) {
        //            if (node.getPotentials().size() == 0) {
        //                avaibleDecisionNodes.add(node);
        //            }
        //        }
        //
        //        if(avaibleDecisionNodes.size() == 0){
        //            configuration.setCanBeDecision(false);
        //        }
        
        if (!configuration.isCanBeDecision() || !configuration.isCanBeGlobal()) {
            if (configuration.isCanBeGlobal()) {
                scopeTypeSelector.setSelectedItem(stringDatabase.getString(ScopeType.GLOBAL.toString()));
            } else {
                scopeTypeSelector.setSelectedItem(stringDatabase.getString(ScopeType.DECISION.toString()));
            }
            for (Component component : scopeTypePanel.getComponents()) {
                component.setEnabled(false);
            }
        } else {
            scopeTypeSelector.setSelectedItem(stringDatabase.getString(ScopeType.GLOBAL.toString()));
        }
        
        return scopeTypePanel;
    }
    
    /**
     * Builds decision selector panel
     *
     * @return
     */
    public JPanel getDecisionSelectorPanel() {
        
        decisionSelectorPanel = new JPanel();
        decisionSelectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        decisionSelectorPanel.add(new JLabel(stringDatabase.getString("ScopeSelector.DecisionSelector")));
        
        decisionSelector = new JComboBox<>();
        for (Node node : probNet.getNodes(NodeType.DECISION)) {
            // If the decision has not an imposed policy, add to the selector
            //            if (node.getPotentials().size() == 0) {
            decisionSelector.addItem(node.getName());
            //            }
        }
        decisionSelector.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                String itemSelected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                setDecisionSelected(probNet.getVariable(itemSelected));
            }
        });
        decisionSelector.setSelectedIndex(0);
        decisionSelectorPanel.add(decisionSelector);
        
        if (scopeType == ScopeType.GLOBAL) {
            setDecisionSelected(null);
            for (Component component : decisionSelectorPanel.getComponents()) {
                component.setEnabled(false);
            }
        } else {
            decisionSelected = probNet.getVariable(decisionSelector.getSelectedItem().toString());
        }
        
        return decisionSelectorPanel;
    }
    
    /**
     * Updates the selected scope type
     *
     * @param scopeType
     */
    private void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
        controller.getSensitivityAnalysisModel().setScopeType(scopeType);
        
    }
    
    /**
     * Builds the decision scenario panel
     *
     * @return
     */
    public JPanel getDecisionScenarioPanel() {
        return getDecisionScenarioPanel(List.of());
    }
    
    /**
     * Builds the decision scenario panel, each selector starting from its state in {@code previousChoices} while it
     * is possible
     */
    private JPanel getDecisionScenarioPanel(List<Finding> previousChoices) {
        decisionScenarioPanel = new JPanel();
        decisionScenarioPanel.setLayout(new BoxLayout(decisionScenarioPanel, BoxLayout.PAGE_AXIS));
        decisionScenarioPanel.setBorder(new TitledBorder(stringDatabase.getString("ScopeSelector.Scenario")));
        
        if (decisionSelected != null && scopeType == ScopeType.DECISION) {
            selectedScenario = new PossibleScenario(probNet, controller.getPreResolutionEvidence(), previousChoices);
            controller.getSensitivityAnalysisModel().setSelectedScenario(new ArrayList<Finding>());
            
            for (Variable variable : ProbNetOperations.getInformationalPredecessors(probNet, decisionSelected)) {
                JPanel informationalPredecessorPanel = new JPanel();
                informationalPredecessorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                if (!variable.equals(decisionSelected)) {
                    informationalPredecessorPanel.add(new JLabel(variable.getName()));
                    informationalPredecessorPanel.add(selectedScenario.selector(variable, this::updateSelectedScenario));
                }
                decisionScenarioPanel.add(informationalPredecessorPanel);
            }
            selectedScenario.offerPossibleStates();
            updateSelectedScenario();
        }
        
        return decisionScenarioPanel;
    }
    
    /**
     * Updates the selected scenario
     */
    private void updateSelectedScenario() {
        controller.getSensitivityAnalysisModel().setSelectedScenario(selectedScenario.findings());
    }
    
    /**
     * Rebuilds the scenario against the evidence the network has now: a variable that has gained
     * evidence since the panel was built shows its state and can no longer be chosen.
     */
    public void refreshScenarioFromEvidence() {
        // The scenario chosen by the user is kept while it is still possible: rebuilding it from scratch put every
        // selector back to its first state, and the analysis was made with a scenario the user had not chosen
        refreshScenario(selectedScenario == null ? List.of() : selectedScenario.findings());
        // With a global scope there is no scenario to update.
        if (selectedScenario != null) {
            updateSelectedScenario();
        }
    }

    /**
     * Rebuilds the scenario panel
     */
    private void refreshScenario() {
        refreshScenario(List.of());
    }
    
    /**
     * Rebuilds the scenario panel, each selector starting from its state in {@code previousChoices} while it is
     * possible
     */
    private void refreshScenario(List<Finding> previousChoices) {
        mainPanel.setVisible(false);
        if (decisionScenarioPanel != null) {
            mainPanel.remove(decisionScenarioPanel);
            mainPanel.add(getDecisionScenarioPanel(previousChoices));
        }
        mainPanel.setVisible(true);
    }
    
    /**
     * Updates the selected decision
     *
     * @param decisionSelected
     */
    private void setDecisionSelected(Variable decisionSelected) {
        this.decisionSelected = decisionSelected;
        controller.getSensitivityAnalysisModel().setDecisionVariable(decisionSelected);
        refreshScenario();
    }
}