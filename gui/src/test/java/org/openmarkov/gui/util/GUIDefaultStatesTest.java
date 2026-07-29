/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.StandardDomain;
import org.openmarkov.gui.configuration.UserPreferences;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link GUIDefaultStates}, the single list of domains the menus of the interface
 * are built from.
 *
 * @author Manuel Arias
 */
class GUIDefaultStatesTest {

    private static final List<String> CATALOGUE_DOMAIN_KEYS = List.of("defaultStates.absentPresent.Text",
                                                                      "defaultStates.noYes.Text",
                                                                      "defaultStates.negativePositive.Text",
                                                                      "defaultStates.absentMildModerateSevere.Text",
                                                                      "defaultStates.lowMediumhigh.Text");

    /**
     * The list on offer is the user's domains followed by the catalogue's, and it ends with all of
     * the catalogue's: a menu cannot leave one of them out.
     */
    @Test
    void theDomainsOnOfferAreTheUsersFollowedByTheCatalogues() {
        List<List<String>> onOffer = GUIDefaultStates.getAllDomains();
        List<List<String>> catalogue = Arrays.stream(StandardDomain.values())
                                             .map(StandardDomain::getStateNames).map(List::copyOf).toList();
        List<List<String>> usersOwn = UserPreferences.CUSTOM_DOMAINS.get().stream()
                                                     .map(List::<String>copyOf).toList();

        assertEquals(usersOwn.size() + catalogue.size(), onOffer.size());
        assertEquals(usersOwn, onOffer.subList(0, usersOwn.size()).stream().map(List::copyOf).toList());
        assertEquals(catalogue, onOffer.subList(usersOwn.size(), onOffer.size()).stream()
                                       .map(List::copyOf).toList());
    }

    @Test
    void theStringsShownForTheMenusAreThoseOfTheCatalogue() {
        assertEquals(List.of("absent - present", "no - yes", "negative - positive",
                             "absent - mild - moderate - severe", "low - medium - high",
                             "not happened - happened"),
                     List.of(GUIDefaultStates.getListStrings()));
    }

    @Test
    void aStateWithoutATextOfItsOwnIsShownByItsName() {
        assertEquals("not happened", GUIDefaultStates.getString("not happened"));
    }

    @Test
    void everyStateIsFoundBackFromTheStringShownForIt() {
        for (StandardDomain domain : StandardDomain.values()) {
            for (String stateName : domain.getStateNames()) {
                assertEquals(stateName,
                             GUIDefaultStates.getStringLanguageDependent(GUIDefaultStates.getString(stateName)));
            }
        }
    }

    @Test
    void everyDomainIsFoundBackFromTheStringsShownForIt() {
        for (StandardDomain domain : StandardDomain.values()) {
            String[] shown = GUIDefaultStates.getStrings(domain.getStates());
            assertEquals(domain.ordinal(), GUIDefaultStates.getIndexLanguageDependent(shown));
        }
    }

    /**
     * The texts of the interface used to carry a second copy of the catalogue, five domains
     * written as one string each. They are gone, and this pins that they do not come back: the
     * domains are declared in {@link StandardDomain} and nowhere else.
     */
    @Test
    void theTextsOfTheInterfaceNoLongerCarryACopyOfTheCatalogue() {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        for (String key : CATALOGUE_DOMAIN_KEYS) {
            assertEquals(StringDatabase.surrondAsUnknown(key), stringDatabase.getString(key),
                         () -> "the domain " + key + " is declared in the texts of the interface again");
        }
    }
}
