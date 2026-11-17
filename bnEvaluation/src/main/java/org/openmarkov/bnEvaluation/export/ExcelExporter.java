/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmarkov.bnEvaluation.FormatExcel;
import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureMatrixIndProb;
import org.openmarkov.bnEvaluation.measures.MeasureMatrixIndicators;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.bnEvaluation.measures.ScoresRow;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;
import java.util.stream.IntStream;

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
        Row predictedRow = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 1 + statesNames.length - 1));
        Cell titleCell = predictedRow.createCell(1);
        titleCell.setCellValue("Prediction");
        titleCell.setCellStyle(format.centricHeaderFormat.get());
        
        Row stateHeaderRow = sheet.createRow(1);
        Cell trueStatesCell = stateHeaderRow.createCell(0);
        trueStatesCell.setCellValue("True states");
        trueStatesCell.setCellStyle(format.headerFormat.get());
        
        for (int i = 0; i < numStates; i++) {
            Cell stateCell = stateHeaderRow.createCell(1 + i);
            stateCell.setCellValue(statesNames[i].replaceFirst("state ", ""));
            stateCell.setCellStyle(format.centricHeaderFormat.get());
        }
        
        Cell rowsTotalHeader = stateHeaderRow.createCell(1 + numStates);
        rowsTotalHeader.setCellValue("Total");
        rowsTotalHeader.setCellStyle(format.headerFormat.get());
        
        Row colsTotalRow = sheet.createRow(numStates + 2);
        Cell totalLabel = colsTotalRow.createCell(0);
        totalLabel.setCellValue("Total");
        totalLabel.setCellStyle(format.headerFormat.get());
        
        
        // Body.
        int sum = 0;
        for (int i = 0; i < numStates; i++) {
            int rowTotal = 0;
            int colTotal = 0;
            Row row = sheet.createRow(i + 2);
            Cell stateName = row.createCell(0);
            stateName.setCellValue(statesNames[i].replaceFirst("state ", ""));
            stateName.setCellStyle(format.headerFormat.get());
            for (int j = 0; j < numStates; j++) {
                Cell cell = row.createCell(1 + j);
                cell.setCellValue(data[i][j]);
                cell.setCellStyle(format.numericFormat.get());
                rowTotal += data[i][j];
                colTotal += data[j][i];
            }
            sum += rowTotal;
            Cell rowTotalCell = row.createCell(numStates + 1);
            rowTotalCell.setCellStyle(format.totalNumericFormat.get());
            rowTotalCell.setCellValue((double) rowTotal);
            Cell colTotalCell = colsTotalRow.createCell(i + 1);
            colTotalCell.setCellStyle(format.totalNumericFormat.get());
            colTotalCell.setCellValue((double) colTotal);
        }
        Cell grandTotal = colsTotalRow.createCell(numStates + 1);
        grandTotal.setCellValue(sum);
        grandTotal.setCellStyle(format.totalNumericFormat.get());
        
        ExcelExporter.autoResizeAllColumns(sheet);
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
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"States", "TP rate", "FP rate", "Precision", "Recall", "F Measure"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(format.centricHeaderFormat.get());
        }
        
        double[] tp = indicators.getTpRates();
        double[] fp = indicators.getFpRates();
        double[] precision = indicators.getPrecisions();
        double[] fMeasure = indicators.getFMeasures();
        
        for (int i = 0; i < numStates; i++) {
            Row row = sheet.createRow(1 + i);
            Cell stateLabel = row.createCell(0);
            stateLabel.setCellStyle(format.headerFormat.get());
            stateLabel.setCellValue(statesNames[i].replaceFirst("state ", ""));
            writeMetricCell(row, 1, tp[i], format);
            writeMetricCell(row, 2, fp[i], format);
            writeMetricCell(row, 3, precision[i], format);
            writeMetricCell(row, 4, tp[i], format); // Recall == TP rate
            writeMetricCell(row, 5, fMeasure[i], format);
        }
        
        Row meansRow = sheet.createRow(numStates + 1);
        Cell meanLabel = meansRow.createCell(0);
        meanLabel.setCellValue("States mean");
        meanLabel.setCellStyle(format.headerFormat.get());
        writeTotalCell(meansRow, 1, tp[numStates], format);
        writeTotalCell(meansRow, 2, fp[numStates], format);
        writeTotalCell(meansRow, 3, precision[numStates], format);
        writeTotalCell(meansRow, 4, tp[numStates], format);
        writeTotalCell(meansRow, 5, fMeasure[numStates], format);
        
        Row accuracyRow = sheet.createRow(numStates + 3);
        Cell accuracyLabel = accuracyRow.createCell(0);
        accuracyLabel.setCellValue("Accuracy");
        accuracyLabel.setCellStyle(format.headerFormat.get());
        writeTotalCell(accuracyRow, 1, indicators.getAccuracy(), format);
        ExcelExporter.autoResizeAllColumns(sheet);
        
    }
    
    private static void writeMetricCell(Row row, int col, double value, FormatExcel format) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(format.decimalFormat.get());
        cell.setCellValue(value);
    }
    
    private static void writeTotalCell(Row row, int col, double value, FormatExcel format) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(format.totalFormat.get());
    }
    
    // -------------------------------------------------------------------------
    // Scores
    // -------------------------------------------------------------------------
    
    private void writeScores(Workbook workbook, MeasuresSet set) {
        FormatExcel format = new FormatExcel(workbook);
        Sheet sheet = workbook.createSheet("Scores");
        int rowIndex = 0;
        for (ScoresRow row : set.buildScoresRows()) {
            if (row instanceof ScoresRow.Section section) {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1));
                Row sectionRow = sheet.createRow(rowIndex);
                Cell cell = sectionRow.createCell(0);
                cell.setCellValue(section.title());
                cell.setCellStyle(format.headerFormat.get());
            } else if (row instanceof ScoresRow.Data data) {
                Row dataRow = sheet.createRow(rowIndex);
                Cell labelCell = dataRow.createCell(0);
                labelCell.setCellValue(data.label());
                labelCell.setCellStyle(format.headerFormat.get());
                Cell valueCell = dataRow.createCell(1);
                valueCell.setCellValue(data.value());
                valueCell.setCellStyle(format.decimalFormat.get());
            }
            rowIndex++;
        }
        ExcelExporter.autoResizeAllColumns(sheet);
        
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
        for (int i = 0; i < mostProbableStates.length; i++) {
            mostProbableStates[i] = mostProbableStates[i].replaceAll("state ", "");
        }
        
        List<Variable> variables = caseDatabase.getVariables();
        int[][] cases = caseDatabase.getCases();
        int numVariables = variables.size();
        
        Row headers = sheet.createRow(0);
        for (int j = 0; j < numVariables; j++) {
            Cell variableHeader = headers.createCell(j);
            variableHeader.setCellValue(variables.get(j).getName());
            variableHeader.setCellStyle(format.headerFormat.get());
        }
        for (int j = 0; j < statesNames.length; j++) {
            Cell stateHeader = headers.createCell(numVariables + j);
            stateHeader.setCellValue("P(" + varName + "=" + statesNames[j].replaceFirst("state ", "") + ")");
            stateHeader.setCellStyle(format.headerFormat.get());
        }
        Cell mostProbableHeader = headers.createCell(numVariables + statesNames.length);
        mostProbableHeader.setCellValue("MPS");
        mostProbableHeader.setCellStyle(format.headerFormat.get());
        
        Cell correctHeader = headers.createCell(numVariables + statesNames.length + 1);
        correctHeader.setCellValue("Correct");
        correctHeader.setCellStyle(format.headerFormat.get());
        
        var targetVariableIndex = IntStream.range(0, variables.size())
                                           .filter(index -> variables.get(index).getName().equals(varName))
                                           .findFirst()
                                           .getAsInt();
        
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < numVariables; j++) {
                Cell stateCell = row.createCell(j);
                stateCell.setCellValue(variables.get(j).getStateName(cases[i][j]));
                stateCell.setCellStyle(j == targetVariableIndex ? format.markedCellMatrixFormat.get() : format.cellMatrixFormat.get());
            }
            for (int j = 0; j < statesNames.length; j++) {
                Cell probCell = row.createCell(numVariables + j);
                probCell.setCellValue(prob[i][j]);
                probCell.setCellStyle(format.decimalFormat.get());
            }
            String mostProbableState = mostProbableStates[i];
            Cell mostProbableCell = row.createCell(numVariables + statesNames.length);
            mostProbableCell.setCellValue(mostProbableState);
            mostProbableCell.setCellStyle(format.markedCellMatrixFormat.get());
            
            String actualState = variables.get(targetVariableIndex).getStateName(cases[i][targetVariableIndex]);
            Cell correctCell = row.createCell(numVariables + statesNames.length + 1);
            correctCell.setCellStyle(format.cellMatrixFormat.get());
            correctCell.setCellValue(actualState.equals(mostProbableState) ? "correct" : "incorrect");
        }
        
        swapColumns(sheet, targetVariableIndex, variables.size() - 1);
        ExcelExporter.autoResizeAllColumns(sheet);
    }
    
    
    public static void swapColumns(Sheet sheet, int col1, int col2) {
        if (col1 == col2) return;
        
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell1 = row.getCell(col1);
                Cell cell2 = row.getCell(col2);
                
                // Temporarily store cell 1 data
                String formula1 = null;
                Double num1 = null;
                String str1 = null;
                Boolean bool1 = null;
                CellStyle style1 = cell1 != null ? cell1.getCellStyle() : null;
                CellType type1 = cell1 != null ? cell1.getCellType() : CellType.BLANK;
                
                if (cell1 != null) {
                    switch (type1) {
                        case STRING:
                            str1 = cell1.getStringCellValue();
                            break;
                        case NUMERIC:
                            num1 = cell1.getNumericCellValue();
                            break;
                        case BOOLEAN:
                            bool1 = cell1.getBooleanCellValue();
                            break;
                        case FORMULA:
                            formula1 = cell1.getCellFormula();
                            break;
                        default:
                            break;
                    }
                }
                
                // Move cell 2 -> cell 1
                if (cell2 != null) {
                    Cell newCell1 = row.getCell(col1);
                    if (newCell1 == null) newCell1 = row.createCell(col1);
                    newCell1.setCellStyle(cell2.getCellStyle());
                    
                    switch (cell2.getCellType()) {
                        case STRING:
                            newCell1.setCellValue(cell2.getStringCellValue());
                            break;
                        case NUMERIC:
                            newCell1.setCellValue(cell2.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            newCell1.setCellValue(cell2.getBooleanCellValue());
                            break;
                        case FORMULA:
                            newCell1.setCellFormula(cell2.getCellFormula());
                            break;
                        default:
                            newCell1.setBlank();
                            break;
                    }
                } else {
                    Cell newCell1 = row.getCell(col1);
                    if (newCell1 != null) row.removeCell(newCell1);
                }
                
                // Move stored cell 1 -> cell 2
                if (type1 != CellType.BLANK) {
                    Cell newCell2 = row.getCell(col2);
                    if (newCell2 == null) newCell2 = row.createCell(col2);
                    newCell2.setCellStyle(style1);
                    
                    if (formula1 != null) newCell2.setCellFormula(formula1);
                    else if (num1 != null) newCell2.setCellValue(num1);
                    else if (str1 != null) newCell2.setCellValue(str1);
                    else if (bool1 != null) newCell2.setCellValue(bool1);
                } else {
                    Cell newCell2 = row.getCell(col2);
                    if (newCell2 != null) row.removeCell(newCell2);
                }
            }
        }
        
        // Swap widths
        int width1 = sheet.getColumnWidth(col1);
        sheet.setColumnWidth(col1, sheet.getColumnWidth(col2));
        sheet.setColumnWidth(col2, width1);
    }
    
    public static void autoResizeAllColumns(Sheet sheet) {
        int columnCount = getColumnCount(sheet);
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    public static int getColumnCount(Sheet sheet) {
        int maxColumns = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                // getLastCellNum() returns the 1-based index (count) of the last cell
                maxColumns = Math.max(maxColumns, row.getLastCellNum());
            }
        }
        return maxColumns;
    }
    
}
