/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

/**
 * Utility class to store the last open files
 *
 * @author jlgozalo
 * @version 1.0 25 Jul 2009
 */
public class LastOpenFiles {

	/**
	 * maximum number of last open files per OPENMARKOV session
	 */
	// TODO to be configured by an external configuration file
	public static final int MAX_LAST_OPEN_FILES = 9;

	/**
	 * retrieves the name of the file that is located in the position index
	 *
	 * @param index - the position of file in the list of last open files
	 * @return the fileName or empty
	 */
	public static String getFilePathAt(int index) {
		try {
            return LocalPreferences.LAST_OPEN_NETWORKS_FILES.get().get(index);
		} catch (IndexOutOfBoundsException ex) {
			return "";
		}
	}
	
	/**
	 * reorder the list of last open files considering that if the file was
	 * already open, only some of the files must be reorder
	 *
	 * @param fileName - name of the file to find
	 */
    public static void setLastFileName(String fileName) {
        LocalPreferences.LAST_OPEN_NETWORKS_FILES.use(latestOpenFiles -> {
			int index = getIndexForFilename(fileName);
			if (index == -1) {
				latestOpenFiles.add(0, fileName);
				while (latestOpenFiles.size() > MAX_LAST_OPEN_FILES) {
					latestOpenFiles.removeLast();
				}
			} else {
				latestOpenFiles.remove(index);
				latestOpenFiles.add(0, fileName);
			}
			
		});
	}

	/**
	 * retrieves the position of a specific file in the list of last open files
	 *
	 * @param fileName - name of the file to find the position
	 * @return index for the filename if exist; otherwise, return -1
	 */
    public static int getIndexForFilename(String fileName) {
        return LocalPreferences.LAST_OPEN_NETWORKS_FILES.get().indexOf(fileName);
	}

	/**
	 * @return true if there are some last open files; false otherwise
	 */
    public static boolean existLastOpenFiles() {
        return !LocalPreferences.LAST_OPEN_NETWORKS_FILES.get().isEmpty();
	}

	/**
	 * @return index the index for the oldest open file
	 */
    public static int getOldestOpenFileIndex() {
        return LocalPreferences.LAST_OPEN_NETWORKS_FILES.get().size() - 1;
	}
}
