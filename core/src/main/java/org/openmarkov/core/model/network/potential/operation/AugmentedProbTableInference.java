package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Variable;

import java.util.Arrays;
import java.util.Map;

import static org.openmarkov.core.model.network.potential.AugmentedProbTable.COMPLEMENT_FUNCTION;

public class AugmentedProbTableInference {
    
    public enum Operation {
        RESOLVE_EXPRESSIONS,
        RESOLVE_COMPLEMENT;
        
        public void solveColumn(Object[] processingValues, Map<Variable, String> findingsMap) throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
            switch (this) {
                case RESOLVE_EXPRESSIONS -> {
                    for (int rowIndex = 0; rowIndex < processingValues.length; rowIndex++) {
                        if (processingValues[rowIndex] instanceof VariableExpression variableExpression
                                && !variableExpression.asStringExpression()
                                                      .equals(COMPLEMENT_FUNCTION.asStringExpression())) {
                            processingValues[rowIndex] = Float.valueOf(variableExpression.evaluateWith(findingsMap));
                        }
                    }
                }
                case RESOLVE_COMPLEMENT -> {
                    float accumulated = 0.0f;
                    int numOfComplements = 0;
                    for (int rowIndex = 0; rowIndex < processingValues.length; rowIndex++) {
                        if (processingValues[rowIndex] instanceof VariableExpression variableExpression
                                && variableExpression.asStringExpression()
                                                     .equals(COMPLEMENT_FUNCTION.asStringExpression())) {
                            numOfComplements++;
                        } else if(processingValues[rowIndex] instanceof Float value){
                            accumulated += value;
                        }
                    }
                    if(numOfComplements==0){
                        return;
                    }
                    float valueForEachComplemented = (1-accumulated) / numOfComplements;
                    for (int rowIndex = 0; rowIndex < processingValues.length; rowIndex++) {
                        if (processingValues[rowIndex] instanceof VariableExpression variableExpression
                                && variableExpression.asStringExpression()
                                                     .equals(COMPLEMENT_FUNCTION.asStringExpression())) {
                            processingValues[rowIndex] = valueForEachComplemented;
                        }
                    }
                }
            }
        }
    }
    
    public static float[] resolveColumn(Object[] originalValues, Map<Variable, String> findingsMap, Operation[] operations) throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        Object[] newValues = Arrays.stream(originalValues).toArray();
        for (Operation operation : operations) {
            operation.solveColumn(newValues, findingsMap);
        }
        float[] finalValues = new float[newValues.length];
        for (int i = 0; i < finalValues.length; i++) {
            finalValues[i] = (float) newValues[i];
        }
        return finalValues;
        
    }
    
}
