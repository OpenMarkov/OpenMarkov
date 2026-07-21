/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ImposedPolicyTest {

	private final String networkTestName = "ImposedPolicy.pgmx";
	
	private String absolutePath;
	
	private String rootPath;
	
	private PGMXWriter_0_2 writer;
	
	private PGMXReader_0_2 reader;
	
	@BeforeEach
    public void setUp() {
    	URL url = getClass().getClassLoader ().getResource (networkTestName);
		File file = new File(url.getPath());
		absolutePath = file.getAbsolutePath();
		rootPath = absolutePath.substring(0, absolutePath.length() - networkTestName.length());
		writer = new PGMXWriter_0_2();
		reader = new PGMXReader_0_2();
    }
    
	@Test
	public final void test() throws ProbNetParserException, IOException {
		String rootPath = 
				absolutePath.substring(0, absolutePath.length() - networkTestName.length());
		String pathAndName = rootPath + networkTestName;
        ProbNet probNet1 = reader.read(new File(pathAndName).toURI().toURL()).probNet();

		ProbNet imposedPolicyNet = Util.createTrivialID();
	}

}
