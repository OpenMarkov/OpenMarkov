/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.ChangeNetworkTypeEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.NoEventNodes;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.gui.graphic.*;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.localize.MenuLocalizer;
import org.openmarkov.gui.menutoolbar.common.*;
import org.openmarkov.gui.window.decisiontree.DecisionTreeEditor;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.gui.window.edition.ZoomManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class assists to the class MainPanel to manage the menus and toolbars.
 *
 * @author jmendoza
 * @version 1.2.1 - cmyago - 20/10/2022; 09/11/2022 - disabling "Add Finding" for temporal nodes which are not the first in the temporal sequence and implementing "Temporal evolution by criterion"
 */
public class MainPanelMenuAssistant extends MenuAssistant implements PNEditListener, SelectionListener {
    /**
     * Composed action command that contains all the save and close actions
     * (except save).
     */
    public static final ActionCommands[] FILING_ACTION_COMMANDS = {ActionCommands.SAVE_OPEN_NETWORK,
            ActionCommands.SAVEAS_NETWORK, ActionCommands.CLOSE_TAB, ActionCommands.LOAD_EVIDENCE,
            ActionCommands.SAVE_EVIDENCE, ActionCommands.NETWORK_PROPERTIES};
    /**
     * Composed action command that contains all the edition actions (except
     * undo and redo).
     */
    public static final ActionCommands[] EDITING_ACTION_COMMANDS = {ActionCommands.OBJECT_SELECTION,
            ActionCommands.CHANCE_CREATION, ActionCommands.DECISION_CREATION, ActionCommands.UTILITY_CREATION,
            ActionCommands.EVENT_CREATION, ActionCommands.LINK_CREATION};
    /**
     * Composed action command that contains inference actions.
     */
    public static final ActionCommands[] INFERENCE_ACTION_COMMANDS = {ActionCommands.CREATE_NEW_EVIDENCE_CASE,
            ActionCommands.GO_TO_FIRST_EVIDENCE_CASE, ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE,
            ActionCommands.GO_TO_NEXT_EVIDENCE_CASE, ActionCommands.GO_TO_LAST_EVIDENCE_CASE,
            ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES, ActionCommands.PROPAGATE_EVIDENCE};
    
    /**
     * Composed action command that contains all the viewing actions (except
     * view message window).
     */
    public static final ActionCommands[] VIEWING_ACTION_COMMANDS = { ActionCommands.ZOOM, ActionCommands.ZOOM_IN,
            ActionCommands.ZOOM_OUT, ActionCommands.NODES };
    
    /**
     * Menus and toolbar that manage zoomManager.
     */
    private final ZoomMenuToolBar[] zoomMenus;
    /**
     * MainPanel from which this object depends.
     */
    private final MainPanel mainPanel;
    /**
     * networkPanel that is currently selected.
     */
    private NetworkEditorPanel currentNetworkEditorPanel = null;
    /**
     * Variable to know if a network was opened from a URL
     */
    private boolean networkOpenedURL = false;
    
    /**
     * Constructor that registers the arrays of menus.
     *
     * @param newBasicMenus array of basic menus and toolbars.
     * @param newZoomMenus  array of zoomManager menus and toolbars.
     * @param mainPanel     MainPanel that creates this MainPanelMenuAssistant.
     */
    public MainPanelMenuAssistant(MenuToolBarBasic[] newBasicMenus, ZoomMenuToolBar[] newZoomMenus,
                                  MainPanel mainPanel) {
        super(newBasicMenus);
        ZoomMenuToolBar[] menus = newZoomMenus;
        if (menus == null) {
            menus = new ZoomMenuToolBar[0];
        }
        zoomMenus = menus;
        this.mainPanel = mainPanel;
    }
    
    /**
     * Sets the zoomManager value on the menus and toolbars.
     *
     * @param value new zoomManager value.
     */
    public void setZoom(double value) {
        for (ZoomMenuToolBar menu : zoomMenus) {
            menu.setZoom(value);
        }
        setOptionEnabled(ActionCommands.ZOOM_OUT, value != ZoomManager.MIN_VALUE);
        setOptionEnabled(ActionCommands.ZOOM_IN, value != ZoomManager.MAX_VALUE);
    }
    
    /**
     * Enables the menu items and toolbar buttons when all networks are closed.
     */
    public void updateOptionsAllNetworkClosed() {
        setOptionEnabled(FILING_ACTION_COMMANDS, false);
        setOptionEnabled(ActionCommands.SAVE_NETWORK, false);
        setOptionEnabled(EDITING_ACTION_COMMANDS, false);
        setOptionEnabled(INFERENCE_ACTION_COMMANDS, false);
        setOptionEnabled(ActionCommands.SELECT_ALL, false);
        setOptionEnabled(ActionCommands.CHANGE_WORKING_MODE, false);
        setOptionEnabled(ActionCommands.CHANGE_TO_INFERENCE_MODE, false);
        setOptionEnabled(ActionCommands.CHANGE_TO_EDITION_MODE, false);
        setOptionEnabled(ActionCommands.EDITION_MODE_PREFIX, false);
        setOptionEnabled(ActionCommands.EVENT_CREATION, false);
        setOptionEnabled(ActionCommands.MC_SIMULATE_NETWORK, false);
        
        setOptionEnabled(ActionCommands.NODE_EXPANSION, false);
        setOptionEnabled(ActionCommands.NODE_CONTRACTION, false);
        setOptionEnabled(ActionCommands.NODE_ADD_FINDING, false);
        setOptionEnabled(ActionCommands.NODE_REMOVE_FINDING, false);
        setOptionEnabled(ActionCommands.NODE_REMOVE_ALL_FINDINGS, false);
        setOptionEnabled(ActionCommands.UNDO, false);
        setOptionEnabled(ActionCommands.REDO, false);
        setOptionEnabled(ActionCommands.CLIPBOARD_CUT, false);
        setOptionEnabled(ActionCommands.CLIPBOARD_COPY, false);
        setOptionEnabled(ActionCommands.CLIPBOARD_PASTE, false);
        setOptionEnabled(ActionCommands.OBJECT_REMOVAL, false);
        setOptionEnabled(ActionCommands.NODE_PROPERTIES, false);
        setOptionEnabled(ActionCommands.EDIT_POTENTIAL, false);
        setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC, false);
        setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_SENSITIVITY, false);
        setOptionEnabled(ActionCommands.SENSITIVITY_ANALYSIS, false);
        setOptionEnabled(ActionCommands.LINK_PROPERTIES, false);
        setOptionEnabled(ActionCommands.PROPAGATION_OPTIONS, false);
        setOptionEnabled(ActionCommands.INFERENCE_OPTIONS, false);
        setOptionEnabled(ActionCommands.TEMPORAL_OPTIONS, false);
        
        setOptionEnabled(ActionCommands.ZOOM_IN, false);
        setOptionEnabled(ActionCommands.ZOOM_OUT, false);
        setOptionEnabled(ActionCommands.ZOOM, false);
        
        setOptionEnabled(ActionCommands.DECISION_IMPOSE_POLICY, false);
        setOptionEnabled(ActionCommands.DECISION_EDIT_POLICY, false);
        setOptionEnabled(ActionCommands.DECISION_REMOVE_POLICY, false);
        setOptionEnabled(ActionCommands.DECISION_SHOW_EXPECTED_UTILITY, false);
        setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_POLICY, false);
        setOptionEnabled(ActionCommands.TEMPORAL_EVOLUTION_ACTION, false);
        setOptionEnabled(ActionCommands.TEMPORAL_EVOLUTION_BY_CRITERION, false);
        setOptionEnabled(ActionCommands.DECISION_TREE, false);
        setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY, false);
        setOptionEnabled(ActionCommands.NEXT_SLICE_NODE, false);
        
        //updateInferenceButtons();
    }
    
    /**
     * Disables the menu items and toolbar buttons when any network is opened.
     */
    public void updateOptionsNewNetworkOpen() {
        NetworkEditorPanel.WorkingMode workingMode = NetworkEditorPanel.WorkingMode.EDITION;
        NetworkEditorPanel currentNetworkEditorPanel = getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel != null) {
            workingMode = currentNetworkEditorPanel.getWorkingMode();
            boolean enable = currentNetworkEditorPanel.getProbNet().getNetworkType() instanceof InfluenceDiagramType
                    || currentNetworkEditorPanel.getProbNet()
                                                .getNetworkType() instanceof MIDType || currentNetworkEditorPanel
                    .getProbNet().getNetworkType() instanceof DecisionAnalysisNetworkType;
            setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC, enable);
            setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_SENSITIVITY, enable);
        }
        setOptionEnabled(FILING_ACTION_COMMANDS, true);
        setOptionEnabled(ActionCommands.ZOOM, true);
        if (workingMode == NetworkEditorPanel.WorkingMode.EDITION) {
            setOptionEnabled(EDITING_ACTION_COMMANDS, true);
            setOptionEnabled(INFERENCE_ACTION_COMMANDS, false);
        }
        setOptionEnabled(ActionCommands.CHANGE_WORKING_MODE, getEnableWorkingModeButton());
        setOptionEnabled(ActionCommands.PROPAGATION_OPTIONS, true);
        

        
        setOptionEnabled(ActionCommands.TEMPORAL_EVOLUTION_BY_CRITERION, false);
        setOptionEnabled(ActionCommands.NEXT_SLICE_NODE, false);
        

        checkInferenceOptions();
    }
    
    private void checkInferenceOptions() {
        updateInferenceButtons();
        NetworkEditorPanel currentNetworkEditorPanel = getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel == null) {
            setOptionEnabled(ActionCommands.INFERENCE_OPTIONS, false);
            return;
        }
        if (currentNetworkEditorPanel.getProbNet()
                                     .hasConstraintOfClass(OnlyChanceNodes.class) && currentNetworkEditorPanel.getProbNet()
                                                                                                              .hasConstraintOfClass(OnlyAtemporalVariables.class)) {
            setOptionEnabled(ActionCommands.INFERENCE_OPTIONS, false);
        } else {
            setOptionEnabled(ActionCommands.INFERENCE_OPTIONS, !currentNetworkEditorPanel.getProbNet()
                                                                                         .hasConstraintOfClass(OnlyAtemporalVariables.class) || (
                    currentNetworkEditorPanel.getProbNet().getDecisionCriteria() != null
                            && currentNetworkEditorPanel.getProbNet().getDecisionCriteria().size() > 1
            ));
        }
    }
    
    private boolean getEnableWorkingModeButton() {
        NetworkEditorPanel currentNetworkEditorPanel = getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel == null) return false;
        NetworkType networkType = currentNetworkEditorPanel.getProbNet().getNetworkType();
        return networkType instanceof InfluenceDiagramType || networkType instanceof BayesianNetworkType;
    }
    
    public void updateInferenceButtons() {
        NetworkEditorPanel currentNetworkEditorPanel = getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel == null) {
            setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC, false);
            setOptionEnabled(ActionCommands.SENSITIVITY_ANALYSIS, false);
            return;
        }
        ProbNet probNet = currentNetworkEditorPanel.getProbNet();
        
        setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC,
                         probNet.getDecisionCriteria() != null && probNet.getDecisionCriteria().size() > 1);
        
        boolean hasUncertainty = false;
        for (Node node : probNet.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                try {
                    if (potential instanceof SameAsPrevious) {
                        Potential originalPotential = ((SameAsPrevious) potential)
                                .getOriginalPotential(probNet);
                        if (originalPotential.isUncertain()) {
                            hasUncertainty = true;
                        }
                    } else {
                        if (potential.isUncertain()) {
                            hasUncertainty = true;
                        }
                    }
                } catch (NotSupportedOperationException e) {
                    throw new UnreachableException(e);
                }
            }
        }
        
        setOptionEnabled(ActionCommands.SENSITIVITY_ANALYSIS, hasUncertainty);
    }
    
    /**
     * Activates the corresponding options when a network has been modified.
     *
     * @param canRedo the can redo
     * @param canUndo the can undo
     */
    public void updateOptionsNetworkModified(boolean canUndo, boolean canRedo) {
        // updateUndoRedo(basicUndoManager);
        // changed by mpalacios
        checkInferenceOptions();
        updateUndoRedo(canUndo, canRedo);
        // If the network has been opened from a URL the save button has to remain disabled
        setOptionEnabled(ActionCommands.SAVE_NETWORK, !networkOpenedURL);
    }
    
    /**
     * Activates the corresponding options when a network has been saved.
     */
    public void updateOptionsNetworkSaved() {
        setOptionEnabled(ActionCommands.SAVE_NETWORK, false);
    }
    
    /**
     * Activates the options byTitle or byName.
     *
     * @param byTitleActive if true, the option 'byTitle' will be activated; if
     *                      false, the option 'byName' will be activated.
     */
    public void setByTitle(boolean byTitleActive) {
        if (byTitleActive) {
            setOptionSelected(ActionCommands.BYTITLE_NODES, true);
        } else {
            setOptionSelected(ActionCommands.BYNAME_NODES, true);
        }
    }
    
    /**
     * It is called when a network has been modified if new network do not have
     * OnlyOneAgentConstraints that means it is multiagent, so network is
     * initialized with two arbitrary agents
     *
     * @param networkPanel the network panel
     */
    public void updateNetworkAgents(NetworkEditorPanel networkPanel) {
        NetworkEditorPanel currentNetworkEditorPanel = networkPanel;
        if (currentNetworkEditorPanel.getProbNet().isMultiagent()) {
            ArrayList<StringWithProperties> agents = new ArrayList<StringWithProperties>();
            agents.add(new StringWithProperties(StringDatabase.getUniqueInstance().getString("Network.Agent1")));
            agents.add(new StringWithProperties(StringDatabase.getUniqueInstance().getString("Network.Agent2")));
            currentNetworkEditorPanel.getProbNet().setAgents(agents);
        }
    }
    
    /**
     * Activates the options on the menus and toolbars that depend on the
     * network.
     *
     * @param networkPanel information of the network panel.
     */
    public void updateOptionsNetworkDependent(NetworkEditorPanel networkPanel) {
        int tabCount = mainPanel.getNetworksTabPanel().getTabCount();
        var networkIndex = IntStream.range(0, tabCount)
                                    .filter(tabIndex -> {
                                        Component component = mainPanel.getNetworksTabPanel().getComponentAt(tabIndex);
                                        return component == networkPanel;
                                    })
                                    .findFirst().getAsInt();
        mainPanel.getNetworksTabPanel().setSelectedIndex(networkIndex);
        NetworkEditorPanel currentNetworkEditorPanel = networkPanel;
        ProbNet currentProbNet = currentNetworkEditorPanel.getProbNet();
        NetworkEditorPanel.WorkingMode workingMode = currentNetworkEditorPanel.getWorkingMode();
        if (currentNetworkEditorPanel.getByTitle()) {
            setOptionSelected(ActionCommands.BYTITLE_NODES, true);
        } else {
            setOptionSelected(ActionCommands.BYNAME_NODES, true);
        }
        var isInferenceEnabled = getEnableWorkingModeButton();
        mainPanel.getMainMenu().getSwitchWorkingMode().setText(MenuLocalizer.getLabel(
                switch (workingMode) {
                    case EDITION -> MenuItemNames.EDIT_SWITCH_TO_INFERENCE_MODE_MENUITEM;
                    case INFERENCE -> MenuItemNames.INFERENCE_SWITCH_TO_EDITION_MODE_MENUITEM;
                }));
        setOptionEnabled(ActionCommands.CHANGE_WORKING_MODE, isInferenceEnabled);
        setOptionEnabled(ActionCommands.CHANGE_TO_INFERENCE_MODE, isInferenceEnabled);
        setOptionEnabled(ActionCommands.CHANGE_TO_EDITION_MODE, isInferenceEnabled);
        setOptionEnabled(ActionCommands.OBJECT_SELECTION, false);
        setOptionEnabled(ActionCommands.CHANCE_CREATION, false);
        setOptionEnabled(ActionCommands.DECISION_CREATION, false);
        setOptionEnabled(ActionCommands.UTILITY_CREATION, false);
        setOptionEnabled(ActionCommands.LINK_CREATION, false);
        setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC, false);
        setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_SENSITIVITY, false);
        setOptionEnabled(ActionCommands.DECISION_TREE, false);
        setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY, false);
        switch (workingMode) {
            case EDITION -> {
                setOptionEnabled(ActionCommands.OBJECT_SELECTION, true);
                setOptionEnabled(ActionCommands.CHANCE_CREATION, true);
                setOptionEnabled(ActionCommands.LINK_CREATION, true);
                setOptionEnabled(INFERENCE_ACTION_COMMANDS, false);
                boolean isOnlyChanceNodes = currentProbNet.hasConstraintOfClass(OnlyChanceNodes.class);
                setOptionEnabled(ActionCommands.DECISION_CREATION, !isOnlyChanceNodes);
                setOptionEnabled(ActionCommands.UTILITY_CREATION, !isOnlyChanceNodes);
                setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC, isOnlyChanceNodes);
                setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_SENSITIVITY, isOnlyChanceNodes);
                setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY, !isOnlyChanceNodes);
                setOptionEnabled(ActionCommands.DECISION_TREE, !isOnlyChanceNodes && ((currentProbNet.getNetworkType() instanceof InfluenceDiagramType)
                        || (currentProbNet.getNetworkType() instanceof DecisionAnalysisNetworkType)));
                setOptionEnabled(ActionCommands.EVENT_CREATION, !isOnlyChanceNodes && !currentProbNet.hasConstraintOfClass(NoEventNodes.class));
                boolean canPerformCE = (
                        currentProbNet.getNetworkType() instanceof MIDType || currentProbNet
                                .getNetworkType() instanceof InfluenceDiagramType || currentProbNet
                                .getNetworkType() instanceof DecisionAnalysisNetworkType
                ) && currentProbNet.getDecisionCriteria() != null && currentProbNet.getDecisionCriteria().size() > 1;
                setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC, canPerformCE);
                setOptionEnabled(ActionCommands.COST_EFFECTIVENESS_SENSITIVITY, canPerformCE);
                setOptionEnabled(ActionCommands.MC_SIMULATE_NETWORK, currentProbNet.getNetworkType() instanceof DESNetworkType);
            }
            case INFERENCE -> {
                setOptionEnabled(ActionCommands.CREATE_NEW_EVIDENCE_CASE, true);
                updateOptionsEvidenceCasesNavigation(currentNetworkEditorPanel);
                setOptionEnabled(ActionCommands.PROPAGATE_EVIDENCE, !currentNetworkEditorPanel.isPropagationActive());
                if (!currentNetworkEditorPanel.getProbNet().hasConstraintOfClass(OnlyChanceNodes.class)) {
                    setOptionEnabled(ActionCommands.DECISION_TREE, true);
                    setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY, true);
                }
            }
        }
        updateOptionsFindingsDependent(currentNetworkEditorPanel);
        updatePropagateEvidenceButton();
        mainPanel.changeWorkingModeButton(workingMode);
        mainPanel.getStandardToolBar().getDecisionTreeButton().setSelected(false);
        /*
         * for (NodeType type : networkPanel.getNetwork().getNetworkType()
         * .getNodeTypes()) { switch (type) { case CHANCE: {
         * setOptionEnabled(ActionCommands.CHANCE_CREATION, true); break; } case
         * DECISION: { setOptionEnabled(ActionCommands.DECISION_CREATION, true);
         * break; } case UTILITY: {
         * setOptionEnabled(ActionCommands.UTILITY_CREATION, true); break; }
         * default: { setOptionEnabled(ActionCommands.CHANCE_CREATION, true);
         * break; } } }
         */
        setOptionEnabled(ActionCommands.SAVE_NETWORK, currentNetworkEditorPanel.getModified());
        objectsSelected();
        setZoom(currentNetworkEditorPanel.getZoom());
        /*
         * updateUndoRedo(networkPanel.getUndoManager().canUndo(),
         * networkPanel.getUndoManager().canUndo());
         */
        updateUndoRedo(currentNetworkEditorPanel.getProbNet().getPNESupport().getCanUndo(),
                       currentNetworkEditorPanel.getProbNet().getPNESupport().getCanRedo());
        // updateUndoRedo(networkPanel.getUndoManager());
        mainPanel.setToolBarPanel(currentNetworkEditorPanel.getWorkingMode());

        
    }
    
    /**
     * Enables or disables the undo and redo operations in the menubar and in
     * the toolbar, according to the state of undo and redo of the network.
     *
     * @param canRedo the can redo
     * @param canUndo the can undo
     */
    private void updateUndoRedo(boolean canUndo, boolean canRedo) {
        setOptionEnabled(ActionCommands.UNDO, canUndo);
        setOptionEnabled(ActionCommands.REDO, canRedo);
    }
    
    /**
     * Activates the options on the menus and toolbars that depend on the
     * working mode established on the network (edition or inference)
     *
     * @param workingMode  the working mode (edition or inference).
     * @param networkPanel information of the network panel.
     */
    public void updateOptionsNewWorkingMode(NetworkEditorPanel.WorkingMode workingMode, NetworkEditorPanel networkPanel) {
        switch (workingMode) {
            case EDITION -> {
                setOptionEnabled(EDITING_ACTION_COMMANDS, true);
                setOptionEnabled(ActionCommands.CHANGE_TO_EDITION_MODE, false);
                setOptionEnabled(ActionCommands.CHANGE_TO_INFERENCE_MODE, true);
                setOptionEnabled(ActionCommands.CHANGE_WORKING_MODE, true);
            }
            case INFERENCE -> {
                setOptionEnabled(EDITING_ACTION_COMMANDS, false);
                setOptionEnabled(ActionCommands.UNDO, false);
                setOptionEnabled(ActionCommands.REDO, false);
                setOptionEnabled(ActionCommands.CLIPBOARD_CUT, false);
                setOptionEnabled(ActionCommands.CLIPBOARD_COPY, false);
                setOptionEnabled(ActionCommands.CLIPBOARD_PASTE, false);
                setOptionEnabled(ActionCommands.OBJECT_REMOVAL, false);
                setOptionEnabled(ActionCommands.LINK_PROPERTIES, false);
                setOptionEnabled(ActionCommands.CHANGE_TO_INFERENCE_MODE, false);
                setOptionEnabled(ActionCommands.CHANGE_TO_EDITION_MODE, true);
                setOptionEnabled(ActionCommands.CREATE_NEW_EVIDENCE_CASE, true);
                updateOptionsEvidenceCasesNavigation(networkPanel);
                setOptionEnabled(ActionCommands.PROPAGATE_EVIDENCE, !networkPanel.isPropagationActive());
            }
        }
        objectsSelected();
    }
    
    /**
     * Activates the menu items and toolbar buttons for navigate among the set
     * of evidence cases.
     *
     * @param networkPanel information of the network panel.
     */
    public void updateOptionsEvidenceCasesNavigation(NetworkEditorPanel networkPanel) {
        if (networkPanel.getNumberOfCases() > 1) {
            setOptionEnabled(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES, true);
            if (networkPanel.getCurrentCase() > 0) {
                setOptionEnabled(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE, true);
                setOptionEnabled(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE, true);
            } else {
                setOptionEnabled(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE, false);
                setOptionEnabled(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE, false);
            }
            if (networkPanel.getCurrentCase() < (networkPanel.getNumberOfCases() - 1)) {
                setOptionEnabled(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE, true);
                setOptionEnabled(ActionCommands.GO_TO_LAST_EVIDENCE_CASE, true);
            } else {
                setOptionEnabled(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE, false);
                setOptionEnabled(ActionCommands.GO_TO_LAST_EVIDENCE_CASE, false);
            }
        } else {
            setOptionEnabled(ActionCommands.GO_TO_FIRST_EVIDENCE_CASE, false);
            setOptionEnabled(ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE, false);
            setOptionEnabled(ActionCommands.GO_TO_NEXT_EVIDENCE_CASE, false);
            setOptionEnabled(ActionCommands.GO_TO_LAST_EVIDENCE_CASE, false);
            setOptionEnabled(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES, networkPanel.areThereFindingsInCase());
        }
        updateOptionsFindingsDependent(networkPanel);
    }
    
    /**
     * Activates the options on the menus and toolbars that depend on the
     * propagation type established on the network (automatic or manual).
     *
     * @param networkPanel information of the network panel.
     */
    public void updateOptionsPropagationTypeDependent(NetworkEditorPanel networkPanel) {
        if (networkPanel.isPropagationActive()) {
            setOptionEnabled(ActionCommands.PROPAGATE_EVIDENCE, false);
        } else {
            if (networkPanel.getWorkingMode() == NetworkEditorPanel.WorkingMode.INFERENCE) {
                setOptionEnabled(ActionCommands.PROPAGATE_EVIDENCE, true);
            }
        }
    }
    
    /**
     * Activates the options on the menus and toolbars that depend on the
     * existence of findings in the current evidence case.
     *
     * @param networkPanel information of the network panel.
     */
    public void updateOptionsFindingsDependent(NetworkEditorPanel networkPanel) {
        setOptionEnabled(ActionCommands.NODE_REMOVE_ALL_FINDINGS, networkPanel.areThereFindingsInCase());
        if (networkPanel.getNumberOfCases() == 1) {
            setOptionEnabled(ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES, networkPanel.areThereFindingsInCase());
        }
    }
    
    /**
     * Activates an edition option on the menus and toolbars according to the
     * edition state.
     *
     * @param newEditionMode new edition mode.
     * @param canPaste       if the state is SELECTION, this parameter says if there
     *                       is data in the clipboard.
     */
    public void setEditionOption(String newEditionMode, boolean canPaste) {
        setOptionSelected(newEditionMode, true);
        setOptionEnabled(ActionCommands.SELECT_ALL, true);
        setOptionEnabled(ActionCommands.CLIPBOARD_PASTE, canPaste);
    }
    
    /**
     * This method activates o desactivates some options depending on the
     * numbers of nodes or links selected or the expanded state of the specific
     * nodes selected
     */
    @Override public void objectsSelected() {
        NetworkEditorPanel.WorkingMode workingMode = NetworkEditorPanel.WorkingMode.EDITION;
        NetworkEditorPanel currentNetworkEditorPanel = getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel != null) {
            workingMode = currentNetworkEditorPanel.getWorkingMode();
        }
        List<VisualNode> selectedNodes = currentNetworkEditorPanel == null ? Collections.emptyList()
                : currentNetworkEditorPanel.getVisualNetwork().getSelectedNodes();
        List<VisualLink> selectedLinks = currentNetworkEditorPanel == null ? Collections.emptyList()
                : currentNetworkEditorPanel.getVisualNetwork().getSelectedLinks();
        boolean thereAreSelectedElements = !selectedNodes.isEmpty() || !selectedLinks.isEmpty();
        boolean onlyLinksAreSelected = selectedNodes.isEmpty() && !selectedLinks.isEmpty();
        boolean canCreateNextSliceNode = false;
        boolean canTemporalEvolution = false;
        boolean canShowOptimalPolicy = false;
        boolean canShowExpectedUtility = false;
        boolean canRemovePolicy = false;
        boolean canAddTimeToEvent = false;
        boolean canEditPolicy = false;
        boolean canImposePolicy = false;
        boolean canLog = false;
        boolean canRemoveFinding = false;
        boolean canAddFinding = false;
        boolean canContract = false;
        boolean canExpand = false;
        boolean canLinkProperties = false;
        boolean canNodeTable = false;
        boolean canNodeProperties = false;
        boolean canRemove = false;
        boolean canCopy = false;
        boolean canCut = false;
        if (onlyLinksAreSelected) {
            if (workingMode == NetworkEditorPanel.WorkingMode.EDITION) {
                canRemove = thereAreSelectedElements;
            }
            if (selectedLinks.size() == 1) {
                if (workingMode == NetworkEditorPanel.WorkingMode.EDITION) {
                    canLinkProperties = thereAreSelectedElements;
                }
            }
        } else {
            canCopy = thereAreSelectedElements;
            if (workingMode == NetworkEditorPanel.WorkingMode.EDITION) {
                canRemove = thereAreSelectedElements;
                canCut = thereAreSelectedElements;
            }
            if (selectedLinks.isEmpty()) {
                // if we are in Inference Mode, options about expansion and
                // contraction must be activated
                if (workingMode == NetworkEditorPanel.WorkingMode.INFERENCE) {
                    for (VisualNode selectedNode : selectedNodes) {
                        // if at least one selected node is expanded,
                        // 'contract node(s)' option must be active
                        if (selectedNode.isExpanded()) {
                            canContract = true;
                        }
                        // if at least one selected node is contracted,
                        // 'expand node(s)' option must be active
                        if (!(selectedNode.isExpanded())) {
                            canExpand = true;
                        }
                    }
                }
                // if at least one selected node has a post-Resolution finding,
                // 'remove finding' option must be active
                for (VisualNode vNode : selectedNodes) {
                    switch (workingMode) {
                        case EDITION -> canRemoveFinding = vNode.isPreResolutionFinding();
                        case INFERENCE -> canRemoveFinding = vNode.isPostResolutionFinding();
                    }
                }
                if (selectedNodes.size() == 1) {
                    canNodeProperties = true;
                    VisualNode visualNode = selectedNodes.getFirst();
                    if (visualNode.getNode().getVariable().isTemporal()) {
                        canCreateNextSliceNode = !visualNode.getNode().getProbNet()
                                                            .containsShiftedVariable(visualNode.getNode()
                                                                                               .getVariable(), 1);
                        
                        if (!(
                                visualNode.getNode().getNodeType() == NodeType.CHANCE &&
                                        visualNode.getNode()
                                                  .getVariable()
                                                  .getVariableType() != VariableType.FINITE_STATES
                        )) {
                            canLog = true;
                            canTemporalEvolution = true;
                        }
                    }
                    switch (visualNode.getNode().getNodeType()) {
                        case CHANCE, UTILITY -> canNodeTable = true;
                        case DECISION -> {
                            switch (workingMode) {
                                case EDITION -> {
                                    if (!visualNode.getNode().getPotentials().isEmpty()) {
                                        canEditPolicy = true;
                                        canRemovePolicy = true;
                                    } else {
                                        canImposePolicy = true;
                                    }
                                }
                                case INFERENCE -> {
                                    if (visualNode.getNode().getPotentials().isEmpty()) { // ...asaez...if network compiled...currently
                                        // not needed
                                        // ...because if not compiled, those options
                                        // are not shown.
                                        canShowExpectedUtility = true;
                                        canShowOptimalPolicy = true;
                                    }
                                }
                            }
                        }
                        case EVENT -> {
                            canAddTimeToEvent = true;
                        }
                        case SV_SUM, SV_PRODUCT -> {
                        }
                    }
                    String label = StringDatabase.getUniqueInstance().getString(
                            switch (visualNode.getNode().getNodeType()) {
                                case CHANCE, DECISION, EVENT -> switch (workingMode) {
                                    case EDITION -> "Edit.NodePotential";
                                    case INFERENCE -> "Edit.ViewNodePotential";
                                };
                                case UTILITY -> switch (workingMode) {
                                    case EDITION -> "Edit.Utility";
                                    case INFERENCE -> "Edit.ViewUtility";
                                };
                                case SV_SUM, SV_PRODUCT -> null;
                            });
                    setText(ActionCommands.EDIT_POTENTIAL.getCommandName(), label);
                    canAddFinding = !visualNode.hasAnyFinding() || (workingMode == NetworkEditorPanel.WorkingMode.EDITION)
                            || (
                            workingMode == NetworkEditorPanel.WorkingMode.INFERENCE && visualNode.isPostResolutionFinding()
                    );
                    canAddFinding &= visualNode.getNode().getNodeType()!=NodeType.UTILITY;
                    boolean addOrChange =
                            (workingMode == NetworkEditorPanel.WorkingMode.EDITION && !visualNode.isPreResolutionFinding())
                                    || (
                                    workingMode == NetworkEditorPanel.WorkingMode.INFERENCE && !visualNode
                                            .isPostResolutionFinding()
                            );
                    if (visualNode.getNode().getVariable().isTemporal() &&
                            visualNode.getNode()
                                      .getParents()
                                      .stream()
                                      .anyMatch(node -> node.getVariable()
                                                            .getBaseName()
                                                            .equals(visualNode.getNode().getVariable().getBaseName())))
                        canAddFinding = false;
                    
                    setText(ActionCommands.NODE_ADD_FINDING.getCommandName(), StringDatabase.getUniqueInstance()
                                                                                            .getString((addOrChange) ? "Inference.AddFinding" : "Inference.ChangeFinding"));
                }
            }
        }
        
        
        setOptionEnabled(ActionCommands.CLIPBOARD_CUT, canCut);
        setOptionEnabled(ActionCommands.CLIPBOARD_COPY, canCopy);
        setOptionEnabled(ActionCommands.OBJECT_REMOVAL, canRemove);
        setOptionEnabled(ActionCommands.NODE_PROPERTIES, canNodeProperties);
        setOptionEnabled(ActionCommands.EDIT_POTENTIAL, canNodeTable);
        setOptionEnabled(ActionCommands.LINK_PROPERTIES, canLinkProperties);
        setOptionEnabled(ActionCommands.NODE_EXPANSION, canExpand);
        setOptionEnabled(ActionCommands.NODE_CONTRACTION, canContract);
        setOptionEnabled(ActionCommands.NODE_ADD_FINDING, canAddFinding);
        setOptionEnabled(ActionCommands.NODE_REMOVE_FINDING, canRemoveFinding);
        setOptionEnabled(ActionCommands.LOG, canLog);
        setOptionEnabled(ActionCommands.DECISION_IMPOSE_POLICY, canImposePolicy);
        setOptionEnabled(ActionCommands.DECISION_EDIT_POLICY, canEditPolicy);
        setOptionEnabled(ActionCommands.DECISION_REMOVE_POLICY, canRemovePolicy);
        setOptionEnabled(ActionCommands.DECISION_SHOW_EXPECTED_UTILITY, canShowExpectedUtility);
        setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_POLICY, canShowOptimalPolicy);
        setOptionEnabled(ActionCommands.EVENT_EDIT_TIME_TO_EVENT, canAddTimeToEvent);
        setOptionEnabled(ActionCommands.TEMPORAL_EVOLUTION_ACTION, canTemporalEvolution);
        setOptionEnabled(ActionCommands.NEXT_SLICE_NODE, canCreateNextSliceNode);
    }
    
    // TODO OOPN start
    
    // TODO OOPN end
    
    @Override public void afterEditExecutes(PNEdit edit) {
        NetworkEditorPanel currentNetworkEditorPanel = getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel == null) return;
        ProbNet probNet = currentNetworkEditorPanel.getProbNet();
        // update menu options and network agents when network type has been
        // modified
        if (edit instanceof ChangeNetworkTypeEdit) {
            updateOptionsNetworkDependent(currentNetworkEditorPanel);
            // updateNetworkAgents(currentNetworkEditorPanel);
        }
        NetworkEditorPanel.WorkingMode workingMode = currentNetworkEditorPanel.getEditorPanel()
                                                                              .getVisualNetwork()
                                                                              .getWorkingMode();
        boolean workingModeIsNotInference = workingMode != NetworkEditorPanel.WorkingMode.INFERENCE;
        updateOptionsNetworkModified(probNet.getPNESupport().getCanUndo() && workingModeIsNotInference,
                                     probNet.getPNESupport().getCanRedo() && workingModeIsNotInference);
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        ProbNet probNet = getCurrentNetworkEditorPanel().getProbNet();
        NetworkEditorPanel.WorkingMode workingMode = getCurrentNetworkEditorPanel().getEditorPanel()
                                                                                   .getVisualNetwork()
                                                                                   .getWorkingMode();
        boolean workingModeIsNotInference = workingMode != NetworkEditorPanel.WorkingMode.INFERENCE;
        updateOptionsNetworkModified(probNet.getPNESupport().getCanUndo() && workingModeIsNotInference,
                                     probNet.getPNESupport().getCanRedo() && workingModeIsNotInference);
    }
    
    @Override public void afterRedoingEdit(PNEdit edit) {
        ProbNet probNet = getCurrentNetworkEditorPanel().getProbNet();
        NetworkEditorPanel.WorkingMode workingMode = getCurrentNetworkEditorPanel().getEditorPanel()
                                                                                   .getVisualNetwork()
                                                                                   .getWorkingMode();
        boolean workingModeIsNotInference = workingMode != NetworkEditorPanel.WorkingMode.INFERENCE;
        updateOptionsNetworkModified(probNet.getPNESupport().getCanUndo() && workingModeIsNotInference,
                                     probNet.getPNESupport().getCanRedo() && workingModeIsNotInference);
    }
    
    public NetworkEditorPanel getCurrentNetworkEditorPanel() {
        int selectedIndex = mainPanel.getNetworksTabPanel().getSelectedIndex();
        if (selectedIndex <= -1 || selectedIndex >= mainPanel.getNetworksTabPanel().getTabCount()) return null;
        Component componentAt = mainPanel.getNetworksTabPanel().getComponentAt(selectedIndex);
        if (componentAt instanceof NetworkEditorPanel networkPanel) {
            return networkPanel;
        }
        return null;
    }
    
    
    /**
     * Enables or disables options on 'File' menu depending on the type of
     * window selected.
     *
     * @param value indicates if options should be enabled or disabled.
     */
    public void updateOptionsWindowSelected(boolean value) {
        setOptionEnabled(ActionCommands.SAVE_OPEN_NETWORK, value);
        setOptionEnabled(ActionCommands.SAVEAS_NETWORK, value);
        setOptionEnabled(ActionCommands.NETWORK_PROPERTIES, value);
        setOptionEnabled(ActionCommands.LOAD_EVIDENCE, value);
        setOptionEnabled(ActionCommands.SAVE_EVIDENCE, value);
    }
    
    /**
     * Shows or hides 'Propagate evidence' option from menu and toolbar.
     */
    public void updatePropagateEvidenceButton() {
        NetworkEditorPanel currentNetworkEditorPanel = getCurrentNetworkEditorPanel();
        if (currentNetworkEditorPanel == null) return;
        if (currentNetworkEditorPanel.isAutomaticPropagation()) {
            mainPanel.getInferenceToolBar().removePropagateNowButton();
            mainPanel.getMainMenu().removePropagateNowItem();
        } else {
            mainPanel.getInferenceToolBar().addPropagateNowButton();
            mainPanel.getMainMenu().addPropagateNowItem();
        }
        updateOptionsEvidenceCasesNavigation(getCurrentNetworkEditorPanel());
        updateOptionsPropagationTypeDependent(getCurrentNetworkEditorPanel());
    }
    
    public void updateOptionsDecisionTree(DecisionTreeEditor decisionTreeEditor) {
        setOptionEnabled(EDITING_ACTION_COMMANDS, false);
        setOptionEnabled(INFERENCE_ACTION_COMMANDS, false);
        // setOptionEnabled(VIEWING_ACTION_COMMANDS, false);
        setOptionEnabled(ActionCommands.SAVE_NETWORK, false);
        setOptionEnabled(ActionCommands.PROPAGATION_OPTIONS, false);
        setOptionEnabled(ActionCommands.INFERENCE_OPTIONS, false);
        setOptionEnabled(ActionCommands.CHANGE_WORKING_MODE, false);
        setOptionEnabled(ActionCommands.CHANGE_TO_INFERENCE_MODE, false);
        setOptionEnabled(ActionCommands.CHANGE_TO_EDITION_MODE, false);
        setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY, false);
        setOptionEnabled(ActionCommands.DECISION_SHOW_OPTIMAL_POLICY, false);
        setOptionEnabled(ActionCommands.DECISION_TREE, false);
        setOptionEnabled(ActionCommands.UNDO, false);
        setOptionEnabled(ActionCommands.REDO, false);
        setOptionEnabled(ActionCommands.CLIPBOARD_CUT, false);
        setOptionEnabled(ActionCommands.CLIPBOARD_COPY, false);
        setOptionEnabled(ActionCommands.CLIPBOARD_PASTE, false);
        setOptionEnabled(ActionCommands.OBJECT_REMOVAL, false);
        setOptionEnabled(ActionCommands.OBJECT_SELECTION, false);
        setOptionEnabled(ActionCommands.CHANCE_CREATION, false);
        setOptionEnabled(ActionCommands.DECISION_CREATION, false);
        setOptionEnabled(ActionCommands.UTILITY_CREATION, false);
        setOptionEnabled(ActionCommands.LINK_CREATION, false);
        
        //mainPanel.getStandardToolBar().getDecisionTreeButton().setSelected(true);
        setZoom(decisionTreeEditor.getZoom());
    }
    
    public void updateOptionsNetworkOpenedURL(boolean networkOpenedURL) {
        this.networkOpenedURL = networkOpenedURL;
        setOptionEnabled(ActionCommands.SAVE_NETWORK, !networkOpenedURL);
        setOptionEnabled(ActionCommands.SAVE_OPEN_NETWORK, !networkOpenedURL);
    }
    
}
