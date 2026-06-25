/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.model;

/**
 * Enum with the analysis types
 *
 * @author jperez-martin
 */
public enum AnalysisType {
    // Deterministic
    TORNADO_SPIDER("SensitivityAnalysis.Type.TornadoSpider"),
    PLOT("SensitivityAnalysis.Type.Plot"),
    MAP("SensitivityAnalysis.Type.Map"),
    ACCEPTABILITY("SensitivityAnalysis.Type.Acceptability"),
    ACCEPTABILITY_CURVE("SensitivityAnalysis.Type.AcceptabilityCurve"),
    CEPLANE("SensitivityAnalysis.Type.CEPlane"),
    EVPI("SensitivityAnalysis.Type.EVPI"),
    SPIDER_CE("SensitivityAnalysis.Type.SpiderCE");
    
    private final String display;
    
    AnalysisType(String display) {
        this.display = display;
    }
    
    @Override public String toString() {
        return display;
    }
    
}