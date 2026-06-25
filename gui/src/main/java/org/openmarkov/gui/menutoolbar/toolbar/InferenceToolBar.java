/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar;

import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.graphic.VisualState;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * This class implements the inference toolbar of the application. This toolbar
 * appears when entering Inference Working Mode
 *
 * @author asaez
 * @version 1.0
 */
public class InferenceToolBar extends ToolBarBasic implements MouseMotionListener {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 2660820001862866000L;
    /**
     * Label for expansion threshold.
     */
    private JLabel expansionThreshold = null;
    /**
     * ComboBox to select the expansion threshold.
     */
    private ExpansionThresholdComboBox expansionThresholdComboBox = null;
    /**
     * Button to create a new evidence case.
     */
    private JButton createNewEvidenceCaseButton = null;
    /**
     * Button to go the first evidence case.
     */
    private JButton goToFirstEvidenceCaseButton = null;
    /**
     * Button to go the previous evidence case.
     */
    private JButton goToPreviousEvidenceCaseButton = null;
    /**
     * Text field for the name of the current evidence case.
     */
    private JTextField currentEvidenceCaseName = null;
    /**
     * Button to go the next evidence case.
     */
    private JButton goToNextEvidenceCaseButton = null;
    /**
     * Button to go the last evidence case.
     */
    private JButton goToLastEvidenceCaseButton = null;
    /**
     * Button to clear out all evidence cases.
     */
    private JButton clearOutAllEvidenceCasesButton = null;
    /**
     * Button to propagateEvidence.
     */
    private JButton propagateEvidenceButton = null;
    
    /**
     * This method initializes this instance.
     *
     * @param newListener object that listens to the buttons events.
     */
    public InferenceToolBar(ActionListener newListener) {
        super(newListener);
        initialize();
    }
    
    /**
     * This method configures the toolbar.
     */
    private void initialize() {
        add(getCreateNewEvidenceCaseButton());
        add(getClearOutAllEvidenceCasesButton());
        addSeparator();
        add(getGoToFirstEvidenceCaseButton());
        add(getGoToPreviousEvidenceCaseButton());
        add(getCurrentEvidenceCaseName());
        add(getGoToNextEvidenceCaseButton());
        add(getGoToLastEvidenceCaseButton());
        addSeparator();
        add(getExpansionThresholdLabel());
        add(getExpansionThresholdComboBox());
        add(Box.createHorizontalGlue());
    }
    
    /**
     * This method inserts the Expansion Threshold Label.
     *
     * @return a Expansion Threshold label.
     */
    private JLabel getExpansionThresholdLabel() {
        if (expansionThreshold == null) {
            expansionThreshold = new JLabel();
            expansionThreshold.setText("  " + stringDatabase.getString("ExpansionThreshold") + ": ");
        }
        return expansionThreshold;
    }
    
    /**
     * This method initializes expansionThresholdComboBox.
     *
     * @return a new expansion threshold comboBox.
     */
    private ExpansionThresholdComboBox getExpansionThresholdComboBox() {
        if (expansionThresholdComboBox == null) {
            expansionThresholdComboBox = new ExpansionThresholdComboBox(listener);
        }
        return expansionThresholdComboBox;
    }
    
    /**
     * This method initializes createNewEvidenceCaseButton.
     *
     * @return a Create New Evidence Case button.
     */
    private JButton getCreateNewEvidenceCaseButton() {
        if (createNewEvidenceCaseButton == null) {
            createNewEvidenceCaseButton = new JButton();
            createNewEvidenceCaseButton.setIcon(IconBind.CREATE_NEW_EVIDENCE_CASE_ENABLED.icon());
            createNewEvidenceCaseButton.setFocusable(false);
            createNewEvidenceCaseButton.setActionCommand(ActionCommands.CREATE_NEW_EVIDENCE_CASE.getCommandName());
            createNewEvidenceCaseButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.CREATE_NEW_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
            createNewEvidenceCaseButton.addActionListener(listener);
            createNewEvidenceCaseButton.addMouseMotionListener(this);
        }
        return createNewEvidenceCaseButton;
    }
    
    /**
     * This method initializes goToFirstEvidenceCaseButton.
     *
     * @return a Go To First Evidence Case button.
     */
    private JButton getGoToFirstEvidenceCaseButton() {
        if (goToFirstEvidenceCaseButton == null) {
            goToFirstEvidenceCaseButton = new JButton();
            goToFirstEvidenceCaseButton.setIcon(IconBind.GO_TO_FIRST_EVIDENCE_CASE_ENABLED.icon());
            goToFirstEvidenceCaseButton.setFocusable(false);
            goToFirstEvidenceCaseButton.setActionCommand(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE.getCommandName());
            goToFirstEvidenceCaseButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
            goToFirstEvidenceCaseButton.addActionListener(listener);
            goToFirstEvidenceCaseButton.addMouseMotionListener(this);
        }
        return goToFirstEvidenceCaseButton;
    }
    
    /**
     * This method initializes goToPreviousEvidenceCaseButton.
     *
     * @return a Go To Previous Evidence Case button.
     */
    private JButton getGoToPreviousEvidenceCaseButton() {
        if (goToPreviousEvidenceCaseButton == null) {
            goToPreviousEvidenceCaseButton = new JButton();
            goToPreviousEvidenceCaseButton
                    .setIcon(IconBind.GO_TO_PREVIOUS_EVIDENCE_CASE_ENABLED.icon());
            goToPreviousEvidenceCaseButton.setFocusable(false);
            goToPreviousEvidenceCaseButton.setActionCommand(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE.getCommandName());
            goToPreviousEvidenceCaseButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
            goToPreviousEvidenceCaseButton.addActionListener(listener);
            goToPreviousEvidenceCaseButton.addMouseMotionListener(this);
        }
        return goToPreviousEvidenceCaseButton;
    }
    
    /**
     * This method initializes the foreground field of the Current Evidence Case.
     *
     * @return a Text Field with the name of the evidence case.
     */
    private JTextField getCurrentEvidenceCaseName() {
        if (currentEvidenceCaseName == null) {
            currentEvidenceCaseName = new JTextField();
            Font font = new Font(currentEvidenceCaseName.getFont().getName(), Font.PLAIN,
                                 currentEvidenceCaseName.getFont().getSize() + 2);
            currentEvidenceCaseName.setFont(font);
            currentEvidenceCaseName.setBackground(GUIColors.Inference.EVIDENCE_CASES_COLORS.getFirst()
                                                                                           .background()
                                                                                           .getColor());
            currentEvidenceCaseName.setForeground(GUIColors.Inference.EVIDENCE_CASES_COLORS.getFirst()
                                                                                           .foreground()
                                                                                           .getColor());
            currentEvidenceCaseName.setText("   " + stringDatabase.getString("CaseNumber") + " 0   ");
            currentEvidenceCaseName.setMaximumSize(currentEvidenceCaseName.getPreferredSize());
            currentEvidenceCaseName.setHorizontalAlignment(SwingConstants.CENTER);
            currentEvidenceCaseName.setEditable(false);
        }
        return currentEvidenceCaseName;
    }
    
    /**
     * This method sets the foreground and the background color to be shown in the
     * foreground field
     *
     * @param currentCase The number of the current case.
     */
    public void setCurrentEvidenceCaseName(int currentCase) {
        if (currentCase < 10) {
            currentEvidenceCaseName
                    .setText("   " + stringDatabase.getString("CaseNumber") + " " + currentCase + "   ");
        } else {
            currentEvidenceCaseName.setText(stringDatabase.getString("CaseNumber") + " " + currentCase);
        }
        
        GUIColors.Inference.EvidenceCaseColor evidenceCaseColor = GUIColors.Inference.EVIDENCE_CASES_COLORS
                .get(currentCase % GUIColors.Inference.EVIDENCE_CASES_COLORS.size());
        
        currentEvidenceCaseName.setBackground(evidenceCaseColor.background().getColor());
        currentEvidenceCaseName.setForeground(evidenceCaseColor.foreground().getColor());

    }
    
    /**
     * This method initializes goToNextEvidenceCaseButton.
     *
     * @return a Go To Next Evidence Case button.
     */
    private JButton getGoToNextEvidenceCaseButton() {
        if (goToNextEvidenceCaseButton == null) {
            goToNextEvidenceCaseButton = new JButton();
            goToNextEvidenceCaseButton.setIcon(IconBind.GO_TO_NEXT_EVIDENCE_CASE_ENABLED.icon());
            goToNextEvidenceCaseButton.setFocusable(false);
            goToNextEvidenceCaseButton.setActionCommand(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE.getCommandName());
            goToNextEvidenceCaseButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
            goToNextEvidenceCaseButton.addActionListener(listener);
            goToNextEvidenceCaseButton.addMouseMotionListener(this);
        }
        return goToNextEvidenceCaseButton;
    }
    
    /**
     * This method initializes goToLastEvidenceCaseButton.
     *
     * @return a Go To Last Evidence Case button.
     */
    private JButton getGoToLastEvidenceCaseButton() {
        if (goToLastEvidenceCaseButton == null) {
            goToLastEvidenceCaseButton = new JButton();
            goToLastEvidenceCaseButton.setIcon(IconBind.GO_TO_LAST_EVIDENCE_CASE_ENABLED.icon());
            goToLastEvidenceCaseButton.setFocusable(false);
            goToLastEvidenceCaseButton.setActionCommand(ActionCommands.GO_TO_LAST_EVIDENCE_CASE.getCommandName());
            goToLastEvidenceCaseButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_LAST_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
            goToLastEvidenceCaseButton.addActionListener(listener);
            goToLastEvidenceCaseButton.addMouseMotionListener(this);
        }
        return goToLastEvidenceCaseButton;
    }
    
    /**
     * This method initializes clearOutAllEvidenceCasesButton.
     *
     * @return a Clear Out All Evidence Cases button.
     */
    private JButton getClearOutAllEvidenceCasesButton() {
        if (clearOutAllEvidenceCasesButton == null) {
            clearOutAllEvidenceCasesButton = new JButton();
            clearOutAllEvidenceCasesButton
                    .setIcon(IconBind.CLEAR_OUT_ALL_EVIDENCE_CASES_ENABLED.icon());
            clearOutAllEvidenceCasesButton.setFocusable(false);
            clearOutAllEvidenceCasesButton.setActionCommand(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES.getCommandName());
            clearOutAllEvidenceCasesButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES + STRING_TOOLTIP_SUFFIX));
            clearOutAllEvidenceCasesButton.addActionListener(listener);
            clearOutAllEvidenceCasesButton.addMouseMotionListener(this);
        }
        return clearOutAllEvidenceCasesButton;
    }
    
    /**
     * This method initialises propagateEvidenceButton.
     *
     * @return a Propagate evidence button.
     */
    private JButton getPropagateEvidenceButton() {
        if (propagateEvidenceButton == null) {
            propagateEvidenceButton = new JButton();
            propagateEvidenceButton.setIcon(IconBind.PROPAGATE_EVIDENCE_ENABLED.icon());
            propagateEvidenceButton.setFocusable(false);
            propagateEvidenceButton.setActionCommand(ActionCommands.PROPAGATE_EVIDENCE.getCommandName());
            propagateEvidenceButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.PROPAGATE_EVIDENCE + STRING_TOOLTIP_SUFFIX));
            propagateEvidenceButton.addActionListener(listener);
            propagateEvidenceButton.addMouseMotionListener(this);
        }
        return propagateEvidenceButton;
    }
    
    /**
     * Returns the component that correspond to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    @Override protected JComponent getJComponentActionCommand(String actionCommand) {
        return switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.PROPAGATE_EVIDENCE -> propagateEvidenceButton;
            case ActionCommands.CREATE_NEW_EVIDENCE_CASE -> createNewEvidenceCaseButton;
            case ActionCommands.GO_TO_FIRST_EVIDENCE_CASE -> goToFirstEvidenceCaseButton;
            case ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE -> goToPreviousEvidenceCaseButton;
            case ActionCommands.GO_TO_NEXT_EVIDENCE_CASE -> goToNextEvidenceCaseButton;
            case ActionCommands.GO_TO_LAST_EVIDENCE_CASE -> goToLastEvidenceCaseButton;
            case ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES -> clearOutAllEvidenceCasesButton;
            case null, default -> null;
        };
    }
    
    /**
     * This method establishes the expansion threshold to be shown in the
     * ComboBox.
     *
     * @param expansionThreshold The expansion threshold to be shown in the
     *                           ComboBox.
     */
    public void setExpansionThreshold(double expansionThreshold) {
        expansionThresholdComboBox.setExpansionThreshold(expansionThreshold);
    }
    
    /**
     * This method adds the button 'Propagate Now' on this tool bar. It is
     * placed at the most left position.
     */
    public void addPropagateNowButton() {
        removeAll();
        propagateEvidenceButton = null;
        add(getPropagateEvidenceButton());
        addSeparator();
        add(getCreateNewEvidenceCaseButton());
        add(getClearOutAllEvidenceCasesButton());
        addSeparator();
        add(getGoToFirstEvidenceCaseButton());
        add(getGoToPreviousEvidenceCaseButton());
        add(getCurrentEvidenceCaseName());
        add(getGoToNextEvidenceCaseButton());
        add(getGoToLastEvidenceCaseButton());
        addSeparator();
        add(getExpansionThresholdLabel());
        add(getExpansionThresholdComboBox());
        add(Box.createHorizontalGlue());
    }
    
    /**
     * This method removes the button 'Propagate Now' from this tool bar.
     */
    public void removePropagateNowButton() {
        removeAll();
        add(getCreateNewEvidenceCaseButton());
        add(getClearOutAllEvidenceCasesButton());
        addSeparator();
        add(getGoToFirstEvidenceCaseButton());
        add(getGoToPreviousEvidenceCaseButton());
        add(getCurrentEvidenceCaseName());
        add(getGoToNextEvidenceCaseButton());
        add(getGoToLastEvidenceCaseButton());
        addSeparator();
        add(getExpansionThresholdLabel());
        add(getExpansionThresholdComboBox());
        add(Box.createHorizontalGlue());
    }
    
    @Override public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    
    @Override public void mouseMoved(MouseEvent e) {
        if (e.getSource().equals(getCreateNewEvidenceCaseButton())) {
            getCreateNewEvidenceCaseButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.CREATE_NEW_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getGoToFirstEvidenceCaseButton())) {
            getGoToFirstEvidenceCaseButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getGoToPreviousEvidenceCaseButton())) {
            getGoToPreviousEvidenceCaseButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getGoToNextEvidenceCaseButton())) {
            getGoToNextEvidenceCaseButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getGoToLastEvidenceCaseButton())) {
            getGoToLastEvidenceCaseButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.GO_TO_LAST_EVIDENCE_CASE + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getClearOutAllEvidenceCasesButton())) {
            getClearOutAllEvidenceCasesButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getPropagateEvidenceButton())) {
            getPropagateEvidenceButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.PROPAGATE_EVIDENCE + STRING_TOOLTIP_SUFFIX));
        }
    }
}
