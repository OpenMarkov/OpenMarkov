/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.DistributionTablePotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Edit that modifies the uncertain values and numeric values of a single column in a
 * node's potential table. Used for sensitivity analysis parameters.
 *
 * @author mluque
 * @version 1 23/06/11
 */
@SuppressWarnings("serial") public class UncertainValuesEdit extends PNEdit {
    private final List<Double> newValuesColumn;
    private final List<UncertainValue> newUncertainColumn;
    private final List<Double> oldValuesColumn;
    private final List<UncertainValue> oldUncertainColumn;
    private final int basePosition;
    private final Node node;
    private final boolean isChanceVariable;
    private final boolean wasNullOldUncertainValues;
    /**
     * Selected column in the values table
     */
    private final int selectedColumn;
    /**
     * True when {@link #doEdit()} replaced the node's plain {@link TablePotential} with an
     * {@link UncertainTablePotential}; {@link #undo()} then restores {@link #originalRawPotential}.
     */
    private boolean potentialWasUpgraded;
    /**
     * The node's original potential, kept so {@link #undo()} can restore it after an upgrade.
     */
    private Potential originalRawPotential;
    
    /**
     * Creates a new {@code AddNodeEdit} with the network where the new
     * new node will be added and basic information about it.
     *
     * @param node             the new node
     * @param uncertainColumn  Uncertain column
     * @param valuesColumn     Values column
     * @param basePosition     Base position
     * @param selectedColumn   Selected column
     * @param isChanceVariable Is chance variable?
     */
    public UncertainValuesEdit(Node node, List<UncertainValue> uncertainColumn, List<Double> valuesColumn,
                               int basePosition, int selectedColumn, boolean isChanceVariable) {
        super(node.getProbNet());
        this.node = node;
        this.isChanceVariable = isChanceVariable;
        Variable variable = node.getVariable();
        newUncertainColumn = uncertainColumn;
        newValuesColumn = valuesColumn;
        this.basePosition = basePosition;
        UncertainValue[] oldUncertainValues = getPotential().getUncertainValues();
        wasNullOldUncertainValues = oldUncertainValues == null;
        oldUncertainColumn = wasNullOldUncertainValues ? null : getColumn(oldUncertainValues, variable, basePosition);
        oldValuesColumn = getColumn(getPotential().getValues(), variable, basePosition);
        this.selectedColumn = selectedColumn;
    }
    
    public int getBasePosition() {
        return basePosition;
    }
    
    public boolean isChanceVariable() {
        return isChanceVariable;
    }
    
    public Node getNode() {
        return node;
    }
    
    private List<Double> getColumn(double[] values, Variable variable, int basePosition) {
        List<Double> column = new ArrayList<>();
        int numElements = (isChanceVariable) ? variable.getNumStates() : 1;
        for (int i = 0; i < numElements; i++) {
            column.add(values[basePosition + i]);
        }
        return column;
    }
    
    private List<UncertainValue> getColumn(UncertainValue[] uncertainValues, Variable variable, int basePosition) {
        int numElements = (isChanceVariable) ? variable.getNumStates() : 1;
        List<UncertainValue> column = new ArrayList<>(Arrays.asList(uncertainValues)
                                                            .subList(basePosition, numElements + basePosition));
        return column;
    }
    
    public int getSelectedColumn() {
        return selectedColumn;
    }
    
    private TablePotential getPotential() {
        if (node.getPotentials().get(0) instanceof TablePotential) {
            return (TablePotential) (node.getPotentials().get(0));
        }
        // 19/05/2024 - first attempt to DESnets PSA; currently only working with DistributionTablePotential and TransitionTablePotential
        if (node.getPotentials().get(0) instanceof TransitionTablePotential) {
            return ((TransitionTablePotential) (node.getPotentials().get(0))).getTablePotential();
        }
        if (node.getPotentials().get(0) instanceof DistributionTablePotential) {
            return ((DistributionTablePotential) node.getPotentials().get(0)).getTableWithEvents().getTablePotential();
        }
        if (node.getPotentials().get(0) instanceof ExactDistrPotential) {
            return ((ExactDistrPotential) (node.getPotentials().get(0))).getTablePotential();
        }
        return null;
    }
    
    public Variable getVariable() {
        return node.getVariable();
    }
    
    @Override protected void doEdit() {
        TablePotential potential = prepareUncertainPotential();
        potential.setUncertainValuesConsistently(newUncertainColumn, newValuesColumn, basePosition);
    }

    /**
     * Returns the {@link TablePotential} that will store the uncertain values, upgrading the node's
     * plain {@link TablePotential} to an {@link UncertainTablePotential} when needed.
     *
     * <p>Since uncertain values were extracted out of {@code TablePotential} into the
     * {@code UncertainTablePotential} subclass, a plain {@code TablePotential} rejects them. A node
     * normally starts without uncertainty, so the first edit must replace its potential with an
     * {@code UncertainTablePotential} that keeps the same values, role and metadata.
     *
     * @return a potential able to carry uncertain values
     */
    private TablePotential prepareUncertainPotential() {
        TablePotential working = getPotential();
        if (working == null) {
            throw new UnsupportedOperationException(
                    "Uncertain values are not supported for the potential of node " + node.getName() + ".");
        }
        if (working instanceof UncertainTablePotential) {
            return working;   // already uncertainty-capable (includes ExactDistrPotential's inner table)
        }
        Potential raw = node.getPotentials().get(0);
        if (raw != working) {
            // 'working' is a plain TablePotential wrapped inside another potential (e.g. a Transition or
            // Distribution table). Upgrading it would require replacing it inside its wrapper, which is
            // not supported here; fail with a clear message instead of an opaque one further down.
            throw new UnsupportedOperationException(
                    "Editing uncertain values is not supported for potentials of type "
                    + raw.getClass().getSimpleName() + " whose inner table carries no uncertainty.");
        }
        UncertainTablePotential upgraded = upgradeToUncertain(working);
        node.setPotential(upgraded);
        originalRawPotential = working;
        potentialWasUpgraded = true;
        return upgraded;
    }

    /**
     * Builds an {@link UncertainTablePotential} equivalent to {@code source}: same variables, role,
     * numeric values (cloned, so the original is left intact for undo) and metadata.
     */
    private static UncertainTablePotential upgradeToUncertain(TablePotential source) {
        UncertainTablePotential upgraded = new UncertainTablePotential(
                source.getVariables(), source.getPotentialRole(), source.getValues().clone());
        upgraded.properties = source.properties;
        if (source.isAdditive()) {
            upgraded.setCriterion(source.getCriterion());
        }
        return upgraded;
    }

    @Override public void undo() {
        super.undo();
        if (potentialWasUpgraded) {
            // The node had no uncertainty before this edit; restore the original plain potential, which
            // still holds the original values (they were cloned into the upgraded copy, not moved).
            node.setPotential(originalRawPotential);
            potentialWasUpgraded = false;
            return;
        }
        TablePotential potential = getPotential();
        if (wasNullOldUncertainValues) {
            potential.setUncertainValues(null);
            // Restore only the edited column in place. Using setValues() here would replace the whole
            // values table with a single column's worth of data, corrupting every other column.
            potential.placeValuesColumn(oldValuesColumn, basePosition);
        } else {
            potential.setUncertainValuesConsistently(oldUncertainColumn, oldValuesColumn, basePosition);
        }
    }

}