package org.openmarkov.core.expression;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VariableExpressionTest {
    
    @Test
    void aPlainExpressionEvaluates() throws Exception {
        VariableExpression expression = new VariableExpression(List.of(), "2 + 3");

        assertEquals(5.0, Double.parseDouble(expression.evaluateWith(Map.of())));
    }

    /** jeval answers a division by zero with the string "Infinity" instead of failing. */
    @Test
    void aDivisionByZeroIsReportedInsteadOfHandingBackInfinity() {
        VariableExpression expression = new VariableExpression(List.of(), "1.0 / 0.0");

        assertThrows(NonProjectablePotentialException.CannotEvaluate.class,
                () -> expression.evaluateWith(Map.of()));
    }

    @Test void test() {
        ProbNet net = new ProbNet();
        Variable variableA = new Variable("VariableA");
        Variable variableB = new Variable("VariableB");
        Variable variableC = new Variable("VariableC");
        net.addNode(variableA, NodeType.CHANCE);
        net.addNode(variableB, NodeType.CHANCE);
        net.addNode(variableC, NodeType.CHANCE);
        var expression = new VariableExpression(net.getVariables(), "1+{VariableA}+({VariableB}+{VariableC})+2");
        assertEquals("1+{VariableA}+({VariableB}+{VariableC})+2", expression.asStringExpression());
        variableA.setName("VarA");
        variableB.setName("VarB");
        variableC.setName("VarC");
        assertEquals("1+{VarA}+({VarB}+{VarC})+2", expression.asStringExpression());
    }
    
}