/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.stochasticPropagationOutput;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.logging.OpenMarkovLogger;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.ExceptionDialog;
import org.openmarkov.inference.algorithm.likelihoodWeighting.StochasticPropagation;
import org.openmarkov.stochasticPropagationOutput.exception.EmptyCaseDatabaseException;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Writes the result of propagating a stochastic algorithm to a xlsx file when clicked from the stochastic
 * propagation output dialog. It is parallel.
 *
 * @author iagoparis - spring 2018
 */
public class XlsxWrite implements Runnable {
    
    /*"********
     * Colors *
     **********/
    
    // Colors for the table headers
    private static final Color HEADER_COLOR = new Color(251, 210, 153);
    private static final Color HEADER_COLOR_2 = new Color(255, 220, 180);
    
    private static final Color INCOMPATIBLE_SAMPLE = new Color(255, 120, 120);
    private static final Color COMPATIBLE_SAMPLE = new Color(120, 255, 120);


    private final StochasticPropagationOutputFrame dialog;
    private final @NotNull StochasticPropagation algorithm;
    private final HashMap<Variable, TablePotential> exactPosteriorValues;
    private final String fileName;


    /**
     * Creates the class with these data:
     * @param dialog the StochasticPropagationOutputFrame that must provide as attributes:
 *               - A probNet and
 *               - A stringDatabase for localization
     * @param algorithm the StochasticPropagationAlgorithm which comes from imports.
     * @param exactPosteriorValues the posteriorValues of an exact algorithm to compare.
     * @param fileName the name of the output file.
     */
    XlsxWrite(StochasticPropagationOutputFrame dialog, @NotNull StochasticPropagation algorithm,
              HashMap<Variable, TablePotential> exactPosteriorValues, String fileName) {
        this.dialog = dialog;
        this.algorithm = algorithm;
        this.exactPosteriorValues = exactPosteriorValues;
        this.fileName = fileName;

    }

    /**
     * The act of writing. Creates the xlsx programatically and writes it to a file in the last folder used.
     * Uses apache.poi.
     */

    /* The bulk of the computing time is dedicated to writing samples (to the workbook object) and writing the workbook
     * to a file. If more speed is needed:
     * - Begin the process when the button is clicked instead of when the save destination is chosen. The workbook
     *   creation will be done while the user chooses the save destination.
     * - A limit on printed samples could be suggested in the GUI
     */
    @Override public void run() {

        long startTime = System.nanoTime();

        /*"*********************************
         * Getting data from the algorithm *
         ***********************************/

        // Posterior values, get them from memory instead of propagating again. Since this method is called write,
        // it should only write.
        HashMap<Variable, TablePotential> results = algorithm.getLastPosteriorValues();

        // Samples
        double[][] sampleStorage = algorithm.getSamples();
        if (sampleStorage.length == 0) { // Case of empty database
            ExceptionDialog.show(new EmptyCaseDatabaseException(algorithm));
            return;
        }

        // Variables
        List<Variable> sampledVariables = algorithm.getVariablesToSample();

        // The maximum number of states defines the number of columns of the second sheet in Excel.
        int maxNStates = 0;
        for (Variable variable : sampledVariables) {
            if (variable.getNumStates() > maxNStates) {
                maxNStates = variable.getNumStates();
            }
        }

        // Evidence variables
        EvidenceCase evidence = algorithm.getFusedEvidence();
        List<Variable> evidenceVariables = evidence.getVariables();

        // Time measuring for this phase
        long elapsedMS = (System.nanoTime() - startTime) / 1000000;
        OpenMarkovLogger.LOGGER.info("Data obtained from the algorithm: " + Long.toString(elapsedMS) + " ms elapsed");
        System.out.println("Data obtained from the algorithm: " + Long.toString(elapsedMS) + " ms elapsed");

        /*"***********************
         * Creating the workbook *
         *************************/

        SXSSFWorkbook workbook = new SXSSFWorkbook(1000);

        /* Styles */

        // Styles to convey graphically the weight

        // Weight = 0
        XSSFCellStyle incompatibleSample = (XSSFCellStyle) workbook.createCellStyle();
        incompatibleSample.setFillForegroundColor(new XSSFColor(INCOMPATIBLE_SAMPLE, null));
        incompatibleSample.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Weight = 1
        XSSFCellStyle compatibleSample = (XSSFCellStyle) workbook.createCellStyle();
        compatibleSample.setFillForegroundColor(new XSSFColor(COMPATIBLE_SAMPLE, null));
        compatibleSample.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        /// Font with big emphasis for the next styles
        XSSFFont bigBoldArial = (XSSFFont) workbook.createFont();
        bigBoldArial.setFontName("Arial");
        bigBoldArial.setFontHeightInPoints((short) 11);
        bigBoldArial.setBold(true);

        // Header of table
        XSSFCellStyle topTable = (XSSFCellStyle) workbook.createCellStyle();

        topTable.setFillForegroundColor(new XSSFColor(HEADER_COLOR, null));
        topTable.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        topTable.setAlignment(HorizontalAlignment.LEFT);
        topTable.setFont(bigBoldArial);

        // Header of table centered
        XSSFCellStyle centeredTopTable = (XSSFCellStyle) workbook.createCellStyle();

        centeredTopTable.setFillForegroundColor(new XSSFColor(HEADER_COLOR_2, null));
        centeredTopTable.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        centeredTopTable.setAlignment(HorizontalAlignment.CENTER);
        centeredTopTable.setFont(bigBoldArial);


        /// Font with some emphasis
        XSSFFont bigArial = (XSSFFont) workbook.createFont();
        bigArial.setFontName("Arial");
        bigArial.setFontHeightInPoints((short) 10);
        bigArial.setBold(true);

        // Chance node
        XSSFCellStyle chanceNode = (XSSFCellStyle) workbook.createCellStyle();
        
        chanceNode.setFillForegroundColor(new XSSFColor(GUIColors.Network.ChanceNode.BACKGROUND.getUnthemedColor(), null));
        chanceNode.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        chanceNode.setFont(bigArial);

        // Algorithm name
        XSSFCellStyle subTopTable = (XSSFCellStyle) workbook.createCellStyle();

        subTopTable.setFont(bigArial);


        /// Font with some emphasis and white for contrast
        XSSFFont bigWhiteArial = (XSSFFont) workbook.createFont();
        bigWhiteArial.setFontName("Arial");
        bigWhiteArial.setFontHeightInPoints((short) 11);
        bigWhiteArial.setColor(new XSSFColor(new Color(255, 255, 255), null));

        // Node with finding
        XSSFCellStyle findingNode = (XSSFCellStyle) workbook.createCellStyle();
        
        findingNode.setFillForegroundColor(new XSSFColor(GUIColors.Network.ChanceNode.BACKGROUND_ON_PRE_RESOLUTION_FINDING.getUnthemedColor(), null));
        findingNode.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        findingNode.setFont(bigWhiteArial);



        // No more than four decimal places
        XSSFCellStyle smartDecimals = (XSSFCellStyle) workbook.createCellStyle();
        smartDecimals.setDataFormat(workbook.createDataFormat().getFormat("0.0000"));

        // No more than two decimal places
        XSSFCellStyle smartFewDecimals = (XSSFCellStyle) workbook.createCellStyle();
        smartFewDecimals.setDataFormat(workbook.createDataFormat().getFormat("0.##"));

        // Time measuring for this phase
        elapsedMS = (System.nanoTime() - startTime)/1000000;
        OpenMarkovLogger.LOGGER.info("Workbook styles created: " + Long.toString(elapsedMS) + " ms elapsed");
        System.out.println("Workbook styles created: " + Long.toString(elapsedMS) + " ms elapsed");

        /*"******************
         * Sheet of samples *
         ********************/
        SXSSFSheet samples = workbook.createSheet(dialog.stringDatabase.getString("SheetNames.Samples"));

        /* Title row */
        Row titlesRow = samples.createRow(0);
        titlesRow.createCell(0).setCellValue(dialog.stringDatabase.getString("ColumnTitles.SampleN"));
        for (int colNum = 1; colNum < sampledVariables.size() + 1; colNum++) { // Names of variables
            titlesRow.createCell(colNum).setCellValue(sampledVariables.get(colNum - 1).getName());
        }
        titlesRow.createCell(sampledVariables.size() + 1).setCellValue(dialog.stringDatabase.
                getString("ColumnTitles.Weight"));

        // Apply header style
        for (int colNum = 0; colNum < sampledVariables.size() + 2; colNum++) {
            titlesRow.getCell(colNum).setCellStyle(topTable);
        }

        // Auto-size columns of first sheet
        for (int colNum = 0; colNum < sampledVariables.size() + 2; colNum++) {
            samples.trackAllColumnsForAutoSizing();
            samples.autoSizeColumn(colNum);
        }

        elapsedMS = (System.nanoTime() - startTime)/1000000;
        OpenMarkovLogger.LOGGER.info("Header of samples sheet: " + Long.toString(elapsedMS) + " ms elapsed");
        System.out.println("Header of samples sheet: " + Long.toString(elapsedMS) + " ms elapsed");

        /* Sample's rows */
        int rowNum;

        // Sample's loop
        for (rowNum = 1; rowNum < algorithm.getSampleSize() + 1; rowNum++) {

            Row sampleRow = samples.createRow(rowNum);
            sampleRow.createCell(0).setCellValue(rowNum);
            double weight = sampleStorage[rowNum - 1][sampledVariables.size()];
            for (int colNum = 1; colNum < sampledVariables.size() + 2; colNum++) { // Sampled states
                sampleRow.createCell(colNum).setCellValue(sampleStorage[rowNum - 1][colNum - 1]);
                if (weight == 0) {
                    sampleRow.getCell(colNum).setCellStyle(incompatibleSample);
                } else {
                    sampleRow.getCell(colNum).setCellStyle(compatibleSample);
                }
            }
        }



        elapsedMS = (System.nanoTime() - startTime)/1000000;
        OpenMarkovLogger.LOGGER.info("Sample rows created: " + Long.toString(elapsedMS) + " ms elapsed");
        System.out.println("Sample rows created: " + Long.toString(elapsedMS) + " ms elapsed");

        /*"************************
         * Sheet of general stats *
         **************************/

        SXSSFSheet generalStats = workbook.createSheet(dialog.stringDatabase.getString("SheetNames.GeneralStats"));

        /* Rows of algorithm data */

        rowNum = 0;
        generalStats.createRow(rowNum).createCell(0).setCellValue(dialog.stringDatabase.
                getString("RowTitles.Algorithm"));
        generalStats.getRow(rowNum).getCell(0).setCellStyle(topTable);
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        generalStats.getRow(rowNum).createCell(1).setCellValue(dialog.algorithmName);
        generalStats.getRow(rowNum).getCell(1).setCellStyle(subTopTable);

        rowNum++; // Row 1
        generalStats.createRow(rowNum).createCell(0).setCellValue(dialog.stringDatabase.
                getString("RowTitles.TotalSamples"));
        generalStats.getRow(rowNum).getCell(0).setCellStyle(topTable);
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        generalStats.getRow(rowNum).createCell(1).setCellValue(algorithm.getSampleSize());

        rowNum++; // Row 2
        generalStats.createRow(rowNum).createCell(0).setCellValue(dialog.stringDatabase.
                getString("RowTitles.RuntimePerSample"));
        generalStats.getRow(rowNum).getCell(0).setCellStyle(topTable);
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        generalStats.getRow(rowNum).createCell(1).
                setCellValue(algorithm.getAlgorithmExecutionTime()/algorithm.getSampleSize());
        generalStats.getRow(rowNum).getCell(1).setCellStyle(smartDecimals);

        rowNum++; // Row 3
        generalStats.createRow(rowNum).createCell(0).setCellValue(dialog.stringDatabase.
                getString("RowTitles.Runtime"));
        generalStats.getRow(rowNum).getCell(0).setCellStyle(topTable);
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        generalStats.getRow(rowNum).createCell(1).
                setCellValue(algorithm.getAlgorithmExecutionTime());
        generalStats.getRow(rowNum).getCell(1).setCellStyle(smartFewDecimals);

        rowNum++; // Row 4
        generalStats.createRow(rowNum).createCell(0).setCellValue(dialog.stringDatabase.
                getString("RowTitles.ValidSamples"));
        generalStats.getRow(rowNum).getCell(0).setCellStyle(topTable);
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        generalStats.getRow(rowNum).createCell(1).setCellValue(algorithm.getNumPositiveSamples());

        rowNum++; // Row 5
        generalStats.createRow(rowNum).createCell(0).setCellValue(dialog.stringDatabase.
                getString("RowTitles.AccumulatedWeight"));
        generalStats.getRow(rowNum).getCell(0).setCellStyle(topTable);
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        generalStats.getRow(rowNum).createCell(1).setCellValue(algorithm.getAccumulatedWeight());
        generalStats.getRow(rowNum).getCell(1).setCellStyle(smartFewDecimals);

        rowNum++; // Row 6
        generalStats.createRow(rowNum).createCell(0).setCellValue(dialog.stringDatabase.
                getString("RowTitles.ExactAlgorithmName"));
        generalStats.getRow(rowNum).getCell(0).setCellStyle(topTable);
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        generalStats.getRow(rowNum).createCell(1).setCellValue(dialog.stringDatabase.getString("Algorithms.Hugin"));

        // Empty row

        rowNum +=2; // rows written until now

        /* Row of titles */
        titlesRow = generalStats.createRow(rowNum);
        titlesRow.createCell(0).setCellValue(dialog.stringDatabase.getString("ColumnTitles.Variable"));

        // Big long cell for state names
        generalStats.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, maxNStates));
        titlesRow.createCell(1).setCellValue(dialog.stringDatabase.getString("ColumnTitles.States"));

        // Set header style
        for (int colNum = 0; colNum < 2; colNum++) {
            titlesRow.getCell(colNum).setCellStyle(centeredTopTable);
        }

        // Empty row for clarity
        if (!evidenceVariables.isEmpty()) {
            rowNum++ ;
        }

        // Write about evidence variables that have not been sampled
        for (Variable evidenceVariable : evidenceVariables) {
            if (!sampledVariables.contains(evidenceVariable)) {

                /* Row of names */
                Row namesRow = generalStats.createRow(rowNum);
                // Write the name of the variable
                Cell variableName = namesRow.createCell(0);
                variableName.setCellValue(evidenceVariable.getName());
                variableName.setCellStyle(findingNode); // The color marks it as a finding

                // and then the names of their states
                for (int colNum = 1; colNum < evidenceVariable.getNumStates() + 1; colNum++) {
                    Cell stateName = namesRow.createCell(colNum);
                    stateName.setCellValue(evidenceVariable.getStateName(colNum - 1));
                    stateName.setCellStyle(findingNode);
                }

                /* Row of occurrences */
                // Write "non-sampled"
                Row occurrencesRow = generalStats.createRow(rowNum + 1);
                Cell nSamples = occurrencesRow.createCell(0);
                nSamples.setCellValue(dialog.stringDatabase.getString("RowTitles.Ocurrences"));

                for (int colNum = 1; colNum < evidenceVariable.getNumStates() + 1; colNum++) {
                    occurrencesRow.createCell(colNum)
                            .setCellValue(dialog.stringDatabase.getString("NonSampled"));
                }

                /* Row of finding */
                // Write the finding
                Row approxProbsRow = generalStats.createRow(rowNum + 2);
                Cell approximateProbs = approxProbsRow.createCell(0);
                approximateProbs.setCellValue(dialog.stringDatabase.getString("RowTitles.Finding"));

                for (int colNum = 1; colNum < evidenceVariable.getNumStates() + 1; colNum++) {
                    int value = 0;
                    if (evidence.getState(evidenceVariable) == colNum - 1) {
                        value = 1;
                    }
                    approxProbsRow.createCell(colNum).setCellValue(value);
                }

                // Empty row
                Row emptyRow = generalStats.createRow(rowNum + 4);
                emptyRow.setHeight((short) 120);

                rowNum += 4; // Go to the next variable (each variable uses 4 rows)
            }
        }

        rowNum++; // Empty row for clarity.

        // Write sampled variables
        for (int variablePosition = 0; variablePosition < sampledVariables.size(); variablePosition++) {

            Variable variableToWrite = sampledVariables.get(variablePosition);

            /* Row of names */
            Row namesRow = generalStats.createRow(rowNum);
            // Write the name of the variable
            Cell variableName = namesRow.createCell(0);
            variableName.setCellValue(sampledVariables.get(variablePosition).getName());

            // and then the names of its states
            for (int colNum = 1; colNum < variableToWrite.getNumStates() + 1; colNum++) {
                Cell stateName = namesRow.createCell(colNum);
                stateName.setCellValue(variableToWrite.getStateName(colNum - 1));
            }

            // In logical sampling, evidence variables are samples. If so, mark them as findings.
            if (evidenceVariables.contains(variableToWrite)) {
                for (int colNum = 0; colNum < variableToWrite.getNumStates() + 1; colNum++) {
                    namesRow.getCell(colNum).setCellStyle(findingNode);
                }
            } else {
                for (int colNum = 0; colNum < variableToWrite.getNumStates() + 1; colNum++) {
                    namesRow.getCell(colNum).setCellStyle(chanceNode);
                }
            }

            /* Row of occurrences */
            Row occurrencesRows = generalStats.createRow(rowNum + 1);
            // Write the title of the row
            Cell nSamples = occurrencesRows.createCell(0);
            nSamples.setCellValue(dialog.stringDatabase.getString("RowTitles.Ocurrences"));
            // Write how many times each state has been sampled
            for (int colNum = 1; colNum < variableToWrite.getNumStates() + 1; colNum++) {
                occurrencesRows.createCell(colNum)
                        .setCellValue(getStateOccurrences(sampleStorage, variablePosition,colNum - 1));
            }

            /* Row of approximate probabilities */
            Row approxProbsRow = generalStats.createRow(rowNum + 2);
            // Write the title of the row
            Cell approximateProbs = approxProbsRow.createCell(0);
            approximateProbs.setCellValue(dialog.stringDatabase.getString("RowTitles.ApproximateProbability"));
            // Write the sampled probability
            for (int colNum = 1; colNum < variableToWrite.getNumStates() + 1; colNum++) {
                if (evidenceVariables.contains(variableToWrite)) {
                    // Logic Sampling: in case of a sampled evidence variable, probability = 1 for the finding.
                    int value = 0;
                    if (evidence.getState(variableToWrite) == colNum - 1) {
                        value = 1;
                    }
                    approxProbsRow.createCell(colNum).setCellValue(value);

                } else {
                    approxProbsRow.createCell(colNum)
                            .setCellValue(results.get(variableToWrite).getValues()[colNum - 1]);
                }
                approxProbsRow.getCell(colNum).setCellStyle(smartDecimals);
            }


            /* Row of exact probabilities */
            Row exactProbsRow = generalStats.createRow(rowNum + 3);
            // Write the title of the row
            Cell exactProbs = exactProbsRow.createCell(0);
            exactProbs.setCellValue(dialog.stringDatabase.getString("RowTitles.ExactProbability"));
            // Write how many times each state has been sampled
            for (int colNum = 1; colNum < variableToWrite.getNumStates() + 1; colNum++) {
                exactProbsRow.createCell(colNum)
                        .setCellValue(exactPosteriorValues.get(variableToWrite).getValues()[colNum - 1]);
                exactProbsRow.getCell(colNum).setCellStyle(smartDecimals);
            }

            // Empty row
            Row emptyRow = generalStats.createRow(rowNum + 4);
            emptyRow.setHeight((short) 120);

            rowNum += 5; // Go to the next variable (each variable uses 5 rows)

        }

        // Auto-size columns of second sheet (variable column + states columns)
        for (int colNum = 0; colNum <= 1 + maxNStates; colNum++) {
            generalStats.trackAllColumnsForAutoSizing();
            generalStats.autoSizeColumn(colNum);
        }

        // Time measuring for this phase
        elapsedMS = (System.nanoTime() - startTime)/1000000;
        OpenMarkovLogger.LOGGER.info("General stats added: " + Long.toString(elapsedMS) + " ms elapsed");
        System.out.println("General stats added: " + Long.toString(elapsedMS) + " ms elapsed");

        /*"*****************
         * Output the file *
         *******************/
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
            // remember to close
            outputStream.close();
        } catch (IOException e) {
            throw new UnrecoverableException(e);
        }

        // Time measuring for this phase
        elapsedMS = (System.nanoTime() - startTime)/1000000;
        OpenMarkovLogger.LOGGER.info("Writing done: " + Long.toString(elapsedMS) + " ms elapsed");
        System.out.println("Writing done: " + Long.toString(elapsedMS) + " ms elapsed");

        String conclusion = dialog.stringDatabase.getString("Conclusion") + "\n" + fileName;
        OpenMarkovLogger.LOGGER.info(conclusion);
        System.out.println(conclusion);
        JOptionPane.showMessageDialog(dialog, conclusion,
                dialog.stringDatabase.getString("ConclusionTitle"), JOptionPane.DEFAULT_OPTION);
    }

    /*
     * In a matrix {storage} that has samples as rows and variables as columns, take the variable in
     * {variablePosition} and return how many samples have the state of index {stateIndex} inside
     * of it.
     */
    private static int getStateOccurrences(double[][] storage, int variablePosition, double stateIndex) {

        int sum = 0;
        for (double[] sample : storage) {
            if (sample[variablePosition] == stateIndex) {
                sum++;
            }
        }
        return sum;
    }

}
