package org.openmarkov.gui.productTour.tour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TourManager {
    
    public static @Nullable Class<? extends Tour> CURRENT_TOUR_CLASS = null;
    
    public static @NotNull Map<? extends ToursProvider, List<Tour>> availableProductTours() {
        return PluginSearch.init()
                           .childrenOf(ToursProvider.class)
                           .stream()
                           .map(tourProviderClass -> {
                               try {
                                   return tourProviderClass.getDeclaredConstructor().newInstance();
                               } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                                        InvocationTargetException e) {
                                   throw new UnreachableException(e);
                               }
                           })
                           .collect(Collectors.toMap(toursProvider -> toursProvider, ToursProvider::tours));
    }
}
