/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.core.preprocess.Discretization;
import org.openmarkov.learning.core.preprocess.FeatureSelection;
import org.openmarkov.learning.core.preprocess.FilterDatabase;
import org.openmarkov.learning.core.preprocess.MissingValues;
import org.openmarkov.learning.core.preprocess.Outliers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure data-preprocessing pipeline extracted from {@code DataPreprocessingDialog}, so the
 * transformation can be reused and unit-tested without any user interface (model-view-controller
 * separation). From options already read off the view it runs, in order, variable filtering,
 * outlier handling, missing-value handling, discretization and feature selection, and returns the
 * resulting {@link CaseDatabase}. It also checks, as pure logic, the combinations that require a
 * class variable, leaving the dialog to turn a {@link ValidationError} into a message.
 *
 * <p>The pipeline never mutates its inputs: every stage returns a new {@link CaseDatabase}.</p>
 *
 * @author Manuel Arias
 */
public final class DataPreprocessor {

    /**
     * Everything the pipeline needs, already read from the view.
     *
     * @param database               the case database to preprocess
     * @param selectedVariables      the variables kept by the initial filter
     * @param missingValuesOptions   missing-value handling per selected variable name
     * @param outliersOption         a single outlier-handling option applied to every filtered variable
     * @param discretizeOptions      discretization option per selected variable name
     * @param numIntervals           number of intervals per selected variable name (for unsupervised discretization)
     * @param classVariable          class variable for supervised discretization / feature selection, or {@code null}
     * @param featureSelectionMethod feature-selection method ({@link FeatureSelection.Method#NONE} skips this stage)
     * @param featureSelectionTopK   number of features to keep when feature selection runs
     */
    public record Request(CaseDatabase database,
                          List<Variable> selectedVariables,
                          Map<String, MissingValues.Option> missingValuesOptions,
                          Outliers.Option outliersOption,
                          Map<String, Discretization.Option> discretizeOptions,
                          Map<String, Integer> numIntervals,
                          Variable classVariable,
                          FeatureSelection.Method featureSelectionMethod,
                          int featureSelectionTopK) {
    }

    /**
     * A reason a request cannot be preprocessed, ready to be shown by the view.
     *
     * @param title   dialog title
     * @param message explanation for the user
     */
    public record ValidationError(String title, String message) {
    }

    private DataPreprocessor() {
    }

    /**
     * Runs the preprocessing pipeline described by {@code request}.
     *
     * @param request the preprocessing parameters
     * @return the preprocessed database
     */
    public static CaseDatabase process(Request request) {
        CaseDatabase result = FilterDatabase.filter(request.database(), request.selectedVariables());
        result = Outliers.process(result, sameOptionForEveryVariable(result, request.outliersOption()));
        result = MissingValues.process(result, request.missingValuesOptions());
        result = Discretization.process(result, request.discretizeOptions(), request.numIntervals(),
                                        null, request.classVariable());
        FeatureSelection.Method method = request.featureSelectionMethod();
        if (method != null && method != FeatureSelection.Method.NONE) {
            Variable classInResult = (request.classVariable() != null)
                    ? result.getVariable(request.classVariable().getName())
                    : null;
            result = FeatureSelection.select(result, classInResult, method, request.featureSelectionTopK());
        }
        return result;
    }

    /**
     * Checks the combinations that need a class variable. Supervised discretization (MDLP or
     * ChiMerge) and feature selection both require a class variable, and that class variable may not
     * itself be discretized by a supervised method.
     *
     * @param request the preprocessing parameters
     * @return {@code null} if the request is valid, otherwise the reason it is not
     */
    public static ValidationError validate(Request request) {
        boolean supervisedUsed = request.discretizeOptions().values().stream().anyMatch(
                option -> option == Discretization.Option.MDLP || option == Discretization.Option.CHIMERGE);
        FeatureSelection.Method method = request.featureSelectionMethod();
        boolean featureSelectionUsed = method != null && method != FeatureSelection.Method.NONE;
        if (!supervisedUsed && !featureSelectionUsed) {
            return null;
        }
        Variable classVariable = request.classVariable();
        if (classVariable == null) {
            return new ValidationError("Class variable required",
                    "Supervised discretization (MDLP / ChiMerge) and feature selection require selecting a class variable.");
        }
        Discretization.Option classVariableOption = request.discretizeOptions().get(classVariable.getName());
        if (classVariableOption == Discretization.Option.MDLP || classVariableOption == Discretization.Option.CHIMERGE) {
            return new ValidationError("Invalid class variable",
                    "The class variable cannot itself be discretized with a supervised method.");
        }
        return null;
    }

    /** Expands a single option to a per-variable map covering every variable of {@code database}. */
    private static Map<String, Outliers.Option> sameOptionForEveryVariable(CaseDatabase database, Outliers.Option option) {
        Map<String, Outliers.Option> options = new HashMap<>();
        for (Variable variable : database.getVariables()) {
            options.put(variable.getName(), option);
        }
        return options;
    }
}
