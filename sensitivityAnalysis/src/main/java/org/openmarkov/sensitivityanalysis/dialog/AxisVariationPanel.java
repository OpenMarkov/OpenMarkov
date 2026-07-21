/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.dialog;

import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisConfiguration;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * Panel with all controls needed to select axis variation of one or two uncertain parameters
 *
 * @author jperez-martin
 */
public class AxisVariationPanel extends JPanel {
    
    private StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
    
    /**
     * Sensitivity analysis controller
     */
    private SensitivityAnalysisController controller;
    
    // First axis elements
    /**
     * Panel with the variation of X axis parameter
     */
    private JPanel horizontalVariationPanel;
    
    /**
     * Variation type of X axis
     */
    private DeterministicAxisVariationType typeSelectedX;
    
    /**
     * Selector of variation type on X axis
     */
    private JComboBox<String> variationTypesComboBoxX;
    
    /**
     * Panel with the parameters
     */
    private JPanel variationTypeParametersPanelX;
    
    /**
     * Variation of X axis
     */
    private AxisVariation variationXAxis;
    
    // Second axis elements (same as above but for second axis)
    private JPanel verticalVariationPanel;
    private DeterministicAxisVariationType typeSelectedY;
    private JComboBox<String> variationTypesComboBoxY;
    private JPanel variationTypeParametersPanelY;
    private AxisVariation variationYAxis;
    
    /**
     * Panel with all controls needed to select axis variation of one or two uncertain parameters
     *
     * @param controller Controller with the model
     */
    public AxisVariationPanel(SensitivityAnalysisController controller) {
        super();
        this.controller = controller;
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        variationXAxis = new AxisVariation();
        variationYAxis = new AxisVariation();
        this.add(getHorizontalVariationPanel());
        this.add(getVerticalVariationPanel());
        
        // Group of flags for the gui
        SensitivityAnalysisConfiguration configuration = controller.getConfiguration();
        
        // Configuration for the first analysis type of deterministic and probabilistic cases
        if (!configuration.isDeterministic()) {
            for (Component component : horizontalVariationPanel.getComponents()) {
                component.setEnabled(false);
            }
            variationTypesComboBoxX.setEnabled(false);
            variationTypeParametersPanelX.setEnabled(false);
            for (Component component : variationTypeParametersPanelX.getComponents()) {
                component.setEnabled(false);
            }
            
            for (Component component : verticalVariationPanel.getComponents()) {
                component.setEnabled(false);
            }
            variationTypesComboBoxY.setEnabled(false);
            variationTypeParametersPanelY.setEnabled(false);
            for (Component component : variationTypeParametersPanelY.getComponents()) {
                component.setEnabled(false);
            }
        } else {
            if (!configuration.isBiaxial()) {
                for (Component component : verticalVariationPanel.getComponents()) {
                    component.setEnabled(false);
                }
                variationTypesComboBoxY.setEnabled(false);
                variationTypeParametersPanelY.setEnabled(false);
                for (Component component : variationTypeParametersPanelY.getComponents()) {
                    component.setEnabled(false);
                }
            }
        }
        
    }
    
    /**
     * Panel for horizontal variation (X axis)
     *
     * @return
     */
    public JPanel getHorizontalVariationPanel() {
        boolean isXAxis = true;
        horizontalVariationPanel = new JPanel();
        horizontalVariationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel horizontalVariationLabel = new JLabel(stringDatabase.getString("SensitivityAnalysis.Axis.Horizontal"));
        horizontalVariationPanel.add(horizontalVariationLabel);
        
        horizontalVariationPanel.add(getAxisTypes(isXAxis));
        
        horizontalVariationPanel.add(getVariationTypeParametersPanel(isXAxis));
        return horizontalVariationPanel;
    }
    
    /**
     * Panel for vertical variation (Y axis)
     *
     * @return
     */
    public JPanel getVerticalVariationPanel() {
        boolean isXAxis = false;
        verticalVariationPanel = new JPanel();
        verticalVariationPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel verticalVariationLabel = new JLabel(stringDatabase.getString("SensitivityAnalysis.Axis.Vertical"));
        verticalVariationPanel.add(verticalVariationLabel);
        
        verticalVariationPanel.add(getAxisTypes(isXAxis));
        
        verticalVariationPanel.add(getVariationTypeParametersPanel(isXAxis));
        
        return verticalVariationPanel;
    }
    
    /**
     * Allowed axis types for X or Y axis
     *
     * @param isXAxis true if X axis, false if Y axis
     *
     * @return
     */
    public JComboBox<String> getAxisTypes(boolean isXAxis) {
        JComboBox<String> jComboBox = new JComboBox<>();
        
        for (DeterministicAxisVariationType type : DeterministicAxisVariationType.values()) {
            jComboBox.addItem(stringDatabase.getString(type.toString()));
        }
        
        if (isXAxis) {
            variationTypesComboBoxX = jComboBox;
            variationTypesComboBoxX
                    .removeItem(stringDatabase.getString(DeterministicAxisVariationType.PREV.toString()));
        } else {
            variationTypesComboBoxY = jComboBox;
        }
        
        if (isXAxis) {
            jComboBox.addActionListener(e -> {
                boolean isXaxis = true;
                setVariationTypeParameters(isXaxis);
            });
        } else {
            jComboBox.addActionListener(e -> {
                boolean isXaxis = false;
                setVariationTypeParameters(isXaxis);
            });
        }
        
        jComboBox.setSelectedIndex(0);
        setVariationTypeParameters(isXAxis);
        return jComboBox;
    }
    
    /**
     * Update selected variation type
     *
     * @param isXAxis true if X axis, false if Y axis
     */
    private void setVariationTypeParameters(boolean isXAxis) {
        String selectedItem;
        if (isXAxis) {
            selectedItem = variationTypesComboBoxX.getSelectedItem().toString();
            if (typeSelectedX == null || !selectedItem.equals(stringDatabase.getString(typeSelectedX.toString()))) {
                if (selectedItem.equals(stringDatabase.getString(DeterministicAxisVariationType.PORV.toString()))) {
                    typeSelectedX = DeterministicAxisVariationType.PORV;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.RORV.toString()))) {
                    typeSelectedX = DeterministicAxisVariationType.RORV;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.POPP.toString()))) {
                    typeSelectedX = DeterministicAxisVariationType.POPP;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.UDIN.toString()))) {
                    typeSelectedX = DeterministicAxisVariationType.UDIN;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.PREV.toString()))) {
                    typeSelectedX = DeterministicAxisVariationType.PREV;
                }
                variationXAxis.setVariationType(typeSelectedX);
            }
        } else {
            selectedItem = variationTypesComboBoxY.getSelectedItem().toString();
            if (typeSelectedY == null || !selectedItem.equals(stringDatabase.getString(typeSelectedY.toString()))) {
                if (selectedItem.equals(stringDatabase.getString(DeterministicAxisVariationType.PORV.toString()))) {
                    typeSelectedY = DeterministicAxisVariationType.PORV;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.RORV.toString()))) {
                    typeSelectedY = DeterministicAxisVariationType.RORV;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.POPP.toString()))) {
                    typeSelectedY = DeterministicAxisVariationType.POPP;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.UDIN.toString()))) {
                    typeSelectedY = DeterministicAxisVariationType.UDIN;
                } else if (selectedItem
                        .equals(stringDatabase.getString(DeterministicAxisVariationType.PREV.toString()))) {
                    typeSelectedY = DeterministicAxisVariationType.PREV;
                }
                variationYAxis.setVariationType(typeSelectedY);
            }
        }
        updateController(isXAxis);
        setMainPanel(isXAxis);
        
    }
    
    /**
     * Get the Panel with the predefined variations for the selected variation type
     *
     * @param isXAxis true if X axis, false if Y axis
     *
     * @return
     */
    public JPanel getVariationTypeParametersPanel(boolean isXAxis) {
        JPanel panel = new JPanel();
        DeterministicAxisVariationType type;
        if (isXAxis) {
            variationTypeParametersPanelX = panel;
            type = typeSelectedX;
        } else {
            variationTypeParametersPanelY = panel;
            type = typeSelectedY;
        }
        
        // Set the predefined percentages and create the action listener to update model for each variation type
        if (type == DeterministicAxisVariationType.PORV) {
            JComboBox<Double> percentage = new JComboBox<>();
            percentage.addItem(10.0);
            percentage.addItem(20.0);
            percentage.addItem(25.0);
            percentage.setEditable(true);
            
            if (isXAxis) {
                percentage.addActionListener(e -> {
                    double percetangeOverReferenceValueX = (Double) ((JComboBox<Double>) e.getSource())
                            .getSelectedItem();
                    setVariationAxisValue(percetangeOverReferenceValueX, true);
                });
            } else {
                percentage.addActionListener(e -> {
                    double percetangeOverReferenceValueY = (Double) ((JComboBox<Double>) e.getSource())
                            .getSelectedItem();
                    setVariationAxisValue(percetangeOverReferenceValueY, false);
                });
            }
            percentage.setSelectedItem(25.0);
            setVariationAxisValue(25.0, isXAxis);
            
            panel.add(percentage);
            panel.add(new JLabel("%"));
            
        } else if (type == DeterministicAxisVariationType.RORV) {
            JComboBox<Double> ratio = new JComboBox<>();
            ratio.addItem(1.10);
            ratio.addItem(1.25);
            ratio.addItem(1.50);
            ratio.addItem(2.00);
            ratio.setEditable(true);
            if (isXAxis) {
                ratio.addActionListener(e -> {
                    double ratioOverReferenceValueX = (Double) ((JComboBox<Double>) e.getSource())
                            .getSelectedItem();
                    setVariationAxisValue(ratioOverReferenceValueX, true);
                });
            } else {
                ratio.addActionListener(e -> {
                    double ratioOverReferenceValueY = (Double) ((JComboBox<Double>) e.getSource())
                            .getSelectedItem();
                    setVariationAxisValue(ratioOverReferenceValueY, false);
                });
            }
            setVariationAxisValue(1.10, isXAxis);
            
            panel.add(ratio);
            
        } else if (type == DeterministicAxisVariationType.POPP) {
            JComboBox<Double> percentaje = new JComboBox<>();
            percentaje.addItem(80.0);
            percentaje.addItem(90.0);
            percentaje.addItem(95.0);
            percentaje.addItem(100.0);
            percentaje.setEditable(true);
            if (isXAxis) {
                percentaje.addActionListener(e -> {
                    double percetangeOfParameterProbabilityX = (Double) ((JComboBox<Double>) e.getSource())
                            .getSelectedItem();
                    setVariationAxisValue(percetangeOfParameterProbabilityX, true);
                });
            } else {
                percentaje.addActionListener(e -> {
                    double percetangeOfParameterProbabilityY = (Double) ((JComboBox<Double>) e.getSource())
                            .getSelectedItem();
                    setVariationAxisValue(percetangeOfParameterProbabilityY, false);
                });
            }
            setVariationAxisValue(80.0, isXAxis);
            panel.add(percentaje);
            panel.add(new JLabel("%"));
            
        } else if (type == DeterministicAxisVariationType.UDIN) {
            panel.add(new JLabel(stringDatabase.getString("SensitivityAnalysis.General.From")));
            JTextField lowerBoundTextField = new JTextField(5);
            lowerBoundTextField.setText("0.000");
            setBoundValue(0, true, isXAxis);
            if (isXAxis) {
                lowerBoundTextField.addActionListener(e -> {
                    String lowerBoundString = ((JTextField) e.getSource()).getText();
                    double lowerBound = Double.parseDouble(lowerBoundString);
                    setBoundValue(lowerBound, true, true);
                });
            } else {
                lowerBoundTextField.addActionListener(e -> {
                    String lowerBoundString = ((JTextField) e.getSource()).getText();
                    double lowerBound = Double.parseDouble(lowerBoundString);
                    setBoundValue(lowerBound, true, false);
                });
            }
            
            panel.add(lowerBoundTextField);
            
            panel.add(new JLabel(stringDatabase.getString("SensitivityAnalysis.General.To")));
            JTextField upperBoundTextField = new JTextField(5);
            upperBoundTextField.setText("1.000");
            setBoundValue(1, false, isXAxis);
            
            if (isXAxis) {
                upperBoundTextField.addActionListener(e -> {
                    String upperBoundString = ((JTextField) e.getSource()).getText();
                    double upperBound = Double.parseDouble(upperBoundString);
                    setBoundValue(upperBound, false, true);
                    
                });
            } else {
                upperBoundTextField.addActionListener(e -> {
                    String upperBoundString = ((JTextField) e.getSource()).getText();
                    double upperBound = Double.parseDouble(upperBoundString);
                    setBoundValue(upperBound, false, false);
                });
            }
            
            panel.add(upperBoundTextField);
        }
        
        return panel;
    }
    
    /**
     * Re-build main panel for each axis (update gui)
     *
     * @param isXAxis true if X axis, false if Y axis
     */
    private void setMainPanel(boolean isXAxis) {
        if (variationTypeParametersPanelX != null && variationTypeParametersPanelY != null) {
            if (isXAxis) {
                horizontalVariationPanel.setVisible(false);
                horizontalVariationPanel.remove(variationTypeParametersPanelX);
                variationTypeParametersPanelX = null;
                horizontalVariationPanel.add(getVariationTypeParametersPanel(isXAxis));
                horizontalVariationPanel.setVisible(true);
            } else {
                verticalVariationPanel.setVisible(false);
                verticalVariationPanel.remove(variationTypeParametersPanelY);
                variationTypeParametersPanelY = null;
                verticalVariationPanel.add(getVariationTypeParametersPanel(isXAxis));
                verticalVariationPanel.setVisible(true);
            }
        }
    }
    
    /**
     * Set bounds for user defined intervals
     *
     * @param value        selected value
     * @param isLowerBound true if lower bound, false if upperbound
     * @param isXAxis      true if X axis, false if Y axis
     */
    public void setBoundValue(double value, boolean isLowerBound, boolean isXAxis) {
        if (isXAxis) {
            if (isLowerBound) {
                variationXAxis.getVariationBounds()[0] = value;
            } else {
                variationXAxis.getVariationBounds()[1] = value;
            }
            
        } else {
            if (isLowerBound) {
                variationYAxis.getVariationBounds()[0] = value;
            } else {
                variationYAxis.getVariationBounds()[1] = value;
            }
        }
        updateController(isXAxis);
        
    }
    
    /**
     * Update variation axis value
     *
     * @param value   Selected value
     * @param isXAxis true if X axis, false if Y axis
     */
    public void setVariationAxisValue(double value, boolean isXAxis) {
        if (isXAxis) {
            variationXAxis.setVariationValue(value);
        } else {
            variationYAxis.setVariationValue(value);
        }
        
        updateController(isXAxis);
    }
    
    /**
     * Updates the controller and model with the selected variation for axis
     *
     * @param isXAxis true if X axis, false if Y axis
     */
    private void updateController(boolean isXAxis) {
        if (isXAxis) {
            controller.getSensitivityAnalysisModel().setHorizontalAxisVariation(variationXAxis);
        } else {
            // If the variation type is "Same as previous" we need to select the same variation as the X axis.
            if (typeSelectedY == DeterministicAxisVariationType.PREV) {
                controller.getSensitivityAnalysisModel().setVerticalAxisVariation(variationXAxis);
            } else {
                controller.getSensitivityAnalysisModel().setVerticalAxisVariation(variationYAxis);
            }
            
        }
    }
    
}
