/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.bnEvaluation;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.core.preprocess.Discretization;
import org.openmarkov.learning.core.preprocess.FeatureSelection;
import org.openmarkov.learning.core.preprocess.MissingValues;
import org.openmarkov.learning.core.preprocess.Outliers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link DataPreprocessor}, the pure preprocessing pipeline extracted from the data
 * preprocessing dialog (F3). Covers the class-variable validation rules and that the pipeline, with
 * neutral options, keeps only the selected variables and preserves the cases.
 *
 * @author Manuel Arias
 */
class DataPreprocessorTest {

    private static List<String> names(List<Variable> variables) {
        return variables.stream().map(Variable::getName).toList();
    }

    /** Every variable set to "keep records", "no outliers" and "no discretization". */
    private static DataPreprocessor.Request neutralRequest(CaseDatabase database,
                                                           List<Variable> selectedVariables) {
        Map<String, MissingValues.Option> missing = new HashMap<>();
        Map<String, Discretization.Option> discretize = new HashMap<>();
        Map<String, Integer> intervals = new HashMap<>();
        for (Variable variable : selectedVariables) {
            missing.put(variable.getName(), MissingValues.Option.KEEP);
            discretize.put(variable.getName(), Discretization.Option.NONE);
            intervals.put(variable.getName(), 2);
        }
        return new DataPreprocessor.Request(database, selectedVariables, missing, Outliers.Option.NONE,
                discretize, intervals, null, FeatureSelection.Method.NONE, 5);
    }

    @Test
    void neutralOptionsKeepOnlyTheSelectedVariablesAndPreserveTheCases() {
        Variable a = new Variable("A", "x", "y");
        Variable b = new Variable("B", "p", "q");
        Variable c = new Variable("C", "m", "n");
        CaseDatabase database = new CaseDatabase(List.of(a, b, c),
                new int[][] { { 0, 1, 0 }, { 1, 0, 1 } });

        // Select A and C, drop B.
        CaseDatabase result = DataPreprocessor.process(neutralRequest(database, List.of(a, c)));

        assertEquals(List.of("A", "C"), names(result.getVariables()));
        assertEquals(2, result.getNumCases());
        assertArrayEquals(new int[] { 0, 0 }, result.getCases()[0]);   // A=0, C=0
        assertArrayEquals(new int[] { 1, 1 }, result.getCases()[1]);   // A=1, C=1
    }

    @Test
    void aRequestWithoutSupervisedMethodsNorFeatureSelectionIsValid() {
        Variable a = new Variable("A", "x", "y");
        CaseDatabase database = new CaseDatabase(List.of(a), new int[][] { { 0 } });

        assertNull(DataPreprocessor.validate(neutralRequest(database, List.of(a))));
    }

    @Test
    void supervisedDiscretizationWithoutAClassVariableIsRejected() {
        Variable a = new Variable("A", "x", "y");
        CaseDatabase database = new CaseDatabase(List.of(a), new int[][] { { 0 } });
        DataPreprocessor.Request request = new DataPreprocessor.Request(database, List.of(a),
                Map.of("A", MissingValues.Option.KEEP), Outliers.Option.NONE,
                Map.of("A", Discretization.Option.MDLP), Map.of("A", 2),
                null, FeatureSelection.Method.NONE, 5);

        DataPreprocessor.ValidationError error = DataPreprocessor.validate(request);

        assertEquals("Class variable required", error.title());
    }

    @Test
    void featureSelectionWithoutAClassVariableIsRejected() {
        Variable a = new Variable("A", "x", "y");
        CaseDatabase database = new CaseDatabase(List.of(a), new int[][] { { 0 } });
        DataPreprocessor.Request request = new DataPreprocessor.Request(database, List.of(a),
                Map.of("A", MissingValues.Option.KEEP), Outliers.Option.NONE,
                Map.of("A", Discretization.Option.NONE), Map.of("A", 2),
                null, FeatureSelection.Method.MUTUAL_INFORMATION, 5);

        DataPreprocessor.ValidationError error = DataPreprocessor.validate(request);

        assertEquals("Class variable required", error.title());
    }

    @Test
    void theClassVariableItselfMayNotBeSupervisedlyDiscretized() {
        Variable a = new Variable("A", "x", "y");
        Variable target = new Variable("Class", "yes", "no");
        CaseDatabase database = new CaseDatabase(List.of(a, target), new int[][] { { 0, 0 } });
        Map<String, Discretization.Option> discretize = new HashMap<>();
        discretize.put("A", Discretization.Option.MDLP);
        discretize.put("Class", Discretization.Option.CHIMERGE);   // the class variable itself
        DataPreprocessor.Request request = new DataPreprocessor.Request(database, List.of(a, target),
                Map.of("A", MissingValues.Option.KEEP, "Class", MissingValues.Option.KEEP),
                Outliers.Option.NONE, discretize,
                Map.of("A", 2, "Class", 2), target, FeatureSelection.Method.NONE, 5);

        DataPreprocessor.ValidationError error = DataPreprocessor.validate(request);

        assertEquals("Invalid class variable", error.title());
    }

    @Test
    void supervisedDiscretizationWithAProperClassVariableIsValid() {
        Variable a = new Variable("A", "x", "y");
        Variable target = new Variable("Class", "yes", "no");
        CaseDatabase database = new CaseDatabase(List.of(a, target), new int[][] { { 0, 0 } });
        Map<String, Discretization.Option> discretize = new HashMap<>();
        discretize.put("A", Discretization.Option.MDLP);
        discretize.put("Class", Discretization.Option.NONE);       // class variable not discretized
        DataPreprocessor.Request request = new DataPreprocessor.Request(database, List.of(a, target),
                Map.of("A", MissingValues.Option.KEEP, "Class", MissingValues.Option.KEEP),
                Outliers.Option.NONE, discretize,
                Map.of("A", 2, "Class", 2), target, FeatureSelection.Method.NONE, 5);

        assertNull(DataPreprocessor.validate(request));
    }
}
