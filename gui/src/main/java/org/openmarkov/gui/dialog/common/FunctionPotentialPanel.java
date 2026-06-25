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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel for editing a {@link FunctionPotential}, displaying the arithmetic expression
 * and allowing the user to modify it via a dialog.
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = FunctionPotential.class)
public class FunctionPotentialPanel extends PotentialPanel {
    
    /**
     * Panel with the function
     */
    
    protected JTextArea functionTextArea = null;
    /**
     *
     */
    protected VariableExpression function;
    /**
     * Variables list
     */
    
    protected List<Variable> variables;
    /**
     * Parents list
     */
    protected List<Variable> parents;
    private JPanel functionPanel;
    private Node node = null;
    private FunctionPotential potential = null;
    
    public FunctionPotentialPanel(Node node) {
        super();
        initComponents();
        setData(node);
    }
    
    private void initComponents() {
        
        setLayout(new BorderLayout());
        JPanel northPanel = new JPanel();
        northPanel.setBorder(new TitledBorder("Function"));
        northPanel.setPreferredSize(new Dimension(800, 100));
        // String function= (potential.getCovariates()==null)?null:potential.getCovariates()[0];
        functionPanel = new JPanel();
        functionPanel.setLayout(new BorderLayout());
        functionPanel.setPreferredSize(new Dimension(750, 50));
        functionPanel.setBorder(new LineBorder(UIManager.getColor("Table.dropLineColor"), 1, false));
        functionPanel.add(getFunctionTextArea());
        functionTextArea.addMouseListener(new FunctionTextAreaMouseListener());
        
        northPanel.add(functionPanel, BorderLayout.NORTH);
        add(northPanel, BorderLayout.NORTH);
    }
    
    protected JTextArea getFunctionTextArea() {
        if (functionTextArea == null) {
            functionTextArea = new JTextArea();
            functionTextArea.setEditable(false);
        }
        return functionTextArea;
    }
    
    @Override public void setData(Node node) {
        this.node = node;
        var firstPotential = this.node.getPotentials().getFirst();

        if (firstPotential instanceof PiecewiseExponentialPotential piecewisePotential) {
            VariableExpression initFunction = piecewisePotential.getInitTimeFunction().getFunction();
            this.potential = new FunctionPotential(node.getPotential().getVariables(),node.getPotential().getPotentialRole(),initFunction);
        } else if (firstPotential instanceof FunctionPotential functionPotential) {
            this.potential = functionPotential;
        }

        this.variables = potential.getVariables();
        this.parents = variables.subList(1, variables.size());
        this.function = potential.getFunction();
        if (function == null) {
            function = FunctionPotential.DEFAULT_FUNCTION;
        }
        functionTextArea.setText(function.asStringExpression());
    }
    
    public VariableExpression getFunction() {
        return function;
    }
    
    @Override
    public boolean saveChanges() throws DoEditException {
        FunctionPotential newPotential = (FunctionPotential) this.potential.copy();
        newPotential.setFunction(function);
        PotentialChangeEdit potentialChangeEdit
                = new PotentialChangeEdit(node, this.potential, newPotential);
        potentialChangeEdit.executeEdit();
        return true;
    }
    
    @Override public void close() {
    
    }
    
    private class FunctionTextAreaMouseListener extends MouseAdapter {
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                List<Variable> availableVariables = potential.getVariables()
                                                             .subList(1, potential.getVariables().size());
                ArithmeticExpressionDialog expressionDialog = new ArithmeticExpressionDialog(null, availableVariables, function.asStringExpression());
                expressionDialog.setVisible(true);
                if (expressionDialog.getSelectedOption() == OkCancelDialog.ChosenOption.Ok) {
                    function = new VariableExpression(availableVariables, expressionDialog.getExpression());
                    functionTextArea.setText(function.asStringExpression());
                }
            }
        }
    }
    
    public void setFunction(String string) {
        this.function=new VariableExpression(this.variables, string);
    }
}
