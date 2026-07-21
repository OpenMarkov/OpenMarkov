package org.openmarkov.gui.productTour.tour;

import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;

import java.util.List;

@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public interface ToursProvider {
    
    String name();
    List<Tour> tours();
    
}
