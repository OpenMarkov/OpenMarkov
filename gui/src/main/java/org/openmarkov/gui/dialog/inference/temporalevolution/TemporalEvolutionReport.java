/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for exporting temporal evolution results to an Excel (.xlsx) file.
 */
public class TemporalEvolutionReport {

	// Methods

	/**
	 * creates a new xlsx book with temporal evolution of a variable
	 *
	 * @throws IOException if an I/O error occurs
	 */
    public static void write(String filename, JTable jtable) throws IOException {
		XSSFWorkbook hwb = new XSSFWorkbook();
		String sheetName = filename;
		XSSFSheet sheetTable = hwb.createSheet("Temporal Evolution Report");

		Row rowIndexes = sheetTable.createRow(0);
		rowIndexes.createCell(0).setCellValue("Cycle");
		for (int j = 0; j < jtable.getRowCount(); j++) {
			rowIndexes.createCell(rowIndexes.getLastCellNum()).setCellValue((String) jtable.getValueAt(j, 0));
		}

		for (int i = 1; i < jtable.getColumnCount(); i++) {
			Row row = sheetTable.createRow(i);
			try {
				row.createCell(0).setCellValue((Integer) jtable.getColumnModel().getColumn(i).getHeaderValue());
			} catch (ClassCastException ex){
				row.createCell(0).setCellValue((String) jtable.getColumnModel().getColumn(i).getHeaderValue());
			}

			for (int j = 0; j < jtable.getRowCount(); j++) {
				try {
					row.createCell(j+1).setCellValue((Double) jtable.getValueAt(j,i));
				} catch (ClassCastException ex){
					row.createCell(j+1).setCellValue((String) jtable.getValueAt(j,i));
				}

			}
		}

		String targetFilename = filename.endsWith(".xlsx") ? filename : filename + ".xlsx";
		FileOutputStream fileOut = new FileOutputStream(targetFilename);
		hwb.write(fileOut);
		fileOut.close();

	}

	/**
	 * creates a new xls book with temporal evolution of a variable
	 *
	 * @throws IOException if an I/O error occurs
	 */
//	public void write(String filename, JTable jtable) throws IOException {
//		HSSFWorkbook hwb = new HSSFWorkbook();
//		String sheetName = filename;
//		HSSFSheet sheetTable = hwb.createSheet("Temporal Evolution Report");
//		// first row, column names
//		HSSFRow rowIndexes = sheetTable.createRow(0);
//		rowIndexes.createCell(0).setCellValue("");
//
//		for (int i = 1; i < jtable.getColumnCount(); i++) {
//			rowIndexes.createCell(i + 1).setCellValue(jtable.getColumnModel().getColumn(i).getHeaderValue().toString());
//		}
//		// fill data
//		for (int i = 0; i < jtable.getRowCount(); i++) {
//			HSSFRow row = sheetTable.createRow(i + 1);
//			for (int j = 0; j < jtable.getColumnCount(); j++) {
//				if (jtable.getValueAt(i, j) instanceof String) {
//					row.createCell(j).setCellValue((String) jtable.getValueAt(i, j));
//				} else if (jtable.getValueAt(i, j) instanceof Integer) {
//					row.createCell(j).setCellValue((Integer) jtable.getValueAt(i, j));
//				} else {
//					row.createCell(j).setCellValue((Double) jtable.getValueAt(i, j));
//				}
//			}
//
//		}
//
//		String targetFilename = filename.endsWith(".xls") ? filename : filename + ".xls";
//		FileOutputStream fileOut = new FileOutputStream(targetFilename);
//		hwb.write(fileOut);
//		fileOut.close();
//	}
}
