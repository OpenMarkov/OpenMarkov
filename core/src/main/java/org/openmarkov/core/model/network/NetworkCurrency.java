/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The currency of a network: its ISO 4217 code, kept in the network property {@link #PROPERTY_NAME}, and the
 * symbol its cost is measured in.
 *
 * @param code   the ISO 4217 code, such as EUR
 * @param symbol the symbol written as the unit of the cost, such as €
 *
 * @author Manuel Arias
 */
public record NetworkCurrency(String code, String symbol) {

    /** Name of the network property that keeps the code of its currency. */
    public static final String PROPERTY_NAME = "Currency";

    /** Currency of a new network when the machine has none. */
    public static final NetworkCurrency WHEN_THERE_IS_NONE = new NetworkCurrency("EUR", "€");

    /** Locale of the machine, taken before the program sets its own format locale. */
    public static Locale USERS_LOCALE = Locale.getDefault(Locale.Category.FORMAT);

    /** The currency of the locale of the machine, if it has one. */
    public static Optional<NetworkCurrency> ofTheMachine() {
        Currency currency;
        try {
            currency = Currency.getInstance(USERS_LOCALE);
        } catch (IllegalArgumentException localeWithoutCountry) {
            return Optional.empty();
        }
        if (currency == null) {
            return Optional.empty();
        }
        return Optional.of(new NetworkCurrency(currency.getCurrencyCode(), currency.getSymbol(USERS_LOCALE)));
    }

    /**
     * Gives a new network the criteria «Cost», measured in this currency, and «Effectiveness», in QALY, and the
     * property with the code of this currency.
     */
    public void giveToANewNetwork(ProbNet probNet) {
        List<Criterion> criteria = new ArrayList<>();
        criteria.add(new Criterion("Cost", symbol));
        criteria.add(new Criterion("Effectiveness", "QALY"));
        probNet.setDecisionCriteria(criteria);
        probNet.putAdditionalProperty(PROPERTY_NAME, code);
    }
}
