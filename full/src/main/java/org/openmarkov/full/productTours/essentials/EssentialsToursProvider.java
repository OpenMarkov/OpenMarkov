package org.openmarkov.full.productTours.essentials;

import org.openmarkov.gui.productTour.tour.Tour;
import org.openmarkov.gui.productTour.tour.ToursProvider;

import java.util.List;

public class EssentialsToursProvider implements ToursProvider {
    @Override public String name() {
        return "Essentials";
    }
    
    @Override public List<Tour> tours() {
        return List.of(new FirstTour());
    }
}
