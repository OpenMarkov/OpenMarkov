/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

/**
 * @author Manuel Arias
 * @version 1.0
 */
public abstract class IncompatibleEvidenceException extends UserInputException {
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    //TODO: It is caught and ignored in almost every catch block, probably leading to unexpected
    // bugs.
    // Perhaps it could be turned into a RuntimeException.
    public static final class EvidenceIsIncompatibleWithOther extends IncompatibleEvidenceException {
        public EvidenceIsIncompatibleWithOther(Finding newFinding, Finding oldFinding) {
            this.newFinding = newFinding;
            this.oldFinding = oldFinding;
        }
        
        public final Finding newFinding;
        public final Finding oldFinding;
    }
    
    
    public static final class VariableMustBeEvent extends IncompatibleEvidenceException {
        public final Variable variable;
        
        public VariableMustBeEvent(Variable variable) {
            this.variable = variable;
        }
    }
    
    public static final class FindingVariableIsMissingAState extends IncompatibleEvidenceException {
        public FindingVariableIsMissingAState(Finding finding, Variable variable, String state) {
            this.finding = finding;
            this.variable = variable;
            this.state = state;
        }
        
        public final Finding finding;
        public final Variable variable;
        public final String state;
    }
    
    public static final class SamplesWeightIsZero extends IncompatibleEvidenceException {
        public SamplesWeightIsZero(double[][] samples) {
            this.samples = samples;
        }
        
        public final double[][] samples;
    }
}
