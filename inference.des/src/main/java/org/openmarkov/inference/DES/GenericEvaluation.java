package org.openmarkov.inference.DES;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Abstract class for performing the part of DES simulation for DESnets corresponding to each NodeType
 *
 * @param <T> DESRecord descendants for creating the hashmap with the nodes for DES simulation
 * @author cmyago
 * @version 1.2 -11/04/2022 - textualLog comprobation moved to DESLogTextWriter
 */
public abstract class GenericEvaluation<T extends DESRecord> {


    /**
     * HashMap when any node is matches with its DESRecord
     */
    protected final HashMap<Node, T> desRecordHashMap;
    /**
     * Coordinates simulation
     */
    DESInference desInference;
    /**
     * List of the nodes with the type associated with T
     */
    List<Node> nodeList;

    /**
     * Decision taken in this simulation
     */
    Finding decisionFinding;

    /**
     * List with the orphanRecords for CHANCE and UTILITY nodes (The only orphan EVENT record is the initial one)
     */
    List<T> orphanRecords;




    /**
     * Constructor
     *
     * @param probNet      DESnet to be evaluated
     * @param nodeType     type of node to be simulated
     * @param recordClass  class of the DESrecord corresponding to nodeType
     * @param desInference
     * @throws OpenMarkovException thrown if simulation cannot be created
     */
    GenericEvaluation(ProbNet probNet, NodeType nodeType, Class<T> recordClass, DESInference desInference) throws OpenMarkovException {
        this.desInference = desInference;
        desRecordHashMap = new HashMap<>();
        nodeList = probNet.getNodes(nodeType);
        Node lastNode = null;
        try {
            Constructor<T> recordConstructor = recordClass.getDeclaredConstructor(Node.class);
            for (Node node : nodeList) {
                lastNode = node;
                desRecordHashMap.put(node, recordConstructor.newInstance(node));
            }

        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new UnreachableException(e);
        }
        setOrphanNodes();
    }


    protected void setOrphanNodes() {
        orphanRecords = new ArrayList<>();
        orphanRecords.addAll(getDesRecordHashMap().values().stream().filter(DESRecord::isOrphanNode).collect(Collectors.toList()));
    }

    /**
     * Prepares the T DESRecord for a new simulation by clearing it and setting the decisionFinding
     *
     * @param decisionFinding
     */
    void startSimulation(Finding decisionFinding) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        desRecordHashMap.values().forEach(T::clear);
        this.decisionFinding = decisionFinding;
    }


    /**
     * Finishes update of UtilityValues after utility has been computed and logs if applicable
     * FIXME finish log
     */
    void finishUpdate(Consumer<T> log) {
        desRecordHashMap.values().forEach(value -> {
//            if (value.isValueChanged()){
//                log.accept(value);
//                value.setValueChanged(false);
//            }
        });
    }

    /**
     * Updates the value of the variables when an event happens. Data from event happened is stored in eventRecord*
     *
     * @param eventHappened - Event happened data
     */
    abstract void update(EventRecord eventHappened) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther;

    /**
     * HashMap when any node is matches with its DESRecord
     */
    public HashMap<Node, T> getDesRecordHashMap() {
        return desRecordHashMap;
    }

    /**
     * Returns the ChanceRecord associated a chanceNode
     *
     * @param node
     * @return the DESRecord associated a chanceNode
     */
    DESRecord getDESRecord(Node node) {

        return getDesRecordHashMap().get(node);
    }

}
