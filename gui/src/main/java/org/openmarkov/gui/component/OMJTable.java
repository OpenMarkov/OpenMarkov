package org.openmarkov.gui.component;

import org.jetbrains.annotations.Nullable;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.function.BiPredicate;

public class OMJTable extends JTable {
    
    /**
     * Outer object that listens to the changes of the table selection.
     */
    private ListSelectionListener listener = null;
    
    private List<BiPredicate<Integer, Integer>> canGenerateEditor = new ArrayList<>();
    
    public OMJTable(OMTableModel dm) {
        super(dm);
    }
    
    @Override public final OMTableModel getModel() {
        return (OMTableModel) super.getModel();
    }
    
    @Override public final Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component component = super.prepareEditor(editor, row, column);
        component.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
            
            }
            
            @Override public void focusLost(FocusEvent e) {
                Component componentToFocus = e.getOppositeComponent();
                Component[] components = OMJTable.this.getComponents();
                if (components == null) {
                    components = new Component[]{};
                }
                boolean isFocusingASubComponent =
                        componentToFocus == OMJTable.this
                                || Arrays.stream(components).anyMatch(subComponent -> subComponent == componentToFocus);
                if (!isFocusingASubComponent) {
                    OMJTable.this.stopCellEditing();
                }
            }
        });
        return component;
    }
    
    /**
     * Returns an appropriate editor for the cell specified by row and column.
     * If the column is 0, returns null, else returns the default editor.
     *
     * @param row    the row of the cell to edit, where 0 is the first row.
     * @param column the column of the cell to edit, where 0 is the first column.
     *
     * @return the editor for this cell.
     */
    @Override public final @Nullable TableCellEditor getCellEditor(int row, int column) {
        if (this.canGenerateEditor.stream().allMatch(verifier->verifier.test(row, column))) {
            return super.getCellEditor(row, column);
        }
        return null;
    }
    
    public final void canGenerateEditorWhen(BiPredicate<Integer, Integer> canGenerateEditor) {
        this.canGenerateEditor.add(canGenerateEditor);
    }
    
    /**
     * Stops the editing in any cell of the table, recording the new value.
     */
    public final void stopCellEditing() {
        TableCellEditor currentEditor = this.getCellEditor();
        if (currentEditor != null) {
            currentEditor.stopCellEditing();
        }
        System.out.println();
    }

    @Override public void setValueAt(Object newValue, int row, int col) {
        Object oldValue = this.getValueAt(row, col);
        if(newValue.equals(oldValue)) {
            return;
        }
        super.setValueAt(newValue, row, col);
    }
    
    /**
     * Sets a new list selection listener.
     *
     * @param newListener new list selection listener.
     */
    public final void setListSelectionListener(ListSelectionListener newListener) {
        this.listener = newListener;
    }
    
    /**
     * Invoked when the row selection changes.
     *
     * @param e selection event information.
     */
    @Override public final void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        if (this.listener != null) {
            this.listener.valueChanged(e);
        }
    }
    
    
    /**
     * This method edits the cell at row, column.
     * <p>
     * It selects the whole content of the editor component, if a JTextComponent.
     *
     * @param row    - the row of the edited cell
     * @param column - the column of the edited cell
     * @param e      - event to pass into shouldSelectCell;
     */
    @Override public final boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        // Returns the component that is handling the editing session.
        final Component editor = this.getEditorComponent();
        if (editor instanceof JTextComponent) {
            switch (e) {
                case null -> ((JTextComponent) editor).selectAll();
                // Typing in the cell was used to activate the editor
                case KeyEvent _ -> ((JTextComponent) editor).selectAll();
                // F2 was used to activate the editor
                case ActionEvent _ -> ((JTextComponent) editor).selectAll();
                // A mouse click was used to activate the editor.
                // Generally this is a double click and the second mouse click is
                // passed to the editor which would remove the foreground selection unless
                // we use the invokeLater()
                case MouseEvent _ -> SwingUtilities.invokeLater(() -> ((JTextComponent) editor).selectAll());
                default -> {
                }
            }
        }
        return result;
    }
}