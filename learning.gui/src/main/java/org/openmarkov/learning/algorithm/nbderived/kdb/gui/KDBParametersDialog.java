package org.openmarkov.learning.algorithm.nbderived.kdb.gui;

import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.nbderived.kdb.KDBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.common.util.CommonUtils;
import org.openmarkov.learning.metric.annotation.MetricManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

/**
 * Dialog showing the options and parameters of the K-Dependence Bayesian (KDB) algorithm.
 *
 * @author Manuel Arias
 */
@AlgorithmConfiguration(algorithm = KDBAlgorithm.class)
public class KDBParametersDialog extends AlgorithmParametersDialog {

    private int kValue = 0;
    private final MetricManager metricManager;
    private final JTextField kValueField;

    public KDBParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        metricManager = new MetricManager();

        JLabel kValueLabel = new JLabel(stringDatabase.getString("Learning.KDB.kValue") + ":");
        kValueLabel.setToolTipText(stringDatabase.getString("Learning.KDB.kValue.Tooltip"));
        kValueField = new JTextField(String.valueOf(kValue), 5);

        initStandardLayout("Learning.KDB.Title",
                new FieldRow(kValueLabel, kValueField));
    }

    @Override
    protected void acceptCustomFields() {
        Double k;
        try {
            k = Double.parseDouble(kValueField.getText());
        } catch (NumberFormatException e) {
            k = null;
        }
        if (k == null || k < 0) {
            throw new InvalidArgumentException(k, "k", "must be higher or equal to 0");
        }
        kValue = Integer.parseInt(kValueField.getText());
    }

    @Override
    public String getDescription() {
        String[] metrics = algorithmMetrics();
        return stringDatabase.getString("Learning.KDB.Metrics") + ": "
                + CommonUtils.getStringFromCamelCaseExpression(metrics[1]) + ", "
                + CommonUtils.getStringFromCamelCaseExpression(metrics[0]) + System.lineSeparator()
                + stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter + System.lineSeparator()
                + stringDatabase.getString("Learning.KDB.kValue") + ": " + kValue;
    }

    @Override
    public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        String[] metrics = algorithmMetrics();
        return new KDBAlgorithm(probNet, database,
                metricManager.createInstance(metrics[0]),
                metricManager.createInstance(metrics[1]),
                Double.parseDouble(alphaParameter), kValue);
    }
}
