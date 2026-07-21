/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.action.core.DecisionCriteriaEdit;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for selecting a standard set of decision criteria (e.g. cost-effectiveness)
 * to apply to the network.
 */
public class StandardCriteriaDialog extends OkCancelDialog {
    
    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 7540156073158685187L;
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    /**
     * Standard criteria panel
     */
    private StandardCriteriaPanel standardCriteriaPanel;
    private final ProbNet probNet;
    
    public StandardCriteriaDialog(Window owner, ProbNet probNet) {
        super(owner);
        this.probNet = probNet;
        initialize();
        setLocationRelativeTo(owner);
        setResizable(true);
        pack();
    }
    
    private void initialize() {
        configureComponentsPanel();
        pack();
    }
    
    private void configureComponentsPanel() {
        getComponentsPanel().setLayout(new BorderLayout(5, 5));
        getComponentsPanel().add(getJPanelStandardCriteria(), BorderLayout.CENTER);
        
    }
    
    public JPanel getJPanelStandardCriteria() {
        
        if (standardCriteriaPanel == null) {
            standardCriteriaPanel = new StandardCriteriaPanel();
            //statesCheckBoxPanel.setLayout( new FlowLayout());
            standardCriteriaPanel.setName("jPanelStandardCriteria");
        }
        return standardCriteriaPanel;
    }
    
    /**
     * This method carries out the actions when the user press the Ok button
     * before hide the dialog.
     *
     * @return true if the dialog box can be closed.
     */
    @Override protected boolean doOkClickBeforeHide() throws DoEditException {
        
        // This must be a single operation
        probNet.getPNESupport().openNewSubEditHistory();
        
        // Remove all criterion in the probNet with edits
        while (!probNet.getDecisionCriteria().isEmpty()) {
            Criterion criterionToBeDeleted = probNet.getDecisionCriteria().get(0);
            DecisionCriteriaEdit edit = new DecisionCriteriaEdit(probNet, StateAction.REMOVE, criterionToBeDeleted, null);
            edit.executeEdit();
        }
        
        //Get the list with the new criteria
        List<Criterion> defaultCriteria = getDefaultCriteria();
        
        // Add new criteria to the probNet
        for (Criterion criterion : defaultCriteria) {
            DecisionCriteriaEdit edit = new DecisionCriteriaEdit(probNet, StateAction.ADD, criterion, null);
            edit.executeEdit();
        }
        
        // Set as default the first criteria of the probNet
        Criterion defaultCriterion = probNet.getDecisionCriteria().get(0);
        for (Variable variable : probNet.getVariables(NodeType.UTILITY)) {
            variable.setDecisionCriterion(defaultCriterion);
        }
        
        probNet.getPNESupport().closeSubEditHistory();
        this.getParent().repaint();
        return true;
    }
    
    public ChosenOption requestValues() {
        
        setVisible(true);
        
        return getSelectedOption();
    }
    
    /**
     * Gets a list with the default criteria selected. Gets the string with the selected default criteria (from the standardCriteriaPanel) and parses it.
     *
     * @return A list with the new criteria
     */
    private List<Criterion> getDefaultCriteria() {
        List<Criterion> defaultCriteria = new ArrayList<Criterion>();
        
        String selectedDefaultCriteria = null;
        for (JRadioButton radioButton : standardCriteriaPanel.getRadioButtons()) {
            if (radioButton.isSelected()) {
                selectedDefaultCriteria = radioButton.getText();
            }
        }
        
        if (selectedDefaultCriteria.equals(stringDatabase.getString("defaultCriteria.costBenefitEuros.Text"))) {
            Criterion costCriterion = new Criterion(stringDatabase.getString("defaultCriteria.cost.Text"),
                                                    stringDatabase.getString("defaultCriteria.euros.Text"));
            Criterion benefitCriterion = new Criterion(stringDatabase.getString("defaultCriteria.benefit.Text"),
                                                       stringDatabase.getString("defaultCriteria.euros.Text"));
            costCriterion.setUnicriterizationScale(-1);
            
            defaultCriteria.add(costCriterion);
            defaultCriteria.add(benefitCriterion);
        } else if (selectedDefaultCriteria.equals(stringDatabase.getString("defaultCriteria.costBenefitPounds.Text"))) {
            Criterion costCriterion = new Criterion(stringDatabase.getString("defaultCriteria.cost.Text"),
                                                    stringDatabase.getString("defaultCriteria.pounds.Text"));
            Criterion benefitCriterion = new Criterion(stringDatabase.getString("defaultCriteria.benefit.Text"),
                                                       stringDatabase.getString("defaultCriteria.pounds.Text"));
            costCriterion.setUnicriterizationScale(-1);
            
            defaultCriteria.add(costCriterion);
            defaultCriteria.add(benefitCriterion);
        } else if (selectedDefaultCriteria
                .equals(stringDatabase.getString("defaultCriteria.costBenefitDollars.Text"))) {
            Criterion costCriterion = new Criterion(stringDatabase.getString("defaultCriteria.cost.Text"),
                                                    stringDatabase.getString("defaultCriteria.dollars.Text"));
            Criterion benefitCriterion = new Criterion(stringDatabase.getString("defaultCriteria.benefit.Text"),
                                                       stringDatabase.getString("defaultCriteria.dollars.Text"));
            costCriterion.setUnicriterizationScale(-1);
            
            defaultCriteria.add(costCriterion);
            defaultCriteria.add(benefitCriterion);
        } else if (selectedDefaultCriteria
                .equals(stringDatabase.getString("defaultCriteria.costEffectivenessEurosQALY.Text"))) {
            Criterion costCriterion = new Criterion(stringDatabase.getString("defaultCriteria.cost.Text"),
                                                    stringDatabase.getString("defaultCriteria.euros.Text"));
            costCriterion.setCECriterion(CECriterion.Cost);
            
            Criterion effectivenessCriterion = new Criterion(
                    stringDatabase.getString("defaultCriteria.effectiveness.Text"),
                    stringDatabase.getString("defaultCriteria.qaly.Text"));
            effectivenessCriterion.setCECriterion(CECriterion.Effectiveness);
            
            defaultCriteria.add(costCriterion);
            defaultCriteria.add(effectivenessCriterion);
        } else if (selectedDefaultCriteria
                .equals(stringDatabase.getString("defaultCriteria.costEffectivenessPoundsQALY.Text"))) {
            Criterion costCriterion = new Criterion(stringDatabase.getString("defaultCriteria.cost.Text"),
                                                    stringDatabase.getString("defaultCriteria.pounds.Text"));
            costCriterion.setCECriterion(CECriterion.Cost);
            
            Criterion effectivenessCriterion = new Criterion(
                    stringDatabase.getString("defaultCriteria.effectiveness.Text"),
                    stringDatabase.getString("defaultCriteria.qaly.Text"));
            effectivenessCriterion.setCECriterion(CECriterion.Effectiveness);
            
            defaultCriteria.add(costCriterion);
            defaultCriteria.add(effectivenessCriterion);
        } else if (selectedDefaultCriteria
                .equals(stringDatabase.getString("defaultCriteria.costEffectivenessDollarsQALY.Text"))) {
            Criterion costCriterion = new Criterion(stringDatabase.getString("defaultCriteria.cost.Text"),
                                                    stringDatabase.getString("defaultCriteria.dollars.Text"));
            costCriterion.setCECriterion(CECriterion.Cost);
            
            Criterion effectivenessCriterion = new Criterion(
                    stringDatabase.getString("defaultCriteria.effectiveness.Text"),
                    stringDatabase.getString("defaultCriteria.qaly.Text"));
            effectivenessCriterion.setCECriterion(CECriterion.Effectiveness);
            
            defaultCriteria.add(costCriterion);
            defaultCriteria.add(effectivenessCriterion);
        }
        
        return defaultCriteria;
        
    }
    
}
