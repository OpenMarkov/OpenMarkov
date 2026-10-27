
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
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.gui.action.LinkRestrictionPotentialValueEdit;


/**
 * This table implementation is responsible for the graphical and data model
 * manipulation of the Link restriction potential.
 **/
public class LinkRestrictionValuesTable extends ValuesTable {
    /***
     * Constant value to describe compatibility of a position of the link
     * restriction potential.
     */
    private static final int COMPATIBILITY_VALUE = 1;
    /***
     * Constant value to describe incompatibility of a position of the link
     * restriction potential.
     */
    private static final int INCOMPATIBILITY_VALUE = 0;
    /****
     * The link with the link restriction.
     **/
    private final Link<Node> link;

    public LinkRestrictionValuesTable(Link<Node> link, ValuesTableModel tableModel, final boolean modifiable) {
        super(link.getFrom(), tableModel, modifiable);
        this.link = link;
    }
    
    /**
     * Marks the pair of states of the cell as compatible or not. The edit changes the restriction and, when the pair
     * becomes incompatible, the table of the child, so that «Cancel» and undo give it back (error 25 of the pending
     * list); the cell takes the value when the edit tells this table. It used to write the value in the table of the
     * parent as well, as if the cells were its probabilities (error 155).
     */
    @Override public void setValueAt(Object newValue, int row, int column, Object source) {
        if (newValue == null) return;
        Integer newNumericValue = Integer.valueOf(newValue.toString());
        if (!newNumericValue.equals(INCOMPATIBILITY_VALUE) && !newNumericValue.equals(COMPATIBILITY_VALUE)) {
            newNumericValue = INCOMPATIBILITY_VALUE;
        }
        try {
            new LinkRestrictionPotentialValueEdit(link, newNumericValue, row, column).executeEdit();
        } catch (DoEditException e) {
            throw new UnrecoverableException(e);
        }
    }
    
    @Override public void afterEditExecutes(@UnknownNullability PNEdit edit) {
        if (edit instanceof LinkRestrictionPotentialValueEdit lrEdit) {
            super.getModel().setValueAt(lrEdit.getNewValue(), lrEdit.getRowPosition(), lrEdit.getColumnPosition());
        }
    }
    
    @Override public void afterUndoingEdit(PNEdit edit) {
        if (edit instanceof LinkRestrictionPotentialValueEdit lrEdit) {
            super.getModel().setValueAt(lrEdit.getOldValue(), lrEdit.getRowPosition(), lrEdit.getColumnPosition());
        }
    }
}
