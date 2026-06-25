/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.*;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.ProbNet;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraIDsTest {

	// Attributes
	private static final String testFile = "IDU1-mediastinet.elv";

	private ProbNet probNet;

	@BeforeEach
	/** Create a ElviraScanner and opens a file for tests */ public void setUp() throws org.openmarkov.core.exception.ParserException, IOException {
		System.out.println(testFile);
		System.out.println("-------------------------------------");
		System.out.println();
        ProbNetReader probNetReader = new ElviraParser();
        probNet = probNetReader.read(this.getClass().getClassLoader().getResource(testFile)).probNet();
	}

	@Test public void test() {
		assertNotNull(probNet);
	}

}
