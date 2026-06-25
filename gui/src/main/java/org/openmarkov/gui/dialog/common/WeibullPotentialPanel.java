/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GLMPotential.MatrixType;
import org.openmarkov.core.model.network.potential.WeibullHazardPotential;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for editing a {@link WeibullHazardPotential}, including shape/scale parameters,
 * time variable selection, covariates, and an optional uncertainty matrix.
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = WeibullHazardPotential.class)
public class WeibullPotentialPanel extends PotentialPanel implements ItemListener, ActionListener {
    
    private static final String MATRIX_TYPE_COVARIANCE = "Covariance matrix";
    private static final String MATRIX_TYPE_CHOLESKY = "Cholesky decomposition";
    
    private Node node = null;
    private WeibullHazardPotential potential = null;
    private GLMPanel regressionPanel;
    private JTable uncertaintyTable;
    private JComboBox<String> timeVariableComboBox;
    private JCheckBox uncertaintyCheckBox;
    private JComboBox<String> matrixTypeComboBox;
    private JPanel uncertaintyPanel;
    private String currentMatrixType;
    private String selectedTimeVariable;
    
    public WeibullPotentialPanel(Node node) {
        super();
        initComponents();
        setData(node);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel deterministicPanel = new JPanel();
        deterministicPanel.setLayout(new BorderLayout());
        JLabel timeVariableLabel = new JLabel();
        timeVariableLabel.setText("Time variable:");
        timeVariableComboBox = new JComboBox<String>();
        timeVariableComboBox.setMinimumSize(new Dimension(100, 20));
        JPanel northPanel = new JPanel();
        timeVariableLabel.setLabelFor(timeVariableComboBox);
        northPanel.add(timeVariableLabel);
        northPanel.add(timeVariableComboBox);
        northPanel.setBorder(new EmptyBorder(10, 25, 10, 25));
        deterministicPanel.add(northPanel, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(new TitledBorder("Coefficients"));
        regressionPanel = new GLMPanel();
        regressionPanel.addActionListener(this);
        regressionPanel.setPreferredSize(new Dimension(600, 200));
        centerPanel.add(regressionPanel);
        deterministicPanel.add(centerPanel, BorderLayout.CENTER);
        uncertaintyCheckBox = new JCheckBox("Uncertainty");
        uncertaintyCheckBox.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                uncertaintyPanel.setVisible(uncertaintyCheckBox.isSelected());
                notifyPanelResizeEventListeners();
            }
        });
        deterministicPanel.add(uncertaintyCheckBox, BorderLayout.SOUTH);
        uncertaintyTable = new JTable();
        JScrollPane covarianceTablePanel = new JScrollPane(uncertaintyTable);
        covarianceTablePanel.setPreferredSize(new Dimension(500, 100));
        uncertaintyPanel = new JPanel();
        uncertaintyPanel.setLayout(new BorderLayout());
        matrixTypeComboBox = new JComboBox<String>(new String[]{MATRIX_TYPE_COVARIANCE, MATRIX_TYPE_CHOLESKY});
        matrixTypeComboBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!matrixTypeComboBox.getSelectedItem().equals(currentMatrixType)) {
                    currentMatrixType = matrixTypeComboBox.getSelectedItem().toString();
                    // clean uncertainty matrix;
                    CovarianceTableModel covarianceTableModel = (CovarianceTableModel) uncertaintyTable.getModel();
                    for (int rowIndex = 1; rowIndex < covarianceTableModel.getRowCount(); ++rowIndex) {
                        for (int columnIndex = 1; columnIndex <= rowIndex; ++columnIndex) {
                            covarianceTableModel.setValueAt(0.0, rowIndex, columnIndex);
                        }
                    }
                }
            }
        });
        currentMatrixType = MATRIX_TYPE_COVARIANCE;
        uncertaintyPanel.add(matrixTypeComboBox, BorderLayout.NORTH);
        uncertaintyTable.setTableHeader(null);
        TableCellRenderer cellRenderer = new CovarianceTableCellRenderer();
        uncertaintyTable.setDefaultRenderer(String.class, cellRenderer);
        uncertaintyTable.setDefaultRenderer(Double.class, cellRenderer);
        covarianceTablePanel.setColumnHeaderView(null);
        uncertaintyPanel.setBorder(new TitledBorder("Uncertainty"));
        uncertaintyPanel.add(covarianceTablePanel, BorderLayout.CENTER);
        add(deterministicPanel, BorderLayout.NORTH);
        add(uncertaintyPanel, BorderLayout.CENTER);
    }
    
    @Override public void setData(Node node) {
        this.node = node;
        this.potential = (WeibullHazardPotential) this.node.getPotentials().get(0);
        List<Variable> variables = potential.getVariables();
        Variable timeVariable = potential.getTimeVariable();
        double[] coefficients = potential.getCoefficients();
        VariableExpression[] covariates = potential.getCovariates();
        regressionPanel.setData(potential);
        
        timeVariableComboBox.removeAllItems();
        timeVariableComboBox.addItem("-- No time variable");
        for (int i = 1; i < variables.size(); ++i) {
            if (isValidTimeVariable(potential.getVariable(i))) {
                timeVariableComboBox.addItem(potential.getVariable(i).getName());
            }
        }
        if (timeVariable != null) {
            timeVariableComboBox.setSelectedItem(timeVariable.getName());
        }
        timeVariableComboBox.addItemListener(this);
        
        DefaultTableModel covarianceTableModel = new CovarianceTableModel();
        int columnCount = coefficients.length + 1;
        int rowCount = coefficients.length + 1;
        covarianceTableModel.setColumnCount(columnCount);
        covarianceTableModel.setRowCount(rowCount);
        
        for (int i = 0; i < covariates.length; ++i) {
            covarianceTableModel.setValueAt(covariates[i], i + 1, 0);
            covarianceTableModel.setValueAt(covariates[i], 0, i + 1);
        }
        
        int index = 0;
        double[] uncertaintyMatrix = potential.getCovarianceMatrix();
        if (uncertaintyMatrix == null && potential.getCholeskyDecomposition() != null) {
            uncertaintyMatrix = potential.getCholeskyDecomposition();
            currentMatrixType = MATRIX_TYPE_CHOLESKY;
        }
        for (int rowIndex = 1; rowIndex < rowCount; ++rowIndex) {
            for (int columnIndex = 1; columnIndex <= rowIndex; ++columnIndex) {
                double value = (uncertaintyMatrix != null) ? uncertaintyMatrix[index] : 0.0;
                covarianceTableModel.setValueAt(value, rowIndex, columnIndex);
                ++index;
            }
        }
        uncertaintyTable.setModel(covarianceTableModel);
        uncertaintyCheckBox.setSelected(uncertaintyMatrix != null);
        uncertaintyPanel.setVisible(uncertaintyCheckBox.isSelected());
        matrixTypeComboBox.setSelectedItem(currentMatrixType);
    }
    
    private static boolean isValidTimeVariable(Variable variable) {
        return variable.isTemporal();
    }
    
    @Override public boolean saveChanges() throws DoEditException {
        WeibullHazardPotential oldPotential = (WeibullHazardPotential) this.node.getPotentials().get(0);
        VariableExpression[] covariates = regressionPanel.getCovariates();
        double[] coefficients = regressionPanel.getCoefficients();
        Variable timeVariable = null;
        if (timeVariableComboBox.getSelectedIndex() > 0) {
            String selectedTimeVariable = timeVariableComboBox.getSelectedItem().toString();
            timeVariable = node.getProbNet().getVariable(selectedTimeVariable);
        }
        
        
        double[] uncertaintyMatrix = null;
        if (uncertaintyCheckBox.isSelected()) {
            CovarianceTableModel tableModel = (CovarianceTableModel) uncertaintyTable.getModel();
            int rowCount = tableModel.getRowCount();
            int n = rowCount - 1;
            int index = 0;
            uncertaintyMatrix = new double[(n + 1) * n / 2];
            for (int rowIndex = 1; rowIndex < rowCount; ++rowIndex) {
                for (int columnIndex = 1; columnIndex <= rowIndex; ++columnIndex) {
                    double value = (Double) tableModel.getValueAt(rowIndex, columnIndex);
                    uncertaintyMatrix[index] = value;
                    ++index;
                }
            }
        }
        String matrixTypeName = matrixTypeComboBox.getSelectedItem().toString();
        MatrixType matrixType = (matrixTypeName.equals(MATRIX_TYPE_COVARIANCE)) ?
                MatrixType.COVARIANCE :
                MatrixType.CHOLESKY;
        
        WeibullHazardPotential newPotential = new WeibullHazardPotential(oldPotential.getVariables(),
                                                                         oldPotential.getPotentialRole(), covariates, coefficients, uncertaintyMatrix, matrixType);
        newPotential.setTimeVariable(timeVariable);
        PNEdit edit = new PotentialChangeEdit(node, oldPotential, newPotential);
        ProbNet probNet = node.getProbNet();
        edit.executeEdit();
        return true;
    }
    
    @Override public void close() {
    
    }
    
    @Override public void itemStateChanged(ItemEvent e) {
        if (e.getSource().equals(timeVariableComboBox) && timeVariableComboBox.getSelectedItem() != null
                && !timeVariableComboBox.getSelectedItem().equals(selectedTimeVariable)) {
            String oldTimeVariable = selectedTimeVariable;
            if (timeVariableComboBox.getSelectedIndex() > 0) {
                selectedTimeVariable = timeVariableComboBox.getSelectedItem().toString();
            } else {
                selectedTimeVariable = null;
            }
            Map<String, Double> coefficients = new LinkedHashMap<>();
            DefaultTableModel dtm = (DefaultTableModel) regressionPanel.getValuesTable().getModel();
            
            for (int i = 0; i < dtm.getRowCount(); ++i) {
                String covariate = dtm.getValueAt(i, 0).toString();
                Double coefficient = Double.parseDouble(dtm.getValueAt(i, 1).toString());
                coefficients.put(covariate, coefficient);
            }
            
            // Clear table
            dtm.setRowCount(0);
            coefficients.remove(selectedTimeVariable);
            List<String> covariates = new ArrayList<>(coefficients.keySet());
            for (String covariate : covariates) {
                dtm.addRow(new Object[]{covariate, coefficients.get(covariate)});
            }
            
            if (oldTimeVariable != null) {
                dtm.addRow(new Object[]{oldTimeVariable, 0.0});
                covariates.add(oldTimeVariable);
            }
            
            DefaultTableModel uncertaintyTableModel = (DefaultTableModel) uncertaintyTable.getModel();
            
            uncertaintyTableModel.setRowCount(covariates.size() + 1);
            uncertaintyTableModel.setColumnCount(covariates.size() + 1);
            for (int i = 0; i < covariates.size(); ++i) {
                uncertaintyTableModel.setValueAt(covariates.get(i), i + 1, 0);
                uncertaintyTableModel.setValueAt(covariates.get(i), 0, i + 1);
            }
            
            // Fill the empty cells with zeros
            for (int i = 1; i < uncertaintyTableModel.getRowCount(); ++i) {
                for (int j = 1; j <= i; ++j) {
                    if (uncertaintyTableModel.getValueAt(i, j) == null) {
                        uncertaintyTableModel.setValueAt(0.0, i, j);
                    }
                }
            }
            
        }
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(regressionPanel)) {
            DefaultTableModel covariateTableModel = (DefaultTableModel) regressionPanel.getValuesTable().getModel();
            int numCovariates = covariateTableModel.getRowCount();
            
            // update uncertainty matrix
            DefaultTableModel uncertaintyTableModel = (DefaultTableModel) uncertaintyTable.getModel();
            uncertaintyTableModel.setRowCount(numCovariates + 1);
            uncertaintyTableModel.setColumnCount(numCovariates + 1);
            for (int i = 0; i < numCovariates; ++i) {
                Object covariate = covariateTableModel.getValueAt(i, 0);
                uncertaintyTableModel.setValueAt(covariate, i + 1, 0);
                uncertaintyTableModel.setValueAt(covariate, 0, i + 1);
            }
            
            // Fill the empty cells with zeros
            for (int i = 1; i < uncertaintyTableModel.getRowCount(); ++i) {
                for (int j = 1; j <= i; ++j) {
                    if (uncertaintyTableModel.getValueAt(i, j) == null) {
                        uncertaintyTableModel.setValueAt(0.0, i, j);
                    }
                }
            }
        }
    }
    
    private static class CovarianceTableModel extends DefaultTableModel {
        
        @Override public boolean isCellEditable(int row, int column) {
            return column > 0 && row >= column;
        }
        
        @Override public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 0) ? String.class : Double.class;
        }
    }
    
    private static class CovarianceTableCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                 boolean hasFocus, int row, int column) {
            Color backgroundColor = GUIColors.Network.BACKGROUND.getColor();
            if (row == 0 || column == 0) {
                backgroundColor = GUIColors.Network.DecisionNode.BACKGROUND.getColor();
            } else if (column > row) {
                backgroundColor = GUIColors.Tables.HEADER_BACKGROUND.getColor();
            }
            setBackground(backgroundColor);
            
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
}
