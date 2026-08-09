/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.database;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public interface CaseDatabaseReader {
    @NotNull CaseDatabase load(File file) throws IOException, ParsingSourceException, EmptyDatabaseException;

    /**
     * Returns the database read from the file, or refuses it when two of its columns carry the same
     * variable name: a case could not say which of the two it means.
     */
    static @NotNull CaseDatabase withDistinctVariableNames(File file, @NotNull CaseDatabase database)
            throws ParsingSourceException.RepeatedVariableNames {
        Set<String> namesSoFar = new HashSet<>();
        for (Variable variable : database.getVariables()) {
            if (!namesSoFar.add(variable.getName())) {
                throw new ParsingSourceException.RepeatedVariableNames(file.getName(), variable.getName());
            }
        }
        return database;
    }
}
