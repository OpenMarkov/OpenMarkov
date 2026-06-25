/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.core.action.base.linkEdits.InvertLinkAndUpdatePotentialsEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.action.RemoveLinkRestrictionEdit;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.gui.dialog.link.LinkRestrictionEditDialog;
import org.openmarkov.gui.dialog.link.RevelationArcEditDialog;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.graphic.VisualLink;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.util.PropertyNames;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JButton;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade that receives all GUI events and delegates to domain-specific handlers.
 * <p>
 * Handlers:
 * <ul>
 *   <li>{@link NetworkFileHandler} — file I/O, open/save/close, evidence</li>
 *   <li>{@link InferenceHandler} — inference mode, evidence cases, propagation</li>
 *   <li>{@link EditAndViewHandler} — undo/redo, zoom, edition mode, dialogs</li>
 * </ul>
 */
public class MainPanelListenerAssistant extends WindowAdapter
        implements ActionListener, PropertyNames, ComponentListener {
    
    private final MainPanel mainPanel;
    private final List<NetworkEditorPanel> networkPanels;
    
    private final NetworkFileHandler fileHandler;
    private final InferenceHandler inferenceHandler;
    private final EditAndViewHandler editAndViewHandler;
    
    public MainPanelListenerAssistant(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.mainPanel.setName(mainPanel.getName());
        this.networkPanels = new ArrayList<>();
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        
        this.fileHandler = new NetworkFileHandler(mainPanel, networkPanels, stringDatabase);
        this.inferenceHandler = new InferenceHandler(mainPanel, fileHandler);
        this.editAndViewHandler = new EditAndViewHandler(mainPanel);
    }
    
    // ── Event dispatch ────────────────────────────────────────────
    
    @Override public void windowClosing(WindowEvent e) {
        try {
            fileHandler.closeApplication();
        } catch (WriterException ex) {
            throw new UnrecoverableException(ex);
        }
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        ActionCommands actionCommandConstant = ActionCommands.of(actionCommand);
        switch (actionCommandConstant) {
            // ── File ──────────────────────────────────────────
            case ActionCommands.NEW_NETWORK -> fileHandler.createNewNetwork();
            case ActionCommands.OPEN_NETWORK -> GUIUtils.executeUIAction(() -> fileHandler.openNetwork());
            case ActionCommands.OPEN_NETWORK_URL -> GUIUtils.executeUIAction(() -> fileHandler.openNetworkURL());
            case ActionCommands.SAVE_NETWORK ->
                    GUIUtils.executeUIAction(() -> fileHandler.saveNetwork(getCurrentNetworkEditorPanel()));
            case ActionCommands.SAVE_OPEN_NETWORK ->
                    GUIUtils.executeUIAction(() -> fileHandler.saveOpenNetwork(getCurrentNetworkEditorPanel()));
            case ActionCommands.SAVEAS_NETWORK ->
                    GUIUtils.executeUIAction(() -> fileHandler.saveNetworkAs(getCurrentNetworkEditorPanel()));
            case ActionCommands.CLOSE_TAB -> GUIUtils.executeUIAction(() -> fileHandler.closeCurrentTab());
            case ActionCommands.LOAD_EVIDENCE ->
                    GUIUtils.executeUIAction(() -> fileHandler.loadEvidence(getCurrentNetworkEditorPanel()));
            case ActionCommands.SAVE_EVIDENCE -> fileHandler.saveEvidence(getCurrentNetworkEditorPanel());
            case ActionCommands.NETWORK_PROPERTIES -> getCurrentNetworkEditorPanel().changeNetworkProperties();
            case ActionCommands.EXIT_APPLICATION -> GUIUtils.executeUIAction(() -> fileHandler.closeApplication());
            
            // ── Edit ──────────────────────────────────────────
            case ActionCommands.CLIPBOARD_COPY -> {
                getCurrentNetworkEditorPanel().exportToClipboard(false);
                mainPanel.getMainPanelMenuAssistant()
                         .setOptionEnabled(ActionCommands.CLIPBOARD_PASTE.getCommandName(), true);
            }
            case ActionCommands.CLIPBOARD_CUT -> {
                getCurrentNetworkEditorPanel().exportToClipboard(true);
                mainPanel.getMainPanelMenuAssistant()
                         .setOptionEnabled(ActionCommands.CLIPBOARD_PASTE.getCommandName(), true);
            }
            case ActionCommands.CLIPBOARD_PASTE ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().pasteFromClipboard(null));
            case ActionCommands.UNDO -> editAndViewHandler.undo();
            case ActionCommands.REDO -> editAndViewHandler.redo();
            case ActionCommands.SELECT_ALL -> getCurrentNetworkEditorPanel().selectAllObjects();
            case ActionCommands.AUTO_ARRANGE -> getCurrentNetworkEditorPanel().autoArrangeNodes();
            case ActionCommands.OBJECT_REMOVAL -> getCurrentNetworkEditorPanel().removeSelectedObjects();
            case ActionCommands.EDITION_MODE_PREFIX -> editAndViewHandler.activateEditionMode(actionCommand);
            case ActionCommands.NODE_PROPERTIES ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().changeNodeProperties());
            case ActionCommands.EDIT_POTENTIAL ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().changePotential());
            
            // ── Inference / working mode ──────────────────────
            case ActionCommands.CHANGE_WORKING_MODE, ActionCommands.CHANGE_TO_INFERENCE_MODE,
                 ActionCommands.CHANGE_TO_EDITION_MODE -> {
                NetworkEditorPanel.WorkingMode initialWorkingMode = getCurrentNetworkEditorPanel().getWorkingMode();
                try {
                    inferenceHandler.toggleWorkingMode();
                } catch (NotEnoughMemoryException | IncompatibleEvidenceException | ConstraintViolatedException |
                         RuntimeException | NotEvaluableNetworkException | NonProjectablePotentialException |
                         CannotNormalizePotentialException ex) {
                    try {
                        inferenceHandler.setWorkingMode(initialWorkingMode, initialWorkingMode);
                    } catch (NotEvaluableNetworkException | NonProjectablePotentialException |
                             NotEnoughMemoryException | IncompatibleEvidenceException | ConstraintViolatedException |
                             CannotNormalizePotentialException exc) {
                        throw new UnreachableException(exc);
                    }
                    throw new UnrecoverableException(ex);
                }
            }
            case ActionCommands.MC_SIMULATE_NETWORK -> monteCarloSimulation();
            case ActionCommands.SET_NEW_EXPANSION_THRESHOLD ->
                    inferenceHandler.setNewExpansionThreshold((Double) e.getSource());
            case ActionCommands.CREATE_NEW_EVIDENCE_CASE ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.evidenceCasesNavigationOption("CREATE_NEW_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_FIRST_EVIDENCE_CASE ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.evidenceCasesNavigationOption("GO_TO_FIRST_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.evidenceCasesNavigationOption("GO_TO_PREVIOUS_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_NEXT_EVIDENCE_CASE ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.evidenceCasesNavigationOption("GO_TO_NEXT_EVIDENCE_CASE"));
            case ActionCommands.GO_TO_LAST_EVIDENCE_CASE ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.evidenceCasesNavigationOption("GO_TO_LAST_EVIDENCE_CASE"));
            case ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.evidenceCasesNavigationOption("CLEAR_OUT_ALL_EVIDENCE_CASES"));
            case ActionCommands.PROPAGATE_EVIDENCE ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().propagateEvidence(mainPanel.getMainPanelMenuAssistant()));
            case ActionCommands.PROPAGATION_OPTIONS -> inferenceHandler.setPropagationOptions();
            case ActionCommands.INFERENCE_OPTIONS ->
                    inferenceHandler.setInferenceOptions(getCurrentNetworkEditorPanel());
            case ActionCommands.EXPAND_NETWORK -> GUIUtils.executeUIAction(() ->
                                                                                   inferenceHandler.expandNetwork(getCurrentNetworkEditorPanel().getProbNet(),
                                                                                                                  getCurrentNetworkEditorPanel().getEditorPanel()
                                                                                                                                                .getEvidenceManager()
                                                                                                                                                .getPreResolutionEvidence()));
            case ActionCommands.TEMPORAL_EVOLUTION_BY_CRITERION, ActionCommands.TEMPORAL_EVOLUTION_ACTION ->
                    getCurrentNetworkEditorPanel().temporalEvolution();
            
            // ── View ──────────────────────────────────────────
            case ActionCommands.BYTITLE_NODES -> editAndViewHandler.activateByTitle(true);
            case ActionCommands.BYNAME_NODES -> editAndViewHandler.activateByTitle(false);
            case ActionCommands.ZOOM_IN -> editAndViewHandler.incrementZoom(getCurrentPanel());
            case ActionCommands.ZOOM_OUT -> editAndViewHandler.decrementZoom(getCurrentPanel());
            
            // ── Node expansion/contraction ────────────────────
            case ActionCommands.NODE_EXPANSION -> {
                NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
                List<VisualNode> selectedNodes = networkPanel.getEditorPanel().getVisualNetwork().getSelectedNodes();
                for (VisualNode visualNode : selectedNodes) {
                    if (!visualNode.isExpanded()) {
                        visualNode.setExpanded(true);
                        networkPanel.getEditorPanel().getVisualNetwork().setSelectedNode(visualNode, false);
                    }
                }
                networkPanel.getEditorPanel().repaint();
            }
            case ActionCommands.NODE_CONTRACTION -> {
                NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
                List<VisualNode> selectedNodes = networkPanel.getEditorPanel().getVisualNetwork().getSelectedNodes();
                for (VisualNode visualNode : selectedNodes) {
                    if (visualNode.isExpanded()) {
                        visualNode.setExpanded(false);
                        networkPanel.getEditorPanel().getVisualNetwork().setSelectedNode(visualNode, false);
                    }
                }
                networkPanel.getEditorPanel().repaint();
            }
            case ActionCommands.NODE_ADD_FINDING -> getCurrentNetworkEditorPanel().addFinding();
            case ActionCommands.NODE_REMOVE_FINDING ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().removeFinding());
            case ActionCommands.NODE_REMOVE_ALL_FINDINGS ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().removeAllFindings());
            case ActionCommands.ABSORB_NODE ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().absorbNode());
            case ActionCommands.ABSORB_PARENTS ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().absorbParents());
            
            // ── Link operations ───────────────────────────────
            case ActionCommands.INVERT_LINK_AND_UPDATE_POTENTIALS -> GUIUtils.executeUIAction(() -> {
                NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
                Link<Node> link = networkPanel.getEditorPanel().getVisualNetwork().getLastSelectedLink().getLink();
                new InvertLinkAndUpdatePotentialsEdit(networkPanel.getEditorPanel()
                                                                  .getVisualNetwork()
                                                                  .getProbNet(), link.getFrom()
                                                                                     .getVariable(), link.getTo()
                                                                                                         .getVariable())
                        .executeEdit();
            });
            case ActionCommands.LINK_RESTRICTION_EDIT_PROPERTIES -> {
                NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
                List<VisualLink> links = networkPanel.getEditorPanel().getVisualNetwork().getSelectedLinks();
                if (!links.isEmpty()) {
                    Link<Node> link = links.getFirst().getLink();
                    if (!link.hasRestrictions()) {
                        link.initializesRestrictionsPotential();
                    }
                    new LinkRestrictionEditDialog(GUIUtils.getOwner(networkPanel.getEditorPanel()), link).requestValues();
                    link.tryResetRestrictionsPotential();
                    networkPanel.getEditorPanel().repaint();
                }
            }
            case ActionCommands.LINK_RESTRICTION_DISABLE_PROPERTIES -> GUIUtils.executeUIAction(() ->
                                                                                                        new RemoveLinkRestrictionEdit(getCurrentNetworkEditorPanel().getEditorPanel()
                                                                                                                                                                    .getVisualNetwork()).executeEdit());
            case ActionCommands.LINK_REVELATIONARC_PROPERTIES -> {
                NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
                Link<Node> link = networkPanel.getEditorPanel().getVisualNetwork().getLastSelectedLink().getLink();
                Window owner = GUIUtils.getOwner(networkPanel.getEditorPanel());
                new RevelationArcEditDialog(owner, link).requestValues();
            }
            
            // ── Decision operations ───────────────────────────
            case ActionCommands.DECISION_IMPOSE_POLICY ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().imposePolicyInNode());
            case ActionCommands.DECISION_EDIT_POLICY ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().editNodePolicy());
            case ActionCommands.DECISION_REMOVE_POLICY ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().removePolicyFromNode());
            case ActionCommands.EVENT_EDIT_TIME_TO_EVENT ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().changePotential());
            case ActionCommands.DECISION_SHOW_EXPECTED_UTILITY ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().showExpectedUtilityOfNode());
            case ActionCommands.DECISION_SHOW_OPTIMAL_POLICY ->
                    GUIUtils.executeUIAction(() -> getCurrentNetworkEditorPanel().showOptimalPolicyOfNode());
            case ActionCommands.DECISION_TREE ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.showDecisionTree(getCurrentNetworkEditorPanel()));
            case ActionCommands.DECISION_SHOW_OPTIMAL_STRATEGY ->
                    GUIUtils.executeUIAction(() -> inferenceHandler.showOptimalStrategy(getCurrentNetworkEditorPanel()));
            
            // ── Misc ──────────────────────────────────────────
            case ActionCommands.NEXT_SLICE_NODE -> GUIUtils.executeUIAction(() -> {
                NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
                Node selectedNode = networkPanel.getEditorPanel()
                                                .getVisualNetwork()
                                                .getLastSelectedNode()
                                                .getNode();
                Variable selectedVariable = selectedNode.getVariable();
                Variable newVariable = new Variable(selectedVariable);
                newVariable.setTimeSlice(selectedVariable.getTimeSlice() + 1);
                Point2D.Double position = new Point2D.Double(selectedNode.getCoordinateX() + 200,
                                                             selectedNode.getCoordinateY());
                new AddNodeEdit(networkPanel.getEditorPanel()
                                            .getVisualNetwork()
                                            .getProbNet(), newVariable, selectedNode.getNodeType(), position).executeEdit();
            });
            case ActionCommands.COST_EFFECTIVENESS_DETERMINISTIC -> {
                try {
                    Method ceMethod = Class.forName("org.openmarkov.costEffectiveness.CostEffectivenessPlugin")
                                           .getDeclaredMethod("onClick");
                    ceMethod.setAccessible(true);
                    ceMethod.invoke(null);
                } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException ex) {
                    throw new UnreachableException(ex);
                } catch (InvocationTargetException ex) {
                    switch (ex.getCause()) {
                        case RuntimeException exc -> throw exc;
                        case Exception exc -> throw new UnrecoverableException(exc);
                        case null, default -> throw new UnreachableException(ex);
                    }
                }
            }
            case ActionCommands.CONFIGURATION ->
                    GUIUtils.executeUIAction(() -> editAndViewHandler.showUserConfigurationDialog());
            case ActionCommands.HELP_CHANGE_LANGUAGE -> editAndViewHandler.showLanguageChangeDialog();
            case ActionCommands.HELP_SHORTCUTS -> editAndViewHandler.showShortcuts();
            case ActionCommands.HELP_ABOUT -> editAndViewHandler.showAbout();
            case ActionCommands.SENSITIVITY_ANALYSIS -> {
                GUIUtils.executeUIAction(() -> {
                    ToolPlugin toolPlugin = (ToolPlugin) Class.forName("org.openmarkov.sensitivityanalysis.SensitivityAnalysisPlugin").getDeclaredConstructor().newInstance();
                    toolPlugin.toMenuItem().doClick();
                });
            }
            
            // ── Default / no-op cases ─────────────────────────
            case ActionCommands.CHANCE_CREATION, ActionCommands.UNCERTAINTY_REMOVE, ActionCommands.UNCERTAINTY_EDIT,
                 ActionCommands.UNCERTAINTY_ASSIGN, ActionCommands.TEMPORAL_OPTIONS,
                 ActionCommands.SENSITIVITY_ANALYSIS_PROBABILISTIC,
                 ActionCommands.SENSITIVITY_ANALYSIS_DETERMINISTIC, ActionCommands.COST_EFFECTIVENESS_SENSITIVITY,
                 ActionCommands.LEARNING,
                 ActionCommands.VIEW_TOOLBARS, ActionCommands.LINK_PROPERTIES,
                 ActionCommands.TREE_SAVE_GRAPHVIZ, ActionCommands.TREE_SHOW_CEP, ActionCommands.TREE_OPEN_NETWORK,
                 ActionCommands.TREE_EXPAND_ALL, ActionCommands.TREE_EXPAND_NEXT,
                 ActionCommands.LINK_CREATION, ActionCommands.UTILITY_CREATION,
                 ActionCommands.DECISION_CREATION, ActionCommands.EVENT_CREATION, ActionCommands.LOG,
                 ActionCommands.CHANGE_ACTIVE_CLASS,
                 ActionCommands.ZOOM_PREFIX, ActionCommands.NODES, ActionCommands.ZOOM,
                 ActionCommands.OBJECT_SELECTION,
                 ActionCommands.OPEN_LAST_1_FILE, ActionCommands.OPEN_LAST_2_FILE,
                 ActionCommands.OPEN_LAST_3_FILE, ActionCommands.OPEN_LAST_4_FILE,
                 ActionCommands.OPEN_LAST_5_FILE, ActionCommands.OPEN_LAST_6_FILE,
                 ActionCommands.OPEN_LAST_7_FILE, ActionCommands.OPEN_LAST_8_FILE,
                 ActionCommands.OPEN_LAST_9_FILE,
                 ActionCommands.SET_IMPOSSIBLE_CONFIGURATION, ActionCommands.UNSET_IMPOSSIBLE_CONFIGURATION,
                 ActionCommands.ADD_FUNCTION -> defaultActionOnCommand(e, actionCommand, actionCommandConstant);
            case null -> defaultActionOnCommand(e, actionCommand, actionCommandConstant);
        }
    }
    
    // ── Default action fallback ───────────────────────────────────
    
    private void defaultActionOnCommand(ActionEvent e, String actionCommand, ActionCommands actionCommandConstant) {
        if (actionCommand.startsWith(ActionCommands.EDITION_MODE_PREFIX.getCommandName())) {
            editAndViewHandler.activateEditionMode(actionCommand);
        } else if (actionCommand.startsWith(ActionCommands.VIEW_TOOLBARS.getCommandName())) {
            MainGUI.INSTANCE.mainPanel.getToolbarManager()
                                      .addToolbar(actionCommand.replace(ActionCommands.VIEW_TOOLBARS.getCommandName() + ".", ""));
        } else if (ActionCommands.isZoomActionCommand(actionCommand)) {
            editAndViewHandler.setZoom(getCurrentPanel(), ActionCommands.getValueZoomActionCommand(actionCommand));
        } else if (e.getSource() instanceof JButton source) {
            var listeners = source.getActionListeners();
        } else if (actionCommandConstant != null
                && actionCommandConstant.openRecentFileIndex().orElse(null) instanceof Integer recentFileIndex) {
            GUIUtils.executeUIAction(() -> fileHandler.openNetwork(LastOpenFiles.getFilePathAt(recentFileIndex)));
        }
    }
    
    // ── Public API (delegates) ────────────────────────────────────
    
    public NetworkEditorPanel getCurrentNetworkEditorPanel() {
        return mainPanel.getMainPanelMenuAssistant().getCurrentNetworkEditorPanel();
    }
    
    public EditorPanel getCurrentPanel() {
        return (EditorPanel) mainPanel.getNetworksTabPanel().getSelectedComponent();
    }
    
    public void openNetwork(String fileName) throws ProbNetParserException, IOException, org.openmarkov.core.io.format.annotation.NoReaderForFileException, org.openmarkov.gui.exception.CorruptNetworkFile {
        fileHandler.openNetwork(fileName);
    }
    
    public void openNetwork(ProbNet probNet) {
        fileHandler.openNetwork(probNet);
    }
    
    public boolean saveNetwork(NetworkEditorPanel networkPanel) throws WriterException {
        return fileHandler.saveNetwork(networkPanel);
    }
    
    public boolean saveNetworkAs(NetworkEditorPanel networkPanel) throws WriterException {
        return fileHandler.saveNetworkAs(networkPanel);
    }
    
    public boolean networkCanBeClosed(NetworkEditorPanel networkPanel) throws WriterException {
        return fileHandler.networkCanBeClosed(networkPanel);
    }
    
    public NetworkEditorPanel createNewFrame(ProbNet probNet) {
        return fileHandler.createNewFrame(probNet);
    }
    
    public List<NetworkEditorPanel> getNetworkEditorPanels() {
        return networkPanels;
    }
    
    public boolean closePanel(EditorPanel panel) {
        return panel.close();
    }
    
    
    // 21/08/2019 22/04/2021 DESInference
    
    /**
     * This method performs N Monte Carlo simulations
     *
     */
    protected void monteCarloSimulation() {
        boolean performInference = true;
        mainPanel.selecMonteCarloButton(false);
        
        InferenceOptionsDialog dialog = new InferenceOptionsDialog(getCurrentNetworkEditorPanel().getProbNet(), SwingUtilities.getWindowAncestor(mainPanel), MulticriteriaOptions.Type.COST_EFFECTIVENESS);
        ProbNet probNet = getCurrentNetworkEditorPanel().getProbNet();
        // Show multicriteria dialog if the probnet has at least two criteria and have utility nodes
        if (dialog.getSelectedOption() == OkCancelDialog.ChosenOption.Cancel) {
            performInference = false;
        }
        
        if (performInference) {
            
            
            ProgressMonitor simulationProgressMonitor = new ProgressMonitor(SwingUtilities.getWindowAncestor(mainPanel), "Running simulation", null, 0, 0);
            
            new Thread(() -> {
                try {
                    new org.openmarkov.inference.DES.DESInference(probNet, simulationProgressMonitor);
                } catch (IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther | IOException e) {
                    throw new UnrecoverableException(e);
                }
            }).start();
        }
    }
    //
    
    
    // ── ComponentListener ─────────────────────────────────────────
    
    @Override public void componentResized(ComponentEvent e) {
        mainPanel.adaptToolBarSize();
    }
    
    @Override public void componentMoved(ComponentEvent e) {
    }
    
    @Override public void componentShown(ComponentEvent e) {
    }
    
    @Override public void componentHidden(ComponentEvent e) {
    }
}
