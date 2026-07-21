package org.openmarkov.gui.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.openmarkov.gui.commonComponents.AutoResizedTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Utility class for creating {@link JTable} instances from data collections
 * and for exporting JTable contents to Apache POI Excel sheets.
 */
public class JTableGeneration {

    /**
     * Creates a JTable from a header collection and row collections.
     *
     * @param header the column headers
     * @param rows   the row data, one collection per row
     * @return a new JTable populated with the given data
     */
    public static JTable dataToJTable(Collection<?> header, Collection<?>... rows) {
        return JTableGeneration.dataToJTable(header.stream(), Arrays.stream(rows).map(Collection::stream));
    }
    
    /**
     * Creates a JTable from streams of header elements and row streams.
     *
     * @param header the column header elements
     * @param rows   a stream of row streams, each inner stream providing the cells for one row
     * @return a new JTable populated with the given data
     */
    public static JTable dataToJTable(Stream<?> header, Stream<? extends Stream<?>> rows) {
        DefaultTableModel model = new AutoResizedTableModel(header.toArray());
        var table = new JTable(model);
        AtomicInteger rowIndex = new AtomicInteger();
        rows.sequential().forEach(row -> {
            AtomicInteger columnIndex = new AtomicInteger();
            row.sequential().forEach(cell -> {
                model.setValueAt(cell, rowIndex.get(), columnIndex.get());
                columnIndex.getAndIncrement();
            });
            rowIndex.getAndIncrement();
        });
        return table;
    }
    
    /**
     * Exports the contents of a JTable (including headers) to an Apache POI {@link Sheet}.
     *
     * @param table the JTable to export
     * @param sheet the target Excel sheet
     */
    public static void saveTableToSheet(JTable table, Sheet sheet) {
        int nextRowToCreateIndex = 0;
        var header = table.getTableHeader().getColumnModel().getColumns().asIterator();
        List<Object> headerObjects = new ArrayList<>();
        while (header.hasNext()) {
            headerObjects.add(header.next().getHeaderValue());
        }
        if (!headerObjects.isEmpty() && headerObjects.stream().anyMatch(Objects::nonNull)) {
            var row = sheet.createRow(nextRowToCreateIndex);
            nextRowToCreateIndex++;
            for (int headerObjectIndex = 0; headerObjectIndex < headerObjects.size(); headerObjectIndex++) {
                Cell cell = row.createCell(headerObjectIndex);
                Object valueAt = headerObjects.get(headerObjectIndex);
                JTableGeneration.saveValueInWorkbookCell(valueAt, cell);
            }
        }
        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
            var row = sheet.createRow(nextRowToCreateIndex);
            nextRowToCreateIndex++;
            for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                Cell cell = row.createCell(columnIndex);
                Object valueAt = table.getValueAt(rowIndex, columnIndex);
                JTableGeneration.saveValueInWorkbookCell(valueAt, cell);
            }
        }
        
    }
    
    private static void saveValueInWorkbookCell(Object valueAt, Cell cell) {
        switch (valueAt) {
            case Double v -> cell.setCellValue(v);
            case Float v -> cell.setCellValue(v);
            case Byte v -> cell.setCellValue(v);
            case Short v -> cell.setCellValue(v);
            case Integer v -> cell.setCellValue(v);
            case Long v -> cell.setCellValue(v);
            case Boolean v -> cell.setCellValue(v);
            case Date v -> cell.setCellValue(v);
            case LocalDate v -> cell.setCellValue(v);
            case LocalDateTime v -> cell.setCellValue(v);
            case Calendar v -> cell.setCellValue(v);
            case RichTextString v -> cell.setCellValue(v);
            case String v -> cell.setCellValue(v);
            case null -> {
            }
            default -> cell.setCellValue(valueAt.toString());
        }
    }
    
}
