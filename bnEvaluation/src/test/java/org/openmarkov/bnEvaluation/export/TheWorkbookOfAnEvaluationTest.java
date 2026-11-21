/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.export;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The workbook with the results of the evaluation of a network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheWorkbookOfAnEvaluationTest {

    /** The names of the states as the evaluation panel gives them to the measures. */
    private static String[] namesOf(Variable variable) {
        return Arrays.stream(variable.getStates()).map(State::toString).toArray(String[]::new);
    }

    private static MeasuresSet measuresOf(Variable classVariable, int[][] confusionMatrix, int numCases) {
        MeasureMatrix matrix = new MeasureMatrix(MeasureType.CONFUSIONMATRIX, namesOf(classVariable),
                                                 classVariable.getName());
        matrix.setMatrix(confusionMatrix, numCases);
        matrix.setIndicators();
        MeasuresSet measures = new MeasuresSet("Evaluation");
        measures.addMeasureMatrix(matrix);
        return measures;
    }

    private static List<String> textsOf(Row row) {
        List<String> texts = new ArrayList<>();
        row.forEach(cell -> texts.add(cell.toString()));
        return texts;
    }

    /** Error 435: with a single state there is nothing to merge under «Prediction». */
    @Test void aClassVariableWithASingleStateHasItsWorkbook() throws Exception {
        Variable classVariable = new Variable("A", "only");

        try (Workbook workbook = new ExcelExporter(measuresOf(classVariable, new int[][]{{20}}, 20)).export()) {
            Sheet sheet = workbook.getSheet("Confusion matrix");
            assertEquals("Prediction", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals(List.of("True states", "only"), textsOf(sheet.getRow(1)).subList(0, 2));
            assertEquals(20, (int) sheet.getRow(2).getCell(1).getNumericCellValue());
        }
    }

    /** Error 436: a state whose name has «state » in it keeps its name, and its hits are counted. */
    @Test void aStateWhoseNameHasTheWordStateIsComparedByItsWholeName() throws Exception {
        Variable xRay = new Variable("X-ray", "no", "state yes", "with state yes");
        ProbNet network = new ProbNet();
        network.addNode(xRay, NodeType.CHANCE);
        int[][] cases = {{1}, {2}, {0}, {1}};
        int[] estimated = {1, 2, 0, 0};
        MeasuresSet measures = measuresOf(xRay, new int[][]{{1, 0, 0}, {1, 1, 0}, {0, 0, 1}}, 4);
        measures.getMeasureMatrix().setShowIndividualProb();
        measures.getMeasureMatrix().setIndividualProb(new CaseDatabase(network.getVariables(), cases),
                                                      new double[][]{{0, 1, 0}, {0, 0, 1}, {1, 0, 0}, {1, 0, 0}},
                                                      estimated);
        ExcelExporter exporter = new ExcelExporter(measures);
        exporter.export().close();

        // The second time, as when the user exports the results again
        try (Workbook workbook = exporter.export()) {
            Sheet sheet = workbook.getSheet("Probabilities");
            List<String> header = textsOf(sheet.getRow(0));
            int mostProbable = header.indexOf("MPS");
            int correct = header.indexOf("Correct");
            List<String> rows = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                rows.add(sheet.getRow(i).getCell(mostProbable) + " " + sheet.getRow(i).getCell(correct));
            }
            assertEquals(List.of("state yes correct", "with state yes correct", "no correct", "no incorrect"), rows);
            assertEquals(List.of("P(X-ray=no)", "P(X-ray=state yes)", "P(X-ray=with state yes)"),
                         header.subList(mostProbable - 3, mostProbable));
        }
    }
}
