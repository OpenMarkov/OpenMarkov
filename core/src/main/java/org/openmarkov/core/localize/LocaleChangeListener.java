/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.localize;

import java.util.EventListener;

/**
 * Interface for LocaleChangeEvent Listeners
 *
 * @author jlgozalo
 * @version 1.0 25 Jun 2009
 */
public interface LocaleChangeListener extends EventListener {
	void processLocaleChange(LocaleChangeEvent event);
}