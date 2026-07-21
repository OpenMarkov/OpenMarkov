package org.openmarkov.gui.dialog.common;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.configuration.GUIColors;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableExpressionTextField extends JTextField {
    
    private String expression;
    private final Evaluator evaluator;
    private final List<Variable> variables;
    private final List<String> functionNames = Arrays
            .asList("abs", "acos", "asin", "atan", "atan2", "ceil", "cos", "exp", "log", "max", "min", "pow", "round",
                    "sin", "sqrt", "tan", "toDegrees", "toRadians");
    
    private final VariableExpressionAutoCompleter variableExpressionAutoCompleter;
    
    public VariableExpressionTextField() {
        evaluator = new Evaluator();
        variables = new ArrayList<>();
        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                validateExpression();
            }
            
            @Override public void removeUpdate(DocumentEvent e) {
                validateExpression();
            }
            
            @Override public void changedUpdate(DocumentEvent e) {
                validateExpression();
            }
        });
        variableExpressionAutoCompleter = new VariableExpressionAutoCompleter();
        variableExpressionAutoCompleter.getFunctions().addAll(functionNames);
        variableExpressionAutoCompleter.setupOn(this);
    }
    
    public void setMinWidthOnEditing(int minWidthOnEditing) {
        this.minWidthOnEditing = minWidthOnEditing;
    }
    
    @Override public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, Math.max(width, this.minWidthOnEditing), height);
    }
    
    public void setupWith(List<Variable> variables, String expression) {
        this.variables.clear();
        this.variables.addAll(variables);
        this.expression = expression;
        Map<String, String> variableValues = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            variableValues.put("v" + i, "1.0");
        }
        evaluator.setVariables(variableValues);
        if (this.expression != null) {
            this.setText(this.expression);
        }
        variableExpressionAutoCompleter.getVariables().clear();
        variableExpressionAutoCompleter.getVariables().addAll(variables.stream().map(Variable::getName).toList());
        validateExpression();
    }
    
    @NotNull public VariableExpression getExpression() {
        return new VariableExpression(variables, this.getText());
    }
    
    public boolean isValidExpression() {
        try {
            evaluator.evaluate(processExpression(this.getText()));
            getExpression();
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
    
    private void validateExpression() {
        boolean expressionIsValid = isValidExpression();
        this.setBackground(expressionIsValid ? GUIColors.Tables.EDITING_BACKGROUND.getColor() : GUIColors.General.ATTENTION_BG.getColor());
        this.setForeground(expressionIsValid ? GUIColors.Tables.EDITING_FOREGROUND.getColor() : GUIColors.General.ATTENTION_FG.getColor());
    }
    
    private int minWidthOnEditing;
}
