/*
 *  Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.util.PropertyNames;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.Window;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Dialog box to set the options of a network.
 *
 * @author jmendoza
 * @version 1.2 jlgozalo new Group layout and semantic errors fixed
 */
public class NetworkPropertiesDialog extends OkCancelDialog implements PropertyNames {
    private static final long serialVersionUID = -8734100506781534551L;
    /**
     * String database
     */
    protected final StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    private ProbNet probNet = null;
    /**
     * Panel to tab the different options.
     */
    private JTabbedPane tabbedPane = null;
    /**
     * Panel that contains the panel where definition fields are. It is used to
     * place the fields at the top of the panel.
     */
    private NetworkDefinitionPanel networkDefinitionPanel = null;
    /**
     * Panel that contains the panel where variables definition fields are. It
     * is used to place the fields at the top of the panel.
     */
    private NetworkVariablesPanel networkVariablesPanel = null;
    /**
     * Panel that contains the panel where a set of other additionalProperties
     * are. It is used to place the fields at the top of the panel.
     */
    private NetworkOtherPropertiesPanel networkOtherPropertiesPanel = null;
    /**
     * Panel containing 'Decision Criteria' only for probNet with chance nodes??????????.
     * It is used to place the fields at the top of the panel.
     */
    private NetworkDecisionCriteriaPanel networkDecisionCriteriaPanel = null;
    /**
     * Panel containing 'Agents' for a probNet.
     * It is used to place the fields at the top of the panel.
     */
    private NetworkAgentsPanel networkAgentsPanel;
    /**
     * Panel containing 'temporal Options' for probNet contains the received
     * constraint type. It is used to place the fields at the top of the panel.
     */
    private NetworkTemporalOptionsPanel networkTemporalOptionsPanel;
    
    /**
     * This method initialises this instance.
     *
     * @param owner    window that owns the dialog.
     * @param probNet  network
     * @param readOnly
     */
    public NetworkPropertiesDialog(Window owner, ProbNet probNet, boolean readOnly) {
        super(owner);
        if (probNet != null) {
            probNet.getPNESupport().setWithUndo(true);
            probNet.getPNESupport().openNewSubEditHistory();
            this.probNet = probNet;
            initialize();
            setName("NetworkPropertiesDialog");
            setLocationRelativeTo(owner);
        }
        initialize();
        this.readOnly = readOnly;
        if (this.readOnly) {
            for (var tab : this.getTabbedPane().getComponents()) {
                ComponentUtilities.findComponents(tab, Component.class, ignored -> true)
                                  .forEach(ComponentUtilities::removeInputsFor);
            }
        }
        setName("NetworkPropertiesDialog");
        setLocationRelativeTo(owner);
    }
    
    /**
     * This method configures the dialog box.
     */
    private void initialize() {
        String title = stringDatabase.getString("NetworkPropertiesDialog.Title");
        if (probNet != null) {
            title += ": " + probNet.getName();
        }
        setTitle(title);
        configureComponentsPanel();
        pack();
    }
    
    /**
     * Sets up the panel where all components, except the buttons of the buttons
     * panel, will be appear.
     */
    private void configureComponentsPanel() {
        getComponentsPanel().add(getTabbedPane());
    }
    
    record Tab(String title, Supplier<Component> component, BooleanSupplier use) {
    }
    
    private final List<Tab> TABS = List.of(
            new Tab("NetworkPropertiesDialog.DefinitionTab", () -> getNetworkDefinitionPanel(), () -> true),
            new Tab("NetworkPropertiesDialog.VariablesTab", () -> getNetworkVariablesPanel(), () -> true),
            new Tab("NetworkPropertiesDialog.DecisionCriteriaTab", () -> getNetworkDecisionCriteriaPanel(), () -> getNetworkDecisionCriteriaPanel().update(probNet)),
            new Tab("NetworkPropertiesDialog.AgentsTab", () -> getNetworkAgentsPanel(), () -> getNetworkAgentsPanel().update(probNet)),
            new Tab("NetworkPropertiesDialog.TemporalOptionsTab", () -> getNetworkTemporalOptionsPanel(), () -> getNetworkTemporalOptionsPanel().update(probNet)),
            new Tab("NetworkPropertiesDialog.OtherPropertiesTab", () -> getNetworkOtherPropertiesPanel(), () -> true)
    );
    
    /**
     * This method initialises tabbedPane.
     *
     * @return a new tabbed pane.
     */
    private JTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            tabbedPane.setName("tabbedPane");
            setOnlyNecesaryTabs();
        }
        return tabbedPane;
    }
    
    private void setOnlyNecesaryTabs() {
        for (int tabIndex = 0; tabIndex < TABS.size(); tabIndex++) {
            Tab tab = TABS.get(tabIndex);
            var shouldBePresent = tab.use.getAsBoolean();
            JTabbedPane jTabbedPane = getTabbedPane();
            var componentIndex = jTabbedPane.indexOfComponent(tab.component.get());
            if (!shouldBePresent) {
                if (componentIndex != -1) {
                    jTabbedPane.remove(componentIndex);
                }
                continue;
            }
            if (componentIndex != -1) {
                continue;
            }
            List<Component> prevComponents = IntStream
                    .range(0, tabIndex)
                    .mapToObj(TABS::get)
                    .map(Tab::component)
                    .map(Supplier::get)
                    .toList();
            var prevComponentIndex = prevComponents
                    .reversed()
                    .stream()
                    .map(jTabbedPane::indexOfComponent)
                    .filter(i -> i != -1)
                    .findFirst()
                    .orElse(-1);
            int insertionIndex = prevComponentIndex+1;
            jTabbedPane.insertTab(StringDatabase.getUniqueInstance()
                                                .getString(tab.title), null, tab.component.get(), null, insertionIndex);
        }
    }
    
    /**
     * Initialising NetworkDecisionCriteriaPanel.
     *
     * @return a new Decision criteria panel.
     */
    private NetworkDecisionCriteriaPanel getNetworkDecisionCriteriaPanel() {
        if (networkDecisionCriteriaPanel == null) {
            networkDecisionCriteriaPanel = new NetworkDecisionCriteriaPanel(this, probNet);
            networkDecisionCriteriaPanel.setName("networkDecisionCriteriaPanel");
        }
        return networkDecisionCriteriaPanel;
    }
    
    /**
     * Initialising NetworkAgentsPanel.
     *
     * @return a new Agents panel.
     */
    private NetworkAgentsPanel getNetworkAgentsPanel() {
        if (networkAgentsPanel == null) {
            networkAgentsPanel = new NetworkAgentsPanel(probNet);
            networkAgentsPanel.setName("networkAgentsPanel");
        }
        return networkAgentsPanel;
    }
    
    /**
     * Initialising NetworkTemporalOptionsPanel.
     *
     * @return a new Temporal options panel.
     */
    private NetworkTemporalOptionsPanel getNetworkTemporalOptionsPanel() {
        if (networkTemporalOptionsPanel == null) {
            networkTemporalOptionsPanel = new NetworkTemporalOptionsPanel(probNet);
            networkTemporalOptionsPanel.setName("networkTemporalOptionsPanel");
        }
        return networkTemporalOptionsPanel;
    }
    
    /**
     * This method initialises networkDefinitionPanel.
     *
     * @return a new definition panel.
     */
    private NetworkDefinitionPanel getNetworkDefinitionPanel() {
        if (networkDefinitionPanel == null) {
            networkDefinitionPanel = new NetworkDefinitionPanel(this, probNet);
            networkDefinitionPanel.setName("networkDefinitionPanel");
        }
        return networkDefinitionPanel;
    }
    
    /**
     * This method initialises networkVariablesPanel.
     *
     * @return a new variables definition panel.
     */
    private NetworkVariablesPanel getNetworkVariablesPanel() {
        if (networkVariablesPanel == null) {
            networkVariablesPanel = new NetworkVariablesPanel(probNet);
            networkVariablesPanel.setName("networkVariablesPanel");
        }
        return networkVariablesPanel;
    }
    
    /**
     * This method initialises networkOtherPropertiesPanel.
     *
     * @return a new other additionalProperties panel.
     */
    private NetworkOtherPropertiesPanel getNetworkOtherPropertiesPanel() {
        if (networkOtherPropertiesPanel == null) {
            networkOtherPropertiesPanel = new NetworkOtherPropertiesPanel();
            networkOtherPropertiesPanel.setName("networkOtherPropertiesPanel");
            if (probNet != null) {
                networkOtherPropertiesPanel.setProbNetProperties(probNet);
            }
        }
        return networkOtherPropertiesPanel;
    }
    
    /**
     * This method carries out the actions when the user press the Ok button
     * before hide the dialog.
     *
     * @return true always
     */
    @Override protected boolean doOkClickBeforeHide() {
        probNet.getPNESupport().closeSubEditHistory();
        return NetworkDefinitionPanel.checkName();
    }
    
    // ESCA-JAVA0025: allows an empty method to override another one
    
    /**
     * This method carries out the actions when the user press the Cancel button
     * before hide the dialog.
     */
    @Override protected void doCancelClickBeforeHide() {
        probNet.getPNESupport().cancelLastSubEditHistory();
        //Should cancel instead
    }
    
    /**
     * This method shows the dialog and requests the user the network
     * additionalProperties.
     *
     * @return OK_BUTTON if the user has pressed the 'OK' button or
     * CANCEL_BUTTON if the user has pressed the 'Cancel' button.
     */
    public ChosenOption showProperties() {
        setVisible(true);
        return getSelectedOption();
    }
    
    /**
     * Returns the probNet.
     *
     * @return the probNet.
     */
    public ProbNet getProbNet() {
        return probNet;
    }
    
    /**
     * Updates the features of a probNet when it´s set in the definition panel
     *
     */
    public void update() {
        setOnlyNecesaryTabs();
    }
    
    private final boolean readOnly;
}