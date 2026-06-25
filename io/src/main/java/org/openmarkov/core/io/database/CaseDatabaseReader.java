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

import java.io.File;
import java.io.IOException;

@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public interface CaseDatabaseReader {
    @NotNull CaseDatabase load(File file) throws IOException, ParsingSourceException, EmptyDatabaseException;
}
