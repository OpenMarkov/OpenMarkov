package org.openmarkov.gui.desnets;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Configuration;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class performs the part of the evaluation algorithm corresponding to Chance Nodes
 *
 * @author cmyago
 * @version 3.1 -25/08/2023 - Recoded orphan nodes  and data from file treatment
 * @version 3 - 08/03/2023 - Adapted to paper and version 1.2 of the simulation algorithm
 * @version 2 -10/06/2022 - Adapted to Collections and reducing time
 * @version1 1.1 -14/05/2020 - added one Decision node
 */
public class ChanceEvaluation extends GenericEvaluation<ChanceRecord> {



    /**
     * Records with data from file
     */

    /**
     * Creates a ChanceEvaluation object
     *
     * @param probNet DESnet in evaluation
     * @throws OpenMarkovException
     */
    ChanceEvaluation(ProbNet probNet, DESInference desInference) throws OpenMarkovException {
        super(probNet, NodeType.CHANCE, ChanceRecord.class, desInference);
        setChanceRecordParents();
    }

    private void setChanceRecordParents() {
        getDesRecordHashMap().values().forEach(chanceRecord -> {
            chanceRecord.setChanceRecordParents(this);
        });
    }
    
    
    void startSimulation(Finding decisionFinding, int dataIndex) throws OpenMarkovException {
       startSimulation(decisionFinding);
       //Simulate initial Chance nodes (those which does not have an event ancestor)
       //Data From File
       //OrphanNodes and its descendants which haven't an Event node as ancestor
       addValuesOrphanNodes(dataIndex);
       // A chance node with an event among its ancestors starts at its first state, or at 0 if it is numeric: the
       // value clear() leaves. Its relation only says what happens when an event occurs.
   }


    /**
     * Calculates values Chance nodes which have no Event ancestor
     */
    private void addValuesOrphanNodes(int dataIndex) throws OpenMarkovException {

        List<ChanceRecord> remainingOrphan = new ArrayList<>(orphanRecords);
        remainingOrphan.forEach(ChanceRecord::resetEvaluationStatus);
        //26/08/2023 - compute value for nodes whose value is in the data file
        HashMap<String, String> valuesHash = desInference.getDataFromFile().getInputValues(dataIndex);

        while (!remainingOrphan.isEmpty()) {
            ChanceRecord chanceRecord = nextReady(remainingOrphan);
            //Data from file
            if ( (dataIndex > -1) && (valuesHash.containsKey(chanceRecord.getRecordNode().getName()))) {
                double value;
                if (chanceRecord.getRecordVariable().getVariableType() == VariableType.FINITE_STATES) {
                    value = chanceRecord.getRecordVariable().getStateIndex(valuesHash.get(chanceRecord.getRecordNode().getName()));
                } else {
                    value = Double.valueOf(valuesHash.get(chanceRecord.getRecordNode().getName()));
                }
                chanceRecord.setVariableValue(value);
                //Computed data from simulation
            } else {
                computeChanceValue(chanceRecord, null);
            }

        }
    }


    /**
     * Updates the value of Chance variables when an event happens. Data from event happened is stored in eventRecord*
     *
     * @param eventHappened - Event happened data
     */
    @Override
    void update(EventRecord eventHappened) throws OpenMarkovException {
        //TODO Impossible Configuration missing - Change to Infinity??
        List<ChanceRecord> remainingDescendants = new ArrayList<>(eventHappened.getChanceDescendants());
        //If the code commented below is necessary something is going wrong
//        chanceRecords.forEach(ChanceRecord::setEvaluated);
        remainingDescendants.forEach(ChanceRecord::resetEvaluationStatus);

        while (!remainingDescendants.isEmpty()) {
            ChanceRecord chanceRecord = nextReady(remainingDescendants);
            if (!transitionsOnlyOnItsOwnEvents(chanceRecord, eventHappened)) {
                computeChanceValue(chanceRecord, eventHappened);
            }
        }
        desInference.getDesLogTextWriter().logChanceChange(new ArrayList<>(eventHappened.getChanceDescendants()));
    }

    /**
     * A transition table says what happens when one of its events occurs: another event, which reaches the node
     * through a chance parent, makes no transition, and the node keeps its value.
     */
    private static boolean transitionsOnlyOnItsOwnEvents(ChanceRecord chanceRecord, EventRecord eventHappened) {
        Node node = chanceRecord.getRecordNode();
        return node.getPotentials().getFirst() instanceof TransitionTablePotential
               && !node.isParent(eventHappened.getRecordNode());
    }

    /**
     * Removes from {@code pending} the first node none of whose chance parents is pending, the next one to compute. A
     * parent that is not pending does not make it wait, also when an event that has not happened changes it.
     */
    private static ChanceRecord nextReady(List<ChanceRecord> pending) {
        for (int i = 0; i < pending.size(); i++) {
            if (pending.get(i).isUpdatableAmong(pending)) {
                return pending.remove(i);
            }
        }
        // Only a cycle of chance nodes without events, which EveryCycleHasAnEvent refuses before the simulation
        throw new IllegalStateException("The chance nodes " + pending.stream().map(record -> record.getRecordNode().getName())
                                                                      .toList() + " wait for each other");
    }


    /**
     * Computes the value of the variable of chanceNode with the current configuration of Chance Parents when event happens.
     * The parents values have already been calculated
     *
     * @param nodeToComputeRecord node whose variable value is computed using a potential not a file
     * @param eventHappened       event for which the value of chanceNode variable is computed
     */
    private void computeChanceValue(ChanceRecord nodeToComputeRecord, EventRecord eventHappened) throws OpenMarkovException {

//        if (nodeToComputeRecord.isFromFile()) {
//            return;
//        }

        Configuration configuration = new Configuration();

        try {
            if (nodeToComputeRecord.hasDecisionParent()) {
                configuration.addFinding(decisionFinding);
            }
            //The event is added to the configuration of parents iff is parent of nodeToComputeRecord
            if ((eventHappened != null) && (nodeToComputeRecord.getRecordNode().isParent(eventHappened.getRecordNode()))) {
                configuration.addFinding(eventHappened.getRecordVariable(), eventHappened.getVariableValue());
            }
            for (Node chanceParent : nodeToComputeRecord.getParentsByType(NodeType.CHANCE)) {
                //18/03/2023 check commented code; why is it there?
                //When having a loop and the computeNodeRecord is being initialised at the beginning of each simulation
//                if (nodeToComputeRecord.getRecordNode().equals(chanceParent) && !nodeToComputeRecord.isEvaluated()) {
//                    continue;
//                }
                //check end
                Variable parentVariable = chanceParent.getVariable();

                //TODO Check if there is a problem with creating findings
                Finding chanceFinding = null;
                if (parentVariable.getVariableType().equals(VariableType.FINITE_STATES))
                    chanceFinding = new Finding(parentVariable, (int) getDesRecordHashMap().get(chanceParent).getVariableValue());
                else
                    chanceFinding = new Finding(parentVariable, getDesRecordHashMap().get(chanceParent).getVariableValue());
                configuration.addFinding(chanceFinding);
            }

        } catch (OpenMarkovException e) {
            e.printStackTrace();
        }
        nodeToComputeRecord.setVariableValue(configuration);
        nodeToComputeRecord.setEvaluated();
    }


}
