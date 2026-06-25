/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GLMPotential;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Table panel for editing covariates and their coefficients in a
 * {@link GLMPotential} regression model.
 */
@SuppressWarnings("serial") public class GLMPanel extends KeyTablePanel {
    
    private final List<ActionListener> listeners;
    private GLMPotential potential = null;
    
    public GLMPanel() {
        super(new String[]{"Covariate", "Coefficient"}, new Object[0][2], true, true, true);
        getValuesTable().onTables(omjTable -> omjTable.setDefaultRenderer(String.class, new CoefficientTableCellRenderer()));
        listeners = new ArrayList<>();
        valuesTable.addMouseListener(new CovariatesTableMouseListener());
        initialize();
    }
    
    /**
     * Invoked when the button 'add' is pressed.
     */
    @Override protected void actionPerformedAddValue(ActionEvent e) {
        ArithmeticExpressionDialog expressionDialog = new ArithmeticExpressionDialog(null, potential.getVariables(), null);
        expressionDialog.setVisible(true);
        if (expressionDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Ok) {
            int selectedRow = valuesTable.getSelectedRow();
            int rowCount = valuesTable.getRowCount();
            tableModel.addRow(new Object[]{new VariableExpression(potential.getVariables(), expressionDialog.getExpression()), 0.0});
            tableModel.moveRow(rowCount, rowCount, selectedRow + 1);
            valuesTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        }
        notifyActionListeners(new ActionEvent(this, 1, "Add"));
    }
    
    /**
     * Invoked when the button 'remove' is pressed.
     */
    @Override
    protected void actionPerformedRemoveValue(ActionEvent e) throws DoEditException {
        super.actionPerformedRemoveValue(e);
        notifyActionListeners(new ActionEvent(this, 2, "Remove"));
    }
    
    /**
     * Invoked when the button 'up' is pressed.
     */
    @Override protected void actionPerformedUpValue(ActionEvent e) throws DoEditException {
        super.actionPerformedUpValue(e);
        notifyActionListeners(new ActionEvent(this, 3, "Up"));
    }
    
    /**
     * Invoked when the button 'down' is pressed.
     */
    @Override protected void actionPerformedDownValue(ActionEvent e) throws DoEditException {
        super.actionPerformedDownValue(e);
        notifyActionListeners(new ActionEvent(this, 4, "Down"));
    }
    
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }
    
    public boolean removeActionListener(ActionListener listener) {
        return listeners.remove(listener);
    }
    
    private void notifyActionListeners(ActionEvent event) {
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }
    
    public void setData(GLMPotential potential) {
        this.potential = potential;
        double[] coefficients = potential.getCoefficients();
        VariableExpression[] covariates = potential.getCovariates();
        Object[][] data = new Object[covariates.length][2];
        for (int i = 0; i < covariates.length; ++i) {
            data[i][0] = covariates[i];
            data[i][1] = (i < coefficients.length) ? coefficients[i] : 0.0;
        }
        setData(data);
    }
    
    public double[] getCoefficients() {
        int rowCount = tableModel.getRowCount();
        double[] coefficients = new double[rowCount];
        for (int i = 0; i < rowCount; ++i) {
            coefficients[i] = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
        }
        return coefficients;
    }
    
    public VariableExpression[] getCovariates() {
        int rowCount = tableModel.getRowCount();
        VariableExpression[] covariates = new VariableExpression[rowCount];
        for (int i = 0; i < rowCount; ++i) {
            Object valueAt = tableModel.getValueAt(i, 0);
            covariates[i] = (VariableExpression) valueAt;
        }
        return covariates;
    }
    
    @Override public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        int row = valuesTable.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            VariableExpression covariate = (VariableExpression) tableModel.getValueAt(row, 0);
            boolean isMandatory = false;
            VariableExpression[] mandatoryCovariates = GLMPotential.getMandatoryCovariates();
            for (VariableExpression mandatoryCovariate : mandatoryCovariates) {
                isMandatory |= mandatoryCovariate.asStringExpression().equals(covariate.asStringExpression());
            }
            setEnabledRemoveValue(!isMandatory);
            setEnabledAddValue(!isMandatory);
        }
    }
    
    private static class CoefficientTableCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                 boolean hasFocus, int row, int column) {
            Color backgroundColor = GUIColors.Network.BACKGROUND.getColor();
            if (column == 0) {
                backgroundColor = GUIColors.Network.DecisionNode.BACKGROUND.getColor();
            }
            setBackground(backgroundColor);
            
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    private class CovariatesTableMouseListener extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && valuesTable.getSelectedColumn(e.getSource()) == 0) {
                int selectedRow = valuesTable.getSelectedRow();
                VariableExpression covariate = (VariableExpression) tableModel.getValueAt(selectedRow, 0);
                boolean isMandatory = false;
                VariableExpression[] mandatoryCovariates = GLMPotential.getMandatoryCovariates();
                for (VariableExpression mandatoryCovariate : mandatoryCovariates) {
                    isMandatory |= mandatoryCovariate.asStringExpression().equals(covariate.asStringExpression());
                }
                if (!isMandatory) {
                    List<Variable> variables = potential.getVariables();
                    variables.remove(potential.getConditionedVariable());
                    ArithmeticExpressionDialog expressionDialog = new ArithmeticExpressionDialog(null, variables, covariate.asStringExpression());
                    expressionDialog.setVisible(true);
                    if (expressionDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Ok) {
                        tableModel.setValueAt(new VariableExpression(variables, expressionDialog.getExpression()), selectedRow, 0);
                    }
                }
            }
        }
    }
}
