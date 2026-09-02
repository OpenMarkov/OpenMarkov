package org.openmarkov.core.exception;


import org.openmarkov.core.model.network.potential.WeibullHazardPotential;

public class ThereIsNoTimeVariableInWeibullHazardPotential extends OpenMarkovException {
    
    public final WeibullHazardPotential weibullHazardPotential;
    
    public ThereIsNoTimeVariableInWeibullHazardPotential(WeibullHazardPotential weibullHazardPotential) {
        this.weibullHazardPotential = weibullHazardPotential;
    }
    
}
