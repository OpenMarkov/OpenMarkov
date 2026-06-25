/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Dialog that lets the user compose and validate an arithmetic expression involving
 * network variables and standard math functions. The expression is validated live and
 * the background colour changes to indicate validity.
 */
@SuppressWarnings("serial") public class ArithmeticExpressionDialog extends OkCancelDialog
        implements DocumentListener {
    
    private final JTextField expressionTextField;
    private final JList<String> variableList;
    private final JList<String> functionList;
    private final JButton helpButton;
    private String expression;
    private final Evaluator evaluator;
    private final List<Variable> variables;
    private final List<String> functionNames = Arrays
            .asList("abs", "acos", "asin", "atan", "atan2", "ceil", "cos", "exp", "log", "max", "min", "pow", "round",
                    "sin", "sqrt", "tan", "toDegrees", "toRadians");
    
    public ArithmeticExpressionDialog(Window owner, List<Variable> variables, String expression) {
        super(owner);
        this.variables = variables;
        setTitle(stringDatabase.getString("ArithmeticExpressionEvaluator.Title"));
        setIconImage(null);
        this.expression = expression;
        evaluator = new Evaluator();
        Map<String, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            variableValues.put("v" + i, "1.0");
        }
        evaluator.setVariables(variableValues);
        JPanel expressionPanel = new JPanel();
        expressionPanel.setLayout(new BorderLayout());
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new BorderLayout());
        helpButton = new JButton(stringDatabase.getString("Help"));
        helpButton.setMaximumSize(new Dimension(40, 20));
        helpPanel.add(helpButton, BorderLayout.LINE_END);
        
        expressionPanel.add(helpPanel, BorderLayout.NORTH);
        expressionTextField = new JTextField();
        expressionTextField.setPreferredSize(new Dimension(400, 20));
        if (this.expression != null) {
            expressionTextField.setText(this.expression);
        }
        expressionPanel.add(expressionTextField, BorderLayout.CENTER);
        JPanel listPanel = new JPanel();
        variableList = new JList<>();
        JScrollPane variableListScroller = new JScrollPane(variableList);
        variableListScroller.setPreferredSize(new Dimension(175, 150));
        JLabel variableListLabel = new JLabel(
                stringDatabase.getString("ArithmeticExpressionEvaluator.Variables"));
        JPanel variableListPanel = new JPanel();
        variableListPanel.setLayout(new BorderLayout());
        variableListPanel.add(variableListLabel, BorderLayout.NORTH);
        variableListPanel.add(variableListScroller, BorderLayout.CENTER);
        
        functionList = new JList<>();
        JScrollPane functionListScroller = new JScrollPane(functionList);
        functionListScroller.setPreferredSize(new Dimension(175, 150));
        JLabel functionListLabel = new JLabel(
                stringDatabase.getString("ArithmeticExpressionEvaluator.Functions"));
        JPanel functionListPanel = new JPanel();
        functionListPanel.setLayout(new BorderLayout());
        functionListPanel.add(functionListLabel, BorderLayout.NORTH);
        functionListPanel.add(functionListScroller, BorderLayout.CENTER);
        
        listPanel.add(variableListPanel);
        listPanel.add(functionListPanel);
        expressionPanel.add(listPanel, BorderLayout.SOUTH);
        getComponentsPanel().add(expressionPanel, BorderLayout.NORTH);
        
        setLocationRelativeTo(null);
        expressionTextField.getDocument().addDocumentListener(this);
        validateExpression();
        DefaultListModel<String> variableListModel = new DefaultListModel<>();
        for (Variable variable : variables) {
            variableListModel.addElement(variable.getName());
        }
        variableList.setModel(variableListModel);
        variableList.setFocusable(false);
        variableList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() < 1) {
                    return;
                }
                try {
                    insertTextInExpression("{" + variableList.getSelectedValue() + "}");
                } catch (BadLocationException ex) {
                    throw new UnrecoverableException(ex);
                }
            }
        });
        variableList.setToolTipText(stringDatabase.getString("ArithmeticExpressionEvaluator.Instructions.Variables"));
        DefaultListModel<String> functionListModel = new DefaultListModel<>();
        Collections.sort(functionNames);
        for (String functionName : functionNames) {
            functionListModel.addElement(functionName);
        }
        
        functionList.setModel(functionListModel);
        functionList.setFocusable(false);
        functionList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() < 1) {
                    return;
                }
                try {
                    insertTextInExpression(functionList.getSelectedValue() + "()");
                } catch (BadLocationException ex) {
                    throw new UnrecoverableException(ex);
                }
            }
        });
        functionList.setToolTipText(stringDatabase.getString("ArithmeticExpressionEvaluator.Instructions.Functions"));
        
        helpButton.addActionListener(new ActionListener() {
            
            @Override public void actionPerformed(ActionEvent e) {
                JEditorPane ed1 = new JEditorPane("text/html",
                                                  stringDatabase.getString("ArithmeticExpressionEvaluator.Help"));
                ed1.setCaretPosition(0);
                ed1.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(ed1);
                JDialog helpDialog = new JDialog(null, stringDatabase.getString("Help"),
                                                 ModalityType.APPLICATION_MODAL);
                helpDialog.add(scrollPane);
                helpDialog.setSize(600, 600);
                helpDialog.setLocationRelativeTo(null);
                helpDialog.setVisible(true);
                helpDialog.setIconImage(null);
            }
        });
        pack();
    }
    
    @Override protected boolean doOkClickBeforeHide() {
        expression = expressionTextField.getText();
        return isValidExpression();
    }
    
    @Override protected void doCancelClickBeforeHide() {
        expression = null;
    }
    
    /**
     * Returns the expression entered by the user, or {@code null} if cancelled.
     *
     * @return the arithmetic expression string
     */
    public String getExpression() {
        return expression;
    }
    
    private boolean isValidExpression() {
        try {
            evaluator.evaluate(processExpression(expressionTextField.getText()));
            new VariableExpression(variables, expressionTextField.getText());
            return true;
        } catch (EvaluationException e) {
            return false;
        }
    }
    
    private String processExpression(String expression) {
        String processedExpression = expression;
        for (int i = 0; i < variables.size(); i++) {
            processedExpression = processedExpression.replace("{" + variables.get(i).getName() + "}", "#{v" + i + "}");
        }
        for (int i = 0; i < variables.size(); i++) {
            processedExpression = processedExpression.replace(variables.get(i).getName(), "#{v" + i + "}");
        }
        return processedExpression;
    }
    
    private void insertTextInExpression(String text) throws BadLocationException {
        expressionTextField.getDocument().insertString(expressionTextField.getCaretPosition(), text, null);
    }
    
    @Override public void insertUpdate(DocumentEvent e) {
        validateExpression();
    }
    
    @Override public void removeUpdate(DocumentEvent e) {
        validateExpression();
    }
    
    @Override public void changedUpdate(DocumentEvent e) {
        validateExpression();
    }
    
    private void validateExpression() {
        boolean expressionIsValid = isValidExpression();
        this.getOKButton().setEnabled(expressionIsValid);
        expressionTextField.setBackground(expressionIsValid ? GUIColors.General.CORRECT.getColor() : GUIColors.General.WRONG.getColor());
    }
    
}
