package org.openmarkov.learning.algorithm.naivebayes.gui;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.naivebayes.NaiveBayesAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

/**
 * Dialog showing the options and parameters of the Naive Bayes algorithm.
 *
 * @author Manuel Arias
 */
@SuppressWarnings("serial")
@AlgorithmConfiguration(algorithm = NaiveBayesAlgorithm.class)
public class NaiveBayesParametersDialog extends AlgorithmParametersDialog {

    public NaiveBayesParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        initStandardLayout("Learning.NaiveBayes.Title");
    }

    @Override
    public String getDescription() {
        return stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter;
    }

    @Override
    public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        return new NaiveBayesAlgorithm(probNet, database, Double.parseDouble(alphaParameter));
    }
}
