package org.openmarkov.core.expression;

import org.openmarkov.core.model.network.Variable;

import java.util.List;
import java.util.stream.Collectors;

public class VariableExpression extends ReferencedExpression<Variable> {
    
    public VariableExpression(List<Variable> possibleVariables, String expression) {
        super(
                possibleVariables.stream().collect(Collectors.toMap(Variable::getName, v -> v)),
                expression,
                Variable::getName,
                v -> null
        );
    }
    
}
