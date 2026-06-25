/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.gui.component.OMTableModel;
import org.openmarkov.gui.component.StickyColumnsTablePane;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serial;
import java.util.Arrays;

/**
 * This class implements a table that has at least one column. The first column
 * is unmodifiable, has a prefixed width and the rest of cells are modifiable
 * clicking twice on them or they can't be modified. It depends of a parameter
 * of the constructor. All of the columns cannot be resized.
 *
 * @author jmendoza
 * @version 1.0 jmendoza
 */
public class KeyTable extends StickyColumnsTablePane {
    /**
     * Width of the key column.
     */
    private static final int ROW_HEIGHT = 20;
    /**
     * Static field for serializable class.
     */
    @Serial
    private static final long serialVersionUID = 5072153109141850112L;
    /**
     * Indicates if the object is already created.
     */
    private final boolean created;
    /**
     * This variable says if the table can be modified. False by default
     */
    private boolean modifiable;
    /**
     * This variable says if the first column is hidden. Not Visible by default
     */
    private boolean firstColumnHidden;
    /**
     * This variable is used to display or not the column header. Visible by
     * default
     */
    private boolean showColumnHeader;
    
    /**
     * Constructs a JTable that is initialized with dm as the data model, a
     * default column model, and a default selection model.
     *
     * @param dm         the data model for the table.
     * @param modifiable specifies if the cells (except the first column) are
     *                   modifiable.
     */
    public KeyTable(OMTableModel dm, boolean modifiable, boolean firstColumnHidden, boolean showColumnHeader) {
        super(dm, 1);
        this.created = true;
        this.modifiable = modifiable;
        this.firstColumnHidden = firstColumnHidden;
        this.showColumnHeader = showColumnHeader;
        this.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
            
            }
            
            @Override public void focusLost(FocusEvent e) {
                KeyTable.this.onFocusLost(e);
            }
        });
        this.setModel(dm);
    }
    
    // ESCA-JAVA0126:
    
    /**
     * Sets the data model for this table to newModel and registers with it for
     * listener notifications from the new data model.
     *
     * @param newDataModel the new data source for this table.
     *
     * @throws IllegalArgumentException if newModel is null.
     */
    @Override public final void setModel(OMTableModel newDataModel) throws IllegalArgumentException {
        super.setModel(newDataModel);
        if(!this.created){
            return;
        }
        this.defaultConfiguration();
    }
    
    /**
     * @param modifiable the modifiable to set
     */
    public final void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }
    
    /**
     * @param firstColumnHidden the firstColumnHidden to set
     */
    protected final void setFirstColumnHidden(boolean firstColumnHidden) {
        this.firstColumnHidden = firstColumnHidden;
    }
    
    /**
     * @param showColumnHeader the showColumnHeader to set
     */
    protected final void setShowColumnHeader(boolean showColumnHeader) {
        this.showColumnHeader = showColumnHeader;
    }
    
    /**
     * This method configures the table to a default state.
     */
    protected void defaultConfiguration() {
        this.setBackground(GUIColors.Tables.KeyTable.BACKGROUND_COLOR.getColor());// Component color

        this.onTables(omjTable -> {
            omjTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            omjTable.setSelectionBackground(GUIColors.Tables.KeyTable.SELECTION_BACKGROUND_COLOR.getColor());// Color for cell
            // renderers
            omjTable.setSelectionForeground(GUIColors.Tables.KeyTable.SELECTION_FOREGROUND_COLOR.getColor());
            omjTable.setRowHeight(KeyTable.ROW_HEIGHT);
            omjTable.setShowGrid(true);
            omjTable.setGridColor(GUIColors.Tables.KeyTable.GRID_COLOR.getColor());
            omjTable.setShowVerticalLines(false);
            omjTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            omjTable.canGenerateEditorWhen((_, _)-> this.modifiable);
        });
        this.onTables(omjTable -> {
            if (omjTable.getColumnClass(0) != null) {
                if (this.modifiable) {
                    TableCellEditor editorCell = omjTable.getDefaultEditor(omjTable.getColumnClass(0));
                    if (editorCell instanceof DefaultCellEditor) {
                        ((DefaultCellEditor) editorCell).setClickCountToStart(2);
                    }
                }
                /**
                 * This variable is used to set additionalProperties for the header in the
                 * table
                 */
                JTableHeader header = omjTable.getTableHeader();
                header.setReorderingAllowed(false);
                header.setResizingAllowed(false);
                header.setVisible(this.showColumnHeader);
                if (!this.showColumnHeader) {
                    header.setPreferredSize(new Dimension(20, 0));
                }

            }
        });
        
        /**
         * This variable is used to set additionalProperties for the columns in the
         * table
         */
        TableColumn column = this.getColumn(0);
        if (this.firstColumnHidden && column != null) {
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setWidth(0);
            column.setPreferredWidth(0);
            column.setResizable(false);
            // Fixing issue 221
            // https://bitbucket.org/cisiad/org.openmarkov.issues/issue/221/button-delete-in-node-properties-parents
            // Without the following line, the column actually showed three dot
            column.setHeaderRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    // Return an empty renderer component
                    Component c = super.getTableCellRendererComponent(table, "", false, false, row, column);
                    c.setPreferredSize(new Dimension(0, 0));
                    c.setSize(0, 0);
                    return c;
                }
            });
            this.removeStickyTableFromView();
            this.getHeaderColumn(0).setMinWidth(0);
            this.getHeaderColumn(0).setMaxWidth(0);
            this.getHeaderColumn(0).setWidth(0);
            this.getHeaderColumn(0).setPreferredWidth(0);
            this.getHeaderColumn(0).setResizable(false);
        }
    }
    
    /**
     * Stops the editing in any cell of the table, recording the new value.
     */
    public void stopCellEditing() {
        this.onTables(omjTable -> {
            if (omjTable.getCellEditor() instanceof TableCellEditor currentEditor) {
                currentEditor.stopCellEditing();
            }
        });
    }
    
    public void cancelCellEditing(){
        this.onTables(omjTable -> {
            if (omjTable.getCellEditor() instanceof TableCellEditor currentEditor) {
                currentEditor.cancelCellEditing();
            }
        });
    }
    
    private void onFocusLost(FocusEvent e) {
        Component componentToFocus = e.getOppositeComponent();
        Component[] components = this.getComponents();
        if (components == null) {
            components = new Component[]{};
        }
        boolean isFocusingASubComponent =
                componentToFocus == this
                        || Arrays.stream(components).anyMatch(subComponent -> subComponent == componentToFocus);
        if (!isFocusingASubComponent) {
            this.stopCellEditing();
        }
    }
    
    
    public int getRowCount() {
        return this.getModel().getRowCount();
    }
}
