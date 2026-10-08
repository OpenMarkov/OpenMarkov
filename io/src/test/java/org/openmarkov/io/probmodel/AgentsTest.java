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
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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


	private static void assertEachDecisionHasItsAgent(ProbNet net) {
		for (String decision : List.of("D1 [0]", "D1 [1]")) {
			assertEquals("Agent 1", net.getVariable(decision).getAgent().getString(), decision);
		}
		for (String decision : List.of("D2 [0]", "D2 [1]")) {
			assertEquals("Agent 2", net.getVariable(decision).getAgent().getString(), decision);
		}
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void theAgentOfEachDecisionIsRead() throws Exception {
		assertEachDecisionHasItsAgent(new PGMXReader().read(getClass().getResource("/" + probNetManualName)).probNet());
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void theAgentOfEachDecisionIsSaved() throws Exception {
		ProbNet net = new PGMXReader().read(getClass().getResource("/" + probNetManualName)).probNet();
		Path written = Files.createTempFile("agents", ".pgmx");
		try {
			new PGMXWriter_1_0().write(written.toString(), net, List.of());
			assertEachDecisionHasItsAgent(new PGMXReader().read(written.toUri().toURL()).probNet());
		} finally {
			Files.delete(written);
		}
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void aDecisionWithAnAgentThatTheNetworkDoesNotHaveIsRefused() throws Exception {
		String text;
		try (InputStream in = getClass().getResourceAsStream("/" + probNetManualName)) {
			text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		String agentOfADecision = "<Agent name=\"Agent 1\" />";
		assertTrue(text.contains(agentOfADecision));
		Path file = Files.createTempFile("unknown-agent", ".pgmx");
		try {
			Files.writeString(file, text.replace(agentOfADecision, "<Agent name=\"Agent 3\" />"));
			PGMXParserException.UnknownAgent refused = assertThrows(PGMXParserException.UnknownAgent.class,
					() -> new PGMXReader().read(file.toUri().toURL()));
			assertEquals("Agent 3", refused.agentName);
		} finally {
			Files.delete(file);
		}
	}

}
