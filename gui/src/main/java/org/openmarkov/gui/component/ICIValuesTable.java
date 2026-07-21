
/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.jetbrains.annotations.UnknownNullability;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.PNEditListener;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.gui.action.ICITablePotentialValueEdit;
import org.openmarkov.gui.exception.MismatchedValueException;

import java.util.ListIterator;

@SuppressWarnings("serial") public class ICIValuesTable extends ValuesTable implements PNEditListener {

    public ICIValuesTable(Node node, ValuesTableModel tableModel, final boolean modifiable) {
        super(node, tableModel, modifiable);
    }
    
    /**
     * set the number of columns in the table for canonical models adding one
     * column per parent state and adding one more for the id column (hidden)
     *
     * @param properties - node properties
     *
     * @return the number of columns in the table
     */
    public static int howManyCanonicalColumns(Node properties) {
        int numColumns;
        if (properties.getParents() != null) {
            int aux = 1;// first column for child states
            for (Node parent : properties.getParents()) {
                State[] parentStates = parent.getVariable().getStates();
                aux += parentStates.length;
            }
            numColumns = aux + 1; // last column for the leak potential
        } else {
            numColumns = 1;
        }
        // numColumns = FIRST_EDITABLE_COLUMN + numColumns;
        return numColumns;
    }
    
    public static int toPositionOnJtable(int index, int col, int numOfStates, int numOfParents) {
        return numOfParents - 1 + numOfStates + (numOfStates * (col - 1)) - index;
    }
    
    @Override public void setValueAt(Object newValue, int row, int column, Object source) {
        Object oldValue = this.getValueAt(row, column, source);
        // The new value has to be transformed to double
        newValue = ValuesTable.resolveNewDouble(newValue, oldValue);
        if (oldValue==newValue || oldValue.equals(newValue)) {
            return;
        }
        
        // TODO Verificar si la ubicación del siguiente código es adecuada
        if (((Double) newValue).isNaN() || ((Double) newValue) < 0) {
            newValue = oldValue;
            throw new UnrecoverableException(new MismatchedValueException("a positive number", newValue));
        }
        if (oldValue.equals(newValue)) {
            return;
        }
        if (nodeType == NodeType.CHANCE || nodeType == NodeType.DECISION) {
            if (lastCol != column) {
                priorityList.clear();
                lastCol = column;
            }
            ICITablePotentialValueEdit nodePotentialEdit =
                    new ICITablePotentialValueEdit(node, (Double) newValue, row, column, priorityList);
            
            ProbNet probNet1 = node.getProbNet();
            try {
                nodePotentialEdit.executeEdit();
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
        }
        
    }
    
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        int priorityListPosition;
        if (edit instanceof ICITablePotentialValueEdit iciEdit) {
            priorityList = iciEdit.getPriorityList();
            if (!iciEdit.getLeakyFlag()) {// noisy parameters
                double[] noisyPotential = iciEdit.getNewNoisyValues();
                ListIterator<Integer> listIterator = priorityList.listIterator();
                while (listIterator.hasNext()) {
                    priorityListPosition = listIterator.next();
                    super.getModel().setValueAt(noisyPotential[priorityListPosition],
                                                iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
                }
            } else {// leaky parametes
                double[] leakyPotential = iciEdit.getNewLeakyValues();
                ListIterator<Integer> listIterator = priorityList.listIterator();
                while (listIterator.hasNext()) {
                    priorityListPosition = listIterator.next();
                    super.getModel().setValueAt(leakyPotential[priorityListPosition],
                                                iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
                }
            }
        }
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        int priorityListPosition;
        if (edit instanceof ICITablePotentialValueEdit iciEdit) {
            priorityList = iciEdit.getPriorityList();
            if (!iciEdit.getLeakyFlag()) {// noisy parameters
                double[] lastNoisyPotential = iciEdit.getLastNoisyValues();
                ListIterator<Integer> listIterator = priorityList.listIterator();
                while (listIterator.hasNext()) {
                    priorityListPosition = listIterator.next();
                    super.getModel().setValueAt(lastNoisyPotential[priorityListPosition],
                                                iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
                }
            } else {// leaky parametes
                double[] lastLeakyPotential = iciEdit.getLastNoisyValues();
                ListIterator<Integer> listIterator = priorityList.listIterator();
                while (listIterator.hasNext()) {
                    priorityListPosition = listIterator.next();
                    super.getModel().setValueAt(lastLeakyPotential[priorityListPosition],
                                                iciEdit.getRowPosition(priorityListPosition), iciEdit.getColumnPosition());
                }
            }
            super.getModel().setValueAt(iciEdit.getNewValue(), iciEdit.getRowPosition(), iciEdit.getColumnPosition());
        }
    }
}
