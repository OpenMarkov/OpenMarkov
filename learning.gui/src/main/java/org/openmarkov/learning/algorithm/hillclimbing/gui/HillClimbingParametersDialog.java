/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.algorithm.hillclimbing.gui;

import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.hillclimbing.HillClimbingAlgorithm;
import org.openmarkov.learning.metric.annotation.MetricManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;
import java.util.Set;

/**
 * Dialog showing the options and parameters of the Hill Climbing algorithm.
 *
 * @author joliva
 * @author Manuel Arias
 */
@AlgorithmConfiguration(algorithm = HillClimbingAlgorithm.class)
public class HillClimbingParametersDialog extends AlgorithmParametersDialog {

    private final MetricManager metricManager;
    private String metric;
    private int maxNumParents = 5;
    private final JComboBox<String> metricComboBox;
    private final JTextField maxNumParentsField;

    public HillClimbingParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        metricManager = new MetricManager();

        JLabel metricLabel = new JLabel(stringDatabase.getString("Learning.HillClimbing.Metric"));
        metricComboBox = new JComboBox<>();
        Set<String> metricNames = metricManager.getAllMetricNames();
        metricComboBox.setModel(new DefaultComboBoxModel<>(metricNames.toArray(new String[0])));
        if (metricNames.contains("K2")) metricComboBox.setSelectedItem("K2");
        metric = metricComboBox.getSelectedItem().toString();

        JLabel maxNumParentsLabel = new JLabel(
                stringDatabase.getString("Learning.HillClimbing.MaxNumParents") + ":");
        maxNumParentsLabel.setToolTipText(
                stringDatabase.getString("Learning.HillClimbing.MaxNumParents.Tooltip"));
        maxNumParentsField = new JTextField(String.valueOf(maxNumParents), 5);

        initStandardLayout("Learning.HillClimbing.Title",
                new FieldRow(metricLabel, metricComboBox),
                new FieldRow(maxNumParentsLabel, maxNumParentsField));
    }

    @Override
    protected void acceptCustomFields() {
        metric = (String) metricComboBox.getSelectedItem();
        Integer parsed;
        try {
            parsed = Integer.parseInt(maxNumParentsField.getText().trim());
        } catch (NumberFormatException e) {
            parsed = null;
        }
        if (parsed == null || parsed < 0) {
            throw new InvalidArgumentException(parsed, "maxNumParents",
                    "must be an integer greater or equal to 0 (0 = unlimited)");
        }
        maxNumParents = parsed;
    }

    @Override public String getDescription() {
        return stringDatabase.getString("Learning.HillClimbing.Metric") + ": " + metric + "\r\n"
                + stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter + "\r\n"
                + stringDatabase.getString("Learning.HillClimbing.MaxNumParents") + ": "
                + (maxNumParents == 0 ? "∞" : maxNumParents);
    }

    public String getMetric() {
        return metric;
    }

    public int getMaxNumParents() {
        return maxNumParents;
    }

    @Override public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        double alpha = Double.parseDouble(alphaParameter);
        return new HillClimbingAlgorithm(probNet, database, alpha,
                metricManager.createInstance(metric, alpha), maxNumParents);
    }
}
