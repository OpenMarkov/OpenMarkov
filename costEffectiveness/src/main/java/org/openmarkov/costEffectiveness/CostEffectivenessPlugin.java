package org.openmarkov.costEffectiveness;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.dialog.common.OkCancelDialog;
import org.openmarkov.gui.dialog.costeffectiveness.CEPDialog;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.gui.dialog.inference.common.ScopeSelectorPanel;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis.DANDecompositionIntoSymmetricDANsCEA;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;

import javax.swing.*;

/**
 * Tool plugin that registers the cost-effectiveness analysis option in the Tools menu.
 * Supports Influence Diagrams, Multi-criteria IDs, and Decision Analysis Networks.
 */
public final class CostEffectivenessPlugin implements ToolPlugin {

    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.ANALYSIS;
    }

    @Override public int priorityInGroup() {
        return 0;
    }

    /**
     * Checks whether this plugin should be enabled based on the presence of an open network.
     *
     * @return {@code true} if a network is currently open, {@code false} otherwise
     */
    public boolean enabled() {
        return MainPanel.getCurrentProbNet() != null;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder(StringDatabase.getUniqueInstance().getString("Menus", "Tools.CostEffectiveness"))
                .enabled(MainPanel.getCurrentProbNet() != null)
                .onClick(CostEffectivenessPlugin::onClick)
                .build();
    }
    
    private static void onClick() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, ConstraintViolatedException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        JFrame parent = MainGUI.INSTANCE.mainPanel.getMainFrame();
        ProbNet probNet = MainPanel.getCurrentProbNet();
        EvidenceCase preResolutionEvidence = MainGUI.INSTANCE.mainPanel
                                                      .getMainPanelMenuAssistant()
                                                      .getCurrentNetworkEditorPanel()
                                                      .getEditorPanel()
                .getEvidenceManager()
                                                      .getPreResolutionEvidence();
        InferenceOptionsDialog inferenceOptionsDialog =
                new InferenceOptionsDialog(probNet, parent, MulticriteriaOptions.Type.COST_EFFECTIVENESS);
        if (inferenceOptionsDialog.getSelectedOption() != OkCancelDialog.ChosenOption.Ok) {
            return;
        }
        CostEffectivenessDialog costEffectivenessDialog = new CostEffectivenessDialog(parent, probNet, preResolutionEvidence);
        if ((costEffectivenessDialog.requestData() != OkCancelDialog.ChosenOption.Ok)) {
            return;
        }
        ScopeSelectorPanel scopeSelectorPanel = costEffectivenessDialog.getScopeSelectorPanel();
        JDialog dialog = switch (scopeSelectorPanel.getScopeType()) {
            case DECISION -> {
                EvidenceCase newPreResolutionEvidence = new EvidenceCase(preResolutionEvidence);
                for (Finding finding : scopeSelectorPanel.getSelectedFindings()) {
                    newPreResolutionEvidence.addFinding(finding);
                }
                yield new CEDecisionResults(parent, probNet, newPreResolutionEvidence,
                                            scopeSelectorPanel.getDecisionSelected());
            }
            case GLOBAL -> {
                CEP cep = switch (probNet.getNetworkType()) {
                    case NetworkType networkType when networkType instanceof InfluenceDiagramType || networkType instanceof MIDType -> {
                        try {
                            VECEAnalysis veGlobalCEA = new VECEAnalysis(probNet);
                            veGlobalCEA.setPreResolutionEvidence(preResolutionEvidence);
                            yield (CEP) veGlobalCEA.getUtility().elementTable.get(0);
                        } catch (IncompatibleEvidenceException e) {
                            throw new UnreachableException(e);
                        }
                    }
                    case null, default -> {
                        DANDecompositionIntoSymmetricDANsCEA decompositionAlgorithmArticleCEA = new DANDecompositionIntoSymmetricDANsCEA(
                                probNet, preResolutionEvidence);
                        yield (CEP) decompositionAlgorithmArticleCEA.getUtility().elementTable.get(0);
                    }
                };
                yield new CEPDialog(parent, cep, probNet);
            }
        };
        dialog.setVisible(true);
    }
}
