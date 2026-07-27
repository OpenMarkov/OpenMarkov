/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openmarkov.core.testTags.TestSpeed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.UtilityOperations;
import org.openmarkov.core.model.network.Variable;
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

    /**
     * How many time slices this test asks the network to be evaluated over, in place of the
     * hundred the file declares.
     * <p>
     * What is checked below is that the Riemann sum of the utilities of the slices agrees with a
     * single global cost-effectiveness analysis. That agreement either holds or does not, whatever
     * the horizon; it does not become truer with more slices. The horizon does decide the price,
     * and steeply, because the global analysis expands every slice into one network: measured on
     * this network it costs 1.6 seconds per analysis at twenty slices and 39 at a hundred, so the
     * test as a whole went from about a hundred seconds to about four.
     * <p>
     * What the shorter horizon gives up is the long run: a fault that only showed up after many
     * cycles, such as numerical error piling up, would no longer be caught here.
     */
    private static final int HORIZON = 20;
    
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
    
    // A few seconds on its own, so it is tagged slow and left out of the pre-commit hook.
    @Tag(TestSpeed.SLOW)
    @Test
    public void veTemporalEvaluationTest() throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(HORIZON);
        TemporalEvaluation temporalEvaluation = new TemporalEvaluation(probNet);
        temporalEvaluation.setPreResolutionEvidence(preResolutionEvidence);
        GTablePotential atemporalUtility = (GTablePotential) temporalEvaluation.getAtemporalUtility();
        Assertions.assertEquals(0, ((CEP) atemporalUtility.elementTable.get(0)).getCost(0), deltaEquals);
        Assertions.assertEquals(21639.98, ((CEP) atemporalUtility.elementTable.get(1)).getCost(0), deltaEquals);
        Assertions.assertEquals(26100, ((CEP) atemporalUtility.elementTable.get(2)).getCost(0), deltaEquals);
        
        List<Potential> potentialsPerSlice = temporalEvaluation.getUtilityPotentialsPerSlice();
        // One entry per slice returned, not a fixed 101. A longer array would leave trailing
        // zeros, and the Riemann sums below would then cover slices that were never evaluated.
        int slices = potentialsPerSlice.size();
        double[] costs_UCI = new double[slices];
        double[] effectiveness_UCI = new double[slices];
        double[] costs_BCI_Sim = new double[slices];
        double[] effectiveness_BCI_Sim = new double[slices];
        double[] costs_BCI_Seq = new double[slices];
        double[] effectiveness_BCI_Seq = new double[slices];

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


