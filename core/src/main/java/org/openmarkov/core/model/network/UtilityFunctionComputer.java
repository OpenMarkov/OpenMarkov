/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.modelUncertainty.Tools;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes utility functions for utility nodes, including recursive
 * computation for super-value nodes (nodes with multiple utility parents
 * combined via Sum or Product potentials).
 * <p>
 * Extracted from {@link Node} to separate complex computational logic
 * from the node's core identity and CRUD responsibilities.
 *
 * @author Manuel Arias
 */
public final class UtilityFunctionComputer {

    private UtilityFunctionComputer() {
    }

    /**
     * Computes the utility function of a utility node. If the node is a
     * super-value node, it recursively combines its parents' utility functions
     * using the appropriate operation (sum or product).
     *
     * @param node the utility node
     * @return the computed utility function as a {@link TablePotential},
     *         or {@code null} if the node has no potentials
     * @throws NonProjectablePotentialException if the potential is not a
     *         Sum or Product potential
     */
    public static @Nullable TablePotential computeUtilityFunction(Node node)
            throws NonProjectablePotentialException {
        List<Potential> potentials = node.getPotentials();
        if (potentials == null || potentials.isEmpty()) {
            return null;
        }
        Potential firstPotential = potentials.getFirst();
        switch (firstPotential) {
            case SumPotential ignored -> { }
            case ProductPotential ignored -> { }
            default -> throw new NonProjectablePotentialException.SuperValueMustBeSumOrProduct(firstPotential);
        }
        if (!node.isSuperValueNode()) {
            return firstPotential.tableProject(null, null);
        }
        List<TablePotential> utilityFunctionsParents = new ArrayList<>();
        for (Node parent : node.getParents()) {
            utilityFunctionsParents.add(computeUtilityFunction(parent));
        }
        return switch (firstPotential) {
            case SumPotential ignored -> DiscretePotentialOperations.sum(utilityFunctionsParents);
            case ProductPotential ignored -> DiscretePotentialOperations.multiply(utilityFunctionsParents);
            default -> throw new NonProjectablePotentialException.SuperValueMustBeSumOrProduct(firstPotential);
        };
    }

    /**
     * Approximates the maximum of the utility function of a node.
     * Computed recursively using parent nodes' utility functions.
     *
     * @param node the utility node
     * @return approximate maximum utility value
     * @throws NonProjectablePotentialException if the potential type is unsupported
     */
    public static double approximateMaxUtility(Node node) throws NonProjectablePotentialException {
        return approximateMaxOrMinUtility(node, true);
    }

    /**
     * Approximates the minimum of the utility function of a node.
     * Computed recursively using parent nodes' utility functions.
     *
     * @param node the utility node
     * @return approximate minimum utility value
     * @throws NonProjectablePotentialException if the potential type is unsupported
     */
    public static double approximateMinUtility(Node node) throws NonProjectablePotentialException {
        return approximateMaxOrMinUtility(node, false);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static double approximateMaxOrMinUtility(Node node, boolean computeMax)
            throws NonProjectablePotentialException {
        List<Potential> potentials = node.getPotentials();
        if (potentials == null || potentials.isEmpty()) {
            return 0.0;
        }
        Potential firstPotential = potentials.getFirst();
        if (!node.isSuperValueNode()) {
            TablePotential tableProject = firstPotential.tableProject(null, null);
            double[] values = tableProject != null ? tableProject.getValues() : new double[1];
            return computeMax ? Tools.max(values) : Tools.min(values);
        }
        List<Node> parents = node.getParents();
        double[] parentValues = new double[parents.size()];
        for (int i = 0; i < parents.size(); i++) {
            parentValues[i] = approximateMaxOrMinUtility(parents.get(i), computeMax);
        }
        return switch (firstPotential) {
            case SumPotential ignored -> Tools.sum(parentValues);
            case ProductPotential ignored -> Tools.multiply(parentValues);
            default -> throw new NonProjectablePotentialException.SuperValueMustBeSumOrProduct(firstPotential);
        };
    }
}
