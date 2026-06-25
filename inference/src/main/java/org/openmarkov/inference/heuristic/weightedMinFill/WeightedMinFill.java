/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.weightedMinFill;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Weighted Min-Fill elimination heuristic.
 * <p>
 * For each candidate variable X, computes the weighted fill-in cost:
 * <pre>
 *   cost(X) = sum of card(Y) * card(Z)
 *             for each pair (Y, Z) of neighbors of X
 *             that are not yet connected by an edge
 * </pre>
 * where card(V) = number of states of V.
 * The variable with minimum cost is eliminated first.
 * Ties are broken by fewer neighbors, then by variable name (lexicographic).
 *
 * @author OpenMarkov
 */
public class WeightedMinFill extends EliminationHeuristic {

    private static final Comparator<String> NAME_ORDER = Comparator.nullsLast(Comparator.naturalOrder());

    /** Working copy of the graph, updated as variables are eliminated. */
    private final ProbNet graphCopy;

    /** Maps each variable to its node in {@code graphCopy}. */
    private final HashMap<Variable, Node> variablesNodes;

    public WeightedMinFill(ProbNet probNet, List<List<Variable>> variablesToEliminate) {
        super(probNet, variablesToEliminate);
        graphCopy = probNet.copy();
        variablesNodes = new HashMap<>();
        for (Node node : graphCopy.getNodes()) {
            variablesNodes.put(node.getVariable(), node);
        }
    }

    /**
     * Returns the variable whose elimination has the minimum weighted fill-in cost.
     * Ties are broken by fewer current neighbors, then by variable name.
     */
    @Override
    public Variable getVariableToDelete() {
        int listIndex = variablesToEliminate.size() - 1;
        while (listIndex >= 0 && variablesToEliminate.get(listIndex).isEmpty()) {
            --listIndex;
        }
        if (listIndex < 0) {
            return null;
        }

        List<Variable> candidates = variablesToEliminate.get(listIndex);
        Variable best = null;
        long bestCost = Long.MAX_VALUE;
        int bestNeighbors = Integer.MAX_VALUE;

        for (Variable candidate : candidates) {
            Node node = variablesNodes.get(candidate);
            if (node == null) {
                continue;
            }
            long cost = weightedFillInCost(node);
            int numNeighbors = node.getNeighbors().size();
            if (cost < bestCost
                    || (cost == bestCost && numNeighbors < bestNeighbors)
                    || (cost == bestCost && numNeighbors == bestNeighbors
                        && NAME_ORDER.compare(candidate.getName(), best.getName()) < 0)) {
                best = candidate;
                bestCost = cost;
                bestNeighbors = numNeighbors;
            }
        }
        return best;
    }

    /**
     * Computes the weighted fill-in cost for a node:
     * sum of card(Y) * card(Z) for every pair (Y, Z) of neighbors
     * that are not already connected.
     */
    private static long weightedFillInCost(Node node) {
        List<Node> neighbors = node.getNeighbors();
        int n = neighbors.size();
        long cost = 0;
        for (int i = 0; i < n - 1; i++) {
            Node ni = neighbors.get(i);
            long cardI = ni.getVariable().getNumStates();
            for (int j = i + 1; j < n; j++) {
                Node nj = neighbors.get(j);
                if (!ni.isNeighbor(nj)) {
                    cost += cardI * nj.getVariable().getNumStates();
                }
            }
        }
        return cost;
    }

    /**
     * Triangulates the graph locally (adds fill-in edges among the neighbors of the
     * eliminated variable) and removes the variable from the working graph.
     */
    @Override
    public void afterEditExecutes(PNEdit edit) {
        super.afterEditExecutes(edit);
        Variable variable = getEventVariable(edit);
        if (variable == null) {
            return;
        }
        Node nodeToRemove = variablesNodes.get(variable);
        List<Node> neighbors = new ArrayList<>(nodeToRemove.getNeighbors());
        int n = neighbors.size();
        for (int i = 0; i < n - 1; i++) {
            Node ni = neighbors.get(i);
            for (int j = i + 1; j < n; j++) {
                Node nj = neighbors.get(j);
                if (!ni.isNeighbor(nj)) {
                    graphCopy.addLink(ni, nj, false);
                }
            }
        }
        graphCopy.removeNode(nodeToRemove);
        variablesNodes.remove(variable);
    }
}
