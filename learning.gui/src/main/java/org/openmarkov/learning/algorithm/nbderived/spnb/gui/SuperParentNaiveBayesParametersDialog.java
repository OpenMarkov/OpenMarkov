package org.openmarkov.learning.algorithm.nbderived.spnb.gui;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.nbderived.spnb.SuperParentNBAlgorithm;
import org.openmarkov.learning.metric.annotation.MetricManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

import static org.openmarkov.learning.algorithm.nbderived.common.util.CommonUtils.getStringFromCamelCaseExpression;

/**
 * Dialog showing the options and parameters of the Super Parent Naive Bayes (SPNB) algorithm.
 *
 * @author Manuel Arias
 */
@AlgorithmConfiguration(algorithm = SuperParentNBAlgorithm.class)
public class SuperParentNaiveBayesParametersDialog extends AlgorithmParametersDialog {

    private final MetricManager metricManager;
    private final JCheckBox sameSPCheckbox;

    public SuperParentNaiveBayesParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        metricManager = new MetricManager();

        JLabel sameSPLabel = new JLabel(stringDatabase.getString("Learning.SuperParentNaiveBayes.SameSP") + ":");
        sameSPLabel.setToolTipText(stringDatabase.getString("Learning.SuperParentNaiveBayes.SameSP.Tooltip"));
        sameSPCheckbox = new JCheckBox();
        sameSPCheckbox.setSelected(true);

        initStandardLayout("Learning.SuperParentNaiveBayes.Title",
                new FieldRow(sameSPLabel, sameSPCheckbox));
    }

    @Override
    public String getDescription() {
        return stringDatabase.getString("Learning.SuperParentNaiveBayes.Metric") + ": "
                + getStringFromCamelCaseExpression(algorithmMetrics()[0]) + System.lineSeparator()
                + stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter;
    }

    @Override
    public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        return new SuperParentNBAlgorithm(probNet, database,
                metricManager.createInstance(algorithmMetrics()[0]),
                Double.parseDouble(alphaParameter),
                sameSPCheckbox.isSelected());
    }
}
