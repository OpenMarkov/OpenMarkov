/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.expression.ReferencedExpression;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

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
    public static sealed class EvidenceIsIncompatibleWithOther extends IncompatibleEvidenceException
            permits FindingIsNotTheFixedValue {
        public EvidenceIsIncompatibleWithOther(Finding newFinding, Finding oldFinding) {
            this.newFinding = newFinding;
            this.oldFinding = oldFinding;
        }
        
        public final Finding newFinding;
        public final Finding oldFinding;
    }
    
    /** A finding gives a node another value than the one its relation fixes, as a delta does. */
    public static final class FindingIsNotTheFixedValue extends EvidenceIsIncompatibleWithOther {
        public FindingIsNotTheFixedValue(Finding finding, Finding fixedValue) {
            super(finding, fixedValue);
            this.variable = finding.getVariable();
            this.observed = valueOf(finding);
            this.fixed = valueOf(fixedValue);
        }
        
        private static String valueOf(Finding finding) {
            return finding.getVariable().getVariableType() == VariableType.FINITE_STATES
                    ? finding.getState() : ReferencedExpression.toExpression(finding.getNumericalValue());
        }
        
        public final Variable variable;
        public final String observed;
        public final String fixed;
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
    
    /** The findings, taken together, have probability zero in the network. */
    public static final class EvidenceIsImpossible extends IncompatibleEvidenceException {
    }
    
    public static final class SamplesWeightIsZero extends IncompatibleEvidenceException {
        public SamplesWeightIsZero(double[][] samples) {
            this.samples = samples;
        }
        
        public final double[][] samples;
    }
}
