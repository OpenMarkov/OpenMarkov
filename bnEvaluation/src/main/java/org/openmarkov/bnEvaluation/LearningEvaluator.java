/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmManager;
import org.openmarkov.learning.core.exception.EmptyModelNetException;
import org.openmarkov.learning.core.exception.UnobservedVariablesException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the GUI responsible for collecting the information for the class
 * that will evaluate the learning algorithm.
 *
 * @author evillar
 */
public class LearningEvaluator {
    
    private Class<? extends LearningAlgorithm> learningAlgorithm;
    private ArrayList<Object> options;
    private SplitSet[] sets;
    private MeasuresSet[] measuresSet;
    private MeasuresSet measureSetToCalculate;
    private int numIterations = 0;
    private String classVariableName;
    
    /**
     * Constructor for the class. The instance is created in
     * CrossValidationDialog
     *
     * @param learningAlgorithm     the learning algorithm class to use
     * @param options               algorithm-specific configuration options
     * @param sets                  the train/test split sets for each iteration
     * @param measureSetToCalculate template defining which measures to compute
     */
    public LearningEvaluator(Class<? extends LearningAlgorithm> learningAlgorithm, ArrayList<Object> options,
                             SplitSet[] sets, MeasuresSet measureSetToCalculate) {
        this.learningAlgorithm = learningAlgorithm;
        this.options = options;
        this.sets = sets;
        this.measureSetToCalculate = measureSetToCalculate;
        numIterations = sets.length;
        measuresSet = new MeasuresSet[numIterations];
        classVariableName = null;
    }
    
    public void setVariable(String classVariableName) {
        this.classVariableName = classVariableName;
    }
    
    /**
     * Runs the full evaluation loop: for each split, learns a network from the training set,
     * evaluates it against the test set, and accumulates the measures.
     *
     * @return the averaged measures across all iterations
     */
    public MeasuresSet runEvaluator() throws IncompatibleEvidenceException, ConstraintViolatedException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, CannotNormalizePotentialException {
        // measuresSetMean has numIterations=0
        MeasuresSet measuresSetMean = new MeasuresSet(measureSetToCalculate);
        // loop in k
        for (int i = 0; i < numIterations; i++) {
            CaseDatabase trainDatabase = sets[i].getTrainDatabase();
            CaseDatabase testDatabase = sets[i].getTestDatabase();
            ProbNet trainNet = learnTrainNet(trainDatabase);
            // test with testDatabase
            // a new measuresSet is created with the same structure than measureSetToCalculate
            measuresSet[i] = new MeasuresSet(measureSetToCalculate);
            NetEvaluator netEvaluator = new NetEvaluator(trainNet, testDatabase, measuresSet[i]);
            netEvaluator.runEvaluator();
            measuresSetMean.accumulateMeasureSet(measuresSet[i]);
        }
        measuresSetMean.setAveraged();
        return measuresSetMean;
    }
    
    private ProbNet learnTrainNet(CaseDatabase trainDatabase) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException, CannotNormalizePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        LearningManager learningManager = null;
        try {
            learningManager = new LearningManager(trainDatabase, learningAlgorithm, null, null);
        } catch (EmptyModelNetException | UnobservedVariablesException e) {
            throw new UnreachableException(e);
        }
        // se crea el algoritmo de aprendizaje con el LearningAlgorithmManager
        List<Object> parameters = new ArrayList<>();
        parameters.add(learningManager.getLearnedNet());
        parameters.add(trainDatabase);
        for (Object option : options) {
            parameters.add(option);
        }
        LearningAlgorithm learningAlgorithm = LearningAlgorithmManager.INSTANCE.instantiateByClass(this.learningAlgorithm, parameters);
        learningAlgorithm.setClassVariableName(classVariableName);
        // se incia el learningManager y se aprende la red
        learningManager.init(learningAlgorithm);
        learningManager.learn();
        return learningManager.getLearnedNet();
    }
    
}
