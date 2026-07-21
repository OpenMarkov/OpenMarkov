/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.common;

import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.ProbNet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

/**
 * Panel with the options/parameters for Monte Carlo Simulations.
 * There are two types of options: those which establish how the simulation is carried out and what is calculated,
 * and those which determine how this simulation is logged (log options)
 * @version 1.0 cyago - 04/01/2019 - In this version Monte Carlo options is only used for DESNets
 * @version 1.1 cyago - 24/04/2021 - Added support for an input file
 * @version 1.2 cyago - 16/08/2023 - Keeping data from previous simulation
 * @version 1.3 cyago - 23/05/2024 - PSA
 */
public class MonteCarloOptionsPanel extends JPanel implements ActionListener {

	/**
	 * ProbNet to simulate
	 */
	private ProbNet probNet;

	/**
	 * Monte Carlo options to carry out the simualtion
	 */
	private MonteCarloOptions monteCarloOptions;

//	/**
//	 * JPanel when the Monte Carlo simulation options are depicted
//	 */
//	private JPanel monteCarloOptionsPanel;

	/**
	 * Label for number of series of simulations
	 */
	private JLabel numSeriesLabel;

	/**
	 * Number of series of simulations JTextField
	 */
	private JTextField numSeriesTextField;

	/**
	 * Label for number of simulations per series
	 */
	private JLabel numSimulationsLabel;

	/**
	 * JTextField for  number of simulations per series
	 */
	private JTextField numSimulationsTextField;

	/**
	 * JCheckBox for PSA; when checked, the series are the number of simulations for the PSA
	 */
	private JCheckBox psaCheckBox;

//	/**
//	 * JCheckBox for PSA; when checked, the series are the number of simulations for the PSA
//	 */
//	private JLabel psaLabel;
//
	/**
	 * Panel for log options
	 */
	private JPanel desNetLogOptionsPanel;


	/**
	 * JCHeckBox for setting the "only summary" option.  When checked, only a summary of the simulations is recorded
	 */
	private JCheckBox resultsPerSeriesCheckBox;

	/**
	 * JCHeckBox for setting the "detailed textual log option"
	 */
	private JCheckBox textualLogCheckBox;


	//Statistics Panel
	/**
	 * Panel with the statistics options
	 */
	private JPanel statisticsPanel;
	/**
	 * JCheckBox for calculating the mean. When checked the simulations mean is computed
	 */
	private JCheckBox meanCheckBox;
	/**
	 * JCheckBox for calculating the sum. When checked the simulations sum is computed
	 */
	private JCheckBox sumCheckBox;
	/**
	 * JCheckBox for calculating the trimmed mean. When checked the simulations trimmed mean is computed
	 */
	private JCheckBox trimmedMeanCheckBox;
	/**
	 * JCheckBox for calculating the median. When checked the simulations median is computed
	 */
	private JCheckBox medianCheckBox;

	//File Panel
	/**
	 * JPanel for setting the input file
	 */
	private JPanel inputFileJPanel = null;
	/**
	 * JButton for setting the input File
	 */
	private JButton addInputFileJButton = null;

	//26/08/2023
	private JButton clearInputFileJButton = null;

	//26/08/2023 Path will be used because File is considered legacy code
	//https://www.baeldung.com/java-path-vs-file
	private Path inputFile = null;


	/**
	 * JTextField of the JButton for setting the input File
	 */
	private JTextField inputFileJTextField = null;


	/**
	 * Constructor of panel containing the monte carlo options
	 *
	 * @param probNet probNet to be simulated
	 */
	public MonteCarloOptionsPanel(ProbNet probNet) {
		super();

		this.probNet = probNet;
		monteCarloOptions = probNet.getInferenceOptions().getMonteCarloOptions().clone();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.setBorder(new TitledBorder("Monte Carlo Options"));
		JPanel numSeriesPanel = new JPanel();
		numSeriesPanel.add(getJLabelNumSeries());
		numSeriesPanel.add(getNumSeriesTextField());
		JPanel numSimulationsPanel = new JPanel();
		numSimulationsPanel.add(getPsaCheckBox());
		numSimulationsPanel.add(getJLabelNumSimulations());
		numSimulationsPanel.add(getNumSimulationsTextField());
		JPanel firstLinePanel = new JPanel();
		firstLinePanel.setLayout(new BorderLayout());
		firstLinePanel.add(numSimulationsPanel,BorderLayout.WEST);
		firstLinePanel.add(numSeriesPanel,BorderLayout.EAST);
		this.add(firstLinePanel);

		JPanel psaPanel = new JPanel();
		psaPanel.setBorder(new TitledBorder("Probabilistic Sensitivity Analysis (PSA)"));
		psaPanel.add(getPsaCheckBox());
		this.add(psaPanel);


		this.add(Box.createRigidArea(new Dimension()));
		this.add(getDesNetLogOptionsPanel());
		this.add(Box.createRigidArea(new Dimension(0,10)));
		this.add(getStatisticsPanel());
		this.add(Box.createRigidArea(new Dimension(0,10)));
		this.add(getInputFileJPanel());

	}


	/**
	 * This method delivers the monteCarloOptions set in this panel
	 * @return  the monteCarloOptions set in this panel
	 */
	public MonteCarloOptions getMonteCarloOptions() {
		this.monteCarloOptions.setNumSeries(Integer.parseInt(numSeriesTextField.getText()));
		this.monteCarloOptions.setNumSimulations(Integer.parseInt(numSimulationsTextField.getText()));
		this.monteCarloOptions.setResultsToExcel(resultsPerSeriesCheckBox.isSelected());
		this.monteCarloOptions.setTextualLog(textualLogCheckBox.isSelected());
		//23/05/ PSA
		this.monteCarloOptions.setPSA(psaCheckBox.isSelected());
		this.monteCarloOptions.setMean(meanCheckBox.isSelected());
		this.monteCarloOptions.setTrimmedMean(trimmedMeanCheckBox.isSelected());
		this.monteCarloOptions.setMedian(medianCheckBox.isSelected());
		this.monteCarloOptions.setSum(sumCheckBox.isSelected());
		//26/08/2023 - set inputFile options
		this.monteCarloOptions.setInputFilePath(inputFile);
		return monteCarloOptions;
	}



	/**
	 * This method returns the label for number of simulations
	 * @return the label for number of simulations
	 */
	private JLabel getJLabelNumSimulations() {
		if (numSimulationsLabel == null) {
			//TODO use stringDatabase
			numSimulationsLabel = new JLabel("Number of individuals");
		}
		return numSimulationsLabel;

	}


	/**
	 * This method returns the JTextField for number of simulations
	 * @return the JTextField for number of simulations
	 */
	private JTextField getNumSimulationsTextField() {

		if (numSimulationsTextField == null) {
			numSimulationsTextField = new JTextField();
			numSimulationsTextField.setText("" + this.monteCarloOptions.getNumSimulations());
			numSimulationsTextField.setColumns(10);
			numSimulationsTextField.setName("numSimulationsTextField");
		}
		return numSimulationsTextField;
	}


	/**
	 * This method returns the JTextField for number of simulations
	 * @return the JTextField for number of simulations
	 */



	/**
	 * This method returns the JLabel for number of series
	 * @return the label for number of series
	 */
	private JLabel getJLabelNumSeries() {
		if (numSeriesLabel == null) {
			//TODO use stringDatabase
			numSeriesLabel = new JLabel("Number of series");
		}
		return numSeriesLabel;

	}


	private JCheckBox getPsaCheckBox() {

		if (psaCheckBox == null) {
			psaCheckBox = new JCheckBox("Perfom PSA");
			psaCheckBox.setEnabled(false);
			psaCheckBox.setSelected(monteCarloOptions.isPsa());
//			psaCheckBox.setSelected(true);

		}
		return psaCheckBox;
	}

//	private JLabel getPsaLabel(){
//		if (psaLabel == null) {
//			//TODO use stringDatabase
//			psaLabel = new JLabel("Perform PSA");
//		}
//		return psaLabel;
//	}
//

	/**
	 * This method returns the JTextField for number of series
	 * @return the JTextField for number of series
	 */
	private JTextField getNumSeriesTextField() {

		if (numSeriesTextField == null) {
			numSeriesTextField = new JTextField();
			numSeriesTextField.setText("" + this.monteCarloOptions.getNumSeries());
			numSeriesTextField.setColumns(10);
			numSeriesTextField.setName("numSeriesTextField");
		}
		return numSeriesTextField;
	}

	/**
	 * This method returns panel for DesNet Log Options
	 * @return the label for number of series
	 */
	private JPanel getDesNetLogOptionsPanel() {
		if (desNetLogOptionsPanel == null) {
			desNetLogOptionsPanel = new JPanel();
			desNetLogOptionsPanel.setBorder( new TitledBorder("DES Inference Log Options"));
			desNetLogOptionsPanel.add(getJCheckBoxExcelResultsPerSeries());
			desNetLogOptionsPanel.add(getTextualLogCheckBox());
		}
		return desNetLogOptionsPanel;
	}



	/**
	 * Returns the JCheckBox for "Detailed Textual Log"
	 * @return the JCheckBox for "Event Log"
	 */
	private JCheckBox getTextualLogCheckBox() {
		if (textualLogCheckBox == null) {
			//TODO use stringDatabase
			textualLogCheckBox = new JCheckBox("Generate evaluation algorithm trace", monteCarloOptions.isTextualLog());
		}

		return textualLogCheckBox;
	}


	/**
	 * This method returns the JCheckBox for "Only Summary"
	 * @return the JCheckBox for "Only Summary"
	 */
	private JCheckBox getJCheckBoxExcelResultsPerSeries() {
		if (resultsPerSeriesCheckBox == null) {
			//FIXME use stringDatabase
			resultsPerSeriesCheckBox = new JCheckBox("Results Per Series (.xlsx file)",monteCarloOptions.isResultsToExcel());
		}
		//FIXME 13/01/2023 Provisional for paper .jar
		resultsPerSeriesCheckBox.setEnabled(false);
		return resultsPerSeriesCheckBox;
	}



	/**
	 * This method returns the statistics JPanel
	 * @return the statistics JPanel
	 */
	private JPanel getStatisticsPanel(){
		if (statisticsPanel == null) {
			statisticsPanel = new JPanel();
			statisticsPanel.setBorder( new TitledBorder("DES Inference Statistics"));
			statisticsPanel.add(getJCheckBoxMean());
			statisticsPanel.add(getJCheckBoxSum());
			statisticsPanel.add(getJCheckBoxTrimmedMean());
			statisticsPanel.add(getJCheckBoxMedian());
		}
		return statisticsPanel;
		
	}
	/**
	 * This method returns the JCheckBox for "Mean"
	 * @return the JCheckBox for "Mean"
	 */
	private JCheckBox getJCheckBoxMean() {
		if (meanCheckBox == null) {
			//TODO use stringDatabase
			meanCheckBox = new JCheckBox("Mean", monteCarloOptions.isMean());
			meanCheckBox.setEnabled(true);
			meanCheckBox.setSelected(true);		
		}
		return meanCheckBox;
	}

	/**
	 * This method returns the JCheckBox for "Sum"
	 * @return the JCheckBox for "Only Summary"
	 */
	private JCheckBox getJCheckBoxSum() {
		if (sumCheckBox == null) {
			//TODO use stringDatabase
			sumCheckBox = new JCheckBox("Sum", monteCarloOptions.isMean());
			sumCheckBox.setEnabled(false);
			sumCheckBox.setSelected(false);
		}
		return sumCheckBox;
	}

	/**
	 * This method returns the JCheckBox for "Trimmed Mean"
	 * @return the JCheckBox for "Trimmed Mean"
	 */
	private JCheckBox getJCheckBoxTrimmedMean() {
		if (trimmedMeanCheckBox == null) {
			//TODO use stringDatabase
			trimmedMeanCheckBox = new JCheckBox("Trimmed Mean", monteCarloOptions.isMean());
			trimmedMeanCheckBox.setEnabled(false);
			trimmedMeanCheckBox.setSelected(false);
		}
		return trimmedMeanCheckBox;
	}

	/**
	 * This method returns the JCheckBox for "Median"
	 * @return the JCheckBox for "Median"
	 */
	private JCheckBox getJCheckBoxMedian() {
		if (medianCheckBox == null) {
			//TODO use stringDatabase
			medianCheckBox = new JCheckBox("Median", monteCarloOptions.isMean());
			medianCheckBox.setEnabled(false);
			medianCheckBox.setSelected(false);
		}
		return medianCheckBox;
	}


	/**
	 * This method returns the JPanel for the simulation input file
	 * @return the JPanel for the simulation input file
	 */
	private JPanel getInputFileJPanel(){
		if (inputFileJPanel == null) {
			inputFile = monteCarloOptions.getInputFilePath();
			inputFileJPanel = new JPanel();
			inputFileJPanel.setBorder( new TitledBorder("DES Input File"));
			inputFileJPanel.setLayout(new BorderLayout(0,5));
			inputFileJPanel.add(getJButtonAddInputFile(), BorderLayout.NORTH);
			inputFileJPanel.add(getJTextFieldInputFile(), BorderLayout.EAST);
			inputFileJPanel.add(getJButtonClearInputFile(), BorderLayout.WEST);

		}
		return inputFileJPanel;
	}

	/**
	 * This method returns the JButton for selecting the simulation input file
	 * @return the JButton to select the simulation input file
	 */
	private JButton getJButtonAddInputFile() {
		if (addInputFileJButton ==null) {
			addInputFileJButton = new JButton("Add Input File");
			addInputFileJButton.setActionCommand("AddInputFile");
			addInputFileJButton.addActionListener(this);
		}
		return addInputFileJButton;
	}


	/**
	 * This method returns the JButton for clearing the JText with the input data file
	 * @return the JButton to clear input file name
	 */
	private JButton getJButtonClearInputFile() {
		if (clearInputFileJButton  ==null) {
			clearInputFileJButton = new JButton("Clear");
			clearInputFileJButton.setActionCommand("ClearInputFile");
			clearInputFileJButton.addActionListener(this);
			if (inputFile == null) clearInputFileJButton.setEnabled(false);
		}
		return clearInputFileJButton;
	}




	/**
	 * This method returns the JTextField for selecting the simulation input file
	 * @return the JTextField to select the simulation input file
	 */
	private JTextField getJTextFieldInputFile() {
		if (inputFileJTextField ==null){
			inputFileJTextField = new JTextField();
//			inputFileJTextField.setVisible(false);
			inputFileJTextField.setEnabled(false);
			if (inputFile == null){
				inputFileJTextField.setText("");
			} else {
				inputFileJTextField.setText(inputFile.toString());
			}

		}
		return inputFileJTextField;
	}

	/**
	 * Action for inputFileJButton. Selects the input file, stores it in monteCarloOptions and write the name in the panel
	 * @param e actionEvent for inputFileButton
	 */
	public void actionPerformed(ActionEvent e) {

		JFileChooser fileChooser = new JFileChooser(".\\DESNetFiles\\InputFile") ;

		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.csv", "csv");
		fileChooser.setFileFilter(filter);

		//Handle open button action.
		if (e.getActionCommand() == "AddInputFile") {
			int returnVal = fileChooser.showOpenDialog(MonteCarloOptionsPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				inputFile= fileChooser.getSelectedFile().toPath();
				inputFileJTextField.setText(inputFile.toString());
				clearInputFileJButton.setEnabled(true);

			}
		}

		if (e.getActionCommand() == "ClearInputFile") {
			inputFile = null;
			inputFileJTextField.setText("");
			clearInputFileJButton.setEnabled(false);

		}
	}


}
