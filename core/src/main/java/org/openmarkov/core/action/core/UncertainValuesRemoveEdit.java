/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.action.base.PNEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code UncertainValuesRemoveEdit} is an edit that allow us to removes the uncertain values
 * column for a certain configuration.
 * If all the values in the uncertain values are null after the removal then the uncertain value is set to null.
 *
 * @author mluque
 * @version 1 23/06/11
 */

@SuppressWarnings("serial") public class UncertainValuesRemoveEdit extends PNEdit {

	private final List<UncertainValue> oldUncertainColumn;

	private final int basePosition;

	private final Node node;

	private final boolean wasNullOldUncertainColumn;

	/**
	 * Creates a new {@code AddNodeEdit} with the network where the new
	 * new node will be added and basic information about it.
	 *
	 * @param node          the new node
	 * @param configuration Configuration
	 */
    public UncertainValuesRemoveEdit(Node node, EvidenceCase configuration) throws NonProjectablePotentialException {
		super(node.getProbNet());

		this.node = node;

		Potential potential = getPotential();
        
        TablePotential auxProjected = potential.tableProject(configuration, null);

		UncertainValue[] auxUncertainTable = auxProjected.getUncertainValues();

		wasNullOldUncertainColumn = !hasUncertainValues(auxUncertainTable);

		UncertainValue[] oldUncertainColumnArray = (wasNullOldUncertainColumn ? null : auxUncertainTable);

		oldUncertainColumn = new ArrayList<>();
        Collections.addAll(oldUncertainColumn, oldUncertainColumnArray);
		TablePotential auxPotential = getTablePotential(potential);
		this.basePosition = getBasePosition(auxPotential,configuration,potential instanceof ExactDistrPotential);

	}
	
	
	/**
	 * It returns the first position in the table of the consecutive cells where all the values corresponding to a certain configuration are stored. It
	 * assumes that configuration is a complete instantiation of the parents of the variable associated to the table.
	 *
	 * @param configuration Evidence case
	 * @return first position in the table of the consecutive cells where all the values corresponding to a certain configuration are stored
	 */
    public static int getBasePosition(TablePotential potential, EvidenceCase configuration, boolean isInAnExactDistrPotential) {
        int sizeEvi = configuration.getFindings().size();
        int sizeCoordinates = sizeEvi + (isInAnExactDistrPotential ? 0 : 1);
        int[] coordinates = new int[sizeCoordinates];
		List<Variable> varsTable = potential.getVariables();
        int startLoop = isInAnExactDistrPotential ? 0 : 1;
		if (!isInAnExactDistrPotential) {
			// It is a typical probability potential of a chance variable
			coordinates[0] = 0;
		}
		for (int i = startLoop; i < sizeCoordinates; i++) {
			coordinates[i] = configuration.getFinding(varsTable.get(i)).getStateIndex();
		}
        int pos = potential.getPosition(coordinates);
		return pos;
	}
    
    
    static TablePotential getTablePotential(Potential potential) {
		return (potential instanceof TablePotential)?(TablePotential)potential:((ExactDistrPotential)potential).getTablePotential();
	}

	private Potential getPotential() {
		return node.getPotentials().get(0);
	}

	public Variable getVariable() {
		return node.getVariable();
	}
	
	@Override protected void doEdit() {

		TablePotential tablePotential = getTablePotential();
		

		//Remove the column of uncertain values
		if (!wasNullOldUncertainColumn) {
			tablePotential.placeUncertainColumn(null,getVariable(), basePosition);
		}
		//If all the elements are null then the uncertain value object is set to null
		if (!hasUncertainValues(tablePotential.getUncertainValues())) {
			tablePotential.setUncertainValues(null);
		}

	}
    
    public TablePotential getTablePotential() {
		return getTablePotential(getPotential());
	}
	
	@Override public void undo() {
		super.undo();

		TablePotential potential = getTablePotential();

		UncertainValue[] table = potential.getUncertainValues();
		//Create uncertain values table if it is null
		if (table == null) {
        }
		//Restore the elements of the uncertain column
		potential.placeUncertainColumn(oldUncertainColumn,getVariable(),basePosition);
	}
    
    private static boolean hasUncertainValues(UncertainValue[] auxUncertainTable) {
		boolean hasUncertainValues;
		if ((auxUncertainTable == null) || (auxUncertainTable.length == 0)) {
			hasUncertainValues = false;
		} else {
			hasUncertainValues = false;
			for (int i = 0; (i < auxUncertainTable.length) && !hasUncertainValues; i++) {
				hasUncertainValues = (auxUncertainTable[i] != null);
			}
		}
		return hasUncertainValues;
	}

}