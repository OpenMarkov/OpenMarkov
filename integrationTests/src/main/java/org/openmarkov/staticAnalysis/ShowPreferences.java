package org.openmarkov.staticAnalysis;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;

import java.util.ArrayList;
import java.util.Comparator;

public class ShowPreferences {
    
    public static void main(String[] args) {
        var sortedByName = new ArrayList<>(UserPreferences.getAllPreferences());
        sortedByName.sort(Comparator.comparing(ShowPreferences::pathNameOfPreference));
        for (UserPreference<?> preference : sortedByName) {
            System.out.println(ShowPreferences.pathNameOfPreference(preference) + ": " + preference.get());
        }
    }
    
    private static @NotNull String pathNameOfPreference(UserPreference<?> userPreference) {
        return String.join("/", userPreference.getPreferencePath());
    }
    
}
