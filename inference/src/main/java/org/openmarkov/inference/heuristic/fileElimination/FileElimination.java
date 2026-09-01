/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.fileElimination;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This heuristic reads a list of variable names from a file.
 *
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
public class FileElimination extends EliminationHeuristic {

	/** The position Hugin writes before the name, as in {@code "   24 Row0"}. */
	private static final Pattern POSITION_AND_NAME = Pattern.compile("\\s*\\d+\\s+(.*?)\\s*");

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

		String orderFileName = replaceExtension(fileName, "hugin");

		List<String> lines;
		try {
			lines = Files.readAllLines(Path.of(orderFileName));
		} catch (IOException ioException) {
			// It used to be logged and swallowed, and the caller got an empty order: the network
			// was then eliminated in whatever order the heuristic fell back to, with nothing said.
			throw new UncheckedIOException("Cannot read the elimination order from " + orderFileName, ioException);
		}

		Map<String, Variable> variablesByName = new HashMap<>();
		for (List<Variable> variables : setsOfSetsVariables) {
			for (Variable variable : variables) {
				variablesByName.putIfAbsent(variable.getName(), variable);
			}
		}

		List<Variable> allVariables = new ArrayList<>();
		for (String line : lines) {
			if (line.isBlank()) {
				continue;
			}
			String name = variableNameIn(line);
			Variable variable = variablesByName.get(name);
			if (variable == null) {
				throw new InvalidArgumentException(line, "fileName",
						"line of " + orderFileName + " names no variable of the network: " + name);
			}
			allVariables.add(variable);
		}

		// Reverse variables because they will be taken from the last to the first in the array.
		Collections.reverse(allVariables);

		List<List<Variable>> orderedVariables = new ArrayList<>(1);
		orderedVariables.add(allVariables);
		return orderedVariables;
	}

	/**
	 * The name of the variable a line names. A line of the file is the position and then the name,
	 * as Hugin writes it — {@code "   24 Row0"} — so the position is dropped; a line that is only a
	 * name is taken whole.
	 * <p>
	 * The name used to be looked for with {@code line.contains(name)}, which took the position along
	 * for the ride but also matched any variable whose name is part of another's. In BN-hepar, where
	 * there is an {@code amn} and a {@code vh_amn}, the line naming {@code vh_amn} answered
	 * {@code amn}: the order applied was not the order written, {@code amn} was eliminated twice and
	 * {@code vh_amn} never, and nothing said so.
	 */
	private static String variableNameIn(String line) {
		Matcher positionAndName = POSITION_AND_NAME.matcher(line);
		return positionAndName.matches() ? positionAndName.group(1) : line.strip();
	}

	/**
	 * The same path with its extension replaced. It used to be {@code fileName.replace("elv",
	 * "hugin")}, which replaces every occurrence anywhere in the path: a directory called
	 * {@code elvira} became {@code huginira} and the file was then looked for where it is not.
	 */
	private static String replaceExtension(String fileName, String extension) {
		int lastDot = fileName.lastIndexOf('.');
		int lastSeparator = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		return lastDot > lastSeparator ? fileName.substring(0, lastDot + 1) + extension
				: fileName + "." + extension;
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
