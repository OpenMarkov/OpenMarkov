package org.openmarkov.gui.component;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.java.cloneUtils.CloneUtils;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OMTableModel extends DefaultTableModel {
    
    public boolean firstColumnIsHeader;
    
    public OMTableModel(Object[][] body, Object[] head, boolean firstColumnIsHeader) {
        super(body, head);
        this.firstColumnIsHeader = firstColumnIsHeader;
        this.cellEditability = new HashMap<>();
        this.editabilityChecker = new ArrayList<>();
    }
    
    public @NotNull Tuple2Record<OMTableModel, OMTableModel> split(int columnSplit) {
        var rows = new Vector<>(this.getDataVector());
        if (this.firstColumnIsHeader) {
            rows.addFirst(this.columnIdentifiers);
        }
        OMTableModel left = OMTableModel.construct(this.firstColumnIsHeader, rows
                .stream()
                .map(vec -> vec.subList(0, columnSplit)
                               .stream()));
        OMTableModel right = OMTableModel.construct(this.firstColumnIsHeader, rows
                .stream()
                .map(vec -> vec.subList(columnSplit, vec.size())
                               .stream()));
        return Tuples.record(
                left,
                right
        );
    }
    
    public static OMTableModel construct(boolean firstColumnIsHeader, Vector<Vector<Object>> vector) {
        return OMTableModel.construct(firstColumnIsHeader, vector.stream().map(Collection::stream));
    }
    
    public static OMTableModel construct(boolean firstColumnIsHeader, Object[][] rows) {
        return OMTableModel.construct(firstColumnIsHeader, Arrays.stream(rows).map(Arrays::stream));
    }
    
    public static OMTableModel construct(boolean firstColumnIsHeader, Stream<Stream<Object>> rows) {
        var rowsList = new ArrayList<>(rows.map(Stream::toList).map(ArrayList::new).toList());
        var maxRowCount = rowsList.stream().mapToInt(ArrayList::size).max().orElse(0);
        for (var row : rowsList) {
            while (row.size() < maxRowCount) {
                row.addLast(null);
            }
        }
        var header = firstColumnIsHeader ? rowsList.removeFirst() : new ArrayList<>();
        while (header.size() < maxRowCount) {
            header.addLast(null);
        }
        Object[][] body = rowsList.stream().map(r -> r.toArray(Object[]::new)).toArray(Object[][]::new);
        Object[] head = header.toArray(Object[]::new);
        return new OMTableModel(body, head, firstColumnIsHeader);
    }
    
    public static OMTableModel emptyModel(){
        return OMTableModel.construct(false, Stream.empty());
    }
    
    private static final List<Class<?>> SPECIALLY_HANDLED_CLASSES = java.util.List.of(Boolean.class, Integer.class, Double.class, String.class);
    
    @Override public Class<?> getColumnClass(int columnIndex) {
        Class<?> columnClass = super.getColumnClass(columnIndex);
        if (columnIndex >= this.getColumnCount()) {
            return columnClass;
        }
        Object firstValue = IntStream.range(0, this.getRowCount())
                                     .mapToObj(i -> this.getValueAt(i, columnIndex))
                                     .filter(Objects::nonNull)
                                     .findFirst()
                                     .orElse(null);
        if (firstValue != null) {
            var valueClass = firstValue.getClass();
            for (var clazz : SPECIALLY_HANDLED_CLASSES) {
                if (clazz.isAssignableFrom(valueClass)) {
                    return clazz;
                }
            }
        }
        return columnClass;
    }
    
    public Vector getColumnIdentifiers() {
        return this.columnIdentifiers;
    }
    
    public void copyFromModel(OMTableModel baseModel) {
        Vector<String> columnHeaders = new Vector<>();
        if (baseModel.firstColumnIsHeader) {
            for (int i = 0; i < baseModel.getColumnCount(); i++) {
                columnHeaders.add(baseModel.getColumnName(i));
            }
        }
        Vector<Vector> data = new Vector<>(baseModel.getDataVector());
        this.setDataVector(data, columnHeaders);
        this.firstColumnIsHeader = baseModel.firstColumnIsHeader;
        this.cellEditability = CloneUtils.safeClone(baseModel.cellEditability);
        this.editabilityChecker = new ArrayList<>(baseModel.editabilityChecker);
    }
    
    
    private HashMap<Integer, HashMap<Integer, Boolean>> cellEditability;
    
    public void setEditabilityOfCell(int row, int column, boolean editable) {
        if (!cellEditability.containsKey(row)) {
            cellEditability.put(row, new HashMap<>());
        }
        cellEditability.get(row).put(column, editable);
    }
    
    public void removeEditabilityOfCell(int row, int column, boolean editable) {
        if (!cellEditability.containsKey(row)) {
            return;
        }
        cellEditability.get(row).remove(column, editable);
    }
    
    @Override public boolean isCellEditable(int row, int column) {
        for (var checker : this.editabilityChecker) {
            var editability = checker.apply(row, column);
            if (editability == Editability.EDITABLE) {
                return true;
            }
            if (editability == Editability.NON_EDITABLE) {
                return false;
            }
        }
        if (this.cellEditability.get(row) instanceof HashMap<Integer, Boolean> rowMap && rowMap.get(column) instanceof Boolean editability) {
            return editability;
        }
        return super.isCellEditable(row, column);
    }
    
    private List<BiFunction<Integer, Integer, Editability>> editabilityChecker;
    
    public void addEditabilityChecker(BiFunction<Integer, Integer, Editability> editabilityChecker) {
        this.editabilityChecker.add(editabilityChecker);
    }
    
    public void removeEditabilityChecker(BiFunction<Integer, Integer, Editability> editabilityChecker) {
        this.editabilityChecker.remove(editabilityChecker);
    }
    
    public void clearEditability() {
        this.editabilityChecker.clear();
        this.cellEditability.clear();
    }
    
    public enum Editability {
        EDITABLE, NON_EDITABLE, UNSPECIFIED
    }
    
}
