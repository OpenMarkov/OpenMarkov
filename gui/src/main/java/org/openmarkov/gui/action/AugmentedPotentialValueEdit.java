/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.potential.AugmentedProbTable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.gui.component.PotentialsTablePanelOperations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.openmarkov.core.expression.VariableExpression.Common.COMPLEMENT;

/**
 * @author carmenyago
 * @version 1.1 22/05/2017 Changed into AugmentedPotentialValueEdit: now is the edit for all the AugmentedPotentials
 */
@SuppressWarnings("serial") public class AugmentedPotentialValueEdit extends PotentialChangeEdit {
    /**
     * The column of the table where is the potential
     */
    private int col;
    /**
     * The row of the table where is the potential
     */
    private int row;
    
    /**
     * Index of the value selected
     */
    private int indexSelected;
    
    
    /**
     * The AugmentedProbTable potential
     */
    private AugmentedProbTablePotential newAugmentedProbTablePotential;
    /**
     * Old table potential
     */
    private AugmentedProbTablePotential oldAugmentedProbTablePotential;
    
    /**
     * The UnivariateDistr Potential
     */
    private UnivariateDistrPotential newUnivariateDistrPotential;
    /**
     * Old table potential
     */
    private UnivariateDistrPotential oldUnivariateDistrPotential;
    
    /**
     * Pseudo-util class with common operations used  in potential tables
     */
    private PotentialsTablePanelOperations tablePotentialsPanelOperations;
    
    /**
     * the table potential
     */
    private AugmentedProbTable newAugmentedProbTable;
    private VariableExpression[] newAugmentedValues;
    
    /**
     * Node
     */
    private final Node node;
    
    // Constructor
    
    /**
     * Creates a new {@code NodePotentialEdit} specifying the node to be
     * edited, the new value of the potential, the row and column where is the
     * value to be modified and a priority list for potentials updating.
     *
     * @param node     the node to be edited
     * @param newValue the new value
     * @param col      the column in the edited table
     * @param row      the row in the edited table
     */
    public AugmentedPotentialValueEdit(Node node, VariableExpression newValue, int row, int col, List<Integer> priorityList) {
        super(node, null, null);
        boolean isAugmentedProbTablePotential = false;
        
        this.node = node;
        oldPotential = node.getPotentials().get(0);
        if (oldPotential instanceof AugmentedProbTablePotential oldAugmentedPotential) {
            oldAugmentedProbTablePotential = oldAugmentedPotential;
            newAugmentedProbTablePotential = new AugmentedProbTablePotential(oldAugmentedProbTablePotential);
            newPotential = newAugmentedProbTablePotential;
            newAugmentedProbTable = newAugmentedProbTablePotential.getAugmentedProbTable();
            newAugmentedValues = newAugmentedProbTable.getFunctionValues();
            isAugmentedProbTablePotential = true;
        } else {
            oldUnivariateDistrPotential = (UnivariateDistrPotential) oldPotential;
            newUnivariateDistrPotential = new UnivariateDistrPotential(oldUnivariateDistrPotential);
            newPotential = newUnivariateDistrPotential;
            newAugmentedProbTable = newUnivariateDistrPotential.getAugmentedProbTable();
            newAugmentedValues = newAugmentedProbTable.getFunctionValues();
        }
        this.row = row;
        this.col = col;
        this.tablePotentialsPanelOperations = new PotentialsTablePanelOperations();
        this.setIndexSelected(PotentialsTablePanelOperations.calculateLastEditableRow(newAugmentedProbTable) - row);
        
        this.indexSelected = PotentialsTablePanelOperations.getPotentialIndex(row, col, newAugmentedProbTable);
        //Set the entire column to "Complement"
        if (isAugmentedProbTablePotential) {
            int firstEditableRow = PotentialsTablePanelOperations.calculateFirstEditableRow(newAugmentedProbTable);
            int lastEditableRow = PotentialsTablePanelOperations.calculateLastEditableRow(newAugmentedProbTable);
            
            newAugmentedValues[indexSelected] = newValue;
            
            @Nullable Double newConstantValue = null;
            try {
                newConstantValue = Double.valueOf(newAugmentedValues[indexSelected].evaluateWith(Collections.emptyMap()));
                if (newConstantValue > 1 || newConstantValue < 0) {
                    newConstantValue = Math.clamp(newConstantValue, 0.0, 1.0);
                    newAugmentedValues[indexSelected] = new VariableExpression(Collections.emptyList(), Double.toString(newConstantValue));
                    newValue = newAugmentedValues[indexSelected];
                }
            } catch (NonProjectablePotentialException.CannotEvaluate |
                     NonProjectablePotentialException.CannotResolveVariable e) {
                newConstantValue = null;
            }
            
            int tableRowOffset = newAugmentedProbTable.getNumVariables() - 1;
            var unmodifiableIndex = row - tableRowOffset;
            
            var valuesOfColumn = IntStream.rangeClosed(firstEditableRow, lastEditableRow)
                                          .mapToObj(r -> newAugmentedValues[PotentialsTablePanelOperations.getPotentialIndex(r, col, newAugmentedProbTable)])
                                          .toArray(VariableExpression[]::new);
            
            var modifiableIndexes = IntStream.range(0, valuesOfColumn.length)
                                             .filter(i -> i != unmodifiableIndex)
                                             .boxed()
                                             .toList();
            
            if (newConstantValue == null) {
                //Turns every other values to Complement.
                for (int index : modifiableIndexes) {
                    valuesOfColumn[index] = COMPLEMENT;
                }
            } else {
                
                var numericModifiableIndexes = modifiableIndexes
                        .stream()
                        .filter(index -> valuesOfColumn[index].isEvaluable(Collections.emptyMap()))
                        .sorted(Comparator.reverseOrder())
                        // Orders values so cells with numbers are first, while cells with constant functions such as
                        // "abs(0.2)" go last.
                        .sorted((index1, index2) -> {
                            Double value1 = null;
                            Double value2 = null;
                            try {
                                value1 = Double.valueOf(valuesOfColumn[index1].asStringExpression().trim());
                            } catch (NumberFormatException _) {
                                value1 = null;
                            }
                            try {
                                value2 = Double.valueOf(valuesOfColumn[index2].asStringExpression().trim());
                            } catch (NumberFormatException _) {
                                value2 = null;
                            }
                            if ((value1 == null && value2 == null) || (value1 != null && value2 != null)) {
                                int tableIndex1 = PotentialsTablePanelOperations.getPotentialIndex(index1 + tableRowOffset, col, newAugmentedProbTable);
                                int tableIndex2 = PotentialsTablePanelOperations.getPotentialIndex(index2 + tableRowOffset, col, newAugmentedProbTable);
                                Integer priority1 = priorityList.indexOf(tableIndex1);
                                Integer priority2 = priorityList.indexOf(tableIndex2);
                                int res = priority1.compareTo(priority2);
                                return res;
                            }
                            if (value1 != null) {
                                return -1;
                            }
                            return 1;
                        })
                        .toList();
                
                //Turns non-constant values (functions) to Complement.
                for (int index : modifiableIndexes) {
                    if (!numericModifiableIndexes.contains(index)) {
                        valuesOfColumn[index] = COMPLEMENT;
                    }
                }
                
                for (int index : numericModifiableIndexes) {
                    var accumulatedProbability = numericModifiableIndexes.stream()
                                                                         .mapToDouble(numericModifiableIndex -> {
                                                                             try {
                                                                                 return Double.valueOf(valuesOfColumn[numericModifiableIndex].evaluateWith(Collections.emptyMap()));
                                                                             } catch (
                                                                                     NonProjectablePotentialException.CannotEvaluate |
                                                                                     NonProjectablePotentialException.CannotResolveVariable e) {
                                                                                 throw new UnreachableException(e);
                                                                             }
                                                                         })
                                                                         .sum();
                    accumulatedProbability += newConstantValue;
                    var roundedAccumulatedProbability = Util.roundAndReduce(accumulatedProbability, PotentialsTablePanelOperations.DEFAULT_EPSILON, PotentialsTablePanelOperations.DEFAULT_MAX_DECIMALS);
                    if (roundedAccumulatedProbability == 1.0) {
                        break;
                    }
                    Double modifyingValue = null;
                    try {
                        modifyingValue = Double.valueOf(valuesOfColumn[index].evaluateWith(Collections.emptyMap()));
                    } catch (NonProjectablePotentialException.CannotEvaluate |
                             NonProjectablePotentialException.CannotResolveVariable e) {
                        throw new UnreachableException(e);
                    }
                    var newModifyingValue = Math.clamp(modifyingValue + (1 - accumulatedProbability), 0, 1);
                    
                    newModifyingValue = Util.roundAndReduce(newModifyingValue, PotentialsTablePanelOperations.DEFAULT_EPSILON, PotentialsTablePanelOperations.DEFAULT_MAX_DECIMALS);
                    
                    valuesOfColumn[index] = new VariableExpression(Collections.emptyList(), Double.toString(newModifyingValue));
                }
            }
            for (int valueIndex = 0; valueIndex < valuesOfColumn.length; valueIndex++) {
                int tableIndex = PotentialsTablePanelOperations.getPotentialIndex(valueIndex + tableRowOffset, col, newAugmentedProbTable);
                newAugmentedValues[tableIndex] = valuesOfColumn[valueIndex];
            }
        }
        newAugmentedValues[indexSelected] = newValue;
    }
    
    /**
     * Creates a new {@code UnivariateDistrPotentialEdit} specifying the node to be and the new probability distribution.
     * This is used when the distribution of {@code UnivariateDistrPotential} is changed.
     *
     * @param node             - the node to be edited
     * @param distributionName - the name of the distribution to be created. Represents the attribute name in ProbDensFunctionType which represents the distribution class
     *
     * @see UnivariateDistrPotential
     */
    public AugmentedPotentialValueEdit(Node node, String distributionName) {
        super(node, null, null);
        
        this.node = node;
        //The old univariateDistrPotential
        oldPotential = node.getPotentials().get(0);
        oldUnivariateDistrPotential = (UnivariateDistrPotential) oldPotential;
        if (distributionName.equals(oldUnivariateDistrPotential.getProbDensFunctionName())) {
            newUnivariateDistrPotential = new UnivariateDistrPotential(oldUnivariateDistrPotential);
        } else {
            Class<? extends ProbDensFunction> newDistributionClass = ProbDensFunctionManager.getUniqueInstance()
                                                                                            .getProbDensFunctionClass(distributionName);
            newUnivariateDistrPotential = new UnivariateDistrPotential(oldPotential.getVariables(),
                                                                       newDistributionClass, oldPotential.getPotentialRole());
        }
        newPotential = newUnivariateDistrPotential;
        this.indexSelected = -1;
    }
    
    /**
     * Gets the row position associated to value edited if priorityList exists
     *
     * @param position position of the value in the array of values
     *
     * @return the position in the table
     */
    public int getRowPosition(int position) {
        int lastRow = PotentialsTablePanelOperations.calculateLastEditableRow(newAugmentedProbTable);
        return lastRow - position % newAugmentedProbTable.getDimensions()[0];
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
     * @return the indexSelected
     */
    public int getIndexSelected() {
        return indexSelected;
    }
    
    /**
     * @param indexSelected the indexSelected to set
     */
    public void setIndexSelected(int indexSelected) {
        this.indexSelected = indexSelected;
    }
    
}
