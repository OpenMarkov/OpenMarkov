/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.potential.StrategyCarrier;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.gui.dialog.PropagationOptionsDialog;
import org.openmarkov.gui.dialog.network.OptimalStrategyDialog;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.exception.NotEnoughMemoryException;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.util.GUIUtils;
import org.openmarkov.gui.window.decisiontree.DecisionTreeEditor;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEOptimalIntervention;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles inference mode switching, evidence case navigation, propagation,
 * network expansion, decision trees, and optimal strategy display.
 * Package-private — only accessed from {@link MainPanelListenerAssistant}.
 *
 * @author Manuel Arias
 */
class InferenceHandler {

    private final MainPanel mainPanel;
    private final NetworkFileHandler fileHandler;

    InferenceHandler(MainPanel mainPanel, NetworkFileHandler fileHandler) {
        this.mainPanel = mainPanel;
        this.fileHandler = fileHandler;
    }

    // ── Working mode ──────────────────────────────────────────────
    
    void toggleWorkingMode() throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        NetworkEditorPanel.WorkingMode currentWorkingMode = getCurrentNetworkEditorPanel().getWorkingMode();
        NetworkEditorPanel.WorkingMode newWorkingMode = switch (currentWorkingMode) {
            case EDITION -> NetworkEditorPanel.WorkingMode.INFERENCE;
            case INFERENCE -> NetworkEditorPanel.WorkingMode.EDITION;
        };
        setWorkingMode(currentWorkingMode, newWorkingMode);
    }
    
    void setWorkingMode(NetworkEditorPanel.WorkingMode currentWorkingMode, NetworkEditorPanel.WorkingMode newWorkingMode) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        boolean performInference = true;
        boolean isTemporal;
        boolean isMulticriteria = false;

        ProbNet probNet = getCurrentNetworkEditorPanel().getProbNet();

        isTemporal = !probNet.hasConstraintOfClass(OnlyAtemporalVariables.class);
        if (probNet.getDecisionCriteria() != null && probNet.getDecisionCriteria().size() > 1) {
            isMulticriteria = true;
        }
        boolean requiredInferenceOptions = isTemporal || isMulticriteria;

        if (currentWorkingMode == NetworkEditorPanel.WorkingMode.EDITION && requiredInferenceOptions) {
            InferenceOptionsDialog dialog = new InferenceOptionsDialog(probNet, GUIUtils.getOwner(mainPanel), MulticriteriaOptions.Type.UNICRITERION);

            if (dialog.getSelectedOption() == OkCancelDialog.ChosenOption.Cancel) {
                newWorkingMode = NetworkEditorPanel.WorkingMode.EDITION;
                performInference = false;
            }
        }

        mainPanel.setToolBarPanel(newWorkingMode);
        mainPanel.changeWorkingModeButton(newWorkingMode);
        if (!fileHandler.getNetworkEditorPanels().isEmpty()) {
            getCurrentNetworkEditorPanel().setWorkingMode(newWorkingMode);
        }
        getCurrentNetworkEditorPanel().setSelectedAllObjects(false);
        mainPanel.getMainPanelMenuAssistant().updateOptionsNetworkDependent(getCurrentNetworkEditorPanel());

        try {
            if (performInference) {
                switch (newWorkingMode) {
                    case EDITION -> {
                    }
                    case INFERENCE -> {
                        getCurrentNetworkEditorPanel().updateIndividualProbabilitiesAndUtilities();
                        mainPanel.getInferenceToolBar()
                                 .setCurrentEvidenceCaseName(getCurrentNetworkEditorPanel().getCurrentCase());
                    }
                }
            }
        } finally {
            getCurrentNetworkEditorPanel().updateNodesExpansionState(newWorkingMode);
            mainPanel.adaptToolBarSize();
        }
        mainPanel.getMainPanelMenuAssistant().updateOptionsNewWorkingMode(newWorkingMode, getCurrentNetworkEditorPanel());
        mainPanel.getMainMenu().reInitialize();
        SwingUtilities.invokeLater(() -> {
            MainGUI.INSTANCE.revalidate();
            MainGUI.INSTANCE.mainPanel.adaptToolBarSize();
        });
    }
    void setNewExpansionThreshold(Double newValue) {
        getCurrentNetworkEditorPanel().setExpansionThreshold(newValue);
        getCurrentNetworkEditorPanel().setSelectedAllNodes(false);
        mainPanel.getMainPanelMenuAssistant()
                 .updateOptionsNewWorkingMode(NetworkEditorPanel.WorkingMode.INFERENCE, getCurrentNetworkEditorPanel());
        getCurrentNetworkEditorPanel().updateNodesExpansionState(NetworkEditorPanel.WorkingMode.INFERENCE);
    }

    // ── Evidence cases ────────────────────────────────────────────
    
    void evidenceCasesNavigationOption(String command) throws NotEvaluableNetworkException, NonProjectablePotentialException, NotEnoughMemoryException, IncompatibleEvidenceException, ConstraintViolatedException, CannotNormalizePotentialException {
        switch (command) {
            case "CREATE_NEW_EVIDENCE_CASE" -> getCurrentNetworkEditorPanel().createNewEvidenceCase();
            case "GO_TO_FIRST_EVIDENCE_CASE" -> getCurrentNetworkEditorPanel().goToFirstEvidenceCase();
            case "GO_TO_PREVIOUS_EVIDENCE_CASE" -> getCurrentNetworkEditorPanel().goToPreviousEvidenceCase();
            case "GO_TO_NEXT_EVIDENCE_CASE" -> getCurrentNetworkEditorPanel().goToNextEvidenceCase();
            case "GO_TO_LAST_EVIDENCE_CASE" -> getCurrentNetworkEditorPanel().goToLastEvidenceCase();
            case "CLEAR_OUT_ALL_EVIDENCE_CASES" -> getCurrentNetworkEditorPanel().clearOutAllEvidenceCases();
        }
        mainPanel.getMainPanelMenuAssistant().updateOptionsEvidenceCasesNavigation(getCurrentNetworkEditorPanel());
        mainPanel.getMainPanelMenuAssistant().updateOptionsPropagationTypeDependent(getCurrentNetworkEditorPanel());
    }

    // ── Propagation & inference options ───────────────────────────

    void setPropagationOptions() {
        NetworkEditorPanel networkPanel = getCurrentNetworkEditorPanel();
        new PropagationOptionsDialog(GUIUtils.getOwner(networkPanel.getEditorPanel()), networkPanel.getEditorPanel(),
                                     networkPanel.getMainPanel().getInferenceToolBar())
                .setVisible(true);
        mainPanel.getMainPanelMenuAssistant().updatePropagateEvidenceButton();
    }

    void setInferenceOptions(NetworkEditorPanel networkPanel) {
        InferenceOptionsDialog dialog = new InferenceOptionsDialog(networkPanel.getProbNet(),
                                                                   GUIUtils.getOwner(mainPanel), null);
    }

    // ── Network expansion ─────────────────────────────────────────

    void expandNetwork(ProbNet probNet, EvidenceCase preResolutionEvidence) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        NetworkEditorPanel networkPanelMID = getCurrentNetworkEditorPanel();
        String path = (new File(networkPanelMID.getNetworkFile())).getParent();
        InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(probNet,
                                                                                    GUIUtils.getOwner(mainPanel), null);
        costEffectivenessDialog.getMulticriteriaPanel().setEnabled(false);
        if (costEffectivenessDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Cancel) {
            return;
        }

        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        String networkName = probNet.getName();
        String fileName = networkName.substring(0, networkName.lastIndexOf('.'));
        fileName = fileName + stringDatabase.getString("CostEffectiveness.ExpandNetwork.FileName") + ".pgmx";

        ProbNet expandedNetwork = TemporalNetOperations.expandNetwork(probNet, preResolutionEvidence, fileName);

        NetworkEditorPanel networkPanel = fileHandler.createNewFrame(expandedNetwork);
        mainPanel.getMainPanelMenuAssistant()
                 .setOptionEnabled(ActionCommands.SAVE_OPEN_NETWORK.getCommandName(), false);
        networkPanel.setNetworkFile(path + File.separator + fileName);
        networkPanel.getEditorPanel()
                    .getEvidenceManager()
                    .setEvidence(preResolutionEvidence, new ArrayList<>());
        fileHandler.getNetworkEditorPanels().add(networkPanel);
    }

    // ── Decision tree & optimal strategy ──────────────────────────

    void showDecisionTree(NetworkEditorPanel networkPanel) throws IncompatibleEvidenceException, NotEvaluableNetworkException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates, NotEnoughMemoryException {
        try {
            InferenceOptionsDialog costEffectivenessDialog = new InferenceOptionsDialog(networkPanel.getProbNet(),
                    GUIUtils.getOwner(mainPanel),null);
            
            DecisionTreeEditor decisionTree = new DecisionTreeEditor(networkPanel);
            mainPanel.addCloseableTab("Decision tree for " + networkPanel.getProbNet().getName(), decisionTree);
            mainPanel.getMainPanelMenuAssistant().updateOptionsDecisionTree(decisionTree);
            mainPanel.getNetworksTabPanel().setSelectedComponent(decisionTree);
        } catch (OutOfMemoryError e) {
            throw new NotEnoughMemoryException(e);
        }
    }

    void showOptimalStrategy(NetworkEditorPanel networkPanel) throws IncompatibleEvidenceException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        if (networkPanel.getModified()) {
            // Network was modified after last inference — would need re-evaluation
        }

        ProbNet probNet = networkPanel.getProbNet();

        if (networkPanel.getProbNet().getNetworkType().equals(DecisionAnalysisNetworkType.getUniqueInstance())) {
            DANEvaluation eval = new DANDecompositionIntoSymmetricDANsEvaluation(probNet, networkPanel.getEditorPanel()
                                                                                                      .getEvidenceManager()
                                                                                                      .getPreResolutionEvidence());
            StrategyTree strategyTree = ((StrategyCarrier) eval.getUtility()).getStrategyTrees()[0];
            strategyTree.pruneAndGraftNode("OD");
            OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(GUIUtils.getOwner(mainPanel),
                                                                                    probNet, strategyTree);
            optimalStrategyDialog.setVisible(true);
        } else {
            VEOptimalIntervention veOptimalStrategy = null;
            try {
                veOptimalStrategy = new VEOptimalIntervention(probNet,
                                                              networkPanel.getEditorPanel()
                                                                          .getEvidenceManager()
                                                                          .getPreResolutionEvidence());
            } catch (NotEvaluableNetworkException.NotApplicableNetwork | IncompatibleEvidenceException |
                     ConstraintViolatedException e) {
                throw new UnrecoverableException(e);
            }

            try {
                OptimalStrategyDialog optimalStrategyDialog = new OptimalStrategyDialog(GUIUtils.getOwner(mainPanel),
                                                                                        probNet, veOptimalStrategy);
                optimalStrategyDialog.setVisible(true);
            } catch (NonProjectablePotentialException e) {
                throw new UnrecoverableException(e);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private NetworkEditorPanel getCurrentNetworkEditorPanel() {
        return mainPanel.getMainPanelMenuAssistant().getCurrentNetworkEditorPanel();
    }
}
