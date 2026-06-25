/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This interface represents an abstraction of {@link ResourceBundle} limiting it to two functions, one for getting
 * all the possible keys in the bundle ({@link StringBundle#getKeys()}) and another to return the value of a key
 * ({@link StringBundle#getString(String)}).
 *
 * @author jrico
 */
public interface StringBundle {
	
	/**
	 * This method returns the requested string resource. If the key does not
	 * exist then a "virtual" string resource is returned to avoid the program
	 * to be stopped
	 *
	 * @param key the key of the desired string.
	 * @return the string associated with the key. if the resource doesn't
	 * exist, then a special string is returned.
	 */
	@Nullable String getString(String key);
	
	/**
	 * Returns all the keys of the bundle.
	 * @return all the keys of the bundle.
	 */
	@NotNull Set<String> getKeys();
	
}
