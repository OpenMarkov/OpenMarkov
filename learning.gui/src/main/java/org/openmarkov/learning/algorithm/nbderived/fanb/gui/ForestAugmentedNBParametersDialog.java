package org.openmarkov.learning.algorithm.nbderived.fanb.gui;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.nbderived.fanb.ForestAugmentedNBAlgorithm;
import org.openmarkov.learning.metric.annotation.MetricManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

import static org.openmarkov.learning.algorithm.nbderived.common.util.CommonUtils.getStringFromCamelCaseExpression;

/**
 * Dialog showing the options and parameters of the Forest Augmented Naive Bayes (FAN) algorithm.
 *
 * @author Manuel Arias
 */
@AlgorithmConfiguration(algorithm = ForestAugmentedNBAlgorithm.class)
public class ForestAugmentedNBParametersDialog extends AlgorithmParametersDialog {

    private final MetricManager metricManager;

    public ForestAugmentedNBParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        metricManager = new MetricManager();
        initStandardLayout("Learning.FANB.Title");
    }

    @Override
    public String getDescription() {
        String[] metrics = algorithmMetrics();
        return stringDatabase.getString("Learning.FANB.Metrics") + ": "
                + getStringFromCamelCaseExpression(metrics[1]) + ", "
                + getStringFromCamelCaseExpression(metrics[0]) + System.lineSeparator()
                + stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter;
    }

    @Override
    public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        String[] metrics = algorithmMetrics();
        return new ForestAugmentedNBAlgorithm(probNet, database,
                metricManager.createInstance(metrics[0]),
                metricManager.createInstance(metrics[1]),
                Double.parseDouble(alphaParameter));
    }
}
