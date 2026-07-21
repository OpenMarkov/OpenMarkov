/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.localize;

import org.openmarkov.core.localize.StringDatabase;

/**
 * Wrapper class for GUI localization
 *
 * @author Iñigo
 */
public class MenuLocalizer {


	/**
	 * Suffix that has mnemonic string resources.
	 */
	private final static String MNEMONIC_SUFFIX = ".Mnemonic";
    
    /**
     * Returns the localized string for the given resource identifier.
     *
     * @param stringId the resource identifier
     * @return the localized string
     */
    public static String getString(String stringId) {
        return StringDatabase.getUniqueInstance().getString(stringId);
	}

	/**
	 * Returns the localized label for a menu item.
	 *
	 * @param stringId the resource identifier for the menu item
     * @return the localized label foreground
	 */
	public static String getLabel(String stringId) {
        return StringDatabase.getUniqueInstance().getString(stringId);
	}

	/**
	 * Returns the mnemonic character string for a menu item.
	 *
	 * @param stringId the resource identifier for the menu item (without the mnemonic suffix)
	 * @return the mnemonic string (typically a single character)
	 */
	public static String getMnemonic(String stringId) {
        return StringDatabase.getUniqueInstance().getString(stringId + MNEMONIC_SUFFIX);
	}

}
