/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.ceanalysis.DANDecompositionIntoSymmetricDANsCEA;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class danAlgorithmTests {
    
    private final double deltaEquals = Math.pow(10, -4);
    
    @BeforeEach public void setUp() {
    
    
    }
    
    @Test
    public void testOneDecision() throws ProbNetParserException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        String networkName = "DAN-one-decision-CE.pgmx";
        String path = "networks/dan/";
        double lambda = 30000;
        
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        PGMXReader.NetworkAndEvidence probNetInfo = pgmxReader.read(getClass().getClassLoader()
                                                                              .getResource(path + networkName));
        ProbNet probNet = probNetInfo.probNet();
        
        CEP resultCEA = new DANDecompositionIntoSymmetricDANsCEA(probNet).getCEP();
        TablePotential resultUNI = new DANDecompositionIntoSymmetricDANsEvaluation(probNet).getUtility();
        
        Assertions.assertEquals(resultUNI.getValues()[0], resultCEA.getEffectiveness(lambda) * lambda - resultCEA.getCost(lambda), deltaEquals);
        
    }
    
    @Disabled("Missing test net DAN-2tests.pgmx")
    @Test
    public void testDAN2tests() throws ProbNetParserException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        String networkName = "DAN-2tests.pgmx";
        String path = "networks/IDCEAnTherapies/";
        double lambda = 30000;
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        
        PGMXReader.NetworkAndEvidence probNetInfo = pgmxReader.read(getClass().getClassLoader()
                                                                              .getResource(path + networkName));
        ProbNet probNet = probNetInfo.probNet();
        
        CEP resultCEA = new DANDecompositionIntoSymmetricDANsCEA(probNet).getCEP();
        TablePotential resultUNI = new DANDecompositionIntoSymmetricDANsEvaluation(probNet).getUtility();
        
        Assertions.assertEquals(resultUNI.getValues()[0], resultCEA.getEffectiveness(lambda) * lambda - resultCEA.getCost(lambda), deltaEquals);
        
    }
    
    @Disabled("Missing test net DAN-3tests.pgmx")
    @Test
    public void testDAN3tests() throws ProbNetParserException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        String networkName = "DAN-3tests.pgmx";
        String path = "networks/IDCEAnTherapies/";
        double lambda = 30000;
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        
        PGMXReader.NetworkAndEvidence probNetInfo = pgmxReader.read(getClass().getClassLoader()
                                                                              .getResource(path + networkName));
        ProbNet probNet = probNetInfo.probNet();
        
        CEP resultCEA = new DANDecompositionIntoSymmetricDANsCEA(probNet).getCEP();
        TablePotential resultUNI = new DANDecompositionIntoSymmetricDANsEvaluation(probNet).getUtility();
        
        Assertions.assertEquals(resultUNI.getValues()[0], resultCEA.getEffectiveness(lambda) * lambda - resultCEA.getCost(lambda), deltaEquals);
        
    }
    
    @Disabled("Missing test net DAN-4tests.pgmx")
    @Test
    public void testDAN4tests() throws ProbNetParserException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        String networkName = "DAN-4tests.pgmx";
        String path = "networks/IDCEAnTherapies/";
        double lambda = 30000;
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        
        PGMXReader.NetworkAndEvidence probNetInfo = pgmxReader.read(getClass().getClassLoader()
                                                                              .getResource(path + networkName));
        ProbNet probNet = probNetInfo.probNet();
        
        CEP resultCEA = new DANDecompositionIntoSymmetricDANsCEA(probNet).getCEP();
        TablePotential resultUNI = new DANDecompositionIntoSymmetricDANsEvaluation(probNet).getUtility();
        
        Assertions.assertEquals(resultUNI.getValues()[0], resultCEA.getEffectiveness(lambda) * lambda - resultCEA.getCost(lambda), deltaEquals);
        
    }
    
    
    @Test
    public void oneChanceCETest() throws ProbNetParserException, NotEvaluableNetworkException.NotApplicableNetwork, NotEvaluableNetworkException.UnsatisfiedConstraints, NonProjectablePotentialException, IncompatibleEvidenceException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        String networkName = "DAN-one-chance-ce.pgmx";
        String path = "networks/dan/";
        double lambda = 30000;
        
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        PGMXReader.NetworkAndEvidence probNetInfo = pgmxReader.read(getClass().getClassLoader()
                                                                              .getResource(path + networkName));
        ProbNet probNet = probNetInfo.probNet();
        
        CEP resultCEA = new DANDecompositionIntoSymmetricDANsCEA(probNet).getCEP();
        TablePotential resultUNI = new DANDecompositionIntoSymmetricDANsEvaluation(probNet).getUtility();
        
        Assertions.assertEquals(resultUNI.getValues()[0], resultCEA.getEffectiveness(lambda) * lambda - resultCEA.getCost(lambda), deltaEquals);
        
    }
}
