/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.variableElimination;


import org.junit.jupiter.api.BeforeEach;

/**
 * @author Manuel Arias
 */
public class BayesianNetworkTest {
    
    @BeforeEach public void setUp() {
    }
    
    //TODO: Most code here cannot compile due to changes in the structure
    
    /*
    @Test
    public void testBayesianNetworksInference() throws Exception {
        NetsRepository netsRepository = new NetsRepository();
        List<URL> bayesianNetworksURLList = netsRepository.getNetworks(BayesianNetworkType.getUniqueInstance());
        PGMXReader_0_2 reader = new PGMXReader_0_2();
        for (URL bayesianNetworkURL : bayesianNetworksURLList) {
            ProbNet probNet = reader.loadProbNet(bayesianNetworkURL.getFile(), bayesianNetworkURL.openStream());
            System.out.println("Checking network: " + bayesianNetworkURL.getFile());
            VariableEliminationCore elimination = new VariableEliminationCore(probNet);
            elimination.getPosteriorValues();
        }
    }
    */
    
    
}
