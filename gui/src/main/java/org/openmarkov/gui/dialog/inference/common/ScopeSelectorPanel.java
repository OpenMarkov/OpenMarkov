/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.localize.StringDatabase;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Panel for selecting the scope and pre-resolution evidence for inference,
 * including global vs. per-decision analysis options.
 */
public class ScopeSelectorPanel extends JPanel {
    
    HashMap<JComboBox<String>, Variable> selectedScenario;
    private final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    private JPanel scopeTypePanel;
    private ButtonGroup scopeTypeSelector;
    private JPanel decisionSelectorPanel;
    private JPanel mainPanel;
    private final ProbNet probNet;
    private Variable decisionSelected;
    private JRadioButton globalRadioButton;
    private JRadioButton decisionRadioButton;
    private JPanel decisionScenarioPanel;
    private JScrollPane decisionScenarioScroll;
    private ScopeType scopeType;
    private JComboBox<String> decisionSelector;
    
    private List<Finding> selectedFindings;
    
    private final EvidenceCase preResolutionEvidence;
    
    public ScopeSelectorPanel(ProbNet probNet, EvidenceCase preResolutionEvidence) {
        super();
        this.probNet = probNet;
        this.preResolutionEvidence = preResolutionEvidence;
        selectedFindings = new ArrayList<>();
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setBorder(new TitledBorder(stringDatabase.getString("ScopeSelector.Title")));
        this.add(getMainPanel());
        this.setVisible(true);
        setMaximumSize(new Dimension(300, 300));
    }
    
    
    /**
     * Creates a ScopeSelectorPanel where scope panel is only shown if the analysis is not temporal evolution.
     *
     * @param probNet               network from which the temporal evolution of one/some of their nodes is shown
     * @param preResolutionEvidence evidence of probNet
     * @param isTemporalEvolution   if true, scope panel (for global/one decision) is removed because it makes no sense. It is shown otherwise
     */
    public ScopeSelectorPanel(ProbNet probNet, EvidenceCase preResolutionEvidence, boolean isTemporalEvolution) {
        this(probNet, preResolutionEvidence);
        if (isTemporalEvolution) mainPanel.remove(scopeTypePanel);
    }
    
    
    public JPanel getMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(getScopeTypePanel());
        
        mainPanel.add(getDecisionSelectorPanel());
        
        List<Node> decisionNodes = probNet.getNodes(NodeType.DECISION);
        if (decisionNodes == null || decisionNodes.isEmpty()) {
            for (Component component : scopeTypePanel.getComponents()) {
                component.setEnabled(false);
            }
        }
        
        decisionScenarioScroll = new JScrollPane(getDecisionScenarioPanel());
        mainPanel.add(decisionScenarioScroll);
        return mainPanel;
    }
    
    public JPanel getScopeTypePanel() {
        scopeTypePanel = new JPanel();
        scopeTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel scopeLabel = new JLabel(stringDatabase.getString("ScopeSelector.Type"));
        scopeTypePanel.add(scopeLabel);
        
        JPanel scopeTypeSelectorPanel = new JPanel();
        scopeTypeSelectorPanel.setLayout(new FlowLayout());
        
        scopeTypeSelector = new ButtonGroup();
        for (ScopeType scopeTypeEnum : ScopeType.values()) {
            JRadioButton selectedScopeType = new JRadioButton(stringDatabase.getString(scopeTypeEnum.toString()));
            scopeTypeSelector.add(selectedScopeType);
            
            //            scopeTypeSelector.addItem(stringDatabase.getValuesInAString(scopeTypeEnum.toString()));
            
            selectedScopeType.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JRadioButton scopeSelector = (JRadioButton) e.getSource();
                    if (scopeSelector.getText().equals(stringDatabase.getString(ScopeType.GLOBAL.toString()))) {
                        setScopeType(ScopeType.GLOBAL);
                        decisionSelected = null;
                        
                        if (decisionSelectorPanel != null) {
                            for (Component component : decisionSelectorPanel.getComponents()) {
                                component.setEnabled(false);
                            }
                        }
                    } else {
                        setScopeType(ScopeType.DECISION);
                        if (decisionSelector != null) {
                            decisionSelected = probNet.getVariable(decisionSelector.getSelectedItem().toString());
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
            if (scopeTypeEnum == ScopeType.GLOBAL) {
                globalRadioButton = selectedScopeType;
                scopeTypeSelectorPanel.add(globalRadioButton);
            } else if (scopeTypeEnum == ScopeType.DECISION) {
                decisionRadioButton = selectedScopeType;
                scopeTypeSelectorPanel.add(decisionRadioButton);
            }
        }
        scopeTypePanel.add(scopeTypeSelectorPanel);
        
        boolean couldBeGlobal = true;
        boolean couldBeDecision = true;
        
        // Get all the avaible decision nodes (without policy)
        List<Node> avaibleDecisionNodes = new ArrayList<>();
        for (Node node : probNet.getNodes(NodeType.DECISION)) {
            if (node.getPotentials().isEmpty()) {
                avaibleDecisionNodes.add(node);
            }
        }
        
        if (avaibleDecisionNodes.isEmpty()) {
            couldBeDecision = false;
        }
        
        if (probNet.getNetworkType() instanceof DecisionAnalysisNetworkType) {
            couldBeDecision = false;
        }
        
        if (!couldBeDecision || !couldBeGlobal) {
            //scopeTypeSelector.setSelectedItem(stringDatabase.getValuesInAString(ScopeType.GLOBAL.toString()));
            globalRadioButton.setSelected(true);
            decisionRadioButton.setEnabled(false);
            setScopeType(ScopeType.GLOBAL);
            for (Component component : scopeTypePanel.getComponents()) {
                component.setEnabled(false);
            }
        } else {
            //            scopeTypeSelector.setSelectedItem(stringDatabase.getValuesInAString(ScopeType.GLOBAL.toString()));
            decisionRadioButton.setSelected(true);
            setScopeType(ScopeType.DECISION);
            
        }
        
        return scopeTypePanel;
    }
    
    public JPanel getDecisionSelectorPanel() {
        
        decisionSelectorPanel = new JPanel();
        decisionSelectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        decisionSelectorPanel.add(new JLabel(stringDatabase.getString("ScopeSelector.DecisionSelector")));
        
        decisionSelector = new JComboBox<>();
        for (Node node : probNet.getNodes(NodeType.DECISION)) {
            // If the decision has not an imposed policy, add to the selector
            if (node.getPotentials().isEmpty()) {
                decisionSelector.addItem(node.getName());
            }
        }
        decisionSelector.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                String itemSelected = (String) ((JComboBox) e.getSource()).getSelectedItem();
                setDecisionSelected(probNet.getVariable(itemSelected));
            }
        });
        if (decisionSelector.getItemCount() > 0) {
            decisionSelector.setSelectedIndex(0);
        }
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
    
    public JPanel getDecisionScenarioPanel() {
        
        decisionScenarioPanel = new JPanel();
        decisionScenarioPanel.setLayout(new BoxLayout(decisionScenarioPanel, BoxLayout.PAGE_AXIS));
        decisionScenarioPanel.setBorder(new TitledBorder(stringDatabase.getString("ScopeSelector.Scenario")));
        
        if (decisionSelected != null && scopeType == ScopeType.DECISION) {
            selectedScenario = new HashMap<>();
            this.selectedFindings = new ArrayList<>();
            
            List<Variable> informationalPredecessors = ProbNetOperations
                    .getInformationalPredecessors(probNet, decisionSelected);
            for (Variable variable : informationalPredecessors) {
                JPanel informationalPredecessorPanel = new JPanel();
                informationalPredecessorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                if (!variable.equals(decisionSelected)) {
                    informationalPredecessorPanel.add(new JLabel(variable.getName()));
                    JComboBox<String> stateSelector = new JComboBox<>();
                    for (State state : variable.getStates()) {
                        stateSelector.addItem(state.getName());
                    }
                    if (preResolutionEvidence.contains(variable)) {
                        stateSelector.setSelectedItem(preResolutionEvidence.getFinding(variable).getState());
                        stateSelector.setEnabled(false);
                    }
                    selectedScenario.put(stateSelector, variable);
                    
                    stateSelector.addActionListener(e -> {
                        JComboBox<String> stateSelector1 = (JComboBox<String>) e.getSource();
                        String selectedStateString = stateSelector1.getSelectedItem().toString();
                        Variable selectedVariable = selectedScenario.get(stateSelector1);
                        State selectedState = selectedVariable.getState(selectedStateString);
                        if (selectedState != null) {
                            updateSelectedScenario();
                        }
                    });
                    
                    informationalPredecessorPanel.add(stateSelector);
                }
                decisionScenarioPanel.add(informationalPredecessorPanel);
            }
            updateSelectedScenario();
        }
        return decisionScenarioPanel;
    }
    
    private void updateSelectedScenario() {
        List<Finding> selectedFindings = new ArrayList<>();
        
        for (JComboBox<String> comboBox : selectedScenario.keySet()) {
            Variable variable = selectedScenario.get(comboBox);
            State state = variable.getState(comboBox.getSelectedItem().toString());
            if (state == null) continue;
            Finding finding = new Finding(variable, state);
            selectedFindings.add(finding);
        }
        
        this.selectedFindings = selectedFindings;
    }
    
    private void refreshScenario() {
        mainPanel.setVisible(false);
        if (decisionScenarioScroll != null) {
            mainPanel.remove(decisionScenarioScroll);
            decisionScenarioScroll = new JScrollPane(getDecisionScenarioPanel());
            mainPanel.add(decisionScenarioScroll);
        }
        mainPanel.setVisible(true);
    }
    
    public ScopeType getScopeType() {
        return scopeType;
    }
    
    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }
    
    public Variable getDecisionSelected() {
        return decisionSelected;
    }
    
    public void setDecisionSelected(Variable decisionSelected) {
        this.decisionSelected = decisionSelected;
        refreshScenario();
    }
    
    public List<Finding> getSelectedFindings() {
        return selectedFindings;
    }
}
