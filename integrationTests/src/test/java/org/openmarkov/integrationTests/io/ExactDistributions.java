/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;

/**
 * Gives a node the "Exact" distribution that says the same as its table of values, for the tests that need a
 * distribution.
 *
 * @author Manuel Arias
 */
public final class ExactDistributions {

    private ExactDistributions() {
    }

    /** Replaces the table of values of {@code node} with the "Exact" distribution of the same numbers, and returns it. */
    public static UnivariateDistrPotential inPlaceOfTheTableOf(Node node) {
        ExactDistrPotential table = (ExactDistrPotential) node.getPotential();
        UnivariateDistrPotential distribution =
                new UnivariateDistrPotential(table.getVariables(), ExactFunction.class, table.getPotentialRole());
        distribution.setParameters(table.getValues().clone());
        node.setPotential(distribution);
        return distribution;
    }
}
