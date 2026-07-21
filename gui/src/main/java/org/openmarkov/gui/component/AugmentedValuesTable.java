/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.AugmentedProbTable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.action.AugmentedPotentialValueEdit;
import org.openmarkov.gui.configuration.GUIColors;
import org.openmarkov.gui.dialog.common.VariableExpressionTextField;

import javax.swing.JTable;
import javax.swing.ToolTipManager;
import java.util.Collections;

/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of the Node Potentials (either in a general family or in a
 * canonical family potential). This table also shows the data in several ways,
 * depending upon the type of user selection:
 * <ul>
 * <li>Probabilities or states values</li>
 * <li>Probabilistic or Deterministic values allowed</li>
 * <li>All parameters or Only independent parameters</li>
 * <li>TPC or canonical parameters(for the Canonical families)</li>
 * <li>Net or Compound values (for the Canonical families)</li>
 * </ul>
 *
 * @author carmenyago
 * @version 1 Apr/2017
 */
public class AugmentedValuesTable extends ValuesTable {
    /**
     * default serial ID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor
     *
     * @param node       - the node with the TablePotential or ExactDistrPotential
     * @param tableModel - the model of the TablePotential or ExactDistrPotential
     * @param modifiable - true if the table can be edited and modified
     */
    public AugmentedValuesTable(Node node, ValuesTableModel tableModel, final boolean modifiable) {
        super(node, tableModel, modifiable);
        this.tablePotential = ((AugmentedProbTablePotential) potential).getAugmentedProbTable();
    }
    
    /**
     * @param node               the node
     * @param tableModel         the table model
     * @param AugmentedProbTable the augmented prob table
     * @param modifiable         the modifiable
     */
    public AugmentedValuesTable(Node node, ValuesTableModel tableModel, AugmentedProbTable AugmentedProbTable,
                                final boolean modifiable) {
        super(node, tableModel, modifiable);
        this.tablePotential = AugmentedProbTable;
    }
    
    /**
     * Default display configuration for this table
     */
    @Override protected void defaultConfiguration() {
        super.defaultConfiguration();
        //CMI
        //CHANGED
        this.onTables(omjTable -> {
            omjTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            omjTable.setRowSelectionAllowed(true);
            omjTable.setColumnSelectionAllowed(true);
            omjTable.setGridColor(GUIColors.Tables.ValuesTable.GRID_COLOR.getColor());
            // next two lines is a cool trick to enhance table performance
            ToolTipManager.sharedInstance().unregisterComponent(omjTable);
            ToolTipManager.sharedInstance().unregisterComponent(omjTable.getTableHeader());
        });
        //CMF
    }
    
    
    @Override public void setValueAt(Object newValue, int row, int column, Object source) {
        Object oldValue = getValueAt(row, column, source);
        if (oldValue.equals(newValue)) {
            return;
        }
        var editorComponent = this.getEditorComponent(source);
        
        VariableExpression expression;
        if (editorComponent instanceof VariableExpressionTextField textField) {
            if (!textField.isValidExpression()) {
                return;
            }
            expression = textField.getExpression();
        } else {
            expression = (VariableExpression) newValue;
        }
        
        AugmentedPotentialValueEdit nodePotentialEdit = new AugmentedPotentialValueEdit(node, expression, row, column, Collections.unmodifiableList(this.priorityList));
        try {
            nodePotentialEdit.executeEdit();
            var modifiedIndex = PotentialsTablePanelOperations.getPotentialIndex(row, column, ((AugmentedProbTablePotential) node.getPotential()).getAugmentedProbTable());
            priorityList.removeIf(value -> value == modifiedIndex);
            priorityList.add(modifiedIndex);
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    /**
     * Updates the edited column
     */
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        if (edit instanceof AugmentedPotentialValueEdit) {
            augmentedPotentialValueEditHappened((AugmentedPotentialValueEdit) edit);
        }
    }
    
    /**
     * @param edit the edit
     */
    public void augmentedPotentialValueEditHappened(AugmentedPotentialValueEdit edit) {
        int position = edit.getIndexSelected();
        Potential editPotential = edit.getNewPotential();
        AugmentedProbTable editTable;
        if (editPotential instanceof AugmentedProbTablePotential) {
            editTable = ((AugmentedProbTablePotential) editPotential).getAugmentedProbTable();
            
        } else {
            editTable = ((UnivariateDistrPotential) editPotential).getAugmentedProbTable();
        }
        VariableExpression[] functionValues = editTable.getFunctionValues();
        if (position >= 0) {
            int rowPosition = edit.getRowPosition(position);
            int columnPosition = edit.getColumnPosition();
            if (editPotential instanceof AugmentedProbTablePotential) {
                int firstEditableRow = PotentialsTablePanelOperations.calculateFirstEditableRow(editTable);
                int lastEditableRow = PotentialsTablePanelOperations.calculateLastEditableRow(editTable);
                
                for (int i = firstEditableRow; i <= lastEditableRow; i++) {
                    var a = PotentialsTablePanelOperations.getPotentialIndex(i, columnPosition, editTable);
                    var v = editTable.getFunctionValues()[a];
                    super.getModel().setValueAt(v, i, columnPosition);
                }
            }
            super.getModel().setValueAt(functionValues[position], rowPosition, columnPosition);
        }
    }
    
    /**
     * UNCLEAR--&gt;Priority list
     */
    @Override public void afterUndoingEdit(PNEdit edit) {
        if (edit instanceof AugmentedPotentialValueEdit augmentedEdit) {
            Potential editPotential = augmentedEdit.getNewPotential();
            AugmentedProbTable editTable;
            if (editPotential instanceof AugmentedProbTablePotential) {
                editTable = ((AugmentedProbTablePotential) editPotential).getAugmentedProbTable();
            } else {
                editTable = ((UnivariateDistrPotential) editPotential).getAugmentedProbTable();
            }
            super.getModel()
                 .setValueAt(editTable.getFunctionValues()[augmentedEdit.getIndexSelected()], augmentedEdit.getRowPosition(),
                             augmentedEdit.getColumnPosition());
        }
        
    }
    
}