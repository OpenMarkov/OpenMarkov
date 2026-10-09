/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination;

import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The work that eliminating the variables of a network will take, worked out beforehand on a copy of the network
 * in which each table is only the set of its variables: no table is built and no number is computed.
 *
 * @author Manuel Arias
 */
public final class EliminationPlan {

    /** The variables of a table; {@code criterion} is null for a table of probabilities. */
    private record Table(Set<Variable> variables, Criterion criterion, boolean ofUtility) {
    }

    private EliminationPlan() {
    }

    /**
     * @param probabilities the variables of each table of probabilities that has the variable to eliminate
     * @param utilities     the variables of each table of utilities that has it
     * @return the work of that elimination: the cells of the tables that have to be built
     */
    public static double workOf(Collection<? extends Collection<Variable>> probabilities,
                                Collection<? extends Collection<Variable>> utilities) {
        Set<Variable> joint = new LinkedHashSet<>();
        probabilities.forEach(joint::addAll);
        double cellsOfTheJoint = cells(joint);
        double work = cellsOfTheJoint * Math.max(1, probabilities.size());
        for (Collection<Variable> utility : utilities) {
            double cellsWithTheUtility = cellsOfTheJoint;
            for (Variable variable : utility) {
                if (!joint.contains(variable)) {
                    cellsWithTheUtility *= variable.getNumStates();
                }
            }
            work += cellsWithTheUtility;
        }
        return work;
    }

    private static double cells(Collection<Variable> variables) {
        double cells = 1;
        for (Variable variable : variables) {
            cells *= variable.getNumStates();
        }
        return cells;
    }

    /**
     * @param markovDecisionNetwork the network that will be eliminated; it is not changed
     * @param heuristicOn           gives, for a copy of the network, the heuristic that orders its eliminations
     * @return the work of all the eliminations, added up
     */
    public static double totalWork(ProbNet markovDecisionNetwork, Function<ProbNet, EliminationHeuristic> heuristicOn) {
        ProbNet copy = markovDecisionNetwork.copy();
        List<Table> tables = tablesOf(copy);
        EliminationHeuristic heuristic = heuristicOn.apply(copy);
        copy.getPNESupport().addListener(heuristic);
        double total = 0;
        for (Variable variable = heuristic.getVariableToDelete(); variable != null;
             variable = heuristic.getVariableToDelete()) {
            total += eliminate(variable, copy, tables);
        }
        return total;
    }

    private static List<Table> tablesOf(ProbNet network) {
        List<Table> tables = new ArrayList<>();
        for (Node node : network.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                if (potential.getVariables().isEmpty()) {
                    continue;
                }
                Criterion criterion = potential.getCriterion();
                boolean ofUtility = criterion != null || (node.getNodeType() == NodeType.UTILITY
                        && node.getVariable().getDecisionCriterion() != null);
                boolean ofProbability = criterion == null && potential.getVariable(0).getDecisionCriterion() == null;
                if (ofUtility || ofProbability) {
                    tables.add(new Table(new LinkedHashSet<>(potential.getVariables()), criterion, ofUtility));
                }
            }
        }
        return tables;
    }

    /** Takes the variable out of the copy as the elimination would, and returns the work of doing it. */
    private static double eliminate(Variable variable, ProbNet copy, List<Table> tables) {
        List<Table> probabilities = new ArrayList<>();
        List<Table> utilities = new ArrayList<>();
        for (Table table : tables) {
            if (table.variables().contains(variable)) {
                (table.ofUtility() ? utilities : probabilities).add(table);
            }
        }
        tables.removeAll(probabilities);
        tables.removeAll(utilities);
        double work = workOf(probabilities.stream().map(Table::variables).toList(),
                             utilities.stream().map(Table::variables).toList());
        boolean isDecision = copy.getNode(variable).getNodeType() == NodeType.DECISION;
        try {
            new RemoveNodeEdit(copy, variable).executeEdit();
        } catch (DoEditException e) {
            throw new IllegalStateException(e);
        }
        Set<Variable> joint = new LinkedHashSet<>();
        probabilities.forEach(table -> joint.addAll(table.variables()));
        List<Table> results = new ArrayList<>();
        if (!probabilities.isEmpty()) {
            results.add(new Table(without(variable, joint), null, false));
        }
        // A chance variable leaves one table of utilities per criterion; a decision, one for all of them
        Map<Criterion, Set<Variable>> utilityOfEachCriterion = new LinkedHashMap<>();
        for (Table utility : utilities) {
            utilityOfEachCriterion.computeIfAbsent(isDecision ? utilities.getFirst().criterion() : utility.criterion(),
                                                   _ -> new LinkedHashSet<>(joint))
                                  .addAll(utility.variables());
        }
        utilityOfEachCriterion.forEach(
                (criterion, variables) -> results.add(new Table(without(variable, variables), criterion, true)));
        for (Table result : results) {
            if (!result.variables().isEmpty()) {
                tables.add(result);
                link(result.variables(), copy);
            }
        }
        return work;
    }

    private static Set<Variable> without(Variable variable, Set<Variable> variables) {
        Set<Variable> rest = new LinkedHashSet<>(variables);
        rest.remove(variable);
        return rest;
    }

    /** Links the variables of a table with each other, as adding the table to the network does. */
    private static void link(Set<Variable> variables, ProbNet copy) {
        List<Node> nodes = variables.stream().map(copy::getNode).toList();
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (!copy.isSibling(nodes.get(i), nodes.get(j))) {
                    copy.addLink(nodes.get(i), nodes.get(j), false);
                }
            }
        }
    }
}
