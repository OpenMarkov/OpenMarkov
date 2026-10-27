/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;

import java.util.List;

/**
 * Edit that modifies a single cell value in a link restriction potential table,
 * supporting undo and redo. When the restriction becomes trivial (all ones), the
 * restriction potential is removed.
 * <p>
 * Marking a pair of states as incompatible also puts that cell of the table of the child at zero, spreading the
 * probability of its column among the states still allowed. That change used to be made by the window outside any
 * edit, so neither «Cancel» nor undo gave the table back (error 25 of the pending list); now it is part of this edit.
 * And the link is looked for in the network each time: undoing and redoing the drawing of the link replaces it by
 * another one, and the restriction used to be written in the old one (error 27).
 */
public class LinkRestrictionPotentialValueEdit extends PNEdit {

	/**
	 * The column of the table where is the potential
	 */
	private final int col;
	/**
	 * The row of the table where is the potential
	 */
	private final int row;
	/**
	 * The new value of the potential
	 */
	private final Integer newValue;

	/****
	 * The parent node of the link.
	 */
	private final Node node1;
	/****
	 * The child node of the link.
	 */
	private final Node node2;

	/**
	 * the table potential before the edit
	 */
	private final double[] lastTable;

	/**
	 * The value of the cell before the edit
	 */
	private final int oldValue;

	/***
	 * the table potential after the edit
	 */
	private double[] newTable;

	/** The relations of the child before and after the edit, when the edit changes its table; null otherwise */
	private List<Potential> oldChildPotentials;
	private List<Potential> newChildPotentials;

	/**
	 * Creates a new edit for modifying a link restriction value.
	 *
	 * @param link     the link whose restriction potential is being edited
	 * @param newValue the new compatibility value (0 or 1)
	 * @param row      the row in the restriction table
	 * @param col      the column in the restriction table
	 */
	public LinkRestrictionPotentialValueEdit(Link<Node> link, Integer newValue, int row, int col) {
        super(link.getFrom().getProbNet());
        this.node1 = link.getFrom();
        this.node2 = link.getTo();
		this.col = col;
		this.row = row;
		this.newValue = newValue;
		TablePotential restrictions = link.getRestrictionsPotential();
		this.lastTable = restrictions.getValues().clone();
		int[] offsets = restrictions.getOffsets();
		this.oldValue = (int) lastTable[stateIndex1() * offsets[0] + stateIndex2() * offsets[1]];
	}

	/** @return the link of the network, which may be another object than the one this edit was made with */
	private Link<Node> link() {
		return probNet.getLink(node1, node2, true);
	}

	private int stateIndex1() {
		return col - 1;
	}

	private int stateIndex2() {
		return node2.getVariable().getNumStates() - row;
	}

	@Override protected void doEdit() {
		Link<Node> link = link();
		State state1 = node1.getVariable().getStates()[stateIndex1()];
		State state2 = node2.getVariable().getStates()[stateIndex2()];
        LinkOperations.setCompatibilityValue(link, state1, state2, this.newValue);
		newTable = link.getRestrictionsPotential().getValues().clone();
		if (newValue == 0 && !node2.getPotentials().isEmpty()
				&& node2.getPotentials().getFirst() instanceof TablePotential table) {
			// On a copy, which the method changes in place, so that undo can give back the table
			oldChildPotentials = node2.getPotentials();
			node2.setPotential(table.copy());
			LinkRestrictionPotentialOperations.updatePotentialByAddLinkRestriction(
					node2, link.getRestrictionsPotential(), stateIndex1(), stateIndex2());
			newChildPotentials = node2.getPotentials();
		}
	}

    @Override public void redo() {
		this.setTypicalRedo(false);
		super.redo();
		setRestrictions(newTable);
		if (newChildPotentials != null) {
			node2.setPotentials(newChildPotentials);
		}
	}

	@Override public void undo() {
		super.undo();
		setRestrictions(lastTable);
		if (oldChildPotentials != null) {
			node2.setPotentials(oldChildPotentials);
		}
	}

	private void setRestrictions(double[] table) {
		Link<Node> link = link();
		if (!link.hasRestrictions()) {
			LinkOperations.initializesRestrictionsPotential(link);
		}
		link.getRestrictionsPotential().setValues(table);
		checkRestrictionPotential(table);
	}

	/**
	 * Returns the link restriction potential.
	 *
	 * @return the restriction potential of the link
	 */
	public TablePotential getPotential() {
		return link().getRestrictionsPotential();
	}

	/**
	 * Returns the row position of the edited cell.
	 *
	 * @return the row index
	 */
	public int getRowPosition() {
		return row;
	}

	/**
	 * Returns the column position of the edited cell.
	 *
	 * @return the column index
	 */
	public int getColumnPosition() {
		return col;
	}

	/**
	 * Returns the new value that was set in this edit.
	 *
	 * @return the new compatibility value
	 */
	public Integer getNewValue() {
		return newValue;
	}

	/** @return the value that the cell had before this edit, which undoing it gives back */
	public Integer getOldValue() {
		return oldValue;
	}

	/**
	 * Checks if the restriction potential still contains any restriction (a zero value).
	 * If not, removes the restriction potential from the link entirely.
	 *
	 * @param table the values array of the restriction potential
	 */
	public void checkRestrictionPotential(double[] table) {
		boolean hasRestriction = false;

		for (int i = 0; i < table.length && !hasRestriction; i++) {
            if (table[i] == 0) {
                hasRestriction = true;
                break;
            }
		}
		if (!hasRestriction) {
			link().setRestrictionsPotential(null);
		}

	}

}
