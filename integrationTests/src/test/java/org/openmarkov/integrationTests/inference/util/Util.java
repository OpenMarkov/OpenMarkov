/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.util;


import bitbucket.NetsRepository;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Util {

	/**
	 * Reads all the networks from the repository that meet the restriction given in the parameter "networkType"
	 * @param networkType <code>NetworkType</code>
	 * @return <code>List</code> of <code>ProbNet</code>s
	 */
    public static List<ProbNet> readProbNetsDB(NetworkType networkType) {
        List<URL> bayesianNetworksURLList = NetsRepository.getNetworks(networkType);
		PGMXReader reader = new PGMXReader();
		List<ProbNet> probNetsDB = new ArrayList<ProbNet>();
		List<String> wrongNetworksNames = new ArrayList<String>();
		int readingErrors = 0;
		for (URL bayesianNetworkURL : bayesianNetworksURLList) {
			ProbNet probNet = null;
			String fileName = null;
			try {
				fileName = bayesianNetworkURL.getFile();
				probNet = reader.read(bayesianNetworkURL).probNet();
				probNetsDB.add(probNet);
			} catch (ProbNetParserException | IOException e) {
				readingErrors++;
				wrongNetworksNames.add(fileName);
			}
		}
		if (readingErrors > 0) {
			if (probNetsDB.isEmpty()) {
				System.err.println("No Bayesian networks for testing due to reading errors.");
			} else {
				System.err.println("Some errors reading these networks:");
			}
			System.err.println();
			for (String wrongNetworkName : wrongNetworksNames) {
				System.err.println(wrongNetworkName);
			}
		} else {
			if (probNetsDB.isEmpty()) {
				System.err.println("No networks found in repository.");
			}
		}
		// Order the networks, from smallest to largest number of variables
		int numNetworks = probNetsDB.size();
		ProbNet aux;
		for (int i = 0; i < numNetworks - 1; i++) {
			for (int j = i + 1; j < numNetworks; j++) {
				if (probNetsDB.get(i).getVariables().size() > probNetsDB.get(j).getVariables().size()) {
					aux = probNetsDB.get(j);
					probNetsDB.set(j, probNetsDB.get(i));
					probNetsDB.set(i, aux);
				}
			}
		}

		return probNetsDB;
	}

	// TODO - Remove this method
	public static List<ProbNet> filterNonPureTablePotentialProbNets(List<ProbNet> probNets) {
		List<ProbNet> filteredProbNets = new ArrayList<ProbNet>(probNets.size());
		for (ProbNet probNet : probNets) {
			List<Potential> potentials = probNet.getPotentials();
			int numPotentials = potentials.size();
			boolean include = true;
			for (int i = 0; include && i < numPotentials; i++) {
				include &= potentials.get(i).getClass() == TablePotential.class;
			}
			if (include) {
				filteredProbNets.add(probNet);
			}
		}
		return filteredProbNets;
	}
}
