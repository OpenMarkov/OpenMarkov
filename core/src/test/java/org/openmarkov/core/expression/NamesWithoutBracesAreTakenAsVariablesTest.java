/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.expression;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A formula may name a variable without braces; it gets them, so that it can be evaluated.
 *
 * @author Manuel Arias
 */
public class NamesWithoutBracesAreTakenAsVariablesTest {

    private final Variable age = new Variable("Age [0]");
    private final Variable ageAtEntry = new Variable("Age at state entry [0]");

    @Tag(TestSpeed.FAST)
    @Test public void aNameWithoutBracesIsEvaluated() throws Exception {
        VariableExpression expression = new VariableExpression(List.of(age), "pow(Age [0],2)");

        assertEquals("pow({Age [0]},2)", expression.asStringExpression());
        assertEquals(List.of(age), expression.references());
        assertEquals(9.0, Double.parseDouble(expression.evaluateWith(Map.of(age, "3"))), 0.0);
    }

    @Tag(TestSpeed.FAST)
    @Test public void aLongerNameIsNotTakenForAShorterOne() {
        VariableExpression expression = new VariableExpression(List.of(age, ageAtEntry),
                "Age at state entry [0]-Age [0]");

        assertEquals("{Age at state entry [0]}-{Age [0]}", expression.asStringExpression());
    }

    @Tag(TestSpeed.FAST)
    @Test public void bracesAlreadyWrittenAreKept() {
        VariableExpression expression = new VariableExpression(List.of(age), "{Age [0]}*12+Age [0]");

        assertEquals("{Age [0]}*12+{Age [0]}", expression.asStringExpression());
    }

    @Tag(TestSpeed.FAST)
    @Test public void partsOfWordsNumbersAndFunctionsAreNotVariables() {
        Variable x = new Variable("X");
        Variable log = new Variable("log");
        Variable one = new Variable("1");

        assertEquals("Xa+{X}", new VariableExpression(List.of(x), "Xa+X").asStringExpression());
        assertEquals("log({X})", new VariableExpression(List.of(x, log), "log(X)").asStringExpression());
        assertEquals("{X}+10+1", new VariableExpression(List.of(x, one), "X+10+1").asStringExpression());
    }
}
