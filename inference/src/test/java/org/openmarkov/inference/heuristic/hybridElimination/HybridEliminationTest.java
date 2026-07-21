/*
 * Copyright 2011 CISIAD, UNED, Spain
 *
 * Licensed under the European Union Public Licence, version 1.1 (EUPL)
 *
 * Unless required by applicable law, this code is distributed
 * on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.hybridElimination;

//import org.openmarkov.core.inference.InferenceAlgorithmBNTest;
//import org.openmarkov.core.inference.PartialOrder;
import org.openmarkov.core.model.network.ProbNet;
//import org.openmarkov.inference.variableElimination.VariableElimination;
//import org.openmarkov.io.probmodel.PGMXReader;


public class HybridEliminationTest { // extends InferenceAlgorithmBNTest {
    private ProbNet probNet1;
    
    private String netName1 = "MPAD-TriangulationTempTest.pgmx";
    
    //todo: Most code in here cannot even compile due to changes
    
    /*
    @Before public void setUp() throws Exception {
        URL url1 = this.getClass().getClassLoader().getResource(netName1);
        String fileNameWithPath1 = url1.getFile();
        PGMXReader reader = new PGMXReader();
        probNet1 = reader.loadProbNet(fileNameWithPath1).getProbNet();
    }
    
    @Test public void testHybridElimination() throws WrongGraphStructureException, DoEditExceptio {
        assertNotNull(probNet1);
        PartialOrder partialOrder = new PartialOrder(probNet1);
        HybridElimination heuristic = new HybridElimination(probNet1, partialOrder.getOrder());
        
        
        Variable variableToDelete;
        
        while ((variableToDelete = heuristic.getVariableToDelete()) != null) {
            System.out.println(variableToDelete);
            RemoveNodeEdit edit = new RemoveNodeEdit(probNet1, variableToDelete);
            PNUndoableEditEvent event = new PNUndoableEditEvent(this, edit, probNet1);
            heuristic.undoableEditWillHappen(event);
            edit.doEdit();
            heuristic.undoableEditHappened(event);
            variableToDelete = heuristic.getVariableToDelete();
        }
        // Utility nodes remain
        assertEquals(4, probNet1.getVariables().size());
    }
    
    @Override
    public InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet)
            throws NotEvaluableNetworkException {
        InferenceAlgorithm inferenceAlgorithm = new VariableElimination(probNet);
        inferenceAlgorithm.setHeuristicFactory(new HeuristicFactory() {
            
            @Override
            public EliminationHeuristic getHeuristic(ProbNet probNet, List<List<Variable>> variables) {
                return new HybridElimination(probNet, variables);
            }
        });
        return inferenceAlgorithm;
    }
     */
}
