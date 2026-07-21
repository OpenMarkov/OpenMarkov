/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;

/**
 * Represents the duration of a single cycle in temporal models, expressed
 * as a numeric value and a time unit (e.g. 1 YEAR, 6 MONTH).
 * Also provides discount-rate conversion between yearly and per-cycle rates.
 */
public class CycleLength implements ClassLocalizable {
    
    /**
     * Cycles of each unit in a year. The order must be the same as in the units (Year, Month, Week, ...)
     */
    private static final double[] CYCLES_IN_A_YEAR = {1, 12, 52, 365, 8760, 525600, 31536000, 31536000.0E3};
    private static final double DEFAULT_CYCLE_LENGTH = 1;
    private static final Unit DEFAULT_UNIT = Unit.YEAR;
    /**
     * Selected unit
     */
    private Unit unit;
    /**
     * Scale of the unit
     */
    private double value;
    
    public CycleLength() {
        this.unit = DEFAULT_UNIT;
        this.value = DEFAULT_CYCLE_LENGTH;
    }
    
    public CycleLength(Unit unit) {
        this.unit = unit;
        this.value = DEFAULT_CYCLE_LENGTH;
    }
    
    public CycleLength(Unit unit, double value) {
        this.unit = unit;
        this.value = value;
    }
    
    public CycleLength(CycleLength cycleLength) {
        this.unit = cycleLength.unit;
        this.value = cycleLength.value;
    }
    
    /**
     * Get the adjusted discount in cycles
     *
     * @param cycleUnit         ProbNet cycle selected unit
     * @param cycleLength       ProbNet cycle length
     * @param unitToBeConverted Actual discount unit
     * @param discount          Value of the discount
     *
     * @return Discount per cycle length
     */
    public static double getTemporalAdjustedDiscount(Unit cycleUnit, double cycleLength, DiscountUnit unitToBeConverted,
                                                     double discount) {
        if (unitToBeConverted == DiscountUnit.YEAR) {
            double rate = CYCLES_IN_A_YEAR[cycleUnit.ordinal()] / cycleLength;
            return Math.pow(1 + discount, 1 / rate) - 1.0;
        } // if(unitToBeConverted.equals(DiscountUnit.CYCLE)){
        return discount;
    }
    
    public Unit getUnit() {
        return unit;
    }
    
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    @Override public CycleLength clone() {
        return new CycleLength(this);
    }
    
    /**
     * Possible units
     */
    public enum Unit implements Localizable {
        YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND, MILISECOND;
        
        
        @Override public @NotNull String path() {
            return "";
        }
        
        @Override public @NotNull String localize(LocalizationFormatter formatter) {
            return super.name();
        }
        
        @Override public String toString() {
            return this.localize();
        }
    }
    
    /**
     * Temporal units of discounts
     */
    public enum DiscountUnit {
        YEAR, CYCLE;
    }
    
    @Override public String toString() {
        return this.localize();
    }
}
