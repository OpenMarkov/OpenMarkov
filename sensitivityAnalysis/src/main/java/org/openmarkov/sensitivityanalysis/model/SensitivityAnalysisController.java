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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
			// Gets the named uncertain parameters of the network
			LinkedHashMap<String, UncertainParameter> named = byLabel(listUncertainParameters);
			this.uncertainParameters.putAll(named);
			orderedUncertainParametersKeys = new ArrayList<>(named.keySet());
		}
	}

	/**
	 * The named parameters, in their order, by the name the analysis shows. Two parameters with the same name, which
	 * a file can bring, are told apart by the node of their relation: the second used to replace the first, which
	 * could not be analysed, and the tornado failed on the repeated name (error 308).
	 */
	public static LinkedHashMap<String, UncertainParameter> byLabel(List<UncertainParameter> parameters) {
		Map<String, Long> timesOfEachName = parameters.stream()
				.filter(UncertainParameter::hasName)
				.collect(Collectors.groupingBy(UncertainParameter::getName, Collectors.counting()));
		LinkedHashMap<String, UncertainParameter> byLabel = new LinkedHashMap<>();
		for (UncertainParameter parameter : parameters) {
			if (!parameter.hasName()) {
				continue;
			}
			String label = timesOfEachName.get(parameter.getName()) == 1
					? parameter.getName()
					: parameter.getName() + " [" + parameter.getPotential().getVariable(0).getName() + "]";
			// Still repeated within the same node
			String uniqueLabel = label;
			for (int copy = 2; byLabel.containsKey(uniqueLabel); copy++) {
				uniqueLabel = label + " (" + copy + ")";
			}
			parameter.setLabel(uniqueLabel);
			byLabel.put(uniqueLabel, parameter);
		}
		return byLabel;
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
	 * Runs the analysis with the model parameters and {@code evidence} as the findings before the resolution, and
	 * shows the results in a dialog
	 */
    public void runAnalysis(EvidenceCase evidence) throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NotSupportedOperationException, ConstraintViolatedException {
		JDialog sensitivityAnalysisResultsDialog = null;
		AnalysisType sensitivityAnalysisType = sensitivityAnalysisModel.getAnalysisType();
        if (sensitivityAnalysisType == AnalysisType.TORNADO_SPIDER) {
			sensitivityAnalysisResultsDialog = new TornadoSpiderDialog(owner, probNet, evidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.PLOT) {
			sensitivityAnalysisResultsDialog = new PlotDialog(owner, probNet, evidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.MAP) {
			sensitivityAnalysisResultsDialog = new MapDialog(owner, probNet, evidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.CEPLANE) {
			sensitivityAnalysisResultsDialog = new CEProbabilisticDialog(owner, probNet, evidence,
					sensitivityAnalysisModel);
        } else if (sensitivityAnalysisType == AnalysisType.SPIDER_CE) {
			sensitivityAnalysisResultsDialog = new CESpiderDialog(owner, probNet, evidence,
					sensitivityAnalysisModel);
		}
		this.sensitivityAnalysisPlots.add(sensitivityAnalysisResultsDialog);
	}

	/** Reads again the pre-resolution evidence that the network of the window has now. */
	public void readPreResolutionEvidenceFromNetwork() {
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
