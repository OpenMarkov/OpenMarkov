/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmarkov.bnEvaluation.FormatExcel;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureMatrixIndProb;
import org.openmarkov.bnEvaluation.measures.MeasureMatrixIndicators;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.bnEvaluation.measures.ScoresRow;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * Renders a {@link MeasuresSet} as an Apache POI {@link Workbook} ready to be
 * written out as {@code .xlsx}. This is the single home of all POI-specific
 * code in the module; the {@code measures} package keeps its dependencies pure.
 */
public final class ExcelExporter {

    private final MeasuresSet measuresSet;
    private final int numIterations;
    private final boolean allVariablesAreUsed;

    public ExcelExporter(MeasuresSet measuresSet) {
        this.measuresSet = measuresSet;
        this.numIterations = measuresSet.getNumIterations();
        this.allVariablesAreUsed = measuresSet.isAllVariablesAreUsed();
    }

    /** Builds and returns a new workbook. The caller is responsible for I/O. */
    public Workbook export() {
        Workbook workbook = new XSSFWorkbook();
        MeasureMatrix matrix = measuresSet.getMeasureMatrix();
        if (matrix != null) {
            writeConfusionMatrix(workbook, matrix);
            writeIndicators(workbook, matrix);
        }
        if (measuresSet.getNumMeasuresValue() > 0) {
            writeScores(workbook, measuresSet);
        }
        if (matrix != null && matrix.getShowIndividualProb()) {
            writeIndividualProbabilities(workbook, matrix);
        }
        return workbook;
    }

    // -------------------------------------------------------------------------
    // Confusion matrix
    // -------------------------------------------------------------------------

    private void writeConfusionMatrix(Workbook workbook, MeasureMatrix m) {
        FormatExcel format = new FormatExcel(workbook);
        Sheet sheet = workbook.createSheet("Confusion matrix");
        String varName = m.getVarName();
        String[] statesNames = m.getStatesNames();
        int[][] data = m.getMatrix();
        int numStates = statesNames.length;
        int numCases = m.getNumCases();

        // Title.
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellStyle(format.getTitleFormat());
        titleCell.setCellValue("Confusion matrix for " + varName.toUpperCase());

        // Header rows.
        Row predictedRow = sheet.createRow(1);
        Cell predictedCell = predictedRow.createCell(1);
        predictedCell.setCellValue("Predicted states");
        predictedCell.setCellStyle(format.getHeaderFormat());

        Row stateHeaderRow = sheet.createRow(2);
        Cell trueStatesCell = stateHeaderRow.createCell(0);
        trueStatesCell.setCellValue("True states");
        trueStatesCell.setCellStyle(format.getHeaderFormat());

        for (int i = 0; i < numStates; i++) {
            Cell stateCell = stateHeaderRow.createCell(1 + i);
            stateCell.setCellValue(statesNames[i]);
            stateCell.setCellStyle(format.getHeaderFormat());
            Cell upperHeader = predictedRow.createCell(2 + i);
            upperHeader.setCellStyle(format.getHeaderFormat());
        }

        Cell rowsTotalHeader = stateHeaderRow.createCell(1 + numStates);
        rowsTotalHeader.setCellValue("Total");
        rowsTotalHeader.setCellStyle(format.getHeaderFormat());

        Row colsTotalRow = sheet.createRow(numStates + 3);
        Cell totalLabel = colsTotalRow.createCell(0);
        totalLabel.setCellValue("Total");
        totalLabel.setCellStyle(format.getHeaderFormat());

        // Body.
        int sum = 0;
        for (int i = 0; i < numStates; i++) {
            int rowTotal = 0;
            int colTotal = 0;
            Row row = sheet.createRow(i + 3);
            Cell stateName = row.createCell(0);
            stateName.setCellValue(statesNames[i]);
            stateName.setCellStyle(format.getHeaderFormat());
            for (int j = 0; j < numStates; j++) {
                Cell cell = row.createCell(1 + j);
                cell.setCellValue(data[i][j]);
                cell.setCellStyle(format.getCellMatrixFormat());
                rowTotal += data[i][j];
                colTotal += data[j][i];
            }
            sum += rowTotal;
            Cell rowTotalCell = row.createCell(numStates + 1);
            rowTotalCell.setCellValue(rowTotal);
            rowTotalCell.setCellStyle(format.getTotalFormat());
            Cell colTotalCell = colsTotalRow.createCell(i + 1);
            colTotalCell.setCellValue(colTotal);
            colTotalCell.setCellStyle(format.getTotalFormat());
        }
        Cell grandTotal = colsTotalRow.createCell(numStates + 1);
        grandTotal.setCellValue(sum);
        grandTotal.setCellStyle(format.getTotalFormat());

        writeFooterNotes(sheet, numStates + 4, "Confusion matrix calculated with " + numCases + " cases ");
    }

    // -------------------------------------------------------------------------
    // Indicators
    // -------------------------------------------------------------------------

    private void writeIndicators(Workbook workbook, MeasureMatrix m) {
        MeasureMatrixIndicators indicators = m.getIndicators();
        if (indicators == null) {
            return;
        }
        FormatExcel format = new FormatExcel(workbook);
        Sheet sheet = workbook.createSheet("Indicators");
        String varName = m.getVarName();
        String[] statesNames = m.getStatesNames();
        int numStates = indicators.getNumStates();
        int numCases = m.getNumCases();

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Confusion matrix indicators for " + varName.toUpperCase());
        titleCell.setCellStyle(format.getTitleFormat());

        Row headerRow = sheet.createRow(1);
        String[] headers = {"States", "TP rate", "FP rate", "Precision", "Recall", "F Measure"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(format.getHeaderFormat());
        }

        double[] tp = indicators.getTpRates();
        double[] fp = indicators.getFpRates();
        double[] precision = indicators.getPrecisions();
        double[] fMeasure = indicators.getFMeasures();

        for (int i = 0; i < numStates; i++) {
            Row row = sheet.createRow(2 + i);
            Cell stateLabel = row.createCell(0);
            stateLabel.setCellStyle(format.getHeaderFormat());
            stateLabel.setCellValue(statesNames[i]);
            writeMetricCell(row, 1, tp[i], format);
            writeMetricCell(row, 2, fp[i], format);
            writeMetricCell(row, 3, precision[i], format);
            writeMetricCell(row, 4, tp[i], format); // Recall == TP rate
            writeMetricCell(row, 5, fMeasure[i], format);
        }

        Row meansRow = sheet.createRow(numStates + 2);
        Cell meanLabel = meansRow.createCell(0);
        meanLabel.setCellValue("States mean");
        meanLabel.setCellStyle(format.getHeaderFormat());
        writeTotalCell(meansRow, 1, tp[numStates], format);
        writeTotalCell(meansRow, 2, fp[numStates], format);
        writeTotalCell(meansRow, 3, precision[numStates], format);
        writeTotalCell(meansRow, 4, tp[numStates], format);
        writeTotalCell(meansRow, 5, fMeasure[numStates], format);

        Row accuracyRow = sheet.createRow(numStates + 3);
        Cell accuracyLabel = accuracyRow.createCell(0);
        accuracyLabel.setCellValue("Accuracy");
        accuracyLabel.setCellStyle(format.getHeaderFormat());
        writeTotalCell(accuracyRow, 1, indicators.getAccuracy(), format);

        writeFooterNotes(sheet, numStates + 4,
                "Confusion matrix indicators calculated with " + numCases + " cases ");
    }

    private static void writeMetricCell(Row row, int col, double value, FormatExcel format) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(format.getCellMatrixFormat());
        cell.setCellValue(value);
    }

    private static void writeTotalCell(Row row, int col, double value, FormatExcel format) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(format.getTotalFormat());
    }

    // -------------------------------------------------------------------------
    // Scores
    // -------------------------------------------------------------------------

    private void writeScores(Workbook workbook, MeasuresSet set) {
        FormatExcel format = new FormatExcel(workbook);
        Sheet sheet = workbook.createSheet("Scores");
        List<MeasureValue> values = set.getMeasures();
        int numCasesScores = values.get(0).getNumCases();

        int rowIndex = 0;
        for (ScoresRow row : set.buildScoresRows()) {
            if (row instanceof ScoresRow.Section section) {
                Row sectionRow = sheet.createRow(rowIndex++);
                Cell cell = sectionRow.createCell(0);
                cell.setCellValue(section.title());
                cell.setCellStyle(format.getTitleFormat());
            } else if (row instanceof ScoresRow.Data data) {
                Row dataRow = sheet.createRow(rowIndex++);
                Cell labelCell = dataRow.createCell(0);
                labelCell.setCellValue(data.label());
                labelCell.setCellStyle(format.getTotalFormat());
                Cell valueCell = dataRow.createCell(1);
                valueCell.setCellValue(data.value());
                valueCell.setCellStyle(format.getCellMatrixFormat());
            }
        }

        Row casesNote = sheet.createRow(rowIndex++);
        casesNote.createCell(0).setCellValue("Scores are calculated with " + numCasesScores + " cases");
        if (numIterations > 1) {
            Row iterationsNote = sheet.createRow(rowIndex);
            iterationsNote.createCell(0).setCellValue(
                    "Measures calculated as an average of " + numIterations + " iterations");
        }
    }

    // -------------------------------------------------------------------------
    // Individual probabilities
    // -------------------------------------------------------------------------

    private void writeIndividualProbabilities(Workbook workbook, MeasureMatrix m) {
        MeasureMatrixIndProb indProb = m.getIndividualProb();
        if (indProb == null) {
            return;
        }
        FormatExcel format = new FormatExcel(workbook);
        Sheet sheet = workbook.createSheet("Probabilities");
        String varName = m.getVarName();
        String[] statesNames = m.getStatesNames();

        CaseDatabase caseDatabase = indProb.getCaseDatabase();
        double[][] prob = indProb.getProbabilities();
        String[] mostProbableStates = indProb.getMostProbableStates();
        List<Variable> variables = caseDatabase.getVariables();
        int[][] cases = caseDatabase.getCases();
        int numVariables = variables.size();

        Row headers = sheet.createRow(0);
        for (int j = 0; j < numVariables; j++) {
            Cell variableHeader = headers.createCell(j);
            variableHeader.setCellValue(variables.get(j).getName());
            variableHeader.setCellStyle(format.getHeaderFormat());
        }
        for (int j = 0; j < statesNames.length; j++) {
            Cell stateHeader = headers.createCell(numVariables + j);
            stateHeader.setCellValue("P(" + varName + "=" + statesNames[j] + ")");
            stateHeader.setCellStyle(format.getHeaderFormat());
        }
        Cell mostProbableHeader = headers.createCell(numVariables + statesNames.length);
        mostProbableHeader.setCellValue("most probable state");
        mostProbableHeader.setCellStyle(format.getHeaderFormat());

        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < numVariables; j++) {
                Cell stateCell = row.createCell(j);
                stateCell.setCellValue(variables.get(j).getStateName(cases[i][j]));
                stateCell.setCellStyle(format.getCellMatrixFormat());
            }
            for (int j = 0; j < statesNames.length; j++) {
                Cell probCell = row.createCell(numVariables + j);
                probCell.setCellValue(String.format("%.3f", prob[i][j]));
                probCell.setCellStyle(format.getCellMatrixFormat());
            }
            Cell mostProbableCell = row.createCell(numVariables + statesNames.length);
            mostProbableCell.setCellValue(mostProbableStates[i]);
            mostProbableCell.setCellStyle(format.getCellMatrixFormat());
        }
    }

    // -------------------------------------------------------------------------
    // Footer notes
    // -------------------------------------------------------------------------

    private void writeFooterNotes(Sheet sheet, int row, String baseMessage) {
        if (numIterations > 1) {
            baseMessage = baseMessage + " evaluated in " + numIterations + " networks";
        }
        sheet.createRow(row).createCell(0).setCellValue(baseMessage);
        if (!allVariablesAreUsed) {
            sheet.createRow(row + 1).createCell(0).setCellValue(
                    "The probabilities were calculated without evidence in all the variables.");
        }
    }
}
