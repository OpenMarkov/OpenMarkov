/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.io.database.CaseDatabaseWriter;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.gui.configuration.LocalPreferences;

import java.io.File;
import java.util.List;

/**
 * File chooser dialog pre-configured with filters for all registered
 * {@link CaseDatabaseWriter} formats, used when saving a case database.
 */
@SuppressWarnings("serial") public class DBWriterOMFileChooser extends CommonDBOMFileChooser {
 
	public DBWriterOMFileChooser(boolean acceptAllFiles) {
		super(acceptAllFiles);
		CaseDatabaseManager.listWriters().forEach(writerClass->{
            var info = CaseDatabaseManager.info(writerClass);
			FileFilterByExtension<? extends Class<? extends CaseDatabaseWriter>> filter = new FileFilterByExtension<>(writerClass, List.of(info.extension()), info.name());
            addChoosableFileFilter(filter);
		});
		File currentDirectory = LocalPreferences.LATEST_SAVED_DATASET_DIRECTORY.get();
        setCurrentDirectory(currentDirectory);
	}
	
}
