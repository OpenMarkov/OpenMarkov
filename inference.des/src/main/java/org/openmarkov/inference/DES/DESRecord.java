package org.openmarkov.inference.DES;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.DESSimulablePotential;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Nodes simulation data.
 *
 * @author cmyago
 * @version 1.7 - 20/08/2022 - adapted to new use of random streams
 * @version 1.7.1 - 24/10/2023 - adapted to potentials with an indeterminate use of random numbers
 */
public class DESRecord {

    /**
     * Constant to test equality with two doubles.
     * TODO Check value
     */
    public final static double COMPARATOR_THRESHOLD = .00001;

    public final static String SEPARATOR = ";";
    /**
     * Manages the random number stream for performing DES
     */
    protected final DESRandomProvider desRandomProvider = new DESRandomProvider();
    /**
     * Node whose variable properties through simulation is recorded
     */
    final Node recordNode;
    /**
     * DESSimulablePotential associated to recordNode
     */
    final DESSimulablePotential recordPotential;
    /**
     * Variable whose properties are recorded here
     */
    final Variable recordVariable;
    /**
     * Type of recordVariable
     */
    final VariableType recordVariableType;
    /**
     * Time when recordVariable changes
     */
    protected double clock = 0;
    /**
     * List of Chance Parents
     * TODO in the next version Decision parents will be added
     */
    protected List<Node> chanceParents;
    /**
     * List of the Variable of Chance Parents
     * TODO in the next version Decision parents will be added
     */
    protected List<Variable> chanceParentsVariables;
    /**
     * List of Event Parents
     * TODO in the next version Decision parents will be added
     */
    protected List<Node> eventParents;
    /**
     * List of Event ancestors of the node
     */
    protected List<Node> eventAncestors;

//    /**
//     * Event Children of nodeRecord
//     */
//    protected List<Node> eventChildren;
    /**
     * Current value of RecordVariable in simulation
     * It is the value taken by chance and utility variables and the time of occurrence of events
     * FIXME 26/10/2023 Which value takes the event? Previously variableValue contained the time of ocurrence of the event; changed to its TTE
     * FIXME in the future it will contain the intervention
     * FIXME Use of Finding?
     */
    protected double variableValue = 0;
    /**
     * True if the DECISION Node is parent of recordNode
     */
    boolean decisionParent;


    public DESRecord(Node recordNode) {
        this.recordNode = recordNode;
        recordVariable = recordNode.getVariable();
        recordVariableType = recordVariable.getVariableType();
        //13/03/2023 - removing the necessity of an initial event.
//        if (recordNode.getPurpose().equals(PurposeType.INITIAL_EVENT.getName()) && recordNode.getVariable().getVariableType() == VariableType.EVENT)

//            recordPotential = new DistributionTablePotential(
//                    recordNode.getPotentials().get(0).getVariables()
//            );

//     else
    //13/03/2023 end

        recordPotential = (DESSimulablePotential) recordNode.getPotentials().getFirst();

        decisionParent = (getParentsByType(NodeType.DECISION).size() > 0);
        eventParents = getParentsByType(NodeType.EVENT);
        chanceParents = getParentsByType(NodeType.CHANCE);
        chanceParentsVariables = new ArrayList<>();
        for (Node chanceParent : chanceParents) {
            chanceParentsVariables.add(chanceParent.getVariable());
        }
        setEventAncestors();

    }

    public void clear() {
        variableValue = 0;
        clock = 0;
    }


    /**
     * Returns the children of the node whose NodeType is given by nodeType
     *
     * @param nodeType NodeType of the children
     * @return the children of the node whose NodeType is given by nodeType
     */
    public List<Node> getParentsByType(NodeType nodeType) {
        //For saving memory
        switch (nodeType) {
            case CHANCE:
                if (chanceParents != null) {
                    return chanceParents;
                }
                break;
            case EVENT:
                if (eventParents != null) {
                    return eventParents;
                }
                break;
        }
        return recordNode.getParents().stream().filter(node -> node.getNodeType() == nodeType).collect(Collectors.toList());
    }

    /**
     * Creates and populates eventAncestors
     */
    protected void setEventAncestors() {
        eventAncestors = new ArrayList<>();
        addEventAncestors(recordNode);
        eventAncestors = eventAncestors.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Gets the Event nodes from with there is a directed path to the node where nodes are only Chance nodes
     */
    protected void addEventAncestors(Node node) {
        List<Node> parents = node.getParents();
        if (parents.isEmpty()) {
            return;
        }
        for (Node parent : parents) {
            if (parent.getNodeType() == NodeType.EVENT) {
                eventAncestors.add(parent);
            } else if (!parent.equals(node)) {
                addEventAncestors(parent);
            }
        }

    }


    /**
     * Returns true if event is an ancestor of recordNode
     *
     * @param event event to check whether is an ancestor or not
     * @return true if event is an ancestor of recordNode
     */
    public boolean isEventAncestor(Node event) {
        return eventAncestors.stream().anyMatch(node -> node.equals(event));
    }

    /**
     * @return true if the node has no event ancestors
     */
    public boolean isOrphanNode() {
        return eventAncestors.isEmpty();
    }


    /**
     * @param possibleParent
     * @return true if possibleParent is the possibleParent of this
     */
    public boolean isParent(DESRecord possibleParent) {
        return recordNode.isParent(possibleParent.recordNode);
    }


    /**
     * Returns the value of the variable stored in this ChanceRecord.
     * If VariableType is VariableType.NUMERIC the real value is returned.
     * If VariableType is VariableType.FINITE_STATES the number of state is returned.
     *
     * @return the value of the variable stored in this ChanceRecord.
     */
    public double getVariableValue() {
        return variableValue;
    }


    /**
     * @param configuration
     */
    public void setVariableValue(Configuration configuration) {
        try {
            //24/10/2023; considering an indeterminate number of random numbers to sample the potential
//            variableValue = recordPotential.sampleConditionedVariable(desRandomProvider.getRandomNumber(), configuration);
            variableValue = recordPotential.sampleConditionedVariable(desRandomProvider.getRandomNumbers(recordPotential.numRandomNumbersNeeded()), configuration);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setVariableValue(double variableValue) {
        this.variableValue = variableValue;
    }


    @Override
    public String toString() {
        String result = "";
        result += "Node: " + recordNode.getName() + "\n";
        result += "Node type: " + recordNode.getNodeType().toString() + "\n";
        result += "Variable type: " + recordVariable.getVariableType().toString() + "\n";
        result += "Variable value: " + variableValue + "\n";
        return result;
    }


    /**
     * Node whose variable properties through simulation is recorded
     */
    public Node getRecordNode() {
        return recordNode;
    }

    /**
     * Gets recordVariable whose properties are recorded here
     */
    public Variable getRecordVariable() {
        return recordVariable;
    }


    /**
     * Time when recordVariable changes
     */
    public double getClock() {
        return clock;
    }

    public void setClock(double clock) {
        this.clock = clock;
    }


    /**
     * recordNode potential
     */
    public DESSimulablePotential getRecordPotential() {
        return recordPotential;
    }


    /**
     * True if the DECISION Node is parent of recordNode
     */
    public boolean hasDecisionParent() {
        return decisionParent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DESRecord desRecord = (DESRecord) o;
        return recordVariable.getName().equals(desRecord.recordVariable.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordVariable.getName());
    }
}
