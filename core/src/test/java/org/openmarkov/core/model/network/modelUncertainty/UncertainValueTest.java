/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Manuel Arias
 */
public class UncertainValueTest {
    
    @Test public void theNameIsWrittenWhenThereIsOne() {
        UncertainValue named = new UncertainValue(new ExactFunction(0.5), "prevalence");
        
        assertTrue(named.toString().startsWith("prevalence: "));
    }
    
    @Test public void nothingIsWrittenInFrontWhenThereIsNoName() {
        UncertainValue unnamed = new UncertainValue(new ExactFunction(0.5), "");
        
        assertTrue(unnamed.toString().startsWith(unnamed.getProbDensFunction().toString()));
    }
}
