/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.*;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.TuningPotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PGMXReaderWriterTest {
    
    private String rootPath;
    private PGMXWriter_0_2 writer;
    private PGMXReader_0_2 reader;
    
    private final String networkTestName = "HPV-model-0.2.0.pgmx";
    
    @BeforeEach
    public void setUp() {
        URL url = getClass().getClassLoader().getResource(networkTestName);
        File file = new File(url.getPath());
        String absolutePath = file.getAbsolutePath();
        rootPath = absolutePath.substring(0, absolutePath.length() - networkTestName.length());
        writer = new PGMXWriter_0_2();
        reader = new PGMXReader_0_2();
    }
    
    //TODO Code in here cannot compile
	/*
	@Disabled("Temporarily disabled as it uses tests classes from openmarkov.core (ProbNetTest)")
	@Test
	public void readWriteNetwork() throws WriterException, ParserException {
		ProbNet trivialNet = Util.createTrivialID();
		
		// Write test network
		String testNetworkName =  rootPath +  "test.pgmx";
		writer.writeProbNet(testNetworkName, trivialNet);
		
		// Read the preceding network
        ProbNet trivialNetRead = reader.loadProbNet(testNetworkName);
		
		// Compare both networks
		ProbNetTest.compareNetworks(trivialNet, trivialNetRead);
	}
	*/
    
    //TODO Code in here cannot compile
    /*
    @Disabled("Ignored because a deprecated network")
    @Test
    public void readHPVNetwork() throws ParserException, WriterException {
        String pathAndName = rootPath + networkTestName;
        ProbNet probNet1 = reader.loadProbNet(pathAndName);
        assertNotNull(probNet1);
        String testNetwork = "/test-" + networkTestName;
        writer.writeProbNet(rootPath + testNetwork, probNet1);
        ProbNet probNet2 = reader.loadProbNet(rootPath + testNetwork);
        ProbNetTest.compareNetworks(probNet1, probNet2);
    }
     */
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void iciPotentialsReadingTest() throws ParserException, IOException {
        
        // Read test network
        String testNetworkName = rootPath + "/test-ici-reading.pgmx";
        
        ProbNet readProbNet = reader.read(new File(testNetworkName).toURI().toURL()).probNet();
        
        Assertions.assertNotNull(readProbNet);
        Assertions.assertEquals(9, readProbNet.getNumNodes());
        Assertions.assertEquals(1, readProbNet.getNode("I").getNumPotentials());
        Assertions.assertTrue(readProbNet.getNode("I").getPotentials().get(0) instanceof TuningPotential);
        Assertions.assertEquals(1, readProbNet.getNode("H").getNumPotentials());
        Assertions.assertTrue(readProbNet.getNode("H").getPotentials().get(0) instanceof MaxPotential);
    }
    
    //TODO Code in here cannot compile
	/*
	@Disabled("Temporarily disabled as it uses tests classes from openmarkov.core (ProbNetTest)")
	@Test
    public void iciPotentialsWritingTest() throws WriterException, ParserException{
        ProbNet probNet = Util.buildNetWithICI ();
        // Write test network
        String testNetworkName = rootPath +  "/test-ici.pgmx";
            writer.writeProbNet(testNetworkName, probNet);
        
        // Read the preceding network
        ProbNet readProbNet = reader.loadProbNet(testNetworkName);
        
        // Compare both networks
        ProbNetTest.compareNetworks(probNet, readProbNet);        
    }
    */
    
    //TODO Code in here cannot compile
    /*
    @Disabled("Temporarily disabled as it uses tests classes from openmarkov.core (ProbNetTest)")
    @Test
    public void decisionAnalysisNetTest() throws WriterException, ParserException {
        ProbNet probNet = createDecisionAnalysisNet();
        
        // Write test network
        String testNetworkName = rootPath + "/test-dan.pgmx";
        writer.writeProbNet(testNetworkName, probNet);
        
        // Read the preceding network
        ProbNet readProbNet = reader.loadProbNet(testNetworkName);
        
        // Compare both networks
        ProbNetTest.compareNetworks(probNet, readProbNet);
    }
    */
    
    private ProbNet createDecisionAnalysisNet() {
        ProbNet probNet = new ProbNet(
                DecisionAnalysisNetworkType.getUniqueInstance());
        probNet.makeLinksExplicit(true);
        
        State[] stateA = new State[]{new State("A1"), new State("A2"),
                new State("A3")};
        State[] stateB = new State[]{new State("B1"), new State("B2")};
        State[] stateC = new State[]{new State("C1"), new State("C2")};
        Variable varA = new Variable("A", stateA);
        Variable varB = new Variable("B", stateB);
        Variable varC = new Variable("C", stateC);
        List<Variable> variables = new ArrayList<Variable>();
        variables.add(varB);
        variables.add(varA);
        variables.add(varC);
        Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
        nodeA.setAlwaysObserved(true);
        probNet.addNode(varB, NodeType.CHANCE);
        probNet.addNode(varC, NodeType.CHANCE);
        Node nodeB = new Node(probNet, varB, NodeType.CHANCE);
        List<Link<Node>> links = probNet.getLinks();
        TablePotential potential = new TablePotential(variables,
                                                      PotentialRole.CONDITIONAL_PROBABILITY);
        nodeB.addPotential(potential);
        probNet.addPotential(potential);
        for (Link<Node> link : links) {
            if (link.getFrom().getVariable().equals(varA)) {
                link.initializesRestrictionsPotential();
                link.setCompatibilityValue(stateA[0], stateB[0], 0);
                link.addRevealingState(stateA[0]);
                
            }
        }
        
        return probNet;
    }
    
    @Test
    public void writeOrphanUtility() {
        ProbNet probNet = Util.createTrivialID();
        probNet.removeNode(probNet.getNode("A"));
        probNet.removeNode(probNet.getNode("B"));
        probNet.removeNode(probNet.getNode("D"));
        
    }
    
}
