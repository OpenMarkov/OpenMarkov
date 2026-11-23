/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The evidence cases that a case database gives for a network: one for each of its rows that has some finding. A
 * column that is not a variable of the network and a value that is not a state of its variable give no finding, and
 * are told of.
 *
 * @param cases   the evidence cases, in the order of their rows
 * @param leftOut one line for each column and for each value that gave no finding, and one for the rows left out
 *
 * @author Manuel Arias
 */
record EvidenceOfADatabase(List<EvidenceCase> cases, List<String> leftOut) {

    static EvidenceOfADatabase of(ProbNet net, CaseDatabase database)
            throws IncompatibleEvidenceException {
        List<Variable> columns = database.getVariables();
        List<String> columnsNotInTheNetwork = new ArrayList<>();
        Variable[] variablesOfTheNetwork = new Variable[columns.size()];
        for (int j = 0; j < columns.size(); j++) {
            variablesOfTheNetwork[j] = net.getVariable(columns.get(j).getName());
            if (variablesOfTheNetwork[j] == null) {
                columnsNotInTheNetwork.add(columns.get(j).getName());
            }
        }
        // How many times each value that is not a state appears, by its variable and the value
        Map<String, Integer> valuesThatAreNotStates = new LinkedHashMap<>();
        List<EvidenceCase> cases = new ArrayList<>();
        int rowsWithoutFindings = 0;
        for (int[] row : database.getCases()) {
            EvidenceCase evidenceCase = new EvidenceCase();
            for (int j = 0; j < row.length; j++) {
                String value = columns.get(j).getStateName(row[j]);
                Variable variable = variablesOfTheNetwork[j];
                if (variable == null || value.isEmpty() || value.equals("?")) {
                    continue;
                }
                int stateIndex = variable.getStateIndex(value);
                if (stateIndex == -1) {
                    valuesThatAreNotStates.merge(variable.getName() + ": «" + value + "» is not one of its states",
                                                 1, Integer::sum);
                } else {
                    evidenceCase.addFinding(new Finding(variable, stateIndex));
                }
            }
            if (evidenceCase.getFindings().isEmpty()) {
                rowsWithoutFindings++;
            } else {
                cases.add(evidenceCase);
            }
        }
        List<String> leftOut = new ArrayList<>();
        for (String column : columnsNotInTheNetwork) {
            leftOut.add("Column " + column + ": the network has no variable with that name.");
        }
        valuesThatAreNotStates.forEach((value, times) -> leftOut.add(value + " (" + times(times) + ")."));
        if (rowsWithoutFindings > 0) {
            leftOut.add(rowsWithoutFindings == 1 ? "1 row gave no finding and has not been added."
                                : rowsWithoutFindings + " rows gave no finding and have not been added.");
        }
        return new EvidenceOfADatabase(cases, leftOut);
    }

    private static String times(int times) {
        return times == 1 ? "once" : times + " times";
    }
}
