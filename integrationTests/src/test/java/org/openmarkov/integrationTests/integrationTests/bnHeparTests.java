/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class bnHeparTests {
    private final String networkName = "networks/bn/BN-hepar.pgmx";
    
    // Delta parameter for Assertions.Equals methods
    private final double deltaEquals = Math.pow(10, -4);
    
    private ProbNet probNet;
    private EvidenceCase preResolutionEvidence;
    
    @BeforeEach public void setUp() throws java.net.URISyntaxException, ProbNetParserException, FileNotFoundException {
        
        // Load the network: ID-decide-test
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        PGMXReader.NetworkAndEvidence probNetInfo = null;
        probNetInfo = pgmxReader.read(getClass().getClassLoader().getResource(networkName));
        this.probNet = probNetInfo.probNet();
        if (probNetInfo.evidence().size() != 0) {
            this.preResolutionEvidence = probNetInfo.evidence().get(0);
        }
    }
    
    @Test
    public void vePropagationWithoutEvidence() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
        VEPropagation vePropagation;
        EvidenceCase postResolutionEvidence = new EvidenceCase();
        List<Variable> variablesOfInterest = new ArrayList<>();
        variablesOfInterest.add(probNet.getVariable("alt"));
        variablesOfInterest.add(probNet.getVariable("ascites"));
        variablesOfInterest.add(probNet.getVariable("carcinoma"));
        vePropagation = new VEPropagation(probNet);
        vePropagation.setVariablesOfInterest(variablesOfInterest);
        vePropagation.setPreResolutionEvidence(preResolutionEvidence);
        vePropagation.setPostResolutionEvidence(postResolutionEvidence);
        HashMap<Variable, TablePotential> posteriorVales = vePropagation.getPosteriorValues();
        
        for (Variable variable : variablesOfInterest) {
            double[] expectedValues = new double[0];
            switch (variable.getName()) {
                case "alt":
                    expectedValues = new double[]{0.3657, 0.4180, 0.1690, 0.0473};
                    break;
                case "ascites":
                    expectedValues = new double[]{0.8791, 0.1209};
                    break;
                case "carcinoma":
                    expectedValues = new double[]{0.8718, 0.1282};
                    break;
            }
            Assertions.assertArrayEquals(posteriorVales.get(variable).getValues(), expectedValues, deltaEquals);
        }
        
    }
    
    @Test
    public void vePropagationWithPostResolutionEvidence2() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException, CannotNormalizePotentialException {
        VEPropagation vePropagation;
        EvidenceCase postResolutionEvidence = new EvidenceCase();
        List<Variable> variablesOfInterest = new ArrayList<>();
        variablesOfInterest.add(probNet.getVariable("cholesterol"));
        variablesOfInterest.add(probNet.getVariable("PBC"));
        variablesOfInterest.add(probNet.getVariable("hepatomegaly"));
        // New Finding: sex = female
        Finding finding1 = new Finding(probNet.getVariable("sex"), 1);
        postResolutionEvidence.addFinding(finding1);
        
        // New Finding: hepatotoxic = present
        Finding finding2 = new Finding(probNet.getVariable("hepatotoxic"), 1);
        postResolutionEvidence.addFinding(finding2);
        
        // New Finding: ChHepatitis = active
        Finding finding3 = new Finding(probNet.getVariable("ChHepatitis"), 2);
        postResolutionEvidence.addFinding(finding3);
        
        vePropagation = new VEPropagation(probNet);
        vePropagation.setVariablesOfInterest(variablesOfInterest);
        vePropagation.setPreResolutionEvidence(preResolutionEvidence);
        vePropagation.setPostResolutionEvidence(postResolutionEvidence);
        HashMap<Variable, TablePotential> posteriorVales = vePropagation.getPosteriorValues();
        
        for (Variable variable : variablesOfInterest) {
            double[] expectedValues = new double[0];
            switch (variable.getName()) {
                case "cholesterol":
                    expectedValues = new double[]{0.7315, 0.1980, 0.0705};
                    break;
                case "PBC":
                    expectedValues = new double[]{0.4841, 0.5159};
                    break;
                case "hepatomegaly":
                    expectedValues = new double[]{0.2051, 0.7949};
                    break;
            }
            Assertions.assertArrayEquals(posteriorVales.get(variable).getValues(), expectedValues, deltaEquals);
        }
    }
}
