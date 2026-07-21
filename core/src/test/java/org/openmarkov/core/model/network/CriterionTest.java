/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Criterion}.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CriterionTest {

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    @Test
    void defaultConstructorUsesDefaultValues() {
        Criterion c = new Criterion();
        assertEquals(Criterion.getDefaultCriterion(), c.getCriterionName());
    }

    @Test
    void nameOnlyConstructorStoresName() {
        Criterion c = new Criterion("Quality of life");
        assertEquals("Quality of life", c.getCriterionName());
    }

    @Test
    void nameAndUnitConstructorStoresBoth() {
        Criterion c = new Criterion("Cost", "EUR");
        assertEquals("Cost", c.getCriterionName());
        assertEquals("EUR", c.getCriterionUnit());
    }

    @Test
    void copyConstructorCopiesAllFields() {
        Criterion original = new Criterion("Effectiveness", "QALY");
        original.setCeScale(2.5);
        original.setDiscount(0.03);
        original.setCECriterion(Criterion.CECriterion.Effectiveness);

        Criterion copy = new Criterion(original);

        assertEquals(original.getCriterionName(),  copy.getCriterionName());
        assertEquals(original.getCriterionUnit(),  copy.getCriterionUnit());
        assertEquals(original.getCeScale(),        copy.getCeScale(),  1e-9);
        assertEquals(original.getDiscount(),       copy.getDiscount(), 1e-9);
        assertEquals(original.getCECriterion(),    copy.getCECriterion());
    }

    // -----------------------------------------------------------------------
    // CECriterion auto-detection from name
    // -----------------------------------------------------------------------

    @Test
    void criterionNamedCostIsDetectedAsCost() {
        Criterion c = new Criterion("Cost");
        assertEquals(Criterion.CECriterion.Cost, c.getCECriterion());
    }

    @Test
    void criterionNamedEffectivenessIsDetectedAsEffectiveness() {
        Criterion c = new Criterion("Effectiveness");
        assertEquals(Criterion.CECriterion.Effectiveness, c.getCECriterion());
    }

    @Test
    void unknownNameDefaultsToCost() {
        Criterion c = new Criterion("SomeOtherCriterion");
        assertEquals(Criterion.CECriterion.Cost, c.getCECriterion());
    }

    // -----------------------------------------------------------------------
    // Defaults for numeric fields
    // -----------------------------------------------------------------------

    @Test
    void defaultDiscountIsZero() {
        assertEquals(0.0, new Criterion().getDiscount(), 1e-9);
    }

    @Test
    void defaultCeScaleIsOne() {
        assertEquals(1.0, new Criterion().getCeScale(), 1e-9);
    }

    @Test
    void defaultUnicriterizationScaleIsOne() {
        assertEquals(1.0, new Criterion().getUnicriterizationScale(), 1e-9);
    }

    @Test
    void defaultDiscountUnitIsYear() {
        assertEquals(CycleLength.DiscountUnit.YEAR, new Criterion().getDiscountUnit());
    }

    // -----------------------------------------------------------------------
    // Setters
    // -----------------------------------------------------------------------

    @Test
    void setCriterionNameUpdates() {
        Criterion c = new Criterion("Old");
        c.setCriterionName("New");
        assertEquals("New", c.getCriterionName());
    }

    @Test
    void setCriterionUnitUpdates() {
        Criterion c = new Criterion("Cost", "USD");
        c.setCriterionUnit("EUR");
        assertEquals("EUR", c.getCriterionUnit());
    }

    @Test
    void setDiscountUpdates() {
        Criterion c = new Criterion();
        c.setDiscount(0.05);
        assertEquals(0.05, c.getDiscount(), 1e-9);
    }

    @Test
    void setCeScaleUpdates() {
        Criterion c = new Criterion();
        c.setCeScale(3.0);
        assertEquals(3.0, c.getCeScale(), 1e-9);
    }

    @Test
    void setUnicriterizationScaleUpdates() {
        Criterion c = new Criterion();
        c.setUnicriterizationScale(0.5);
        assertEquals(0.5, c.getUnicriterizationScale(), 1e-9);
    }

    // -----------------------------------------------------------------------
    // clone
    // -----------------------------------------------------------------------

    @Test
    void cloneProducesEqualButDistinctObject() {
        Criterion original = new Criterion("Cost", "EUR");
        original.setDiscount(0.03);
        Criterion cloned = original.clone();

        assertNotSame(original, cloned);
        assertEquals(original.getCriterionName(), cloned.getCriterionName());
        assertEquals(original.getDiscount(), cloned.getDiscount(), 1e-9);
    }

    @Test
    void modifyingCloneDoesNotAffectOriginal() {
        Criterion original = new Criterion("Cost", "EUR");
        Criterion cloned = original.clone();
        cloned.setCriterionName("Changed");

        assertEquals("Cost", original.getCriterionName());
    }

    // -----------------------------------------------------------------------
    // copy
    // -----------------------------------------------------------------------

    @Test
    void copyOverwritesFieldsFromSource() {
        Criterion target = new Criterion("Old", "USD");
        Criterion source = new Criterion("New", "EUR");
        source.setDiscount(0.1);

        target.copy(source);

        assertEquals("New", target.getCriterionName());
        assertEquals("EUR", target.getCriterionUnit());
        assertEquals(0.1, target.getDiscount(), 1e-9);
    }
}
