package org.openmarkov.gui.productTour.tour.action;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.gui.productTour.tour.Tour;
import org.openmarkov.gui.productTour.tour.TourEffects;

public final class UserActionRequester extends ActionRequester {
    
    public UserActionRequester(Tour tour) {
        super(tour);
    }
    
    @Override protected void text(@NotNull TextRequest textRequest) {
    
    }
    
    @Override protected void click(@NotNull ClickRequest clickRequest) {
    
    }
    
    
    @Override protected void doMessage(TourEffects.ShowMessage message) {

    }
    
}
