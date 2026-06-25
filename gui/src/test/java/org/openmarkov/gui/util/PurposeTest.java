/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.core.localize.StringDatabase;


/**
 * This class tests the class {@link Purpose}.
 *
 * @author jmendoza
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PurposeTest {
	/**
	 * This method initializes the language to English.
	 */
	@BeforeEach public void setUp() {
		StringDatabase.getUniqueInstance().setLanguage("en");
	}

	/**
	 * This method obtains the language-dependent string of a purpose that is
	 * known in English. Also it gets the language-dependent string of an
	 * unknown purpose.
	 */
	@Test public final void testGetString() {
        String string = Purpose.getString("treatment");
		assertEquals("Treatment", string);
		string = Purpose.getString("unknown");
		assertEquals(string, ">>> purpose.unknown.Text <<<");
	}
	
}
