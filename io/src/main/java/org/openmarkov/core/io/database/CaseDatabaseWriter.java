/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.database;

import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.model.database.CaseDatabase;

import java.io.File;
import java.io.IOException;


@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public interface CaseDatabaseWriter {
    void save(File file, CaseDatabase database) throws IOException;
    
}
