package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.inference.DES.exception.NodeMustBeChance;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Stores data from Variables from Chance nodes
 *
 * @author cmyago
 * @version 3 - 10/06/2022 - adapted to Collection
 */
public class ChanceRecord extends DESRecord {


    /**
     * True if the value of recordVariable has been computed in the current simulation
     */
    protected boolean evaluated;
    /**
     * ChanceRecords of the chance parents
     */
    Collection<ChanceRecord> chanceRecordParents;

//    /**
//     * Constructor. Object created at the beginning of the simulation (time =0)
//     *
//     * @param chanceNode    node for which the record is created
//     * @param variableValue if the variable represented by chanceNODE has VariableType.NUMERIC,it is its value.
//     *                      If the variable is "Finite States" numericValue is the index of the recorded state.
//     * @param isFromFile    true if the value of the chanceRecord variable is taken from a file
//     * @throws OpenMarkovException when chanceNode hasn't NodeType.CHANCE or Variable.VariableType isn't VariableType.NUMERIC or VariableType.FINITE_STATES
//     */
//    public ChanceRecord(Node chanceNode, double variableValue, boolean isFromFile) throws OpenMarkovException {
//        this(chanceNode, variableValue, 0);
//    }


//    /**
//     * Constructor. Object created at the beginning of the simulation (time =0)
//     *
//     * @param chanceNode    node for which the record is created
//     * @param variableValue if the variable represented by chanceNODE has VariableType.NUMERIC,it is its value.
//     *                      If the variable is "Finite States" numericValue is the index of the recorded state.
//     * @throws OpenMarkovException when chanceNode hasn't NodeType.CHANCE or Variable.VariableType isn't VariableType.NUMERIC or VariableType.FINITE_STATES
//     */
//    public ChanceRecord(Node chanceNode, double variableValue) throws OpenMarkovException {
//        this(chanceNode, variableValue, 0);
//    }


    /**
     * Constructor. Object created at the beginning of the simulation (time =0) with value 0
     *
     * @param chanceNode node for which the record is created
     * @throws OpenMarkovException when chanceNode hasn't NodeType.CHANCE or Variable.VariableType isn't VariableType.NUMERIC or VariableType.FINITE_STATES
     */
    public ChanceRecord(Node chanceNode) throws OpenMarkovException {
        this(chanceNode, 0);
    }


    /**
     * Constructor
     *
     * @param chanceNode    node for which the record is created
     * @param variableValue if the variable represented by chanceNODE has VariableType.NUMERIC,it is its value.
     *                      If the variable is "Finite States" numericValue is the index of the recorded state.
     * @throws OpenMarkovException when chanceNode hasn't NodeType.CHANCE or Variable.VariableType isn't VariableType.NUMERIC or VariableType.FINITE_STATES
     */
    public ChanceRecord(Node chanceNode, double variableValue) throws OpenMarkovException {
        super(chanceNode);
        if ((chanceNode.getNodeType() != NodeType.CHANCE)
                || ((chanceNode.getVariable().getVariableType() != VariableType.NUMERIC)
                && (chanceNode.getVariable().getVariableType() != VariableType.FINITE_STATES)))
            throw new NodeMustBeChance(chanceNode);
        setVariableValue(variableValue);
        clear();
    }


    /**
     * Sets isInitialized and valueHasChanged to its previous values
     */
    public void clear() {
        super.clear();
        evaluated = false;
    }


    /**
     * True if the value of recordVariable has been computed in the current simulation
     */
    public boolean isEvaluated() {
        return evaluated;
    }

    /**
     * Marks the node as evaluated
     */
    public void setEvaluated() {
        this.evaluated = true;
    }

    /**
     * Marks the node as no evaluated
     */
    public void resetEvaluationStatus() {
        evaluated = false;
    }

    /**
     * @return true if its CHANCE parents has been previously evaluated
     */
    public boolean isUpdatable() {
        //08/03/2023; Self-loop-->first extracted parents different from node
        return chanceRecordParents.stream().filter(chanceRecord -> !chanceRecord.equals(this)).allMatch(ChanceRecord::isEvaluated);
    }



    @Override
    public void setVariableValue(double variableValue) {
        this.evaluated = true;
        this.variableValue = variableValue;
    }


    @Override
    public String toString() {

        String result = super.toString();

        return result;
    }


    /**
     * @param chanceEvaluation
     */
    public void setChanceRecordParents(ChanceEvaluation chanceEvaluation) {
        chanceRecordParents = new ArrayList<>();
        chanceParents.forEach(chanceParent -> chanceRecordParents.add((ChanceRecord) chanceEvaluation.getDESRecord(chanceParent)));
    }

}
