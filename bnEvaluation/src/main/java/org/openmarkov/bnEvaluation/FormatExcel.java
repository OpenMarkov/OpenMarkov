/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.apache.poi.ss.usermodel.*;

/**
 * This class compiles the Excel formats to export the results
 */
public class FormatExcel {
    private CellStyle cellMatrixFormat;
    private CellStyle titleFormat;
    private CellStyle totalFormat;
    private CellStyle headerFormat;
    private Workbook workbook;
    
    public FormatExcel(Workbook workbook) {
        this.workbook = workbook;
        cellMatrixFormat = generateCellMatrixFormat();
        titleFormat = generateTitleFormat();
        totalFormat = generateTotalFormat();
        headerFormat = generateHeaderFormat();
    }
    
    private CellStyle generateCellMatrixFormat() {
        CellStyle cellStyle = workbook.createCellStyle();
        Font basicfont = workbook.createFont();
        cellStyle.setFont(basicfont);
        return cellStyle;
        
    }
    
    private CellStyle generateTotalFormat() {
        CellStyle cellStyle = workbook.createCellStyle();
        Font basicfont = workbook.createFont();
        basicfont.setBold(true); // Negrita
        cellStyle.setFont(basicfont);
        return cellStyle;
    }
    
    private CellStyle generateTitleFormat() {
        CellStyle cellStyle = workbook.createCellStyle();
        Font titlefont = workbook.createFont();
        titlefont.setBold(true); // Negrita
        titlefont.setFontHeightInPoints((short) 14); // Tamaño de fuente
        cellStyle.setFont(titlefont);
        return cellStyle;
        
    }
    
    private CellStyle generateHeaderFormat() {
        CellStyle cellStyle = workbook.createCellStyle();
        Font headerfont = workbook.createFont();
        headerfont.setBold(true);
        cellStyle.setFont(headerfont);
        // foreground color
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }
    
    public CellStyle getCellMatrixFormat() {
        return cellMatrixFormat;
    }
    
    public CellStyle getTitleFormat() {
        return titleFormat;
    }
    
    public CellStyle getTotalFormat() {
        return totalFormat;
    }
    
    public CellStyle getHeaderFormat() {
        return headerFormat;
    }
}
