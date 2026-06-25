/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.xmlbif;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.network.ProbNet;
// TODO: Use the ProbNetTest class from the openmarkov.core tests
//import org.openmarkov.core.model.network.ProbNetTest;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class XMLBIFReaderTest {
	   private String rootPath;
	    private XMLBIFReader reader;
	    
		private final String networkTestName = "netAB.xml";

		@BeforeEach
	    public void setUp() {
	    	URL url = getClass().getClassLoader ().getResource (networkTestName);
			File file = new File(url.getPath());
			String absolutePath = file.getAbsolutePath();
			rootPath = absolutePath.substring(0, absolutePath.length() - networkTestName.length());
			reader = new XMLBIFReader();
	    }
		@Disabled
		@Test
		public void readNetworkTest() throws ParserException, IOException {
			ProbNet probNet1;
				String pathAndName = rootPath + networkTestName;
            probNet1 = reader.read(new File(pathAndName).toURI().toURL()).probNet();
				assertNotNull(probNet1);
				// TODO: Once the ProbNetTest class is available, remove the next line
				//ProbNetTest.compareNetworks(probNet1,BNFactory.createBN_XY("class","Symptom",0.34375,0.75,0.681818181818));

		}
}
