/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NetworkCurrency;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The currency of a network is its property Currency and the unit of every criterion whose role is the cost; changing
 * it is one step that is undone at once. A new network takes the currency chosen for new networks, or the machine's.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheCurrencyOfANetworkIsChangedInOneStepTest {

    private static final NetworkCurrency EUR = new NetworkCurrency("EUR", "€");
    private static final NetworkCurrency USD = new NetworkCurrency("USD", "$");

    private Locale localeOfTheMachine;
    private ProbNet net;

    @BeforeEach void anInfluenceDiagramInEurosOnASpanishMachine() {
        localeOfTheMachine = NetworkCurrency.USERS_LOCALE;
        NetworkCurrency.USERS_LOCALE = Locale.of("es", "ES");
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.putAdditionalProperty("Version", "1.0");
        EUR.giveToANewNetwork(net);
        net.getPNESupport().setWithUndo(true);
    }

    @AfterEach void restoreTheLocaleOfTheMachine() {
        NetworkCurrency.USERS_LOCALE = localeOfTheMachine;
    }

    private List<String> units() {
        return net.getDecisionCriteria().stream().map(Criterion::getCriterionUnit).toList();
    }

    @Test void choosingDollarsChangesTheUnitOfTheCostAndIsUndoneAtOnce() throws Exception {
        new NetworkCurrencyEdit(net, USD).executeEdit();

        assertEquals(List.of("$", "QALY"), units());
        assertEquals(Map.of("Version", "1.0", "Currency", "USD"), net.getAdditionalProperties());
        assertEquals(Optional.of(USD), NetworkCurrency.of(net));

        net.getPNESupport().undo();
        assertEquals(List.of("€", "QALY"), units());
        assertEquals(Optional.of(EUR), NetworkCurrency.of(net));

        net.getPNESupport().redo();
        net.getPNESupport().undo();
        assertEquals(List.of("€", "QALY"), units());
        assertEquals(Optional.of(EUR), NetworkCurrency.of(net));
    }

    @Test void aNetworkWithoutCurrencyLosesItWhenTheChangeIsUndone() throws Exception {
        net.setAdditionalProperties(Map.of("Version", "1.0"));
        net.setDecisionCriteria(new ArrayList<>(List.of(new Criterion())));

        new NetworkCurrencyEdit(net, USD).executeEdit();
        assertEquals(List.of("$"), units());
        assertEquals(List.of("Version", "Currency"), List.copyOf(net.getAdditionalProperties().keySet()));

        net.getPNESupport().undo();
        assertEquals(List.of("---"), units());
        assertEquals(Map.of("Version", "1.0"), net.getAdditionalProperties());
    }

    @Test void aCostMeasuredInAnotherUnitIsInNoCurrency() throws Exception {
        new DecisionCriterionUnitEdit(net, "Cost", "£").executeEdit();

        assertEquals(Optional.empty(), NetworkCurrency.of(net));
        assertEquals(Optional.of("£"), NetworkCurrency.costUnitOf(net));
    }

    @Test void aCodeThatIsNotOfferedTakesTheUnitOfTheCostAsItsSymbol() {
        net.putAdditionalProperty("Currency", "XYZ");

        assertEquals(Optional.of(new NetworkCurrency("XYZ", "€")), NetworkCurrency.of(net));
    }

    @Test void aNewNetworkTakesTheChosenCurrencyOrTheMachines() {
        assertEquals(USD, NetworkCurrency.forANewNetwork("USD"));
        assertEquals(EUR, NetworkCurrency.forANewNetwork(""));
        assertEquals(EUR, NetworkCurrency.forANewNetwork("XYZ"));

        NetworkCurrency.USERS_LOCALE = Locale.ROOT;
        assertEquals(NetworkCurrency.WHEN_THERE_IS_NONE, NetworkCurrency.forANewNetwork(""));
    }
}
