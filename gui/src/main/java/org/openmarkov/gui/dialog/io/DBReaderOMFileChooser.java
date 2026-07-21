/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;

import java.util.List;

/**
 * File chooser dialog pre-configured with filters for all registered
 * {@link CaseDatabaseReader} formats, used when opening a case database.
 */
@SuppressWarnings("serial") public class DBReaderOMFileChooser extends CommonDBOMFileChooser {
    
    public DBReaderOMFileChooser(boolean acceptAllFiles) {
		super(acceptAllFiles);
        CaseDatabaseManager.listReaders().forEach(readerClass->{
			var info = CaseDatabaseManager.info(readerClass);
            FileFilterByExtension<? extends Class<? extends CaseDatabaseReader>> filter = new FileFilterByExtension<>(readerClass, List.of(info.extension()), info.name());
            addChoosableFileFilter(filter);
		});
	}
 
}
