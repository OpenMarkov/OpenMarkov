/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AgentsTest {

    private static String rootPath;
    
	private static final String probNetManualName = "test-decpomdp-manual.pgmx";

    /**
	 */
	@BeforeEach
	public void setUp() {
		URL url = getClass().getClassLoader ().getResource (probNetManualName);
		File file = new File(url.getPath());
		String absolutePath = file.getAbsolutePath();
		rootPath = absolutePath.substring(0, absolutePath.length() - probNetManualName.length());
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testAgentsNumber() throws ProbNetParserException, IOException {
		ProbNet manualProbNet = new PGMXReader_0_2().read(new File(rootPath + probNetManualName).toURI().toURL())
                                                    .probNet();
		List<StringWithProperties> agents = manualProbNet.getAgents();
		assertEquals(2, agents.size());
		StringWithProperties agent1 = agents.get(0);
		assertTrue(agent1.string.contentEquals("Agent 1"));
		StringWithProperties agent2 = agents.get(1);
		assertTrue(agent2.string.contentEquals("Agent 2"));
	}


}
