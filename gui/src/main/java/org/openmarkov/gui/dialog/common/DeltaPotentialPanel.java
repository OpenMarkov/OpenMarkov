/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;
import org.openmarkov.gui.exception.ValueOutOfDomainRangeException;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * Panel for editing a {@link DeltaPotential}, allowing the user to select the
 * fixed state (for discrete variables) or enter a numeric value (for continuous variables).
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = DeltaPotential.class)
public class DeltaPotentialPanel
        extends PotentialPanel {
    
    private JComboBox<String> stateComboBox;
    private JSpinner valueSpinner;
    private Node node;
    private double defaultSpinnerValue;
    // Allowed range for the delta potential
    private double minValue;
    private double maxValue;
    
    public DeltaPotentialPanel(Node node) {
        super();
        this.node = node;
        initComponents();
        
        setData(node);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel namelessPanel = new JPanel();
        namelessPanel.setBorder(new EtchedBorder());
        
        if (node.getVariable().getVariableType() != VariableType.FINITE_STATES) {
            // Get the min and max values for the interval
            
            // If is left closed, we get the nearest number
            if (node.getVariable().getPartitionedInterval().isLeftClosed()) {
                minValue = node.getVariable().getPartitionedInterval().getMin();
            } else {
                minValue = node.getVariable().getPartitionedInterval().getMin() + node.getVariable().getPrecision();
            }
            // If is right closed, we get the nearest number
            if (node.getVariable().getPartitionedInterval().isRightClosed()) {
                maxValue = node.getVariable().getPartitionedInterval().getMax();
            } else {
                maxValue = node.getVariable().getPartitionedInterval().getMax() - node.getVariable().getPrecision();
            }
            
            // Calculate the mean value
            if (minValue == Double.NEGATIVE_INFINITY) {
                if (maxValue == Double.POSITIVE_INFINITY) {
                    defaultSpinnerValue = 0;
                } else {
                    defaultSpinnerValue = maxValue / 2;
                }
            } else if (maxValue == Double.POSITIVE_INFINITY) {
                defaultSpinnerValue = minValue * 2;
            } else {
                defaultSpinnerValue = minValue + Math.abs(maxValue - minValue) / 2;
            }
            //Create the model with the defaultValue, the min and max values and the precision
            SpinnerNumberModel model = new SpinnerNumberModel(defaultSpinnerValue, minValue, maxValue,
                                                              node.getVariable().getPrecision());
            
            valueSpinner = new JSpinner(model);
            valueSpinner.setPreferredSize(new Dimension(100, 20));
            JLabel valueLabel = new JLabel("Numeric value:");
            valueLabel.setLabelFor(valueSpinner);
            namelessPanel.add(valueLabel);
            namelessPanel.add(valueSpinner);
        } else {
            stateComboBox = new JComboBox<String>();
            stateComboBox.setPreferredSize(new Dimension(100, 20));
            JLabel stateLabel = new JLabel("State:");
            stateLabel.setLabelFor(stateComboBox);
            namelessPanel.add(stateLabel);
            namelessPanel.add(stateComboBox);
        }
        namelessPanel.setPreferredSize(new Dimension(200, 50));
        add(namelessPanel);
    }
    
    @Override public void setData(Node node) {
        this.node = node;
        DeltaPotential oldPotential = null;
        if (!node.getPotentials().isEmpty() && node.getPotentials().get(0) instanceof DeltaPotential) {
            oldPotential = (DeltaPotential) node.getPotentials().get(0);
        }
        // The model inits the valid range and the mean value
        if (node.getVariable().getVariableType() != VariableType.FINITE_STATES) {
            if (oldPotential != null) {
                
                double value = oldPotential.getNumericValue();
                // If the value is in the bounds and is not equal to any Infinity (positive or negative)
                // we put these value into the spinner
                if (value >= minValue && value <= maxValue && (
                        !(
                                value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY
                        )
                )) {
                    valueSpinner.setValue(value);
                }
            }
        } else {
            stateComboBox.removeAllItems();
            for (State state : node.getVariable().getStates()) {
                stateComboBox.addItem(state.getName());
            }
            
            if (oldPotential != null) {
                stateComboBox.setSelectedItem(oldPotential.getState().getName());
            }
        }
    }
    
    @Override
    public boolean saveChanges() throws DoEditException, BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong {
        boolean result = super.saveChanges();
        Potential oldPotential = node.getPreviousPotential();
        Potential newPotential = null;
        if (node.getVariable().getVariableType() != VariableType.FINITE_STATES) {
            double numericValue = Double.parseDouble(valueSpinner.getValue().toString());
            PartitionedInterval domain = node.getVariable().getPartitionedInterval();
            if (numericValue > domain.getMax() || numericValue < domain.getMin()) {
                throw new UnrecoverableException(new ValueOutOfDomainRangeException(numericValue, domain));
            }
            newPotential = new DeltaPotential(oldPotential.getVariables(), oldPotential.getPotentialRole(),
                                              numericValue);
        } else {
            int selectedIndex = stateComboBox.getSelectedIndex();
            State state = node.getVariable().getStates()[selectedIndex];
            newPotential = new DeltaPotential(oldPotential.getVariables(), oldPotential.getPotentialRole(), state);
        }
        newPotential.setComment(oldPotential.getComment());
        PotentialChangeEdit edit = new PotentialChangeEdit(node, oldPotential, newPotential);
        edit.executeEdit();
        return result;
        
    }
    
    @Override public void close() {
        // Do nothing
    }
    
}
