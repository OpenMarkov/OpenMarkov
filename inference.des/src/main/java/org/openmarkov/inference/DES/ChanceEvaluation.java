package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.*;

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


   void startSimulation(Finding decisionFinding, int dataIndex) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
       startSimulation(decisionFinding);
       //Simulate initial Chance nodes (those which does not have an event ancestor)
       //Data From File
       //OrphanNodes and its descendants which haven't an Event node as ancestor
       addValuesOrphanNodes(dataIndex);
   }


    /**
     * Calculates values Chance nodes which have no Event ancestor
     */
    private void addValuesOrphanNodes(int dataIndex) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {

        List<ChanceRecord> remainingOrphan = new ArrayList<>(orphanRecords);
        remainingOrphan.forEach(ChanceRecord::resetEvaluationStatus);
        //26/08/2023 - compute value for nodes whose value is in the data file
        HashMap<String, String> valuesHash = desInference.getDataFromFile().getInputValues(dataIndex);

        while (!remainingOrphan.isEmpty()) {
            int i = 0;
            while (!remainingOrphan.get(i).isUpdatable()) ++i;
            ChanceRecord chanceRecord = remainingOrphan.remove(i);
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
    void update(EventRecord eventHappened) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        //TODO Impossible Configuration missing - Change to Infinity??
        List<ChanceRecord> remainingDescendants = new ArrayList<>(eventHappened.getChanceDescendants());
        //If the code commented below is necessary something is going wrong
//        chanceRecords.forEach(ChanceRecord::setEvaluated);
        remainingDescendants.forEach(ChanceRecord::resetEvaluationStatus);

        while (!remainingDescendants.isEmpty()) {
            int i = 0;
            while (!remainingDescendants.get(i).isUpdatable())
                ++i;
            ChanceRecord chanceRecord = remainingDescendants.remove(i);
            computeChanceValue(chanceRecord, eventHappened);
        }
        desInference.getDesLogTextWriter().logChanceChange(new ArrayList<>(eventHappened.getChanceDescendants()));
    }


    /**
     * Computes the value of the variable of chanceNode with the current configuration of Chance Parents when event happens.
     * The parents values have already been calculated
     *
     * @param nodeToComputeRecord node whose variable value is computed using a potential not a file
     * @param eventHappened       event for which the value of chanceNode variable is computed
     */
    private void computeChanceValue(ChanceRecord nodeToComputeRecord, EventRecord eventHappened) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {

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
