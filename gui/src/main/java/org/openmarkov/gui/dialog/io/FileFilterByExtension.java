/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.openmarkov.core.localize.StringDatabase;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements the base code for all the file filters of the
 * application. By default, it accepts all the directories and initialises the
 * string resource.
 *
 * @author jmendoza
 * @version 1.0
 */
public class FileFilterByExtension<T> extends FileFilter {

	/**
	 * Extension of the files that match this filter.
	 */
	private final List<String> formatExtensions;

	/**
	 * Description of the files that match this filter.
	 */
	private String fileDescription;

	private final T formatInfo;
	
	public T getFormatInfo() {
		return this.formatInfo;
	}
	
	/**
	 * Create a new instance and create a new string resource.
	 */
	public FileFilterByExtension(T formatInfo, List<String> extension, String description) {
		this.formatInfo = formatInfo;
		formatExtensions = extension;
		fileDescription = description;
	}

	/**
	 * Accepts all the directories (by default in OpenMarkovtFileFilter) and files
	 * whose extension is 'pgmx'.
	 *
	 * @return true if the file is a directory; false otherwise
	 */
	@Override public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}
		String fileExtension = FileFilterByExtension.getExtension(file);
		return formatExtensions.stream().anyMatch(formatExtensions -> formatExtensions.equals(fileExtension));
	}

	/**
	 * Returns the description of the OpenMarkov files
	 *
	 * @return a string representing the description of the files type
	 */
	@Override public String getDescription() {
		return StringDatabase.getUniqueInstance().getString("FileExtension." + getFileDescription() + ".Description")
				+ " (" + formatExtensions.stream()
				                         .map(extension -> "." + extension)
				                         .collect(Collectors.joining(", ")) + ")";

	}
	//CMI

	/**
	 * @return the fileDescription used to match the filter with the proper Reader/Writer
	 */
	public String getFileDescription() {
		return fileDescription;
	}

	/**
	 * Sets the fileDescripion used to match the filter with the proper Reader/Writer
	 *
	 * @param fileDescription the fileDescription used to match the filter with the proper Reader/Writer
	 */
	public void setFileDescription(String fileDescription) {
		this.fileDescription = fileDescription;
	}
	
	/**
	 * Returns the extension of the given file.
	 *
	 * @param file file of which obtain the extension.
	 * @return extension of the file.
	 */
	protected static String getExtension(File file) {
		String name = file.getName();
		int i = name.lastIndexOf('.');
		if ((i > 0) && (i < (name.length() - 1))) {
			return name.substring(i + 1).toLowerCase();
		}
		return "";
	}
	
	public List<String> getExtensions() {
		return this.formatExtensions;
	}
}
