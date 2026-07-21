/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyOneAgent;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.Set;
import java.util.TreeSet;

/**
 * Static utility methods that classify a {@link ProbNet} according to its
 * constraints and contents (temporal, multiagent, chance-only, etc.).
 * <p>
 * These methods are pure queries that do not modify the network.
 * Follows the same pattern as {@link VariableTypeConverter} and
 * {@link VariableStateOperations}.
 *
 * @author Manuel Arias
 * @see ProbNet
 */
public final class ProbNetClassifier {

    private ProbNetClassifier() {
    }

    /**
     * Returns {@code true} when this network allows temporal variables
     * (i.e. it does not have the {@link OnlyAtemporalVariables} constraint).
     */
    public static boolean variablesCouldBeTemporal(ProbNet probNet) {
        return !probNet.hasConstraintOfClass(OnlyAtemporalVariables.class);
    }

    /**
     * Returns {@code true} when this network is multiagent
     * (i.e. it does not have the {@link OnlyOneAgent} constraint).
     */
    public static boolean isMultiagent(ProbNet probNet) {
        return !probNet.hasConstraintOfClass(OnlyOneAgent.class);
    }

    /**
     * Returns {@code true} when this network requires all variables to be
     * temporal (i.e. it has the {@link OnlyTemporalVariables} constraint).
     */
    public static boolean onlyTemporal(ProbNet probNet) {
        return probNet.hasConstraintOfClass(OnlyTemporalVariables.class);
    }

    /**
     * Returns {@code true} when this network requires all nodes to be chance
     * nodes (i.e. it has the {@link OnlyChanceNodes} constraint).
     */
    public static boolean onlyChanceNodes(ProbNet probNet) {
        return probNet.hasConstraintOfClass(OnlyChanceNodes.class);
    }

    /**
     * Returns {@code true} when at least one node in the network has a
     * temporal variable.
     */
    public static boolean thereAreTemporalNodes(GraphNetwork probNet) {
        return probNet.getNodes().stream()
                      .anyMatch(node -> node.getVariable().isTemporal());
    }

    /**
     * Counts the number of distinct decision criteria across all potentials
     * in the network (case-insensitive comparison).
     */
    public static int getNumCriteria(PotentialNetwork probNet) {
        Set<String> criterionNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Potential potential : probNet.getPotentials()) {
            Criterion criterion = potential.getCriterion();
            if (criterion != null && criterion.getCriterionName() != null) {
                criterionNames.add(criterion.getCriterionName());
            }
        }
        return criterionNames.size();
    }
}
