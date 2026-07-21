package org.openmarkov.gui.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.modelUncertainty.FamilyDistribution;

public sealed abstract class FamilyDistributionRuleBrokenException extends OpenMarkovException {
    
    public FamilyDistributionRuleBrokenException(FamilyDistribution family) {
        this.family = family;
    }
    
    public final FamilyDistribution family;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    public static final class Rule1Broken extends FamilyDistributionRuleBrokenException {
        
        public Rule1Broken(FamilyDistribution family) {
            super(family);
        }
    }
    
    public static final class Rule2Broken extends FamilyDistributionRuleBrokenException {
        public Rule2Broken(FamilyDistribution family) {
            super(family);
        }
    }
    
    public static final class Rule3Broken extends FamilyDistributionRuleBrokenException {
        public Rule3Broken(FamilyDistribution family) {
            super(family);
        }
    }
    
    public static final class Rule4Broken extends FamilyDistributionRuleBrokenException {
        public Rule4Broken(FamilyDistribution family) {
            super(family);
        }
    }
}
