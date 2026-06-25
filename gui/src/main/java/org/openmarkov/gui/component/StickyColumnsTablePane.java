package org.openmarkov.gui.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.java.swing.ComponentUtilities;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StickyColumnsTablePane extends JScrollPane {
    
    private final OMJTable stickyTable;
    private final OMJTable scrollableTable;
    private OMTableModel stickyTableModel;
    private OMTableModel scrollableTableModel;
    private OMTableModel model;
    private final int frozenColumns;
    private boolean tableModelListenersAreEnabled;
    private final TableModelListener onModelChange;
    
    enum Source {
        MAIN, STICKY, SCROLLABLE
    }
    
    public StickyColumnsTablePane(OMTableModel baseModel, int frozenColumns) {
        this.frozenColumns = frozenColumns;
        this.onModelChange = this::onModelChange;
        this.stickyTable = new OMJTable(OMTableModel.emptyModel()){
            @Override public void setValueAt(Object newValue, int row, int col) {
                StickyColumnsTablePane.this.setValueAt(newValue, row, col, StickyColumnsTablePane.this.model);
            }
        };
        this.scrollableTable = new OMJTable(OMTableModel.emptyModel()){
            @Override public void setValueAt(Object newValue, int row, int col) {
                StickyColumnsTablePane.this.setValueAt(newValue, row, col+frozenColumns, StickyColumnsTablePane.this.model);
            }
        };
        this.setModel(baseModel);
        this.stickyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.scrollableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        this.stickyTable.setSelectionModel(this.scrollableTable.getSelectionModel());
        this.stickyTable.setRowHeight(this.scrollableTable.getRowHeight());
        
        this.stickyTable.setPreferredScrollableViewportSize(this.stickyTable.getPreferredSize());
        
        this.setViewportView(this.scrollableTable);
        this.setRowHeaderView(this.stickyTable);
        this.setCorner(JScrollPane.UPPER_LEFT_CORNER, this.stickyTable.getTableHeader());
        
        this.scrollableTable.addPropertyChangeListener("rowHeight", _ -> {
            if (this.stickyTable.getRowHeight() != this.scrollableTable.getRowHeight()) {
                this.stickyTable.setRowHeight(this.scrollableTable.getRowHeight());
            }
        });
        
        this.stickyTable.addPropertyChangeListener("rowHeight", _ -> {
            if (this.scrollableTable.getRowHeight() != this.stickyTable.getRowHeight()) {
                this.scrollableTable.setRowHeight(this.stickyTable.getRowHeight());
            }
        });
        onTables(omjTable -> omjTable.setBackground(GUIColors.Tables.KeyTable.BACKGROUND_COLOR.getColor()));
    }
    
    public void removeStickyTableFromView(){
        this.setRowHeaderView(null);
        this.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
    }
    
    public void setModel(OMTableModel baseModel) {
        if(this.model != null) {
            this.model.removeTableModelListener(onModelChange);
        }
        if(this.stickyTableModel != null) {
            this.stickyTableModel.removeTableModelListener(onModelChange);
        }
        if(this.scrollableTableModel != null) {
            this.scrollableTableModel.removeTableModelListener(onModelChange);
        }
        
        this.tableModelListenersAreEnabled = true;
        var split = baseModel.split(frozenColumns);
        this.stickyTableModel = split.v0();
        this.scrollableTableModel = split.v1();
        this.stickyTable.setModel(this.stickyTableModel);
        this.scrollableTable.setModel(this.scrollableTableModel);
        this.model = baseModel;
        
        this.model.addTableModelListener(onModelChange);
        this.stickyTableModel.addTableModelListener(onModelChange);
        this.scrollableTableModel.addTableModelListener(onModelChange);
    }
    
    
    private void onModelChange(TableModelEvent tableModelEvent) {
        if (!this.tableModelListenersAreEnabled) {
            return;
        }
        int firstRow = tableModelEvent.getFirstRow();
        int lastRow = tableModelEvent.getLastRow();
        var source = (OMTableModel) tableModelEvent.getSource();
        var sourceKind = this.getSourceKind(source);
        this.tableModelListenersAreEnabled = false;
        if (firstRow == TableModelEvent.HEADER_ROW) {
            switch (sourceKind) {
                case MAIN -> {
                    while(this.stickyTableModel.getRowCount() > 0) {
                        this.stickyTableModel.removeRow(0);
                    }
                    while(this.scrollableTableModel.getRowCount() > 0) {
                        this.scrollableTableModel.removeRow(0);
                    }
                    for(var vector: this.model.getDataVector()){
                        this.stickyTableModel.addRow(new Vector<>(vector.subList(0, frozenColumns)));
                        this.scrollableTableModel.addRow(new Vector<>(vector.subList(frozenColumns, this.getColumnCount())));
                    };
                }
                case STICKY -> {
                    throw new UnsupportedOperationException();
                }
                case SCROLLABLE -> {
                    throw new UnsupportedOperationException();
                }
            }
            
            this.tableModelListenersAreEnabled = true;
            return;
        }
        try {
            switch (tableModelEvent.getType()) {
                case TableModelEvent.INSERT -> IntStream.rangeClosed(firstRow, lastRow)
                                                        .boxed()
                                                        .toList()
                                                        .reversed()
                                                        .stream()
                                                        .forEach(rowIndex -> {
                                                            switch (sourceKind) {
                                                                case MAIN -> {
                                                                    var rowContents = source.getDataVector().get(rowIndex);
                                                                    stickyTableModel.insertRow(firstRow, new Vector<>(rowContents.subList(0, frozenColumns)));
                                                                    scrollableTableModel.insertRow(firstRow, new Vector<>(rowContents.subList(frozenColumns, model.getColumnCount())));
                                                                }
                                                                case STICKY -> {
                                                                    var rowContents = source.getDataVector()
                                                                                            .get(rowIndex);
                                                                    for (int i = 0; i < model.getColumnCount() - stickyTableModel.getColumnCount(); i++) {
                                                                        rowContents.addLast(null);
                                                                    }
                                                                    model.insertRow(firstRow, rowContents);
                                                                    scrollableTableModel.insertRow(firstRow, new Vector<>(rowContents.subList(frozenColumns, model.getColumnCount())));
                                                                }
                                                                case SCROLLABLE -> {
                                                                    var rowContents = source.getDataVector()
                                                                                            .get(rowIndex);
                                                                    for (int i = 0; i < model.getColumnCount() - scrollableTableModel.getColumnCount(); i++) {
                                                                        rowContents.addFirst(null);
                                                                    }
                                                                    model.insertRow(firstRow, rowContents);
                                                                    stickyTableModel.insertRow(firstRow, new Vector<>(rowContents.subList(0, frozenColumns)));
                                                                }
                                                            }
                                                        });
                case TableModelEvent.DELETE -> Stream.of(model, stickyTableModel, scrollableTableModel)
                                                     .filter(targetModel -> targetModel != source)
                                                     .forEach(targetModel -> IntStream.rangeClosed(firstRow, lastRow)
                                                                                      .forEach(targetModel::removeRow));
                case TableModelEvent.UPDATE -> {
                    int column = tableModelEvent.getColumn();
                    int firstRowAppliedToUpdate=firstRow;
                    int lastRowAppliedToUpdate=lastRow;
                    
                    if(firstRowAppliedToUpdate==-1 && lastRowAppliedToUpdate==-1){
                        firstRowAppliedToUpdate=0;
                        lastRowAppliedToUpdate=0;
                    }
                    IntStream.rangeClosed(firstRowAppliedToUpdate, lastRowAppliedToUpdate).forEach(updatedRow -> {
                        if (column == TableModelEvent.ALL_COLUMNS) {
                            IntStream.range(0, source.getColumnCount()).forEach(individualColumn -> {
                                this.updateCell(updatedRow, individualColumn, sourceKind);
                            });
                        } else {
                            this.updateCell(updatedRow, column, sourceKind);
                        }
                    });
                }
                default -> System.out.println("Unknown event type.");
            }
        } finally {
            tableModelListenersAreEnabled = true;
        }
    }
    
    public OMTableModel getModel() {
        return this.model;
    }
    
    private void updateCell(int row, int column, Source sourceKind) {
        Object value = (switch (sourceKind) {
            case MAIN -> this.model;
            case STICKY -> this.stickyTableModel;
            case SCROLLABLE -> this.scrollableTableModel;
        }).getValueAt(row, column);
        switch (sourceKind) {
            case MAIN -> {
                if (column < this.frozenColumns) {
                    this.stickyTableModel.setValueAt(value, row, column);
                } else {
                    this.scrollableTableModel.setValueAt(value, row, column - this.frozenColumns);
                }
            }
            case STICKY -> this.model.setValueAt(value, row, column);
            case SCROLLABLE -> this.model.setValueAt(value, row, column + this.frozenColumns);
        }
        
    }
    
    private @Nullable Source getSourceKind(@Nullable Object source) {
        if (source == this.model) {
            return Source.MAIN;
        }
        if (source == this.scrollableTableModel || source == this.scrollableTable) {
            return Source.SCROLLABLE;
        }
        if (source == this.stickyTableModel || source == this.stickyTable) {
            return Source.STICKY;
        }
        return null;
    }
    
    private @NotNull Source getSourceKindOf(@Nullable Object source) {
        Source sourceKind = this.getSourceKind(source);
        if (sourceKind != null) {
            return sourceKind;
        }
        if (source instanceof Component component) {
            sourceKind = ComponentUtilities.parentsWithSelf(component)
                                           .stream()
                                           .map(this::getSourceKind)
                                           .filter(Objects::nonNull)
                                           .findFirst()
                                           .orElse(null);
        }
        if (sourceKind != null) {
            return sourceKind;
        }
        return Source.MAIN;
    }
    
    
    public void onTables(Consumer<? super OMJTable> consumer) {
        consumer.accept(this.stickyTable);
        consumer.accept(this.scrollableTable);
    }
    
    public void onScrollableTable(Consumer<? super OMJTable> consumer) {
        consumer.accept(this.scrollableTable);
    }
    
    public void onStickyTable(Consumer<? super OMJTable> consumer) {
        consumer.accept(this.stickyTable);
    }
    
    static void main() {
        var frame = new JFrame();
        OMTableModel tableModel = OMTableModel.construct(
                true,
                Stream.of(
                        Stream.of("Name", "Age", "Birthday"),
                        Stream.of("Jorge", 25, "23/09/2000"),
                        Stream.of("Jorgee", 26, "24/09/2000")
                )
        );
        StickyColumnsTablePane comp = new StickyColumnsTablePane(tableModel, 1);
        frame.add(comp);
        frame.setVisible(true);
        frame.pack();
        tableModel.insertRow(2, new Vector<>(Arrays.asList("Jo", "0", "B")));
        tableModel.removeRow(2);
        tableModel.setValueAt("JOOOO", 1, 2);
        tableModel.setValueAt("FROZEN", 1, 0);
        System.out.println();
    }
    
    public int getSelectedRow() {
        if (this.stickyTable.getSelectedRow() != -1) {
            return this.stickyTable.getSelectedRow();
        }
        return this.scrollableTable.getSelectedRow();
    }
    
    public int getSelectedRowCount() {
        if (this.stickyTable.getSelectedRowCount() != 0) {
            return this.stickyTable.getSelectedRowCount();
        }
        return this.scrollableTable.getSelectedRowCount();
    }
    
    public int[] getSelectedRows() {
        if (this.stickyTable.getSelectedRowCount() != 0) {
            return this.stickyTable.getSelectedRows();
        }
        return this.scrollableTable.getSelectedRows();
    }
    
    public void setRowSelectionInterval(int index0, int index1) {
        this.stickyTable.setRowSelectionInterval(index0, index1);
        this.scrollableTable.setRowSelectionInterval(index0, index1);
    }
    
    public int getSelectedColumn(Object source) {
        return switch (this.getSourceKindOf(source)) {
            case MAIN -> {
                if (this.stickyTable.getSelectedColumn() != -1) {
                    yield  this.stickyTable.getSelectedColumn();
                }
                if (this.scrollableTable.getSelectedColumn() != -1) {
                    yield this.scrollableTable.getSelectedColumn() + this.frozenColumns;
                }
                yield -1;
            }
            case STICKY -> this.stickyTable.getSelectedColumn();
            case SCROLLABLE -> this.scrollableTable.getSelectedColumn();
        };
    }
    
    public int getColumnCount() {
        return this.model.getColumnCount();
    }
    
    public int rowAtPoint(Point point, Object source) {
        return switch (this.getSourceKindOf(source)) {
            case MAIN -> -1;
            case STICKY -> this.stickyTable.rowAtPoint(point);
            case SCROLLABLE -> this.scrollableTable.rowAtPoint(point);
        };
    }
    
    public int columnAtPoint(Point point, Object source) {
        return switch (this.getSourceKindOf(source)) {
            case MAIN -> -1;
            case STICKY -> this.stickyTable.columnAtPoint(point);
            case SCROLLABLE -> this.scrollableTable.columnAtPoint(point)+frozenColumns;
        };
    }
    
    public Object getValueAt(int row, int column, @Nullable Object source) {
        return switch (this.getSourceKindOf(source)) {
            case MAIN -> {
                if (column < this.frozenColumns) {
                    yield this.stickyTableModel.getValueAt(row, column);
                }
                yield this.scrollableTableModel.getValueAt(row, column - this.frozenColumns);
            }
            case STICKY -> this.stickyTableModel.getValueAt(row, column);
            case SCROLLABLE -> this.scrollableTableModel.getValueAt(row, column);
        };
    }
    
    public void setValueAt(Object newValue, int row, int column, Object source) {
        switch (this.getSourceKindOf(source)) {
            case MAIN -> {
                if (column < this.frozenColumns) {
                    this.stickyTableModel.setValueAt(newValue, row, column);
                }
                this.scrollableTableModel.setValueAt(newValue, row, column - this.frozenColumns);
            }
            case STICKY -> this.stickyTableModel.setValueAt(newValue, row, column);
            case SCROLLABLE -> this.scrollableTableModel.setValueAt(newValue, row, column);
        }
    }
    
    public boolean editCellAt(int row, int column, Object source) {
        return switch (this.getSourceKindOf(source)) {
            case MAIN -> {
                if (column < this.frozenColumns) {
                    yield this.stickyTable.editCellAt(row, column);
                }
                yield this.scrollableTable.editCellAt(row, column - this.frozenColumns);
            }
            case STICKY -> this.stickyTable.editCellAt(row, column);
            case SCROLLABLE -> this.scrollableTable.editCellAt(row, column);
        };
    }
    
    public Component getEditorComponent(Object source) {
        return switch (this.getSourceKindOf(source)) {
            case MAIN -> {
                if(this.scrollableTable.getEditorComponent()!=null){
                    yield this.scrollableTable.getEditorComponent();
                }
                yield this.stickyTable.getEditorComponent();
            }
            case STICKY -> this.stickyTable.getEditorComponent();
            case SCROLLABLE -> this.scrollableTable.getEditorComponent();
        };
    }
    
    public TableColumn getColumn(int i) {
        if(i<frozenColumns){
            return this.stickyTable.getColumnModel().getColumn(i);
        }
        return this.scrollableTable.getColumnModel().getColumn(i-frozenColumns);
    }
    
    public boolean isCellEditable(int row, int column, Object source) {
        return switch (this.getSourceKindOf(source)) {
            case MAIN -> {
                if (column < this.frozenColumns) {
                    yield this.stickyTable.isCellEditable(row, column);
                }
                yield this.scrollableTable.isCellEditable(row, column - this.frozenColumns);
            }
            case STICKY -> this.stickyTable.isCellEditable(row, column);
            case SCROLLABLE -> this.scrollableTable.isCellEditable(row, column);
        };
    }
    
    public TableColumn getHeaderColumn(int i) {
        if(i<frozenColumns){
            return this.stickyTable.getTableHeader().getColumnModel().getColumn(i);
        }
        return this.scrollableTable.getTableHeader().getColumnModel().getColumn(i-frozenColumns);
        
    }
    
    
    public int resolveMainColumnIndex(JTable table, int column) {
        if(this.stickyTable==table){
            return column;
        }
        if(this.scrollableTable==table){
            return column + this.frozenColumns;
        }
        return -1;
    }
    
    public void canGenerateEditorWhen(BiPredicate<Integer, Integer> canGenerateEditor){
        this.stickyTable.canGenerateEditorWhen(canGenerateEditor);
        this.scrollableTable.canGenerateEditorWhen((row,  column) -> canGenerateEditor.test(row, column+frozenColumns));
    }
}