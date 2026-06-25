/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.*;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.TemporalEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by JORGE on 08/02/2017.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class midCochlearTests {
    private final String networkName = "networks/mid/MID-Cochlear.pgmx";
    
    // Delta parameter for Assertions.Equals methods
    private final double deltaEquals = Math.pow(10, -4);
    
    private ProbNet probNet;
    private EvidenceCase preResolutionEvidence;
    
    @BeforeEach public void setUp() throws java.net.URISyntaxException, org.openmarkov.core.exception.ParserException, FileNotFoundException {
        
        // Load the network: ID-decide-test
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        PGMXReader.NetworkAndEvidence probNetInfo = null;
        probNetInfo = pgmxReader.read(getClass().getClassLoader().getResource(networkName));
        this.probNet = probNetInfo.probNet();
        if (probNetInfo.evidence().size() != 0) {
            this.preResolutionEvidence = probNetInfo.evidence().get(0);
        }
    }
    
    @Disabled
    @Test
    public void veTemporalEvaluationTest() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        TemporalEvaluation temporalEvaluation = new TemporalEvaluation(probNet);
        temporalEvaluation.setPreResolutionEvidence(preResolutionEvidence);
        GTablePotential atemporalUtility = (GTablePotential) temporalEvaluation.getAtemporalUtility();
        Assertions.assertEquals(0, ((CEP) atemporalUtility.elementTable.get(0)).getCost(0), deltaEquals);
        Assertions.assertEquals(21639.98, ((CEP) atemporalUtility.elementTable.get(1)).getCost(0), deltaEquals);
        Assertions.assertEquals(26100, ((CEP) atemporalUtility.elementTable.get(2)).getCost(0), deltaEquals);
        
        List<Potential> potentialsPerSlice = temporalEvaluation.getUtilityPotentialsPerSlice();
        double[] costs_UCI = new double[101];
        double[] effectiveness_UCI = new double[101];
        double[] costs_BCI_Sim = new double[101];
        double[] effectiveness_BCI_Sim = new double[101];
        double[] costs_BCI_Seq = new double[101];
        double[] effectiveness_BCI_Seq = new double[101];

        int slice = 0;
        for (Potential tablePotential : potentialsPerSlice) {
            costs_UCI[slice] = ((CEP) ((GTablePotential) tablePotential).elementTable.get(0)).getCost(0);
            effectiveness_UCI[slice] = ((CEP) ((GTablePotential) tablePotential).elementTable.get(0))
                    .getEffectiveness(0);
            costs_BCI_Sim[slice] = ((CEP) ((GTablePotential) tablePotential).elementTable.get(1)).getCost(0);
            effectiveness_BCI_Sim[slice] = ((CEP) ((GTablePotential) tablePotential).elementTable.get(1))
                    .getEffectiveness(0);
            costs_BCI_Seq[slice] = ((CEP) ((GTablePotential) tablePotential).elementTable.get(2)).getCost(0);
            effectiveness_BCI_Seq[slice] = ((CEP) ((GTablePotential) tablePotential).elementTable.get(2))
                    .getEffectiveness(0);
            slice++;
        }

        double c_UCI = UtilityOperations.applyLeftRiemannSum(costs_UCI, 1) + ((CEP) atemporalUtility.elementTable.get(0)).getCost(0);
        double e_UCI = UtilityOperations.applyLeftRiemannSum(effectiveness_UCI, 1);

        double c_BCI_Sim = UtilityOperations.applyLeftRiemannSum(costs_BCI_Sim, 1) + ((CEP) atemporalUtility.elementTable.get(1)).getCost(0);
        double e_BCI_Sim = UtilityOperations.applyLeftRiemannSum(effectiveness_BCI_Sim, 1);

        double c_BCI_Seq = UtilityOperations.applyLeftRiemannSum(costs_BCI_Seq, 1) + ((CEP) atemporalUtility.elementTable.get(2)).getCost(0);
        double e_BCI_Seq = UtilityOperations.applyLeftRiemannSum(effectiveness_BCI_Seq, 1);
        Variable decisionVariable = null;
        decisionVariable = probNet.getVariable("Intervention decided");
        
        //Asserting that Left Rieman summ is equals to a transition at the end
        probNet.getInferenceOptions().getTemporalOptions().setTransition(TemporalOptions.TransitionTime.END);
        CEAnalysis veceaDecision = new VECEAnalysis(probNet);
        veceaDecision.setPreResolutionEvidence(preResolutionEvidence);
        veceaDecision.setDecisionVariable(decisionVariable);
        GTablePotential ceaResult = veceaDecision.getUtility();
        double c_uci_cea = ((CEP) (ceaResult.elementTable.get(0))).getCost(0);
        double e_uci_cea = ((CEP) (ceaResult.elementTable.get(0))).getEffectiveness(0);
        double c_bciSim_cea = ((CEP) (ceaResult.elementTable.get(1))).getCost(0);
        double e_bciSim_cea = ((CEP) (ceaResult.elementTable.get(1))).getEffectiveness(0);
        double c_bciSeq_cea = ((CEP) (ceaResult.elementTable.get(2))).getCost(0);
        double e_bciSeq_cea = ((CEP) (ceaResult.elementTable.get(2))).getEffectiveness(0);
        
        Assertions.assertEquals(c_UCI, c_uci_cea, deltaEquals);
        Assertions.assertEquals(e_UCI, e_uci_cea, deltaEquals);
        Assertions.assertEquals(c_BCI_Sim, c_bciSim_cea, deltaEquals);
        Assertions.assertEquals(e_BCI_Sim, e_bciSim_cea, deltaEquals);
        Assertions.assertEquals(c_BCI_Seq, c_bciSeq_cea, deltaEquals);
        Assertions.assertEquals(e_BCI_Seq, e_bciSeq_cea, deltaEquals);
        
        //Asserting that Right Riemann Summ is equals to a transition at the beginning
        probNet.getInferenceOptions().getTemporalOptions().setTransition(TemporalOptions.TransitionTime.BEGINNING);
        c_UCI = UtilityOperations.applyRightRiemannSum(costs_UCI, 1) + ((CEP) atemporalUtility.elementTable.get(0)).getCost(0);
        e_UCI = UtilityOperations.applyRightRiemannSum(effectiveness_UCI, 1);

        c_BCI_Sim = UtilityOperations.applyRightRiemannSum(costs_BCI_Sim, 1) + ((CEP) atemporalUtility.elementTable.get(1)).getCost(0);
        e_BCI_Sim = UtilityOperations.applyRightRiemannSum(effectiveness_BCI_Sim, 1);

        c_BCI_Seq = UtilityOperations.applyRightRiemannSum(costs_BCI_Seq, 1) + ((CEP) atemporalUtility.elementTable.get(2)).getCost(0);
        e_BCI_Seq = UtilityOperations.applyRightRiemannSum(effectiveness_BCI_Seq, 1);
        
        veceaDecision = new VECEAnalysis(probNet);
        veceaDecision.setPreResolutionEvidence(preResolutionEvidence);
        veceaDecision.setDecisionVariable(decisionVariable);
        ceaResult = veceaDecision.getUtility();
        c_uci_cea = ((CEP) (ceaResult.elementTable.get(0))).getCost(0);
        e_uci_cea = ((CEP) (ceaResult.elementTable.get(0))).getEffectiveness(0);
        c_bciSim_cea = ((CEP) (ceaResult.elementTable.get(1))).getCost(0);
        e_bciSim_cea = ((CEP) (ceaResult.elementTable.get(1))).getEffectiveness(0);
        c_bciSeq_cea = ((CEP) (ceaResult.elementTable.get(2))).getCost(0);
        e_bciSeq_cea = ((CEP) (ceaResult.elementTable.get(2))).getEffectiveness(0);
        
        Assertions.assertEquals(c_UCI, c_uci_cea, deltaEquals);
        Assertions.assertEquals(e_UCI, e_uci_cea, deltaEquals);
        Assertions.assertEquals(c_BCI_Sim, c_bciSim_cea, deltaEquals);
        Assertions.assertEquals(e_BCI_Sim, e_bciSim_cea, deltaEquals);
        Assertions.assertEquals(c_BCI_Seq, c_bciSeq_cea, deltaEquals);
        Assertions.assertEquals(e_BCI_Seq, e_bciSeq_cea, deltaEquals);
        
    }
    
}


