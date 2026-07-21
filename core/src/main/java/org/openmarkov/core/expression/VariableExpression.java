package org.openmarkov.core.expression;

import org.openmarkov.core.model.network.Variable;

import java.util.Collections;
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
    
    public static final class Common {
        
        public static final VariableExpression COMPLEMENT = new VariableExpression(Collections.emptyList(), "#");
        public static final VariableExpression CONSTANT = new VariableExpression(Collections.emptyList(), "Constant");
        public static final VariableExpression GAMMA = new VariableExpression(Collections.emptyList(), "Gamma");
        public static final VariableExpression ZERO = new VariableExpression(List.of(), "0");
        
    }
    
}
