/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A network is built with the default criterion alone; a new network is given «Cost», in the currency of the
 * machine, «Effectiveness», in QALY, and the code of that currency (error 299 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANewNetworkIsGivenTheCurrencyOfTheMachineTest {

    private Locale localeOfTheMachine;

    @BeforeEach void rememberTheLocaleOfTheMachine() {
        localeOfTheMachine = NetworkCurrency.USERS_LOCALE;
    }

    @AfterEach void restoreTheLocaleOfTheMachine() {
        NetworkCurrency.USERS_LOCALE = localeOfTheMachine;
    }

    /** Name, unit and role in a cost-effectiveness analysis of each criterion. */
    private static List<String> criteria(ProbNet probNet) {
        return probNet.getDecisionCriteria().stream()
                      .map(criterion -> criterion.getCriterionName() + " " + criterion.getCriterionUnit() + " "
                              + criterion.getCECriterion())
                      .toList();
    }

    @Test void aNetworkIsBuiltWithTheDefaultCriterionAndNoCurrency() {
        NetworkCurrency.USERS_LOCALE = Locale.of("es", "ES");
        ProbNet probNet = new ProbNet();

        assertEquals(List.of("--- --- Cost"), criteria(probNet));
        assertFalse(probNet.getAdditionalProperties().containsKey(NetworkCurrency.PROPERTY_NAME));
    }

    @Test void inSpainANewNetworkMeasuresItsCostInEuros() {
        NetworkCurrency.USERS_LOCALE = Locale.of("es", "ES");
        ProbNet probNet = new ProbNet();
        NetworkCurrency.ofTheMachine().orElseThrow().giveToANewNetwork(probNet);

        assertEquals(List.of("Cost € Cost", "Effectiveness QALY Effectiveness"), criteria(probNet));
        assertEquals("EUR", probNet.getAdditionalProperties().get(NetworkCurrency.PROPERTY_NAME));
    }

    @Test void inTheUnitedStatesANewNetworkMeasuresItsCostInDollars() {
        NetworkCurrency.USERS_LOCALE = Locale.US;
        ProbNet probNet = new ProbNet();
        NetworkCurrency.ofTheMachine().orElseThrow().giveToANewNetwork(probNet);

        assertEquals(List.of("Cost $ Cost", "Effectiveness QALY Effectiveness"), criteria(probNet));
        assertEquals("USD", probNet.getAdditionalProperties().get(NetworkCurrency.PROPERTY_NAME));
    }

    @Test void aLocaleWithoutCountryHasNoCurrency() {
        NetworkCurrency.USERS_LOCALE = Locale.ROOT;
        assertEquals(Optional.empty(), NetworkCurrency.ofTheMachine());
        NetworkCurrency.USERS_LOCALE = Locale.of("en");
        assertEquals(Optional.empty(), NetworkCurrency.ofTheMachine());
    }
}
