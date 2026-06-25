package org.openmarkov.bnEvaluation.component;

import org.openmarkov.bnEvaluation.Coherence;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Reusable panel containing checkboxes for selecting evaluation measures
 * (confusion matrix, log-likelihood, and various score metrics).
 */
public class MeasuresPanel extends JPanel {
    
    private JCheckBox CMMeasuresCheck;
    private JCheckBox showIndProbCheckBox;
    private JComboBox<String> existingVariablesComboBox;
    private JCheckBox logLikelihoodMeasureCheck;
    private JCheckBox scoreMeasuresCheck;
    
    private JCheckBox BayesianMetricCheck;
    private JCheckBox AicMetricCheck;
    private JCheckBox EntropyMetricCheck;
    private JCheckBox BdeMetricCheck;
    private JCheckBox K2MetricCheck;
    private JCheckBox MDLMetricCheck;
    
    public MeasuresPanel() {
        initialize();
    }
    
    /**
     * Gets the Panel with the options to select the measures (classification variable...)
     *
     * @return JPanel
     */
    private JPanel initialize() {
        this.setBorder(BorderFactory.createTitledBorder("Measures"));
        this.setPreferredSize(new Dimension(840, 220));
        
        // create components
        CMMeasuresCheck = new JCheckBox("Predictive capacity");
        JLabel clasVariableLabel = new JLabel("Classification variable");
        showIndProbCheckBox = new JCheckBox("Show individual probabilities");
        existingVariablesComboBox = new JComboBox<>();
        existingVariablesComboBox.addItem("select variable");
        logLikelihoodMeasureCheck = new JCheckBox("Goodness of fit Log-Likelihood measures");
        scoreMeasuresCheck = new JCheckBox("Score measures:");
        BayesianMetricCheck = new JCheckBox("Bayesian");
        AicMetricCheck = new JCheckBox("AIC");
        EntropyMetricCheck = new JCheckBox("Entropy");
        BdeMetricCheck = new JCheckBox("BDe");
        K2MetricCheck = new JCheckBox("K2");
        MDLMetricCheck = new JCheckBox("MDL");
        
        
        CMMeasuresCheck.addActionListener(e -> CMMeasuresCheckActionPerformed());
        scoreMeasuresCheck.addActionListener(e -> this.metricsCheckboxes().forEach(metricCheckboxes -> {
            metricCheckboxes.setSelected(scoreMeasuresCheck.isSelected());
        }));
        this.metricsCheckboxes().forEach(metricCheckboxes -> {
            metricCheckboxes.addChangeListener(e -> {
                if (this.metricsCheckboxes().allMatch(AbstractButton::isSelected)) {
                    scoreMeasuresCheck.setSelected(true);
                }
                if (this.metricsCheckboxes().noneMatch(AbstractButton::isSelected)) {
                    scoreMeasuresCheck.setSelected(false);
                }
            });
        });
        
        // add components to measuresPanel with a GroupLayout
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                                        .addGap(10)
                                        .addGroup(layout.createParallelGroup()
                                                        .addComponent(CMMeasuresCheck)
                                                        .addGroup(layout.createSequentialGroup()
                                                                        .addGap(20)
                                                                        .addComponent(clasVariableLabel))
                                                        .addComponent(logLikelihoodMeasureCheck)
                                                        .addComponent(scoreMeasuresCheck)
                                                        .addGroup(layout.createSequentialGroup()
                                                                        .addGap(20)
                                                                        .addComponent(EntropyMetricCheck))
                                                        .addGroup(layout.createSequentialGroup()
                                                                        .addGap(20)
                                                                        .addComponent(AicMetricCheck))
                                                        .addGroup(layout.createSequentialGroup()
                                                                        .addGap(20)
                                                                        .addComponent(BayesianMetricCheck)))
                                        .addGroup(layout.createParallelGroup()
                                                        .addComponent(existingVariablesComboBox, 250, 250, 250)
                                                        .addComponent(BdeMetricCheck)
                                                        .addComponent(K2MetricCheck)
                                                        .addComponent(MDLMetricCheck))
                                        .addGap(20)
                                        .addGroup(layout.createParallelGroup()
                                                        .addGap(20)
                                                        .addComponent(showIndProbCheckBox))
                                        .addGap(20));
        layout.setVerticalGroup(layout.createSequentialGroup()
                                      .addComponent(CMMeasuresCheck)
                                      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                      .addComponent(clasVariableLabel)
                                                      .addComponent(existingVariablesComboBox, 20, 20, 20)
                                                      .addComponent(showIndProbCheckBox))
                                      .addGap(5)
                                      .addComponent(logLikelihoodMeasureCheck)
                                      .addGap(5)
                                      .addComponent(scoreMeasuresCheck)
                                      .addGroup(layout.createParallelGroup()
                                                      .addComponent(BayesianMetricCheck)
                                                      .addComponent(BdeMetricCheck))
                                      .addGroup(layout.createParallelGroup()
                                                      .addComponent(EntropyMetricCheck)
                                                      .addComponent(K2MetricCheck))
                                      .addGroup(layout.createParallelGroup()
                                                      .addComponent(AicMetricCheck)
                                                      .addComponent(MDLMetricCheck)));
        return this;
    }
    
    /**
     * This method is the listener of CheckBox: CMMeasuresCheck
     */
    private void CMMeasuresCheckActionPerformed() {
        existingVariablesComboBox.setEnabled(CMMeasuresCheck.isSelected());
        showIndProbCheckBox.setEnabled(CMMeasuresCheck.isSelected());
    }
    
    /**
     * Returns a stream of the individual score metric checkboxes.
     *
     * @return stream of score metric checkboxes
     */
    public Stream<JCheckBox> metricsCheckboxes() {
        return Stream.of(BayesianMetricCheck, AicMetricCheck, EntropyMetricCheck, BdeMetricCheck, K2MetricCheck, MDLMetricCheck);
    }
    
    /**
     * Returns a stream of all measure-related checkboxes (confusion matrix, log-likelihood, and all scores).
     *
     * @return stream of all measure checkboxes
     */
    public Stream<JCheckBox> allMeasureRelatedCheckboxes() {
        return Stream.of(CMMeasuresCheck,
                         logLikelihoodMeasureCheck,
                         scoreMeasuresCheck,
                         BayesianMetricCheck, AicMetricCheck, EntropyMetricCheck, BdeMetricCheck, K2MetricCheck, MDLMetricCheck);
    }
    
    /**
     * Updates the classification variable combo box with the given variable names.
     *
     * @param variables the variable names to populate in the combo box
     */
    public void changeVariables(Collection<String> variables) {
        existingVariablesComboBox.removeAllItems();
        variables.forEach(existingVariablesComboBox::addItem);
    }
    
    /**
     * Builds a {@link MeasuresSet} based on the currently selected checkboxes and options.
     *
     * @param title       descriptive title for the evaluation
     * @param coherence   the coherence level between network and database
     * @param netdatabase the case database aligned to the network variables
     * @return a configured {@link MeasuresSet} ready for evaluation
     */
    public MeasuresSet measuresSet(String title, Coherence coherence, CaseDatabase netdatabase) {
        MeasuresSet measuresSet = new MeasuresSet(title);
        if (coherence == Coherence.WEAK) {
            measuresSet.setNotAllVariablesAreUsed();
        }
        if (CMMeasuresCheck.isSelected()) {
            // the variables in netdatabase are the same variables of probNet
            String selectedVariableName = existingVariablesComboBox.getSelectedItem().toString();
            Variable var = netdatabase.getVariable(selectedVariableName);
            State[] states = var.getStates();
            String[] statesNames = new String[states.length];
            for (int i = 0; i < states.length; i++) {
                statesNames[i] = states[i].toString();
            }
            MeasureMatrix measureCM = new MeasureMatrix(MeasureType.CONFUSIONMATRIX,
                                                        statesNames, var.getName());
            if (showIndProbCheckBox.isSelected()) measureCM.setShowIndividualProb();
            measuresSet.addMeasureMatrix(measureCM);
        }
        if (logLikelihoodMeasureCheck.isSelected()) {
            MeasureValue measure = new MeasureValue(MeasureType.LOGLIKELIHOOD);
            measuresSet.addMeasureValue(measure);
        }
        if (BayesianMetricCheck.isSelected()) {
            measuresSet.addMeasureValue(new MeasureValue(MeasureType.BAYES));
        }
        if (AicMetricCheck.isSelected()) {
            measuresSet.addMeasureValue(new MeasureValue(MeasureType.AIC));
        }
        if (EntropyMetricCheck.isSelected()) {
            measuresSet.addMeasureValue(new MeasureValue(MeasureType.ENTROPY));
        }
        if (BdeMetricCheck.isSelected()) {
            measuresSet.addMeasureValue(new MeasureValue(MeasureType.BDE));
        }
        if (K2MetricCheck.isSelected()) {
            measuresSet.addMeasureValue(new MeasureValue(MeasureType.K2));
        }
        if (MDLMetricCheck.isSelected()) {
            measuresSet.addMeasureValue(new MeasureValue(MeasureType.MDL));
        }
        return measuresSet;
    }
    
    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.allMeasureRelatedCheckboxes().forEach(checkbox -> checkbox.setEnabled(true));
        } else {
            for (Component component : this.getComponents()) {
                component.setEnabled(false);
            }
        }
    }
    
    
}
