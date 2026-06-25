package org.openmarkov.inference.DES;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EqualCriterion;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmarkov.core.inference.MonteCarloOptions.DESNET_RESULTS_DIRECTORY;
import static org.openmarkov.core.inference.MonteCarloOptions.EXCEL_DIRECTORY;

/**
 * Write the results of a Monte Carlo Simulation in an excel file
 * First sheet is the summary. Then it will be a sheet per Series
 * @author  cmyago
 * @version 1 05/2020
 * @version 1.1 13/11/2020 - added discounted results
 */
class DESResultsExcelWriter {
//    Create a Workbook.
//    Create a Sheet.
//    Repeat the following steps until all data is processed:
//    Create a Row.
//    Create Cells in a Row. Apply formatting using CellStyle.
//    Write to an OutputStream.
//    Close the output stream.


    private static final String DOT = ".";
    private static final String SUMMARY= "SUMMARY";
    private static final int  SUMARY_HEADER_ROW_NUMBER = 7;




    private List <TreeMap<String, double[][]>> summaryList;

    private Sheet summarySheet = null;


    XSSFWorkbook workbook;
    SimulationSummaryResults simulationSummaryResults;

    /**
     * With template
     * @param simulationSummaryResults
     * @param name
     * @throws IOException
     * @throws InvalidFormatException
     */
    DESResultsExcelWriter(SimulationSummaryResults simulationSummaryResults, String name) throws IOException, InvalidFormatException {
        this.simulationSummaryResults = simulationSummaryResults;
        InputStream inputStream = null;
        try{
            inputStream =  DESResultsExcelWriter.class.getClassLoader().getResourceAsStream("template.xlsx");
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, "desResultsExcelWriter exception");
            e.printStackTrace();
        }
        Workbook workbook = WorkbookFactory.create(inputStream);
        inputStream.close();
        summarySheet = workbook.getSheet(SUMMARY);
        addHeaders();
        addSeriesBody();
        FileOutputStream fileOuputStream = new FileOutputStream(createFilename(name));
        workbook.write(fileOuputStream);

    }



    /**
     * This method adds a line with the summary headers (Series Number, Number of Simulations, Decision, Criterion1, Criterion2...)
     */
    private void addHeaders(){

        List<EqualCriterion> criteria = simulationSummaryResults.getCriteria();



        String[] seriesHeader = new String[]{ "Series Number", "Number of Simulations"};

        String[] headerPerDecision = new String[criteria.size() *4+1];
        headerPerDecision[0] = "Decision";
        int c=1;
        for (Criterion criterion: criteria){
            headerPerDecision[c++]=criterion.getCriterionName() + " (mean)";
            headerPerDecision[c++]=criterion.getCriterionName() + " (sd)";
            headerPerDecision[c++]=criterion.getCriterionName() + " (discounted mean)";
            headerPerDecision[c++]=criterion.getCriterionName() + " (sd)";
        }


        //Summary header
        Row row = summarySheet.getRow(1);
        int cellnum=3;
        for (int i=0; i<simulationSummaryResults.getDecisionVariable().getNumStates();i++){
            for (int j=0; j<headerPerDecision.length;j++){
                Cell cell =row.getCell(cellnum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(headerPerDecision[j]);
                cell.setCellStyle(row.getRowStyle());
                summarySheet.autoSizeColumn(cellnum);
                cellnum++;
            }
        }



        // SeriesHeader
        row = summarySheet.getRow(SUMARY_HEADER_ROW_NUMBER);
        cellnum=1;

        for (int i=0; i<seriesHeader.length;i++){
            Cell cell = row.createCell(cellnum);
            cell.setCellValue(seriesHeader[i]);
            cell.setCellStyle(row.getRowStyle());

            summarySheet.autoSizeColumn(cellnum);
            cellnum++;

        }

        for (int i=0; i<simulationSummaryResults.getDecisionVariable().getNumStates();i++){
            for (int j=0; j<headerPerDecision.length;j++){
                Cell cell =row.getCell(cellnum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(headerPerDecision[j]);
                cell.setCellStyle(row.getRowStyle());
                summarySheet.autoSizeColumn(cellnum);
                cellnum++;
            }
        }
        summarySheet.createFreezePane(0, SUMARY_HEADER_ROW_NUMBER+ 1);
    }


    /**
     * Writes one line per Series
     */
    private void addSeriesBody(){

        int firstDataRow =SUMARY_HEADER_ROW_NUMBER+1;
        int seriesNumber = 1;
        for (HashMap<String, CriteriaValues> seriesMap:simulationSummaryResults.getSeriesList()) {
            Row row = summarySheet.createRow(firstDataRow++);
            int cell = 1;
            row.createCell(cell++, CellType.NUMERIC).setCellValue(seriesNumber++);
            row.createCell(cell++, CellType.NUMERIC).setCellValue(simulationSummaryResults.getMonteCarloOptions().getNumSimulations());
            for (Map.Entry<String, CriteriaValues> entry : seriesMap.entrySet()) {
                //Decision
                row.createCell(cell++, CellType.STRING).setCellValue(entry.getKey());
                //Values
                HashMap<EqualCriterion, SimulationResults> resultsPerCriterion = entry.getValue().getCriteriaValuesHashMap();
                for (Criterion criterion : resultsPerCriterion.keySet()) {
//                    row.createCell(cell++, CellType.NUMERIC).setCellValue(resultsPerCriterion.get(criterion).getMean());
//                    row.createCell(cell++, CellType.NUMERIC).setCellValue(resultsPerCriterion.get(criterion).getSampleSD());
//                    row.createCell(cell++, CellType.NUMERIC).setCellValue(resultsPerCriterion.get(criterion).getDiscountedMean());
//                    row.createCell(cell++, CellType.NUMERIC).setCellValue(resultsPerCriterion.get(criterion).getSampleSD());
                }
            }

        }

    }





    /**
     *
     * @return
     */
    private File createFilename(String probNetName){
        String filename =probNetName.substring(0,probNetName.indexOf('.'));
        filename+="-";
        filename+= new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
        filename+=".xlsx";



        //Checking if ".DESNetFiles\ExcelLog\ exists"
        File resultsDirectory = new File(DESNET_RESULTS_DIRECTORY);
        if (!resultsDirectory.exists()) {
            resultsDirectory.mkdir();
        }
        File logDirectory = new File(resultsDirectory, EXCEL_DIRECTORY);
        if (!logDirectory.exists()) {
            logDirectory.mkdir();
        }

        return new File(logDirectory, filename);
    }



}
