/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NetworkCurrency;
import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sets the currency of a network: its code in the network property {@link NetworkCurrency#PROPERTY_NAME} and its
 * symbol as the unit of every criterion whose role is the cost.
 *
 * @author Manuel Arias
 */
public class NetworkCurrencyEdit extends PNEdit {

    private final String code;
    private final String symbol;
    private String oldCode;
    private final List<Criterion> costCriteria = new ArrayList<>();
    private final List<String> oldUnits = new ArrayList<>();

    public NetworkCurrencyEdit(ProbNet probNet, NetworkCurrency currency) {
        super(probNet);
        this.code = currency.code();
        this.symbol = currency.symbol();
    }

    @Override protected void doEdit() {
        oldCode = probNet.getAdditionalProperties().get(NetworkCurrency.PROPERTY_NAME);
        probNet.putAdditionalProperty(NetworkCurrency.PROPERTY_NAME, code);
        costCriteria.clear();
        oldUnits.clear();
        for (Criterion criterion : probNet.getDecisionCriteria()) {
            if (criterion.getCECriterion() == Criterion.CECriterion.Cost) {
                costCriteria.add(criterion);
                oldUnits.add(criterion.getCriterionUnit());
                criterion.setCriterionUnit(symbol);
            }
        }
    }

    @Override public void undo() {
        super.undo();
        if (oldCode != null) {
            probNet.putAdditionalProperty(NetworkCurrency.PROPERTY_NAME, oldCode);
        } else {
            Map<String, String> properties = new LinkedHashMap<>(probNet.getAdditionalProperties());
            properties.remove(NetworkCurrency.PROPERTY_NAME);
            probNet.setAdditionalProperties(properties);
        }
        for (int i = 0; i < costCriteria.size(); i++) {
            costCriteria.get(i).setCriterionUnit(oldUnits.get(i));
        }
    }
}
