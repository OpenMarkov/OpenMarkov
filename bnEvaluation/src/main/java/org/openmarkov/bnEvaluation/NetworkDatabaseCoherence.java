/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure analysis of how coherent a case database is with a network, extracted from the evaluation
 * dialog so it can be reused and unit-tested without any user interface (model-view-controller
 * separation). It reports which network variables are matched, missing or state-incompatible, and,
 * when at least two variables match, produces a translated {@link CaseDatabase} whose columns follow
 * the network variables and whose state indices are mapped <em>by name</em> to the network ordering.
 *
 * <p>The analysis never mutates its inputs and performs no side effect; the dialog turns the
 * {@link Result} into the appropriate messages and state.</p>
 *
 * @author Manuel Arias
 */
public final class NetworkDatabaseCoherence {

    /**
     * Outcome of {@link #analyze}.
     *
     * @param level                          overall coherence between the network and the database
     * @param netDatabase                    database restricted and translated to the matched network
     *                                       variables, or {@code null} when {@code level} is {@link Coherence#ZERO}
     * @param matchedVariableNames           names of the network variables present in the database with
     *                                       compatible states, in network order
     * @param variablesNotInDatabase         network variables that the database does not contain
     * @param variablesWithIncompatibleStates database variables whose states are not all present in the
     *                                       corresponding network variable
     */
    public record Result(Coherence level,
                         CaseDatabase netDatabase,
                         List<String> matchedVariableNames,
                         List<Variable> variablesNotInDatabase,
                         List<Variable> variablesWithIncompatibleStates) {
    }

    private NetworkDatabaseCoherence() {
    }

    /**
     * Analyses the coherence between {@code probNet} and {@code database}.
     *
     * @param probNet  the network to evaluate
     * @param database the case database to evaluate it against
     * @return the analysis result (see {@link Result})
     */
    public static Result analyze(ProbNet probNet, CaseDatabase database) {
        List<Variable> netVariables = probNet.getVariables();
        List<Variable> matchedVariables = new ArrayList<>();
        List<int[]> translatedColumns = new ArrayList<>();
        List<String> matchedVariableNames = new ArrayList<>();
        List<Variable> variablesNotInDatabase = new ArrayList<>();
        List<Variable> variablesWithIncompatibleStates = new ArrayList<>();

        for (Variable netVariable : netVariables) {
            Variable caseVariable = database.getVariable(netVariable.getName());
            if (caseVariable == null) {
                variablesNotInDatabase.add(netVariable);
                continue;
            }
            int[] translated = translateStates(netVariable, caseVariable, database.getCases(caseVariable));
            if (translated != null) {
                matchedVariables.add(netVariable);
                translatedColumns.add(translated);
                matchedVariableNames.add(netVariable.getName());
            } else {
                variablesWithIncompatibleStates.add(caseVariable);
            }
        }

        if (matchedVariables.size() > 1) {
            int numCases = database.getNumCases();
            int[][] cases = new int[numCases][matchedVariables.size()];
            for (int v = 0; v < matchedVariables.size(); v++) {
                int[] column = translatedColumns.get(v);
                for (int i = 0; i < numCases; i++) {
                    cases[i][v] = column[i];
                }
            }
            CaseDatabase netDatabase = new CaseDatabase(matchedVariables, cases);
            Coherence level = (matchedVariables.size() == netVariables.size()) ? Coherence.STRONG : Coherence.WEAK;
            return new Result(level, netDatabase, matchedVariableNames,
                              variablesNotInDatabase, variablesWithIncompatibleStates);
        }
        return new Result(Coherence.ZERO, null, matchedVariableNames,
                          variablesNotInDatabase, variablesWithIncompatibleStates);
    }

    /**
     * Returns the database variable's case column translated to the network variable's state
     * ordering (mapped by state name), or {@code null} if the database variable has any state the
     * network variable lacks. The input array is not modified.
     */
    private static int[] translateStates(Variable netVariable, Variable caseVariable, int[] cases) {
        State[] caseStates = caseVariable.getStates();
        int[] indexInNet = new int[caseStates.length];
        for (int i = 0; i < caseStates.length; i++) {
            State state = netVariable.getState(caseStates[i].getName());
            if (state == null) {
                return null;
            }
            indexInNet[i] = netVariable.getStateIndex(state);
        }
        int[] translated = new int[cases.length];
        for (int i = 0; i < cases.length; i++) {
            translated[i] = indexInNet[cases[i]];
        }
        return translated;
    }
}
