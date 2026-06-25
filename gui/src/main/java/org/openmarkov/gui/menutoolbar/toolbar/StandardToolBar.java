/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.toolbar;

import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.ZoomMenuToolBar;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * This class implements the standard toolbar of the application.
 *
 * @author jmendoza
 * @version 1.2 20100408 jlgozalo Change the order of ZoomIn and ZoomOut buttons
 */
public class StandardToolBar extends ToolBarBasic implements ZoomMenuToolBar, MouseMotionListener {
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 6583864684969808896L;
    /**
     * Button to create a new network.
     */
    private JButton newNetworkButton = null;
    /**
     * Button to open a network.
     */
    private JButton openNetworkButton = null;
    /**
     * Button to open a network.
     */
    private JButton saveNetworkButton = null;
    /**
     * Button to open a network.
     */
    private JButton closeNetworkButton = null;
    /**
     * Button to zoomManager in a network.
     */
    private JButton zoomInButton = null;
    /**
     * Button to zoomManager out a network.
     */
    private JButton zoomOutButton = null;
    /**
     * Combobox to select zoomManager values.
     */
    private ZoomComboBox zoomComboBox = null;
    /**
     * Button to switch to Inference mode.
     */
    private JToggleButton workingModeButton = null;
    // 25/08/2019
    /**
     * Button perform a Monte Carlo simulation. Currently only it works with DESNet
     */
    private JToggleButton monteCarloSimulationButton = null;
    //
    /**
     * Button to show optimal strategy.
     */
    private JButton showOptimalStrategyButton = null;
    /**
     * Button to open a decision tree panel.
     */
    private JButton decisionTreeButton = null;
    /**
     * Button to perform a cost-effectiveness analysis.
     */
    private JButton costEffectivenessButton = null;
    /**
     * Button to perform a sensitivity analysis.
     */
    private JButton sensAnalysisButton = null;
    
    /**
     * This method initialises this instance.
     *
     * @param newListener object that listens to the buttons events.
     */
    public StandardToolBar(ActionListener newListener) {
        super(newListener);
        initialize();
    }
    
    /**
     * This method configures the toolbar.
     */
    private void initialize() {
        add(getNewNetworkButton());
        add(getOpenNetworkButton());
        add(getSaveNetworkButton());
        addSeparator();
        add(getZoomOutButton());
        add(getZoomComboBox());
        add(getZoomInButton());
        addSeparator();
        add(getWorkingModeButton());
        add(getShowOptimalStrategyButton());
        add(getDecisionTreeButton());
        add(getSensAnalysisButton());
        add(getCostEffectivenessButton());
        add(getMonteCarloSimulationButton());
        add(Box.createHorizontalGlue());
    }
    
    /**
     * This method initialises newNetworkButton.
     *
     * @return a new button.
     */
    private JButton getNewNetworkButton() {
        if (newNetworkButton == null) {
            newNetworkButton = new JButton();
            newNetworkButton.setIcon(IconBind.NEW_ENABLED.icon());
            newNetworkButton.setActionCommand(ActionCommands.NEW_NETWORK.getCommandName());
            newNetworkButton.setName("ButtonOpenNewNetwork");
            newNetworkButton.setFocusable(false);
            newNetworkButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.NEW_NETWORK + STRING_TOOLTIP_SUFFIX));
            newNetworkButton.addActionListener(listener);
            newNetworkButton.addMouseMotionListener(this);
        }
        return newNetworkButton;
    }
    
    /**
     * This method initialises openNetworkButton.
     *
     * @return a new button.
     */
    private JButton getOpenNetworkButton() {
        if (openNetworkButton == null) {
            openNetworkButton = new JButton();
            openNetworkButton.setIcon(IconBind.OPEN_ENABLED.icon());
            openNetworkButton.setActionCommand(ActionCommands.OPEN_NETWORK.getCommandName());
            openNetworkButton.setFocusable(false);
            openNetworkButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.OPEN_NETWORK + STRING_TOOLTIP_SUFFIX));
            openNetworkButton.addActionListener(listener);
            openNetworkButton.addMouseMotionListener(this);
        }
        return openNetworkButton;
    }
    
    /**
     * This method initialises saveNetworkButton.
     *
     * @return a new button.
     */
    private JButton getSaveNetworkButton() {
        if (saveNetworkButton == null) {
            saveNetworkButton = new JButton();
            saveNetworkButton.setIcon(IconBind.SAVE_ENABLED.icon());
            saveNetworkButton.setActionCommand(ActionCommands.SAVE_NETWORK.getCommandName());
            saveNetworkButton.setFocusable(false);
            saveNetworkButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.SAVE_NETWORK + STRING_TOOLTIP_SUFFIX));
            saveNetworkButton.addActionListener(listener);
            saveNetworkButton.addMouseMotionListener(this);
        }
        return saveNetworkButton;
    }
    
    /**
     * This method initialises zoomInButton.
     *
     * @return a new button.
     */
    private JButton getZoomInButton() {
        if (zoomInButton == null) {
            zoomInButton = new JButton();
            zoomInButton.setIcon(IconBind.ZOOM_IN_ENABLED.icon());
            zoomInButton.setActionCommand(ActionCommands.ZOOM_IN.getCommandName());
            zoomInButton.setFocusable(false);
            zoomInButton.setToolTipText(stringDatabase.getString(ActionCommands.ZOOM_IN + STRING_TOOLTIP_SUFFIX));
            zoomInButton.addActionListener(listener);
            zoomInButton.addMouseMotionListener(this);
        }
        return zoomInButton;
    }
    
    /**
     * This method initialises zoomOutButton.
     *
     * @return a new button.
     */
    private JButton getZoomOutButton() {
        if (zoomOutButton == null) {
            zoomOutButton = new JButton();
            zoomOutButton.setIcon(IconBind.ZOOM_OUT_ENABLED.icon());
            zoomOutButton.setActionCommand(ActionCommands.ZOOM_OUT.getCommandName());
            zoomOutButton.setFocusable(false);
            zoomOutButton.setToolTipText(stringDatabase.getString(ActionCommands.ZOOM_OUT + STRING_TOOLTIP_SUFFIX));
            zoomOutButton.addActionListener(listener);
            zoomOutButton.addMouseMotionListener(this);
        }
        return zoomOutButton;
    }
    
    /**
     * This method initialises zoomComboBox.
     *
     * @return a new zoomManager combobox.
     */
    public ZoomComboBox getZoomComboBox() {
        if (zoomComboBox == null) {
            zoomComboBox = new ZoomComboBox(listener);
            zoomComboBox.setFocusable(false);
        }
        return zoomComboBox;
    }
    
    /**
     * This method sets the value of the combobox.
     *
     * @param value new value of zoomManager.
     */
    @Override public void setZoom(double value) {
        zoomComboBox.setZoom(value);
    }
    
    /**
     * This method initialises workingModeButton.
     *
     * @return a working mode button.
     */
    private JToggleButton getWorkingModeButton() {
        if (workingModeButton == null) {
            workingModeButton = new JToggleButton();
            workingModeButton.setIcon(IconBind.INFERENCE_MODE_ENABLED.icon());
            workingModeButton.setFocusable(false);
            workingModeButton.setActionCommand(ActionCommands.CHANGE_WORKING_MODE.getCommandName());
            workingModeButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.CHANGE_WORKING_MODE + STRING_TOOLTIP_SUFFIX));
            workingModeButton.addActionListener(listener);
            workingModeButton.addMouseMotionListener(this);
        }
        return workingModeButton;
    }
    
    /**
     * This method initialises showOptimalStrategyButton.
     *
     * @return a new button.
     */
    public JButton getShowOptimalStrategyButton() {
        if (showOptimalStrategyButton == null) {
            showOptimalStrategyButton = new JButton();
            showOptimalStrategyButton.setIcon(IconBind.OPTIMAL_STRATEGY.icon());
            showOptimalStrategyButton.setActionCommand(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY.getCommandName());
            showOptimalStrategyButton.setFocusable(false);
            showOptimalStrategyButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY + STRING_TOOLTIP_SUFFIX));
            showOptimalStrategyButton.addActionListener(listener);
            showOptimalStrategyButton.addMouseMotionListener(this);
        }
        return showOptimalStrategyButton;
    }
    
    /**
     * This method initialises decisionTreeButton.
     *
     * @return a new button.
     */
    public JButton getDecisionTreeButton() {
        if (decisionTreeButton == null) {
            decisionTreeButton = new JButton();
            decisionTreeButton.setIcon(IconBind.DECISION_TREE.icon());
            decisionTreeButton.setActionCommand(ActionCommands.DECISION_TREE.getCommandName());
            decisionTreeButton.setFocusable(false);
            decisionTreeButton
                    .setToolTipText(stringDatabase.getString(ActionCommands.DECISION_TREE + STRING_TOOLTIP_SUFFIX));
            decisionTreeButton.addActionListener(listener);
            decisionTreeButton.addMouseMotionListener(this);
        }
        return decisionTreeButton;
    }
    
    /**
     * This method initialises costEffectivenessButton.
     *
     * @return a new button.
     */
    public JButton getCostEffectivenessButton() {
        if (costEffectivenessButton == null) {
            costEffectivenessButton = new JButton();
            costEffectivenessButton.setIcon(IconBind.COST_EFFECTIVENESS.icon());
            costEffectivenessButton.setActionCommand(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC.getCommandName());
            costEffectivenessButton.setFocusable(false);
            costEffectivenessButton.setToolTipText(
                    stringDatabase.getString("Tools.CostEffectiveness") + ": " + stringDatabase
                            .getString(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC.toString()));
            costEffectivenessButton.addActionListener(listener);
            costEffectivenessButton.addMouseMotionListener(this);
        }
        //costEffectivenessButton.setEnabled(false);
        return costEffectivenessButton;
    }
    
    // 21/08/2019
    /**
     * This method initialises simulateDESNetBUtton.
     *
     * @return a
     */
    private JToggleButton getMonteCarloSimulationButton() {
        if (monteCarloSimulationButton == null) {
            monteCarloSimulationButton = new JToggleButton();
            monteCarloSimulationButton.setIcon(IconBind.SIMULATION_ENABLED.icon());
            monteCarloSimulationButton.setFocusable(false);
            monteCarloSimulationButton.setActionCommand(ActionCommands.MC_SIMULATE_NETWORK.getCommandName());
            monteCarloSimulationButton.setToolTipText(
                    stringDatabase.getString(ActionCommands.MC_SIMULATE_NETWORK + STRING_TOOLTIP_SUFFIX));
            monteCarloSimulationButton.addActionListener(listener);
            monteCarloSimulationButton.addMouseMotionListener(this);
        }
        return monteCarloSimulationButton;
    }
    
    /**
     * This method initialises costEffectivenessButton.
     *
     * @return a new button.
     */
    public JButton getSensAnalysisButton() {
        if (sensAnalysisButton == null) {
            sensAnalysisButton = new JButton();
            sensAnalysisButton.setIcon(IconBind.SENS_ANALYSIS.icon());
            sensAnalysisButton.setActionCommand(ActionCommands.SENSITIVITY_ANALYSIS.getCommandName());
            sensAnalysisButton.setFocusable(false);
            sensAnalysisButton.setToolTipText(stringDatabase.getString("SensitivityAnalysis.ToolTip"));
            sensAnalysisButton.addActionListener(listener);
            sensAnalysisButton.addMouseMotionListener(this);
            sensAnalysisButton.setEnabled(false);
        }
        return sensAnalysisButton;
    }
    
    /**
     * This method sets the button for switching between Edition/inference to
     * the pertinent value (pressed or not)
     *
     * @param workingMode the working mode of the currently selected
     *                    NetworkEditorPanel. Depending on this value, the button will be set
     *                    pressed or not.
     */
    public void changeWorkingModeButton(NetworkEditorPanel.WorkingMode workingMode) {
        workingModeButton.setSelected(workingMode == NetworkEditorPanel.WorkingMode.INFERENCE);
    }
    
    // 2019/09/09
    /**
     * Select/unselect MonteCarloButon
     * @param select - true if moteCarloButton will be selected0
     */
    public void selectMonteCarloButton(boolean select) {
        monteCarloSimulationButton.setSelected(false);
    }
    
    /**
     * Returns the component that corresponds to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    @Override protected JComponent getJComponentActionCommand(String actionCommand) {
        return switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.NEW_NETWORK -> newNetworkButton;
            case ActionCommands.OPEN_NETWORK -> openNetworkButton;
            case ActionCommands.SAVE_NETWORK -> saveNetworkButton;
            case ActionCommands.ZOOM_IN -> zoomInButton;
            case ActionCommands.ZOOM_OUT -> zoomOutButton;
            case ActionCommands.ZOOM -> zoomComboBox;
            case ActionCommands.DECISION_TREE -> decisionTreeButton;
            case ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC -> costEffectivenessButton;
            case ActionCommands.SENSITIVITY_ANALYSIS -> sensAnalysisButton;
            case ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY -> showOptimalStrategyButton;
            case ActionCommands.CHANGE_TO_EDITION_MODE, ActionCommands.CHANGE_TO_INFERENCE_MODE,
                 ActionCommands.CHANGE_WORKING_MODE -> workingModeButton;
            case ActionCommands.MC_SIMULATE_NETWORK -> monteCarloSimulationButton;
            case null, default -> null;
        };
    }
    
    @Override public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    
    @Override public void mouseMoved(MouseEvent e) {
        if (e.getSource().equals(getNewNetworkButton())) {
            getNewNetworkButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.NEW_NETWORK + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getOpenNetworkButton())) {
            getOpenNetworkButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.OPEN_NETWORK + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getSaveNetworkButton())) {
            getSaveNetworkButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.SAVE_NETWORK + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getZoomInButton())) {
            getZoomInButton().setToolTipText(stringDatabase.getString(ActionCommands.ZOOM_IN + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getZoomOutButton())) {
            getZoomOutButton()
                    .setToolTipText(stringDatabase.getString(ActionCommands.ZOOM_OUT + STRING_TOOLTIP_SUFFIX));
        } else if (e.getSource().equals(getWorkingModeButton())) {
            getWorkingModeButton().setToolTipText(
                    stringDatabase.getString(ActionCommands.CHANGE_WORKING_MODE + STRING_TOOLTIP_SUFFIX));
        }
    }
    
}
