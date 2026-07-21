/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.IOException;
import java.net.URL;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraReaderTest {

	private ElviraParser elviraParser;

	@BeforeEach
	/** Creates a small ProbNet */ public void setUp() {
		elviraParser = new ElviraParser();
	}
	
	@Test public void testAlarm() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("alarm.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(37, probNet.getNumNodes());
		Assertions.assertEquals(46, probNet.getLinks().size());
	}
	
	@Test public void testApples() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("apples.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(8, probNet.getNumNodes());
		Assertions.assertEquals(9, probNet.getLinks().size());
	}
	
	@Test public void testAzar() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("azar.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(7, probNet.getNumNodes());
		Assertions.assertEquals(9, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.SLOW)
	@Test public void testBarley() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Barley.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(48, probNet.getNumNodes());
		Assertions.assertEquals(84, probNet.getLinks().size());
	}
	
	@Test public void testBoblo() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("boblo.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(23, probNet.getNumNodes());
		Assertions.assertEquals(24, probNet.getLinks().size());
	}
	
	@Test public void testBoerlage92() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("boerlage92.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(23, probNet.getNumNodes());
		Assertions.assertEquals(36, probNet.getLinks().size());
	}
	
	@Test public void testCancer() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("cancer.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(5, probNet.getNumNodes());
		Assertions.assertEquals(5, probNet.getLinks().size());
	}
	
	@Test public void testCarStarts() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("car-starts.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(18, probNet.getNumNodes());
		Assertions.assertEquals(17, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.SLOW)
	@Test public void testDiabetes() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Diabetes.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(413, probNet.getNumNodes());
		Assertions.assertEquals(602, probNet.getLinks().size());
	}
	
	@Test public void testDogProblem() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("dog-problem.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(5, probNet.getNumNodes());
		Assertions.assertEquals(4, probNet.getLinks().size());
	}
	
	@Test public void testElimbel2() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("elimbel2.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(10, probNet.getNumNodes());
		Assertions.assertEquals(10, probNet.getLinks().size());
	}
	
	@Test public void testHailfinder() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("hailfinder.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(56, probNet.getNumNodes());
		Assertions.assertEquals(66, probNet.getLinks().size());
	}
	
	@Test public void testHeadache() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("headache.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(12, probNet.getNumNodes());
		Assertions.assertEquals(11, probNet.getLinks().size());
	}
	
	@Test public void testInfUrinarias() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("inf-unirarias.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(12, probNet.getNumNodes());
		Assertions.assertEquals(20, probNet.getLinks().size());
	}
	
	@Test public void testInsurance() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("insurance.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(27, probNet.getNumNodes());
		Assertions.assertEquals(52, probNet.getLinks().size());
	}
	
	@Test public void testJavier1() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("javier1.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(8, probNet.getNumNodes());
		Assertions.assertEquals(9, probNet.getLinks().size());
	}
	
	@Test public void testJensenDI() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("jensendi.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(8, probNet.getNumNodes());
		Assertions.assertEquals(10, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test public void testLink() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Link.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(724, probNet.getNumNodes());
		Assertions.assertEquals(1125, probNet.getLinks().size());
	}
	
	@Test public void testMediastinoBasico3() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("mediastino-basico-3.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(15, probNet.getNumNodes());
		Assertions.assertEquals(28, probNet.getLinks().size());
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test public void testMildew() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Mildew.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(35, probNet.getNumNodes());
		Assertions.assertEquals(46, probNet.getLinks().size());
	}
	
	@Test public void testOil() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("oil.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(6, probNet.getNumNodes());
		Assertions.assertEquals(6, probNet.getLinks().size());
	}
	
	@Test public void testPigs() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("Pigs.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(441, probNet.getNumNodes());
		Assertions.assertEquals(592, probNet.getLinks().size());
	}
	
	@Test public void testProstanetE() throws ProbNetParserException, IOException {
		URL url = this.getClass().getClassLoader().getResource("prostanetE.elv");
        ProbNet probNet = elviraParser.read(url).probNet();
		Assertions.assertNotNull(probNet);
		Assertions.assertEquals(47, probNet.getNumNodes());
		Assertions.assertEquals(81, probNet.getLinks().size());
	}
}
