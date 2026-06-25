/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.aic;

import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.metric.annotation.MetricType;
import org.openmarkov.learning.metric.entropy.EntropyMetric;

import java.util.HashMap;

/**
 * This class implements the AIC metric.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@MetricType(name = "AIC") public class AICMetric extends EntropyMetric {
    
    //Members
    /**
     * HashMap with the dimension of each node. (We do not want to
     * recalculate the dimension of all the nodes of the net every time
     * we make an edition)
     */
    protected HashMap<String, Double> nodesDimensions;
    
    public AICMetric() {
        super();
        nodesDimensions = new HashMap<String, Double>();
    }
    
    @Override public void init(ProbNet probNet, CaseDatabase caseDatabase) {
        super.init(probNet, caseDatabase);
        calculateDimension();
    }
    
    /**
     * Scores the associated network.
     *
     * @return {@code double} score of the net
     */
    @Override public double getScore() {
        return super.getScore() - calculateDimension();
    }
    
    /**
     * Scores the associated network with the link given in the received
     * edition added. We only have to recalculate the entropy and dimension
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     *
     * @param edition {@code AddLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is
     *                definitive (UndoableEditHappend called this method) or not.
     *
     * @return {@code double} score of the net with the given edition
     */
    @Override protected double score(AddLinkEdit edition, boolean change) {
        
        Node destinationNode = probNet.getNode(edition.getVariableTo());
        /* dimension of the node without adding the link */
        
        /* To calculate the dimension, we subtract the last dimension
         * of the destination node and sum the new dimension of this node*/
        double lastNodeDimension = nodesDimensions.get(destinationNode.getName());
        
        double newNodeDimension = (
                (
                        edition.getVariableFrom().
                               getNumStates()
                ) * lastNodeDimension
        );
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change) {
            nodesDimensions.put(destinationNode.getName(), newNodeDimension);
        }
        
        return super.score(edition, change) - (newNodeDimension - lastNodeDimension);
    }
    
    /**
     * Scores the associated network with the link given in the received
     * edition removed. We only have to recalculate the entropy and dimension
     * of the destination node. If an undoable edit happened (that is, if
     * parameter change is true) we update the entropy and dimension of the
     * destination node and the net.
     *
     * @param edition {@code AddLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is
     *                definitive (UndoableEditHappend called this method) or not.
     *
     * @return {@code double} score of the net with the given edition
     */
    @Override protected double score(RemoveLinkEdit edition, boolean change) {
        
        Node destinationNode = probNet.getNode(edition.getVariableTo());
        /* dimension of the node without adding the link */
        
        /* To calculate the dimension, we subtract the last dimension
         * of the destination node and sum the new dimension of this node*/
        double lastNodeDimension = nodesDimensions.get(destinationNode.getName());
        double newNodeDimension = lastNodeDimension / (
                (
                        edition.
                                getVariableFrom()
                ).getNumStates()
        );
        
        /*If change is true it's because we have to update the probNet values*/
        if (change) {
            nodesDimensions.put(destinationNode.getName(), newNodeDimension);
        }
        
        return super.score(edition, change) - (newNodeDimension - lastNodeDimension);
    }
    
    /**
     * Scores the associated network with the link given in the received
     * edition inverted. We have to recalculate the entropies and dimensions
     * of the destinations nodes before and after the inversion. If an undoable
     * edit happened (that is, if parameter change is true) we update the
     * entropies and dimensions of the destinations node and the net.
     *
     * @param edition {@code InvertLinkEdit}
     * @param change  {@code boolean} indicates whether the edition is
     *                definitive (UndoableEditHappend called this method) or not.
     *
     * @return {@code double} score of the net with the given edition
     */
    @Override protected double score(InvertLinkEdit edition, boolean change) {
        
        Node initialDestinationNode = probNet.getNode(edition.getVariableTo());
        Node initialOriginNode = probNet.getNode(edition.getVariableFrom());
        /* dimension of the node without adding the link */
        
        double lastNodeDimension = nodesDimensions.get(initialDestinationNode.getName());
        double newNodeDimension = lastNodeDimension / ((edition.getVariableFrom()).getNumStates());
        
        /*If change is true it's because we have to update the probNet values*/
        if (change) {
            nodesDimensions.put(initialDestinationNode.getName(), newNodeDimension);
        }
        
        double result = super.score(edition, change) - (newNodeDimension - lastNodeDimension);
        
        lastNodeDimension = nodesDimensions.get(initialOriginNode.getName());
        newNodeDimension = ((edition.getVariableFrom().getNumStates()) * lastNodeDimension);
        
        /*If change is true it's because we have to update the probNet values
         * and store the node dimension and entropy to avoid repeating the
         * calculations */
        if (change) {
            nodesDimensions.put(initialOriginNode.getName(), newNodeDimension);
        }
        
        result -= newNodeDimension - lastNodeDimension;
        return result;
    }
    
    /**
     * Calculates the dimension of the net as the sum of the dimensions
     * of each node. It is only used the first time we score the net, so we make
     * all the calculations.
     *
     * @return double dimension of the net
     */
    protected double calculateDimension() {
        double newDimension = 0;
        
        for (Node node : probNet.getNodes()) {
            newDimension += nodeDimension(node, true);
        }
        return newDimension;
    }
    
    /**
     * Calculates the dimension of the given node as the product of
     * its number of states and the number of possible configurations
     * of its parents.
     *
     * @param node   {@code Node} whose dimension we want to calculate.
     * @param change {@code boolean} indicates whether the edition is
     *               definitive (UndoableEditHappend called this method) or not.
     *
     * @return double dimension of this node
     */
    protected double nodeDimension(Node node, boolean change) {
        int numStates = node.getVariable().getNumStates();
        int parentsConfigurations = 1;
        if (node.getNumParents() != 0) {
            for (Node parent : node.getParents()) {
                parentsConfigurations *= parent.getVariable().getNumStates();
            }
        }
        double nodeDimension = (numStates - 1) * parentsConfigurations;
        
        /* Store the dimension of the node to avoid repeating the calculations*/
        if (change) {
            nodesDimensions.put(node.getName(), nodeDimension);
        }
        
        return nodeDimension;
    }
}
