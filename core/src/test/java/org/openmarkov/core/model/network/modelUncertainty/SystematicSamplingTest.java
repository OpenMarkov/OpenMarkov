package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.InferenceAlgorithmTest;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.factory.NetsFactory;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// TODO - Adapt these test to the new task approach (or remove them)
public class SystematicSamplingTest {
    
    static final String iterationVariableName = "Iteration";
    
    @Test
    public void testIDDecideTestSA() {
        
        ProbNet net = SensitivityAnalysisFactory.buildIDDecideTestSA();
        
        HashMap<String, Integer> positionsParams = new HashMap<>();
        positionsParams.put("sensitivity", 11);
        positionsParams.put("prevalence", 1);
        positionsParams.put("specificity", 4);
        positionsParams.put("utility non-treated disease", 1);
        positionsParams.put("utility not treated", 2);
        positionsParams.put("utility treated disease", 3);
        
        List<UncertainParameter> uncertainParams = SystematicSampling.getUncertainParameters(net);
        
        for (UncertainParameter uncertainParameter : uncertainParams) {
            if (uncertainParameter.hasName()) {
                UncertainValue uncertain = uncertainParameter.uncertainValue;
                Integer expectedPosition = positionsParams.get(uncertain.getName());
                assertEquals(expectedPosition, uncertainParameter.configuration, 0.0);
                ProbDensFunction probDensFunction = uncertain.getProbDensFunction();
                assertNotNull(probDensFunction.getInterval(0.6));
                assertNotNull(uncertainParameter.min(0.6));
                assertNotNull(uncertainParameter.max(0.6));
            }
        }
    }
    
    
    @Test
    public void testSimpleIDWithoutDecisionsSATriangular() {
        testSampleNetwork(SensitivityAnalysisFactory.createSimpleIDWithoutDecisionsTriangular(), 5, 0.0, 1.0);
    }
    
    @Test
    public void testSimpleIDWithoutDecisionsSABeta() {
        testSampleNetwork(SensitivityAnalysisFactory.createSimpleIDWithoutDecisionsBeta(), 5, 0.0, 1.0);
    }
    
    @Test
    public void testSimpleIDWithoutDecisionsDiseaseFourStates() {
        testSampleNetwork(SensitivityAnalysisFactory.createSimpleIDWithoutDecisionsDiseaseFourStates(), 4, 0.1, 0.5);
    }
    
    
    public void testSampleNetwork(ProbNet net, int numIntervals, double min,
                                  double max) {
        
        ProbNet sampledNet;
        TablePotential pot;
        List<UncertainParameter> uncertainParams = SystematicSampling
                .getUncertainParameters(net);
        for (UncertainParameter uncert : uncertainParams) {
            if (uncert.hasName()) {
                sampledNet = sampleNetworkProbParam(net, uncert, numIntervals,
                                                    min, max);
                Potential potential = getPotentialFirstVariable(sampledNet,
                                                                NetsFactory.diseaseName);
                if (potential instanceof ExactDistrPotential) {
                    pot = ((ExactDistrPotential) potential).getTablePotential();
                } else {
                    pot = (TablePotential) potential;
                }
                
                InferenceAlgorithmTest.checkIsAConditionalProbability(pot);
            }
        }
    }
    
    Potential getPotentialFirstVariable(ProbNet probNet, String variableName) {
        List<Potential> potentials = probNet.getPotentials(probNet
                                                                   .getVariable(variableName));
        boolean found = false;
        Potential pot = null;
        if (potentials != null) {
            for (int i = 0; i < potentials.size() && !found; i++) {
                Potential auxPot = potentials.get(i);
                if (auxPot.getVariable(0).getName()
                          .equalsIgnoreCase(variableName)) {
                    found = true;
                    pot = auxPot;
                }
            }
        }
        return pot;
    }
    
    public static ProbNet sampleNetworkProbParam(
            ProbNet originalNet, UncertainParameter uncertainParameter, int numIntervals, double min, double max) {
        return SystematicSampling.sampleNetwork(
                originalNet, uncertainParameter, min, max, numIntervals, iterationVariableName);
    }
    
}
