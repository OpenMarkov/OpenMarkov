package org.openmarkov.learning.algorithm.nbderived.treeaugmentednb.gui;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.metric.annotation.MetricManager;
import org.openmarkov.learning.algorithm.nbderived.treeaugmentednb.TreeAugmentedNBAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

import static org.openmarkov.learning.algorithm.nbderived.common.util.CommonUtils.getStringFromCamelCaseExpression;

/**
 * Dialog showing the options and parameters of the Tree Augmented Naive Bayes (TAN) algorithm.
 *
 * @author Manuel Arias
 */
@AlgorithmConfiguration(algorithm = TreeAugmentedNBAlgorithm.class)
public class TreeAugmentedNBParametersDialog extends AlgorithmParametersDialog {

    private final MetricManager metricManager;

    public TreeAugmentedNBParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        metricManager = new MetricManager();
        initStandardLayout("Learning.TreeAugmentedNaiveBayes.Title");
    }

    @Override
    public String getDescription() {
        return stringDatabase.getString("Learning.TreeAugmentedNaiveBayes.Metric") + ": "
                + getStringFromCamelCaseExpression(algorithmMetrics()[0]) + System.lineSeparator()
                + stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter;
    }

    @Override
    public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        return new TreeAugmentedNBAlgorithm(probNet, database,
                metricManager.createInstance(algorithmMetrics()[0]),
                Double.parseDouble(alphaParameter));
    }
}
