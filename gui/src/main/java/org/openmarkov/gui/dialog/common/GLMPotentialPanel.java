/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.GLMPotential;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for editing a Generalized Linear Model ({@link GLMPotential}), including
 * the regression covariates/coefficients and an optional uncertainty (covariance) matrix.
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = GLMPotential.class) public class GLMPotentialPanel
        extends PotentialPanel implements ActionListener {
    
    private static final String MATRIX_TYPE_COVARIANCE = "Covariance matrix";
    private static final String MATRIX_TYPE_CHOLESKY = "Cholesky decomposition";
    
    private Node node = null;
    private GLMPotential potential = null;
    private GLMPanel regressionPanel;
    private JTable uncertaintyTable;
    private JCheckBox uncertaintyCheckBox;
    private JComboBox<String> matrixTypeComboBox;
    private JPanel uncertaintyPanel;
    private String currentMatrixType;
    
    public GLMPotentialPanel(Node node) {
        super();
        initComponents();
        setData(node);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel deterministicPanel = new JPanel();
        deterministicPanel.setLayout(new BorderLayout());
        JPanel northPanel = new JPanel();
        northPanel.setBorder(new TitledBorder("Coefficients"));
        regressionPanel = new GLMPanel();
        regressionPanel.addActionListener(this);
        regressionPanel.setPreferredSize(new Dimension(600, 200));
        northPanel.add(regressionPanel);
        deterministicPanel.add(northPanel, BorderLayout.NORTH);
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
        this.potential = (GLMPotential) this.node.getPotentials().get(0);
        double[] coefficients = potential.getCoefficients();
        VariableExpression[] covariates = potential.getCovariates();
        regressionPanel.setData(potential);
        
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
    
    @Override
    public boolean saveChanges() throws DoEditException {
        GLMPotential newPotential = (GLMPotential) this.potential.copy();
        VariableExpression[] covariates = regressionPanel.getCovariates();
        double[] coefficients = regressionPanel.getCoefficients();
        
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
        newPotential.setCovariates(covariates);
        newPotential.setCoefficients(coefficients);
        if (matrixTypeName.equals(MATRIX_TYPE_COVARIANCE)) {
            newPotential.setCovarianceMatrix(uncertaintyMatrix);
        } else {
            newPotential.setCholeskyDecomposition(uncertaintyMatrix);
        }
        PotentialChangeEdit potentialChangeEdit = new PotentialChangeEdit(node, this.potential, newPotential);
        potentialChangeEdit.executeEdit();
        return true;
    }
    
    @Override public void close() {
    
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
                ;
            } else if (column > row) {
                backgroundColor = GUIColors.Tables.HEADER_BACKGROUND.getColor();
            }
            setBackground(backgroundColor);
            
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
