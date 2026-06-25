/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CycleLength.DiscountUnit;
import org.openmarkov.core.model.network.CycleLength.Unit;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class TemporalUnitTest {
    
    @Test public void testYearToThreeMonths() {
        double adjustedDiscount = CycleLength.getTemporalAdjustedDiscount(Unit.MONTH, 3, DiscountUnit.YEAR, 0.1);
        double realAdjustedDiscount = 0.02411368908444512940414496002301;
        assertTrue(Math.abs((adjustedDiscount - realAdjustedDiscount)) < (adjustedDiscount / Math.pow(10, 9)));
    }
    
    @Test public void testYearToThirtyMinutes() {
        double adjustedDiscount = CycleLength.getTemporalAdjustedDiscount(Unit.MINUTE, 30, DiscountUnit.YEAR, 0.3);
        double realAdjustedDiscount = 1.4975241378235313741624150657728e-5;
        assertTrue(Math.abs((adjustedDiscount - realAdjustedDiscount)) < (adjustedDiscount / Math.pow(10, 9)));
    }
    
    @Test public void testYearToEightyNineMiliseconds() {
        double adjustedDiscount = CycleLength.getTemporalAdjustedDiscount(Unit.MILISECOND, 89, DiscountUnit.YEAR, 0.15);
        double realAdjustedDiscount = 3.944321687545109726884071225588e-10;
        assertTrue(Math.abs((adjustedDiscount - realAdjustedDiscount)) < (adjustedDiscount / Math.pow(10, 6)));
    }
    
}
