package org.openmarkov.learning.algorithm.nbderived.snb.gui;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.nbderived.snb.SelectiveNBAlgorithm;
import org.openmarkov.learning.metric.annotation.MetricManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

import static org.openmarkov.learning.algorithm.nbderived.common.util.CommonUtils.getStringFromCamelCaseExpression;

/**
 * Dialog showing the options and parameters of the Selective Naive Bayes algorithm.
 *
 * @author Manuel Arias
 */
@AlgorithmConfiguration(algorithm = SelectiveNBAlgorithm.class)
public class SelectiveNBParametersDialog extends AlgorithmParametersDialog {

    private final MetricManager metricManager;
    private final JCheckBox forwardCheckbox;

    public SelectiveNBParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        metricManager = new MetricManager();

        JLabel forwardLabel = new JLabel(stringDatabase.getString("Learning.SelectiveNaiveBayes.Forward") + ":");
        forwardCheckbox = new JCheckBox();
        forwardCheckbox.setSelected(true);

        initStandardLayout("Learning.SelectiveNaiveBayes.Title",
                new FieldRow(forwardLabel, forwardCheckbox));
    }

    @Override
    public String getDescription() {
        return stringDatabase.getString("Learning.SelectiveNaiveBayes.Metric") + ": "
                + getStringFromCamelCaseExpression(algorithmMetrics()[0]) + System.lineSeparator()
                + stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter;
    }

    @Override
    public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        return new SelectiveNBAlgorithm(probNet, database,
                metricManager.createInstance(algorithmMetrics()[0]),
                Double.parseDouble(alphaParameter),
                forwardCheckbox.isSelected());
    }
}
