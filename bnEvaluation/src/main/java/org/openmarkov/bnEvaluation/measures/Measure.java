/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.measures;


/**
 * This class represents a measure of goodness of fit
 *
 * @author evillar
 * @version 1.0
 */

public abstract class Measure {
    
    private MeasureType type;
    private int numCases;
    
    public Measure(MeasureType type) {
        this.type = type;
    }
    
    /**
     * Accumulates the values from another measure into this one.
     *
     * @param measure the measure whose values are to be added
     */
    abstract public void accumulate(Measure measure);
    
    public MeasureType getMeasureType() {
        return type;
    }
    
    public int getNumCases() {
        return numCases;
    }
    
    public void setNumCases(int numCases) {
        this.numCases = numCases;
    }
    
    
    

}
