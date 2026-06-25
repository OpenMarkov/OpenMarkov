
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
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.gui.action.LinkRestrictionPotentialValueEdit;

import java.util.ArrayList;

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
    /****
     * The child node of the link
     */
    private final Node node2;

    public LinkRestrictionValuesTable(Link<Node> link, ValuesTableModel tableModel, final boolean modifiable) {
        super(null, tableModel, modifiable);
        this.link = link;
        node2 = link.getTo();
    }
    
    @Override public void setValueAt(Object newValue, int row, int column, Object source) {
        if (newValue == null) return;
        Integer newNumericValue = (Integer) newValue;
        if (!newNumericValue.equals(INCOMPATIBILITY_VALUE) && !newNumericValue.equals(COMPATIBILITY_VALUE)) {
            newValue = INCOMPATIBILITY_VALUE;
        }
        LinkRestrictionPotentialValueEdit linkPotentialEdit =
                new LinkRestrictionPotentialValueEdit(link, (Integer) newValue, row, column);
        try {
            linkPotentialEdit.executeEdit();
            super.setValueAt(newValue, row, column, source);
            int variable1Index = column - 1;
            int variable2Index = node2.getVariable().getNumStates() - row;
            if ((Integer) newValue == 0) {
                if (!node2.getPotentials().isEmpty() && node2.getPotentials().getFirst() instanceof TablePotential) {
                    Potential potential = LinkRestrictionPotentialOperations
                            .updatePotentialByAddLinkRestriction(node2,
                                                                 link.getRestrictionsPotential(), variable1Index,
                                                                 variable2Index);
                    ArrayList<Potential> potentials = new ArrayList<>();
                    potentials.add(potential);
                    node2.setPotentials(potentials);
                }
            }
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
            super.getModel().setValueAt(lrEdit.getNewValue(), lrEdit.getRowPosition(), lrEdit.getColumnPosition());
        }
    }
}
