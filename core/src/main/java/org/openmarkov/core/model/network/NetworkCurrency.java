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

    /** The common currencies, each with the symbol it has in its own country. */
    public static final List<NetworkCurrency> COMMON = List.of(
            new NetworkCurrency("EUR", "€"), new NetworkCurrency("USD", "$"), new NetworkCurrency("GBP", "£"),
            new NetworkCurrency("CHF", "CHF"), new NetworkCurrency("SEK", "kr"), new NetworkCurrency("NOK", "kr"),
            new NetworkCurrency("DKK", "kr."), new NetworkCurrency("PLN", "zł"), new NetworkCurrency("JPY", "￥"),
            new NetworkCurrency("CNY", "¥"), new NetworkCurrency("KRW", "₩"), new NetworkCurrency("INR", "₹"),
            new NetworkCurrency("CAD", "$"), new NetworkCurrency("AUD", "$"), new NetworkCurrency("MXN", "$"),
            new NetworkCurrency("BRL", "R$"), new NetworkCurrency("ARS", "$"), new NetworkCurrency("CLP", "$"),
            new NetworkCurrency("COP", "$"), new NetworkCurrency("PEN", "S/"));

    /** The common currencies, followed by the one of the machine when it is not among them. */
    public static List<NetworkCurrency> offered() {
        List<NetworkCurrency> offered = new ArrayList<>(COMMON);
        ofTheMachine().filter(machine -> COMMON.stream().noneMatch(common -> common.code().equals(machine.code())))
                      .ifPresent(offered::add);
        return offered;
    }

    /** The offered currency with that code, if there is one. */
    public static Optional<NetworkCurrency> withCode(String code) {
        return offered().stream().filter(currency -> currency.code().equals(code)).findFirst();
    }

    /**
     * The currency of a new network: the one chosen, when it is the machine's or a common one; otherwise the
     * machine's; and EUR when the machine has none.
     *
     * @param chosenCode code of the chosen currency; empty means the machine's
     */
    public static NetworkCurrency forANewNetwork(String chosenCode) {
        Optional<NetworkCurrency> ofTheMachine = ofTheMachine();
        if (chosenCode != null && !chosenCode.isEmpty()) {
            if (ofTheMachine.isPresent() && ofTheMachine.get().code().equals(chosenCode)) {
                return ofTheMachine.get();
            }
            Optional<NetworkCurrency> common = COMMON.stream().filter(c -> c.code().equals(chosenCode)).findFirst();
            if (common.isPresent()) {
                return common.get();
            }
        }
        return ofTheMachine.orElse(WHEN_THERE_IS_NONE);
    }

    /**
     * The currency a network is in: the one its property names, while every criterion whose role is the cost is
     * measured in its symbol. For a code that is not offered, the unit of the cost is taken as its symbol.
     */
    public static Optional<NetworkCurrency> of(ProbNet probNet) {
        String code = probNet.getAdditionalProperties().get(PROPERTY_NAME);
        if (code == null || code.isEmpty()) {
            return Optional.empty();
        }
        List<String> costUnits = costUnits(probNet);
        NetworkCurrency currency = withCode(code).orElse(null);
        if (currency == null) {
            currency = new NetworkCurrency(code, costUnits.isEmpty() ? code : costUnits.getFirst());
        }
        for (String unit : costUnits) {
            if (!currency.symbol().equals(unit)) {
                return Optional.empty();
            }
        }
        return Optional.of(currency);
    }

    /** The unit of the first criterion whose role is the cost, if there is one and it has a unit. */
    public static Optional<String> costUnitOf(ProbNet probNet) {
        List<String> costUnits = costUnits(probNet);
        return costUnits.isEmpty() ? Optional.empty() : Optional.ofNullable(costUnits.getFirst());
    }

    private static List<String> costUnits(ProbNet probNet) {
        List<String> units = new ArrayList<>();
        if (probNet.getDecisionCriteria() != null) {
            for (Criterion criterion : probNet.getDecisionCriteria()) {
                if (criterion.getCECriterion() == Criterion.CECriterion.Cost) {
                    units.add(criterion.getCriterionUnit());
                }
            }
        }
        return units;
    }

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
