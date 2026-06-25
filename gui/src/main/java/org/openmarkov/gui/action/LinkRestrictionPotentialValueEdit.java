/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * Edit that modifies a single cell value in a link restriction potential table,
 * supporting undo and redo. When the restriction becomes trivial (all ones), the
 * restriction potential is removed.
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

	/***
	 * The link with the link restriction potential.
	 */
	private final Link<Node> link;
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

	/***
	 * the table potential after the edit
	 */
	private double[] newTable;

	/**
	 * The potential of the link restriction
	 */
	private TablePotential tablePotential;

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
		this.link = link;
        this.node1 = link.getFrom();
        this.node2 = link.getTo();
		this.col = col;
		this.row = row;
		this.tablePotential = link.getRestrictionsPotential();
		this.newValue = newValue;
		this.lastTable = link.getRestrictionsPotential().getValues().clone();
	}
	
	@Override protected void doEdit() {
		int numStates2 = node2.getVariable().getNumStates();
		int stateIndex1 = col - 1;
		int stateIndex2 = numStates2 - row;
		State state1 = node1.getVariable().getStates()[stateIndex1];
		State state2 = node2.getVariable().getStates()[stateIndex2];
        link.setCompatibilityValue(state1, state2, this.newValue);
		newTable = link.getRestrictionsPotential().getValues().clone();

	}
    
    @Override public void redo() {
		this.setTypicalRedo(false);
		super.redo();
		if (!link.hasRestrictions()) {
			link.initializesRestrictionsPotential();
			this.tablePotential = link.getRestrictionsPotential();
		}
		tablePotential.setValues(newTable);
		checkRestrictionPotential(newTable);
	}

	@Override public void undo() {
		super.undo();
		if (!link.hasRestrictions()) {
			link.initializesRestrictionsPotential();
			this.tablePotential = link.getRestrictionsPotential();
		}
		tablePotential.setValues(lastTable);
		checkRestrictionPotential(lastTable);
	}

	/**
	 * Returns the link restriction table potential.
	 *
	 * @return the table potential of the link restriction
	 */
	public TablePotential getPotential() {
		return tablePotential;
	}

	/**
	 * Gets the row position associated to value edited if priorityList no
	 * exists
	 *
	 * @return the position in the table
	 */
	public int getRowPosition() {
		return row;
	}

	/**
	 * Gets the column where the value is edited
	 *
	 * @return the column edited
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
			tablePotential = null;
			this.link.setRestrictionsPotential(tablePotential);
		}

	}

}
