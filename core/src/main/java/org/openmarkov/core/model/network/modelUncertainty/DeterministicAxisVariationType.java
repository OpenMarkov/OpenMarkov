/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

/**
 * Enumerated class with deterministic axis variation types
 */
public enum DeterministicAxisVariationType {
    
    /** Percentage of the parameter probability */
    POPP,
    /** Same variation as the X axis */
    PREV,
    /** Percentage over reference value */
    PORV,
    /** Ratio over reference value */
    RORV,
    /** User defined interval between 0 and 1 */
    UDIN;
    
    @Override public String toString() {
        return "SensitivityAnalysis.Axis.Variation." + this.name();
    }
    
    public String toStringSpiderLegend() {
        return "SensitivityAnalysis.Axis.VariationSpiderLegend." + this.name();
    }
}

