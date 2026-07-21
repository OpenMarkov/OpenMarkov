/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * Factory methods for constant {@link TablePotential}s (single-value potentials
 * with no variables).
 *
 * @author Manuel Arias
 */
public final class TablePotentialFactory {

    private TablePotentialFactory() {
    }

    public static TablePotential createOneValuePotential(PotentialRole role, double value) {
        TablePotential newPotential = new TablePotential(role);
        double[] values = new double[1];
        values[0] = value;
        newPotential.setValues(values);
        return newPotential;
    }

    public static TablePotential createUnityProbabilityPotential() {
        return createOneValuePotential(PotentialRole.CONDITIONAL_PROBABILITY, 1.0);
    }

    public static TablePotential createZeroProbabilityPotential() {
        return createOneValuePotential(PotentialRole.CONDITIONAL_PROBABILITY, 0.0);
    }

    public static TablePotential createZeroUtilityPotential(ProbNet dan) {
        TablePotential newPotential = createOneValuePotential(PotentialRole.UNSPECIFIED, 0.0);
        if (dan != null) {
            newPotential.setCriterion(dan.getDecisionCriteria().get(0));
        }
        return newPotential;
    }
}
