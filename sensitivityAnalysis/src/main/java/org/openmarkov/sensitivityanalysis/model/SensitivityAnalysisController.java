/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.model;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.sensitivityanalysis.dialog.CEProbabilisticDialog;
import org.openmarkov.sensitivityanalysis.dialog.CESpiderDialog;
import org.openmarkov.sensitivityanalysis.dialog.MapDialog;
import org.openmarkov.sensitivityanalysis.dialog.PlotDialog;
import org.openmarkov.sensitivityanalysis.dialog.TornadoSpiderDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller of the sensitivity analysis
 *
 * @author jperez-martin
 */
public class SensitivityAnalysisController {

	/**
	 * Sensitivity analysis model
	 */
	private SensitivityAnalysisModel sensitivityAnalysisModel;

	/**
	 * Configuration of the selected analysis type
	 */
	private SensitivityAnalysisConfiguration configuration;

	/**
	 * Network
	 */
	private ProbNet probNet;

	/**
	 * Pre-resolution evidence in the network
	 */
	private EvidenceCase preResolutionEvidence;

	/**
	 * Uncertain parameters of the network
	 */
	private HashMap<String, UncertainParameter> uncertainParameters;

	/**
	 * Ordered names of uncertain parameters
	 */
	private List<String> orderedUncertainParametersKeys;

	/**
	 * Launched analysis dialogs
	 */
	private List<JDialog> sensitivityAnalysisPlots;

	/**
	 * Owner window
	 */
	private Window owner;

	/**
	 * Controller constructor
	 *
	 * @param owner owner windos
	 */
	public SensitivityAnalysisController(Window owner) {
		this.owner = owner;
		this.sensitivityAnalysisModel = new SensitivityAnalysisModel();
		this.configuration = new SensitivityAnalysisConfiguration();
		this.uncertainParameters = new HashMap<>();
		this.sensitivityAnalysisPlots = new ArrayList<>();
        
        boolean isOpenNet = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel()
				!= null;
		if (isOpenNet) {
            this.probNet = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel()
                                                     .getProbNet();
            this.preResolutionEvidence = new EvidenceCase(
                    MainGUI.INSTANCE.mainPanel.getMainPanelMenuAssistant().getCurrentNetworkEditorPanel().getEditorPanel()
                                              .getEvidenceManager().getPreResolutionEvidence());
            this.configuration.setIsUnicriterion(this.probNet.getInferenceOptions()
                                                             .getMultiCriteriaOptions()
                                                             .getMulticriteriaType() == MulticriteriaOptions.Type.UNICRITERION);

			// Get uncertain Parameters from the ProbNet
			List<UncertainParameter> listUncertainParameters = SystematicSampling
					.getUncertainParameters(this.probNet);
			orderedUncertainParametersKeys = new ArrayList<>();

			// Gets the named uncertain parameters of the network
			int unnamedParameter = 1;
			for (UncertainParameter uncertainParameter : listUncertainParameters) {
				String parameterName = uncertainParameter.getName();
				if (parameterName != null && !parameterName.isEmpty()) {
					this.uncertainParameters.put(parameterName, uncertainParameter);
					orderedUncertainParametersKeys.add(parameterName);
				}
			}
		}
	}

	public SensitivityAnalysisModel getSensitivityAnalysisModel() {
		return sensitivityAnalysisModel;
	}

	public SensitivityAnalysisConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SensitivityAnalysisConfiguration configuration) {
		this.configuration = configuration;
	}

	public ProbNet getProbNet() {
		return probNet;
	}

	public void setProbNet(ProbNet probNet) {
		this.probNet = probNet;
	}

	public HashMap<String, UncertainParameter> getUncertainParameters() {
		return uncertainParameters;
	}

	/**
	 * Run the analysis with the model parameters and shows the results in a dialog
	 */
    public void runAnalysis() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NotSupportedOperationException, ConstraintViolatedException {
		JDialog sensitivityAnalysisResultsDialog = null;
		AnalysisType sensitivityAnalysisType = sensitivityAnalysisModel.getAnalysisType();
        if (sensitivityAnalysisType == AnalysisType.TORNADO_SPIDER) {
			sensitivityAnalysisResultsDialog = new TornadoSpiderDialog(owner, probNet, preResolutionEvidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.PLOT) {
			sensitivityAnalysisResultsDialog = new PlotDialog(owner, probNet, preResolutionEvidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.MAP) {
			sensitivityAnalysisResultsDialog = new MapDialog(owner, probNet, preResolutionEvidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.CEPLANE) {
			sensitivityAnalysisResultsDialog = new CEProbabilisticDialog(owner, probNet, preResolutionEvidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.SPIDER_CE) {
			sensitivityAnalysisResultsDialog = new CESpiderDialog(owner, probNet, preResolutionEvidence,
					sensitivityAnalysisModel);
		}
		this.sensitivityAnalysisPlots.add(sensitivityAnalysisResultsDialog);

		// Clean preResolutionEvidence of the controller
        this.preResolutionEvidence = new EvidenceCase(
                MainGUI.INSTANCE.mainPanel.getMainPanelMenuAssistant().getCurrentNetworkEditorPanel().
                                          getEditorPanel().getEvidenceManager().getPreResolutionEvidence());
	}

	public EvidenceCase getPreResolutionEvidence() {
		return this.preResolutionEvidence;
	}

	public void setPreResolutionEvidence(EvidenceCase preResolutionEvidence) {
		this.preResolutionEvidence = preResolutionEvidence;
	}

	/**
	 * Close all open plots
	 */
	public void closeAllPlots() {
		for (JDialog sensitivityAnalysisPlot : sensitivityAnalysisPlots) {
			sensitivityAnalysisPlot.dispose();
		}
	}

	public List<String> getOrderedUncertainParametersKeys() {
		return orderedUncertainParametersKeys;
	}
}
