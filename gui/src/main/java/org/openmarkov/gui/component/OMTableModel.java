package org.openmarkov.gui.component;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import java.util.stream.Stream;

public class OMTableModel extends DefaultTableModel {
    
    public final boolean firstColumnIsHeader;
    
    public OMTableModel(Object[][] body, Object[] head, boolean firstColumnIsHeader) {
        super(body, head);
        this.firstColumnIsHeader = firstColumnIsHeader;
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
    
}
