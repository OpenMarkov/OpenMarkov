/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A formula is scaled as a whole, with the factor written without scientific notation, and a
 * product keeps its scale in its copies.
 *
 * @author Manuel Arias
 */
public class ScalingAFormulaOrAProductTest {

    private final Variable u0 = new Variable("U0");
    private final Variable u1 = new Variable("U1");

    @Tag(TestSpeed.FAST)
    @Test public void theWholeFormulaIsScaled() {
        FunctionPotential function = new FunctionPotential(List.of(u0, u1), PotentialRole.UNSPECIFIED,
                new VariableExpression(List.of(u1), "{U1}+100"));
        function.scalePotential(0.000000001);

        assertEquals("0.000000001*({U1}+100)", function.getFunction().asStringExpression());
    }

    @Tag(TestSpeed.FAST)
    @Test public void aProductKeepsItsScaleInItsCopies() {
        ProductPotential product = new ProductPotential(List.of(u0, u1), PotentialRole.UNSPECIFIED);
        product.scalePotential(2);
        product.scalePotential(0.5);
        product.scalePotential(3);

        assertEquals(3, ((ProductPotential) product.copy()).getScale(), 1E-15);
    }
}
