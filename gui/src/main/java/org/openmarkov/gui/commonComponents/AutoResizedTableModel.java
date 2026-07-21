package org.openmarkov.gui.commonComponents;

import javax.swing.table.DefaultTableModel;
import java.util.Collection;

public class AutoResizedTableModel extends DefaultTableModel {
    
    
    public AutoResizedTableModel() {
        super();
    }
    
    public AutoResizedTableModel(Collection<?> columnNames) {
        this(columnNames.toArray());
    }
    
    public AutoResizedTableModel(Object[] columnNames) {
        super(columnNames, 0);
    }
    
    @Override public void setValueAt(Object aValue, int row, int column) {
        if (row + 1 > this.getRowCount()) {
            this.setRowCount(row + 1);
        }
        if (column + 1 > this.getColumnCount()) {
            this.setColumnCount(column + 1);
        }
        super.setValueAt(aValue, row, column);
    }
}
