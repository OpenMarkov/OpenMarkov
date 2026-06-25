package org.openmarkov.core.expression;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.*;

class VariableExpressionTest {
    
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