/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.inference.tasks.CE_PSA;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author jperez-martin
 */
public class VECEPSA extends VariableElimination implements CE_PSA {

    private static final Logger logger = LogManager.getLogger(VECEPSA.class);

    private Collection<GTablePotential> ceaResults;

    private int progress;

    private boolean useMultithreading;

    private int numSimulations;
    
    private Variable decisionVariable;
    
    /**
     * @param network a symmetric network having at least two criteria (and usually decisions and utility nodes)
     */
    public VECEPSA(ProbNet network) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        super(network);
        
    }
    
    private void resolve()
            throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        List<GTablePotential> results = new ArrayList<>();
        progress = 0;
        if (useMultithreading) {
            int numThreads = Runtime.getRuntime().availableProcessors();
            boolean success = false;
            while (!success && numThreads > 0) {
                try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
                    List<Future<GTablePotential>> list = new ArrayList<>();
                    for (int i = 0; i < numSimulations; ++i) {
                        Simulation simulation = new Simulation(probNet);
                        list.add(executor.submit(simulation));
                    }
                    int simulationIndex = 0;
                    for (Future<GTablePotential> result : list) {
                        results.add(result.get());
                        progress = simulationIndex * 100 / numSimulations;
                        simulationIndex++;
                    }
                    success = true;
                } catch (InterruptedException | ExecutionException e) {
                    logger.warn("PSA failed with {} threads: {}", numThreads, e.getMessage(), e);
                    results.clear();
                    numThreads /= 2;
                }
            }
        } else {
            for (int i = 0; i < numSimulations; ++i) {
                sampleNetworkPotentials(probNet);
                CEAnalysis veEvaluation = new VECEAnalysis(probNet);
                veEvaluation.setPreResolutionEvidence(getPreResolutionEvidence());
                veEvaluation.setDecisionVariable(this.decisionVariable);
                results.add(veEvaluation.getUtility());
                progress = i * 100 / numSimulations;
            }
        }
        this.ceaResults = results;
        progress = 100;
    }
    
    public void setUseMultithreading(boolean useMultithreading) {
        this.useMultithreading = useMultithreading;
    }
    
    public void setNumSimulations(int numSimulations) {
        this.numSimulations = numSimulations;
    }
    
    private static void sampleNetworkPotentials(ProbNet probNet) {
        for (Node node : probNet.getNodes()) {
            List<Potential> sampledPotentials = new ArrayList<>();
            for (Potential potential : node.getPotentials()) {
                sampledPotentials.add(potential.sample());
            }
            node.setPotentials(sampledPotentials);
        }
        
    }
    
    @Override public Collection<GTablePotential> getCEPPotentials()
            throws NonProjectablePotentialException, IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        if (ceaResults == null) {
            resolve();
        }
        return ceaResults;
    }
    
    @Override public void setDecisionVariable(Variable decisionSelected) {
        this.decisionVariable = decisionSelected;
    }
    
    private class Simulation implements Callable<GTablePotential> {
        
        final ProbNet probNet;
        
        Simulation(ProbNet probNet) {
            super();
            this.probNet = probNet;
        }
        
        @Override
        public GTablePotential call() throws IncompatibleEvidenceException, NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
            sampleNetworkPotentials(probNet);
            CEAnalysis veEvaluation = new VECEAnalysis(probNet);
            veEvaluation.setPreResolutionEvidence(getPreResolutionEvidence());
            veEvaluation.setDecisionVariable(decisionVariable);
            return veEvaluation.getUtility();
        }
    }
    
}
