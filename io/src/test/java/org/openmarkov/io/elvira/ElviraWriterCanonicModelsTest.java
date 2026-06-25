/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.jupiter.api.*;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraWriterCanonicModelsTest {
    
    // Attributes
    private ProbNet miniICI;
    
    // Methods
    
    /** Creates a small ProbNet */
    @BeforeEach
    public void setUp() {
        //probNet small
        //Variables
        String y = new String("Y");
        String x1 = new String("X1");
        String x2 = new String("X2");
        
        //finite States variables
        Variable variableY = new Variable(y, 4);
        Variable variableX1 = new Variable(x1, 3);
        Variable variableX2 = new Variable(x2, 2);
        
        //additional properties
        String relevance = new String("Relevance");
        String value = new String("7.0");
        
        variableY.setAdditionalProperty(relevance, value);
        variableX1.setAdditionalProperty(relevance, value);
        variableX2.setAdditionalProperty(relevance, value);
        
        State severe = new State("severe");
        State moderate = new State("moderate");
        State mild = new State("mild");
        State increased = new State("increased");
        State normal = new State("normal");
        State decreased = new State("decreased");
        State absent = new State("absent");
        State present = new State("present");
        
        //Setting variable states for Y
        State[] statesY = {severe, moderate, mild, absent};
        variableY.setStates(statesY);
        //Setting variable states for X1
        State[] statesX1 = {increased, normal, decreased};
        variableX1.setStates(statesX1);
        //Setting variable states for X2
        State[] statesX2 = {absent, present};
        variableX2.setStates(statesX2);
        
        // Create Potentials
        // X1 Potential
        ArrayList<Variable> X1Variables = new ArrayList<>();
        X1Variables.add(variableX1);
        double[] tableX1 = {0.33333, 0.33333, 0.33333};
        TablePotential X1Potential = new TablePotential(X1Variables, PotentialRole.CONDITIONAL_PROBABILITY, tableX1);
        
        // X2 Potential
        ArrayList<Variable> X2Variables = new ArrayList<>();
        X2Variables.add(variableX2);
        double[] tableX2 = {0.5, 0.5};
        TablePotential X2Potential = new TablePotential(X2Variables, PotentialRole.CONDITIONAL_PROBABILITY, tableX2);
        
        // MaxPotential
        // Variables
        ArrayList<Variable> maxPotentialVariables = new ArrayList<>();
        maxPotentialVariables.add(variableY);
        maxPotentialVariables.add(variableX1);
        maxPotentialVariables.add(variableX2);
        MaxPotential generalizedMax = new MaxPotential(maxPotentialVariables);
        // Add noisy parameters
        // SubPotential YX1
        double[] tableYX1 = {0.67, 0.24, 0.08, 0.01, 1.0, 0.0, 0.0, 0.0, 0.09, 0.18, 0.32, 0.41};
        // SubPotential YX2
        double[] tableYX2 = {1.0, 0.0, 0.0, 0.0, 0.49, 0.15, 0.27, 0.09};
        // Leaky parameters
        double[] residual = {1.0, 0.0, 0.0, 0.0};
        
        generalizedMax.setNoisyParameters(variableX1, tableYX1);
        generalizedMax.setNoisyParameters(variableX2, tableYX2);
        generalizedMax.setLeakyParameters(residual);
        
        miniICI = new ProbNet();
        
        NodeType nodeType = NodeType.CHANCE;
        
        miniICI.addNode(variableY, nodeType);
        miniICI.addNode(variableX1, nodeType);
        miniICI.addNode(variableX2, nodeType);
        
        // Links throws NodeNotFoundException
        miniICI.addLink(variableX1, variableY, true);
        miniICI.addLink(variableX2, variableY, true);
        
        // Potentials
        miniICI.addPotential(X1Potential);
        miniICI.addPotential(X2Potential);
        miniICI.addPotential(generalizedMax);
        
    }
    
    @Test
    public final void testWriteProbNet() throws ParserException, IOException, WriterException.CannotCreateFile, WriterException.UnknownNetworkType, WriterException.ICIModelNotSupportedByElvira {
        URL url = this.getClass().getClassLoader().getResource("trivial3jensen.elv");
        String rootPath = url.getPath();
        File file = new File(rootPath);
        String absolutePath = file.getAbsolutePath();
        String name = file.getName();
        rootPath = absolutePath.substring(0, absolutePath.length() - name.length());
        
        String fullNetworkName = rootPath + "MiniICI.elv";
        System.out.println(fullNetworkName);
        new ElviraWriter().write(fullNetworkName, miniICI, null);
        ProbNetReader probNetReader = new ElviraParser();
        probNetReader.read(new File(fullNetworkName).toURI().toURL()).probNet();
    }
    
}
