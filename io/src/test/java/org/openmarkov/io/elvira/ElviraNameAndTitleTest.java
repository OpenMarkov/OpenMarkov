/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.*;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraNameAndTitleTest {

	private static final String testFile = "BNU2-NasoNet.elv";

	private ProbNet probNet;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws org.openmarkov.core.exception.ParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource(testFile);
        ProbNetReader probNetReader = new ElviraParser();
        probNet = probNetReader.read(url).probNet();
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test public void testTitleAndName() {
		assertNotNull(probNet.getVariable("Primary infiltrating tumor on nasopharyngeal anterior wall"));
	}

}