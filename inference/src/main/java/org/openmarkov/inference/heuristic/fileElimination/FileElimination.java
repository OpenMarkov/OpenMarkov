/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.fileElimination;

import org.apache.logging.log4j.LogManager;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This heuristic reads a list of variable names from a file.
 *
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public class FileElimination extends EliminationHeuristic {

	// Attributes
	protected final List<List<Variable>> fileVariables;

	// Constructor

	/**
     * @param probNet                    {@code ProbNet}
     * @param setsOfVariablesToEliminate {@code ArrayList} of
     *                                   {@code Variable}
     * @param fileName                   = path + file name. {@code String}
	 */
	public FileElimination(ProbNet probNet, List<List<Variable>> setsOfVariablesToEliminate, String fileName) {
		super(probNet, setsOfVariablesToEliminate);
        fileVariables = readEliminationOrder(fileName, setsOfVariablesToEliminate);
	}

	/**
	 * Reads from a file a set of variables names
	 *
     * @param fileName            {@code String}
     * @param setsOfSetsVariables {@code ArrayList} of
     *                            {@code ? extends Variable}
     * @return An ordered {@code ArrayList} of variables taken from
     * {@code variables} corresponding to the given names
	 */
	public static List<List<Variable>> readEliminationOrder(String fileName, List<List<Variable>> setsOfSetsVariables) {

		String fullFileName = fileName.replace("elv", "hugin");

		ArrayList<String> names = new ArrayList<String>();
        // Reads the file
        try (BufferedReader in = new BufferedReader(new FileReader(fullFileName))) {
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                names.add(line);
            }
		} catch (IOException ioException) {
			LogManager.getLogger(FileElimination.class).fatal(ioException);
		}

		List<List<Variable>> orderedVariables = new ArrayList<>(1);
		List<Variable> allVariables = new ArrayList<Variable>();
		orderedVariables.add(allVariables);

		for (String name : names) {
			for (List<Variable> variables : setsOfSetsVariables) {
				for (Variable variable : variables) {
					if (name.contains(variable.getName())) {
						allVariables.add(variable);
						break;
					}
				}
			}
		}

		// Reverse variables because they will be taken from the last to the first in the array.
		int first = 0;
		int last = allVariables.size() - 1;
		Variable auxSwap;
		while (first < last) {
			auxSwap = allVariables.get(first);
			allVariables.set(first, allVariables.get(last));
			allVariables.set(last, auxSwap);
			first++;
			last--;
		}

		return orderedVariables;
	}

	// Methods
	@Override
    /** @return The {@code Variable} that the heuristic suggest to
	 *   eliminate */ public Variable getVariableToDelete() {
		Variable variable = null;
		List<Variable> variables = null;
		int i = fileVariables.size();
        while (--i >= 0 && (variables = fileVariables.get(i)).isEmpty()) {
        }
		if (i >= 0) {
			variable = variables.get(variables.size() - 1);
		}
		return variable;
	}
    
    @Override public void afterEditExecutes(PNEdit edit) {
		Variable variableToEliminate = getVariableToDelete();
		fileVariables.get(0).remove(variableToEliminate);
	}

}
