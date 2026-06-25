/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.algorithm.em.gui;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.em.EMAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;

/**
 * Dialog showing the options and parameters of the EM algorithm.
 *
 * @author ibermejo
 */
@SuppressWarnings("serial")
@AlgorithmConfiguration(algorithm = EMAlgorithm.class)
public class EMParametersDialog extends AlgorithmParametersDialog {

    public EMParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        initStandardLayout("Learning.EM.Title");
    }

    @Override public String getDescription() {
        return stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter;
    }

    @Override public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        return new EMAlgorithm(probNet, database, Double.parseDouble(alphaParameter));
    }
}
