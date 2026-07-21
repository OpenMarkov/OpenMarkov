package org.openmarkov.learning.gui;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.core.exception.EmptyModelNetException;
import org.openmarkov.learning.core.exception.UnobservedVariablesException;
import org.openmarkov.learning.core.preprocess.Discretization;
import org.openmarkov.learning.core.preprocess.FilterDatabase;
import org.openmarkov.learning.core.preprocess.MissingValues;
import org.openmarkov.learning.core.util.ModelNetUse;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the business logic of the learning workflow, separated from the
 * {@link LearningDialog} UI layer.
 */
public class LearningController {

    private LearningController() {
    }

    /**
     * Applies the preprocessing pipeline (variable filtering, missing-value
     * handling, discretization) to the raw case database.
     *
     * @param database            raw case database loaded from disk
     * @param selectedVariables   variables the user chose to include
     * @param missingValuesOptions per-variable missing-value strategy
     * @param discretizeOptions   per-variable discretization strategy
     * @param numIntervals        per-variable number of intervals (for discretization)
     * @param modelNet            optional model network (may be null)
     * @return the preprocessed, discretized {@link CaseDatabase}
     */
    public static CaseDatabase preprocessDatabase(
            CaseDatabase database,
            List<Variable> selectedVariables,
            Map<String, MissingValues.Option> missingValuesOptions,
            Map<String, Discretization.Option> discretizeOptions,
            Map<String, Integer> numIntervals,
            ProbNet modelNet) {
        CaseDatabase preprocessed = FilterDatabase.filter(database, selectedVariables);
        preprocessed = MissingValues.process(preprocessed, missingValuesOptions);
        return Discretization.process(preprocessed, discretizeOptions, numIntervals, modelNet);
    }

    /**
     * Instantiates the learning algorithm and initialises the
     * {@link LearningManager} so it is ready to call {@code learn()}.
     *
     * @param preprocessedDb preprocessed case database (from {@link #preprocessDatabase})
     * @param algorithm      algorithm class to use
     * @param modelNet       optional model network (may be null)
     * @param modelNetUse    how the model network should be used (may be null)
     * @param optionsGUI     optional parameters dialog already configured by the user
     * @param classVariable  class variable name for discriminative algorithms (may be empty)
     * @return initialised {@link LearningManager}, ready to run
     */
    public static LearningManager initLearning(
            CaseDatabase preprocessedDb,
            Class<? extends LearningAlgorithm> algorithm,
            ProbNet modelNet,
            ModelNetUse modelNetUse,
            AlgorithmParametersDialog optionsGUI,
            String classVariable) throws EmptyModelNetException, UnobservedVariablesException {
        LearningManager learningManager = new LearningManager(preprocessedDb, algorithm, modelNet, modelNetUse);
        LearningAlgorithm learningAlgorithm;
        if (optionsGUI != null) {
            learningAlgorithm = optionsGUI.getInstance(learningManager.getLearnedNet(), preprocessedDb);
            if (classVariable != null && !classVariable.isEmpty()) {
                learningAlgorithm.setClassVariableName(classVariable);
            }
        } else {
            learningAlgorithm = learningManager.instantiate(algorithm);
        }
        learningManager.init(learningAlgorithm);
        return learningManager;
    }

    /**
     * Arranges all nodes of {@code probNet} in a circle, placing
     * {@code classVariable} first (at the top) when it is present.
     *
     * @param probNet       the learned network whose nodes are to be positioned
     * @param classVariable class-variable name, or empty/null for generative models
     */
    public static void placeNodesInCircle(ProbNet probNet, String classVariable) {
        List<Node> nodes = new ArrayList<>();
        if (classVariable != null && !classVariable.isEmpty()) {
            nodes.add(probNet.getNode(classVariable));
        }
        nodes.addAll(probNet.getNodes()
                            .stream()
                            .filter(node -> !node.getName().equals(classVariable))
                            .toList());

        double radius = 250 + nodes.size() * 2;
        double margin = 100;
        Point2D center = new Point2D.Double(radius + margin, radius + margin);
        for (int i = 0; i < nodes.size(); ++i) {
            double rad = 2 * Math.PI * i / nodes.size();
            Node node = nodes.get(i);
            if (node != null) {
                node.setCoordinateX(center.getX() + Math.sin(rad) * radius);
                node.setCoordinateY(center.getY() - Math.cos(rad) * radius);
            }
        }
    }

    /**
     * Formats an elapsed-time value as {@code m' s'' ms ms.}.
     *
     * @param elapsedTimeMillis elapsed time in milliseconds
     * @return human-readable elapsed-time string
     */
    public static String formatElapsedTime(long elapsedTimeMillis) {
        int minutes = (int) (elapsedTimeMillis / 60000);
        elapsedTimeMillis -= minutes * 60000L;
        int seconds = (int) (elapsedTimeMillis / 1000);
        elapsedTimeMillis -= seconds * 1000L;
        return minutes + "' " + seconds + "\" " + elapsedTimeMillis + " ms.";
    }
}
