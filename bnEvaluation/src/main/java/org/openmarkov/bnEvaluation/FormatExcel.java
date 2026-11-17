/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.java.initialization.Lazy;

/**
 * This class compiles the Excel formats to export the results
 */
public class FormatExcel {
    
    private Workbook workbook;
    
    public FormatExcel(Workbook workbook) {
        this.workbook = workbook;
    }
    
    public final Lazy<CellStyle> cellMatrixFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        Font basicfont = workbook.createFont();
        cellStyle.setFont(basicfont);
        return cellStyle;
    });
    
    public final Lazy<CellStyle> markedCellMatrixFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(new XSSFColor(GUIColors.BayesianNetworkEvaluation.ResultingExcel.MARKED.getColor(), new DefaultIndexedColorMap()));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font basicfont = workbook.createFont();
        cellStyle.setFont(basicfont);
        return cellStyle;
    });
    
    public final Lazy<CellStyle> titleFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        Font titlefont = workbook.createFont();
        titlefont.setBold(true); // Negrita
        titlefont.setFontHeightInPoints((short) 14); // Tamaño de fuente
        cellStyle.setFont(titlefont);
        return cellStyle;
    });
    
    public final Lazy<CellStyle> totalFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        Font basicfont = workbook.createFont();
        basicfont.setBold(true); // Negrita
        cellStyle.setFont(basicfont);
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
        return cellStyle;
    });
    
    public final Lazy<CellStyle> headerFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        Font headerfont = workbook.createFont();
        headerfont.setBold(true);
        cellStyle.setFont(headerfont);
        // foreground color
        cellStyle.setFillForegroundColor(new XSSFColor(GUIColors.BayesianNetworkEvaluation.ResultingExcel.HEADER.getColor(), new DefaultIndexedColorMap()));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    });
    
    public final Lazy<CellStyle> centricHeaderFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerfont = workbook.createFont();
        headerfont.setBold(true);
        cellStyle.setFont(headerfont);
        // foreground color
        cellStyle.setFillForegroundColor(new XSSFColor(GUIColors.BayesianNetworkEvaluation.ResultingExcel.LIGHT_HEADER.getColor(), new DefaultIndexedColorMap()));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    });
    
    public final Lazy<CellStyle> numericFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        Font basicfont = workbook.createFont();
        cellStyle.setFont(basicfont);
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0"));
        return cellStyle;
    });
    
    public final Lazy<CellStyle> decimalFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        Font basicfont = workbook.createFont();
        cellStyle.setFont(basicfont);
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
        return cellStyle;
    });
    
    public final Lazy<CellStyle> totalNumericFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        Font basicfont = workbook.createFont();
        basicfont.setBold(true); // Negrita
        cellStyle.setFont(basicfont);
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0"));
        return cellStyle;
    });
    
    public final Lazy<CellStyle> totalDecimalFormat = new Lazy<>(() -> {
        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        Font basicfont = workbook.createFont();
        basicfont.setBold(true); // Negrita
        cellStyle.setFont(basicfont);
        cellStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
        return cellStyle;
    });
    
}
