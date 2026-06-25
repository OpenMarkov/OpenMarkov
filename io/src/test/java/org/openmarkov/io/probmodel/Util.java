/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

public class Util {


    static ProbNet buildNetWithICI() {
        ProbNet probNet = new ProbNet();

        // Variables
        Variable A = new Variable("A", 2);
        Variable B = new Variable("B", 2); 
        Variable C = new Variable("C", 2);

        // Create potentials
        // TablePotential A
        List<Variable> aVariable = new ArrayList<Variable>(1);
        aVariable.add(A);
        TablePotential pA = new TablePotential(aVariable,
				PotentialRole.CONDITIONAL_PROBABILITY);
		pA.getValues()[0] = 0.9;
		pA.getValues()[1] = 0.1;
        
        // TablePotential B
        List<Variable> bVariable = new ArrayList<Variable>(1);
        bVariable.add(B);
		TablePotential pB = new TablePotential(aVariable,
				PotentialRole.CONDITIONAL_PROBABILITY);
		pB.getValues()[0] = 0.8;
		pB.getValues()[1] = 0.2;
        
        //ICI Potential
        List<Variable> iciVariables = new ArrayList<Variable>(3);
        iciVariables.add(C);
        iciVariables.add(A);
        iciVariables.add(B);
        
        ICIPotential iciPotential = new MinPotential (iciVariables);
        iciPotential.setNoisyParameters (A, new double[] {0.8, 0.2, 0.1, 0.9});
        iciPotential.setNoisyParameters (B, new double[] {0.7, 0.3, 0.2, 0.8});
        iciPotential.setLeakyParameters (new double[] {0.1, 0.9});
        
        // Add potentials. Do not add links because addPotential does it.
        
        probNet.addPotential(pA);
        probNet.addPotential(pB);
        probNet.addPotential(iciPotential);
        
        return probNet;
    }
    

	/** Auxiliary method for method writeReadNetwork.
	 * @return An influence diagram with two chance nodes (X and Y), one 
	 * decision (D) and one utility (U). Links: X->Y, Y->D, X->U and D->U.
	 * <code>ProbNet</code> */
	static ProbNet createTrivialID() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());

		// Variables
		Variable X = new Variable("A", 2);
		Variable Y = new Variable("B", 2); 
		Variable D = new Variable("D", 2);
		Variable U = new Variable("U");

		// Create potentials
		// TablePotential X
		List<Variable> xVariable = new ArrayList<Variable>(1);
		xVariable.add(X);
		TablePotential pX = new TablePotential(xVariable,
				PotentialRole.CONDITIONAL_PROBABILITY);
		pX.getValues()[0] = 0.9;
		pX.getValues()[1] = 0.1;
		// TablePotential YX
		List<Variable> yxVariables = new ArrayList<Variable>(2);
		yxVariables.add(Y);
		yxVariables.add(X);
		TablePotential pYX = new TablePotential(yxVariables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		pYX.getValues()[0] = 0.2;
		pYX.getValues()[1] = 0.8;
		pYX.getValues()[2] = 0.9;
		pYX.getValues()[3] = 0.1;
		// Utility potential DX
		List<Variable> dxVariables = new ArrayList<Variable>(2);
		dxVariables.add(U);
		dxVariables.add(D);
		dxVariables.add(X);
		ExactDistrPotential pDX = new ExactDistrPotential(dxVariables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		pDX.getTablePotential().getValues()[0] = 3.0;
		pDX.getTablePotential().getValues()[1] = 1.0;
		pDX.getTablePotential().getValues()[2] = 0.0;
		pDX.getTablePotential().getValues()[3] = 2.0;

		// Add potentials. Do not add links because addPotential do it.
		probNet.addPotential(pX);
		probNet.addPotential(pYX);
		probNet.addPotential(pDX);
		
		// Create agents
		List<StringWithProperties> agents = new ArrayList<StringWithProperties>();
		StringWithProperties agent1 = new StringWithProperties("Agent 1 (no properties)");
		StringWithProperties agent2 = new StringWithProperties("Agent 2");
		agent2.put("DrNo","007");
		agents.add(agent1);
		agents.add(agent2);
		probNet.setAgents(agents);
		return probNet;
	}
	
}
