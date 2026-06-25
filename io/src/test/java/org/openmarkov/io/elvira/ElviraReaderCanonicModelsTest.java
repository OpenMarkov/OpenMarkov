/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.canonical.ICIFamily;
import org.openmarkov.core.model.network.potential.canonical.ICIModelType;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Manuel Arias
 * @vesion 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraReaderCanonicModelsTest {

	// Constants
    final double maxError = 1E-5;
	private ElviraParser elviraParser;
	// Attributes
	/*private final String[] cataratasVariables = {"agudeza_vis_sin_catar", 
			"camara_estrecha", "ojo_hundido", "miopia_magna", "pupila_estrecha",
			"pseudoexfoliacion", "tipo_catarata", "ojo_vitrectomizado",
			"mala_colaboracion", "retinopatia_diabetica", "retinopatia_nd",
			"maculopatias", "neuropatias", "ambliopia", "opacidades_corneales",
			"distrofia_corneal_fuchs", "av_complic", "incision_anormal",
			"endoftalmitis", "edema_corneal", "edema_macular_cistoide",
			"mecha_vitrea", "ruptura_caps_post", "agudeza_visual_pre",
			"av_post", "fvnd_pre_catar", "otros_trast_fv", "fvnd_pre",
			"otros_trast_fvnd_complic", "fvnd_post", "av_contral", 
			"fvnd_contral", "fvnd_global_pre", "fvnd_global_post", 
			"despr_retina", "Fibrosis_C_Ant", "sinequias_post", 
			"sublux_cristalino", "despr_coroideo", "deslu_complic",
			"deslu_pre_no_catar", "deslu_contral", "D", "deslu_pre",
			"deslu_post", "fv_global_post", "fv_global_pre", "catarata_contral",
			"deslu_global_pre", "deslu_global_post"}; */

	@BeforeEach public void setUp() {
		elviraParser = new ElviraParser();
	}
	
	@Tag(TestSpeed.SLOW)
	@Test public void testLoadElviraOr() throws ProbNetParserException, IOException {
		String testFile = "puerta-or.elv";
        ProbNet probNet = elviraParser.read(this.getClass().getClassLoader().getResource(testFile)).probNet();

		// Test probNet
		assertNotNull(probNet);
		List<Variable> variables = probNet.getVariables();
		assertEquals(3, variables.size());
		Variable A = probNet.getVariable("A");
		Variable B = probNet.getVariable("B");
		Variable C = probNet.getVariable("C");
		assertNotNull(A);
		assertNotNull(B);
		assertNotNull(C);

		// Test canonical potential
		Node nodeC = probNet.getNode(C);
		List<Potential> potentials = nodeC.getPotentials();
		assertEquals(1, potentials.size());
		ICIPotential potential = (ICIPotential) potentials.get(0);
		ICIFamily family = potential.getFamily();
		ICIModelType model = potential.getModelType();
		assertEquals(ICIFamily.OR, family);
		assertEquals(ICIModelType.OR, model);
		// Test subPotentials
		// Noisy parameters A
		assertEquals(4, potential.getNoisyParameters(A).length);
		assertEquals(1.0, potential.getNoisyParameters(A)[0], maxError);
		assertEquals(0.2, potential.getNoisyParameters(A)[2], maxError);
		// Noisy parameters B
		assertEquals(1.0, potential.getNoisyParameters(B)[0], maxError);
		assertEquals(0.3, potential.getNoisyParameters(B)[2], maxError);
		// Leaky parameters C
		assertEquals(2, potential.getLeakyParameters().length);
		assertEquals(0.999, potential.getLeakyParameters()[0], maxError);
		assertEquals(0.001, potential.getLeakyParameters()[1], maxError);
	}

	/**
	 * Reads a big probabilistic network with canonical models.
	 *
	 * @throws ProbNetParserException
	 * @throws Exception
	 */
	@Test public void testNaN() throws ProbNetParserException, IOException {
		String testFile = "cataratas-NaN.elv";
        ProbNet probNet = elviraParser.read(this.getClass().getClassLoader().getResource(testFile)).probNet();
		// Test a canonical potential
		assertNotNull(probNet); // Test NaN readed
		Node nodeFPC = probNet.getNode("fv-pre-catar");
		assertNotNull(nodeFPC);
		List<Potential> potentials = nodeFPC.getPotentials();
		assertNotNull(potentials);
		assertEquals(1, potentials.size());
	}

	/**
	 * Reads a big probabilistic network with canonical models.
	 *
	 * @throws Exception
	 */
	@Test public void testCataratas() throws ProbNetParserException, IOException {
		String testFile = "cataratas-escenarios-091123.elv";
        ProbNet probNet = elviraParser.read(this.getClass().getClassLoader().getResource(testFile)).probNet();
		// Test a normal node potential
		Node nodeDCF = probNet.getNode("av_sin_catar");
		assertEquals(1, nodeDCF.getPotentials().size());
	}

}
